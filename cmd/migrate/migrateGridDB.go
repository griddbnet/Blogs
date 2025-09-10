package migrate

import (
	"fmt"
	"log"
	"os"
	"strings"

	"github.com/Imisrael/griddb-cloud-cli/cmd/createContainer"
	"github.com/spf13/cobra"
)

var (
	force bool
)

func init() {
	migrateCmd.AddCommand(migrateGriddbCmd)
}

func migrateGriddb(dirName string) {

	c, err := os.ReadDir(dirName)
	if err != nil {
		log.Fatal(err)
	}
	var propFiles []string
	for _, entry := range c {
		name := entry.Name()
		if strings.Contains(name, ".json") && name != "gs_export.json" {
			propFiles = append(propFiles, dirName+"/"+name)
		}

	}

	fmt.Println(propFiles)

	for _, propFile := range propFiles {
		conInfo, csvFiles := createContainer.ParseJson(propFile)

		createContainer.Create(conInfo, force)
		containerName := conInfo.ContainerName
		types := mapping(conInfo.Columns)

		for _, file := range csvFiles {
			fileName := dirName + "/" + file
			allRows, err := readAllRows(fileName)
			// Chop off first four rows, they are meta data for import tool
			allRows = allRows[4:]
			if err != nil {
				log.Fatal(err)
			}
			processCSV(allRows, types, containerName, fileName)
		}
	}

}

var migrateGriddbCmd = &cobra.Command{
	Use:     "griddb",
	Short:   "Migrate from GridDB CE Export Files to Cloud",
	Long:    "Use the export tool on your GridDB CE Instance to create the dir output of csv files and a properties file and then migrate that table to GridDB Cloud",
	Example: "griddb-cloud-cli migrate griddb <directory>",
	Run: func(cmd *cobra.Command, args []string) {
		migrateGriddb(args[0])
	},
}
