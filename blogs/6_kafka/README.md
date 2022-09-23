We have written before about using Apache Kafka to load real-time data directly into your GridDB server. You can read our previous blogs with the following links: [Using Kafka With GridDB](https://griddb.net/en/blog/using-kafka-with-griddb/) & [Using GridDB as a source for Kafka with JDBC](https://griddb.net/en/blog/using-griddb-as-a-source-for-kafka-with-jdbc/).

Apache Kafka is a tool which allows for "real-time processing of record streams". What this means is that you can send real-time data from your sensors or various other pieces of tools directly into something else, and in this case into GridDB.

For this article, we will again be using Kafka in conjunction with GridDB with the newly released GridDB Kafka Connector. In our previous articles, we had been marrying GridDB and Kafka via JDBC, because with the help of the connector, JDBC is no longer a piece of the equation. There are two pieces which effectively connect GridDB with Kafka: the source and the sink. The GridDB Kafka sink connector pushes data from Apache Kafka topics and persists the data to GridDB database tables. And the source connector works in the opposite fashion, pulling data from GridDB and putting it into Kafka topics.

To showcase this, we will be running some sample code to get data from Kafka directly into GridDB and vice-versa. 

## Conclusion

And with that, we have successfully used Kafka with GridDB, which is immensely useful for getting real-time data from your devices directly into your GridDB database. 
