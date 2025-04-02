import React, { useState } from 'react'

const FileUpload = () => {
    const [file, setFile] = useState(null)
    const [uploadStatus, setUploadStatus] = useState('')
    const [uploadData, setUploadData] = useState(null)

    const handleFileChange = (e) => {
        setFile(e.target.files[0])
    }

    const handleUpload = async () => {
        if (!file) {
            setUploadStatus('Please select a file first.')
            return
        }

        const formData = new FormData()
        formData.append('file', file)

        try {
            setUploadStatus('Uploading...')
            const response = await fetch('/api/upload', {
                method: 'POST',
                body: formData,
            })

            if (!response.ok) {
                throw new Error('Network response was not ok')
            }

            const data = await response.json()
            setUploadData(data)
            setUploadStatus('Upload successful!')
        } catch (error) {
            console.error('Error uploading file:', error)
            setUploadStatus('Error uploading file. Please try again.')
        }
    }

    const getDownloadLink = (filename) => {
        return `/${filename}`
    }

    return (
        <div className="max-w-xl mx-auto p-4 border border-gray-300 rounded-lg shadow-md bg-white">
            <h2 className="text-xl font-semibold mb-4">File Upload</h2>
            <input 
                type="file" 
                onChange={handleFileChange} 
                className="block w-full text-sm text-gray-500 file:py-2 file:px-4 file:border file:border-gray-300 file:rounded-md file:text-sm file:font-semibold file:bg-gray-100 file:text-gray-700 hover:file:bg-gray-200"
            />
            <button 
                onClick={handleUpload} 
                className="mt-4 px-4 py-2 bg-blue-500 text-white font-semibold rounded-md shadow hover:bg-blue-600 transition"
            >
                Upload
            </button>
            {uploadStatus && (
                <p className={`mt-4 ${uploadStatus.includes('successful') ? 'text-green-500' : 'text-red-500'}`}>
                    {uploadStatus}
                </p>
            )}
            {uploadData && (
                <div className="mt-4 p-4 border border-gray-200 rounded-md bg-gray-50">
                    <h3 className="text-lg font-semibold mb-2">Upload Data:</h3>
                    <pre className="whitespace-pre-wrap break-words text-sm text-gray-700">{JSON.stringify(uploadData, null, 2)}</pre>
                    
                    <div className="mt-4">
                        <h4 className="text-md font-semibold mb-2">Download Links:</h4>
                        {uploadData.filename && (
                            <a 
                                href={getDownloadLink(uploadData.filename)}
                                download
                                className="block mb-2 text-blue-500 hover:underline"
                            >
                                Download Video File
                            </a>
                        )}
                        {uploadData.voice && (
                            <a 
                                href={getDownloadLink(uploadData.voice)}
                                download
                                className="block text-blue-500 hover:underline"
                            >
                                Download Voice File
                            </a>
                        )}
                    </div>
                </div>
            )}
        </div>
    )
}

export default FileUpload