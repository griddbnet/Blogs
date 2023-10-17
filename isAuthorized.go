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
		cookie, err := r.Cookie("token")
		if err != nil {
			if err == http.ErrNoCookie {
				w.WriteHeader(http.StatusUnauthorized)
				return
			}
			w.WriteHeader(http.StatusBadRequest)
			return
		}

		tokenString := cookie.Value

		token, err := jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
			return MySigningKey, nil
		}, jwt.WithLeeway(5*time.Second))

		if token.Valid {
			fmt.Println("Successful Authorization check")
			endpoint(w, r)
		} else if errors.Is(err, jwt.ErrTokenMalformed) {
			w.WriteHeader(http.StatusUnauthorized)
			fmt.Fprintf(w, "Authorization Token nonexistent or malformed")
			return
		} else if errors.Is(err, jwt.ErrTokenSignatureInvalid) {
			fmt.Println("Invalid signature")
			w.WriteHeader(http.StatusUnauthorized)
			fmt.Fprintf(w, "Authorization Signature Invalid")
			return
		} else if errors.Is(err, jwt.ErrTokenExpired) || errors.Is(err, jwt.ErrTokenNotValidYet) {
			fmt.Println("Expired Token")
			w.WriteHeader(http.StatusUnauthorized)
			fmt.Fprintf(w, "Authorization Signature Expired")
			return
		} else {
			fmt.Println("Couldn't handle this token:", err)
			w.WriteHeader(http.StatusUnauthorized)
			return
		}
	}
}
