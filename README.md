mkdir -p ~/go/src/github.com/influxdata
cd ~/go/src/github.com/influxdata
git clone https://github.com/influxdata/telegraf.git
cd telegraf

# Copy the GridDB plugin source from the Toshiba bundle
cp -r /path/to/telegraf-output-plugin/plugins/outputs/griddb \
      plugins/outputs/griddb

Create plugins/outputs/all/griddb.go:

```golang
//go:build !custom || outputs || outputs.griddb

package all

import _ "github.com/influxdata/telegraf/plugins/outputs/griddb" // register plugin
```

```bash
$ make telegraf
$ ./telegraf --output-list | grep griddb
```

```bash
cp ./telegraf ~/development/griddb/26a-content-creation/blogs/3_telegraf_logstash/sample/

cd ~/development/griddb/26a-content-creation/blogs/3_telegraf_logstash/sample
export GRIDDB_PASSWORD='your-password'
./telegraf --config griddb.conf --test    # dry run
./telegraf --config griddb.conf --once    # single write to GridDB
./telegraf --config griddb.conf           # run continuously
```


## Logstash

```bash
mkdir -p ~/logstash-demo && cd ~/logstash-demo
curl -O https://artifacts.elastic.co/downloads/logstash/logstash-9.4.1-darwin-aarch64.tar.gz
tar -xzf logstash-9.4.1-darwin-aarch64.tar.gz
mv logstash-9.4.1 logstash
./logstash/bin/logstash --version
```

```bash
cp ~/Downloads/GridDB_Cloud_doc_lib_for_paidplan\ 3/logstash-output-plugin/logstash-output-griddb-1.0.0.gem ./

./logstash/bin/logstash-plugin install ./logstash-output-griddb-1.0.0.gem
```

```bash
./logstash/bin/logstash-plugin list | grep griddb

mkdir -p logs
curl -L -o logs/nginx-access.log \
  https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/apache_logs/apache_logs

```

```bash
./logstash/bin/logstash -f ~/logstash-demo/nginx-to-griddb.conf
```


## Grafana

Install grafana (v10 or below)

must inject password for web api url through REST API becuse the form seems broken