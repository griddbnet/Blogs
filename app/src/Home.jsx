import { useState } from 'react';
import { AudioRecorder, useAudioRecorder } from 'react-audio-voice-recorder';

export default function Home() {
	const apiUrl = import.meta.env.VITE_API_URL;
	const [response, setResponse] = useState(null);
	const [isLoading, setIsLoading] = useState(false);

	const recorderControls = useAudioRecorder({
		noiseSuppression: true,
		echoCancellation: true,
	},
		(err) => console.table(err)
	);

	const addAudioElement = (blob) => {
		setIsLoading(true);
		const url = URL.createObjectURL(blob);
		const audio = document.createElement('audio');
		audio.src = url;
		audio.controls = true;
		//document.body.appendChild(audio);

		const formData = new FormData();
		formData.append("file", blob, "audio.webm");

		fetch(`${apiUrl}/upload`, {
			method: 'POST',
			body: formData,
		})
			.then(response => response.json())
			.then(data => {
				setResponse(data);
				console.log('Success:', data);
				setIsLoading(false);

			})
			.catch((error) => {
				console.error('Error:', error);
				setIsLoading(false);
			});
	};

	return (
		<div className="hero min-h-screen bg-base-200">
			<div className="hero-content text-center">
				<div className="max-w-md">
					<h1 className="text-5xl font-bold">Audio Note</h1>
					<p className="py-6">Push the button below to start audio recording</p>
					<div className='flex justify-center items-center w-full my-24'>
						<AudioRecorder
							onRecordingComplete={(blob) => addAudioElement(blob)}
							recorderControls={recorderControls}
							showVisualizer={true}
						/>
					</div>
					{isLoading && <span className="loading loading-spinner loading-lg"></span>}
					{response && (
						<div className="overflow-x-auto">
							<table className="table">
								<thead>
									<tr>
										<th>Audio File</th>
										<th>Transcription</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td className="underline"><a href={response.downloadLink}>{response.filename}</a></td>
										<td>{response.transcription}</td>
									</tr>
								</tbody>
							</table>
						</div>
					)}
				</div>
			</div>
		</div>
	);
}
