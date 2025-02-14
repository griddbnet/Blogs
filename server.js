import path from 'path'
import express from 'express'
import bodyParser from 'body-parser'
import { __dirname } from './dirname.js'
import { getColorAnalysis } from './libs/ai.js'
import {
    saveData,
    getAllData,
    deleteDatabyID,
    getDatabyID,
} from './griddbservices.js'

const app = express()

// eslint-disable-next-line no-undef
if (!process.env.VITE_APP_URL) {
    throw new Error('VITE_APP_URL environment variable is not set')
}

// eslint-disable-next-line no-undef
const apiURL = new URL(process.env.VITE_APP_URL)
const HOSTNAME = apiURL.hostname || 'localhost'
const PORT = apiURL.port || 3000

app.use(bodyParser.json({ limit: '10mb' }))
app.use(express.static(path.join(__dirname, 'dist')))
app.use(express.static(path.join(__dirname, 'public')))

app.post('/process-image', async (req, res) => {
    const { image } = req.body

    if (!image) {
        return res.status(400).json({ error: 'No image provided' })
    }

    // eslint-disable-next-line no-undef
    const result = await getColorAnalysis(image)
    const colorsArray = result.choices[0].message.content

    // save data to the database
    const saveStatus = await saveData({ image, genColors: `${colorsArray}` })
    console.log(saveStatus)
    res.json(result.choices[0])
})

app.get('/colors', async (req, res) => {
    try {
        const data = await getAllData()
        res.json({ message: 'Data retrieved successfully', data })
    } catch (error) {
        console.error('Error retrieving all data:', error)
        res.status(500).json({
            message: 'Failed to retrieve data',
            error: error.message,
        })
    }
})

app.get('/colors/:id', async (req, res) => {
    const dataId = req.params.id
    try {
        const data = await getDatabyID(dataId)
        res.json(data)
    } catch (error) {
        console.error(`Error retrieving data with id:`, error)
        res.status(500).json({
            message: 'Failed to retrieve data',
            error: error.message,
        })
    }
})

app.get('/delete/:id', async (req, res) => {
    const response = await deleteDatabyID(req.params.id)
    res.json({ delete: response })
})

app.listen(PORT, HOSTNAME, () => {
    console.log(`Server is running on http://${HOSTNAME}:${PORT}`)
})
