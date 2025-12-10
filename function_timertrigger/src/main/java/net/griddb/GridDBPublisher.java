package net.griddb;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventHubOutput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class GridDBPublisher {
    @FunctionName("GridDBPublisher")
    @EventHubOutput(name = "outputEvent", eventHubName = "griddb-telemetry", // <--- YOUR EVENT HUB NAME
            connection = "EventHubConnectionAppSetting" // <--- CONNECTION STRING SETTING NAME
    )
    public List<String> run( // Change return type to List<String> for batching
            @TimerTrigger(name = "timerInfo", schedule = "0 */1 * * * *") String timerInfo,
            final ExecutionContext context) {

        // 1. Array to hold the serialized GridDB data (JSON Strings)
        List<String> recordsToPublish = new ArrayList<>();

        GridDBJdbc griddbSql = new GridDBJdbc();
        try {
            // Grabbing the last time data was pushed to Kafka
            java.sql.Timestamp last_pushed_timestamp = griddbSql.GetMaxTime(context, "ControlTable",
                    "last_pushed_time");
            // Query the telemtry data table using the timestamp from above.
            // If values are newer than our control table says, grab those rows
            BatchResult result = griddbSql.GetTelemetryDataNewerThanControlTimeStamp(context, "telemetryData",
                    last_pushed_timestamp);
            recordsToPublish = result.getRecords();

            if (recordsToPublish.size() > 0) {
                java.sql.Timestamp max_telemetryTs = result.getMaxTimestamp();
                griddbSql.WriteLastPushedTimeStampToControlTable(context, max_telemetryTs);
            } else {
                return recordsToPublish;
            }
        } catch (SQLException e) {
            context.getLogger().log(Level.SEVERE, "Error processing row or serializing JSON", e);
        }

        return recordsToPublish; // The binding sends the contents of this list
    }

}