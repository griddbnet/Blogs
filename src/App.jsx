import InvoiceTable from './InvoiceTable'
import { useState, useEffect } from 'react'
import { CreateSlideButton } from './CreateSlideButton'

const Home = () => {
	const [fileMetadata, setFileMetadata] = useState({})
	const [selectedFile, setSelectedFile] = useState('')
	const [tableData, setTableData] = useState([])
	const [loading, setLoading] = useState(false)
	const [pptxPath, setPptxPath] = useState('')

	useEffect(() => {
		// Fetch the metadata for the files
		fetch(`${import.meta.env.VITE_APP_URL}/metadata`)
			.then(response => response.json())
			.then(data => {
				if (typeof data === 'object' && data !== null) {
					setFileMetadata(data)
				} else {
					console.error('Fetched data is not an object:', data)
				}
			})
			.catch(error => console.error('Error fetching file metadata:', error))
	}, [])

	useEffect(() => {
		if (selectedFile) {
			const selectedFileId = fileMetadata[selectedFile].name
			const baseURL = `${import.meta.env.VITE_APP_URL}/data/files/${selectedFileId}`
			fetch(baseURL)
				.then(response => response.json())
				.then(data => {
					if (Array.isArray(data)) {
						setTableData(data)
					} else {
						console.error('Fetched data is not an array:', data)
					}
				})
				.catch(error => console.error('Error fetching data:', error))
		}
	}, [selectedFile, fileMetadata])

	const handleFileChange = (e) => {
		setSelectedFile(e.target.value)
	}

	const handleCreateSlide = async () => {
		if (!selectedFile) return

		setLoading(true)

		try {
			const fileId = fileMetadata[selectedFile].id
			const response = await fetch(`${import.meta.env.VITE_APP_URL}/create/${fileId}`)
			if (response.ok) {
				const responseData = await response.json()
				console.log(responseData.data)
				setPptxPath(responseData.pptx)
				console.log('Slide creation request sent successfully')
			} else {
				console.error('Failed to send slide creation request')
			}
		} catch (error) {
			console.error('Error in creating slide:', error)
		} finally {
			setLoading(false)
		}
	}

	return (
		<div className="p-4">
			<h1 className='text-3xl py-5'>AI-Powered Slide Creator</h1>
			<div className="flex items-center space-x-4 mb-4">
				<select
					onChange={handleFileChange}
					value={selectedFile}
					className="bg-gray-900 text-white font-semibold py-2 px-4 rounded border border-gray-700"
				>
					<option value="" disabled>Select a file</option>
					{Object.keys(fileMetadata).map((key) => (
						<option key={key} value={key}>{fileMetadata[key].name}</option>
					))}
				</select>
				<CreateSlideButton
					disabled={!selectedFile}
					loading={loading}
					onClick={handleCreateSlide}
				/>

				{pptxPath && (
				<a
					href={`${import.meta.env.VITE_APP_URL}/${pptxPath}`}
					className="text-blue-500 underline"
					download
				>
					Download Generated Presentation
				</a>
			)}
			</div>
			{tableData.length > 0 && <InvoiceTable data={tableData} />}
			
		</div>
	)
}

export default Home