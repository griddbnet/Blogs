import OpenAI from 'openai'
import fs from 'fs'
import path from 'path'

const openai = new OpenAI({
	apiKey: process.env.OPENAI_API_KEY,
})

async function generateTitle(narrative) {
	const titleResponse = await openai.chat.completions.create({
		model: 'gpt-4o-mini',
		messages: [
			{
				role: 'system',
				content: 'You are a professional title generator.'
			},
			{
				role: 'user',
				content: `Direct answer only. Generate a title for the following narrative text: \n${narrative}`
			}
		],
		temperature: 1,
		max_tokens: 1000,
		top_p: 1,
		frequency_penalty: 0,
		presence_penalty: 0,
		response_format: {
			"type": "text"
		},
	})

	const title = titleResponse.choices[0].message.content
	return title
}

async function generateNarrative(frames, videoDuration = 10) {

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
			{ type: 'text', text: `The original video, in which frames are generated  is ${videoDuration} seconds. Create a story based on these frames that fit for exactly ${videoDuration} seconds. BE CREATIVE. DIRECT ANSWER ONLY.` },
			...frameObjects
		],
	}

	const response = await openai.chat.completions.create({
		model: "gpt-4o-2024-08-06",
		messages: [
			{
				role: "system",
				content: "You are a professional storyteller."
			},
			videoContent
		],
		temperature: 1,
		max_tokens: 4095,
		top_p: 1,
		frequency_penalty: 0,
		presence_penalty: 0,
		response_format: {
			"type": "text"
		},
	});

	if (response.choices[0].finish_reason === 'stop') {
		const narrative = response.choices[0].message.content
		const title = await generateTitle(narrative)
		
		const fileName = title.split(' ').join('-').toLowerCase()
		const voice = await generateSpeechToFile(narrative, 'audio', fileName)

		return { narrative, title, voice }
	}
	else {
		throw new Error('Failed to generate narrative')
	}
}

/**
 * Generate speech from text and save it to an MP3 file inside a specified folder
 * @param {string} text - The input text to convert into speech
 * @param {string} folderPath - The folder path where the MP3 will be saved
 * @param {string} fileName - The name of the MP3 file (without extension)
 * @param {string} [model="tts-1"] - The model to use for text-to-speech
 * @param {string} [voice="alloy"] - The voice to use for text-to-speech
 * @returns {Promise<string>} - Returns the file path where the MP3 was saved
 */
async function generateSpeechToFile(text, folderPath, fileName, model = 'tts-1', voice = 'shimmer') {
	try {
		if (!fs.existsSync(folderPath)) {
			await fs.promises.mkdir(folderPath, { recursive: true });
		}
        
		const mp3Filename = `${fileName}.mp3`
		const outputFilePath = path.join(folderPath, mp3Filename);
		const mp3 = await openai.audio.speech.create({
			model,
			voice,
			input: text,
		});

		const buffer = Buffer.from(await mp3.arrayBuffer());
		await fs.promises.writeFile(outputFilePath, buffer);
		console.log(`File saved at: ${outputFilePath}`);
		return mp3Filename;
	} catch (error) {
		console.error('Error generating speech:', error);
		throw error;
	}
}

export { generateNarrative, generateTitle, generateSpeechToFile as generateVoice }
