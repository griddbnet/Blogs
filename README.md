# Storing OpenTelemetry Metrics, Traces, and Logs in GridDB Cloud with Kafka

OpenTelemetry (OTel) is an open-source, vendor-neutral observability framework for cloud-native software. Rather than tying an application to a single monitoring vendor, it defines a common way to generate, collect, and export telemetry across three signal types: metrics, traces, and logs; commonly called the three pillars of observability. Metrics are numeric measurements sampled over time, such as CPU utilization or request counts. Traces describe the path of a single request as it moves through a system, broken into individual units of work called *spans*. Logs are timestamped event records emitted by the application.

The value of the three pillars comes from being able to correlate across them: spotting a latency spike in a trace, then jumping to the exact log lines emitted during that request. Realizing that benefit requires the signals to live somewhere you can query them together, which is where GridDB Cloud comes in. All three signals are, at their core, streams of timestamped events, making them a natural fit for a time-series database.

We have written extensively about pairing [Kafka with GridDB](https://www.griddb.net/en/?s=kafka&lang=en), because a data-ingestion pipeline like Kafka fits well with what GridDB is built for. Previously we used the GridDB Kafka Connector to push time-series data to GridDB Cloud over the Web API. With the release of GridDB Cloud v3.2, we can now connect to a cloud instance natively, without the Web API, which opens up a broader set of tools. In this article, we will use that native connection to land all three OpenTelemetry signals into GridDB Cloud through Kafka. We will collect host metrics from a local machine, instrument a small application to emit traces and logs, route everything through Kafka, flatten the nested OTLP payloads with a Go bridge, and sink each signal into its own GridDB Cloud `TIME_SERIES` container using the GridDB Kafka Connector. We will then write queries that analyze the data, per-operation latency profiles, error rates, metric summaries, and a cross-signal lookup that connects a failing trace to the logs it produced.

GridDB's design favors one container per series, `TIME_SERIES` containers are optimized per series, and a query scoped to a single container is faster than filtering one large mixed container. That principle shapes the whole pipeline: rather than dumping raw OTLP into one place, we explode it into one container per metric, plus one container each for spans and logs.

At a high level, this is what we will build:

1. Set up the OpenTelemetry Collector to scrape host metrics and receive application traces and logs, pushing each signal to Kafka.
2. Set up Kafka to receive and store the raw OTLP data.
3. Run a Go "bridge" that reads the dense, nested OTLP JSON and explodes it into flat, one-row-per-event topics — one per metric, one for spans, one for logs.
4. Use the GridDB Kafka Connector to sink those flattened topics into GridDB Cloud.

```
host metrics ─┐
                  ├─► OTel Collector ─► Kafka  [otel-metrics] [otel-traces] [otel-logs]
Instrumented app ─┘                                       │
                                                       Go bridge  (flatten nested OTLP)
                                                          │
        Kafka  [metric_system_cpu_utilization, ...]  [otel_spans]  [otel_logs]
                                                          │
                                              Kafka Connect (GridDB sink)
                                                          │
                          GridDB Cloud — one TIME_SERIES container per signal
```

One naming detail to note up front: the raw input topics use hyphens (`otel-metrics`, `otel-traces`, `otel-logs`) and the bridge's flattened output topics use underscores (`metric_*`, `otel_spans`, `otel_logs`). The output names double as GridDB container names, and the hyphen/underscore split lets the sink's topic filter target only the flattened topics.

## OpenTelemetry

Install OpenTelemetry from [opentelemetry.io](https://opentelemetry.io/). In this case the Collector was installed on bare metal. Once installed, configure `config.yaml` to describe how the Collector should behave. The configuration below sets up all three pillars: the `hostmetrics` scraper produces metrics, while the `otlp` receiver accepts traces and logs from an instrumented application. Each pipeline exports to Kafka with OTLP-JSON encoding:

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318
  hostmetrics:
    collection_interval: 10s
    scrapers:
      cpu:
        metrics:
          system.cpu.utilization:
            enabled: true
      memory:
        metrics:
          system.memory.utilization:
            enabled: true
      load:
      disk:

processors:
  resourcedetection:
    detectors: [system]
    system:
      hostname_sources: [os]
  batch:
    timeout: 5s
    send_batch_size: 100

exporters:
  kafka:
    brokers:
      - localhost:9092
    metrics:
      topic: otel-metrics
      encoding: otlp_json
    traces:
      topic: otel-traces
      encoding: otlp_json
    logs:
      topic: otel-logs
      encoding: otlp_json
  debug:
    verbosity: basic

service:
  pipelines:
    metrics:
      receivers: [otlp, hostmetrics]
      processors: [resourcedetection, batch]
      exporters: [kafka, debug]
    traces:
      receivers: [otlp]
      processors: [resourcedetection, batch]
      exporters: [kafka, debug]
    logs:
      receivers: [otlp]
      processors: [resourcedetection, batch]
      exporters: [kafka, debug]
```

The `hostmetrics` scraper is the metrics source, configured at the bottom of the file in the `metrics` pipeline; it exports to the `otel-metrics` topic. The `resourcedetection` processor attaches the host's name to every signal as a resource attribute. Start the Collector by pointing it at the config:

```bash
otelcol-contrib --config ~/otel-griddb/config.yaml
```

### Instrumenting an Application for Traces and Logs

Host metrics arrive on their own through the scraper, but in a real world setting, you'd want to be collecting traces and logs coming from the application you're monitoring. To mimic this sort of real-world-use-case, we have set up a small Python worker that simulates processing jobs and instrument it with OpenTelemetry's auto-instrumentation. Auto-instrumentation works well in this case because the application does not need an SDK wired in by hand; the `opentelemetry-instrument` launcher configures everything based on the environment variables including the providers, exporters, and the logging handler.

A representative worker:

```python
import logging
import random
import time
import uuid

from opentelemetry import trace

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger("job-worker")
tracer = trace.get_tracer("job-worker")

JOB_TYPES = ["transcode_video", "resize_image", "send_email", "build_report"]


def process_job(job_type: str, job_id: str) -> None:
    with tracer.start_as_current_span(job_type) as span:
        span.set_attribute("job.id", job_id)
        span.set_attribute("job.type", job_type)

        duration = random.uniform(0.05, 1.5)
        log.info("job %s (%s) started", job_id, job_type)
        time.sleep(duration)

        if random.random() < 0.1:  # occasionally fail, so the data has errors to query
            span.set_status(trace.Status(trace.StatusCode.ERROR, "job failed"))
            log.error("job %s (%s) failed after %.2fs", job_id, job_type, duration)
            return

        log.info("job %s (%s) completed in %.2fs", job_id, job_type, duration)


def main() -> None:
    log.info("worker starting")
    while True:
        job_type = random.choice(JOB_TYPES)
        process_job(job_type, uuid.uuid4().hex[:8])
        time.sleep(random.uniform(0.2, 0.8))


if __name__ == "__main__":
    main()
```

Telemetry destinations are set through environment variables, which the launcher reads to decide where to send spans and logs:

```bash
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
export OTEL_SERVICE_NAME=job-worker
export OTEL_TRACES_EXPORTER=otlp
export OTEL_LOGS_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=none
```

Run the worker under the launcher:

```bash
opentelemetry-instrument python worker.py
```

As a tip, you can keep these exports in a file (for example `otel-env.sh`) and load them with `source otel-env.sh`. Pasting a multi-line block directly into a shell can silently corrupt the values if the newlines arrive as the literal characters `\n` — each variable then absorbs the leftover `export` keyword, producing names such as `otlpnexport` that fail at startup with `Requested component 'otlpnexport' not found`. Sourcing a file avoids the issue.

The `OTEL_SERVICE_NAME` value is attached to every span and log as the `service.name` resource attribute, which the bridge promotes to a dedicated column.

## Kafka

This setup uses the KRaft build of Kafka, which no longer requires a separate ZooKeeper process. Install Kafka and start it by pointing at the default config; installed via Homebrew, it starts like this:

```bash
kafka-server-start $(brew --prefix)/etc/kafka/kraft/server.properties
```

### Kafka Plugins and the GridDB Sink

Wiring Kafka to GridDB Cloud requires the GridDB connector and its supporting `gridstore` JARs. Gather them into a directory of your choosing — here, a `griddb` subdirectory under a `kafka-plugins` folder in the home directory:

```bash
➜  ls ~/kafka-plugins/griddb
griddb-kafka-connector-0.6.jar        gridstore-conf-5.8.0.jar
gridstore-5.8.0.jar                   gridstore-jdbc-5.8.0.jar
gridstore-advanced-5.8.0.jar          gridstore-jdbc-call-logging-5.8.0.jar
gridstore-call-logging-5.8.0.jar
```

These JARs are gathered from the [GridDB Cloud v3.2](https://www.griddb.net/en/blog/connecting-to-griddb-cloud-v3-2-from-your-local-dev-environment-no-vpn-no-vnet-peering/) support page. You will also need to build the GridDB Kafka Connector from [this fork](https://github.com/Imisrael/griddb-kafka-connect), which adds support for the `connection.route` and `database` configuration properties required for GridDB Cloud's native connection.



### Kafka Connect

Kafka Connect needs a `connect-standalone.properties` worker configuration. Because the bridge emits Kafka Connect envelopes that carry an explicit schema, the value converter must have schemas enabled:

```properties
# connect-standalone.properties

bootstrap.servers=localhost:9092

key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=false
value.converter.schemas.enable=true

offset.storage.file.filename=/tmp/connect.offsets
offset.flush.interval.ms=10000

# Directory above the griddb folder; Connect scans subdirectories for plugins
plugin.path=/Users/israelimru/kafka-plugins

rest.port=8083
```

The sink configuration tells Connect how to push data to GridDB Cloud. A single sink handles all three signal types: every flattened topic — metrics, spans, and logs — carries a `datetime` field and maps to a `TIME_SERIES` container, so one topic filter and one timestamp transform cover them all. The `TimestampConverter` is essential: it coerces the integer epoch-millis `datetime` into a real `Timestamp`, which is what makes GridDB create a `TIME_SERIES` container keyed on time rather than a collection with a plain integer column.

```properties
name=griddb-otel-sink
connector.class=com.github.griddb.kafka.connect.GriddbSinkConnector
tasks.max=1

cluster.name=[yourClusterName]
user=[yourUser]
password=[yourPassword]
multicast=false
notification.provider.url=[yourNotificationProviderURL]

connection.route=PUBLIC
database=[yourDatabase]

container.type=TIME_SERIES
topics.regex=metric_.*|otel_.*

# Coerce the int64 epoch-millis datetime into a Timestamp before mapping,
# so datetime becomes the TIMESTAMP row key of each TIME_SERIES container.
transforms=TimestampConverter
transforms.TimestampConverter.type=org.apache.kafka.connect.transforms.TimestampConverter$Value
transforms.TimestampConverter.field=datetime
transforms.TimestampConverter.target.type=Timestamp
```



The `topics.regex=metric_.*|otel_.*` pattern matches the underscore output topics (`metric_system_cpu_utilization`, `otel_spans`, `otel_logs`) while ignoring the hyphenated raw inputs. Start the Connect worker with both files:

```bash
connect-standalone ~/otel-griddb/connect-standalone.properties ~/otel-griddb/griddb-otel-sink.properties
```

The `connect-standalone` command runs the Kafka Connect worker — the link between Kafka topics and external systems. It loads the GridDB sink plugin and starts the job defined in the sink properties, which consumes from the matching topics and writes each record as a row in GridDB Cloud.

## The Go Bridge

OTLP JSON is deeply nested. A metrics message wraps `resourceMetrics → scopeMetrics → metrics → dataPoints`; a traces message wraps `resourceSpans → scopeSpans → spans`; logs follow the same shape. GridDB stores flat rows, so the bridge reads each raw OTLP topic and explodes it into flat, one-row-per-event records, each wrapped in a Kafka Connect envelope carrying its schema. The main loop dispatches on the source topic:

```go
switch rec.Topic {
case metricsTopic:                  // otel-metrics
    produced, err = explode(rec.Value)
case tracesTopic:                   // otel-traces
    produced, err = explodeTraces(rec.Value)
case logsTopic:                     // otel-logs
    produced, err = explodeLogs(rec.Value)
}
```

### Metrics

The metrics handler walks down to each data point and emits one flat row per point, routing it to a per-metric topic (`metricToTopic` turns `system.cpu.utilization` into `metric_system_cpu_utilization`):

```go
func explode(payload []byte) ([]*kgo.Record, error) {
	var o otlpMetrics
	if err := json.Unmarshal(payload, &o); err != nil {
		return nil, fmt.Errorf("unmarshal otlp: %w", err)
	}

	var out []*kgo.Record
	for _, rm := range o.ResourceMetrics {
		host := hostFromResource(rm.Resource)
		for _, sm := range rm.ScopeMetrics {
			for _, m := range sm.Metrics {
				var series *dataSeries
				switch {
				case m.Gauge != nil:
					series = m.Gauge
				case m.Sum != nil:
					series = m.Sum
				default:
					continue // histograms not supported
				}

				topic := metricToTopic(m.Name)
				for _, dp := range series.DataPoints {
					val, ok := dpValue(dp)
					if !ok {
						continue
					}
					nanos, err := strconv.ParseInt(dp.TimeUnixNano, 10, 64)
					if err != nil {
						continue
					}
					envelope := connectEnvelope{
						Schema: flatSchema,
						Payload: flatRow{
							Datetime: nanos / 1_000_000,
							Value:    val,
							Host:     host,
							Unit:     m.Unit,
							Attrs:    attrsToString(dp.Attributes),
						},
					}
					b, _ := json.Marshal(envelope)
					out = append(out, &kgo.Record{Topic: topic, Value: b})
				}
			}
		}
	}
	return out, nil
}
```

### Traces

The traces handler pulls `service.name` and `host.name` from the resource, iterates down to individual spans, converts nanosecond timestamps to milliseconds, and computes each span's duration from its start and end times:

```go
func explodeTraces(payload []byte) ([]*kgo.Record, error) {
	var o otlpTraces
	if err := json.Unmarshal(payload, &o); err != nil {
		return nil, fmt.Errorf("unmarshal otlp traces: %w", err)
	}

	var out []*kgo.Record
	for _, rs := range o.ResourceSpans {
		host := resourceAttr(rs.Resource, "host.name")
		service := resourceAttr(rs.Resource, "service.name")
		for _, ss := range rs.ScopeSpans {
			for _, s := range ss.Spans {
				startNs, err1 := strconv.ParseInt(s.StartTimeUnixNano, 10, 64)
				endNs, err2 := strconv.ParseInt(s.EndTimeUnixNano, 10, 64)
				if err1 != nil || err2 != nil {
					continue
				}
				envelope := spanEnvelope{
					Schema: spanSchema,
					Payload: spanRow{
						Datetime:     startNs / 1_000_000,
						DurationMs:   (endNs - startNs) / 1_000_000,
						TraceID:      s.TraceID,
						SpanID:       s.SpanID,
						ParentSpanID: s.ParentSpanID,
						Name:         s.Name,
						Kind:         s.Kind,
						StatusCode:   s.Status.Code,
						ServiceName:  service,
						Host:         host,
						Attrs:        attrsToString(s.Attributes),
					},
				}
				b, _ := json.Marshal(envelope)
				out = append(out, &kgo.Record{Topic: spansContainer, Value: b})
			}
		}
	}
	return out, nil
}
```

Its schema leaves `datetime` as `int64`; the sink's `TimestampConverter` promotes it to a GridDB `TIMESTAMP` row key:

```go
var spanSchema = connectSchema{
	Type: "struct",
	Name: "otel_span",
	Fields: []connectField{
		{Type: "int64", Optional: false, Field: "datetime"},
		{Type: "int64", Optional: false, Field: "duration_ms"},
		{Type: "string", Optional: true, Field: "trace_id"},
		{Type: "string", Optional: true, Field: "span_id"},
		{Type: "string", Optional: true, Field: "parent_span_id"},
		{Type: "string", Optional: true, Field: "name"},
		{Type: "int32", Optional: true, Field: "kind"},
		{Type: "int32", Optional: true, Field: "status_code"},
		{Type: "string", Optional: true, Field: "service_name"},
		{Type: "string", Optional: true, Field: "host"},
		{Type: "string", Optional: true, Field: "attrs"},
	},
}
```

### Logs

The logs handler is structurally identical, walking `resourceLogs → scopeLogs → logRecords` and emitting one flat row per record to the `otel_logs` topic. Each row carries the record's severity number and text, its body, the `service.name`/`host.name` from the resource, and — critically — the `trace_id` and `span_id` that tie the log back to the span that produced it. That correlation key is the reason for landing logs and traces in the same database, and we use it in the queries below.

Build and run the bridge:

```bash
go build -o bridge
./bridge
```

It logs each input record it explodes:

```
2026/05/15 13:31:36 topic=otel-metrics offset=150 exploded into 58 records
2026/05/15 13:31:37 topic=otel-traces  offset=151 exploded into 12 records
2026/05/15 13:31:37 topic=otel-logs    offset=149 exploded into 9 records
```

## Running Everything

With every piece in place, start the components in order:

1. Kafka (KRaft)
2. OTel Collector
3. The instrumented worker (`opentelemetry-instrument python worker.py`)
4. The Go bridge
5. Kafka Connect (`connect-standalone`)

Once running, Kafka Connect reports writing rows to GridDB Cloud, with a container per metric plus the span and log containers:

```
[griddb-otel-sink|task-0] Put 1 record to buffer of container metric_system_cpu_load_average_15m
[griddb-otel-sink|task-0] Put 1 record to buffer of container metric_system_disk_io
[griddb-otel-sink|task-0] Put 1 record to buffer of container otel_spans
[griddb-otel-sink|task-0] Put 1 record to buffer of container otel_logs
```

In the GridDB Cloud console, the containers appear as `TIME_SERIES`. The `otel_spans` container holds one row per span — `datetime` (the span start) as the `TIMESTAMP` row key, plus `duration_ms`, the ID fields (`trace_id`, `span_id`, `parent_span_id`), the operation `name`, `kind`, `status_code`, `service_name`, `host`, and a flattened `attrs` string. The `otel_logs` container holds one row per log record, keyed on `datetime`, with `severity_number`, `severity_text`, `body`, `trace_id`, `span_id`, `service_name`, `host`, and `attrs`. Each `metric_*` container holds `datetime`, `value`, `host`, `unit`, and `attrs`.

## Querying the Data

Storing telemetry is only useful if you can ask questions of it. The following queries move from simple feeds to genuine analysis, one pillar at a time, and finish by crossing between them.

### Metrics

For a single metric, a one-line aggregate summarizes its range over the whole collection period — useful for spotting how hot the CPU has run:

```sql
SELECT MIN(value) AS min_util,
       AVG(value) AS avg_util,
       MAX(value) AS max_util
FROM metric_system_cpu_utilization;
```

The latest readings give a live view of a single series:

```sql
SELECT datetime, value, host
FROM metric_system_cpu_utilization
ORDER BY datetime DESC
LIMIT 20;
```

### Traces

A few conventions matter for the trace and log queries. A span's `status_code` is `0` for UNSET, `1` for OK, and `2` for ERROR. A log record's `severity_number` follows fixed ranges: 1–4 TRACE, 5–8 DEBUG, 9–12 INFO, 13–16 WARN, 17–20 ERROR, 21–24 FATAL.

The first real question for any traced system is where the time is going. Because each span carries its own `duration_ms`, a single grouped query produces a full latency profile per operation — minimum, average, and maximum duration with the call count:

```sql
SELECT name,
       COUNT(*)         AS calls,
       MIN(duration_ms) AS min_ms,
       AVG(duration_ms) AS avg_ms,
       MAX(duration_ms) AS max_ms
FROM otel_spans
GROUP BY name
ORDER BY avg_ms DESC;
```

A high `max_ms` against a low `avg_ms` points to intermittent outliers. To inspect those outliers directly, sort by duration:

```sql
SELECT datetime, name, duration_ms, trace_id
FROM otel_spans
ORDER BY duration_ms DESC
LIMIT 10;
```

Reliability is the next question. Since `status_code = 2` marks an errored span, error counts per operation come from a single filtered aggregate:

```sql
SELECT name, COUNT(*) AS errors
FROM otel_spans
WHERE status_code = 2
GROUP BY name
ORDER BY errors DESC;
```

### Logs

A severity breakdown gives a quick health summary:

```sql
SELECT severity_text, COUNT(*) AS count
FROM otel_logs
GROUP BY severity_text
ORDER BY count DESC;
```

Filtering on the severity number isolates everything at ERROR level or above:

```sql
SELECT datetime, severity_text, service_name, body
FROM otel_logs
WHERE severity_number >= 17
ORDER BY datetime DESC
LIMIT 20;
```

### Crossing Signals

The most compelling result of collectingg traces and logs in the same database is correlation. Suppose the error query above flags an operation that fails often. We can take its most recent failing trace and pull every log record emitted during that exact request, joined on `trace_id`. First identify the trace:

```sql
SELECT trace_id, name, duration_ms
FROM otel_spans
WHERE status_code = 2
ORDER BY datetime DESC
LIMIT 1;
```

Then retrieve its logs in order:

```sql
SELECT datetime, severity_text, body, span_id
FROM otel_logs
WHERE trace_id = '<trace_id from the previous result>'
ORDER BY datetime;
```

In two short queries we go from "this operation is failing" to the precise log lines explaining why  an investigation that typically spans two separate systems, performed here against one database.


## Conclusion

With this pipeline in place, all three OpenTelemetry signals land in GridDB Cloud through a single path: the Collector handles ingestion and fan-out to Kafka, the Go bridge flattens nested OTLP into one clean row per event, and the GridDB Kafka Connector — using GridDB Cloud v3.2's native connection — sinks each signal into a purpose-built `TIME_SERIES` container. Because the data is queryable with standard aggregates and the trace identifier is shared between spans and logs, GridDB becomes a single backend for the correlated analysis that observability work depends on: metric summaries, latency profiling, error attribution, and trace-to-log drilldown.