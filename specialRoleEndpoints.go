package main

import (
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

}

func Advisor(w http.ResponseWriter, r *http.Request) {

}

func Admin(w http.ResponseWriter, r *http.Request) {

}

func Owner(w http.ResponseWriter, r *http.Request) {

}
