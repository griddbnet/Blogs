package main

import (
	"fmt"
	"log"
	"net/http"
	"strings"
	"time"

	"github.com/golang-jwt/jwt"
	griddb "github.com/griddb/go_client"
)

func granularAuth(endpoint func(http.ResponseWriter, *http.Request)) func(http.ResponseWriter, *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {

		if r.Header["Authorization"] != nil {
			authorization := r.Header.Get("Authorization")
			tokenString := strings.TrimSpace(strings.Replace(authorization, "Bearer", "", 1))

			token, err := jwt.ParseWithClaims(tokenString, &MyCustomClaims{}, func(token *jwt.Token) (interface{}, error) {
				return MySigningKey, nil
			}, jwt.WithLeeway(5*time.Second))

			if err != nil {
				log.Fatal(err)
			} else if claims, ok := token.Claims.(*MyCustomClaims); ok {
				urlPath := strings.TrimLeft(r.URL.Path, "/")

				gridstore := ConnectGridDB()
				defer griddb.DeleteStore(gridstore)

				col := GetContainer(gridstore, urlPath)
				defer griddb.DeleteContainer(col)

				rs, err := QueryContainer(gridstore, col, "select *")
				if err != nil {
					fmt.Println("Error getting container", err)
					return
				}
				defer griddb.DeleteRowSet(rs)

				var b strings.Builder

				switch urlPath {
				case "basic":
					if claims.Role.Basic {
						for rs.HasNext() {
							rrow, err := rs.NextRow()
							if err != nil {
								fmt.Println("GetNextRow err:", err)
								panic("err GetNextRow")
							}

							str := rrow[0].(string)
							fmt.Fprintln(&b, str)
						}
						fmt.Fprintf(w, b.String())
						endpoint(w, r)
						return
					} else {
						w.WriteHeader(http.StatusUnauthorized)
						fmt.Fprintln(w, "Insufficient Role to view this content")
						fmt.Fprintln(w, "Please make sure you have the role of BASIC")
					}
				case "admin":
					if claims.Role.Admin {
						for rs.HasNext() {
							rrow, err := rs.NextRow()
							if err != nil {
								fmt.Println("GetNextRow err:", err)
								panic("err GetNextRow")
							}

							str := rrow[0].(string)
							fmt.Fprintln(&b, str)
						}
						fmt.Fprintf(w, b.String())
						endpoint(w, r)
						return
					} else {
						w.WriteHeader(http.StatusUnauthorized)
						fmt.Fprintln(w, "Insufficient Role to view this content")
						fmt.Fprintln(w, "Please make sure you have the role of ADMIN")
					}
				case "advisor":
					if claims.Role.Advisor {
						for rs.HasNext() {
							rrow, err := rs.NextRow()
							if err != nil {
								fmt.Println("GetNextRow err:", err)
								panic("err GetNextRow")
							}

							str := rrow[0].(string)
							fmt.Fprintln(&b, str)
						}
						fmt.Fprintf(w, b.String())
						endpoint(w, r)
						return
					} else {
						w.WriteHeader(http.StatusUnauthorized)
						fmt.Fprintln(w, "Insufficient Role to view this content")
						fmt.Fprintln(w, "Please make sure you have the role of ADVISOR")
					}
				case "owner":
					if claims.Role.Owner {
						for rs.HasNext() {
							rrow, err := rs.NextRow()
							if err != nil {
								fmt.Println("GetNextRow err:", err)
								panic("err GetNextRow")
							}

							str := rrow[0].(string)
							fmt.Fprintln(&b, str)
						}
						fmt.Fprintf(w, b.String())
						endpoint(w, r)
						return
					} else {
						w.WriteHeader(http.StatusUnauthorized)
						fmt.Fprintln(w, "Insufficient Role to view this content")
						fmt.Fprintln(w, "Please make sure you have the role of OWNER")
					}

				default:
					w.WriteHeader(http.StatusUnauthorized)
					fmt.Fprintln(w, "Authorization Signature Invalid")
				}

			} else {
				log.Fatal("unknown claims type, cannot proceed")
			}

		} else {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}

	}
}
