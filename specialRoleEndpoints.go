package main

import (
	"fmt"
	"net/http"
)

type Roles struct {
	Basic   bool   `json:"basic"`
	Advisor bool   `json:"advisor"`
	Admin   bool   `json:"admin"`
	Owner   bool   `json:"owner"`
	Value   string `json:"value"`
	Name    string `json:"name"`
}

func Basic(w http.ResponseWriter, r *http.Request) {
	if r.Method == http.MethodPost {

		data := r.FormValue("basic-data")
		addData("basic", data)
		return
	}
}

func Advisor(w http.ResponseWriter, r *http.Request) {
	if r.Method == http.MethodPost {

		data := r.FormValue("advisor-data")
		fmt.Println(data)
		addData("advisor", data)
		return
	}
}

func Admin(w http.ResponseWriter, r *http.Request) {
	if r.Method == http.MethodPost {

		data := r.FormValue("admin-data")
		addData("admin", data)
		return
	}
}

func Owner(w http.ResponseWriter, r *http.Request) {
	if r.Method == http.MethodPost {

		data := r.FormValue("owner-data")
		addData("owner", data)
		return
	}
}
