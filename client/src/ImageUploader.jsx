import { useState } from 'react';

const ImageUploader = (props) => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [fileName, setFileName] = useState('No file chosen');
    const [uploading, setUploading] = useState(false); 

    const isValidFileType = (file) => {
        return ['image/jpeg', 'image/png'].includes(file.type);
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file && isValidFileType(file)) {
            setFileName(file.name);
            setSelectedFile(file);
        } else {
            alert('Please select a valid image file (JPG or PNG).');
        }
    };

    const handleUpload = async () => {
        if (selectedFile && !uploading) { 
            setUploading(true); 

            const formData = new FormData();
            formData.append('image', selectedFile);

            try {
                const response = await fetch('/process-image', {
                    method: 'POST',
                    body: formData,
                });
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                const markdownData = await response.json();
                props.onMarkdownFetch(markdownData);
            } catch (error) {
                console.error('Error posting image:', error);
            } finally {
                setUploading(false);
            }
        } else {
            alert('Please select an image first.');
        }
    };

    return (
        <div className="flex flex-col items-center justify-center p-4 space-y-4">
            <label className="w-64 flex flex-col items-center px-4 py-2 text-black tracking-wide uppercase cursor-pointer hover:bg-blue hover:text-gray-700">
                <svg className="w-8 h-8 hover:opacity-50" fill="currentColor" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" data-slot="icon" className="w-6 h-6">
						<path strokeLinecap="round" strokeLinejoin="round" d="M9 8.25H7.5a2.25 2.25 0 0 0-2.25 2.25v9a2.25 2.25 0 0 0 2.25 2.25h9a2.25 2.25 0 0 0 2.25-2.25v-9a2.25 2.25 0 0 0-2.25-2.25H15m0-3-3-3m0 0-3 3m3-3V15" />
					</svg>
                </svg>
                <span className="mt-2 text-base leading-normal overflow-hidden text-ellipsis whitespace-nowrap">{fileName}</span>

                <input type='file' className="hidden" accept="image/jpeg, image/png" onChange={handleFileChange} name="image" disabled={uploading} />
            </label>
            <button
                className={`bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded ${uploading ? 'cursor-not-allowed opacity-50' : ''}`}
                onClick={handleUpload}
                disabled={uploading}
            >
                {uploading ? 'Processing...' : 'Upload'}
            </button>
        </div>
    );
};

export default ImageUploader;
