## Introducing GridDB Cloud v2.0

GridDB Cloud v2.0 has officially been released and has a new free tier, but for now, the new GridDB Cloud v2.0 Free Plan is only offered to people living in Japan.

### How To Sign Up

If you would like to sign up for a GridDB free trial, you can follow along with the instructions in this video: [](). 

## First Steps with GridDB Cloud

Your GridDB Cloud instance can be communicated with via HTTP Requests; every action needed to interact with GridDB will require formulating and issuing an HTTP Request with different URLs, parameters, methods, and payload bodies.

### Whitelisting Your IP Address

If you haven't already, please whitelist your public IP address in the network settings of your GridDB Cloud Management dashboard.

### GridDB Users with Database Access

Next, we should create a new GridDB User. From the side panel, click the icon which says GridDB User. From this page, click `CREATE DATABASE USER`. This user's name and password will be attached to all of our HTTP Requests as a Basic Authorization Header. You will need to encode the username/password combination into base 64, separated by a colon; for example: admin:admin becomes `YWRtaW46YWRtaW4=`.

Once you create the new user, you will also need to grant access to your database. Click on the user from the table of users in GridDB Users page and from this page, grant access to your database (either READ or ALL). Now we can move on to making actual HTTP requests.

### Checking your GridDB Connection

Let's start with a sanity check and make sure that we can reach out to the GridDB instance.

#### Check Connection URL Endpoint

The Web API uses a `base url` which we will use and expand upon to build out our requests. The base url looks like this:

## `https://cloud<number>.griddb.com/griddb/v2/`

To check that our connection exists, we can append the following to our base url `/:cluster/dbs/:database/checkConnection`. Because we are not sending any data back to the server, we will use the `GET` HTTP method. 

Lastly, we need to include `basic authentication` in our HTTP Request's headers. For this, we will need to include our username and password encoded into base64. With all that said, here is the final result 

`https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/checkConnection`

We can now use this URL with any number of interfaces to communicate with our database.

#### cURL Request

To check our connection with cURL, you can use the following command 

curl -i --location 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/checkConnection' \
--header 'Authorization: Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'

Because it's a `GET` request, it's rather simple and we only needed to add in the authorization header. You should be able to run this and get an HTTP Response of `200`. If you receive `401 (unauthorized)`, check the credentials of your GridDB User. If you recieve `403 (forbidden)`, ensure that your IP address is allowed to pass through the Cloud's firewall.

#### Python Request

Here is that same request written in Python 

```python
import requests

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/checkConnection"

payload = {}
headers = {
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("GET", url, headers=headers, data=payload)

print(response.status_code)
```

#### node.js Request

```js
const request = require('request');
const options = {
  'method': 'GET',
  'url': 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/checkConnection',
  'headers': {
    'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'
  }
};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log("Response Status Code: ", response.statusCode);
});
```

### Creating your First Container

