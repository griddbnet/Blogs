import express from 'express'
import path from 'path'
import bodyParser from 'body-parser'
import metadataRoutes from './routes/metadataRoutes.js'
import uploadRoutes from './routes/uploadRoutes.js'
import { __dirname } from './dirname.js'

const app = express()

if (!process.env.VITE_APP_URL) {
    throw new Error('VITE_APP_URL environment variable is not set')
}
const apiURL = new URL(process.env.VITE_APP_URL)
const HOSTNAME = apiURL.hostname || 'localhost'
const PORT = apiURL.port || 3000

app.use(bodyParser.json({ limit: '10mb' }))
app.use(express.static(path.join(__dirname, 'dist')))
app.use(express.static(path.join(__dirname, 'public')))
app.use(express.static(path.join(__dirname, 'audio')))
app.use(express.static(path.join(__dirname, 'uploads')))

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'dist', 'index.html'))
})

app.use('/api', uploadRoutes)
app.use('/api/metadata', metadataRoutes)

app.listen(PORT, HOSTNAME, () => {
    console.log(`Server is running on http://${HOSTNAME}:${PORT}`)
})