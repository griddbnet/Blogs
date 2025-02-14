import fs from 'node:fs'
import OpenAI from "openai"
import path from 'node:path'
import { Buffer } from "node:buffer"
import { __dirname } from '../dirname.js'
import { createPresentation } from './pptx.js'

const openai = new OpenAI({
	// eslint-disable-next-line no-undef
	apiKey: process.env.OPENAI_API_KEY
});

const insightPrompt = `Give me two medium length sentences (~20-30 words per sentence) of the most important insights from the plot you just created, save each sentence as item in one array. Give me a raw array, no formatting, no commentary. These will be used for a slide deck, and they should be about the 'so what' behind the data.`

const analyzeDataPrompt = "Calculate profit (revenue minus cost) by quarter and year, and visualize as a line plot across the distribution channels, where the colors of the lines are green, light red, and light blue"

const titlePrompt = "Given the plot and bullet points you created,come up with a very brief title only for a slide. It should reflect just the main insights you came up with."

const slideTitle = "Global Auto Parts Distribution Inc."
const slideSubtitle = "Quarterly financial planning meeting"

const dataScienceAssistantId = "asst_FOqRnMVXw0WShTSGw70NZJAX"

export async function aiAssistant(fileId) {

	const thread = await openai.beta.threads.create({
		messages: [
			{
				"role": "user",
				"content": analyzeDataPrompt,
				"attachments": [
					{
						file_id: fileId,
						tools: [{ type: "code_interpreter" }]
					}
				]
			}
		]
	});

	const run = await openai.beta.threads.runs.createAndPoll(
		thread.id,
		{ assistant_id: dataScienceAssistantId }
	);

	if (run.status === "completed") {
		const messages = await openai.beta.threads.messages.list(thread.id)
		console.log(messages.data[0].content[0].image_file)

		try {
			const filename = `output-${fileId}.png`
			const analyticFileId = messages.data[0].content[0].image_file.file_id

			convertFileToPng(analyticFileId, path.join(__dirname, 'public', filename))

			let presentationOptions
			const pptxFilename = `generated-presentation-${analyticFileId}.pptx`
			// AI Insight
			const result = await addMessage(dataScienceAssistantId, thread.id, insightPrompt)

			if (result.status === 'completed') {
				const message = await openai.beta.threads.messages.list(thread.id)
				const bulletPointsText = message.data[0].content[0].text.value

				let bulletPoints
				try {
					bulletPoints = JSON.parse(bulletPointsText.replace(/'/g, '"'))
					if (!Array.isArray(bulletPoints)) {
						throw new Error('Parsed bulletPoints is not an array')
					}
				} catch (error) {
					console.error('Error parsing bulletPoints:', error)
				}

				console.log(bulletPoints)

				const bulletPointsSummary = await addMessage(dataScienceAssistantId, thread.id, titlePrompt)

				if (bulletPointsSummary.status === "completed") {
					const message = await openai.beta.threads.messages.list(thread.id)
					const dataVisTitle = message.data[0].content[0].text.value

					presentationOptions = {
						title: slideTitle,
						subtitle: slideSubtitle,
						dataVisTitle: dataVisTitle,
						chartImagePath: path.join(__dirname, "public", `${filename}`),
						keyInsights: "Key Insights:",
						bulletPoints: bulletPoints,
						outputFilename: path.join(__dirname, 'public', pptxFilename)
					};

					try {
						createPresentation(presentationOptions)
					} catch (error) {
						console.log(error)
					}
				}
			}

			return { data: presentationOptions, status: "completed", pptx: pptxFilename }
		} catch (error) {
			return { data: null, status: run.status, error }
		}
	} else {
		return { data: null, status: run.status, error: 'Error' }
	}
}

async function addMessage(assistantId, threadId, prompt) {
	await openai.beta.threads.messages.create(threadId, { role: 'user', content: prompt })
	const response = await openai.beta.threads.runs.createAndPoll(threadId, { assistant_id: assistantId })
	return response
}

// Helper function to convert output file to PNG
async function convertFileToPng(fileId, writePath) {
	try {
		// Retrieve file content from OpenAI
		const response = await openai.files.content(fileId)
		// Write buffer to a file
		const dataBuffer = Buffer.from(await response.arrayBuffer())

		fs.writeFile(writePath, dataBuffer, (err) => {
			if (err) {
				console.error('Error writing file:', err)
				throw err
			}
			console.log('File saved successfully to', writePath)
		})
	} catch (error) {
		console.error('Error fetching file content:', error)
		throw error
	}
}
