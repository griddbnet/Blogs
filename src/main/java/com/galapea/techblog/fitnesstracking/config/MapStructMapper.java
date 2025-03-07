package com.galapea.techblog.fitnesstracking.config;

import org.mapstruct.Mapper;

import com.galapea.techblog.fitnesstracking.entity.Workout;
import com.galapea.techblog.fitnesstracking.model.WorkoutCreated;
import com.galapea.techblog.fitnesstracking.model.WorkoutDto;

@Mapper(componentModel = "spring")
public interface MapStructMapper {

	WorkoutCreated workoutToWorkoutCreated(Workout source);

	WorkoutDto workoutToWorkoutDto(Workout source);

}
