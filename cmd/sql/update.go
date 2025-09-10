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
	sqlCmd.AddCommand(updateCmd)
	updateCmd.Flags().StringVarP(&userSqlString, "string", "s", "", "SQL STRING")
	updateCmd.MarkFlagRequired("string")
}

func runUpdate() {
	client := &http.Client{}

	var s = SqlString(userSqlString)
	s.wrapInDblQuoteAndStmt()
	fmt.Println(s)
	convert := []byte(s)

	buf := bytes.NewBuffer(convert)

	url := "/sql/dml/update"
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

var updateCmd = &cobra.Command{
	Use:     "update",
	Short:   "Run a sql command",
	Long:    "Run a DML Sql Command to Query your GridDB Container",
	Example: "sql update -s \"INSERT INTO pyIntPart2(date, value) VALUES (NOW(), 'fourth')\"",
	Run: func(cmd *cobra.Command, args []string) {
		runUpdate()
	},
}
