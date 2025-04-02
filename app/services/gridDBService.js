import * as GridDB from '../libs/griddb.cjs';
import { generateRandomID } from '../libs/rangen.js';

const { collectionDb, store, conInfo } = await GridDB.initGridDbTS();

async function saveDocumentaryMetadata({ video, audio, narrative, title }) {
	const id = generateRandomID();
	const videoFilename = String(video);
	const audioFilename = String(audio);
	const videoNarrative = String(narrative);
	const videoTitle = String(title)

	const packetInfo = [parseInt(id), videoFilename, audioFilename, videoNarrative, videoTitle];
	const saveStatus = await GridDB.insert(packetInfo, collectionDb);
	return saveStatus;
}

async function getDocumentaryMetadata(id) {
	return await GridDB.queryByID(id, conInfo, store);
}

async function getAllDocumentaryMetadata() {
	return await GridDB.queryAll(conInfo, store);
}

async function info() {
	return await GridDB.containersInfo(store);
}

export { getDocumentaryMetadata, saveDocumentaryMetadata, getAllDocumentaryMetadata }
