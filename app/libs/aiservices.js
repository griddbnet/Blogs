import fs from 'fs';
import OpenAI from "openai";

const openai = new OpenAI({
	// eslint-disable-next-line no-undef
	apiKey: process.env.OPENAI_API_KEY
});

async function createVideoSummarization(frames, audioTranscription) {
	const frameObjects = frames.map(x => ({
		type: 'image_url',
		image_url: {
			url: `data:image/png;base64,${x}`,
			detail: "low"
		}
	}));

	const videoContent = {
		role: "user",
		content: [
			{ type: 'text', text: "These are the frames from the video." },
			...frameObjects,
			{ type: 'text', text: `The audio transcription is: ${audioTranscription}` }
		],
	}

	const response = await openai.chat.completions.create({
		model: "gpt-4o",
		messages: [
			{
				role: "system",
				content: "You are generating a video summary. Please provide a summary of the video. Respond in Markdown."
			},
			videoContent
		],
		temperature: 0,
	});

	console.log(response)
	const summarizeResponse = response.choices[0].message.content
	return summarizeResponse;
}

async function transcribeAudio(filePath) {
	try {
		const transcription = await openai.audio.transcriptions.create({
			file: fs.createReadStream(filePath),
			model: 'whisper-1'
		})
		return transcription.text
	} catch (error) {
		throw new Error(`Transcription failed: ${error.message}`)
	}
}

export { createVideoSummarization, transcribeAudio }