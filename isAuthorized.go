package main

import (
	"errors"
	"fmt"
	"net/http"
	"os"
	"time"

	"github.com/golang-jwt/jwt"
)

var MySigningKey = []byte(os.Getenv("SigningKey"))

func isAuthorized(endpoint func(http.ResponseWriter, *http.Request)) func(http.ResponseWriter, *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		cookie, err := r.Cookie("login")
		if err != nil {
			if err == http.ErrNoCookie {
				w.WriteHeader(http.StatusUnauthorized)
				return
			}
			w.WriteHeader(http.StatusBadRequest)
			return
		}

		tokenString := cookie.Value

		token, err := jwt.ParseWithClaims(tokenString, &MyCustomClaims{}, func(token *jwt.Token) (interface{}, error) {
			return MySigningKey, nil
		}, jwt.WithLeeway(5*time.Second))

		if token.Valid {
			fmt.Println("Successful Authorization check")
			endpoint(w, r)
		} else if errors.Is(err, jwt.ErrTokenMalformed) {
			w.WriteHeader(http.StatusUnauthorized)
			fmt.Fprintf(w, "Authorization Token nonexistent or malformed")
			http.Redirect(w, r, "/signIn", http.StatusSeeOther)
			return
		} else if errors.Is(err, jwt.ErrTokenSignatureInvalid) {
			fmt.Println("Invalid signature")
			w.WriteHeader(http.StatusUnauthorized)
			fmt.Fprintf(w, "Authorization Signature Invalid")
			http.Redirect(w, r, "/signIn", http.StatusSeeOther)
			return
		} else if errors.Is(err, jwt.ErrTokenExpired) || errors.Is(err, jwt.ErrTokenNotValidYet) {
			fmt.Println("Expired Token")
			w.WriteHeader(http.StatusUnauthorized)
			fmt.Fprintf(w, "Authorization Signature Expired")
			http.Redirect(w, r, "/signIn", http.StatusSeeOther)
			return
		} else {
			fmt.Println("Couldn't handle this token:", err)
			w.WriteHeader(http.StatusUnauthorized)
			http.Redirect(w, r, "/signIn", http.StatusSeeOther)
			return
		}
	}
}
