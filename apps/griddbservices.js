import * as GridDB from './libs/griddb.cjs';
import { generateRandomID } from './libs/rangen.js';

const { collectionDb, store, conInfo } = await GridDB.initGridDbTS();

export async function saveData({ titlePptx, subtitlePptx, dataVisTitlePptx, chartImagePptx, bulletPointsPptx, pptxFile }) {
	const id = generateRandomID();
	const title = String(titlePptx);
	const subtitle = String(subtitlePptx);
	const dataVisTitle = String(dataVisTitlePptx)
	const chartImage = String(chartImagePptx);
	const bulletPoints = String(bulletPointsPptx);
	const pptx = String(pptxFile);

	const packetInfo = [parseInt(id), title, subtitle, dataVisTitle, chartImage, bulletPoints, pptx];
	const saveStatus = await GridDB.insert(packetInfo, collectionDb);
	return saveStatus;
}

export async function getDatabyID(id) {
	return await GridDB.queryByID(id, conInfo, store);
}

export async function getAllData() {
	return await GridDB.queryAll(conInfo, store);
}

export async function info() {
	return await GridDB.containersInfo(store);
}
