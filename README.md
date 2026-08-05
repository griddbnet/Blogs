# Pushing 150,000 Rows per Second into GridDB Cloud with Fluentd

Fluentd is one of the most widely deployed log collectors in the world, and log ingestion is one of the workloads GridDB was built for: high-volume, append-heavy, time-stamped data. In this post we wire the two together using the official `fluent-plugin-griddb` output plugin and GridDB Cloud. Here's the major finding and headline: we went from 15,000 rows per second to a sustained 150,000 rows per second by changing one buffer parameter, and the thing that finally capped us was not GridDB at all.

Along the way we'll cover a realistic installation (the plugin targets an older Ruby, so there is some dependency archaeology involved), a small patch the plugin needs before its buffered mode works, and the container schema gotchas that the Web API's positional row format will happily punish you for.

## GridDB Cloud's Many Connectors

GridDB has steadily grown its connector ecosystem: Kafka, JDBC, Grafana, and a range of collection agents can all read from or write to GridDB, which means it usually slots into an existing data pipeline rather than demanding a new one. Fluentd is a natural addition to that list. It is the standard for log collection in a lot of Kubernetes and traditional server environments, it has hundreds of input and parser plugins, and once events are flowing through it, pointing them at GridDB is a single `<match>` block.

Everything in this post talks to GridDB Cloud through its Web API: JSON rows over HTTPS. The Web API that GridDB Cloud exposes works from anywhere without special client libraries, and, as the numbers below will show, with sensible batching it is nowhere near being the limiting factor in a real pipeline.

## Installation

Now let's begin with installation: `fluent-plugin-griddb` targets Ruby 2.7 and Fluentd 1.12. Ruby 2.7 predates OpenSSL 3, which is what modern Ubuntu ships, so Ruby will not compile against the system OpenSSL. The fix is to build OpenSSL 1.1.1 from source into its own prefix, build Ruby 2.7.2 against it with rbenv, and then pin RubyGems, Bundler, and Fluentd to the last versions that still support Ruby 2.7. It sounds worse than it is; each step is mechanical.

### 1. System dependencies

```bash
sudo apt update
sudo apt install -y git curl build-essential libreadline-dev zlib1g-dev \
    libyaml-dev libncurses5-dev libffi-dev libgdbm-dev
```

### 2. OpenSSL 1.1.1 from source

```bash
cd /tmp
wget https://www.openssl.org/source/openssl-1.1.1w.tar.gz
tar xvfz openssl-1.1.1w.tar.gz
cd openssl-1.1.1w
./config --prefix=/opt/openssl-1.1.1w --openssldir=/opt/openssl-1.1.1w shared zlib
make -j$(nproc)
sudo make install
```

One step that is easy to miss: the freshly built OpenSSL has an empty certificate directory, so anything compiled against it will fail TLS verification, including HTTPS calls to GridDB Cloud. Point it at the system certificate store:

```bash
sudo rm -rf /opt/openssl-1.1.1w/certs
sudo ln -s /etc/ssl/certs /opt/openssl-1.1.1w
```

### 3. rbenv and Ruby 2.7.2

Install rbenv, but pull `ruby-build` directly from GitHub. The version definitions that come with the packaged rbenv are stale and may not build 2.7.2 cleanly:

```bash
sudo apt install -y rbenv
mkdir -p "$(rbenv root)"/plugins
git clone https://github.com/rbenv/ruby-build.git "$(rbenv root)"/plugins/ruby-build

RUBY_CONFIGURE_OPTS="--with-openssl-dir=/opt/openssl-1.1.1w" rbenv install 2.7.2
rbenv global 2.7.2
echo 'eval "$(rbenv init - bash)"' >> ~/.bashrc
source ~/.bashrc
ruby --version   # should report 2.7.2
```

### 4. Fluentd and the plugin

Ruby 2.7 also needs older RubyGems and Bundler; the current releases of both have dropped support. Pin them, then install Fluentd 1.12.0 and the plugin gem:

