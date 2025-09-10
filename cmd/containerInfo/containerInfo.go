package containerInfo

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"

	"github.com/Imisrael/griddb-cloud-cli/cmd"
	"github.com/spf13/cobra"
)

var (
	raw bool
)

func init() {
	cmd.RootCmd.AddCommand(containerInfo)
	containerInfo.Flags().BoolVar(&raw, "raw", false, "When enabled, will simply output direct results from GridDB Cloud")
}

func GetContainerInfo(containerName string) []byte {

	client := &http.Client{}
	req, err := cmd.MakeNewRequest("GET", "/containers/"+containerName+"/info", nil)
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
		return []byte{}
	}

	var info cmd.ContainerInfo
	if err := json.Unmarshal(body, &info); err != nil {
		panic(err)
	}

	jso, err := json.MarshalIndent(info, "", "    ")
	if err != nil {
		fmt.Println("Error", err)
	}

	return jso
}

var containerInfo = &cobra.Command{
	Use:     "show",
	Short:   "get container info ",
	Long:    "Show container information from the cloud",
	Example: "griddb-cloud-cli show device2",
	Run: func(cmd *cobra.Command, args []string) {
		json := GetContainerInfo(args[0])
		fmt.Println(string(json))
	},
}
