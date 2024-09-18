// src/App.js
import { Card, Box, Grid } from '@radix-ui/themes'
import VideoUpload from './VideoUpload'

const App = () => {
	return (
		<Grid columns="3" gap="3" width="auto" p="8">
           <Box></Box>
			<Card>
				<VideoUpload />
			</Card>
			<Box></Box>
		</Grid>
	)
}

export default App