With our connection firmly established, we can create our first container -- either Collection or Time Series -- of which are similar to relational tables. You can read more about that here: [GridDB Data Model](https://docs.griddb.net/architecture/data-model/). 

The URL suffix looks like this: `/:cluster/dbs/:database/containers`. This request can sometimes require a multitude of data and can have a big range, therefore this request will require an HTTP method of `POST`.

The body of the request requires container name, container type, whether a rowkey exists (bool), and the schema. Let's first take a look at the structure outside of the context of an HTTP Request and then we will send it inside of a Request body. We will also need to include in our Request's headers that we are sending a data payload of type JSON like so: `'Content-Type: application/json'`

```bash
{
    "container_name": "cloud_quickstart",
    "container_type": "TIME_SERIES",
    "rowkey": true,
    "columns": [
        {
            "name": "ts",
            "type": "TIMESTAMP"
        },
        {
            "name": "co",
            "type": "DOUBLE"
        },
        {
            "name": "humidity",
            "type": "DOUBLE"
        },
        {
            "name": "light",
            "type": "BOOL"
        },
        {
            "name": "lpg",
            "type": "DOUBLE"
        },
        {
            "name": "motion",
            "type": "BOOL"
        },
        {
            "name": "smoke",
            "type": "DOUBLE"
        },
        {
            "name": "temp",
            "type": "DOUBLE"
        }
    ]
}
```

Now we simply attach this to the body when we make our Request and we should create our new container. If successful, you should get a status code of `201 (Created)`.

#### cURL

```bash
curl -i -X POST --location 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs' \
--data '{
    "container_name": "cloud_quickstart",
    "container_type": "TIME_SERIES",
    "rowkey": true,
    "columns": [
        {
            "name": "ts",
            "type": "TIMESTAMP"
        },
        {
            "name": "co",
            "type": "DOUBLE"
        },
        {
            "name": "humidity",
            "type": "DOUBLE"
        },
        {
            "name": "light",
            "type": "BOOL"
        },
        {
            "name": "lpg",
            "type": "DOUBLE"
        },
        {
            "name": "motion",
            "type": "BOOL"
        },
        {
            "name": "smoke",
            "type": "DOUBLE"
        },
        {
            "name": "temp",
            "type": "DOUBLE"
        }
    ]
}'
```


#### Python

```python
import requests
import json

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers"

payload = json.dumps({
  "container_name": "cloud_quickstart",
  "container_type": "TIME_SERIES",
  "rowkey": True,
  "columns": [
    {
      "name": "ts",
      "type": "TIMESTAMP"
    },
    {
      "name": "co",
      "type": "DOUBLE"
    },
    {
      "name": "humidity",
      "type": "DOUBLE"
    },
    {
      "name": "light",
      "type": "BOOL"
    },
    {
      "name": "lpg",
      "type": "DOUBLE"
    },
    {
      "name": "motion",
      "type": "BOOL"
    },
    {
      "name": "smoke",
      "type": "DOUBLE"
    },
    {
      "name": "temp",
      "type": "DOUBLE"
    }
  ]
})
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.status_code)
```


#### node.js

```js
var request = require('request');
var options = {
  'method': 'POST',
  'url': 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'
  },
  body: JSON.stringify({
    "container_name": "cloud_quickstart",
    "container_type": "TIME_SERIES",
    "rowkey": true,
    "columns": [
      {
        "name": "ts",
        "type": "TIMESTAMP"
      },
      {
        "name": "co",
        "type": "DOUBLE"
      },
      {
        "name": "humidity",
        "type": "DOUBLE"
      },
      {
        "name": "light",
        "type": "BOOL"
      },
      {
        "name": "lpg",
        "type": "DOUBLE"
      },
      {
        "name": "motion",
        "type": "BOOL"
      },
      {
        "name": "smoke",
        "type": "DOUBLE"
      },
      {
        "name": "temp",
        "type": "DOUBLE"
      }
    ]
  })

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.statusCode);
});
```

### Adding Rows of Data

We can add rows of data directly inside of our container. The URL suffix: `/:cluster/dbs/public/containers/:container/rows`

To `PUT` a row of data into our container, we will need to use the HTTP Method `PUT`. Similar to before, we will need to specify that our content is JSON and we will include the row data in our Request body.

You can add multiple rows at once, you just need to make sure that your payload is formed to accomdate extra rows and that you don't have a trailing comma on the last row.

```bash
[
  ["2024-01-09T10:00:01.234Z", 0.003551, 50.0, false, 0.00754352, false, 0.0232432, 21.6],
  ["2024-01-09T11:00:01.234Z", 0.303551, 60.0, false, 0.00754352, true, 0.1232432, 25.3],
  ["2024-01-09T12:00:01.234Z", 0.603411, 70.0, true, 0.00754352, true, 0.4232432, 41.5]
]
```

You of course also need to be sure that your row's schema matches your container's. If it doesn't, you will be met with an error message and a status code of `400 (Bad Request)`.

#### cURL

https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers

```bash
curl --location --request PUT 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/cloud_quickstart/rows' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs' \
--data '[
  ["2024-01-09T10:00:01.234Z", 0.003551, 50.0, false, 0.00754352, false, 0.0232432, 21.6],
  ["2024-01-09T11:00:01.234Z", 0.303551, 60.0, false, 0.00754352, true, 0.1232432, 25.3],
  ["2024-01-09T12:00:01.234Z", 0.603411, 70.0, true, 0.00754352, true, 0.4232432, 41.5]
]'
```

#### Python

```python
import requests
import json

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/cloud_quickstart/rows"

payload = json.dumps([
  [
    "2024-01-09T10:00:01.234Z",
    0.003551,
    50,
    False,
    0.00754352,
    False,
    0.0232432,
    21.6
  ],
  [
    "2024-01-09T11:00:01.234Z",
    0.303551,
    60,
    False,
    0.00754352,
    True,
    0.1232432,
    25.3
  ],
  [
    "2024-01-09T12:00:01.234Z",
    0.603411,
    70,
    True,
    0.00754352,
    True,
    0.4232432,
    41.5
  ]
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("PUT", url, headers=headers, data=payload)

print(response.text)
```

#### node.js

```js
var request = require('request');
var options = {
  'method': 'PUT',
  'url': 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/cloud_quickstart/rows',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'
  },
  body: JSON.stringify([
    [
      "2024-01-09T10:00:01.234Z",
      0.003551,
      50,
      false,
      0.00754352,
      false,
      0.0232432,
      21.6
    ],
    [
      "2024-01-09T11:00:01.234Z",
      0.303551,
      60,
      false,
      0.00754352,
      true,
      0.1232432,
      25.3
    ],
    [
      "2024-01-09T12:00:01.234Z",
      0.603411,
      70,
      true,
      0.00754352,
      true,
      0.4232432,
      41.5
    ]
  ])

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.body);
});
```

### Reading Container

After writing to our containers, we will want to read from our containers. The URL suffix is exactly the same as before: `/:cluster/dbs/:database/containers/:container/rows` except now we will be using the `POST` method request. The data expected by the server in these requests are how we expect our row data returned to us -- for example, we can choose a row limit, an offset, any conditions, and a sort method. Here is what that body looks like: 

```bash
{
  "offset" : 0,
  "limit"  : 100,
  "condition" : "temp >= 30",
  "sort" : "temp desc"
}
```

The one caveat with making this Request is that because it is a `POST` request, you will need to send *something* in the body of the request. Any of the parameters above will do, but the limit is likely the easiest option to include and has the added benefit of reducing server strain.

If successful, you should get a server response with a status code of `200 (OK)` and a body with the data requested. 

```bash
{
    "columns": [
        {
            "name": "ts",
            "type": "TIMESTAMP",
            "timePrecision": "MILLISECOND"
        },
        {
            "name": "co",
            "type": "DOUBLE"
        },
        {
            "name": "humidity",
            "type": "DOUBLE"
        },
        {
            "name": "light",
            "type": "BOOL"
        },
        {
            "name": "lpg",
            "type": "DOUBLE"
        },
        {
            "name": "motion",
            "type": "BOOL"
        },
        {
            "name": "smoke",
            "type": "DOUBLE"
        },
        {
            "name": "temp",
            "type": "DOUBLE"
        }
    ],
    "rows": [
        [
            "2024-01-09T12:00:01.234Z",
            0.603411,
            70.0,
            true,
            0.00754352,
            true,
            0.4232432,
            41.5
        ]
    ],
    "offset": 0,
    "limit": 100,
    "total": 1
}
```


#### cURL

```bash
curl -i -X POST --location 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/cloud_quickstart/rows' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs' \
--data '{
  "offset" : 0,
  "limit"  : 100,
  "condition" : "temp >= 30",
  "sort" : "temp desc"
}'
```

#### Python

```python
import requests
import json

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/cloud_quickstart/rows"

payload = json.dumps({
  "offset": 0,
  "limit": 100,
  "condition": "temp >= 30",
  "sort": "temp desc"
})
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)
```


#### nodejs

```js
var request = require('request');
var options = {
  'method': 'POST',
  'url': 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/cloud_quickstart/rows',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'
  },
  body: JSON.stringify({
    "offset": 0,
    "limit": 100,
    "condition": "temp >= 30",
    "sort": "temp desc"
  })

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.body);
});

```