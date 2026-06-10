package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"os/signal"
	"regexp"
	"sort"
	"strconv"
	"strings"
	"syscall"

	"github.com/twmb/franz-go/pkg/kgo"
)

const (
	brokerAddr  = "localhost:9092"
	sourceTopic = "otel-metrics"
	groupID     = "otel-griddb-bridge"
	topicPrefix = "metric_"
)

// OTLP JSON structures — only the fields we actually use.
type otlpMetrics struct {
	ResourceMetrics []resourceMetric `json:"resourceMetrics"`
}

type resourceMetric struct {
	Resource     resource      `json:"resource"`
	ScopeMetrics []scopeMetric `json:"scopeMetrics"`
}

type resource struct {
	Attributes []attr `json:"attributes"`
}

type scopeMetric struct {
	Metrics []metric `json:"metrics"`
}

type metric struct {
	Name      string      `json:"name"`
	Unit      string      `json:"unit"`
	Gauge     *dataSeries `json:"gauge,omitempty"`
	Sum       *dataSeries `json:"sum,omitempty"`
	Histogram *anything   `json:"histogram,omitempty"` // we skip these
}

type dataSeries struct {
	DataPoints []dataPoint `json:"dataPoints"`
}

type dataPoint struct {
	TimeUnixNano string  `json:"timeUnixNano"`
	AsDouble     float64 `json:"asDouble,omitempty"`
	AsInt        string  `json:"asInt,omitempty"` // OTLP sends ints as strings
	Attributes   []attr  `json:"attributes"`
}

type attr struct {
	Key   string    `json:"key"`
	Value attrValue `json:"value"`
}

type attrValue struct {
	StringValue string  `json:"stringValue,omitempty"`
	IntValue    string  `json:"intValue,omitempty"`
	DoubleValue float64 `json:"doubleValue,omitempty"`
	BoolValue   bool    `json:"boolValue,omitempty"`
}

type anything struct{} // placeholder for fields we skip

// Connect-envelope JSON output shape that the GridDB sink expects.
type connectEnvelope struct {
	Schema  connectSchema `json:"schema"`
	Payload flatRow       `json:"payload"`
}

type connectSchema struct {
	Type   string         `json:"type"`
	Fields []connectField `json:"fields"`
	Name   string         `json:"name"`
}

type connectField struct {
	Type     string `json:"type"`
	Optional bool   `json:"optional"`
	Field    string `json:"field"`
}

type flatRow struct {
	Datetime int64   `json:"datetime"`
	Value    float64 `json:"value"`
	Host     string  `json:"host"`
	Unit     string  `json:"unit"`
	Attrs    string  `json:"attrs"`
}

// Fixed schema used for every metric record.
var flatSchema = connectSchema{
	Type: "struct",
	Name: "otel_metric",
	Fields: []connectField{
		{Type: "int64", Optional: false, Field: "datetime"},
		{Type: "double", Optional: false, Field: "value"},
		{Type: "string", Optional: true, Field: "host"},
		{Type: "string", Optional: true, Field: "unit"},
		{Type: "string", Optional: true, Field: "attrs"},
	},
}

// GridDB container names: alphanumeric + underscore. Sanitize metric name.
var sanitizer = regexp.MustCompile(`[^a-zA-Z0-9_]`)

func metricToTopic(name string) string {
	return topicPrefix + sanitizer.ReplaceAllString(name, "_")
}

func attrsToString(attrs []attr) string {
	parts := make([]string, 0, len(attrs))
	for _, a := range attrs {
		v := a.Value.StringValue
		if v == "" && a.Value.IntValue != "" {
			v = a.Value.IntValue
		}
		parts = append(parts, fmt.Sprintf("%s=%s", a.Key, v))
	}
	sort.Strings(parts) // deterministic
	return strings.Join(parts, ",")
}

func hostFromResource(res resource) string {
	for _, a := range res.Attributes {
		if a.Key == "host.name" {
			return a.Value.StringValue
		}
	}
	return ""
}

func dpValue(dp dataPoint) (float64, bool) {
	if dp.AsInt != "" {
		v, err := strconv.ParseFloat(dp.AsInt, 64)
		if err != nil {
			return 0, false
		}
		return v, true
	}
	return dp.AsDouble, true
}

// Walk one OTLP message and produce flat envelopes per data point.
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
				// Pick whichever series is set (gauge or sum)
				var series *dataSeries
				switch {
				case m.Gauge != nil:
					series = m.Gauge
				case m.Sum != nil:
					series = m.Sum
				case m.Histogram != nil:
					// histograms not supported in v1 of bridge
					continue
				default:
					continue
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
					millis := nanos / 1_000_000

					envelope := connectEnvelope{
						Schema: flatSchema,
						Payload: flatRow{
							Datetime: millis,
							Value:    val,
							Host:     host,
							Unit:     m.Unit,
							Attrs:    attrsToString(dp.Attributes),
						},
					}
					b, err := json.Marshal(envelope)
					if err != nil {
						continue
					}
					out = append(out, &kgo.Record{Topic: topic, Value: b})
				}
			}
		}
	}
	return out, nil
}

func main() {
	ctx, cancel := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer cancel()

	client, err := kgo.NewClient(
		kgo.SeedBrokers(brokerAddr),
		kgo.ConsumerGroup(groupID),
		kgo.ConsumeTopics(sourceTopic),
		kgo.AllowAutoTopicCreation(), // bridge auto-creates the per-metric topics on first produce
	)
	if err != nil {
		log.Fatalf("new client: %v", err)
	}
	defer client.Close()

	log.Printf("bridge running. consuming %q, producing to %s* topics", sourceTopic, topicPrefix)

	for {
		fetches := client.PollFetches(ctx)
		if errs := fetches.Errors(); len(errs) > 0 {
			if ctx.Err() != nil {
				log.Printf("shutting down")
				return
			}
			for _, e := range errs {
				log.Printf("fetch error: topic=%s partition=%d err=%v", e.Topic, e.Partition, e.Err)
			}
			continue
		}

		iter := fetches.RecordIter()
		for !iter.Done() {
			rec := iter.Next()
			out, err := explode(rec.Value)
			if err != nil {
				log.Printf("explode error (offset=%d): %v", rec.Offset, err)
				continue
			}
			if len(out) == 0 {
				continue
			}
			// Synchronous produce — keeps things simple, fine for this scale
			results := client.ProduceSync(ctx, out...)
			for _, r := range results {
				if r.Err != nil {
					log.Printf("produce error: %v", r.Err)
				}
			}
			log.Printf("offset=%d exploded into %d records", rec.Offset, len(out))
		}
	}
}
