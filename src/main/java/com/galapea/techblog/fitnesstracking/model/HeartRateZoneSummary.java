package com.galapea.techblog.fitnesstracking.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeartRateZoneSummary {

	String zoneValue;

	long durationInMinuets;

}
