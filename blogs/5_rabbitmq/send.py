# SPDX-FileCopyrightText: 2021 ladyada for Adafruit Industries
# SPDX-License-Identifier: MIT

"""
Example sketch to connect to PM2.5 sensor with either I2C or UART.
"""

# pylint: disable=unused-import
import time
import datetime
import json
import logging

import board
import busio
from adafruit_pm25.i2c import PM25_I2C

logging.basicConfig(level=logging.INFO)

import pika 

confirmed = 0
errors = 0
published = 0

credentials = pika.PlainCredentials('izzy', 'guest')
parameters = pika.ConnectionParameters('192.168.50.206',
                                   5672,
                                   '/',
                                   credentials)

connection = pika.BlockingConnection(parameters)
channel = connection.channel()
channel.confirm_delivery()
channel.queue_declare(queue='airQuality', durable=True)

reset_pin = None
i2c = busio.I2C(board.SCL, board.SDA, frequency=100000)
# Connect to a PM2.5 sensor over I2C
pm25 = PM25_I2C(i2c, reset_pin)

print("Found PM2.5 sensor, reading data...")

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
                          properties=pika.BasicProperties(delivery_mode=pika.DeliveryMode.Transient),
                        mandatory=True)
        print(" [x] Sent payload: " + payload)
    except pika.exceptions.UnroutableError:
        # If the message is not confirmed, it means something went wrong
        print("Message could not be confirmed")