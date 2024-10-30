import fs from 'fs'
import path from 'path'
import { URL } from 'url'
import express from 'express'
import { aiAssistant } from './libs/ai.js'
import { __dirname } from './dirname.js'
import { saveData, getAllData } from './griddbservices.js';

const app = express()
// eslint-disable-next-line no-undef
if (!process.env.VITE_APP_URL) {
	throw new Error('VITE_APP_URL environment variable is not set')
}
// eslint-disable-next-line no-undef
const apiURL = new URL(process.env.VITE_APP_URL)
const hostname = apiURL.hostname || 'localhost'
const port = apiURL.port || 4000

app.use(express.json())
app.use(express.static(path.join(__dirname, 'dist')))
app.use(express.static(path.join(__dirname, 'public')))

app.get('/', (req, res) => {
	// Serve the index.html file from the 'dist' folder
	res.sendFile(path.join(__dirname, 'dist', 'index.html'))
})

app.get('/create/:fileId', async (req, res) => {
	const fileId = req.params.fileId

	try {
		const result = await aiAssistant(fileId)

		if (result.status === "completed") {
			const {
				title: titlePptx,
				subtitle: subtitlePptx,
				dataVisTitle: dataVisTitlePptx,
				chartImage: chartImagePptx,
				bulletPoints: bulletPointsPptx,
				outputFilename: pptxFile
			} = result.data

			const saveDataStatus = await saveData({ titlePptx, subtitlePptx, dataVisTitlePptx, chartImagePptx, bulletPointsPptx, pptxFile })

			res.json({
				save: saveDataStatus,
				data: result.data,
				pptx: result.pptx
			})
		} else {
			res.status(500).json({
				error: 'Task not completed', status: result.status
			})
		}

	} catch (error) {
		console.error('Error in AI Assistant:', error)
		res.status(500).json({ error: 'Error in AI Assistant', details: error.message })
	}
})

app.get('/metadata', (req, res) => {
	res.sendFile(path.join(__dirname, 'data', 'metadata.json'))
})

// Route to list filenames in the 'data' directory
app.get('/data/files', (req, res) => {
	const dirPath = path.join(__dirname, 'data')
	fs.readdir(dirPath, (err, files) => {
		if (err) {
			return res.status(500).send({ error: 'Failed to read the directory' })
		}
		// Filter out non-JSON files
		const jsonFiles = files.filter(file => path.extname(file) === '.json')
		res.setHeader('Content-Type', 'application/json')
		res.send(JSON.stringify(jsonFiles))
	})
})

// Route to serve a specific JSON file
app.get('/data/files/:filename', (req, res) => {
	const filename = req.params.filename
	if (!filename.endsWith('.json')) {
		return res.status(400).json({ error: 'Only JSON files are allowed' })
	}

	const filePath = path.join(__dirname, 'data', filename)

	fs.readFile(filePath, 'utf8', (err, data) => {
		if (err) {
			return res.status(500).send({ error: 'Failed to read the file' })
		}
		res.setHeader('Content-Type', 'application/json')
		res.send(data)
	})
})

app.get('/slides', async (req, res) => {
	try {
		const data = await getAllData();
		res.json({ message: 'Data retrieved successfully', data });
	} catch (error) {
		console.error('Error retrieving all data:', error);
		res.status(500).json({ message: 'Failed to retrieve data', error: error.message });
	}
})

app.listen(port, hostname, () => {
	console.log(`Server is running at http://${hostname}:${port}`)
})