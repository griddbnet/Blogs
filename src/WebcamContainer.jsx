// WebcamContainer.js
import { useEffect, useRef, useState } from 'react'

const parseColors = (colors) => {
    if (Array.isArray(colors)) {
        return colors
    }

    if (typeof colors === 'string') {
        return colors.split(/[\s,]+/).map((color) => color.trim())
    }

    return []
}

// eslint-disable-next-line react/prop-types
const WebcamContainer = ({ onColorsExtracted }) => {
    const videoRef = useRef(null)
    const canvasRef = useRef(null)
    const [usingFrontCamera, setUsingFrontCamera] = useState(true)

    const imageProcessingURL = `${import.meta.env.VITE_APP_URL}/process-image`

    useEffect(() => {
        initializeWebcam()
    }, [])

    const initializeWebcam = () => {
        navigator.mediaDevices
            .getUserMedia({ video: true })
            .then((stream) => {
                videoRef.current.srcObject = stream
            })
            .catch((error) => {
                console.error('getUserMedia error:', error)
            })
    }

    const captureImage = () => {
        const context = canvasRef.current.getContext('2d')
        context.drawImage(
            videoRef.current,
            0,
            0,
            canvasRef.current.width,
            canvasRef.current.height
        )

        const base64Image = canvasRef.current.toDataURL('image/jpeg')
        processImage(base64Image)
    }

    const processImage = (base64Image) => {
        toggleLoader(true)

        fetch(imageProcessingURL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ image: base64Image }),
        })
            .then((response) => response.json())
            .then(handleResponse)
            .catch(handleError)
    }

    const handleResponse = (data) => {
        let colorsData = data?.message.content
        toggleLoader(false)
        if (data.error) {
            console.error(data.error)
            appendToChatbox(`Error: ${data.error}`, true)
            return
        }

        appendToChatbox(colorsData)

        if (
            Array.isArray(parseColors(colorsData)) &&
            parseColors(colorsData).every((color) => typeof color === 'string')
        ) {
            // Dispatch a custom event with the colors
            const event = new CustomEvent('colorsExtracted', {
                detail: parseColors(colorsData),
            })
            window.dispatchEvent(event)
            if (typeof onColorsExtracted === 'function') {
                onColorsExtracted(parseColors(colorsData))
            }
        }
    }

    const handleError = (error) => {
        toggleLoader(false)
        console.error('Fetch error:', error)
        //appendToChatbox(`Error: ${error.message}`, true)
    }

    const toggleLoader = (show) => {
        document.querySelector('.loader').style.display = show
            ? 'block'
            : 'none'
    }

    const appendToChatbox = (message, isUserMessage = false) => {
        const chatbox = document.getElementById('chatbox')
        const messageElement = document.createElement('div')
        const timestamp = new Date().toLocaleTimeString()

        messageElement.className = isUserMessage
            ? 'user-message'
            : 'assistant-message'
        messageElement.innerHTML = `<div class="message-content">${message}</div><div class="timestamp">${timestamp}</div>`

        if (chatbox.firstChild) {
            chatbox.insertBefore(messageElement, chatbox.firstChild)
        } else {
            chatbox.appendChild(messageElement)
        }
    }

    const switchCamera = () => {
        setUsingFrontCamera(!usingFrontCamera)
        const constraints = {
            video: { facingMode: usingFrontCamera ? 'user' : 'environment' },
        }

        if (videoRef.current.srcObject) {
            videoRef.current.srcObject
                .getTracks()
                .forEach((track) => track.stop())
        }

        navigator.mediaDevices
            .getUserMedia(constraints)
            .then((stream) => {
                videoRef.current.srcObject = stream
            })
            .catch((error) => {
                console.error('Error accessing media devices.', error)
            })
    }

    return (
        <div id="webcam-container">
            <video
                id="webcam"
                ref={videoRef}
                autoPlay
                playsInline
                width="640"
                height="480"
            ></video>
            <canvas
                id="canvas"
                ref={canvasRef}
                width="640"
                height="480"
                style={{ display: 'none' }}
            ></canvas>
            <button id="capture" onClick={captureImage}>
                Capture
            </button>
            <button id="switch-camera" onClick={switchCamera}>
                Switch Camera
            </button>
        </div>
    )
}

export default WebcamContainer
