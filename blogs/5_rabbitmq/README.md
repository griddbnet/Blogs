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

1. Install and set up Raspberry pi
2. Read sensor readings and translate into readable data payloads
3. push data onto an Exchange/Queue of our creation
4. Install RabbitMQ onto Ubuntu server
5. Receive queue with Java 
6. Save received payloads directly GridDB

Once put this way, I think the task seems rather simple!

## Implementation

Finally, let's get into specifics. We will first focus on the pi and move to the server. 

### Prereqs & Getting Started

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

### Python Script for Reading Data and Creating RabbitMQ Queue

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

This snippet of code is all you need to read/translate the sensor readings. With this, assuming everything is connected properly, will save the current values into the variable we called `aqdata`. Next, let's look at the RabbitMQ code: 

```python
import pika

credentials = pika.PlainCredentials('israel', 'israel')
parameters = pika.ConnectionParameters('192.168.50.206',
                                   5672,
                                   '/',
                                   credentials)

connection = pika.BlockingConnection(parameters)
channel = connection.channel()
channel.queue_declare(queue='airQuality')
```