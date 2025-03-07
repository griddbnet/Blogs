package com.galapea.techblog.fitnesstracking.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.galapea.techblog.fitnesstracking.Helper;
import com.galapea.techblog.fitnesstracking.entity.HeartRate;
import com.galapea.techblog.fitnesstracking.model.HeartRateDashboard;
import com.galapea.techblog.fitnesstracking.model.HeartRateDto;
import com.galapea.techblog.fitnesstracking.model.HeartRateZoneSummary;
import com.galapea.techblog.fitnesstracking.model.WorkoutCreated;

import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowSet;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeartRateService {

	private final Logger log = LoggerFactory.getLogger(WorkoutService.class);

	private final Collection<String, HeartRate> heartRateCollection;

	@Async
	@EventListener
	public void onWorkoutCreated(WorkoutCreated workoutCreated) {
		log.info("Workout Created: {}", workoutCreated);
		generateHeartRate(workoutCreated);
	}

	public void generateHeartRate(WorkoutCreated workout) {
		Faker faker = new Faker();
		LocalDateTime start = Helper.convertToLocalDateTime(workout.getStartTime());
		LocalDateTime end = Helper.convertToLocalDateTime(workout.getEndTime());
		for (LocalDateTime date = start; date.isBefore(end); date = date.plusMinutes(3)) {
			HeartRate heartRate = new HeartRate();
			heartRate.setWorkoutId(workout.getId());
			heartRate.setValue(faker.number().randomDouble(0, 50, 190));
			heartRate.setTimestamp(Date.from(date.toInstant(ZoneOffset.UTC)));
			heartRate.setId(KeyGenerator.next("hr_"));
			try {
				heartRateCollection.put(heartRate);
				log.debug("Append: {}", heartRate);
			}
			catch (GSException e) {
				log.error("Error generateHeartRate", e);
			}
		}
	}

	public HeartRateDashboard fetchForDashboardByWorkoutId(String workoutId) {
		List<HeartRateDto> heartRates = new ArrayList<>(0);
		List<HeartRate> heartRatesList = getHeartRatesList(workoutId);
		heartRates = heartRatesList.stream()
			.map(hr -> HeartRateDto.builder()
				.timestampLabel(getTimestampLabel(hr.getTimestamp()))
				.value(hr.getValue())
				.build())
			.collect(Collectors.toList());

		List<HeartRateZoneSummary> heartRateZoneSummaries = HeartRateAnalyzer.analyze(heartRatesList);

		return new HeartRateDashboard(heartRates, heartRateZoneSummaries);
	}

	private List<HeartRate> getHeartRatesList(String workoutId) {
		List<HeartRate> heartRatesList = new ArrayList<>(0);
		try {
			Query<HeartRate> query = heartRateCollection.query("SELECT * WHERE workoutId='" + workoutId + "'",
					HeartRate.class);
			RowSet<HeartRate> rowSet = query.fetch();
			while (rowSet.hasNext()) {
				HeartRate row = rowSet.next();
				log.debug("Fetched: {}", row);
				heartRatesList.add(row);
			}
		}
		catch (GSException e) {
			log.error("Error fetchAllByWorkoutId", e);
		}
		return heartRatesList;
	}

	private String getTimestampLabel(java.util.Date time) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(time);
		StringBuilder sb = new StringBuilder();
		int year = cal.get(java.util.Calendar.YEAR);
		int month = cal.get(java.util.Calendar.MONTH) + 1;
		int day = cal.get(java.util.Calendar.DAY_OF_MONTH);
		int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
		int minute = cal.get(java.util.Calendar.MINUTE);
		sb.append(year);
		sb.append('/');
		if (month < 10) {
			sb.append('0');
		}
		sb.append(month);
		sb.append('/');
		if (day < 10) {
			sb.append('0');
		}
		sb.append(day);
		sb.append(" ");
		if (hour < 10) {
			sb.append('0');
		}
		sb.append(hour);
		sb.append(":");
		if (minute < 10) {
			sb.append('0');
		}
		sb.append(minute);
		return sb.toString();
	}

}
