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

func SignUp(w http.ResponseWriter, r *http.Request) {

	tmpl, err := template.ParseFiles("signUp.tmpl")
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

	tmpl, err := template.ParseFiles("signIn.tmpl")
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

		token := IssueToken()
		expirationTime := time.Now().Add(5 * time.Minute)

		http.SetCookie(w,
			&http.Cookie{
				Name:     "token",
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
	tmpl, err := template.ParseFiles("home.tmpl")
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
