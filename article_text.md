# CRUD Operations on GridDB with LangChain

This article explains how to use natural language queries to perform CRUD (create, read, update, and delete) operations on [GridDB](https://griddb.net/). You will use the Python [LangChain](https://python.langchain.com/v0.2/docs/introduction/) module and the [OpenAI GPT-4o LLM](https://openai.com/index/hello-gpt-4o/) (Large Language Model to convert natural language queries to GridDB queries and perform various operations on GridDB. 

GridDB is a robust NoSQL database designed to handle large volumes of real-time data. It excels in scalability and provides advanced in-memory processing capabilities and efficient time series data management. These features make GridDB well-suited for Internet of Things (IoT) and big data applications.

**Note:** You can find the code for this blog on my [GridDB Blogs GitHub repository](https://bit.ly/3R9tY4Y).

## Prerequisites

You need to install the GridDB C Client and the GridDB Python client before you can connect your Python application to GridDB. Follow the instructions on the [GridDB Python Package Index (Pypi) page](https://pypi.org/project/griddb-python/) to install these clients.

We will use the  OpenAI GPT-4o large language model (LLM) to convert natural language queries to GridDB queries. To use the GPT-4o model, you must [create an account with OpenAI](https://platform.openai.com/login?launch) and get your API key. Finally, you must install the following libraries to access OpenAI API from the LangChain framework. 

```
!pip install langchain
!pip install langchain-core
!pip install langchain-openai
```

**Note:** If you do not want to use GPT-4o, you can also use any other LangChain-supported LLM. The process remains the same, except that you will have to install the corresponding libraries. 

The script below imports the libraries you will need to run the code in this blog.

```
import griddb_python as griddb
import pandas as pd
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.pydantic_v1 import BaseModel, Field
from typing import List, Dict
```


## Creating a Connection with GridDB

The first step is to create a connection with GridDB. To do so, you must create a GridDB `StoreFactory` instance using the `get_instance()` method. 
Next, you need to pass your database host, cluster name, admin, and passwords to the `get_store()` method. This method returns a GridDB store object you can use to perform CRUD operations on GridDB. 
The following script creates a GridDB connection and tests it by retrieving a random container. 
```
factory = griddb.StoreFactory.get_instance()

DB_HOST = "127.0.0.1:10001"
DB_CLUSTER = "myCluster"
DB_USER = "admin"
DB_PASS = "admin"

try:
    gridstore = factory.get_store(
        notification_member = DB_HOST,
        cluster_name = DB_CLUSTER,
        username = DB_USER,
        password = DB_PASS
    )

    container1 = gridstore.get_container("container1")
    if container1 == None:
        print("Container does not exist")
    print("Successfully connected to GridDB")

except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))


```

**Output:**

```
Container does not exist
Successfully connected to GridDB
```

Once the connection is established, you can use natural language queries with the LangChain framework to perform CRUD operations on GridDB, as you will see in the upcoming sections. 

## Inserting Data with Natural Language Commands Using LangChain

As discussed, we will use the OpenAI GPT-4o LLM to parse natural language queries and convert them into GridDB queries. To do so, you need to create an object of the `ChatOpenAI` class and pass your OpenAI API key, temperature, and model name to it. 

Higher temperatures allow the model to be more creative. However, we want the model to be more accurate, hence we set the temperature to 0. 


```
llm = ChatOpenAI(api_key="YOUR_OPENAI_API_KEY",
                 temperature = 0,
                model_name = "gpt-4o")

```

We will adopt the following approach to convert natural language queries to GridDB queries.

1. From natural language queries, using LLMs, extract entities needed to execute GridDB queries.
2. Embed these entities into GridDB queries to perform CRUD operations on GridDB. 

For example, to execute INSERT queries on GridDB containers, we need data column names,  column types, column values, and container names. To extract these values as entities from natural language queries, we will first create a Pydantic `BaseModel` child class and add these entities as attributes. 
Based on the description of the class attributes, LLM will populate these attributes with data extracted from a natural language query. 
The following script creates a pydantic `InsertData`  class for extracting insertion entities. 

```
class InsertData(BaseModel):
    column_names: List[str]= Field(description="All the column names from the structured data")
    column_types: List[str] = Field(description="All the column types from the structured data")
    column_values: List[List[str]] = Field(description="All the column values from the structured data")
    container_name: str = Field(description="Name of container extracted from the user query")
```

Next, we will create a LangChain chain that tells the LLM about the information it must extract from the natural language. We use the `with_structured_output()` method and pass the `InsertData` pydantic class we created earlier. The LLM will return a dictionary populated with the attributes described in the `InsertData` class. 

```


system_command = """ You are an expert who extracts structure from natural language queries that must be converted to database queries.
Parse the user input query and extract the container name and column names along with their types from the user query and the user records.
The types should be parsed as STRING, LONG, FLOAT, INTEGER
"""

user_prompt = ChatPromptTemplate.from_messages([
    ("system", system_command ),
    ("user", "{input}")
])

insert_chain = user_prompt | llm.with_structured_output(InsertData)

```

To test the LLM, let's create a natural language query that inserts data into a database. We pass this query to the `insert_chain` we created in the previous script and print the extracted entities. 

```

user_query = """

Insert the following student records into the student_data container.

Name = Michael
Age = 10
Gender = Male
Grade = A


Name = Sara
Age = 11
Gender = Female
Grade = C

Name = Nick
Age = 9
Gender = Male
Grade = B

"""
user_data = insert_chain.invoke({"input": user_query})

print(user_data.column_names)
print(user_data.column_types)
print(user_data.column_values)
print(user_data.container_name)


```

**Output:**

<img src="images\img1-structured-llm-output.png">


From the above output, you can see that the LLM has successfully extracted the entities.

The rest of the process is straightforward. We embed the extracted entities in the script that inserts data into a GridDB container. 

The script below defines two helper functions. The `convert_list_of_types()` function converts the extracted string column types to GridDB column types. 

Similarly, the column values extracted by an LLM will be in string format. However, some of these values will be integers. We will try to convert the string values to integer format to match the column type. The `try_convert_to_int()` function does this. 

```
str_to_griddb_type = {
    "LONG": griddb.Type.LONG,
    "INTEGER": griddb.Type.INTEGER,
    "STRING": griddb.Type.STRING,
    "FLOAT": griddb.Type.FLOAT,
    # Add other types as needed
}

# Function to convert a list of string types to GridDB types
def convert_list_of_types(type_list):
    try:
        return [str_to_griddb_type[type_str] for type_str in type_list]
    except KeyError as e:
        raise ValueError(f"Unsupported type string: {e.args[0]}")

def try_convert_to_int(value):
    try:
        return int(value)
    except ValueError:
        return value

```

We are now ready to convert a natural language query into a GridDB data insertion query. To do so, we will define the `insert_records()` function as shown in the script below. This function accepts a natural language query, extracts entities from the query using a GPT-4o LLM from LangChain, embeds the entities into the GridDB query, and inserts the data into GridDB. 

```

def insert_records(query):
    user_data = insert_chain.invoke({"input": query})

    container_name = user_data.container_name
    column_names = user_data.column_names
    
    column_values = user_data.column_values
    column_values  = [[try_convert_to_int(item) for item in sublist] for sublist in column_values]

    column_types = user_data.column_types
    griddb_type = convert_list_of_types(column_types)

    container_columns = []
    for column_name, dtype in zip(column_names, griddb_type):
        container_columns.append([column_name, dtype])

    container_info = griddb.ContainerInfo(container_name,
                                          container_columns,
                                          griddb.ContainerType.COLLECTION, True)

    try:
        cont = gridstore.put_container(container_info)
        for row in column_values:
            cont.put(row)
        print("All rows have been successfully stored in the GridDB container.")
    
    except griddb.GSException as e:
        for i in range(e.get_error_stack_size()):
            print("[", i, "]")
            print(e.get_error_code(i))
            print(e.get_location(i))
            print(e.get_message(i))
            
insert_records(user_query)

```

**Output:**

```
All rows have been successfully stored in the GridDB container.

```

If you see the above message, the data is successfully inserted. Let's now see how to select this data. 

## Selecting Data 

The process of selecting data using natural language queries is similar. We will create a pydantic class to extract the container name and the select query from the natural language query. We create a LangChain chain and tell the LLM that it should convert the user command to SQL query. 

The following script performs these steps. 


```
class SelectData(BaseModel):
    container_name: str = Field(description="the container name from the user query")
    query:str = Field(description="natural language converted to SELECT query")



system_command = """ 
Convert user commands into SQL queries for Griddb.
"""

user_prompt = ChatPromptTemplate.from_messages([
    ("system", system_command),
    ("user", "{input}")
])

select_chain = user_prompt | llm.with_structured_output(SelectData)


```

Next, we will define a function `select_records` that accepts a natural language query, converts it into a SELECT query using the LLM, and retrieves records from GridDB. 

```

def select_records(query):

    select_data = select_chain.invoke(query)
    container_name = select_data.container_name
    select_query = select_data.query
    
    result_container = gridstore.get_container(container_name)
    query = result_container.query(select_query)
    rs = query.fetch()
    result_data = rs.fetch_rows()
    return result_data


select_records("Give me student records from student_data container where Age is greater than or equal to 10")

```

**Output:**

<img src="images\img2-selecting-data-from-griddb.png">

From the above output, you can see that the LLM generated a SELECT query containing the WHERE clause and only retrieved student records where the `Age` column values are greater than or equal to 10. 
## Update Data
The approach remains the same for updating records with natural language. We create a pydantic class `UpdateData` which extracts the container name, the records to select, the column name, and the updated value. 

Next, we create an `update_chain` that tells the LLM to retrieve the necessary entities for updating records. 

```
class UpdateData(BaseModel):
    container_name: str = Field(description="the container name from the user query")
    select_query:str = Field(description="natural language converted to SELECT query")
    column_name: str = Field(description="name of the column to be updated")
    column_value: str = Field(description="Column value to be updated")


system_command = """ 
Convert user commands into SQL query as follows. If the user enters an Update query, return the following:
1. The name of the container
2. A SELECT query to query records in the update statement.
3. The name of the column to be updated. 
4. The new value for the column. 
"""

user_prompt = ChatPromptTemplate.from_messages([
    ("system", system_command),
    ("user", "{input}")
])

update_chain = user_prompt | llm.with_structured_output(UpdateData)


```
We can then define a function `update_records()` that converts natural language user query into a GridDB query for updating records. 
```

def update_records(query):

    update_data = update_chain.invoke(query)

    result_container = gridstore.get_container(update_data.container_name)
    result_container.set_auto_commit(False)
    query = result_container.query(update_data.select_query)
    rs = query.fetch(True)

    select_data = select_records(f"Select all records from {update_data.container_name}")
    
    if rs.has_next():
        data = rs.next()
        column_index = select_data.columns.get_loc(update_data.column_name)
    
        data[column_index] = int(update_data.column_value)
        rs.update(data)
    
    result_container.commit()
    print("record updated successfully.")


update_records("Update the age of the students in the student_data container to 11 where Age is greater than or equal to 10")


```
You can select the following records to see if the data has been updated. 

```

select_records(f"Select all records from student_data container")

```

**Output:**

<img src="images\img3-selecting-updated-data-from-griddb.png">

The above output shows updated records. 

## Delete Data

Finally, the process for deleting records also remains similar. We create a pydantic `DeleteData` class that extracts the container and the records to delete from a natural language user query. 

We create a `delete_chain` that tells the LLM to return a select statement that retrieves records the user wants to delete. 

For example, running the following script returns a select query for deleting student records where students' age is greater than 10. 

```
class DeleteData(BaseModel):
    select_query:str = Field(description="natural language converted to SELECT query")
    container_name: str = Field(description="the container name from the user query")


system_command = """ 
Given a user natural language query, return an SQL select statement which selects the records that user wants to delete
"""

user_prompt = ChatPromptTemplate.from_messages([
    ("system", system_command),
    ("user", "{input}")
])

delete_chain = user_prompt | llm.with_structured_output(DeleteData)

result_chain = delete_chain.invoke("Delete all records from student_data container whose Age is greater than 10")
print(result_chain)

```
**Output:**

```
select_query='SELECT * FROM student_data WHERE Age > 10' container_name='student_data'

```

Next, we define the `delete_records()` method that accepts a natural language query, extracts entities for deleting records using the `delete_chain`, and deletes records from the GridDB.

```

def delete_records(query):

    update_data = update_chain.invoke(query)

    result_container = gridstore.get_container(update_data.container_name)

    result_container.set_auto_commit(False)

    query = result_container.query(update_data.select_query)
    
    rs = query.fetch(True)
    
    while rs.has_next():
        data = rs.next()
        rs.remove()
    
    
    result_container.commit()
    print("Records deleted successfully")

delete_records("Delete all records from student_data container whose Age is greater than 10")

```

You can select all records to confirm whether the records have been deleted or not. 

```
select_records(f"Select all records from student_data container")
```

**Output:**

<img src="images\img4-records-after-deletion.png">

## Conclusion

In this article, you learned how to perform CRUD operations on GridDB using natural language queries with the LangChain framework and the OpenAI GPT-4o LLM. We explored how to connect Python to GridDB, insert data into GridDB containers, and select, update, and delete records using natural language commands.
GridDB is a highly scalable open-source database designed to efficiently manage large volumes of real-time data. It excels at handling IoT and time-series data, making it an excellent choice for applications requiring advanced in-memory processing capabilities.

You can find the complete code for this blog on the [GridDB Blogs GitHub repository](https://bit.ly/3R9tY4Y).
If you have any questions or queries related to GridDB, feel free to create a post on Stack Overflow. Remember to use the `griddb` tag so our engineers can respond promptly.