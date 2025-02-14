import OpenAI from 'openai'

// eslint-disable-next-line no-undef
if (!process.env.OPENAI_API_KEY) {
	throw new Error('OPENAI_API_KEY environment variable is not set')
}

const openai = new OpenAI({
	// eslint-disable-next-line no-undef
	apiKey: process.env.OPENAI_API_KEY,
})

const systemPrompt = `You are an AI specialized in colorimetry, the science and technology of color detection and measurement. You possess deep knowledge of the principles of color science, including color spaces, color matching functions, and the use of devices such as spectrophotometers and colorimeters. You provide accurate and detailed analyses of color properties, offer solutions for color consistency issues, and assist in applications ranging from imaging and printing to manufacturing and display technologies. Use your expertise to answer questions, solve problems, and provide color detection and measurement guidance.`

const userPrompt =
	'Extract the seven most prominent colors from the provided image. Use color clustering techniques to identify and present these colors in Hex values. Answer with the raw array values ONLY. DO NOT FORMAT IT.'

/**
 * @param {*} apiKey
 * @param {*} base64Image
 * @param {*} model
 * @returns
 *
 * Usage example:
 * const apiKey = process.env.OPENAI_API_KEY
 * const base64Image = 'your_base64_image_string_here'
 * getColorAnalysis(apiKey, base64Image).then(response => console.log(response))
 *
 */
async function getColorAnalysis(base64Image) {
	const response = await openai.chat.completions.create({
		model: 'gpt-4o-mini-2024-07-18',
		messages: [
			{
				role: 'system',
				content: systemPrompt,
			},
			{
				role: 'user',
				content: [
					{
						type: 'image_url',
						image_url: {
							url: base64Image,
						},
					},
					{
						type: 'text',
						text: userPrompt,
					},
				],
			},
		],
		temperature: 0.51,
		max_tokens: 3000,
		top_p: 1,
		frequency_penalty: 0,
		presence_penalty: 0,
	})

	return response
}

export { getColorAnalysis }
