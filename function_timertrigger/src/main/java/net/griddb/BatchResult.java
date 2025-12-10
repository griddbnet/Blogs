package net.griddb;

import java.sql.Timestamp;
import java.util.List;

// Class is for handling the batch results from our telemetry data
// records here will be returned to our kafka producer
// and max timestamp is for keeping track of the max time of the batched telemetry data
// which is written into the control table
public class BatchResult {
    private final List<String> records;
    private final Timestamp maxTimestamp;

    public BatchResult(List<String> records, Timestamp maxTimestamp) {
        this.records = records;
        this.maxTimestamp = maxTimestamp;
    }

    public List<String> getRecords() {
        return records;
    }

    public Timestamp getMaxTimestamp() {
        return maxTimestamp;
    }
}
