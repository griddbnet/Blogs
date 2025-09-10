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
	sqlCmd.AddCommand(queryCmd)
	queryCmd.Flags().StringVarP(&userSqlString, "string", "s", "", "SQL STRING")
	queryCmd.Flags().BoolVarP(&pretty, "pretty", "p", false, "Pretty print?")
	queryCmd.MarkFlagRequired("string")
	queryCmd.Flags().BoolVar(&raw, "raw", false, "Print raw Cloud Results?")
	queryCmd.Flags().BoolVarP(&showOnlyRows, "rows", "r", false, "Just print rows with no col info")
}

func runQuery() {
	client := &http.Client{}

	var s SqlString = SqlString(userSqlString)
	s.wrapInDblQuoteAndStmt()
	fmt.Println(s)

	convert := []byte(s)
	buf := bytes.NewBuffer(convert)

	url := "/sql/dml/query"
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

	if raw {
		fmt.Println(string(body))
	} else {
		parsed := prettyPrint(body, pretty, showOnlyRows)
		fmt.Println(string(parsed))
	}

}

var queryCmd = &cobra.Command{
	Use:     "query",
	Short:   "Run a sql command",
	Long:    "Run a DML Sql Command to Query your GridDB Container",
	Example: "sql query -s \"SELECT * FROM pyIntPart2\"",
	Run: func(cmd *cobra.Command, args []string) {

		runQuery()
	},
}
