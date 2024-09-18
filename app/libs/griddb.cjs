// eslint-disable-next-line no-undef
const griddb = require('griddb-node-api');
const containerName = 'AIVideoSummarizer';

const initStore = async () => {
	const factory = griddb.StoreFactory.getInstance();
	// eslint-disable-next-line no-useless-catch
	try {
		// Connect to GridDB Cluster
		const store = await factory.getStore({
			host: '127.0.0.1',
			// transaction port (see in griddb config gs_cluster.json)
			port: 10001,
			clusterName: 'myCluster',
			username: 'admin',
			password: 'admin',
		});
		return store;
	} catch (e) {
		throw e;
	}
};

// Initialize container but not yet create it
function initContainer() {
	const conInfo = new griddb.ContainerInfo({
		name: containerName,
		columnInfoList: [
			['id', griddb.Type.INTEGER],
			['filename', griddb.Type.STRING],
			['audiotranscription', griddb.Type.STRING],
			['summary', griddb.Type.STRING]
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

/*
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
							console.log('Container Info: \n💽 %s', info.name);
							if (info.type == griddb.ContainerType.COLLECTION) {
								console.log('📦 Type: Collection');
							} else {
								console.log('📦 Type: TimeSeries');
							}
							//console.log("rowKeyAssigned=%s", info.rowKey.toString());
							console.log(
								'🛢️  Column Count: %d',
								info.columnInfoList.length
							);
							info.columnInfoList.forEach((element) =>
								console.log(
									'🔖 Column (%s, %d)',
									element[0],
									element[1]
								)
							);
						}
					});
				});
				return true;
			})
			.catch((err) => {
				if (err.constructor.name == 'GSException') {
					for (var i = 0; i < err.getErrorStackSize(); i++) {
						console.log('[%d]', i);
						console.log(err.getErrorCode(i));
						console.log(err.getMessage(i));
					}
				} else {
					console.log(err);
				}
			});
	}
}
*/

async function containersInfo(store) {
	let containers = [];
	for (let index = 0; index < store.partitionController.partitionCount; index++) {
		try {
			const nameList = await store.partitionController.getContainerNames(index, 0, -1);
			for (const element of nameList) {
				const info = await store.getContainerInfo(element);
				if (info.name === containerName) {
					let containerInfo = {
						'Container Info': info.name,
						'Type': info.type == griddb.ContainerType.COLLECTION ? 'Collection' : 'TimeSeries',
						'Column Count': info.columnInfoList.length,
						'Columns': info.columnInfoList.map(column => ({ 'Name': column[0], 'Type': column[1] }))
					};
					containers.push(containerInfo);
				}
			}
		} catch (err) {
			if (err.constructor.name == 'GSException') {
				for (let i = 0; i < err.getErrorStackSize(); i++) {
					console.error(`Error ${i}: ${err.getErrorCode(i)}, ${err.getMessage(i)}`);
				}
			} else {
				console.error(err);
			}
		}
	}
	return containers;
}


/**
 * Insert data to GridDB
 */
async function insert(data, container) {
	try {
		await container.put(data);
		return { status: true };
	} catch (err) {
		if (err.constructor.name == 'GSException') {
			for (var i = 0; i < err.getErrorStackSize(); i++) {
				console.log('[%d]', i);
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
			const rowData = {
				id: `${row[0]}`,
				filename: `${row[1]}`,
				audioTranscription: `${row[2]}`,
				summary: `${row[3]}`,
			};
			results.push(rowData);
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
		const row = await cont.get(parseInt(id));
		const result = [];
		result.push(row);
		return result;
	} catch (err) {
		console.log(err);
	}
}

// Delete container
async function dropContainer(store, containerName, conInfo) {
	store
		.dropContainer(containerName)
		.then(() => {
			console.log('drop ok');
			return store.putContainer(conInfo);
		})
		.catch((err) => {
			if (err.constructor.name == 'GSException') {
				for (var i = 0; i < err.getErrorStackSize(); i++) {
					console.log('[%d]', i);
					console.log(err.getErrorCode(i));
					console.log(err.getMessage(i));
				}
			} else {
				console.log(err);
			}
		});
}

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
};