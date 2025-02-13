# -------------------------------------------------------------------------
# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License. See License.txt in the project root for
# license information.
# --------------------------------------------------------------------------

import os
import asyncio
import datetime
import time as t
import pickle
import json
import random
import uuid
from azure.iot.device.aio import IoTHubDeviceClient
from azure.iot.device import Message


from base64 import b64encode, b64decode
from hashlib import sha256
from time import time
from urllib import parse
from hmac import HMAC

def generate_sas_token(uri, key, policy_name, expiry=3600):
    ttl = time() + expiry
    sign_key = "%s\n%d" % ((parse.quote_plus(uri)), int(ttl))
    signature = b64encode(HMAC(b64decode(key), sign_key.encode('utf-8'), sha256).digest())

    rawtoken = {
        'sr' :  uri,
        'sig': signature,
        'se' : str(int(ttl))
    }

    if policy_name is not None:
        rawtoken['skn'] = policy_name

    return 'SharedAccessSignature ' + parse.urlencode(rawtoken)


messages_to_send = 2
number_of_devices = 10
conn_device_ids = []

async def main():
    for i in range(1, number_of_devices+1):
        dev = "device"+str(i)
        conn_device_ids.append(dev)

    key = '3oMsHYvHvMkErdKGxTXh1f15eRCm8SCK5AIoTFc721A='
    policy = 'iothubowner'
    for device in conn_device_ids:
        uri = 'griddb-udemy.azure-devices.net/devices/' + device
        sas_token = generate_sas_token(uri, key, policy)
        #print("conn string: " + sas_token)
        device_client = IoTHubDeviceClient.create_from_sastoken(sas_token)

        await device_client.connect()

        async def send_test_message(i):
            time = datetime.datetime.now()
            now = time.strftime('%Y-%m-%dT%H:%M:%S.%fZ')
            print("sending message #" + str(i) + " to device #: " + device)
            data = [{
                "deviceId": device,
                "ts": now,
                "temp": str(random.uniform(0, 120)), 
                "speed": str(random.uniform(0, 190)),
            }]
            json_str = json.dumps(data)
            msg = Message(json_str)
            msg.message_id = uuid.uuid4()
            msg.content_type = "application/json"
            await device_client.send_message(msg)
            print("done sending message #" + str(i))

        await asyncio.gather(*[send_test_message(i) for i in range(1, messages_to_send + 1)])

        await device_client.shutdown()


if __name__ == "__main__":
    asyncio.run(main())
