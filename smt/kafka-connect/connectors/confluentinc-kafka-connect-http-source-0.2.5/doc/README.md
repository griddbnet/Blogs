# Introduction

This project contains a Kafka Connect source connector to read JSON data from an HTTP API

# Documentation

Documentation on the connector is hosted on Confluent's
[docs site](https://docs.confluent.io/current/connect/kafka-connect-http-source/).

Source code is located in Confluent's
[docs repo](https://github.com/confluentinc/docs/tree/master/connect/kafka-connect-http-source). If changes
are made to configuration options for the connector, be sure to generate the RST docs (as described
below) and open a PR against the docs repo to publish those changes!

# Configs

Documentation on the configurations for each connector can be automatically generated via Maven.

To generate documentation for the source connector:
```bash
mvn -Pdocs exec:java@source-config-docs
```

# Compatibility Matrix:

This connector has been tested against the following versions of Apache Kafka
and HTTP:

|                          | AK 1.0             | AK 1.1        | AK 2.0        |
| ------------------------ | ------------------ | ------------- | ------------- |
| **HTTP v1.2.3** | NOT COMPATIBLE (1) | OK            | OK            |

1. The connector needs header support in Connect.
