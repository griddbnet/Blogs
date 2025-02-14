// eslint-disable-next-line react/prop-types
const ColorRectangles = ({ colors }) => {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 700 100"
            className="mx-auto"
        >
            {colors.map((color, index) => (
                <rect
                    key={index}
                    x={index * 100}
                    y="0"
                    width="100"
                    height="100"
                    fill={color}
                />
            ))}
        </svg>
    )
}

export default ColorRectangles
