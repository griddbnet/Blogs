package ingest

import (
	"bytes"
	"encoding/csv"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"slices"
	"strconv"
	"strings"

	"github.com/Imisrael/griddb-cloud-cli/cmd"
	"github.com/Imisrael/griddb-cloud-cli/cmd/containerInfo"
	"github.com/Imisrael/griddb-cloud-cli/cmd/createContainer"
	"github.com/Imisrael/griddb-cloud-cli/cmd/listContainers"
	"github.com/Imisrael/griddb-cloud-cli/cmd/putRow"
	"github.com/cqroot/prompt"
	"github.com/spf13/cobra"
)

func init() {
	cmd.RootCmd.AddCommand(ingestCmd)
}

func readCSVFile(filename string) ([]byte, error) {
	f, err := os.Open(filename)
	if err != nil {
		return nil, err
	}
	defer f.Close()
	data, err := io.ReadAll(f)
	if err != nil {
		return nil, err
	}
	return data, nil
}

func parseCSV(data []byte) (*csv.Reader, error) {
	reader := csv.NewReader(bytes.NewReader(data))
	return reader, nil
}

func putMultiRows(arrayString, containerName string) {

	url := "/containers/" + containerName + "/rows"
	convert := []byte(arrayString)
	buf := bytes.NewBuffer(convert)

	client := &http.Client{}
	req, err := cmd.MakeNewRequest("PUT", url, buf)
	if err != nil {
		fmt.Println("Error making new request", err)
	}

	resp, err := client.Do(req)
	if err != nil {
		fmt.Println("error with client DO: ", err)
	}

	cmd.CheckForErrors(resp)

	fmt.Println(resp.Status)
}

func processCSV(reader *csv.Reader,
	header []string,
	containerName string,
	indexMapping map[string]int,
	typeMapping map[string]string,
) {
	//	fmt.Println(header)
	var idx int
	var stringOfValues string = "["
	for {
		record, err := reader.Read()
		if err == io.EOF {
			break
		} else if err != nil {
			fmt.Println("Error reading CSV data:", err)
			break
		}
		if idx != 0 {
			stringOfValues = stringOfValues + ", ["
		}

		for i, field := range header {

			//fmt.Printf("%s: %s\n", field, record[indexMapping[field]])
			if i == 0 {
				stringOfValues = stringOfValues + putRow.ConvertType(typeMapping[field], record[indexMapping[field]])
			} else {
				stringOfValues = stringOfValues + ",  " + putRow.ConvertType(typeMapping[field], record[indexMapping[field]])
			}
			idx++
		}
		stringOfValues = stringOfValues + "]"
		if idx%1000 == 0 {
			fmt.Println("Inserting 1000 rows")
			stringOfValues = "[" + stringOfValues + "]"
			putMultiRows(stringOfValues, containerName)
			idx = 0
			stringOfValues = "["
		}

	}
}

func ColsWithKnownNames(header []string, timeseries bool) []cmd.ContainerInfoColumns {

	n := len(header)
	var columnInfo []cmd.ContainerInfoColumns = make([]cmd.ContainerInfoColumns, n)
	for i := range n {

		var colType string
		var err error

		// if it's timeseries and first col, it's always set to ROWKEY=true and timestamp type
		if timeseries && i == 0 {
			colType, err = prompt.New().Ask("Col " + header[i] + "(TIMESTAMP CONTAINERS ARE LOCKED TO TIMESTAMP FOR THEIR ROWKEY)").
				Choose([]string{"TIMESTAMP"})
			cmd.CheckErr(err)
		} else {
			// User inputs col type for every other scenario
			colType, err = prompt.New().Ask("(" + header[i] + ") Column Type").
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

		columnInfo[i].Name = header[i]
		columnInfo[i].Type = colType

	}
	return columnInfo
}

// Similar to the function in createContainer, but this one assumes you have the col names already from the csv file
func containerInfoWithKnownNames(header []string) cmd.ContainerInfo {

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
			fmt.Println(err)
		}
		rowkey = val
	} else {
		timeseries = true
	}

	var colInfos []cmd.ContainerInfoColumns = ColsWithKnownNames(header, timeseries)

	retVal.ContainerName = containerName
	retVal.ContainerType = containerType
	retVal.RowKey = rowkey
	retVal.Columns = colInfos

	return retVal

}

