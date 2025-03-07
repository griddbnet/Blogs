package com.galapea.techblog.fitnesstracking.model;

import java.util.List;

public record HeartRateDashboard(List<HeartRateDto> heartRates, List<HeartRateZoneSummary> heartRateZoneSummaries) {

}
