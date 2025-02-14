import PptxGenJS from "pptxgenjs";

export function createPresentation(options) {
	const {
		title,
		subtitle,
		dataVisTitle,
		chartImagePath,
		keyInsights,
		bulletPoints,
		outputFilename
	} = options;

	let pptx = new PptxGenJS();

	// Title Slide
	let titleSlide = pptx.addSlide();
	titleSlide.background = { color: "000000" };

	titleSlide.addText(title, {
		x: 0,
		y: 2.5,
		w: "100%",
		h: 1,
		fontSize: 44,
		bold: true,
		color: "FFFFFF",
		align: pptx.AlignH.center,
	});

	titleSlide.addText(subtitle, {
		x: 0,
		y: 3.5,
		w: "100%",
		h: 1,
		fontSize: 24,
		color: "FFFFFF",
		align: pptx.AlignH.center,
	});

	// Data Visualization Slide
	let dataVisSlide = pptx.addSlide();
	dataVisSlide.background = { color: "000000" };

	dataVisSlide.addText(dataVisTitle, {
		x: 0,
		y: 0.3,
		w: "100%",
		h: 0.5,
		fontSize: 32,
		bold: true,
		color: "FFFFFF",
		align: pptx.AlignH.center,
	});

	// Add the chart image
	dataVisSlide.addImage({
		path: chartImagePath,
		x: 0.5,
		y: 1,
		w: 6,
		h: 4.5,
	});

	// Add "Key Insights:" text
	dataVisSlide.addText(keyInsights, {
		x: 7,
		y: 1,
		w: 3,
		h: 0.5,
		fontSize: 24,
		bold: true,
		color: "00B050",
		align: pptx.AlignH.left,
	});

	// Add bullet points
	dataVisSlide.addText(bulletPoints.join("\n\n"), {
		x: 7,
		y: 1.7,
		w: 3,
		h: 3,
		fontSize: 14,
		color: "FFFFFF",
		bullet: true,
		align: pptx.AlignH.left,
		lineSpacing: 20,
	});

	pptx.writeFile({ fileName: outputFilename });
	console.log(`Create Presentation: ${outputFilename}`)
	return pptx;
}
