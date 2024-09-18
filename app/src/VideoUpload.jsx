import axios from 'axios'
import { useState } from 'react'
import { Box, Heading, Badge } from '@radix-ui/themes'
import Markdown from 'react-markdown'

const VideoUpload = () => {
	const [file, setFile] = useState(null)
	const [uploadStatus, setUploadStatus] = useState('')
	const [frameCount, setFrameCount] = useState(0)
	const [audioPath, setAudioPath] = useState('')
	// eslint-disable-next-line no-unused-vars
	const [audioTranscription, setAudioTranscription] = useState('')
	const [videoSummary, setVideoSummary] = useState('')

	const handleFileChange = (e) => {
		setFile(e.target.files[0])
	}

	const handleUpload = async (e) => {
		e.preventDefault()
		if (!file) {
			setUploadStatus('Please select a file to upload.')
			return
		}

		const formData = new FormData()
		formData.append('video', file)

		try {
			setUploadStatus('Uploading...')
			const response = await axios.post('/upload', formData, {
				headers: {
					'Content-Type': 'multipart/form-data',
				},
			})
			setUploadStatus('Upload successful!')
			setFrameCount(response.data.frames.length)
			setAudioPath(response.data.audio)
			setAudioTranscription(response.data.audioTranscription)
			setVideoSummary(response.data.videoSummary)
		} catch (error) {
			console.error('Error uploading file:', error)
			setUploadStatus('Upload failed.')
		}
	}

	return (
		<Box>
			<Heading>AI Video Summarizer</Heading>
			<Box py="4">

			<form onSubmit={handleUpload}>
				<input type="file" accept="video/*" onChange={handleFileChange} />
				<button type="submit">Upload</button>
			</form>
			</Box>
			<Badge>{uploadStatus}</Badge>
			{frameCount > 0 && (
				<div>
					<Badge>Number of Extracted Frames: {frameCount}</Badge>
				</div>
			)}
			{audioPath && (
				<div>
					<Badge>
						<a href={audioPath} download>
							Download extracted audio
						</a>
					</Badge>
				</div>
			)}
			{ videoSummary && (
				<Box py="4">
					<Markdown>{ videoSummary }</Markdown>
				</Box>
			)}
		</Box>
	)
}

export default VideoUpload