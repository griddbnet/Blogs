package net.griddb_cloud;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.sql.Timestamp;

public class IotTelemetryHandler {

	private static GridDB griddb = null;
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@FunctionName("IoTHubTrigger")
	public void run(
			@EventHubTrigger(name = "message", eventHubName = "events", connection = "IotHubConnectionString", consumerGroup = "myfuncapp-cg", cardinality = Cardinality.ONE) String message,

			@BindingName("SystemProperties") Map<String, Object> properties,

			final ExecutionContext context) {

		TelemetryData data;
		try {
			data = MAPPER.readValue(message, TelemetryData.class);

		} catch (Exception e) {
			context.getLogger().severe("Failed to parse JSON message: " + e.getMessage());
			context.getLogger().severe("Raw Message: " + message);
			return;
		}

		try {
			context.getLogger().info("Java Event Hub trigger processed a message: " + message);

			String deviceId = properties.get("iothub-connection-device-id").toString();
			String eventTimeIso = properties.get("iothub-enqueuedtime").toString();

			Instant enqueuedInstant = Instant.parse(eventTimeIso);
			long eventTimeMillis = enqueuedInstant.toEpochMilli();
			Timestamp dbTimestamp = new Timestamp(eventTimeMillis);
			data.ts = dbTimestamp;

			context.getLogger().info("Data received from Device: " + deviceId);

			griddb = new GridDB();
			String containerName = "telemetryData";
			griddb.CreateContainer(containerName);
			griddb.WriteToContainer(containerName, data);
			context.getLogger().info("Successfully saved to DB.");

		} catch (Throwable t) {
			context.getLogger().severe("CRITICAL: Function execution failed with exception:");
			context.getLogger().severe(t.toString());
			// throw new RuntimeException("GridDB processing failed", t);
		}
	}

}