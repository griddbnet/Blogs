package main

import (
	"github.com/Imisrael/griddb-cloud-cli/cmd"
	_ "github.com/Imisrael/griddb-cloud-cli/cmd/checkConnection"
	_ "github.com/Imisrael/griddb-cloud-cli/cmd/containerInfo"
	_ "github.com/Imisrael/griddb-cloud-cli/cmd/createContainer"
	_ "github.com/Imisrael/griddb-cloud-cli/cmd/deleteContainer"
	_ "github.com/Imisrael/griddb-cloud-cli/cmd/ingest"
	_ "github.com/Imisrael/griddb-cloud-cli/cmd/listContainers"
	_ "github.com/Imisrael/griddb-cloud-cli/cmd/migrate"
	_ "github.com/Imisrael/griddb-cloud-cli/cmd/putRow"
	_ "github.com/Imisrael/griddb-cloud-cli/cmd/readContainer"
	_ "github.com/Imisrael/griddb-cloud-cli/cmd/sql"
)

func main() {
	cmd.Execute()
}
