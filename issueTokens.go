package main

import (
	"errors"
	"fmt"
	"os"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

type MyCustomClaims struct {
	Role Roles `json:"roles"`
	jwt.RegisteredClaims
}

var claims = MyCustomClaims{
	Roles{},
	jwt.RegisteredClaims{
		ExpiresAt: jwt.NewNumericDate(time.Unix(time.Now().Unix()*time.Hour.Milliseconds(), 0)),
		IssuedAt:  jwt.NewNumericDate(time.Now()),
		NotBefore: jwt.NewNumericDate(time.Now()),
		Issuer:    "griddb-auth-server",
	},
}

func IssueToken(roles Roles, expiry time.Time) (string, error) {

	claims.Role = roles
	claims.ExpiresAt = jwt.NewNumericDate(expiry)

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	key := []byte(os.Getenv("SigningKey"))
	if string(key) != "" {
		if len(key) <= 0 {
			return "", errors.New("Key is less than length 0")
		}
		s, err := token.SignedString(key)
		if err != nil {
			fmt.Printf("Error, couldn't read os environment: %q", err)
			return "", errors.New("could not read environment var")
		}
		return s, nil
	}

	return "", errors.New("No environment variable set")
}
