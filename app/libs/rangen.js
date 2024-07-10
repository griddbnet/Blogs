function generateRandomID() {
	const min = 1; 
	const max = 10000; 

	const randomDecimal = Math.random();
	const scaledNumber = Math.floor(randomDecimal * (max - min + 1)) + min;
	const randomID = Math.floor(scaledNumber);

	return randomID;
}

export { generateRandomID }