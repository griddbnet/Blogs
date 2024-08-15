# SPDX-FileCopyrightText: 2021 ladyada for Adafruit Industries
# SPDX-License-Identifier: MIT

"""
Example sketch to connect to PM2.5 sensor with either I2C or UART.
"""

# pylint: disable=unused-import
import time
import datetime
import board
import busio
from adafruit_pm25.i2c import PM25_I2C

import json

import pika

credentials = pika.PlainCredentials('izzy', 'guest')
parameters = pika.ConnectionParameters('192.168.50.206',
                                   5672,
                                   '/',
                                   credentials)

connection = pika.BlockingConnection(parameters)
channel = connection.channel()
channel.queue_declare(queue='airQuality')

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
    channel.basic_publish(exchange='',
                      routing_key='airQuality',
                      body=payload)
    print(" [x] Sent payload: " + payload)