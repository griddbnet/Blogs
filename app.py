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
from digitalio import DigitalInOut, Direction, Pull
from adafruit_pm25.i2c import PM25_I2C


import http.client
import json

conn = http.client.HTTPSConnection("cloud5197.griddb.com")
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic <redacted>'
}

reset_pin = None
i2c = busio.I2C(board.SCL, board.SDA, frequency=100000)
# Connect to a PM2.5 sensor over I2C
pm25 = PM25_I2C(i2c, reset_pin)

print("Found PM2.5 sensor, reading data...")

while True:
    time.sleep(1)

    try:
        aqdata = pm25.read()
        # print(aqdata)
        current_time = datetime.datetime.utcnow().replace(microsecond=0)
        now = current_time.strftime('%Y-%m-%dT%H:%M:%S.%fZ')
        print(now)
        temp = []
        temp.append(now)
    except RuntimeError:
        print("Unable to read from sensor, retrying...")
        continue
    
    for data in aqdata.values():
        temp.append(data)
    payload = json.dumps([temp])
    print(payload)
    conn.request("PUT", "/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/aqdata/rows", payload, headers)
    res = conn.getresponse()
    data = res.read()
    print(data.decode("utf-8"))
