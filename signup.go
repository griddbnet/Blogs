package main

import (
	"fmt"
	"html/template"
	"net/http"
	"time"

	griddb "github.com/griddb/go_client"
	"golang.org/x/crypto/bcrypt"
)

type Credentials struct {
	Password string `json:"password"`
	Username string `json:"username"`
}

func createUsersContainer() {
	gridstore := ConnectGridDB()
	defer griddb.DeleteStore(gridstore)
	conInfo, err := griddb.CreateContainerInfo(map[string]interface{}{
		"name": "users",
		"column_info_list": [][]interface{}{
			{"username", griddb.TYPE_STRING},
			{"password", griddb.TYPE_STRING}},
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

func SignUp(w http.ResponseWriter, r *http.Request) {

	tmpl, err := template.ParseFiles("./pages/signUp.tmpl")
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	if r.Method == http.MethodPost {

		creds := &Credentials{}
		creds.Username = r.FormValue("username")
		creds.Password = r.FormValue("password")

		hashedPassword, err := bcrypt.GenerateFromPassword([]byte(creds.Password), 8)
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
		}

		saveUser(creds.Username, string(hashedPassword))
		http.Redirect(w, r, "/signIn", http.StatusFound)
		return
	}

	data := struct {
		Message string
	}{
		Message: "Please enter create a username and password combo",
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}

func SignIn(w http.ResponseWriter, r *http.Request) {

	tmpl, err := template.ParseFiles("./pages/signIn.tmpl")
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	if r.Method == "POST" {
		creds := &Credentials{}
		creds.Username = r.FormValue("username")
		creds.Password = r.FormValue("password")

		gridstore := ConnectGridDB()
		defer griddb.DeleteStore(gridstore)

		userCol := GetContainer(gridstore, "users")
		defer griddb.DeleteContainer(userCol)
		userCol.SetAutoCommit(false)

		queryStr := fmt.Sprintf("select * FROM users where username = '%s'", creds.Username)
		rs, err := QueryContainer(gridstore, userCol, queryStr)
		defer griddb.DeleteRowSet(rs)
		if err != nil {
			fmt.Println("Issue with querying container")
			w.WriteHeader(http.StatusBadRequest)
			return
		}

		storedCreds := &Credentials{}
		for rs.HasNext() {
			rrow, err := rs.NextRow()
			if err != nil {
				fmt.Println("GetNextRow err:", err)
				panic("err GetNextRow")
			}

			storedCreds.Username = rrow[0].(string)
			storedCreds.Password = rrow[1].(string)

		}

		if err = bcrypt.CompareHashAndPassword([]byte(storedCreds.Password), []byte(creds.Password)); err != nil {
			fmt.Println("unauthorized")
			w.WriteHeader(http.StatusUnauthorized)
			return
		}
		expirationTime := time.Now().Add(5 * time.Hour)
		// sending nil in place of slice of string for roles
		token, err := IssueToken(Roles{}, expirationTime)
		if err != nil {
			fmt.Println("issue getting token", err)
			fmt.Fprintf(w, "Issue getting token, possible no environment variable set")
			return
		}

		http.SetCookie(w,
			&http.Cookie{
				Name:     "login",
				Value:    token,
				Expires:  expirationTime,
				HttpOnly: true,
			})

		http.Redirect(w, r, "/auth", http.StatusFound)
		return
	}

	data := struct {
		Message string
	}{
		Message: "Please enter your username and password",
	}

	err = tmpl.Execute(w, data)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}

func AuthPage(w http.ResponseWriter, r *http.Request) {
	tmpl, err := template.ParseFiles("./pages/home.tmpl")
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	err = tmpl.Execute(w, nil)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}
