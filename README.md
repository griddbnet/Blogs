Though GridDB can be used with a variety of programming languages, Java is the one language which works out of the box with the database as it has the distinction of being its sole native connector. And because it is the native connector, we have decided we would write an article on how to write a CRUD API using Java's [Spring Boot](https://spring.io/). 

With the Spring Boot library, we can easily spin up a server which will accept [REST Interface](https://restfulapi.net/) commands to accomplish our tasks. So in this article, we will be writing some Java code which allows for a user to Create, Read, Update, and Delete from their GridDB database. 

There are many benefits to accessing our database through a RESTful buffer, but the biggest benefit comes in the form of security. As you probably can imagine, allowing users to directly interact with a production database is always a risk; we want to limit what sorts of information and access users can have with our data, especially if we are housing user's private information. 

Creating a RESTful API with Spring Boot helps to limit our exposure and allows for us to only allow users to read, write, update, and delete exactly what we want them to. The way it works is that we create functions with Java which have pre-set query statements which can take in one or two user-generated parameters to interact with the database. By only allowing users to change one or two parameters, we can directly block them from accessing GridDB containers which they can contain sensitive information.

The basic premise of using an [API endpoints](https://en.wikipedia.org/wiki/Web_API) to interact with the database is as follows: when a user makes an [HTTP Request](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods), depending on the method used (GET, POST, PUT, DELETE, etc), will call a different Java function which will accomplish the task.

So, in this article, we will go over the source code included with this project which creates API endpoints which allow for a user to interact with GridDB through a safe buffer. The source code will also show you to write Java code which can accomplish fundamental GridDB tasks in an efficient way. 

## Prequisites and Following Along

If you would like to follow along with this blog, you can clone the publically available GitHub repo here: 

You will also need some of the prereqs:

1. [GridDB Installed and Running](https://docs.griddb.net/gettingstarted/using-apt/)
2. [Maven](https://maven.apache.org/install.html)

And then you can run the project using the following command `$ mvn spring-boot:run`. If everything goes well, your terminal will have an instance of Spring Boot running in the foreground and can be located at your server's ip address on port 8081 -- for example: http://localhost:8081. 

## Device Class

Before we dive into the all-important GridDB Class, we will first start with the Device Class.

This class is responsible for housing the schema for the data used in this article, as well as some other helper methods

class Device

    @RowKey String id;
    Double lat;
    Double lon;

And indeed, when writing GridDB Java applications, we set the schema for our container by creating a class and placing the schema inside.

## The GridDB Class

Before getting into the actual server endpoints, we will need to discuss the source code for the GridDB Class. The GridDB Class is responsible for all of the basic functionality -- when an endpoint is called, it simply calls for a GridDB class method to be run. 

To start, the GridDB Class will connect to your instance of GridDB running, whether it be locally running or somewhere else.  

public GridDB() {
        try {
		Properties props = new Properties();
		props.setProperty("notificationMember", "127.0.0.1:10001");
		props.setProperty("clusterName", "myCluster");
		props.setProperty("user", "admin"); 
		props.setProperty("password", "admin");
		store = GridStoreFactory.getInstance().getGridStore(props);
    	devCol = store.putCollection("sbDEVICES", Device.class);
        } catch (Exception e) {
            System.out.println("Could not get Gridstore instance, exitting.");
            System.exit(-1);
        }
    }

As explained before, our class here is called `GridDB`, and in this case, our default contructor houses our host machine's GridDB server to which we would like to connect. So this means, when you instantiate a new instance of the GridDB Class, your object should already be able to communicate with your running GridDB database.

We also create a new container (if not yet exists) called `sbDEVICES` which will house our data for this article with a schema provided by the `Device.class`

With that out of the way, next up are the functions responsible for our basic CRUD commands.

## Device Controller

On top of the two previous classes, with Spring Boot, we need to also create a Device Controller class which will house the endpoints we intend to use with our project. In this file, we will set the specific endpoint, the method, and finally what the code should do.

So, for example, the default constructor of our controller will create an instance of our GridDB class:

@RestController
public class DeviceController

    GridDB gridDb;

    public DeviceController() {
        super();
        gridDb = new GridDB();
    }

From this point, we simply create functions and associate them with certain endpoints and HTTP Methods, which will then call our GridDB class's methods to accomplish what we need.

## Creating And Calling API Endpoints

Now that we know a bit more about how these endpoints will be called, let's actually try running our server and creating some rows, reading from our database, and then finally deleting some rows.

Because we do not have a working frontend with this project, we can play around with our API with [Postman](https://www.postman.com/) or simply through a [CLI](https://en.wikipedia.org/wiki/Command-line_interface) and curl commands.

### POST Method

To begin, we will need to create some rows of data into our database. This of course means we will need to use a POST request to send an object data to our server.

First, we will look at actually putting rows of data into our database using this `putDevice` java function

public void putDevice(Device dev) {

        try {   
            System.out.println("dev="+dev);
            devCol.setAutoCommit(false);
            devCol.put(dev);
            devCol.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

Here we are simply using the Java API to `.put` the user-generated data found in the HTTP request's body directly into our GridDB server. The function expects a device as the parameter, meaning the `POST` request should have a device object (id, lat, lon) in the body.

### POST Request

Let's take a look at the actual API Endpoint's code in the Device Controller

@PostMapping("/device")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity&lt;?&gt; postDevice(@RequestBody Device dev) {
        if (gridDb.getDevice(dev.getId()) == null) {
            gridDb.putDevice(dev);
            return new ResponseEntity&lt;Device&gt;(dev, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<String>("Device already exists\n", HttpStatus.BAD_REQUEST);
        }
    }

The very top of this code snippet tells our controller what HTTP Method we are associating with this function. So, when a user calls `http://localhost:8081/device` with a POST, the code which proceeds that line will be called. 

In this case, we grab the body of the request and use that information to run our griddb class method `.putDevice` and make a new row in our `sbDEVICES` collection container. Here is that code using a curl command: 

curl --location 'localhost:8081/device' \
--header 'Content-Type: application/json' \
--data '{
    "id": "1",
    "lat": 0,
    "lon": 0
}'

Running this command should result in a 201 response from the server and our row of data should be available now.

### GET Methods

Next up, let's take a look at the functions called by the HTTP Method of `GET`; GET is the most used and simple method (think: making a request to open a web page and the repsonse being the webpage). 

With a GET, will be able to verify that our row was actually saved into our database.

First up here's our GridDB Class's methods called `getDevices` and `getDevice`.

public List&lt;Device&gt; getDevices() {
        List&lt;Device&gt; retval= new ArrayList&lt;Device&gt;();
        try {
		    Query&lt;Device&gt; query = devCol.query("select *");
            RowSet&lt;Device&gt; rs = query.fetch(false);
            while(rs.hasNext()) {
                Device dev = rs.next();
                retval.add(dev);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return retval;

    }
    public Device getDevice(String id) {
        try {
		    Query&lt;Device&gt; query = devCol.query("select * where id='"+id+"'");
            RowSet&lt;Device&gt; rs = query.fetch(false);
            if (rs.hasNext()) {
                Device dev = rs.next();
                return dev;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;

    }

These two functions are obviously extremely similar, the top one will grab every single row inside our selected container, while the second function will look up a specifc row with the user-generated row key.

### GET Requests

To make sure that our row of data is available, we can use our GET methods to READ from our database.

First, the endpoint to get ALL rows of data

@GetMapping("/devices")
	public List&lt;Device&gt; getDevice() {
        return gridDb.getDevices();
	}

And then the one to get a specific row: 

@GetMapping("/device/{id}")
	public ResponseEntity&lt;?&gt; getDevice(@PathVariable String id) {
        Device dev = gridDb.getDevice(id);
        if (dev == null)
            return new ResponseEntity<String>("Device not found\n", HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity&lt;Device&gt;(dev, HttpStatus.OK);
	}

$ curl --location 'localhost:8081/devices'

This should result in a 200 http response along with the data you requested: 

    [{"id":"1","lat":0,"lon":0}]

And though we only have one row of data, let's try our second endpoint where we give our server a specific rowkey to lookup.

$ curl --location 'localhost:8081/device/1'

    {"id":"1","lat":0,"lon":0}

Also you may notice that the 2nd query is not enveloped in side square brackets "[]". This is because the second query responds with a singular row, whereas the above functions returns all available rows.

### PUT Method

Okay, now let's say we want to update the lat and lon of our row. We can't use the POST request because it will spit out the error `400 -- Device already exists`. If we want to update our row's data, we will need to use the PUT Request.

And the convenient thing here is that we actually use the exact same function as the POST method for PUTs, so we can skip to the next section. The main caveat is that we need to tell our Device Controller to change the HTTP method to use.

### PUT Request

Here is the actual Device Controller API endpoint which will handle our PUT requests.

@PutMapping("/device")
    public ResponseEntity&lt;?&gt; putDevice(@RequestBody Device dev) {
        Device dbdev = gridDb.getDevice(dev.getId());

        if (dbdev == null)
            return new ResponseEntity<String>("Device not found\n", HttpStatus.NOT_FOUND);

        if(dev.getLat() != null)
            dbdev.setLat(dev.getLat());
        if(dev.getLon() != null)
            dbdev.setLon(dev.getLon());
        
        gridDb.putDevice(dev);
        return new ResponseEntity&lt;Device&gt;(dbdev, HttpStatus.OK);
    }

And now let's call it and set some values

curl --location --request PUT 'localhost:8081/device' \
--header 'Content-Type: application/json' \
--data '{
    "id": "1",
    "lat": 33.8121,
    "lon": 117.9190
}'

If our server responds with a 200 code, we can assume our row was updated with the new values. We can also run a `GET` to make sure: 

$ curl --location 'localhost:8081/device/1'

    {"id":"1","lat":33.8121,"lon":117.919}

Our row was updated!

### DELETE Method

Similar to the previous function, we have the DELETE method which will delete a row based on the user-generated row key

    public void deleteDevice(String id) {

        try {   
            System.out.println("deleting dev="+id);
            devCol.setAutoCommit(false);
            devCol.remove(id);
            devCol.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

By supplying this function with the proper ID, GridDB will remove that row of data directly inside the `sbDEVICES` container.

### DELETE Request

Finally let's delete our row and wrap up.

The delete method is very similar to our GET of a specific row because the underlying information needed is the same: we just need the row key (ID).

@DeleteMapping("/device/{id}")
    public ResponseEntity&lt;?&gt; deleteDevice(@PathVariable String id) {
        if (gridDb.getDevice(id) != null) {
            gridDb.deleteDevice(id);
            return new ResponseEntity<String>("device deleted\n", HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<String>("Device does not yet exist. Nothing deleted.\n", HttpStatus.BAD_REQUEST);
        }
    }

Once we run this with the proper ID, we will remove the row from our container and be left with an empty conainer: 

curl --location --request DELETE 'localhost:8081/device/1' \
--data ''

And one more check with our GET:

$ curl --location 'localhost:8081/devices'

    []

## Conclusion

And with that, we have shown the benefits of creating a Spring Boot CRUD API endpoint to safely interact with your GridDB server.