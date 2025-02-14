export function ColorPalette({ colors }) {
    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100">
            <div className="p-6 bg-white rounded-lg shadow-lg">
                <h2 className="text-2xl font-bold mb-4 text-center">
                    Color Palette
                </h2>
                <div className="w-[700px] h-[100px]">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 700 100"
                        className="w-full h-full"
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
                </div>
                <div className="mt-4 grid grid-cols-7 gap-2">
                    {colors.map((color, index) => (
                        <div key={index} className="text-center">
                            <div className="text-xs">{color}</div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}
