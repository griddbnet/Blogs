# Creating a Mind Map for Data Visualization using GridDB

## Introduction

Throughout history, groundbreaking innovations that outlived their creators have sprung from the seeds of simple ideas. From Charles Babbage's pioneering work in digital computing to the transformative inventions of Thomas Edison who created the light bulb, Steve Jobs who founded Apple, and Bill Gates who founded Microsoft, these visionaries have turned concepts into reality, shaping the world as we know it today.

What these innovators have in common is that their innovations came about from a simple idea. They were able to map out their app and turn fiction into reality.

Now, it's your turn to embark on your own journey of innovation.

Perhaps you aspire to become a frontend software engineer, a goal that holds the potential to change the trajectory of your life. But where do you start? How do you transform your vision into tangible progress?

In this article, we'll explore the power of visualization and practical application by building a fullstack web mind map application using cutting-edge technologies such as [ReactJS](https://react.dev/), [ReactFlow](https://reactflow.dev/), [ExpressJS](https://expressjs.com/), [GridDB](https://griddb.net/en/), and [NodeJS](https://nodejs.org/en).

For those who do not know, GridDB is a highly scalable, in-memory NoSQL time-series database optimized for IoT and Big Data. While GridDB is optimized for IoT and Big Data, it can also be used for other purposes such as gaming and web applications.

## The Application

GridDB can be used on any of the Windows, Linux, or Mac operating systems.

