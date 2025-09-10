package deleteContainer

import (
	"bytes"
	"fmt"
	"log"
	"net/http"
	"strings"

	"github.com/Imisrael/griddb-cloud-cli/cmd"
	"github.com/Imisrael/griddb-cloud-cli/cmd/containerInfo"
	"github.com/cqroot/prompt"
	"github.com/spf13/cobra"
)

var (
	multipleContainers []string
	force              bool
)

func init() {
	cmd.RootCmd.AddCommand(deleteContainerCmd)
	deleteContainerCmd.Flags().StringSliceVarP(&multipleContainers, "multi", "m", []string{}, "All containers to be deleted 'col1,col2,col3'")
	deleteContainerCmd.Flags().BoolVarP(&force, "force", "f", false, "Force delete (no prompt)")
}

type ContainerToBeDeleted string

func (c *ContainerToBeDeleted) wrapInDblQuotesAndBracket() {
	*c = "\"" + *c + "\""
	*c = "[" + *c + "]"
}

func (c *ContainerToBeDeleted) wrapInDblQuotes() {
	*c = "\"" + *c + "\""
}

func deleteRequest(payload []byte) {
	client := &http.Client{}

	buf := bytes.NewBuffer(payload)

	req, err := cmd.MakeNewRequest("DELETE", "/containers", buf)
	if err != nil {
		fmt.Println("Error making new request", err)
	}

	resp, err := client.Do(req)
	if err != nil {
		fmt.Println("error with client DO: ", err)
	}

	cmd.CheckForErrors(resp)

	fmt.Println(resp.StatusCode, "Successfully Deleted")
}

func deleteAllContainers() {
	var cont strings.Builder
	cont.WriteString("[")
	for idx, val := range multipleContainers {
		c := ContainerToBeDeleted(val)
		c.wrapInDblQuotes()
		if idx == 0 {
			cont.WriteString(string(c))
		}
		cont.WriteString(", " + string(c))
	}
	cont.WriteString("]")
	fmt.Println(cont.String())
	payload := []byte(cont.String())

	deleteRequest(payload)

}

func deleteContainer(containerName string) {

	info := containerInfo.GetContainerInfo(containerName)

	var containerToDelete = ContainerToBeDeleted(containerName)
	containerToDelete.wrapInDblQuotesAndBracket()
	payload := []byte(containerToDelete)

	if force {
		deleteRequest(payload)
		return
	}

	make, err := prompt.New().Ask("Delete Container? \n" + string(info)).
		Choose([]string{"NO", "YES"})
	cmd.CheckErr(err)

	if make == "NO" {
		log.Fatal("Aborting")
	} else {

		deleteRequest(payload)

	}
}

var deleteContainerCmd = &cobra.Command{
	Use:     "delete",
	Short:   "Test your Connection with GridDB Cloud",
	Long:    "A response of 200 is ideal, 401 is an auth error",
	Example: "griddb-cloud-cli checkConnection",
	Run: func(cmd *cobra.Command, args []string) {
		if len(multipleContainers) > 0 {
			deleteAllContainers()
		} else {
			if strings.Contains(args[0], ",") {
				log.Fatalln("Did you forget the '-m'/'multi' flag?")
			}
			deleteContainer(args[0])
		}

	},
}
