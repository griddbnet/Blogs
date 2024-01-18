package main

import (
	"fmt"
	"log"
	"net/http"
)

func init() {
	createUsersContainer()
	createAdminContainer()
	createAdvisorContainer()
	createBasicContainer()
	createOwnerContainer()
}

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		http.Redirect(w, r, "/signIn", http.StatusSeeOther)
	})

	http.HandleFunc("/signUp", SignUp)
	http.HandleFunc("/signIn", SignIn)

	http.HandleFunc("/getToken", isAuthorized(GetToken))
	http.HandleFunc("/auth", isAuthorized(AuthPage))
	http.HandleFunc("/data", isAuthorized(DataEndPoints))

	http.HandleFunc("/basic", granularAuth(Basic))
	http.HandleFunc("/admin", granularAuth(Admin))
	http.HandleFunc("/advisor", granularAuth(Advisor))
	http.HandleFunc("/owner", granularAuth(Owner))

	fmt.Println("Listening on port :2828....")
	log.Fatal(http.ListenAndServe(":2828", nil))

}
