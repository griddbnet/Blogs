// bulk_load_cassandra loads a Cassandra daemon with data from stdin.
//
// The caller is responsible for assuring that the database is empty before
// bulk load.
package main

import (
	"bufio"
//	"flag"
	"fmt"
//	"log"
	"os"
    "strconv"
	"time"
    "sync"
	"github.com/griddb/go_client"
	"github.com/timescale/tsbs/internal/utils"
	"github.com/timescale/tsbs/load"
	"github.com/timescale/tsbs/pkg/targets"
	"github.com/timescale/tsbs/pkg/targets/constants"
	"github.com/timescale/tsbs/pkg/targets/initializers"

)

// Program option vars:
var (
	hosts             string
	replicationFactor int
	consistencyLevel  string
	writeTimeout      time.Duration
)

// Global vars
var (
	loader load.BenchmarkRunner
	config  load.BenchmarkRunnerConfig
    bufPool sync.Pool
	target  targets.ImplementedTarget

)


// Parse args:
func init() {
    //loader = load.GetBenchmarkRunner()
    var batchSize uint64
    batchSize, _ = strconv.ParseUint(os.Getenv("BATCH_SIZE"), 10, 64)
    fmt.Printf("Using batch size: %d\n", uint(batchSize))
	config = load.BenchmarkRunnerConfig{}
	config.HashWorkers = false
	loader = load.GetBenchmarkRunner(config)
}

type benchmark struct{}


func (b *benchmark) GetDataSource() targets.DataSource {
	return &fileDataSource{scanner: bufio.NewScanner(load.GetBufferedReader(config.FileName))}
}


func (b *benchmark) GetBatchFactory() targets.BatchFactory {
	return &factory{}
}
func (b *benchmark) GetPointIndexer(_ uint) targets.PointIndexer {
	return &targets.ConstantIndexer{}
}

func (b *benchmark) GetProcessor() targets.Processor {
	return &processor{}
}

func (b *benchmark) GetDBCreator() targets.DBCreator {
	return &dbCreator{}
}

func main() {
	loader.RunBenchmark(&benchmark{})
}

type processor struct {
	dbc *dbCreator
}

func (p *processor) Init(workerNum int, doLoad bool, hashWorkers bool) {}

// ProcessBatch reads eventsBatches which contain rows of CQL strings and
// creates a gocql.LoggedBatch to insert
func (p *processor) ProcessBatch(b targets.Batch, doLoad bool) (uint64, uint64) {
	eb := b.(*eventsBatch)

    count := 0 
	if doLoad {
        hosts := make([][]interface{}, 0) 
        for host, val:= range eb.hosts {

//            if(host == "host_0") {
//               fmt.Printf("host_0 %v = %d\n", eb.measurements[host][0], len(eb.measurements[host])) 
//            }

            hosts = append(hosts, val)
            measCon, err := p.dbc.getCpuCon(host)
            err = measCon.MultiPut(eb.measurements[host])
            if (err != nil) {
                fmt.Println("PutContainer failed, err:", err)
                panic("err PutContainer")
            }
            count += len(eb.measurements[host])
        }
        p.dbc.hostCon.MultiPut(hosts) 
	} 

	metricCnt := uint64(count)
	return 10*metricCnt, metricCnt
}
