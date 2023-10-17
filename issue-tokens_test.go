package main

import (
	"fmt"
	"os"
	"testing"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

func TestGETToken(t *testing.T) {
	t.Run("returns token string", func(t *testing.T) {

		claims := &jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Unix(time.Now().Unix()*time.Hour.Milliseconds(), 0)),
			Issuer:    "griddb-auth-server",
		}
		localToken := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
		localKey := []byte(os.Getenv("SigningKey"))

		want, err := localToken.SignedString(localKey)
		if err != nil {
			fmt.Printf("error: %v", err)
		}

		got := IssueToken()

		if got != want {
			t.Errorf("got %q, want %q", got, want)
		}
	})
}
