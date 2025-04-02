import express from 'express'
import {
	getDocumentaryMetadata,
	saveDocumentaryMetadata,
	getAllDocumentaryMetadata
} from '../services/gridDBService.js'

const router = express.Router()

// Get metadata for a documentary
router.get('/:docId', async (req, res) => {
	try {
		const metadata = await getDocumentaryMetadata(req.params.docId)
		if (metadata) {
			res.json(metadata)
		} else {
			res.status(404).json({ message: 'Documentary not found' })
		}
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: 'Error fetching metadata' })
	}
})

// Save or update metadata for a documentary via API
// for future use
router.post('/', async (req, res) => {
	console.log(re.body)
	try {
		await saveDocumentaryMetadata(req.body)
		res.json({ message: 'Metadata saved successfully' })
	} catch (error) {
		console.log(error)
		res.status(500).json({ message: 'Error saving metadata' })
	}
})

router.get('/', async(req, res) => {
	try {
		const allData = await getAllDocumentaryMetadata()
		res.json(allData)
	} catch(error) {
		console.log(error)
		res.status(500).json({ message: 'Error retrieving metadata' })
	}
})

export default router
