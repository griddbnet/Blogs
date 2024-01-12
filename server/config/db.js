const griddb = require("griddb-node-api");

const containerName = "meal-planner";

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
      ["title", griddb.Type.STRING],
      ["calories", griddb.Type.DOUBLE],
      ["fat", griddb.Type.DOUBLE],
      ["cabs", griddb.Type.DOUBLE],
      ["protein", griddb.Type.DOUBLE],
      ["days", griddb.Type.STRING],
      ["breakfast", griddb.Type.STRING],
      ["lunch", griddb.Type.STRING],
      ["dinner", griddb.Type.STRING],
      ["snack1", griddb.Type.STRING],
      ["snack2", griddb.Type.STRING],
      ["snack3", griddb.Type.STRING],
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

	  console.log(savedData)
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
