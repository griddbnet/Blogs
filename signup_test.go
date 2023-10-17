package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
)

func TestSignUpServer(t *testing.T) {

	t.Run("can GET Signup endpoint", func(t *testing.T) {
		request, _ := http.NewRequest(http.MethodGet, "/signUp", nil)
		response := httptest.NewRecorder()

		SignUp(response, request)

		want := http.StatusOK
		got := response.Code

		if got != want {
			t.Errorf("got %v, want %v", got, want)
		}
	})

	t.Run("can run a POST request", func(t *testing.T) {
		payload := Credentials{
			Username: "user",
			Password: "pass",
		}
		out, err := json.Marshal(payload)
		if err != nil {
			t.Fatalf("Unable to parse response from server nto slice of Credentials, '%v'", err)
		}
		request, _ := http.NewRequest(http.MethodPost, "/signUp", bytes.NewBuffer(out))
		response := httptest.NewRecorder()

		SignUp(response, request)
		got := response.Body.String()
		if err != nil {
			t.Fatalf("Unable to parse response from server %q into slice of Credentials, '%v'", response.Body, err)
		}

		assertStatus(t, response.Code, http.StatusOK)

		want, err := bcrypt.GenerateFromPassword([]byte("password"), 8)
		if err != nil {
			t.Fatalf("Failed encryption %q", want)
		}

		if len(got) <= 0 {
			t.Errorf("empty hash password")
		}
	})
}

func TestSignIn(t *testing.T) {
	payload := Credentials{
		Username: "user",
		Password: "pass",
	}
	out, err := json.Marshal(payload)
	if err != nil {
		t.Fatalf("Unable to parse response from server nto slice of Credentials, '%v'", err)
	}
	request, _ := http.NewRequest(http.MethodPost, "/signIn", bytes.NewBuffer(out))
	response := httptest.NewRecorder()

	SignIn(response, request)

	assertStatus(t, response.Code, http.StatusOK)

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
	got := response.Body.String()

	if got != want {
		t.Errorf("Not returning proper token. Got %q, want %q", got, want)
	}

}

func assertStatus(t testing.TB, got, want int) {
	t.Helper()
	if got != want {
		t.Errorf("got status %d, want %d", got, want)
	}
}
