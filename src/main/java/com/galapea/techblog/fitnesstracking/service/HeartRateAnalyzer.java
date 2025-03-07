package com.galapea.techblog.fitnesstracking.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.galapea.techblog.fitnesstracking.Helper;
import com.galapea.techblog.fitnesstracking.entity.HeartRate;
import com.galapea.techblog.fitnesstracking.model.HeartRateZoneSummary;

public class HeartRateAnalyzer {

	public static List<HeartRateZoneSummary> analyze(List<HeartRate> heartRates) {
		List<HeartRateZoneSummary> heartRateZoneSummaries = new ArrayList<>();
		long peakZoneTime = 0;
		long cardioZoneTime = 0;
		long fatBurnZoneTime = 0;
		LocalDateTime previousTime = null;
		for (HeartRate heartRate : heartRates) {
			if (previousTime != null) {
				long durationInMillis = Duration
					.between(previousTime, Helper.convertToLocalDateTime(heartRate.getTimestamp()))
					.toMillis();

				if (heartRate.getValue() >= HeartRateZones.PEAK_ZONE_LOWER_LIMIT) {
					peakZoneTime += durationInMillis;
				}
				else if (heartRate.getValue() >= HeartRateZones.CARDIO_ZONE_LOWER_LIMIT) {
					cardioZoneTime += durationInMillis;
				}
				else if (heartRate.getValue() <= HeartRateZones.FAT_BURN_ZONE_UPPER_LIMIT) {
					fatBurnZoneTime += durationInMillis;
				}
			}

			previousTime = Helper.convertToLocalDateTime(heartRate.getTimestamp());
		}

		heartRateZoneSummaries.addAll(Arrays.asList(
				HeartRateZoneSummary.builder()
					.zoneValue("Peak Zone")
					.durationInMinuets(TimeUnit.MILLISECONDS.toMinutes(peakZoneTime))
					.build(),
				HeartRateZoneSummary.builder()
					.zoneValue("Cardio Zone")
					.durationInMinuets(TimeUnit.MILLISECONDS.toMinutes(cardioZoneTime))
					.build(),
				HeartRateZoneSummary.builder()
					.zoneValue("Fat Burn Zone")
					.durationInMinuets(TimeUnit.MILLISECONDS.toMinutes(fatBurnZoneTime))
					.build()));

		return heartRateZoneSummaries;
	}

}
