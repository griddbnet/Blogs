In a previous article, we showcased how one could pair GridDB Cloud's free infastructure with Kafka using a custom Single Message Transform and some SSL certs/rules; you can read that article here: [TODO: Add Blog Link](). In this article, we will expand on those efforts and add timestamp data types into the mix. By the time you finish this article, you should be able to understand how you can stream data from some source over to GridDB Cloud, with the added benefit of being able to push to time series containers which take timestamps as their rowkey (a must!)

As stated above, the big addition for this article is the handling of time series data and pushing it out into the GridDB Cloud. There were two things that had to be learned in order to get this project to work: chaining together Single Message Transforms, and learning the exact time format the GridDB WebAPI accepts as acceptable for time series data; there was also a miniscule change made to the SMT we used in the previous article.

## Prereqs

This article is part II, and therefore a continuation of a previous effort; in part I, we go over the fundamentals of what this project is and how it works This means that understanding part I of this series is a pseudo-prerequisite for this article but is not necessarily required. 

In any case, the prereqs for both of these articles are the same:

1. A Kafka system/cluster running (docker is the easiest way)
2. The source code for the custom Single Message Transform (or the pre-compiled .jar)
3. The GridDB Web API Sink connector (just connection details to make it yourself)

The source code (and all of the required configs/yamls) can found on the GridDB.net github page:

`$ git clone https://github.com/griddbnet/Blogs.git --branch kafka_http_timeseries`

## Implementation

Most of the code implementation for this project was done in the previous effort, but there are still some changes we need to make to the existing code base. Mostly though, we will be using an existing Single Message Transform to be able to send time series data to GridDB Cloud. The way it works is this: an SMT allows for to transforming the Kafka records ***before*** it gets sent over to your Kafka sink. It also allows for using multiple SMTs (executed in order) before the data gets sent out.

### Chaining Single Message Transforms

In part I of this series, we used our custom SMT to decouple the values from the field names from our Kafka record and form it into a nested array, which is the only data struct that a `PUT` to GridDB Cloud accepts. Using just this alone, we were able to successfully push data to a GridDB Collection container. 

