import * as GridDB from './libs/griddb.cjs'
import { generateRandomID } from './libs/rangen.js'

const { collectionDb, store, conInfo } = await GridDB.initGridDbTS()

export async function saveData({ image, genColors }) {
    const id = generateRandomID()
    const picture = Buffer(image)
    const colors = String(genColors)

    const packetInfo = [parseInt(id), picture, colors]
    const saveStatus = await GridDB.insert(packetInfo, collectionDb)
    return saveStatus
}

export async function deleteDatabyID(id) {
    return await GridDB.deleteQuery(id, conInfo, store)
}

export async function getDatabyID(id) {
    return await GridDB.queryByID(id, conInfo, store)
}

export async function getAllData() {
    return await GridDB.queryAll(conInfo, store)
}

export async function info() {
    return await GridDB.containersInfo(store)
}
