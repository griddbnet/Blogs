package main

import (
	"fmt"
	"net/http"
)

func main() {
	client := &http.Client{}

	// --- 1. DEMO WITH BASIC AUTH ---
	fmt.Println("Sending request with Basic Auth...")
	reqBasic, _ := http.NewRequest("GET", "http://localhost:8080/basic", nil)

	// This is the "secure" helper... but watch what it sends.
	reqBasic.SetBasicAuth("MyUsername", "MySuperSecretPassword123")

	respBasic, err := client.Do(reqBasic)
	if err != nil {
		fmt.Println("Error:", err)
	}
	defer respBasic.Body.Close()

	// --- 2. DEMO WITH BEARER TOKEN ---
	fmt.Println("Sending request with Bearer Token...")
	reqBearer, _ := http.NewRequest("GET", "http://localhost:8080/bearer", nil)

	// This is the token your manager would get
	fakeToken := "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc3JhZWwiLCJleHAiOjE3NjE3NjA3NjZ9.fake_signature_part"
	reqBearer.Header.Set("Authorization", "Bearer "+fakeToken)

	respBearer, err := client.Do(reqBearer)
	if err != nil {
		fmt.Println("Error:", err)
	}
	defer respBearer.Body.Close()
}
