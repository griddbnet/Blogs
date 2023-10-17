package main

import (
	"log"
	"net/http"
)

func main() {

	http.HandleFunc("/signUp", SignUp)
	http.HandleFunc("/signIn", SignIn)
	http.HandleFunc("/auth", isAuthorized(AuthPage))
	http.HandleFunc("/data", isAuthorized(DataEndPoints))

	log.Fatal(http.ListenAndServe(":2828", nil))

}
