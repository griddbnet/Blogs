package main

import (
	// "encoding/json"
	"encoding/json"
	"fmt"
	"log"
    	"io/ioutil"
	"net/http"
	"strconv"

	"github.com/gorilla/mux"
	griddb "github.com/griddb/go_client"
)

type Todo struct {
	Id        int
	Title     string
	Completed bool
}

func GetContainer(gridstore griddb.Store, cont_name string) griddb.Container {

	col, err := gridstore.GetContainer(cont_name)
	if err != nil {
		fmt.Println("getting failed, err:", err)
		panic("err create query")
	}
	col.SetAutoCommit(false)

	return col
}

func QueryContainer(gridstore griddb.Store, col griddb.Container, query_string string) griddb.RowSet {
	query, err := col.Query(query_string)
	if err != nil {
		fmt.Println("create query failed, err:", err)
		panic("err create query")
	}

	// Execute query
	rs, err := query.Fetch(true)
	if err != nil {
		fmt.Println("fetch failed, err:", err)
		panic("err create rowset")
	}
	return rs
}

func GetToken(w http.ResponseWriter, r *http.Request)  {

    w.Header().Set("Access-Control-Allow-Origin", "*")

    resp, err := http.Get("http://auth:2828")
    if err != nil {
        log.Fatalln("error grabbing Token", err)
    }

   body, err := ioutil.ReadAll(resp.Body)
   if err != nil {
      log.Fatalln(err)
   }
    fmt.Println(string(body))
    w.Write(body)
}

func Get(w http.ResponseWriter, r *http.Request) {

	w.Header().Set("Content=Type", "application/json")
	w.Header().Set("Access-Control-Allow-Origin", "*")

	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)

	col := GetContainer(gridstore, "todo")
	defer griddb.DeleteContainer(col)

	rs := QueryContainer(gridstore, col, "select *")
	defer griddb.DeleteRowSet(rs)

	var todos []Todo
	for rs.HasNext() {
		rrow, err := rs.NextRow()
		if err != nil {
			fmt.Println("NextRow from rs failed, err:", err)
			panic("err NextRow from rowset")
		}
		var todo Todo
		todo.Id = rrow[0].(int)
		todo.Title = rrow[1].(string)
		todo.Completed = rrow[2].(bool)
		todos = append(todos, todo)
	}
	if err := json.NewEncoder(w).Encode(todos); err != nil {
		log.Panic(err)
	}
}

func CreateTodoItem(w http.ResponseWriter, r *http.Request) {

	w.Header().Set("Content=Type", "application/json")
	w.Header().Set("Access-Control-Allow-Origin", "*")

	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)

	col := GetContainer(gridstore, "todo")
	defer griddb.DeleteContainer(col)

	decoder := json.NewDecoder(r.Body)
	var t Todo
	err := decoder.Decode(&t)
	if err != nil {
		fmt.Println("decode err", err)
	}

	col.SetAutoCommit(false)
	err = col.Put([]interface{}{t.Id, t.Title, t.Completed})
	if err != nil {
		fmt.Println("put row name01 fail, err:", err)
	}
	fmt.Println("Creating Item")
	col.Commit()
}

func UpdateTodoItem(w http.ResponseWriter, r *http.Request) {

	w.Header().Set("Content=Type", "application/json")
	w.Header().Set("Access-Control-Allow-Origin", "*")

	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)

	col := GetContainer(gridstore, "todo")
	defer griddb.DeleteContainer(col)
	col.SetAutoCommit(false)

	vars := mux.Vars(r)
	idStr := vars["id"]
	id, err := strconv.Atoi(idStr)
	if err != nil {
		http.Error(w, "Invalid ID", http.StatusBadRequest)
		return
	}

	query_str := fmt.Sprintf("SELECT * WHERE id = %d", id)

	rs := QueryContainer(gridstore, col, query_str)
	defer griddb.DeleteRowSet(rs)

	for rs.HasNext() {
		rrow, err := rs.NextRow()
		if err != nil {
			fmt.Println("GetNextRow err:", err)
			panic("err GetNextRow")
		}

		todo_title := rrow[1]
		todo_done := rrow[2].(bool) //casting interface{} to bool

		//Update row from completed to not, etc
		err2 := rs.Update([]interface{}{id, todo_title, !todo_done})
		if err2 != nil {
			fmt.Println("Update err: ", err2)
		}
	}
	col.Commit()
}

func DeleteTodoItem(w http.ResponseWriter, r *http.Request) {

	w.Header().Set("Content=Type", "application/json")
	w.Header().Set("Access-Control-Allow-Origin", "*")

	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)

	col := GetContainer(gridstore, "todo")
	defer griddb.DeleteContainer(col)

	vars := mux.Vars(r)
	idStr := vars["id"]
	id, err := strconv.Atoi(idStr)
	if err != nil {
		http.Error(w, "Invalid ID", http.StatusBadRequest)
		return
	}

	query_str := fmt.Sprintf("SELECT * WHERE id = %d", id)
	fmt.Println(query_str)

	rs := QueryContainer(gridstore, col, query_str)
	defer griddb.DeleteRowSet(rs)

	for rs.HasNext() {
		rrow, err11 := rs.NextRow() //Avoid invalid results returned
		if err11 != nil {
			fmt.Println("GetNextRow err:", err11)
			panic("err GetNextRow")
		}
		fmt.Println(rrow)
		fmt.Println(("removing"))
		err5 := rs.Remove()
		if err5 != nil {
			fmt.Println("Remove err: ", err5)
		}
	}

}

func ConnectGridDB() griddb.Store {
	factory := griddb.StoreFactoryGetInstance()

	// Get GridStore object
	gridstore, err := factory.GetStore(map[string]interface{}{
		"notification_member": "griddb-server:10001",
		"cluster_name":        "myCluster",
		"username":            "admin",
		"password":            "admin"})
	if err != nil {
		fmt.Println(err)
		panic("err get store")
	}

	return gridstore
}

func createTodo() {
	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)
	conInfo, err := griddb.CreateContainerInfo(map[string]interface{}{
		"name": "todo",
		"column_info_list": [][]interface{}{
			{"id", griddb.TYPE_INTEGER},
			{"title", griddb.TYPE_STRING},
			{"completed", griddb.TYPE_BOOL}},
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

func main() {

	createTodo()

	router := mux.NewRouter()
	router.Use(mux.CORSMethodMiddleware(router))

	router.HandleFunc("/get", isAuthorized(Get))
    router.HandleFunc("/getToken", GetToken)
	router.HandleFunc("/create", isAuthorized(CreateTodoItem)).Methods("POST")
	router.HandleFunc("/update/{id}", isAuthorized(UpdateTodoItem)).Methods("POST")
	router.HandleFunc("/delete/{id}", isAuthorized(DeleteTodoItem)).Methods("POST")

	router.PathPrefix("/").Handler(http.FileServer(http.Dir("./public/")))

    srv := &http.Server{
        Handler: router,
        Addr:    "0.0.0.0:8000",
    }

	fmt.Println("Listening on port 8000...")
	log.Fatal(srv.ListenAndServe())
}
