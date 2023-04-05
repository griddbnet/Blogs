The web-based GridDB Cloud service has been updated to version 1.6, with GridDB CE 5.2 being the basis of that update underneath the hood. And though we have written about the GridDB Cloud before: [An Introduction to GridDB Cloud](*https://griddb.net/en/blog/an-introduction-to-griddb-cloud/) and [Using GridDB Cloud Version 1.2](https://griddb.net/en/blog/using-griddb-cloud-version-1-2/). 

In this article, we will go over the new features brought about by this new version, namely a new method of interfacing directly with the cloud. Previously, the [Web API](https://github.com/griddb/webapi) was the only way to access the GridDB Cloud without using an online [cloud virtual network](https://learn.microsoft.com/en-us/azure/virtual-network/virtual-networks-overview). With the Web API, you could make GET or POST HTTP requests to PUT data into your GridDB database server in the sky. Using this method, you can even run SQL or TQL commands through the Web API.

With the advent of version 1.6, the GridDB Cloud can not be accessible through your local machine via Java, JDBC, ODBC, and C. In this article, we will go over the set up and go over a small example of connecting to the cloud using Java and then JDBC.

<div class="clipboard">
<pre><code class="language-sh">$ git clone https://github.com/griddbnet/Blogs.git --branch griddb_cloud_1.6</code></pre>
</div>

## Managing the GridDB Cloud's Settings

To begin, we will need to head to the network portion of the cloud's portal. In there, we will see a new setting made fresh to us: "GridDB Access". In this panel, we can add in our public IP Address to the Exceptional List to make a tiny sliver of space to get through the Cloud's firewall. 

![griddb_access](images/griddb_access.png)

Once we are done with that, we can head to the support section and download the `.jar` files we will need onto our local machine's environment. From the support page, download the files under the heading of "GridDB Cloud Library and Plugin download".

Though this `.zip` file includes many different files, the relevant files for this article are located inside the JDBC folder. From here, we need `gridstore-5.2.0.jar`, `gridstore-jdbc-5.2.0.jar`, and `gridstore-advanced-5.2.0.jar`. 

Once these are downloaded, we can add them into our `$CLASSPATH` environment so that when we run our java commands, we have access to these new APIs.

## Connecting to the Cloud via Java

Before we begin, let's add these `.jar` files into our CLASSPATH.

<div class="clipboard">
<pre><code class="language-sh">export CLASSPATH=${CLASSPATH}:$PWD/gridstore-5.2.0.jar
export CLASSPATH=${CLASSPATH}:$PWD/gridstore-jdbc-5.2.0.jar
export CLASSPATH=${CLASSPATH}:$PWD/gridstore-advanced-5.2.0.jar</code></pre>
</div>

And now we can write some Java code to make a connection with our GridDB Cloud Server.

We will not go over too much source code in this article as it is not the main point of the article. We will instead go over relevant code snippets.

### Notification Provider

To connect via Java, we will be using the GridDB notification provider method. This is because the GridDB Cloud provides for us a notification provider URL which we can simply plug into our GridDB Store variable.

In your GridB Cloud portal, you can find the URL with the title: `Notification Provider URL for external connection`. Copy that URL and use it in your Java code.

<div class="clipboard">
<pre><code class="language-java">            String provider =
            "https://dbaasshareextconsta.blob.core.windows.net/dbaas-share-extcon-blob/trial1602.json?sv=2015-04-05&sr=b&st=2023-03-14T00%3A00%3A00.00Z&se=2073-03-14T00%3A00%3A00.0000000Z&sp=r&sig=h2VJ0xAqsnfdgfgdfgRsqWVgfgfg5CAS66RifPIZ1PDCJ0x%2FiXb2FOhA%3D";

            Properties props = new Properties();
            props.setProperty("notificationProvider", provider);
            props.setProperty("clusterName", "clustername");
            props.setProperty("user", "admin");
            props.setProperty("password", "admin");
            props.setProperty("database", "israel_db");
            props.setProperty("sslMode", "PREFERRED");
            props.setProperty("connectionRoute", "PUBLIC");
            store = GridStoreFactory.getInstance().getGridStore(props);</code></pre>
</div>

If you normally connect via the FIXED_LIST method, you will see that we are not using notificationMember and instead notificationProvider, but besides that, no major changes up top on the properties.

The other big and *mandatory* changes are found in the bottom, the `sslMode` and `connectionRoute` property keys. These are required because we are now using a web address secured by SSL. This inclusion means we will need to tell our connection that SSL is PREFERRED and that the connectionRoute is PUBLIC. 

Assuming everything is good, we can run our java code and we will have whatever changes we made reflected in the GridDB Cloud. If you would like to run this code on your machine, of course you can checkout our GitHub page (linked above) and run the code on your machine with your credentials and URLs.

<div class="clipboard">
<pre><code class="language-sh">$ javac *.java
$ java SimulateData 10 3</code></pre>
</div>

And just to have an easy demo to showcase, the code we are running here is simulating IoT data for Java, meaning we are placing a bunch of different containers into our Cloud with random data. And sure enough, when we check out the Cloud, we can see the containers along with the data in there: 

![query](images/query.png)

## Connecting to GridDB Cloud using JDBC

Of course, using JDBC with GridDB will allow for full use of SQL which has its own benefits and use cases. And luckily for us, using JDBC to connect to the Cloud will be about the same as using Java. The main difference is we need to include the `jdbc jar` to our CLASSPATH (already done), and we need to build out our `jdbc url`.

<div class="clipboard">
<pre><code class="language-java">        try {
            String provider = "https://dbaasshareextconsta.blob.core.windows.net/dbaas-share-extcon-blob/trial1602.json?sv=2015-04-05&sr=b&st=2023-03-14T00%3A00%3A00.00Z&se=2073-03-14T00%3A00%3A00.0000000Z&sp=r&sig=h2VJ0xAqsnfdgfgdfgRsqWVgfgfg5CAS66RifPIZ1PDCJ0x%2FiXb2FOhA%3D";
            String encodeProviderUrl = URLEncoder.encode(provider, "UTF-8");
            String clusterName = "clustername";
            String encodeClusterName = URLEncoder.encode(clusterName, "UTF-8");
            String databaseName = "public";
            String encodeDatabaseName = URLEncoder.encode(databaseName, "UTF-8");
            String ssl = "&sslMode=VERIFY"; //Necessary
            String encodeSsl = URLEncoder.encode(ssl, "UTF-8");

            Properties props = new Properties();
            String jdbcUrl = "jdbc:gs:///" + encodeClusterName + "/" + encodeDatabaseName
      + "?notificationProvider="+encodeProviderUrl + encodeSsl;
            props.setProperty("user", "admin");
            props.setProperty("password", "admin");
            props.setProperty("loginTimeout", "60");
            props.setProperty("connectionRoute", "PUBLIC"); //Also a new necessary props for this

            System.out.println(jdbcUrl);
            conn = DriverManager.getConnection(jdbcUrl, props );
            System.out.println("Sucessfully connected!");

        } catch (Exception e) {
            System.out.println("error connecting to DB: "+e);
        }</code></pre>
</div>

The full url looks like this: 

    jdbc:gs:///gs_clustertrial1602/public?notificationProvider=https%3A%2F%2Fdbaasshareextconsta.blob.core.windows.net%2Fdbaas-share-extcon-blob%2Ftrial1602.json%3Fsv%3D2015-04-05%26sr%3Db%26st%3D2023-03-14T00%253A00%253A00.0000000Z%26se%3D2073-03-14T00%2500%253A00.0Z%26sp%3Dr%26sig%3Dh2VJ0xAqsqWV5CAS66RifPIZ1PDCJ0x%252F2FOhA%253D%26sslMode%3DVERIFY

Again, when making this JDBC URL, you will need to be sure to include the `sslMode` in the URL with the value of `PREFERRED`, and then you will also need to include `connectionRoute` with a value of `PREFERRED`.

Once all of that information is OK, it will run whatever SQL code you chose to include with your file ("griddb-jdbc.properties") onto the cloud. In this case, I made a series of [tpch tables](https://github.com/dragansah/tpch-dbgen) in the cloud:

![jdbc_tables](images/tpch_tables.png)

You can, of course, change this information to fit your needs; simply change the contents of the gridstore-jdbc.properties file to include whichever SQL contents you'd like.

## Conclusion

And with that, we have successfully connected our local machine directly to our instance of the GridDB Cloud. With this set up, you don't need to worry about managing any servers, you can simply write your java applications and utilize the full, awesome power of GridDB.

