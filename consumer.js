const net = require('net');
const { RecordBatchReader } = require('apache-arrow');

const HOST = '127.0.0.1';
const PORT = 2828;

const client = new net.Socket();

client.connect(PORT, HOST, async () => {
    console.log(`Connected to Python producer at ${HOST}:${PORT}`);

    try {
        // Create the reader from the socket stream.
        const reader = await RecordBatchReader.from(client);

        let schemaPrinted = false;

        for await (const recordBatch of reader) {

            if (!schemaPrinted) {
                console.log("Successfully parsed schema from stream.");
                console.log(`Schema:`, reader.schema.fields.map(f => `${f.name}: ${f.type}`).join(', '));
                console.log("--- Processing data batches ---");
                schemaPrinted = true;
            }

            // Convert the record batch to a more familiar JavaScript object format
            const data = recordBatch.toArray().map(row => row.toJSON());
            console.log("Received data batch:", data);
        }

        console.log("-------------------------------");
        console.log("Stream finished.");

    } catch (error) {
        console.error("Error processing Arrow stream:", error);
    }
});

client.on('close', () => {
    console.log('Connection closed');
});

client.on('error', (err) => {
    console.error('Connection error:', err.message);
});