// tsbs_run_queries_mongo speed tests Mongo using requests from stdin.
//
// It reads encoded Query objects from stdin, and makes concurrent requests
// to the provided Mongo endpoint using mgo.
package main

import (
	"fmt"
//	"log"
	"time"

	"github.com/griddb/go_client"
	"github.com/timescale/tsbs/query"
)


// Global vars:
var (
	runner  *query.BenchmarkRunner
)

// Parse args:
func init() {
	runner = query.NewBenchmarkRunner()
    return
}

func main() {
	runner.Run(&query.GridDBPool, newProcessor)
}

type processor struct {
	gridstore griddb_go.Store
    hostCon griddb_go.Container
    measCons map[string]griddb_go.Container 

}

func newProcessor() query.Processor { return &processor{} }

func (p *processor) getHostCon() (griddb_go.Container, error) {
    conInfo, err := griddb_go.CreateContainerInfo(map[string]interface{} {
        "name": "HOSTS",
        "column_info_list":[][]interface{}{
            {"host", griddb_go.TYPE_STRING},
            {"region", griddb_go.TYPE_STRING},
            {"datacenter", griddb_go.TYPE_STRING},
            {"rack", griddb_go.TYPE_INTEGER},
            {"os", griddb_go.TYPE_STRING},
            {"arch", griddb_go.TYPE_STRING},
            {"team", griddb_go.TYPE_STRING},
            {"service", griddb_go.TYPE_INTEGER},
            {"service_version", griddb_go.TYPE_INTEGER},
            {"service_environment", griddb_go.TYPE_STRING}},
        "type": griddb_go.CONTAINER_COLLECTION,
        "row_key": true})
    if (err != nil) {
        fmt.Println("Create containerInfo failed, err:", err)
        panic("err CreateContainerInfo")
    }
    //defer griddb_go.DeleteContainerInfo(conInfo)

    col, err := p.gridstore.PutContainer(conInfo)   
    if (err != nil) {
        fmt.Println("PutContainer failed, err:", err)
        panic("err PutContainer")

    }
	return col, err
}


func (p *processor) getCpuCon(dbName string) (griddb_go.Container, error) {

    con,ok := p.measCons[dbName]
    if(ok) {
        return con, nil
    } else {
        conInfo, err := griddb_go.CreateContainerInfo(map[string]interface{} {
            "name": fmt.Sprintf("CPU_%s", dbName),
            "column_info_list":[][]interface{}{
                {"timestamp", griddb_go.TYPE_TIMESTAMP},
                {"usage_user", griddb_go.TYPE_DOUBLE},
                {"usage_system", griddb_go.TYPE_DOUBLE},
                {"usage_idle", griddb_go.TYPE_DOUBLE},
                {"usage_nice", griddb_go.TYPE_DOUBLE},
                {"usage_iowait", griddb_go.TYPE_DOUBLE},
                {"usage_irq", griddb_go.TYPE_DOUBLE},
                {"usage_irq_softirq", griddb_go.TYPE_DOUBLE},
                {"usage_steal", griddb_go.TYPE_DOUBLE},
                {"usage_guest", griddb_go.TYPE_DOUBLE},
                {"usage_guest_nice", griddb_go.TYPE_DOUBLE}},
            "type": griddb_go.CONTAINER_TIME_SERIES,
            "row_key": true})

        if (err != nil) {
            fmt.Println("Create containerInfo failed, err:", err)
            panic("err CreateContainerInfo")
        }
    //    defer griddb_go.DeleteContainerInfo(conInfo)

        col, err := p.gridstore.PutContainer(conInfo)
        p.measCons[dbName] = col
    	return col, err
    }
}


func (p *processor) Init(workerNumber int) {
    factory := griddb_go.StoreFactoryGetInstance()
    defer griddb_go.DeleteStoreFactory(factory)

    gridstore, err := factory.GetStore(map[string]interface{} {
//        "notification_member": "192.168.1.75:10001",
        "host": "239.0.0.1",
        "port": 31999,
        "cluster_name": "defaultCluster",
        "username": "admin",
        "password": "admin"})
    if (err != nil) {
        fmt.Println(err)
        panic("err get store")
    }
    fmt.Printf("Connected to griddb\n")
    p.gridstore = gridstore
    p.measCons= make(map[string]griddb_go.Container)
    p.hostCon, _ = p.getHostCon()
//    defer griddb_go.DeleteStore(gridstore)

}

func (p *processor) ProcessQuery(q query.Query, _ bool) ([]*query.Stat, error) {
	tq := q.(*query.GridDB)
	start := time.Now().UnixNano()

    if len(tq.TqlQuery) == 1 && len(tq.Container) == 1 {
        ts, _ :=  p.getCpuCon(string(tq.Container[0]))
        query, err := ts.Query(string(tq.TqlQuery[0]))
        if (err != nil) {
            fmt.Println("create query failed, err:", err)
            panic("err create query")
        }
        defer griddb_go.DeleteQuery(query)

        // Execute query
        rs, err := query.Fetch(false)
        if (err != nil) {
            fmt.Println("fetch failed, err:", err)
            panic("err create rowset")
        }
        defer griddb_go.DeleteRowSet(rs)

    } else {
        queries := make([]griddb_go.Query, 0)
        //fmt.Printf("Multi queries found!\n")
        fmt.Printf("%d hosts, %d querys\n", len(tq.Container), len(tq.TqlQuery))
        for _, host := range tq.Container {
            //fmt.Printf("host=%s\n", host)
            for _, tql := range tq.TqlQuery {
                //fmt.Printf("\ttql=%s\n", tql)
                ts, _ := p.getCpuCon(string(host))
                query, _ := ts.Query(string(tql))
                queries = append(queries, query)
            }
        }
        p.gridstore.FetchAll(queries)
        //fmt.Printf("fetching %d queries\n", len(queries))
        for _, query := range queries {
            rs, _ := query.Fetch()
            fmt.Printf("%d results.\n", rs.Size())
        }

    }

    took := time.Now().UnixNano() - start
	lag := float64(took) / 1e6 // milliseconds
	stat := query.GetStat()
	stat.Init(q.HumanLabelName(), lag)
	return []*query.Stat{stat}, nil
}
