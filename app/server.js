import path from 'path'
import { URL } from 'url'
import multer from 'multer'
import express from 'express'
import { fileURLToPath } from 'url'
import { processVideo, outputAudioFolder } from './libs/videoprocessor.js'
// eslint-disable-next-line no-unused-vars
import { createVideoSummarization, transcribeAudio } from './libs/aiservices.js'
import { saveData, getAllData } from './griddbservices.js'

const app = express()

// eslint-disable-next-line no-undef
const apiUrl = new URL(process.env.VITE_API_URL)
const hostname = apiUrl.hostname
const port = apiUrl.port || 3000

// Determine the __dirname equivalent in ES modules
const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

app.use(express.json())

const storage = multer.diskStorage({
	destination: (req, file, cb) => {
		cb(null, path.join(__dirname, 'uploads'))
	},
	filename: (req, file, cb) => {
		const ext = path.extname(file.originalname)
		const name = path.basename(file.originalname, ext)
		cb(null, `${name}-${Date.now()}${ext}`)
	},
})

const upload = multer({ storage })

app.use(express.static(path.join(__dirname, 'dist')))
app.use(express.static(path.join(__dirname, 'audio')))
app.use(express.static(path.join(__dirname, 'frames')))

app.get('/', (req, res) => {
	res.sendFile(path.join(__dirname, 'dist', 'index.html'))
})

app.post('/upload', upload.single('video'), async (req, res) => {
	if (!req.file) {
		return res.status(400).send('No file uploaded')
	}
	try {
		const videoPath = path.join(__dirname, 'uploads', req.file.filename)
		const { base64Frames, audioFilename } = await processVideo(videoPath)
		const audioToTextResponse = await transcribeAudio(path.join(outputAudioFolder, audioFilename))
		const videoSummary = await createVideoSummarization(base64Frames, audioToTextResponse)

		const summaryData = {
			filename: videoPath,
			audioTranscription: audioToTextResponse,
			summary: videoSummary
		}

		const saveResponse = await saveData(summaryData)
		console.log(saveResponse)

		res.json({
			message: `File uploaded and processed: ${req.file.filename}`,
			frames: base64Frames,
			audio: audioFilename,
			audioTranscription: audioToTextResponse,
			videoSummary: videoSummary
		})
	} catch (error) {
		console.error('Error processing video:', error)
		res.status(500).send('Error processing video')
	}
})

app.get('/summaries', async (req, res) => {
	const allDataSummaries = await getAllData()
	res.json(allDataSummaries)
})

app.listen(port, hostname, () => {
	// eslint-disable-next-line no-undef
	console.log(`Server is running on ${process.env.VITE_API_URL}`)
})