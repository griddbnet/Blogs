package main

import (
	"fmt"
	"github.com/griddb/go_client"
)

type dbCreator struct {
    gridstore griddb_go.Store
    hostCon griddb_go.Container
    measCons map[string]griddb_go.Container
}

func (d *dbCreator) getHostCon() (griddb_go.Container, error) {
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

    col, err := d.gridstore.PutContainer(conInfo)   
    if (err != nil) {
        fmt.Println("PutContainer failed, err:", err)
        panic("err PutContainer")

    }
	return col, err
}

func (d *dbCreator) Init() {
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
    d.gridstore = gridstore
    d.measCons= make(map[string]griddb_go.Container)
    d.hostCon, _ = d.getHostCon()
//    defer griddb_go.DeleteStore(gridstore)
}

func (d *dbCreator) DBExists(dbName string) bool {
	return false
}

func (d *dbCreator) RemoveOldDB(dbName string) error {
    d.gridstore.DropContainer(dbName)

	return nil
}

func (d *dbCreator) CreateDB(dbName string)  error {
    return nil
}



func (d *dbCreator) getCpuCon(dbName string) (griddb_go.Container, error) {

    con,ok := d.measCons[dbName]
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

        col, err := d.gridstore.PutContainer(conInfo)
        //d.measCons[dbName] = col
    	return col, err
    }
}

func (d *dbCreator) PostCreateDB(dbName string) error {
    return nil
}

func (d *dbCreator) Close() {
}
