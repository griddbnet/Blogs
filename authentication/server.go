package main

import (
	"fmt"
	"log"
	"net/http"
	"time"

	jwt "github.com/golang-jwt/jwt"
)

var mySigningKey = []byte("ScruffMcGruff")

func GetJWT() (string, error) {
	token := jwt.New(jwt.SigningMethodHS256)

	claims := token.Claims.(jwt.MapClaims)

	claims["authorized"] = true
	claims["aud"] = "golang-blog"
	claims["iss"] = "israel"
	claims["exp"] = time.Now().Add(time.Hour * 1).Unix()

	tokenString, err := token.SignedString(mySigningKey)

	if err != nil {
		fmt.Errorf("Something Went Wrong: %s", err.Error())
		return "", err
	}

	return tokenString, nil
}

func Index(w http.ResponseWriter, r *http.Request) {

	enableCors(&w)

	validToken, err := GetJWT()
	fmt.Println(validToken)
	if err != nil {
		fmt.Println("Failed to generate token")
	}

	fmt.Fprintf(w, string(validToken))
}

func enableCors(w *http.ResponseWriter) {
	(*w).Header().Set("Access-Control-Allow-Origin", "*")
}

func handleRequests() {

	http.HandleFunc("/", Index)
	fmt.Println("Starting on port 2828")
	log.Fatal(http.ListenAndServe(":2828", nil))
}

func main() {
	handleRequests()
}
