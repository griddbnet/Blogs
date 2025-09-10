package createContainer

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"reflect"
	"strconv"
	"strings"

	"github.com/Imisrael/griddb-cloud-cli/cmd"
	"github.com/cqroot/prompt"
	"github.com/cqroot/prompt/input"
	"github.com/spf13/cobra"
)

var (
	interactive bool
	force       bool
)

type ColumnSet struct {
	ColumnName string `json:"columnName"`
	Type       string `json:"type"`
	NotNull    bool   `json:"notNull"`
}

type ContainerFile []string

type ExportProperties struct {
	Version           string        `json:"version,omitempty"`
	Database          string        `json:"database,omitempty"`
	Container         string        `json:"container"`
	ContainerType     string        `json:"containerType,omitempty"`
	ContainerFileType string        `json:"containerFileType,omitempty"`
	ContainerFile     ContainerFile `json:"containerFile"`
	ColumnSet         []ColumnSet   `json:"columnSet"`
	RowKeySet         []string      `json:"rowKeySet"`
}

// custom JSON unmarshaler for the case where sometimes the value is a slice
// and sometimes it's just a singular string
func (c *ContainerFile) UnmarshalJSON(data []byte) error {
	var nums any
	err := json.Unmarshal(data, &nums)
	if err != nil {
		return err
	}

	items := reflect.ValueOf(nums)
	switch items.Kind() {
	case reflect.String:
		*c = append(*c, items.String())

	case reflect.Slice:
		*c = make(ContainerFile, 0, items.Len())
		for i := 0; i < items.Len(); i++ {
			item := items.Index(i)
			switch item.Kind() {
			case reflect.String:
				*c = append(*c, item.String())
			case reflect.Interface:
				*c = append(*c, item.Interface().(string))
			}
		}
	}
	return nil
}

func init() {
	cmd.RootCmd.AddCommand(createContainerCmd)
	createContainerCmd.Flags().BoolVarP(&interactive, "interactive", "i", false, "When enabled, goes through interactive to make cols and types")
	createContainerCmd.Flags().BoolVarP(&force, "force", "f", false, "Force create (no prompt)")
}

func ColDeclaration(numberOfCols int, timeseries bool) []cmd.ContainerInfoColumns {
	if numberOfCols < 1 {
		log.Fatal("Please pick a number greater than 0 for the number of cols")
	}
	var columnInfo []cmd.ContainerInfoColumns = make([]cmd.ContainerInfoColumns, numberOfCols)
	for i := range numberOfCols {

		colName, err := prompt.New().Ask("Col name For col #" + strconv.Itoa(i+1)).Input("temperature")
		cmd.CheckErr(err)
		var colType string

		// if it's timeseries and first col, it's always set to ROWKEY=true and timestamp type
		if timeseries && i == 0 {
			colType, err = prompt.New().Ask("Col #" + strconv.Itoa(i+1) + "(TIMESTAMP CONTAINERS ARE LOCKED TO TIMESTAMP FOR THEIR ROWKEY)").
				Choose([]string{"TIMESTAMP"})
			cmd.CheckErr(err)
		} else {
			// User inputs col type for every other scenario
			colType, err = prompt.New().Ask("Column Type for col #" + strconv.Itoa(i+1)).
				Choose(cmd.GridDBTypes)
			cmd.CheckErr(err)
		}

		// if collection type and first col, you must set an index type.
		if !timeseries && i == 0 {
			colIndex, err := prompt.New().Ask("Column Index Type" + strconv.Itoa(i+1)).
				Choose([]string{"none (default)", "TREE", "SPATIAL"})
			cmd.CheckErr(err)
			var indexArr []string = make([]string, 1)
			if colIndex == "none (default)" {
				columnInfo[i].Index = nil
			} else {
				indexArr[0] = colIndex
				columnInfo[i].Index = indexArr
			}

		}

		columnInfo[i].Name = colName
		columnInfo[i].Type = colType

	}
	return columnInfo
}

func typeSwitcher(s string) string {
	switch s {
	case "boolean":
		return "BOOL"
	case "boolean[]":
		return "BOOL_ARRAY"
	case "string[]":
		return "STRING_ARRAY"
	case "byte[]":
		return "BYTE_ARRAY"
	case "short[]":
		return "SHORT_ARRAY"
	case "integer[]":
		return "INTEGER_ARRAY"
	case "long[]":
		return "LONG_ARRAY"
	case "float[]":
		return "FLOAT_ARRAY"
	case "double[]":
		return "DOUBLE_ARRAY"
	case "timestamp[]":
		return "TIMESTAMP_ARRAY"
	default:
		return strings.ToUpper(s)

	}

}

func transformToConInfoCols(colSet []ColumnSet) []cmd.ContainerInfoColumns {
	n := len(colSet)
	var conInfoCols = make([]cmd.ContainerInfoColumns, n)

	for idx, val := range colSet {
		conInfoCols[idx].Name = val.ColumnName
		conInfoCols[idx].Type = typeSwitcher(val.Type)
		//conInfoCols[idx].Index = []string{}
	}
	return conInfoCols
}

