import ColorRectangles from './ColorRectangle'

// eslint-disable-next-line react/prop-types
const CustomBox = ({ colors }) => {
    return (
        <div id="chatbox">
            <div className="loader" style={{ display: 'none' }}></div>
            <ColorRectangles colors={colors} />
        </div>
    )
}

export default CustomBox
