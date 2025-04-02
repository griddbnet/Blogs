import ffmpeg from 'fluent-ffmpeg'
import fs from 'fs'
import path from 'path'

function cleanFramesFolder(folderPath) {
	return new Promise((resolve, reject) => {
		fs.readdir(folderPath, (err, files) => {
			if (err) {
				return reject(err)
			}

			const pngFiles = files.filter(file => path.extname(file).toLowerCase() === '.png' && /^frame-\d{3}\.png$/.test(file))

			const deletePromises = pngFiles.map(file => new Promise((res, rej) => {
				fs.unlink(path.join(folderPath, file), err => {
					if (err) {
						return rej(err)
					}
					res()
				})
			}))

			Promise.all(deletePromises)
				.then(() => resolve())
				.catch(reject)
		})
	})
}

export const outputFolder = 'frames'
export const outputAudioFolder = 'audio'

export function extractFrames(videoPath, secondsPerFrame, outputFolder, scaleFactor = 0.5) {
	return new Promise((resolve, reject) => {
		const frameRate = 1 / secondsPerFrame
		const framePattern = path.join(outputFolder, 'frame-%03d.png')
		const resizeOptions = `fps=${frameRate},scale=iw*${scaleFactor}:ih*${scaleFactor}`

		ffmpeg(videoPath)
			.outputOptions([`-vf ${resizeOptions}`])
			.output(framePattern)
			.on('end', () => {
				fs.readdir(outputFolder, (err, files) => {
					if (err) {
						reject(err)
					} else {
						const framePaths = files.map(file => path.join(outputFolder, file))
						resolve(framePaths)
					}
				})
			})
			.on('error', reject)
			.run()
	})
}

// Function to convert an image file to base64 using Buffer with UTF-8 encoding
export function imageToBase64(imagePath) {
	console.log(`imagePath: ${imagePath}`)
	return new Promise((resolve, reject) => {
		const fileName = path.basename(imagePath)
		const isPng = path.extname(fileName).toLowerCase() === '.png'
		const isValidPattern = /^frame-\d{3}\.png$/.test(fileName)

		if (!isPng || !isValidPattern) {
			const errorMsg = `Skipping file: ${fileName}. Only files matching the pattern frame-###.png are processed.`
			console.log(errorMsg)
			resolve(null) // Skip invalid files by resolving with null
			return
		}

		fs.readFile(imagePath, (err, data) => {
			if (err) {
				console.error(`Error reading file ${imagePath}:`, err)
				reject(err)
			} else {
				try {
					const base64String = data.toString('base64')
					resolve(base64String)
				} catch (conversionError) {
					console.error(`Error converting file ${imagePath} to base64:`, conversionError)
					reject(conversionError)
				}
			}
		})
	})
}

async function getVideoDuration(filePath) {
	return new Promise((resolve, reject) => {
	  ffmpeg.ffprobe(filePath, (err, metadata) => {
		if (err) {
		  reject(err)
		} else {
		  const duration = metadata.format.duration
		  resolve(duration)
		}
	  })
	})
  }

export function extractAudio(videoPath, audioPath) {
	return new Promise((resolve, reject) => {
		ffmpeg(videoPath)
			.output(audioPath)
			.audioBitrate('32k')
			.on('end', resolve)
			.on('error', reject)
			.run()
	})
}

export async function processVideo(videoPath, secondsPerFrame = 4) {
	const base64Frames = []
	const baseVideoPath = path.parse(videoPath).name

	if (!fs.existsSync(outputFolder)) {
		fs.mkdirSync(outputFolder)
	}

	if (!fs.existsSync(outputAudioFolder)) {
		fs.mkdirSync(outputAudioFolder)
	}

	// Clean the frames folder before extracting new frames
	await cleanFramesFolder(outputFolder)

	const duration = await getVideoDuration(videoPath)

	// Extract frames from the video
	const framePaths = await extractFrames(videoPath, secondsPerFrame, outputFolder)

	// Convert each frame to base64 and filter out null values
	const base64Promises = framePaths.map(framePath => imageToBase64(framePath))
	const base64Results = await Promise.all(base64Promises)
	const validBase64Frames = base64Results.filter(base64 => base64 !== null)
	base64Frames.push(...validBase64Frames)

	const audioFilename = `${baseVideoPath}.mp3`
	const audioPath = path.join(outputAudioFolder, audioFilename)
	await extractAudio(videoPath, audioPath)

	console.log(`Extracted ${base64Frames.length} frames`)
	console.log(`Extracted audio to ${audioPath}`)
	return { base64Frames, audioFilename, duration }
}