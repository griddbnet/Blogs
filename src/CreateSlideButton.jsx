import React from 'react'

export const CreateSlideButton = ({ disabled, loading, onClick }) => {
	return (
		<button
			onClick={onClick}
			disabled={disabled || loading}
			className={`bg-blue-500 text-white font-semibold py-2 px-4 rounded border border-blue-700 ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
		>
			{loading ? 'Processing...' : 'Create Slide'}
		</button>
	)
}