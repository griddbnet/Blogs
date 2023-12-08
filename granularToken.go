package main

import (
	"fmt"
	"html/template"
	"net/http"
	"strconv"
	"time"
)

func GetToken(w http.ResponseWriter, r *http.Request) {

	tmpl, err := template.ParseFiles("./pages/token.tmpl")
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	roles := &Roles{}
	if r.Method == "POST" {

		roles.Basic, _ = strconv.ParseBool(r.FormValue("basic"))
		roles.Advisor, _ = strconv.ParseBool(r.FormValue("advisor"))
		roles.Admin, _ = strconv.ParseBool(r.FormValue("admin"))
		roles.Owner, _ = strconv.ParseBool(r.FormValue("owner"))
		roles.Name = r.FormValue("name")
		fmt.Println("Received form values", roles)

		expiryTime, _ := strconv.ParseInt(r.FormValue("expiration"), 10, 64)
		expiryTimeInDays := expiryTime * 24
		expirationTime := time.Now().Add(time.Duration(expiryTimeInDays) * time.Hour)

		token, err := IssueToken(*roles, expirationTime)
		if err != nil {
			fmt.Println("issue getting token", err)
			fmt.Fprintf(w, "Issue getting token, possible no environment variable set")
			return
		}
		roles.Value = token

		http.SetCookie(w,
			&http.Cookie{
				Name:     "token",
				Value:    token,
				Expires:  expirationTime,
				HttpOnly: true,
				SameSite: 3, //Strict
			})
	}
	err = tmpl.Execute(w, roles)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}
