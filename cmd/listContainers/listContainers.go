package listContainers

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strconv"

	"github.com/Imisrael/griddb-cloud-cli/cmd"
	"github.com/spf13/cobra"
)

func init() {
	cmd.RootCmd.AddCommand(listContainersCmd)
}

func ListContainers() []string {

	client := &http.Client{}
	req, err := cmd.MakeNewRequest("GET", "/containers?limit=100", nil)
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

	var listOfContainers cmd.ContainersList

	if err := json.Unmarshal(body, &listOfContainers); err != nil {
		panic(err)
	}

	return listOfContainers.Names
}

var listContainersCmd = &cobra.Command{
	Use:     "list",
	Short:   "Get a list of all of the containers",
	Long:    "The limit is set to 100 and is not configurable",
	Example: "griddb-cloud-cli list",
	Run: func(cmd *cobra.Command, args []string) {
		list := ListContainers()
		for i, name := range list {
			fmt.Println(strconv.Itoa(i) + ": " + name)
		}
	},
}
