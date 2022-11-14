package main

import (
	"bufio"
	"fmt"
	"log"
	"strings"
    "strconv"
    "time"
	"github.com/timescale/tsbs/load"
	"github.com/timescale/tsbs/pkg/data"
	"github.com/timescale/tsbs/pkg/data/usecases/common"
	"github.com/timescale/tsbs/pkg/targets"

)

type fileDataSource struct {
	scanner *bufio.Scanner
}
type decoder struct {
	scanner *bufio.Scanner
}

func (d *fileDataSource) NextItem() data.LoadedPoint {
	ok := d.scanner.Scan()
	if !ok && d.scanner.Err() == nil { // nothing scanned & no error = EOF
		return data.LoadedPoint{}
	} else if !ok {
		//fatal("scan error: %v", d.scanner.Err())
		return data.LoadedPoint{}
	}
	return data.NewLoadedPoint(d.scanner.Bytes())
}
func (d *fileDataSource) Headers() *common.GeneratedDataHeaders { return nil }

// Reads and returns a CSV line that encodes a data point.
// Since scanning happens in a single thread, we hold off on transforming it
// to an INSERT statement until it's being processed concurrently by a worker.

func (d *decoder) Decode(_ *bufio.Reader) data.LoadedPoint {
	ok := d.scanner.Scan()
	if !ok && d.scanner.Err() == nil { // nothing scanned & no error = EOF
		return data.NewLoadedPoint(nil)
	} else if !ok {
		log.Fatalf("scan error: %v", d.scanner.Err())
	}

    var err error
	parts := strings.Split(d.scanner.Text(), ",")
    r := reading{}
	r.host = parts[1]
    r.region = parts[2]
    r.datacenter = parts[3]
    r.rack, err = strconv.Atoi(parts[4])
    r.os = parts[5]
    r.arch = parts[6]
    r.team = parts[7]
    r.service,err = strconv.Atoi(parts[8])
    r.service_version,err = strconv.Atoi(parts[9])
    r.service_environment = parts[10]
    r.timestamp,err = strconv.ParseInt(parts[11], 10, 64 )
    r.usage_user, err  = strconv.ParseFloat(parts[12], 64)
    r.usage_system, err = strconv.ParseFloat(parts[13], 64)
    r.usage_idle, err = strconv.ParseFloat(parts[14], 64)
    r.usage_nice, err = strconv.ParseFloat(parts[15],64)
    r.usage_iowait, err = strconv.ParseFloat(parts[16],64)
    r.usage_irq, err = strconv.ParseFloat(parts[17],64)
    r.usage_irq_softirq, err = strconv.ParseFloat(parts[18],64)
    r.usage_steal, err = strconv.ParseFloat(parts[19],64)
    r.usage_guest, err = strconv.ParseFloat(parts[20],64)
    r.usage_guest_nice, err = strconv.ParseFloat(parts[21], 64)
 
    if(err != nil) {
        fmt.Printf("err=%v", err)
    }
 
	return data.NewLoadedPoint(r)
}

// Transforms a CSV string encoding a single metric into a CQL INSERT statement.
// We currently only support a 1-line:1-metric mapping for Cassandra. Implement
// other functions here to support other formats.

type reading struct {
    host string
    region string
    datacenter string
    rack int
    os string
    arch string
    team string
    service int
    service_version int
    service_environment string
    timestamp int64
    usage_user float64
    usage_system float64
    usage_idle float64
    usage_nice float64
    usage_iowait float64
    usage_irq float64
    usage_irq_softirq float64
    usage_steal float64
    usage_guest float64
    usage_guest_nice float64
}


/* func singleMetricToInsertStatement(text string) reading {

    var err error
	parts := strings.Split(text, ",")

    r := reading{}
	r.host = parts[1]
    r.region = parts[2]
    r.datacenter = parts[3]
    r.rack, err = strconv.Atoi(parts[4])
    r.os = parts[5]
    r.arch = parts[6]
    r.team = parts[7]
    r.service,err = strconv.Atoi(parts[8])
    r.service_version,err = strconv.Atoi(parts[9])
    r.service_environment = parts[10]
    r.timestamp,err = strconv.ParseInt(parts[11], 10, 64 )
    r.usage_user, err  = strconv.ParseFloat(parts[12], 64)
    r.usage_system, err = strconv.ParseFloat(parts[13], 64)
    r.usage_idle, err = strconv.ParseFloat(parts[14], 64)
    r.usage_nice, err = strconv.ParseFloat(parts[15],64)
    r.usage_iowait, err = strconv.ParseFloat(parts[16],64)
    r.usage_irq, err = strconv.ParseFloat(parts[17],64)
    r.usage_irq_softirq, err = strconv.ParseFloat(parts[18],64)
    r.usage_steal, err = strconv.ParseFloat(parts[19],64)
    r.usage_guest, err = strconv.ParseFloat(parts[20],64)
    r.usage_guest_nice, err = strconv.ParseFloat(parts[21], 64)
  
    if(err != nil) {
        fmt.Printf("err=%v", err)
    }
 

//    fmt.Printf("%v %v %v %v %v %v %v %v %v %v ", host , region, datacenter, rack, os, arch, team, service, service_version, service_environment)
//    fmt.Printf("%v %v %v %v %v %v %v %v %v %v %v\n", timestamp, usage_user, usage_system, usage_idle, usage_nice, usage_iowait, usage_irq, usage_irq_softirq, usage_steal, usage_guest, usage_guest_nice)

//    data := []interface{}{timestamp, usage_user, usage_system, usage_idle, usage_nice, usage_iowait, usage_irq, usage_irq_softirq, usage_steal, usage_guest, usage_guest_nice}
    return r
}
*/

type eventsBatch struct {
    hosts map[string][]interface{}
    measurements  map[string][][]interface{}
    count uint 
}

func (eb eventsBatch) Len() uint {
    return eb.count
}

func (eb eventsBatch) Append(item data.LoadedPoint) {
    data := item.Data.(reading)
    eb.hosts[data.host] = []interface{}{data.host, data.region, data.datacenter, data.rack, data.os, data.arch, data.team, data.service, data.service_version, data.service_environment}
    eb.measurements[data.host] = append(eb.measurements[data.host], []interface{}{time.Unix(0, data.timestamp), data.usage_user, data.usage_system, data.usage_idle, data.usage_nice, data.usage_iowait, data.usage_irq, data.usage_irq_softirq, data.usage_steal, data.usage_guest, data.usage_guest_nice} )
    eb.count++
}

type factory struct{}

func (f *factory) New() targets.Batch {
    eb := eventsBatch{}
    eb.count=0
    eb.measurements  = make(map[string][][]interface{})
    eb.hosts = make(map[string][]interface{})
    return eb
}
