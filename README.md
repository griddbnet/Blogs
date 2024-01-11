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
--header 'Authorization: Basic YWRtaW46YWRtaW4='

Because it's a `GET` request, it's rather simple and we only needed to add in the authorization header. You should be able to run this and get an HTTP Response of `200`. If you receive `401 (unauthorized)`, check the credentials of your GridDB User. If you recieve `403 (forbidden)`, ensure that your IP address is allowed to pass through the Cloud's firewall.

#### Python Request

Here is that same request written in Python 

```python
import requests

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/checkConnection"

payload = {}
headers = {
  'Authorization': 'Basic aXNyYWVsejppc3JhZWw='
}

response = requests.request("GET", url, headers=headers, data=payload)

print(response.text)
```

#### JavaScript Request

```js
var myHeaders = new Headers();
myHeaders.append("Authorization", "Basic aXNyYWVsejppc3JhZWw=");

var requestOptions = {
  method: 'GET',
  headers: myHeaders,
  redirect: 'follow'
};

fetch("https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/checkConnection", requestOptions)
  .then(response => response.text())
  .then(result => console.log(result))
  .catch(error => console.log('error', error));
```

### Creating your First Container

With our connection firmly established, we can create our first container -- either Collection or Time Series -- of which are similar to relational tables. You can read more about that here: [GridDB Data Model](https://docs.griddb.net/architecture/data-model/). 

The URL suffix looks like this: `/:cluster/dbs/:database/containers`. This request can sometimes require a multitude of data and can have a big range, therefore this request will require an HTTP method of `POST`.

The body of the request requires container name, container type, whether a rowkey exists (bool), and the schema. Let's first take a look at the structure outside of the context of an HTTP Request and then we will send it inside of a Request body. We will also need to include in our Request's headers that we are sending a data payload of type JSON like so: `'Content-Type: application/json'`

```bash
{
    "container_name": "time_series_container1",
    "container_type": "TIME_SERIES",
    "rowkey": true,
    "columns": [
        {
            "name": "timestamp",
            "type": "TIMESTAMP"
        },
        {
            "name": "active",
            "type": "BOOL"
        },
        {
            "name": "voltage",
            "type": "DOUBLE"
        }
    ]
}
```

Now we simply attach this to the body when we make our Request and we should create our new container. If successful, you should get a status code of `201 (Created)`.

#### cURL

```bash
curl -i --location 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic aXNyYWVsOmlzcmFlbA==' \
--data '{
    "container_name": "time_series_container1",
    "container_type": "TIME_SERIES",
    "rowkey": true,
    "columns": [
        {
            "name": "timestamp",
            "type": "TIMESTAMP"
        },
        {
            "name": "active",
            "type": "BOOL"
        },
        {
            "name": "voltage",
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
  "container_name": "time_series_container1",
  "container_type": "TIME_SERIES",
  "rowkey": True,
  "columns": [
    {
      "name": "timestamp",
      "type": "TIMESTAMP"
    },
    {
      "name": "active",
      "type": "BOOL"
    },
    {
      "name": "voltage",
      "type": "DOUBLE"
    }
  ]
})
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic aXNyYWVsOmlzcmFlbA=='
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)
```


#### JavaScript

```js
var myHeaders = new Headers();
myHeaders.append("Content-Type", "application/json");
myHeaders.append("Authorization", "Basic aXNyYWVsOmlzcmFlbA==");

var raw = JSON.stringify({
  "container_name": "time_series_container1",
  "container_type": "TIME_SERIES",
  "rowkey": true,
  "columns": [
    {
      "name": "timestamp",
      "type": "TIMESTAMP"
    },
    {
      "name": "active",
      "type": "BOOL"
    },
    {
      "name": "voltage",
      "type": "DOUBLE"
    }
  ]
});

var requestOptions = {
  method: 'POST',
  headers: myHeaders,
  body: raw,
  redirect: 'follow'
};

fetch("https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers", requestOptions)
  .then(response => response.text())
  .then(result => console.log(result))
  .catch(error => console.log('error', error));
```

### Adding Rows of Data

We can add rows of data directly inside of our container. The URL suffix: `/:cluster/dbs/public/containers/:container/rows`

To `PUT` a row of data into our container, we will need to use the HTTP Method `PUT`. Similar to before, we will need to specify that our content is JSON and we will include the row data in our Request body.

You can add multiple rows at once, you just need to make sure that your payload is formed to accomdate extra rows and that you don't have a trailing comma on the last row.

```bash
[
  ["2023-12-15T10:25:00.253Z", true, 66.76],
  ["2023-12-15T10:35:00.691Z", false, 89.31],
  ["2023-12-15T10:45:00.032Z", false, 55.43]
]
```

You of course also need to be sure that your row's schema matches your container's. If it doesn't, you will be met with an error message and a status code of `400 (Bad Request)`.

#### cURL

https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers

```bash
curl --location --request PUT 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/time_series_container1/rows' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic aXNyYWVsOmlzcmFlbA==' \
--data '[
  ["2023-12-15T10:25:00.253Z", true, 66.76],
  ["2023-12-15T10:35:00.691Z", false, 89.31],
  ["2023-12-15T10:45:00.032Z", false, 55.43]
]'
```

#### Python

```python
import requests
import json

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/time_series_container1/rows"

payload = json.dumps([
  [
    "2023-12-15T10:25:00.253Z",
    True,
    66.76
  ],
  [
    "2023-12-15T10:35:00.691Z",
    False,
    89.31
  ],
  [
    "2023-12-15T10:45:00.032Z",
    False,
    55.43
  ]
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic aXNyYWVsOmlzcmFlbA=='
}

response = requests.request("PUT", url, headers=headers, data=payload)

print(response.text)
```

#### JavaScript

```js
var myHeaders = new Headers();
myHeaders.append("Content-Type", "application/json");
myHeaders.append("Authorization", "Basic aXNyYWVsOmlzcmFlbA==");

var raw = JSON.stringify([
  [
    "2023-12-15T10:25:00.253Z",
    true,
    66.76
  ],
  [
    "2023-12-15T10:35:00.691Z",
    false,
    89.31
  ],
  [
    "2023-12-15T10:45:00.032Z",
    false,
    55.43
  ]
]);

var requestOptions = {
  method: 'PUT',
  headers: myHeaders,
  body: raw,
  redirect: 'follow'
};

fetch("https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/time_series_container1/rows", requestOptions)
  .then(response => response.text())
  .then(result => console.log(result))
  .catch(error => console.log('error', error));
```

### Reading Container

After writing to our containers, we will want to read from our containers. The URL suffix is exactly the same as before: `/:cluster/dbs/:database/containers/:container/rows` except now we will be using the `POST` method request. The data expected by the server in these requests are how we expect our row data returned to us -- for example, we can choose a row limit, an offset, any conditions, and a sort method. Here is what that body looks like: 

```bash
{
  "offset" : 0,
  "limit"  : 100,
  "condition" : "voltage >= 60",
  "sort" : "voltage desc"
}
```

The one caveat with making this Request is that because it is a `POST` request, you will need to send *something* in the body of the request. Any of the parameters above will do, but the limit is likely the easiest option to include and has the added benefit of reducing server strain.

If successful, you should get a server response with a status code of `200 (OK)` and a body with the data requested. 

```bash
{
    "columns": [
        {
            "name": "timestamp",
            "type": "TIMESTAMP",
            "timePrecision": "MILLISECOND"
        },
        {
            "name": "active",
            "type": "BOOL"
        },
        {
            "name": "voltage",
            "type": "DOUBLE"
        }
    ],
    "rows": [
        [
            "2023-12-15T10:35:00.691Z",
            false,
            89.31
        ],
        [
            "2023-12-15T10:25:00.253Z",
            true,
            66.76
        ]
    ],
    "offset": 0,
    "limit": 100,
    "total": 2
}
```


#### cURL

```bash
curl -i --location 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/time_series_container1/rows' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic aXNyYWVsOmlzcmFlbA==' \
--data '{
  "offset" : 0,
  "limit"  : 100,
  "condition" : "voltage >= 60",
  "sort" : "voltage desc"
}'
```

#### Python

```python
import requests
import json

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/time_series_container1/rows"

payload = json.dumps({
  "offset": 0,
  "limit": 100,
  "condition": "voltage >= 60",
  "sort": "voltage desc"
})
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic aXNyYWVsOmlzcmFlbA=='
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)
```


#### JavaScript

```js
var myHeaders = new Headers();
myHeaders.append("Content-Type", "application/json");
myHeaders.append("Authorization", "Basic aXNyYWVsOmlzcmFlbA==");

var raw = JSON.stringify({
  "offset": 0,
  "limit": 100,
  "condition": "voltage >= 60",
  "sort": "voltage desc"
});

var requestOptions = {
  method: 'POST',
  headers: myHeaders,
  body: raw,
  redirect: 'follow'
};

fetch("https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/time_series_container1/rows", requestOptions)
  .then(response => response.text())
  .then(result => console.log(result))
  .catch(error => console.log('error', error));
```