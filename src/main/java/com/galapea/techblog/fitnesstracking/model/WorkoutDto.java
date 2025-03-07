package com.galapea.techblog.fitnesstracking.model;

import java.util.Date;

import com.galapea.techblog.fitnesstracking.entity.User;
import com.galapea.techblog.fitnesstracking.entity.WorkoutType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkoutDto {

	private String id;

	private String title;

	private WorkoutType type;

	private String userId;

	private User user;

	private Date startTime;

	private Date endTime;

	private Double distance;

	private Double duration;

	private String durationText;

	private Double calories;

	private Date createdAt;

}
