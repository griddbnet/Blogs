package main

import (
	"fmt"
	"log"
	"net/http"
)

func main() {

	http.HandleFunc("/signUp", SignUp)
	http.HandleFunc("/signIn", SignIn)
	http.HandleFunc("/auth", isAuthorized(AuthPage))
	http.HandleFunc("/data", isAuthorized(DataEndPoints))

	fmt.Println("Listening on port :2828....")
	log.Fatal(http.ListenAndServe(":2828", nil))

}
