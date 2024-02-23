#!/usr/bin/python3

from kafka import KafkaProducer
import json
import datetime
import time as t
import logging
import random
import threading

NUM_DEVICES=10


logging.basicConfig(format='%(asctime)s %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S',
                    filename='producer.log',
                    filemode='w')

logger = logging.getLogger()
logger.setLevel(logging.INFO)

p=KafkaProducer(bootstrap_servers=['broker:9092'])
print('Kafka Producer has been initiated...')

def register_device(meterid):
     data= {
        "payload": 
        {
            'meterid': meterid,
            'billdate': random.randint(1, 28) 
        },
        "schema": 
        {
            "fields": [ 
                { "field": "meterid", "optional": False, "type": "int32" },
                { "field": "billdate", "optional": False, "type": "int32" } 
            ], 
            "name": "device", "optional": False, "type": "struct" 
        }    
     }
     m=json.dumps(data, indent=4, sort_keys=True, default=str)
     print("Registering",m)
     p.send("meters", m.encode('utf-8'))
 
    
def produce(meterid, usagemodel=None):
    time = datetime.datetime.now()-datetime.timedelta(days=100)
    register_device(meterid)

    base_temp = random.uniform(-10,40)
    base_kwh = random.uniform(0,2)
    while True:
        now = time.strftime('%Y-%m-%d %H:%M:%S.%f')
        data= {
            "payload": 
            {
                'timestamp': now,
                'kwh': base_kwh+random.uniform(-.2, 2),
                'temp': base_temp+random.uniform(-5, 5) 
            },
            "schema": 
            {
                "fields": [ 
                    { "field": "timestamp", "optional": False, "type": "string" },
                    { "field": "kwh", "optional": False, "type": "double" }, 
                    { "field": "temp", "optional": False, "type": "double" } 
                ], 
                "name": "iot", "optional": False, "type": "struct" 
            }    
         }
        time = time + datetime.timedelta(minutes=60)
        if time > datetime.datetime.now():
            time.sleep(3600)

        m=json.dumps(data, indent=4, sort_keys=True, default=str)
        p.send("meter_"+str(meterid), m.encode('utf-8'))
        print("meter_"+str(meterid), data['payload'])

def main():
   
    i=0;
    threads={}
    while i < NUM_DEVICES: 
        threads[i] = threading.Thread(target=produce, args=(i,))
        threads[i].start()
        i=i+1
    

if __name__ == '__main__':
    main()
