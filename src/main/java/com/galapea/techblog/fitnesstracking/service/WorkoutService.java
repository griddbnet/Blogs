package com.galapea.techblog.fitnesstracking.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.galapea.techblog.fitnesstracking.AppConstant;
import com.galapea.techblog.fitnesstracking.Helper;
import com.galapea.techblog.fitnesstracking.config.MapStructMapper;
import com.galapea.techblog.fitnesstracking.entity.Workout;
import com.galapea.techblog.fitnesstracking.entity.WorkoutType;
import com.galapea.techblog.fitnesstracking.model.WorkoutDto;

import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowSet;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkoutService {

	private final Logger log = LoggerFactory.getLogger(WorkoutService.class);

	private final Collection<String, Workout> workoutCollection;

	private final ApplicationEventPublisher applicationEventPublisher;

	private final MapStructMapper mapStructMapper;

	public List<WorkoutDto> fetchAll() {
		List<WorkoutDto> dtos = new ArrayList<>(0);
		try {
			String tql = "SELECT * FROM " + AppConstant.WORKOUT_CONTAINER + " ORDER BY createdAt DESC";
			Query<Workout> query = workoutCollection.query(tql);
			RowSet<Workout> rowSet = query.fetch();
			while (rowSet.hasNext()) {
				Workout row = rowSet.next();
				WorkoutDto dto = WorkoutDto.builder()
					.id(row.id)
					.title(row.title)
					.type(WorkoutType.valueOf(row.type))
					.userId(row.userId)
					.startTime(row.startTime)
					.endTime(row.endTime)
					.distance(row.distance)
					.duration(row.duration)
					.calories(row.calories)
					.createdAt(row.createdAt)
					.build();
				dtos.add(dto);
			}
		}
		catch (GSException e) {
			log.error("Error fetch all workouts", e);
		}
		return dtos;
	}

	public WorkoutDto fetchById(String id) {
		WorkoutDto result = null;
		try {
			Workout row = workoutCollection.get(id);
			if (row != null) {
				result = mapStructMapper.workoutToWorkoutDto(row);
				String durationText = Helper.getDurationText(row.startTime, row.endTime);
				result.setDurationText(durationText);
			}
		}
		catch (GSException e) {
			log.error("Error fetch a workout", e);
		}
		return result;
	}

	public void create(WorkoutDto workoutDto) {
		try {
			Double duration = workoutDto.getDuration() == null ? 0.0 : workoutDto.getDuration();
			if (workoutDto.getId() == null) {
				workoutDto.setId(KeyGenerator.next("wk_"));
			}
			Workout workout = Workout.builder()
				.id(workoutDto.getId())
				.title(workoutDto.getTitle())
				.type(workoutDto.getType().name())
				.userId(workoutDto.getUserId())
				.startTime(workoutDto.getStartTime())
				.endTime(workoutDto.getEndTime())
				.distance(workoutDto.getDistance())
				.duration(duration)
				.calories(workoutDto.getCalories())
				.createdAt(new java.util.Date())
				.build();

			if (workoutCollection.get(workoutDto.getId()) == null) {
				workoutCollection.put(workoutDto.getId(), workout);
				applicationEventPublisher.publishEvent(mapStructMapper.workoutToWorkoutCreated(workout));
			}
		}
		catch (GSException e) {
			log.error("Error create a workout", e);
		}
	}

}
