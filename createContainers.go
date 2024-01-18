package main

import (
	"fmt"

	griddb "github.com/griddb/go_client"
)

// type Credentials struct {
// 	Password string `json:"password"`
// 	Username string `json:"username"`
// }

func createBasicContainer() {
	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)
	conInfo, err := griddb.CreateContainerInfo(map[string]interface{}{
		"name": "basic",
		"column_info_list": [][]interface{}{
			{"data", griddb.TYPE_STRING}},
		"type":    griddb.CONTAINER_COLLECTION,
		"row_key": true})
	if err != nil {
		fmt.Println("Create containerInfo failed, err:", err)
		panic("err CreateContainerInfo")
	}
	_, e := gridstore.PutContainer(conInfo)
	if e != nil {
		fmt.Println("put container failed, err:", e)
		panic("err PutContainer")
	}
}

func createAdminContainer() {
	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)
	conInfo, err := griddb.CreateContainerInfo(map[string]interface{}{
		"name": "admin",
		"column_info_list": [][]interface{}{
			{"data", griddb.TYPE_STRING}},
		"type":    griddb.CONTAINER_COLLECTION,
		"row_key": true})
	if err != nil {
		fmt.Println("Create containerInfo failed, err:", err)
		panic("err CreateContainerInfo")
	}
	_, e := gridstore.PutContainer(conInfo)
	if e != nil {
		fmt.Println("put container failed, err:", e)
		panic("err PutContainer")
	}
}

func createAdvisorContainer() {
	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)
	conInfo, err := griddb.CreateContainerInfo(map[string]interface{}{
		"name": "advisor",
		"column_info_list": [][]interface{}{
			{"data", griddb.TYPE_STRING}},
		"type":    griddb.CONTAINER_COLLECTION,
		"row_key": true})
	if err != nil {
		fmt.Println("Create containerInfo failed, err:", err)
		panic("err CreateContainerInfo")
	}
	_, e := gridstore.PutContainer(conInfo)
	if e != nil {
		fmt.Println("put container failed, err:", e)
		panic("err PutContainer")
	}
}

func createOwnerContainer() {
	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)
	conInfo, err := griddb.CreateContainerInfo(map[string]interface{}{
		"name": "owner",
		"column_info_list": [][]interface{}{
			{"data", griddb.TYPE_STRING}},
		"type":    griddb.CONTAINER_COLLECTION,
		"row_key": true})
	if err != nil {
		fmt.Println("Create containerInfo failed, err:", err)
		panic("err CreateContainerInfo")
	}
	_, e := gridstore.PutContainer(conInfo)
	if e != nil {
		fmt.Println("put container failed, err:", e)
		panic("err PutContainer")
	}
}

func addData(containerName, data string) {
	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)

	userCol := GetContainer(gridstore, containerName)
	err := userCol.Put([]interface{}{data})
	if err != nil {
		fmt.Println("error putting new user into GridDB", err)
	}

	fmt.Println("Adding data to: " + containerName + " this string: " + data)
	userCol.Commit()

}
