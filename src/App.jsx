import CustomBox from './CustomBox'
import WebcamContainer from './WebcamContainer'
import { useEffect, useState } from 'react'

const App = () => {
    const [colors, setColors] = useState([])

    const handleColorsExtracted = (colors) => {
        console.log('Extracted colors:', colors)
        setColors(colors)
    }

    useEffect(() => {
        const handleColorsExtractedEvent = (event) => {
            handleColorsExtracted(event.detail)
        }

        window.addEventListener('colorsExtracted', handleColorsExtractedEvent)

        // Clean up the event listener on unmount
        return () => {
            window.removeEventListener(
                'colorsExtracted',
                handleColorsExtractedEvent
            )
        }
    }, [])

    return (
        <div id="content-container">
            <WebcamContainer onColorsExtracted={handleColorsExtracted} />
            <CustomBox colors={colors} />
        </div>
    )
}

export default App