```bash
gem update --system 3.4.22
gem install bundler -v 2.4.22
gem install fluentd --version "1.12.0" --no-doc
rbenv rehash

gem install --force --local fluent-plugin-griddb-1.0.2.gem
rbenv rehash
```

A quick `fluentd --version` confirms the pipeline pieces are in place.

## Patching the Plugin

Version 1.0.2 of the plugin has a latent bug: it works in its default, unbuffered mode, but the moment you add a `<buffer>` section to the match block (which you will want for any serious throughput), Fluentd switches the plugin to its buffered code path, and that path calls a method that does not exist:

```
error_class=NoMethodError error="undefined method `handle_record' for
#<Fluent::Plugin::GriddbOutput> Did you mean?  handle_create_container"
```

Inside `out_griddb.rb`, the unbuffered `process` path routes records through a method called `handle_insert_type`, which works. The buffered `write(chunk)` path calls `handle_record`, which was never implemented. Every chunk raises, gets flagged as a bad chunk, and is shunted to Fluentd's backup directory. The fix is one line: make the buffered path call the same method the working path uses.

```bash
sed -i 's/handle_record(record_array)/handle_insert_type(record_array)/' \
  "$(gem which fluent/plugin/out_griddb)"
```

Note the use of `gem which` to locate the file. If you have the plugin's source repository checked out as well as the gem installed, it is easy to edit the wrong copy and wonder why nothing changed. `gem which fluent/plugin/out_griddb` tells you exactly which file Fluentd loads.

Two related notes while we are in the plugin internals. First, the plugin's `insert_type` parameter accepts `single` and `multiple`, but `multiple` is currently a stub that logs "Currently insert type only support for single type" and does nothing, so leave it on `single`. Second, and this matters for the results later: even in `single` mode, the buffered path sends an entire buffer chunk in one HTTP request. So batch size is controlled by the buffer's chunk settings, not by the insert type.

## Creating the Target Container

The Web API inserts rows positionally: each row arrives as an array of values, matched to columns strictly by order. 

The event we will generate has six fields, plus a `seq` counter that Fluentd's sample input appends to the end of each record. Appends, as in: `seq` arrives as the last value in the row, so it must be the last column in the schema. 

The schema that matches what the plugin actually sends:

```bash
curl -X POST \
  -u "$GRIDDB_USER:$GRIDDB_PASS" \
  -H 'Content-Type: application/json' \
  "$GRIDDB_WEBAPI_URL/dbs/$GRIDDB_DB/containers" \
  -d '{
    "container_name": "fluentdStress",
    "container_type": "COLLECTION",
    "rowkey": false,
    "columns": [
      {"name": "host",   "type": "STRING"},
      {"name": "method", "type": "STRING"},
      {"name": "path",   "type": "STRING"},
      {"name": "code",   "type": "INTEGER"},
      {"name": "size",   "type": "INTEGER"},
      {"name": "agent",  "type": "STRING"},
      {"name": "seq",    "type": "LONG"}
    ]
  }'
```

We use a `COLLECTION` with `rowkey false` rather than a time-series container on purpose. A time-series container upserts on duplicate timestamps, and at tens of thousands of rows per second many events share a millisecond. A throughput test against a time-series container without a carefully unique key silently measures overwrites while reporting success. A rowkey-less collection accepts every insert as a distinct row, which is what we want to count.

## The Stress Configuration

For a throughput test, tailing a log file written by a generator makes the generator and the disk the bottleneck. Fluentd's built-in `sample` input creates events in memory instead, which lets us dial the input rate directly:

```
<system>
  workers 2
</system>

<source>
  @type sample
  tag stress.griddb
  size 200                # events per emit
  rate 50000              # events per second
  auto_increment_key seq  # unique trailing counter per event
  dummy {"host":"10.0.0.1","method":"GET","path":"/api/v1/users","code":200,"size":4096,"agent":"gridstress/1.0"}
