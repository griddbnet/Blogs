[RabbitMQ](https://www.rabbitmq.com/) is a popular message-queueing system, used in a variety of systems where message delivery are of utmost importance. For our case, we would like to use RabbitMQ to ensure delivery of on-the-field sensor data to be delivered to GridDB for later processing. 

Of course, we could always send data from the field to our main server via other means, namely HTTP, but those methods of data transfer can be finicky and unsafe; how often have you tried listening a song via Apple Music through a sparsely connected rural part of the state, only to be met with a connection error and then dead silence? Once that connection is broken, it won't come back until the entire handshake process occurs again, and all of the data sent in the intermediary is completely lost. The goal of RabbitMQ in the context of this project will be to ensure that even if there are connection issues, the data will persist until it receives acknowledgement from the server that the data has been received and saved into GridDB.

## The Project

The goal of this article is to create a proof-of-concept for a very basic IoT message-queue system; we will have one physical sensor out "in the field" reading data from its environment, pushing the readings onto an exchange which will then push the data onto the queue and then finally into our server. Once that server acknowledges that it has received the entirety of the data, it will remove that value from the queue and move on to the next one (if it exists).

To accomplish this, first let's talk hardware.

### The Hardware

We have set up a [Raspberry Pi 4](https://www.raspberrypi.com/products/raspberry-pi-4-model-b/) to connect with an air quality sensor [Adafruit PMSA003I Air Quality Breakout](https://www.adafruit.com/product/4632) via this [STEMMA Hat](https://www.adafruit.com/product/4688) and a STEMMA wire; if you are interested in learning more about this particular sensor, you can read about it in the [Docs](https://learn.adafruit.com/pmsa003i?view=all) page provided by adafruit. 

![hardware](/images/hardware.jpg)

The data will be recieved from the queue from an Ubuntu server -- the specs are not important.

Next, let's take a look at the software.

### The Software

Of course, we are going to be utilizing RabbitMQ for the pushing and receiving of messages of relevant data. RabbitMQ provides various connectors for many programming languages, so we essentially are free to mix and match as we see fit (which is another stealth benefit of utilizing RabbitMQ for your stack). In our case, because we were already provided with a python library to which we can easily read and translate the raw sensor data, we want to push the payload data with Python. We *could* receive our payload data on the server with another python script with the aid of the [GridDB Python Connector](https://docs.griddb.net/gettingstarted/python/), but we will instead opt to receive with Java as it is GridDB's native interface and doesn't require any additional downloads.

### The Plan

Overall, our plan is as follows: 

1. Install RabbitMQ onto Ubuntu server
2. Read sensor readings and translate into readable data payloads (python)
3. push data onto an Exchange/Queue of our creation
4. Receive queue with Java 
5. Save received payloads directly GridDB

### How to Run

The python script can be easily run: install the required libraries and then simply run the script: `python3 app.py`.

For Java, because we have some dependencies to outside libraries, we need to reference them (they're in the `lib` directory) and then run that way. For example: 

```bash
$ cd lib/
$ export CP=.:amqp-client-5.16.0.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar:gridstore-5.6.0.jar:jackson-databind-2.17.2.jar:jackson-core-2.17.2.jar:jackson-annotations-2.17.2.jar
$ java -cp $CP ../Recv.java
```

The order of running these two files is not important; the receive will stay on even if the queue is empty.

## Prereqs & Getting Started

Here are list of needs if you would like to follow this project 1:1

1. Raspberry Pi
2. STEMMA Hat & Wire (or other means of connecting to board)
3. Python, RabbitMQ, GridDB, Java, & various other libraries

You can install RabbitMQ from their [download](https://www.rabbitmq.com/docs/download) page; instructions are straightforward. The only caveats are you will need to create yourself a new user and set the permissions properly: 

```bash
$ sudo rabbitmqctl add_user username password
$ sudo rabbitmqctl set_permissions -p / username ".*" ".*" ".*"
```

The credentials here will be the same ones used when forging the connection between the data sender and the data receiver. 

One note: I was unsuccessful in trying to use "special characters" in my password when making my connection, so I'd advise to keep the password simple for now (ie. just A-z and integers).

## Implementation: The Producer

Finally, let's get into specifics. We will first focus on our producer (the raspberry pi) and then move on to the consumer (our server). We will also be setting some configs to ensure our messages are delivered and saved into the database.

### Python Script for Reading Data 

We are using a modified version of the python script provided by adafruit to read the sensor data. Essentially, our task is very simple: we read the data, convert to JSON, and push to the Exchange/Queue. First, let's look at the hardware part of the code; after that we will get into the code for creating and pushing onto a queue to the correct machine.

```python
import board
import busio
from adafruit_pm25.i2c import PM25_I2C

reset_pin = None
i2c = busio.I2C(board.SCL, board.SDA, frequency=100000)
# Connect to a PM2.5 sensor over I2C
pm25 = PM25_I2C(i2c, reset_pin)
aqdata = pm25.read()
```

This snippet of code is all you need to read/translate the sensor readings. With this, assuming everything is connected properly, we will save the current values into the variable we called `aqdata`. 

### Python Code to Create and Push Data to RabbitMQ Queue

Next, let's look at the RabbitMQ code. First, we want to establish our connection to our ubuntu server. We will point the address to the IP of the machine and set the port to the default. We will also use the credentials we made earlier on our Ubuntu server

```python
import pika

credentials = pika.PlainCredentials('israel', 'israel')
parameters = pika.ConnectionParameters('192.168.50.206',
                                   5672,
                                   '/',
                                   credentials)

connection = pika.BlockingConnection(parameters)
channel = connection.channel()
```

Next we want to create and set some parameters for our queue, including how we handle pushing data messages to it.

```python
channel.confirm_delivery()
channel.queue_declare(queue='airQuality', durable=True)
```

By default, RabbitMQ prioritizes throughput above all else, meaning we need to change some default values to ensure our data is being sent to our server (broker).

First, we want to enable `confirm delivery`. This will produce an exception/error if the producer receives a negative acknowledgement (nack) from our broker. This means if our data is falling off, we will at least have a log of it. Unfortunately for us, there isn't a very robust handling of failed messages on the Python side; if this were for a production project, we would need to migrate from Python to some other language where you can deal with messages in a variety of ways. Namely, I think, we'd like to add batch processing of messages so that there's less of a chance of dropped data readings, and an easier time of re-sending dropped efforts.

Anyway, working with what we have, the next thing we do is turn on `durable` which will save the queue in the event of a broker crash/reboot. This means the `aqdata` won't need to be re-created but the messages inside of the queue won't necessarily be saved.

After that, we read and send data simultaneously: 

```python
while True:
    time.sleep(1)

    try:
        aqdata = pm25.read()
        current_time = datetime.datetime.utcnow().replace(microsecond=0)
        now = current_time.strftime('%Y-%m-%dT%H:%M:%S.%fZ')
        aqdata['ts'] = now
        aqdata['pm1'] = aqdata.pop('pm10 standard')
        aqdata['pm25'] = aqdata.pop('pm25 standard')
        aqdata['pm10'] = aqdata.pop('pm100 standard')
        aqdata['pm1e'] = aqdata.pop('pm10 env')
        aqdata['pm25e'] = aqdata.pop('pm25 env')
        aqdata['pm10e'] = aqdata.pop('pm100 env')
        aqdata['particles03'] = aqdata.pop('particles 03um')
        aqdata['particles05'] = aqdata.pop('particles 05um')
        aqdata['particles10'] = aqdata.pop('particles 10um')
        aqdata['particles25'] = aqdata.pop('particles 25um')
        aqdata['particles50'] = aqdata.pop('particles 50um')
        aqdata['particles100'] = aqdata.pop('particles 100um')
        #print(aqdata)
    except RuntimeError:
        print("Unable to read from sensor, retrying...")
        continue
    
    payload = json.dumps(aqdata)
    try: 
        channel.basic_publish(exchange='',
                        routing_key='airQuality',
                        body=payload,
                        properties=pika.BasicProperties(delivery_mode=pika.DeliveryMode.Persistent),
                        mandatory=True)
        print(" [x] Sent payload: " + payload)
    except pika.exceptions.UnroutableError:
        # If the message is not confirmed, it means something went wrong
        print("Message could not be confirmed")
```

For this snippet of code, we are reading the sensor data, changing the column names into the ones we want to use on the consumer side, and then pushing the payload into the channel to our queue we made earlier. Some things to note here: we set the mandatory flag to true and set the delivery mode to persistent. These two settings will try to save our messages into disk if they don't receive positive acknowledgement from our broker that the messages were safely delivered. 

The exception occurs if the broker ends back to our producer a `nack` (negative acknowledgement).

And so now every 1 second, our script will read sensor values and push it into the queue. Once the data is confirmed by the broker, the producer no longer cares about that data message.

## Implementation: The Consumer

Our consumer will be written in Java and its job is to read from the Queue in our broker (in our case, the same host machine as our consumer), unmarshal the data into a Java Object, and then save the results into GridDB.

### Consuming the Queue in Java

The consumer portion of the code is rather simple: forge the connection and read from the queue.

```java
private final static String QUEUE_NAME = "airQuality";
private final static boolean AUTO_ACK = false;
ConnectionFactory factory = new ConnectionFactory();
factory.setHost("localhost");
Connection connection = factory.newConnection();
Channel channel = connection.createChannel();
channel.queueDeclare(QUEUE_NAME, true, false, false, null);
System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
```

Here we are making our connection to our broker (hosted on the same machine as the consumer, hence `localhost`). We declare the queue we want to read from and set some options; we are using the default values for everything except for the first `true` which corresponds to `durable mode`, which we are setting to true, as explained above in the python section, it means that our queue will persist even if the broker goes down.

Next, let's run the actual consume: 

```java
channel.basicConsume(QUEUE_NAME, AUTO_ACK, deliverCallback, consumerTag -> { });
```

The only thing I'd like to point out here is that we've turned off `AUTO_ACK`. This means we will need to manually acknowledge either if the message being read from the queue was successful or not.

Next, here's the callback function that is run everytime it reads a new message off of the queue: 

```java
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            byte[] data = delivery.getBody();
   
            try {
                AirData ad = mapper.readValue(data, AirData.class);
                String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ad);
                System.out.println(jsonString);
                container.put(ad);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (Exception e) {
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                System.out.println("Setting nack");
            }
        };
```

Here is what's going on: we read the message (type of array of bytes), we use the jackson json library to unmarshal the value from raw bytes into a class we declare called `AirData`: 

```java
    static public class AirData {
        @JsonProperty("ts")
        @RowKey Date ts;
        @JsonProperty("pm1")
        double pm1;
        @JsonProperty("pm25")
        double pm25;
        @JsonProperty("pm10")
        double pm10;
        @JsonProperty("pm1e")
        double pm1e;
        @JsonProperty("pm25e")
        double pm25e;
        @JsonProperty("pm10e")
        double pm10e;
        @JsonProperty("particles03")
        double particles03;
        @JsonProperty("particles05")
        double particles05;
        @JsonProperty("particles10")
        double particles10;
        @JsonProperty("particles25")
        double particles25;
        @JsonProperty("particles50")
        double particles50;
        @JsonProperty("particles100")
        double particles100;
    }
```

Next we save that newly made Java object into GridDB and then finally acknowledge to our broker that we received the message. If something goes wrong, we will send a `nack` and the message will remain in the queue until it gets an `ack`.

### GridDB

Lastly, let's go over how GridDB fits into this. We will do our standard connecting to GridDB and then get our timeseries container. In this case, I created the table/container in the shell as it's easier than writing a one-time use java code.

```bash
$ sudo su gsadm
$ gs_sh
gs> createtimeseries aqdata NO ts timestamp pm1 double pm25 double pm10 double pm1e double pm25e double pm10e double particles03 double particles05 double particles10 double particles25 double particles50 double particles100 double
```

And now we make our connection in our Java code: 

```java
    public static GridStore GridDBNoSQL() throws GSException {

        GridStore store = null;

        try {
            Properties props = new Properties();
            props.setProperty("notificationMember", "127.0.0.1:10001");
            props.setProperty("clusterName", "myCluster");
            props.setProperty("user", "admin");
            props.setProperty("password", "admin");
            store = GridStoreFactory.getInstance().getGridStore(props);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return store;
    }
```

Using our `AirData` class from earlier we grab our newly made container: 

```java
TimeSeries<AirData> container = store.getTimeSeries("aqdata", AirData.class);
System.out.println("Connected to GridDB!");
```

And then we've already seen this above, but as we receive new payloads, we immediately save to GridDB and then send the positive acknolwedgement: 

```java
container.put(ad);
channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
```

## Conclusion

In this article, we set up a robust system in which our IoT data will safely transferred from our python producer to an exchange with no name (''), transferred to our broker which houses our queue called `airQuality`, and then finally will be read by our java consumer.
