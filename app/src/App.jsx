import React from 'react'
import FileUpload from './components/FileUpload'

function App() {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-100">
            <h1 className="text-4xl font-bold mb-8 text-gray-800">Video Narrative Generator</h1>
            <FileUpload />
        </div>
    )
}

export default App