</source>

<match stress.griddb>
  @type griddb
  host      "$GRIDDB_CLOUD_HOST"
  cluster   "$GRIDDB_CLUSTER"
  database  "$GRIDDB_DB"
  container "fluentdStress"
  insert_mode "append"
  insert_type "single"
  username  "$GRIDDB_USER"
  password  "$GRIDDB_PASS"

  <buffer>
    @type memory
    chunk_limit_records 50000    # rows per HTTP request
    chunk_limit_size    64MB
    flush_thread_count  8
    flush_mode          interval
    flush_interval      1s
    flush_thread_interval 0.1
    total_limit_size    4GB
    retry_max_times     3
    retry_type          periodic
    retry_wait          1s
    overflow_action     throw_exception
  </buffer>
</match>
```

The parameters worth understanding: `chunk_limit_records` sets how many rows travel in each PUT to the Web API, `flush_thread_count` sets how many of those requests run in parallel, and `workers` in the system block spreads the whole pipeline across CPU cores. Everything else is safety plumbing: `total_limit_size` bounds the memory buffer, and `overflow_action throw_exception` makes backpressure loud instead of silent, which is exactly what you want when the point of the exercise is to see what breaks.

## Measuring

Fluentd's own logs tell you what it sent; they do not tell you what the database kept. The only number that counts is the row count in the container, so we sample it on a fixed interval and compute the delta:

```bash
#!/usr/bin/env bash
# usage: ./sample.sh fluentdStress 10
CONTAINER="$1"; INTERVAL="${2:-10}"
prev=""
while true; do
  count=$(griddb-cloud-cli sql query -s "select count(*) from ${CONTAINER}" \
          | grep -o '"Value":[0-9]*' | grep -o '[0-9]*' | head -1)
  if [[ -n "$prev" ]]; then
    echo "$(date -u +%T)  total=${count}  rows/sec=$(( (count - prev) / INTERVAL ))"
  fi
  prev=$count
  sleep "$INTERVAL"
done
```

Confirmed rows per second, measured at the destination, is the metric.

## Results: Batch Size Is the Whole Game

All runs use the same event shape and the same 2-core client VM. The only variables changed between runs are the chunk size and the worker count.

| chunk_limit_records | workers | sustained rows/sec |
|---:|---:|---:|
| 5,000  | 1 | ~15,000  |
| 20,000 | 2 | ~116,000 |
| 50,000 | 2 | ~150,000 |

One things to note.

The leap from 15k to 116k. Growing the batch from 5,000 to 20,000 rows per request (and adding a second worker) produced nearly an 8x improvement. Going to 50,000-row chunks bought another ~30%, with clearly diminishing returns.

Eventually we reached a limit; `htop` on the client told the story: both CPU cores pinned at 98%+, both of them burned by the Fluentd Ruby workers serializing JSON and driving HTTPS. Meanwhile GridDB Cloud returned nothing but clean 200s the entire time: no 429s, no 503s, no rising latency, no divergence between rows sent and rows stored. Across every run, more than 30 million rows landed without a single error. We never found GridDB Cloud's ingest ceiling, because a 2-core Fluentd node cannot generate enough load to reach it.

## Takeaways

**Tune the buffer before anything else.** The difference between a naive Fluentd-to-GridDB config and a tuned one is an order of magnitude, and it is almost entirely `chunk_limit_records`. If your pipeline feels slow, your batches are almost certainly too small.

**The collector is your capacity limit, not the database.** Plan Fluentd capacity (cores, workers) as the scaling unit of the ingest tier. In our test the database side never registered strain; every ceiling we hit was client CPU.

**Measure at the destination.** Sent and stored are different numbers until proven otherwise. A count-based sampler is twenty lines of bash and turns "it seems fast" into a defensible figure.

The conclusion is the one that matters for anyone building a logging pipeline today: Fluentd plus GridDB Cloud, ingests at rates that make the database the least of your worries.