While I installed [WSL](https://learn.microsoft.com/en-us/windows/wsl/) (Windows Subsystem for Linux) on my Windows to have access to Linux (Ubuntu) on my machine, you could also follow along with this tutorial using Docker or MacOS. The [GridDB documentation](https://docs.griddb.net/latest/gettingstarted/wsl/) provides you with a detailed installation process to successfully install the database on your computer.

There are also top-notch YouTube videos on their [YouTube channel](https://www.youtube.com/@GridDB) for those who prefer videos to written documentation. The entire code for the web application is available on [Github](https://github.com/Babajide777/grid-db-mind-map).

Open your terminal and clone the repo using this command

```bash
git clone https://github.com/Babajide777/grid-db-mind-map.git
```

Then,

```bash
cd grid-db-mind-map
```

To change to the grid db mind map app directory.

## The App Classification

This application is divided into 3 parts –

1. The Backend
2. The Frontend
3. Frontend and Backend connected

## Prerequisites

- GridDB version 5.3.0
- Node v12.22.9

## The Frontend

The UI of this mind map app allows users to add new map items, edit map items, and a delete map item. The following libraries were used to build the UI for this project:

### [ReactJs](https://react.dev/):

ReactJS is a JavaScript library built and maintained by Meta for building user interfaces.

### [Material UI](https://mui.com/):

Material UI is a comprehensive library of components from Google's Material Design system.

### [RTK Query](https://redux-toolkit.js.org/rtk-query/overview):

Redux Toolkit is a state management library.

### [React Flow](https://reactflow.dev/):

React Flow a customizable React component for building node-based editors and interactive diagrams

To view the frontend of the app change to the client directory.

```bash
cd client
```

Now install the required dependencies

```bash
npm i
```

Then run the app using

```bash
npm start
```

![Image](/blog/images/1.png)
![Image](/blog/images/1.png)

On the top right corner, we have the form that allows you to enter the details of a new idea. There are 4 input fields. Source is a dropdown of the node your idea is to be linked to. For example, the source for “Frontend developer” is “software developer”.

positionX is the position of the node on the x axis, positionY is the position of the ode on the Y axis, both of which are numbers, while label is the name of the node.

On the bottom left corner, there are 4 buttons, the + sign is to zoom in, the – sign is to zoom out, the box sign is to centralize all the nodes, while the padlock sign is to lock or unlock things in place. On the bottom right, we have a min map that shows a miniature version of the entire canvas.

Each node comes with a delete and edit button that allows you to both delete and edit details of a node.

## The Backend

The backend in this project ensures the correct map item data is gotten from the frontend, and then save to the GridDB database. We are able to perform functionalities with the GridDB database.

The proper CRUD functionalities are carried out in the app.

These are the packages that are needed to build the backend.

- [ExpressJs](https://expressjs.com/): A minimalist NodeJS framework that is used for building RESTful APIs.

- [Morgan](https://www.npmjs.com/package/morgan): A NodeJS middleware that is used to log HTTP requests.

- [GridDB Node API](https://github.com/griddb/node-api): The GridDB client for NodeJS

- [Joi](https://joi.dev/api/?v=17.9.1): A schema description language and data validator for JavaScript

### Step-by-Step Guide to Building the Meal Plan App

Follow the steps as explained below;

#### Step 1: Create a Server Folder

Create a “server” folder and initialize npm to generate a package.json file.
You can name the folder anything you want:

```bash
npm i
```

#### Step 2: Install Required Packages

We are going to install all the required packages at once by running the following line of code:

```bash
npm i express morgan joi griddb-node-api cors
```

**_Addition_**

While it is not required to install nodemon, it's nice to have in development so that the server would restart automatically when any change is saved.
This is the command to install nodemon as a dev dependency:

```bash
npm i -D nodemon
```

```bash
{
  "name": "grid-db-mind-map-server",
  "version": "1.0.0",
  "description": "backend for grid db mind-map",
  "main": "server.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1",
    "dev": "nodemon server.js"
  },
  "keywords": [
    "mind-map",
    "griddb",
    "griddb_node"
  ],
  "author": "Oyafemi Babajide",
  "license": "ISC",
  "dependencies": {
    "cors": "^2.8.5",
    "express": "^4.18.2",
    "griddb_node": "^0.8.4",
    "griddb-node-api": "^0.8.6",
    "joi": "^17.11.0",
    "morgan": "^1.10.0"
  },
  "devDependencies": {
    "nodemon": "^3.0.1"
  }
}

```

#### Step 3: Create Server.js File

Create an server.js file and insert the following code:

```bash
const express = require("express");
const morgan = require("morgan");
const app = express();
const cors = require("cors");

const PORT = 4000 || process.env.PORT;

app.use(morgan("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cors("*"));

app.get("/", (req, res) => {
  res.send("GridDB mind map Backend API");
});

app.use("/api", require("./routes/mindMapRoutes"));

app.listen(PORT, () => {
  console.log(`Server started on ${PORT}`);
});

```

If you installed nodemon as a dev dependency, you’ll need to add this line of code to the “scripts” section in your package.json file:

```bash
"dev": "nodemon index.js"
```

#### Step 4: Run the Application

If you installed nodemon, you could use ‘npm start’ to start the application, however, this would require you to restart the application any time you make changes, going against what nodemon is intended for. The following method doesn’t require you to restart the application when you make any changes (the benefit of nodemon):

```bash
npm run dev
```

![Image](/blog/images/3.png)

#### Step 5: Setup the GridDB Database

We will connect to the GridDB database using the griddb-node-api package.
We then set the container name of the project. I chose _“mind-map”_ because it is related to the project. However, you can call it whatever you want.

```bash

const griddb = require("griddb-node-api");

const containerName = "mind-map";

const initStore = async () => {
  const factory = griddb.StoreFactory.getInstance();
  try {
    // Connect to GridDB Cluster
    const store = await factory.getStore({
      host: "127.0.0.1",
      port: 10001,
      clusterName: "myCluster",
      username: "admin",
      password: "admin",
    });
    return store;
  } catch (e) {
    throw e;
  }
};

function initContainer() {
  const conInfo = new griddb.ContainerInfo({
    name: containerName,
    columnInfoList: [
      ["id", griddb.Type.STRING],
      ["source", griddb.Type.STRING],
      ["target", griddb.Type.STRING],
      ["x", griddb.Type.DOUBLE],
      ["y", griddb.Type.DOUBLE],
      ["label", griddb.Type.STRING],
      ["lineId", griddb.Type.STRING],
    ],
    type: griddb.ContainerType.COLLECTION,
    rowKey: true,
  });

  return conInfo;
}

async function createContainer(store, conInfo) {
  try {
    const collectionDB = await store.putContainer(conInfo);
    return collectionDB;
  } catch (err) {
    console.error(err);
    throw err;
  }
}

async function initGridDbTS() {
  try {
    const store = await initStore();
    const conInfo = await initContainer();
    const collectionDb = await createContainer(store, conInfo);
    return { collectionDb, store, conInfo };
  } catch (err) {
    console.error(err);
    throw err;
  }
}

async function containersInfo(store) {
  for (
    var index = 0;
    index < store.partitionController.partitionCount;
    index++
  ) {
    store.partitionController
      .getContainerNames(index, 0, -1)
      .then((nameList) => {
        nameList.forEach((element) => {
          // Get container information
          store.getContainerInfo(element).then((info) => {
            if (info.name === containerName) {
              console.log("Container Info: \n💽 %s", info.name);
              if (info.type == griddb.ContainerType.COLLECTION) {
                console.log("📦 Type: Collection");
              } else {
                console.log("📦 Type: TimeSeries");
              }
              //console.log("rowKeyAssigned=%s", info.rowKey.toString());
              console.log("🛢️  Column Count: %d", info.columnInfoList.length);
              info.columnInfoList.forEach((element) =>
                console.log("🔖 Column (%s, %d)", element[0], element[1])
              );
            }
          });
        });
        return true;
      })
      .catch((err) => {
        if (err.constructor.name == "GSException") {
          for (var i = 0; i < err.getErrorStackSize(); i++) {
            console.log("[%d]", i);
            console.log(err.getErrorCode(i));
            console.log(err.getMessage(i));
          }
        } else {
          console.log(err);
        }
      });
  }
}

/**
 * Insert data to GridDB
 */
async function insert(data, container) {
  try {
    let savedData = await container.put(data);

    console.log(savedData);
    return { status: true };
  } catch (err) {
    if (err.constructor.name == "GSException") {
      for (var i = 0; i < err.getErrorStackSize(); i++) {
        console.log("[%d]", i);
        console.log(err.getErrorCode(i));
        console.log(err.getMessage(i));
      }

      return { status: false, error: err.toString() };
    } else {
      console.log(err);
      return { status: false, error: err };
    }
  }
}

async function multiInsert(data, db) {
  try {
    await db.multiPut(data);
    return { ok: true };
  } catch (err) {
    console.log(err);
    return { ok: false, error: err };
  }
}

async function queryAll(conInfo, store) {
  const sql = `SELECT *`;
  const cont = await store.putContainer(conInfo);
  const query = await cont.query(sql);
  try {
    const rowset = await query.fetch();
    const results = [];

    while (rowset.hasNext()) {
      const row = rowset.next();
      results.push(row);
    }
    return { results, length: results.length };
  } catch (err) {
    console.log(err);
    return err;
  }
}

async function queryByID(id, conInfo, store) {
  try {
    const cont = await store.putContainer(conInfo);
    const row = await cont.get(id);
    return row;
  } catch (err) {
    console.log(err, "here");
  }
}

// Delete container
async function dropContainer(store, containerName) {
  store
    .dropContainer(containerName)
    .then(() => {
      console.log("drop ok");
      return store.putContainer(conInfo);
    })
    .catch((err) => {
      if (err.constructor.name == "GSException") {
        for (var i = 0; i < err.getErrorStackSize(); i++) {
          console.log("[%d]", i);
          console.log(err.getErrorCode(i));
          console.log(err.getMessage(i));
        }
      } else {
        console.log(err);
      }
    });
}

//Delete entry
const deleteByID = async (store, id, conInfo) => {
  try {
    const cont = await store.putContainer(conInfo);
    let res = await cont.remove(id);

    return [true, res];
  } catch (error) {
    return [false, error];
  }
};

const editByID = async (store, conInfo, data) => {
  try {
    const cont = await store.putContainer(conInfo);
    const res = await cont.put(data);
    return [true, ""];
  } catch (err) {
    return [false, err];
  }
};

module.exports = {
  initStore,
  initContainer,
  initGridDbTS,
  createContainer,
  insert,
  multiInsert,
  queryAll,
  dropContainer,
  containersInfo,
  containerName,
  queryByID,
  deleteByID,
  editByID,
};

```

The initStore function connects the app to the GridDB Cluster using the host, port, clusterName, username, and password.

The initContainer function is used to set the columns for the container and the datatypes for the different columns.

The createContainer creates the container while initGridDbTS initializes the database connection.

#### Step 6: Create a Meal plan

To create a mind map item

```bash
router.post("/add-meal", addMealPlan);
```

We use the Joi package to validate the request body sent from the frontend and then insert into the container that as created in step 6.

```bash

const Joi = require("joi");

//map item validation rules
const mapItemValidation = async (field) => {
  const schema = Joi.object({
    id: Joi.string().required(),
    source: Joi.string().required(),
    target: Joi.string().required(),
    x: Joi.number().integer().required(),
    y: Joi.number().integer().required(),
    label: Joi.string().required(),
    lineId: Joi.string().required(),
  });
  try {
    return await schema.validateAsync(field, { abortEarly: false });
  } catch (err) {
    return err;
  }
};

module.exports = {
  mapItemValidation,
};


```

```bash

async function insert(data, container) {
  try {
    let savedData = await container.put(data);

    console.log(savedData);
    return { status: true };
  } catch (err) {
    if (err.constructor.name == "GSException") {
      for (var i = 0; i < err.getErrorStackSize(); i++) {
        console.log("[%d]", i);
        console.log(err.getErrorCode(i));
        console.log(err.getMessage(i));
      }

      return { status: false, error: err.toString() };
    } else {
      console.log(err);
      return { status: false, error: err };
    }
  }
}

```

The id and lineId are randomly generated on the frontend and sent along with the source, target, x, y, and label. After the map item is saved to the database, we then query the map item using the created random id to get the details of the saved map item.

```bash
async function queryByID(id, conInfo, store) {
  try {
    const cont = await store.putContainer(conInfo);
    const row = await cont.get(id);
    return row;
  } catch (err) {
    console.log(err, "here");
  }
}


```

```bash
const addMealItem = async (req, res) => {
  //validate req.body
  const { collectionDb, store, conInfo } = await initGridDbTS();

  const { details } = await mapItemValidation(req.body);
  if (details) {
    let allErrors = details.map((detail) => detail.message.replace(/"/g, ""));
    return responseHandler(res, allErrors, 400, false, "");
  }

  try {
    const { id, source, target, x, y, label, lineId } = req.body;

    const data = [id, source, target, x, y, label, lineId];

    const saveStatus = await insert(data, collectionDb);

    if (saveStatus.status) {
      const result = await queryByID(id, conInfo, store);
      let returnData = {
        id: result[0],
        source: result[1],
        target: result[2],
        x: result[3],
        y: result[4],
        label: result[5],
        lineId: result[6],
      };

      return responseHandler(
        res,
        "Map Item saved successfully",
        201,
        true,
        returnData
      );
    }

    return responseHandler(
      res,
      "Unable to save map item",
      400,
      false,
      saveStatus.error
    );
  } catch (error) {
    responseHandler(res, "Error saving map item", 400, false, error.message);
  }
};


```

The map plan item details are then sent to the frontend as a json response.

![Image](/blog/images/4.png)

![Image](/blog/images/5.png)

#### Step 7: Get a Map Item Detail

The id of the map item that is required is gotten from the params of the request data.

```bash
router.get("/map-detail/:id", mapItemDetails);
```

Then id of the map item is then queried with the data in the database and a 200 response with the map item data is sent if the map item is found.

This route will mostly be used to get the details of a map item that is to be edited.

```bash
const mapItemDetails = async (req, res) => {
  try {
    const { store, conInfo } = await initGridDbTS();
    const { id } = req.params;

    const result = await queryByID(id, conInfo, store);

    let returnData = {
      id: result[0],
      source: result[1],
      target: result[2],
      x: result[3],
      y: result[4],
      label: result[5],
      lineId: result[6],
    };

    return result
      ? responseHandler(res, "map item detail found", 200, true, returnData)
      : responseHandler(res, "No map item detail found", 400, false, "");
  } catch (error) {
    responseHandler(res, "Error saving map item", 400, false, error.message);
  }
};

```

![Image](/blog/images/6.png)

![Image](/blog/images/7.png)

#### Step 8: Edit a Map Item

```bash
router.put("/edit-map-item/:id", editMapItem);
```

To edit a map item, again the id of the required map item is sent in the params of the request. The map item is queried using the given id and the old details of the map item is replaced by the new ones.

```bash
const editMapItem = async (req, res) => {
  try {
    const { store, conInfo } = await initGridDbTS();
    const { id } = req.params;

    const result = await queryByID(id, conInfo, store);

    if (!result) {
      return responseHandler(res, "incorrect map item ID", 400, false, "");
    }

    const { source, target, x, y, label, lineId } = req.body;

    const data = [id, source, target, x, y, label, lineId];

    const check = await editByID(store, conInfo, data);

    if (check[0]) {
      const result2 = await queryByID(id, conInfo, store);

      let returnData = {
        id: result2[0],
        source: result2[1],
        target: result2[2],
        x: result2[3],
        y: result2[4],
        label: result2[5],
        lineId: result2[6],
      };

      return responseHandler(
        res,
        "map item edited successfully",
        200,
        true,
        returnData
      );
    }
    return responseHandler(res, "Error editing map item ", 400, false, "");
  } catch (error) {
    responseHandler(res, "Error saving map item", 400, false, error.message);
  }
};


```

```bash
const editByID = async (store, conInfo, data) => {
  try {
    const cont = await store.putContainer(conInfo);
    const res = await cont.put(data);
    return [true, ""];
  } catch (err) {
    return [false, err];
  }
};


```

![Image](/blog/images/8.png)

### Step 9: Delete a Map Item

```bash
router.delete("/delete-map-item/:id", deleteMapItem);
```

The id is gotten from the params or the request data and that is used to delete the row containing the specified map item.

```bash
const deleteMapItem = async (req, res) => {
  try {
    const { store, conInfo } = await initGridDbTS();
    const { id } = req.params;

    const result = await deleteByID(store, id, conInfo);

    return result[0]
      ? responseHandler(res, "map item deleted successfully", 200, true, "")
      : responseHandler(res, "Error deleting map item", 400, false, "");
  } catch (error) {
    responseHandler(res, "Error saving map item", 400, false, error.message);
  }
};


```

```bash

const deleteByID = async (store, id, conInfo) => {
  try {
    const cont = await store.putContainer(conInfo);
    let res = await cont.remove(id);

    return [true, res];
  } catch (error) {
    return [false, error];
  }
};


```

![Image](/blog/images/9.png)

![Image](/blog/images/10.png)

### Step 10: Get List of All Map Items in the Database

To get list of all map items in the database, you have to do this;

```bash
router.get("/all-map-items", getAllMapItems);
```

This returns all the map items in the database.

```bash
const getAllMapItems = async (req, res) => {
  try {
    const { store, conInfo } = await initGridDbTS();
    const result = await queryAll(conInfo, store);
    let data = [];

    result.results.forEach((result) => {
      let returnData = {
        id: result[0],
        source: result[1],
        target: result[2],
        x: result[3],
        y: result[4],
        label: result[5],
        lineId: result[6],
      };
      data.push(returnData);

      return result;
    });

    return responseHandler(
      res,
      "all map items in the database successfully retrieved",
      200,
      true,
      data
    );
  } catch (error) {
    return responseHandler(
      res,
      "Unable to retrieve meal plans",
      400,
      false,
      ""
    );
  }
};



```

```bash
async function queryAll(conInfo, store) {
  const sql = `SELECT *`;
  const cont = await store.putContainer(conInfo);
  const query = await cont.query(sql);
  try {
    const rowset = await query.fetch();
    const results = [];

    while (rowset.hasNext()) {
      const row = rowset.next();
      results.push(row);
    }
    return { results, length: results.length };
  } catch (err) {
    console.log(err);
    return err;
  }
}


```

![Image](/blog/images/11.png)

## Frontend and Backend connected

The essence of this app is to connect the backend with the frontend to ensure the app is fully functional. I built a short software developer roadmap to test the functionality of the app.

![Image](/blog/images/12.png)

## Conclusion

In this comprehensive guide, we've embarked on a journey of innovation, inspired by the visionary creators of history who turned simple ideas into transformative realities.

From the foundational work of Charles Babbage to the groundbreaking achievements of modern-day innovators like Thomas Edison, Steve Jobs, and Bill Gates, we've witnessed the power of mapping out ideas and translating them into tangible outcomes.

By delving into the realm of frontend software engineering, we've explored the potential to reshape our lives and the world around us. Leveraging the latest technologies such as ReactJS, ReactFlow, ExpressJS, GridDB, and NodeJS, we've embarked on the creation of a fullstack web mind map application, bridging the gap between imagination and implementation.

At the heart of our journey lies GridDB, a versatile and scalable NoSQL time-series database optimized for IoT and Big Data applications. Through its integration into our project, we've unlocked new possibilities for data visualization and management, empowering us to bring our ideas to life with precision and efficiency.

From the installation process to the development of frontend and backend components, we've navigated through each step with clarity and purpose. By following our step-by-step guide, you've gained the knowledge and tools necessary to embark on your own journey of innovation, armed with the skills to create impactful solutions in the digital landscape.

As we conclude our exploration, remember that innovation knows no bounds.

Whether you're a seasoned developer or a budding enthusiast, the path to discovery awaits. Embrace the spirit of creativity, challenge the status quo, and let your ideas soar.

With determination and perseverance, you have the power to shape the future.

Let's continue to innovate, inspire, and make a difference in the world.
