The GridDB Cloud CLI Tool aims to make routine maintenance of checking of your GridDB Cloud instance a much easier endeavor. When we first [introduced it](https://griddb.net/en/blog/griddb-cloud-cli/), it was able to do your basic CRUD commands, but it lacked the ability to read from the filesystem to create containers/tables and to push data to those containers. Although maybe a subtle difference, essentially before, whenever the tool was run to CREATE, you needed manual intervention (known as interactive mode) to use the tool in this way.

I'm sure you know where I am going with this -- in this latest release, we have added some functionality revolved around reading JSON files from the filesystem which can help certain workflows and can help automate some testing that your Cloud, for instance. Being able to read from JSON programs to create tables also means we are able to migrate from GridDB CE to GridDB Cloud as discussed in this blog: []().

So for this article, since there's not much in the way of showcasing features, I will walk through the technical updates made to the tool, as well as a simple demo of how this tool can be useful.

## Creating Container from JSON File

First off, I changed the way GridDB Cloud CLI Tool handles the create command as explained above. From now on, when you use Create, if you want to create a new container by filling out CLI prompts, you can use the `-i` flag to indicate `--interactive` mode. So if you run: `griddb-cloud-cli create -i`, it will begin the process of asking a series of questions until you get the container you want. 

And now if you run `griddb-cloud-cli create` the tool will expect exactly one argument, a json file. The JSON file format is modeled to be the same as exported by the GridDB CE tool, here's an example: 

```json
{
    "version":"5.6.0",
    "database":"public",
    "container":"device",
    "containerType":"TIME_SERIES",
    "containerFileType":"csv",
    "containerFile":[
        "public.device_2020-07-11_2020-07-12.csv",
        "public.device_2020-07-12_2020-07-13.csv",
        "public.device_2020-07-13_2020-07-14.csv",
        "public.device_2020-07-14_2020-07-15.csv",
        "public.device_2020-07-15_2020-07-16.csv",
        "public.device_2020-07-16_2020-07-17.csv",
        "public.device_2020-07-17_2020-07-18.csv",
        "public.device_2020-07-18_2020-07-19.csv",
        "public.device_2020-07-19_2020-07-20.csv"
    ],
    "partitionNo":14,
    "columnSet":[
        {
            "columnName":"ts",
            "type":"timestamp",
            "notNull":true
        },
        {
            "columnName":"co",
            "type":"double",
            "notNull":false
        },
        {
            "columnName":"humidity",
            "type":"double",
            "notNull":false
        },
        {
            "columnName":"light",
            "type":"boolean",
            "notNull":false
        },
        {
            "columnName":"lpg",
            "type":"double",
            "notNull":false
        },
        {
            "columnName":"motion",
            "type":"boolean",
            "notNull":false
        },
        {
            "columnName":"smoke",
            "type":"double",
            "notNull":false
        },
        {
            "columnName":"temp",
            "type":"double",
            "notNull":false
        }
    ],
    "rowKeySet":[
        "ts"
    ],
    "timeSeriesProperties":{
        "compressionMethod":"NO",
        "compressionWindowSize":-1,
        "compressionWindowSizeUnit":"null",
        "expirationDivisionCount":-1,
        "rowExpirationElapsedTime":-1,
        "rowExpirationTimeUnit":"null"
    },
    "compressionInfoSet":[
    ],
    "timeIntervalInfo":[
        {
            "containerFile":"public.device_2020-07-11_2020-07-12.csv",
            "boundaryValue":"2020-07-11T17:00:00.000-0700"
        },
        {
            "containerFile":"public.device_2020-07-12_2020-07-13.csv",
            "boundaryValue":"2020-07-12T00:00:00.000-0700"
        },
        {
            "containerFile":"public.device_2020-07-13_2020-07-14.csv",
            "boundaryValue":"2020-07-13T00:00:00.000-0700"
        },
        {
            "containerFile":"public.device_2020-07-14_2020-07-15.csv",
            "boundaryValue":"2020-07-14T00:00:00.000-0700"
        },
        {
            "containerFile":"public.device_2020-07-15_2020-07-16.csv",
            "boundaryValue":"2020-07-15T00:00:00.000-0700"
        },
        {
            "containerFile":"public.device_2020-07-16_2020-07-17.csv",
            "boundaryValue":"2020-07-16T00:00:00.000-0700"
        },
        {
            "containerFile":"public.device_2020-07-17_2020-07-18.csv",
            "boundaryValue":"2020-07-17T00:00:00.000-0700"
        },
        {
            "containerFile":"public.device_2020-07-18_2020-07-19.csv",
            "boundaryValue":"2020-07-18T00:00:00.000-0700"
        },
        {
            "containerFile":"public.device_2020-07-19_2020-07-20.csv",
            "boundaryValue":"2020-07-19T00:00:00.000-0700"
        }
    ]
}
```

Now, obviously, if you were just writing your own json files, you wouldn't need a lot of the information from the migration tool, so instead you can create something with just the bare minimum.

```json
{
    "database":"public",
    "container":"device10",
    "containerType":"TIME_SERIES",
    "columnSet":[
        {
            "columnName":"ts",
            "type":"timestamp",
            "notNull":true
        },
        {
            "columnName":"co",
            "type":"double",
            "notNull":false
        },
        {
            "columnName":"humidity",
            "type":"double",
            "notNull":false
        },
        {
            "columnName":"light",
            "type":"boolean",
            "notNull":false
        },
        {
            "columnName":"lpg",
            "type":"double",
            "notNull":false
        },
        {
            "columnName":"motion",
            "type":"boolean",
            "notNull":false
        },
        {
            "columnName":"smoke",
            "type":"double",
            "notNull":false
        },
        {
            "columnName":"temp",
            "type":"double",
            "notNull":false
        }
    ],
    "rowKeySet":[
        "ts"
    ]
}
```

Small note, you can also use the `-f` flag to avoid the confirmation of the container you are about to create, again avoiding manual user input when commiting an action. 

### Technical Details of Implementing

Let's do a quick look at the underlying code of writing a program which can take the JSON file from the filesystem and push it to GridDB Cloud to be made into a new container. 

First, let's look at interpreting the file after being read from the filesystem:

```go
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
```

Here we read the file contents, create a data structure of type `ExportProperties` which was declared earlier (and shown below). We unmarshal (think of mapping) the json file to match our `struct` so that our program knows which values correspond to which keys. From there, we do some other processing to create the data object which will be sent to GriddB Cloud via an HTTP Web Request.

```go
// data struct to unmarshal user json file 
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
```

Here we define the entire struct which may read from either the user-made json, or the GridDB CE migration tool-generated json (notice the keys which are omitted in the user jsons have a special designation of `omitempty`). 

One last thing I'll point out is that we needed to use a custom unmarshaler for this specific struct (`ContainerFile`) because when the GridDB export tool outputs the meta json file, the containerFile value is sometimes a single string, and sometimes it's a slice of strings. Here's the custom unmarshaler: 

```go
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
```

## Examples Of Using New Features 

As explained above, you can now use the tool as part of your workflow and bash scripting. So you can, for example, run a script with cron to track your Downloads folder list of files and general file size. Let's take a look at a working example.

### Bash Script to Track Downloads Directory

As a silly example of how you can use the tool in your bash scripts, let's create a TIME_SERIES container which will keep track of the status of your downloads directory. We will create a json file with the container's schema, then we will use a simple bash script to push the current datetime and a couple of data points to our table. We then schedule a cron to run the script every 1 hour so that we have up to date data. 

First, the json file: 

```json
{
    "database": "public",
    "container": "download_data",
    "containerType": "TIME_SERIES",
    "columnSet": [
        {
            "columnName": "ts",
            "type": "timestamp",
            "notNull": true
        },
        {
            "columnName": "file_count",
            "type": "integer",
            "notNull": false
        },
        {
            "columnName": "total_size",
            "type": "string",
            "notNull": false
        }
    ]
}
```

And then our script, which will create the table (I realize it's not the best showcase as we are using just one table, but you could also create a situation where you push logs to a table and create new daily tables, kind of like fluentd), and then push up to date data of your directory: 

```bash
#!/bin/bash

DOWNLOADS_DIR="/Users/israelimru/Downloads"

FILE_COUNT=$(ls -1A "$DOWNLOADS_DIR" | wc -l | xargs)

TOTAL_SIZE=$(du -sh "$DOWNLOADS_DIR" | awk '{print $1}')

LOG_DATA="NOW(),$FILE_COUNT,$TOTAL_SIZE"

griddb-cloud-cli create /Users/israelimru/download_table.json -f
griddb-cloud-cli put -n download_data -v $LOG_DATA

echo "Log complete."
```

NOTE: It's crucial to use the `-f` flag when calling the create command because it won't ask for a prompt, it will simply create the table for you (and simply fail if the table already exists, which is fine!)

Here you can see our `LOG_DATA` uses the `NOW()` variable; this will be auto translated by the tool to be a current timestamp in the proper format. And then we just grab the relevant data points using simple linux commands and push that data to the Cloud. 

Then set up the cronjob: 

```bash
0 * * * * /your/script/testing.sh
```

## Conclusion

Being able to use the GridDB Cloud CLI Tool without user intervention opens up some new possibilities and we hope these changes can be useful for you and your team.