However, when dealing with time series containers, an issue arises because the WebAPI expects a very specific data format for the time series data column. If your data is in milliseconds since epoch, for example, the GridDB WebAPI will not accept that as a valid time column type and will reject the HTTP Request. According to the [docs](https://github.com/griddb/webapi/blob/master/GridDB_Web_API_Reference.md), the format expected by GridDB WebAPI is this: `YYYY-MM-DDThh:mm:ss.SSSZ (ie. "2016-01-16T10:25:00.253Z")`.

So, before we transform our data to extract the values and create our nested array, we can run a Single Message Transform on just the `ts` column, transform whatever the value is into the format it likes, and *then* run the process of building our nested array. Using this flow allows for us to push data successfully but to also transform the timestamp column into the exact format expected. And please remember, the order of your transforms matter!

```bash
    "transforms.timestamp.type": "org.apache.kafka.connect.transforms.TimestampConverter$Value",
    "transforms.timestamp.target.type": "string",
    "transforms.timestamp.field": "ts",
    "transforms.timestamp.format": "yyyy-MM-dd'\''T'\''HH:mm:ss.SSS'\''Z'\'' ",
    "transforms.nestedList.type": "net.griddb.GridDBWebAPITransform$Value",
    "transforms.nestedList.fields": "ts",
```

Here you see we target the `ts` column and we explicitly state the format we expect. One small gotcha is that you must wrap the `T` and `Z` characters in single quotes otherwise Kafka will reject the format as `illegal`. And of course, if you deviate from this format at all, you will be rejected by the GridDB Cloud -- ouch!


### Handling Strings Sent to GridDB WebAPI

Now that we've got our SMTs in place, there's one more 'gotcha' to investigate. The GridDB Web API expects the timestamp to be wrapped in double quotes, and so we need to make a small change to our SMT from part I of this article:

```java
    Object val = fPath.valueFrom(value);
    String valType = val.getClass().getName();
    if (valType.contains("String")) {
        val = "\"" + val + "\"";
        row.add(val);
    } else {
        row.add(val);
    }
```

Luckily for us, the WebAPI expects *all* strings to be wrapped in double quotes, so we don't need to do any explicit checking if the value is a timestamp or not, we just need to check if the value is a string. Once we have this settled, fire up the connector (you can run the script inside of the `scripts/` dir just please make the necessary changes before you do so) and then create some topics.

### Creating Topics with Schemas Using the Control Center 

Now that we've got our infrastructure in place, let's run it! 

First, please make sure the GridDB Cloud URL you're using points to a real container already in place in your db. In my case, I made a time series container called `kafka_ts` and gave it a schema of: ts (timestamp), data (float), temp (float). This container is already being pointed to in the URL of my sink connector.

With that out of the way, let's make our topic and schema. If you used the script to create the connector, your topic may be named `topic_griddb_cloud`, so head into your Kafka control-center (located in http://localhost:9021) and create a new topic. From the Schema tab, you can copy and paste the following schema: 

```json
{
  "connect.name": "net.griddb.webapi.griddb",
  "connect.parameters": {
    "io.confluent.connect.avro.field.doc.data": "The string is a unicode character sequence.",
    "io.confluent.connect.avro.field.doc.temp": "The double type is a double precision (64-bit) IEEE 754 floating-point number.",
    "io.confluent.connect.avro.field.doc.ts": "The int type is a 32-bit signed integer.",
    "io.confluent.connect.avro.record.doc": "Sample schema to help you get started."
  },
  "doc": "Sample schema to help you get started.",
  "fields": [
    {
      "doc": "The int type is a 32-bit signed integer.",
      "name": "ts",
      "type": "string"
    },
    {
      "doc": "The double type is a double precision (64-bit) IEEE 754 floating-point number.",
      "name": "temp",
      "type": "double"
    },
    {
      "doc": "The string is a unicode character sequence.",
      "name": "data",
      "type": "double"
    }
  ],
  "name": "griddb",
  "namespace": "net.griddb.webapi",
  "type": "record"
}
```

Once created, from the messages tab, `produce` a new message like so: 

```json
{
  "ts": "2025-03-13T18:00:00.032Z",
  "data": 23.2,
  "temp": 43.23
}
```

If all goes well, your sink should still be running and you should have a new row of data inside of your container -- cool!!

### Troubleshooting

While preparing for this article, I had lots of issues getting everything to run properly, despite the results showing how relatively simple it is. There are two reasons for that: 1, debugging was pretty obtuse as the logs are extremely difficult to follow, and 2, the schema rules are extremely finicky and must be precise for Kafka to follow through on streaming data (which is a good thing!). 

So, if you are encountering issues, I recommend first changing the log level of your Kafka cluster's connect container from just "WARN" (default) to either DEBUG or TRACE. I'd start with DEBUG and move up if necessary as the TRACE logs move extremely quickly and are difficult to read. You can change the log level with Docker by adding some environment variables and doing a hard reset of your containers. Add this to the bottom of your Kafka connect docker-compose section

```bash
#docker-compose.yml
connect:
    image: cnfldemos/cp-server-connect-datagen:0.6.4-7.6.0
    hostname: connect
    container_name: connect
    depends_on:
      - broker
      - schema-registry
    ports:
      - "8083:8083"
      - "2929:2929"
      - "443:443"
    environment:
      CONNECT_LOG4J_ROOT_LOGLEVEL: DEBUG # Or TRACE for even more detail
      CONNECT_LOG4J_LOGGERS: org.apache.kafka.connect=DEBUG,io.confluent.connect=DEBUG

$ docker compose up -d --force-recreate
```

And once it's printing out the enormous amounts of logs, you can narrow down what you're searching for using grep

```bash
$ dockers logs -f connect | grep Caused

Caused by: org.apache.kafka.connect.errors.DataException: Failed to deserialize data for topic topic_griddb_cloud to Avro: 
Caused by: org.apache.kafka.common.errors.SerializationException: Unknown magic byte!
```

I've found `Caused` to be the best way to debug the issues with the connectors, but you can try searching for the topic name, the connector name, or maybe your URL endpoint. 

Another thing you can do is to modify the SMT code and print messages from there to observe how the SMT is handling your records.

### Conclusion

And now we can successfully push our kafka data directly into GridDB Time Series Containers on the Cloud.