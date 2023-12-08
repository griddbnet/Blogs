package main

import (
	"fmt"
	"net/http"
	"time"

	griddb "github.com/griddb/go_client"
)

type Device struct {
	TS       time.Time `json:"ts"`
	CO       float64   `json:"co"`
	Humidity float64   `json:"humidity"`
	Light    bool      `json:"light"`
	LPG      float64   `json:"lpg"`
	Motion   bool      `json:"motion"`
	Smoke    float64   `json:"smoke"`
	Temp     float64   `json:"temp"`
}

func ConnectGridDB() griddb.Store {
	factory := griddb.StoreFactoryGetInstance()

	// Get GridStore object
	gridstore, err := factory.GetStore(map[string]interface{}{
		"notification_member": "127.0.0.1:10001",
		"cluster_name":        "myCluster",
		"username":            "admin",
		"password":            "admin"})
	if err != nil {
		fmt.Println(err)
		panic("err get store")
	}

	return gridstore
}

func GetContainer(gridstore griddb.Store, cont_name string) griddb.Container {
	fmt.Println("Getting container " + cont_name)
	col, err := gridstore.GetContainer(cont_name)
	if err != nil {
		fmt.Println("getting failed, err:", err)
		panic("err create query")
	}
	col.SetAutoCommit(false)

	return col
}

func QueryContainer(gridstore griddb.Store, col griddb.Container, query_string string) (griddb.RowSet, error) {
	fmt.Println("querying: ", query_string)
	query, err := col.Query(query_string)
	if err != nil {
		fmt.Println("create query failed, err:", err)
		panic("err create query")
	}

	rs, err := query.Fetch(true)
	if err != nil {
		fmt.Println("fetch failed, err:", err)
		return nil, err
	}
	return rs, nil
}

func saveUser(username, hashedPassword string) {
	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)

	userCol := GetContainer(gridstore, "users")
	err := userCol.Put([]interface{}{username, hashedPassword})
	if err != nil {
		fmt.Println("error putting new user into GridDB", err)
	}

	fmt.Println("Saving user into GridDB")
	userCol.Commit()

}

func DataEndPoints(w http.ResponseWriter, r *http.Request) {
	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)

	devicesCol := GetContainer(gridstore, "device2")
	queryStr := "SELECT *"
	rs, err := QueryContainer(gridstore, devicesCol, queryStr)
	if err != nil {
		fmt.Println("Failed fetching device2", err)
		return
	}

	device := Device{}
	devices := []Device{}
	for rs.HasNext() {
		rrow, err := rs.NextRow()
		if err != nil {
			fmt.Println("GetNextRow err:", err)
			panic("err GetNextRow")
		}

		device.TS = rrow[0].(time.Time)
		device.CO = rrow[1].(float64)
		device.Humidity = rrow[2].(float64)
		device.Light = rrow[3].(bool)
		device.LPG = rrow[4].(float64)
		device.Motion = rrow[5].(bool)
		device.Smoke = rrow[6].(float64)
		device.Temp = rrow[7].(float64)
		devices = append(devices, device)
	}
	//fmt.Println(devices)
	fmt.Fprint(w, devices)
}
