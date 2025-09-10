package readContainer

import (
	"fmt"
	"log"

	"github.com/Imisrael/griddb-cloud-cli/cmd"
	"github.com/guptarohit/asciigraph"
	"github.com/spf13/cobra"
)

func init() {
	readContainerCmd.AddCommand(readIntoGraph)
	readIntoGraph.Flags().IntVar(&offset, "offset", 0, "How many rows you'd like to offset in your query")
	readIntoGraph.Flags().IntVarP(&limit, "limit", "l", 50, "How many rows you'd like to limit")
	readIntoGraph.Flags().IntVar(&height, "height", 30, "Line Height. Default is 30, 0 is auto-scaled")
	readIntoGraph.Flags().StringSliceVar(&columns, "columns", []string{}, "Which columns would you like to see printed, IMPORTANT: no spaces between col names, just a comma like this: 'col1,col2,col3'")
}

func graphIt(data [][]cmd.QueryData, containerName string) {

	var m map[string][]float64 = make(map[string][]float64)
	var rejects map[string]string = make(map[string]string)

	for i := range data {
		for j := range data[i] {
			if data[i][j].Type == "FLOAT" || data[i][j].Type == "INTEGER" || data[i][j].Type == "DOUBLE" {
				m[data[i][j].Name] = append(m[data[i][j].Name], data[i][j].Value.(float64))
			} else {
				rejects[data[i][j].Name] = data[i][j].Type
			}
		}
	}

	var rows [][]float64 = make([][]float64, len(m))
	var colNames []string = make([]string, len(m))

	var i int
	for rowName, rowValue := range m {
		rows[i] = make([]float64, len(rowValue))
		rows[i] = rowValue
		colNames[i] = rowName
		i++
	}
	for rejectName, rejectType := range rejects {
		fmt.Println("Column " + rejectName + " (of type " + rejectType + " ) is not a `number` type. Omitting")
	}

	graph := asciigraph.PlotMany(
		rows,
		asciigraph.Height(height),
		asciigraph.SeriesColors(asciigraph.Red, asciigraph.Green, asciigraph.Blue, asciigraph.Pink, asciigraph.Orange),
		asciigraph.SeriesLegends(colNames...),
		asciigraph.Caption("Col names from container "+containerName),
		asciigraph.Width(100),
	)
	fmt.Println(graph)
}

var readIntoGraph = &cobra.Command{
	Use:     "graph",
	Short:   "Read container",
	Long:    "Read container and print out line graph",
	Example: "griddb-cloud-cli read graph device2 --limit 50 --columns \"co,humidity,temp\" --height 0",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) > 1 {
			log.Fatal("you may only read from one container at a time")
		} else if len(args) == 1 {
			data := readTql(args[0], true)
			graphIt(data, args[0])
		} else {
			log.Fatal("Please include the container name you'd like to read from!")
		}

	},
}