func mapping(
	header, colNames []string,
	cols []cmd.ContainerInfoColumns,
) (map[string]int, map[string]string) {

	var indexMapping map[string]int = make(map[string]int)
	var typeMapping map[string]string = make(map[string]string)
	var correctOrder string
	var err error
	for i, val := range cols {
		fmt.Println(i, val.Name, header[i])
	}
	// Prints out the 1:1 mapping from how it already is. If true, continue as is.

	correctOrder, err = prompt.New().Ask("Is the above mapping correct?").
		Choose([]string{"YES", "NO"})
	cmd.CheckErr(err)
	if correctOrder == "YES" {
		fmt.Println("Ingesting. Please wait...")
		for i, val := range cols {
			indexMapping[header[i]] = i
			typeMapping[header[i]] = val.Type
		}
		return indexMapping, typeMapping
	} else {
		fmt.Println("We will now match eash csv header with col name, one by one")
		clone := slices.Clone(header)
		for i, val := range colNames {

			correctOrder, err = prompt.New().Ask(strconv.Itoa(i+1) + " of " + strconv.Itoa(len(colNames)) + ": Which csv header corresponds to column: " + val).
				Choose(clone)
			cmd.CheckErr(err)

			// find the current index position to match header and col and save into maps
			idx := slices.Index(header, correctOrder)
			indexMapping[header[i]] = idx
			typeMapping[header[i]] = cols[idx].Type

			//TODO Remove option once picked
			// fmt.Println(clone, i, idx)
			// clone = slices.Delete(clone, idx, idx+1)

		}
		// fmt.Println(indexMapping)
		// fmt.Println(typeMapping)
		return indexMapping, typeMapping
	}
}

func ingest(csvName string) {

	var containerName string
	var rawInfo []byte
	var indexMapping map[string]int = make(map[string]int)
	var typeMapping map[string]string = make(map[string]string)

	data, err := readCSVFile(csvName)
	if err != nil {
		fmt.Println("Error reading file:", err)
		return
	}
	reader, err := parseCSV(data)
	if err != nil {
		fmt.Println("Error creating CSV reader:", err)
		return
	}
	header, err := reader.Read()
	if err != nil {
		fmt.Println("Error reading header:", err)
		return
	}

	//First Question: Figure out if pushing to existing container
	exists, err := prompt.New().Ask("Does this container already exist?").
		Choose([]string{"YES", "NO"})
	cmd.CheckErr(err)

	// 1: Yes, find existing container
	if exists == "YES" {
		list := listContainers.ListContainers()
		containerName, err = prompt.New().Ask("Please select which CONTAINER you are ingesting data into").
			Choose(list)
		cmd.CheckErr(err)

		rawInfo = containerInfo.GetContainerInfo(containerName)

		correct, err := prompt.New().Ask("Is this the correct container? \n" + string(rawInfo)).
			Choose([]string{"YES", "NO"})
		cmd.CheckErr(err)

		// 1. Making sure it's the correct container
		if correct == "YES" {
			fmt.Println("Great. Proceeding. Next we will match CSV headers with their respect colunm names")
			var info cmd.ContainerInfo
			if err := json.Unmarshal(rawInfo, &info); err != nil {
				panic(err)
			}

			cols := info.Columns
			var colNames []string
			for _, val := range cols {
				colNames = append(colNames, val.Name)
			}

			if len(header) == len(cols) {
				indexMapping, typeMapping = mapping(header, colNames, cols)
			} else {
				log.Fatal("Length of columns not equal to length of headers present in CSV File. Did you choose the correct CONTAINER?")
			}

		} else {
			log.Fatal("Please try again.")
		}
	} else {

		// User will walk through creating new container to ingest into since it doesn't exist
		h := strings.Join(header, ",")
		h = strings.Replace(h, " ", "_", 10)
		h = strings.ToLower(h)
		fmt.Println("Use CSV Header names as your GridDB Container Col names? \n" + h)
		newHeader := strings.Split(h, ",")

		sameNames, err := prompt.New().Ask("Y/n").
			Choose([]string{"YES", "NO"})
		cmd.CheckErr(err)

		var containerToMake cmd.ContainerInfo
		if sameNames == "YES" {
			containerToMake = containerInfoWithKnownNames(newHeader)
			createContainer.Create(containerToMake, false)
		} else {
			containerToMake = createContainer.InteractiveContainerInfo(true, header)
			createContainer.Create(containerToMake, false)
		}
		fmt.Println("Container Created. Starting Ingest")

		cols := containerToMake.Columns
		containerName = containerToMake.ContainerName
		var colNames []string
		for _, val := range cols {
			colNames = append(colNames, val.Name)
		}
		indexMapping, typeMapping = mapping(header, colNames, cols)
	}

	processCSV(reader, header, containerName, indexMapping, typeMapping)
}

var ingestCmd = &cobra.Command{
	Use:     "ingest",
	Short:   "Ingest a `csv` file to a new or existing container",
	Long:    "Ingesting a csv file. You decide the type mapping an placement based on the interactive CLI options",
	Example: "griddb-cloud-cli ingest <file.csv>",
	Run: func(cmd *cobra.Command, args []string) {
		ingest(args[0])
	},
}
