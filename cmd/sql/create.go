package sql

import (
	"bytes"
	"fmt"
	"io"
	"net/http"

	"github.com/Imisrael/griddb-cloud-cli/cmd"
	"github.com/spf13/cobra"
)

func init() {
	sqlCmd.AddCommand(createCmd)
	createCmd.Flags().StringVarP(&userSqlString, "string", "s", "", "SQL STRING")
	createCmd.MarkFlagRequired("string")
}

func runCreate() {
	client := &http.Client{}

	var s = SqlString(userSqlString)
	s.wrapInDblQuoteAndStmt()
	fmt.Println(s)

	convert := []byte(s)
	buf := bytes.NewBuffer(convert)

	url := "/sql/ddl"
	req, err := cmd.MakeNewRequest("POST", url, buf)
	if err != nil {
		fmt.Println("Error making new request", err)
	}

	resp, err := client.Do(req)
	if err != nil {
		fmt.Println("error with client DO: ", err)
	}
	cmd.CheckForErrors(resp)

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("Error with reading body! ", err)
	}
	fmt.Println(string(body))
}

var createCmd = &cobra.Command{
	Use:     "create",
	Short:   "Run a sql command",
	Long:    "Run a DDL Sql Command to Create or Alter or Update your SQL Tables",
	Example: "\"CREATE TABLE IF NOT EXISTS pyIntPart2 (date TIMESTAMP NOT NULL PRIMARY KEY, value STRING) WITH (expiration_type='PARTITION',expiration_time=10,expiration_time_unit='DAY') PARTITION BY RANGE (date) EVERY (5, DAY);\"",
	Run: func(cmd *cobra.Command, args []string) {
		runCreate()
	},
}
