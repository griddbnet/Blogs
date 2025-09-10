[![Go Report Card](https://goreportcard.com/badge/github.com/Imisrael/griddb-cloud-cli)](https://goreportcard.com/report/github.com/Imisrael/griddb-cloud-cli)

A simple CLI tool wrapper for making HTTP Requests to your GridDB Cloud instance.

## Getting Started

To start, first gather your GridDB Cloud credentials and stick them in $HOME/.griddb.yaml (or, you can simply use the `--config` flag and point to your file when using the cli tool) Required fields:

```bash
cloud_url: "url"
cloud_username: "example"
cloud_pass: "pass"
```

## Examples

$ griddb-cloud-cli checkConnection

```bash
    200 OK
```

$ griddb-cloud-cli list 

```bash
0: actual_reading_1
1: actual_reading_10
2: boiler_control_10
3: device1
4: device2
5: device3
6: device4
7: device6
```

griddb-cloud-cli show device2        

```bash
{
    "container_name": "device2",
    "container_type": "TIME_SERIES",
    "rowkey": true,
    "columns": [
        {
            "name": "ts",
            "type": "TIMESTAMP",
            "timePrecision": "MILLISECOND",
            "index": []
        },
        {
            "name": "device",
            "type": "STRING",
            "index": []
        },
        {
            "name": "co",
            "type": "DOUBLE",
            "index": []
        },
        {
            "name": "humidity",
            "type": "FLOAT",
            "index": []
        },
        {
            "name": "light",
            "type": "BOOL",
            "index": []
        },
        {
            "name": "lpg",
            "type": "DOUBLE",
            "index": []
        },
        {
            "name": "motion",
            "type": "BOOL",
            "index": []
        },
        {
            "name": "smoke",
            "type": "DOUBLE",
            "index": []
        },
        {
            "name": "temperature",
            "type": "DOUBLE",
            "index": []
        }
    ]
}
```

$ griddb-cloud-cli read device2 --limit 1 --pretty

```bash
    [ { "name": "device2", "stmt": "select * limit 1", "columns": null, "hasPartialExecution": true }]

[
  [
    {
      "Name": "ts",
      "Type": "TIMESTAMP",
      "Value": "2006-01-02T07:04:05.700Z"
    },
    {
      "Name": "device",
      "Type": "STRING",
      "Value": "b8:27:eb:bf:9d:51"
    },
    {
      "Name": "co",
      "Type": "DOUBLE",
      "Value": 0.004955938648391245
    },
    {
      "Name": "humidity",
      "Type": "FLOAT",
      "Value": 51
    },
    {
      "Name": "light",
      "Type": "BOOL",
      "Value": false
    },
    {
      "Name": "lpg",
      "Type": "DOUBLE",
      "Value": 0.00765082227055719
    },
    {
      "Name": "motion",
      "Type": "BOOL",
      "Value": false
    },
    {
      "Name": "smoke",
      "Type": "DOUBLE",
      "Value": 0.02041127012241292
    },
    {
      "Name": "temperature",
      "Type": "DOUBLE",
      "Value": 22.7
    }
  ]
]
```

$ griddb-cloud-cli read device2 --limit 9 --rows

```bash
[ { "name": "device2", "stmt": "select * limit 9", "columns": null, "hasPartialExecution": true }]

ts,device,co,humidity,light,lpg,motion,smoke,temperature,
[2006-01-02T07:04:05.700Z b8:27:eb:bf:9d:51 0.004955938648391245 51 false 0.00765082227055719 false 0.02041127012241292 22.7]
[2020-07-11T17:01:34.700Z 00:0f:00:70:91:0a 0.0028400886071015706 76 false 0.005114383400977071 false 0.013274836704851536 19.700000762939453]
[2020-07-11T17:01:38.700Z b8:27:eb:bf:9d:51 0.004976012340421658 50.9 false 0.007673227406398091 false 0.02047512557617824 22.6]
[2020-07-11T17:01:39.700Z 1c:bf:ce:15:ec:4d 0.004403026829699689 76.8 true 0.007023337145877314 false 0.018628225377018803 27]
[2020-07-11T17:01:41.700Z b8:27:eb:bf:9d:51 0.004967363641908952 50.9 false 0.007663577282372411 false 0.020447620810233658 22.6]
[2020-07-11T17:01:44.700Z 1c:bf:ce:15:ec:4d 0.004391003954583357 77.9 true 0.007009458543138704 false 0.01858890754005078 27]
[2020-07-11T17:01:45.700Z b8:27:eb:bf:9d:51 0.004976025118224167 50.9 false 0.007673241660297752 false 0.020475166204362245 22.6]
[2020-07-11T17:01:46.700Z 00:0f:00:70:91:0a 0.0029381156266604295 76 false 0.005241481841731117 false 0.013627521132019194 19.700000762939453]
[2020-07-11T17:01:48.700Z 1c:bf:ce:15:ec:4d 0.004345471359573249 77.9 true 0.006956802377235561 false 0.01843978190211682 27]
```



$ griddb-cloud-cli read graph device2 -l 10

```bash
[ { "name": "device2", "stmt": "select * limit 10", "columns": null, "hasPartialExecution": true }]

Column ts (of type TIMESTAMP ) is not a `number` type. Omitting
Column device (of type STRING ) is not a `number` type. Omitting
Column light (of type BOOL ) is not a `number` type. Omitting
Column motion (of type BOOL ) is not a `number` type. Omitting
 77.90 ┤                                                             ╭╮                           ╭────────
 75.30 ┤           ╭─╮                     ╭──╮                     ╭╯╰╮                     ╭────╯
 72.71 ┤          ╭╯ ╰╮                   ╭╯  ╰╮                  ╭─╯  ╰╮                   ╭╯
 70.11 ┤        ╭─╯   ╰╮                 ╭╯    ╰╮                ╭╯     ╰─╮                ╭╯
 67.51 ┤       ╭╯      ╰─╮              ╭╯      ╰─╮             ╭╯        ╰╮             ╭─╯
 64.92 ┤      ╭╯         ╰╮           ╭─╯         ╰╮           ╭╯          ╰╮           ╭╯
 62.32 ┤    ╭─╯           ╰╮         ╭╯            ╰╮         ╭╯            ╰╮         ╭╯
 59.72 ┤   ╭╯              ╰─╮      ╭╯              ╰╮      ╭─╯              ╰╮      ╭─╯
 57.13 ┤  ╭╯                 ╰╮    ╭╯                ╰─╮   ╭╯                 ╰╮    ╭╯
 54.53 ┤ ╭╯                   ╰╮ ╭─╯                   ╰╮ ╭╯                   ╰─╮ ╭╯
 51.93 ┼─╯                     ╰─╯                      ╰─╯                      ╰─╯
 49.34 ┤
 46.74 ┤
 44.14 ┤
 41.55 ┤
 38.95 ┤
 36.35 ┤
 33.76 ┤
 31.16 ┤
 28.57 ┤
 25.97 ┤                              ╭────────────╮           ╭────────────╮                          ╭───
 23.37 ┼──╮                   ╭───────╯            ╰───────────╯            ╰───────╮             ╭────╯
 20.78 ┤  ╰───────────────────╯                                                     ╰─────────────╯
 18.18 ┤
 15.58 ┤
 12.99 ┤
 10.39 ┤
  7.79 ┤
  5.20 ┤
  2.60 ┤
  0.00 ┼───────────────────────────────────────────────────────────────────────────────────────────────────
                                          Col names from container device2

                                ■ co   ■ humidity   ■ lpg   ■ smoke   ■ temperature
```

\# Interactive mode with create and ingest

$ griddb-cloud-cli create --interactive

```bash
✔ Container Name: … sample1
✔ Choose: … TIME_SERIES
✔ How Many Columns for this Container? … 2
✔ Col name For col #1 … ts
✔ Col #1(TIMESTAMP CONTAINERS ARE LOCKED TO TIMESTAMP FOR THEIR ROWKEY) … TIMESTAMP
✔ Col name For col #2 … temp
✔ Column Type for col #2 … DOUBLE
✔ Make Container? 
{
    "container_name": "sample1",
    "container_type": "TIME_SERIES",
    "rowkey": true,
    "columns": [
        {
            "name": "ts",
            "type": "TIMESTAMP",
            "index": null
        },
        {
            "name": "temp",
            "type": "DOUBLE",
            "index": null
        }
    ]
} … YES
{"container_name":"sample1","container_type":"TIME_SERIES","rowkey":true,"columns":[{"name":"ts","type":"TIMESTAMP","index":null},{"name":"temp","type":"DOUBLE","index":null}]}
201 Created
```

\# Create container with json

```bash
$ griddb-cloud-cli create sample.json -f 

{  sample COLLECTION  [] [{ts timestamp true} {name string false} {temp float[] false} {counts long[] false} {names string[] false} {daties timestamp[] true}] [name]}
{"container_name":"gorg","container_type":"COLLECTION","rowkey":true,"columns":[{"name":"ts","type":"TIMESTAMP","index":null},{"name":"name","type":"STRING","index":null},{"name":"temp","type":"FLOAT_ARRAY","index":null},{"name":"counts","type":"LONG_ARRAY","index":null},{"name":"names","type":"STRING_ARRAY","index":null},{"name":"daties","type":"TIMESTAMP_ARRAY","index":null}]}
201 Created
```


$ griddb-cloud-cli put sample1

```bash
Container Name: sample1
✔ Column 1 of 2
 Column Name: ts
 Column Type: TIMESTAMP … NOW()
✔ Column 2 of 2
 Column Name: temp
 Column Type: DOUBLE … 20.2
[["2025-04-30T07:43:03.700Z",  20.2]]
✔ Add the Following to container sample1? … YES
200 OK
```

$ griddb-cloud-cli ingest iot_telemetry_data.csv

```bash
✔ Does this container already exist? … NO
Use CSV Header names as your GridDB Container Col names? 
ts,device,co,humidity,light,lpg,motion,smoke,temp
✔ Y/n … YES
✔ Container Name: … device6
✔ Choose: … TIME_SERIES
✔ Col ts(TIMESTAMP CONTAINERS ARE LOCKED TO TIMESTAMP FOR THEIR ROWKEY) … TIMESTAMP
✔ (device) Column Type … STRING
✔ (co) Column Type … DOUBLE
✔ (humidity) Column Type … DOUBLE
✔ (light) Column Type … BOOL
✔ (lpg) Column Type … DOUBLE
✔ (motion) Column Type … BOOL
✔ (smoke) Column Type … DOUBLE
✔ (temp) Column Type … DOUBLE
        },
        {
            "name": "device",
            "type": "STRING",
            "index": null
        },
        {
            "name": "co",
            "type": "DOUBLE",
            "index": null
        },
        {
            "name": "humidity",
            "type": "DOUBLE",
            "index": null
        },
        {
            "name": "light",
            "type": "BOOL",
            "index": null
        },
        {
            "name": "lpg",
            "type": "DOUBLE",
            "index": null
        },
        {
            "name": "motion",
            "type": "BOOL",
            "index": null
        },
        {
            "name": "smoke",
            "type": "DOUBLE",
            "index": null
        },
        {
            "name": "temp",
            "type": "DOUBLE",
            "index": null
        }
    ]
} … YES
{"container_name":"device6","container_type":"TIME_SERIES","rowkey":true,"columns":[{"name":"ts","type":"TIMESTAMP","index":null},{"name":"device","type":"STRING","index":null},{"name":"co","type":"DOUBLE","index":null},{"name":"humidity","type":"DOUBLE","index":null},{"name":"light","type":"BOOL","index":null},{"name":"lpg","type":"DOUBLE","index":null},{"name":"motion","type":"BOOL","index":null},{"name":"smoke","type":"DOUBLE","index":null},{"name":"temp","type":"DOUBLE","index":null}]}
201 Created

Container Created. Starting Ingest

0 ts ts
1 device device
2 co co
3 humidity humidity
4 light light
5 lpg lpg
6 motion motion
7 smoke smoke
8 temp temp
✔ Is the above mapping correct? … YES
Ingesting. Please wait...
Inserting 1000 rows
200 OK
Inserting 1000 rows
200 OK
Inserting 1000 rows
```

## SQL Examples


$ griddb-cloud-cli sql create -s "CREATE TABLE IF NOT EXISTS pyIntPart1 (date TIMESTAMP NOT NULL PRIMARY KEY, value STRING) WITH (expiration_type='PARTITION',expiration_time=10,expiration_time_unit='DAY') PARTITION BY RANGE (date) EVERY (5, DAY);"

```bash
[{"stmt": "CREATE TABLE IF NOT EXISTS pyIntPart1 (date TIMESTAMP NOT NULL PRIMARY KEY, value STRING) WITH (expiration_type='PARTITION',expiration_time=10,expiration_time_unit='DAY') PARTITION BY RANGE (date) EVERY (5, DAY);" }]
```

$ griddb-cloud-cli sql update -s "INSERT INTO pyIntPart2(date, value) VALUES (NOW(), 'fourth')"

```bash
[{"stmt": "INSERT INTO pyIntPart2(date, value) VALUES (NOW(), 'fourth')" }]
[{"updatedRows":1,"status":1,"message":null,"stmt":"INSERT INTO pyIntPart2(date, value) VALUES (NOW(), 'fourth')"}]
```

$ griddb-cloud-cli sql query -s "select * from pyIntPart2 limit 1" --pretty

```bash
[{"stmt": "select * from pyIntPart2 limit 1" }]

[
    [
        {
            "Name": "date",
            "Type": "TIMESTAMP",
            "Value": "2025-04-30T14:58:00.255Z"
        },
        {
            "Name": "value",
            "Type": "STRING",
            "Value": "fourth"
        }
    ]
]
```