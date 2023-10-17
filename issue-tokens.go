package main

import (
	"fmt"
	"os"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

var claims = &jwt.RegisteredClaims{
	ExpiresAt: jwt.NewNumericDate(time.Unix(time.Now().Unix()*time.Hour.Milliseconds(), 0)),
	Issuer:    "griddb-auth-server",
}

func IssueToken() string {

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	key := []byte(os.Getenv("SigningKey"))
	if len(key) <= 0 {
		fmt.Println("Key is less than length 0")
		return ""
	}
	s, err := token.SignedString(key)
	if err != nil {
		fmt.Printf("Error, couldn't read os environment: %q", err)
		return ""
	}
	return s
}
