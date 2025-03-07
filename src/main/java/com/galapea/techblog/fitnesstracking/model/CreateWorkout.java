package com.galapea.techblog.fitnesstracking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateWorkout {

	String title;

	String type;

	String distance;

	String calories;

	String startTime;

	String endTime;

}