func ParseJson(jsonName string) (cmd.ContainerInfo, []string) {

	filename := jsonName
	properties, err := os.ReadFile(filename)
	if err != nil {
		log.Fatal(err)
	}
	var exportProperties ExportProperties
	err = json.Unmarshal(properties, &exportProperties)
	if err != nil {
		log.Fatal(err)
	}
	//fmt.Println(exportProperties)

	var conInfo cmd.ContainerInfo

	conInfo.ContainerName = exportProperties.Container
	conInfo.ContainerType = exportProperties.ContainerType
	conInfo.RowKey = len(exportProperties.RowKeySet) > 0

	cols := transformToConInfoCols(exportProperties.ColumnSet)
	conInfo.Columns = cols

	return conInfo, exportProperties.ContainerFile
}

func InteractiveContainerInfo(ingest bool, header []string) cmd.ContainerInfo {

	var retVal cmd.ContainerInfo
	var rowkey bool = true
	var timeseries bool = false

	containerName, err := prompt.New().Ask("Container Name:").Input("device2")
	cmd.CheckErr(err)

	containerType, err := prompt.New().Ask("Choose:").
		Choose([]string{"COLLECTION", "TIME_SERIES"})
	cmd.CheckErr(err)

	if containerType == "COLLECTION" {
		rk, err := prompt.New().Ask("Row Key?").
			Choose([]string{"true", "false"})
		cmd.CheckErr(err)
		val, err := strconv.ParseBool(rk)
		if err != nil {
			log.Fatal(err)
		}
		rowkey = val
	} else {
		timeseries = true
	}

	var colInfos []cmd.ContainerInfoColumns

	if ingest {
		colInfos = ColDeclaration(len(header), timeseries)
	} else {
		numberOfCols, err := prompt.New().Ask("How Many Columns for this Container?").
			Input("", input.WithInputMode(input.InputInteger))
		cmd.CheckErr(err)

		numOfCols, err := strconv.Atoi(numberOfCols)
		if err != nil {
			log.Fatal("ERROR", err)
		}
		colInfos = ColDeclaration(numOfCols, timeseries)
	}

	retVal.ContainerName = containerName
	retVal.ContainerType = containerType
	retVal.RowKey = rowkey
	retVal.Columns = colInfos

	return retVal

}

func Create(conInfo cmd.ContainerInfo, migrateForce bool) {

	jsonContainerInfo, err := json.Marshal(conInfo)
	if err != nil {
		log.Fatal("Error", err)
	}
	fmt.Println(string(jsonContainerInfo))
	convert := []byte(jsonContainerInfo)
	buf := bytes.NewBuffer(convert)

	client := &http.Client{}
	req, err := cmd.MakeNewRequest("POST", "/containers", buf)
	if err != nil {
		log.Fatal("Error making new request", err)
	}

	if force || migrateForce {
		resp, err := client.Do(req)
		if err != nil {
			log.Fatal("error with client DO: ", err)
		}

		cmd.CheckForErrors(resp)

		fmt.Println(resp.Status)
		return
	}

	jsoPrettyPrint, err := json.MarshalIndent(conInfo, "", "    ")
	if err != nil {
		log.Fatal("Error", err)
	}

	make, err := prompt.New().Ask("Make Container? \n" + string(jsoPrettyPrint)).
		Choose([]string{"YES", "NO"})
	cmd.CheckErr(err)

	if make == "NO" {
		log.Fatal("Aborting")
	} else {

		resp, err := client.Do(req)
		if err != nil {
			log.Fatal("error with client DO: ", err)
		}

		cmd.CheckForErrors(resp)

		fmt.Println(resp.Status)
	}

}

var createContainerCmd = &cobra.Command{
	Use:   "create",
	Short: "Create A container TIME_SERIES or COLLECTION container",
	Long: `Create a container through a series of prompts in interactive mode, or through a json file in your filesystem. Here's an example of a json file: 
{
    "database": "public",
    "container": "device1",
    "containerType": "TIME_SERIES",
    "columnSet": [
        {
            "columnName": "ts",
            "type": "timestamp",
            "notNull": true
        },
        {
            "columnName": "temperature",
            "type": "float",
            "notNull": false
        },
        {
            "columnName": "data",
            "type": "double[]",
            "notNull": false
        }
    ]
}`,
	Example: "griddb-cloud-cli create table1.json or griddb-cloud-cli create -i",
	Run: func(cmd *cobra.Command, args []string) {

		var ingest bool = false
		if interactive {
			conInfo := InteractiveContainerInfo(ingest, nil)
			Create(conInfo, false)
		} else {
			if len(args) != 1 {
				if len(args) == 0 {
					log.Fatal("Please add a json file as an argument")
				} else {
					log.Fatal("Please only select one json file at a time")
				}
			}

			conInfo, _ := ParseJson(args[0])
			Create(conInfo, false)
		}

	},
}
