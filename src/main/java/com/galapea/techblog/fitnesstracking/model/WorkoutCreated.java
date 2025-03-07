package com.galapea.techblog.fitnesstracking.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class WorkoutCreated {

	private String id;

	private String title;

	private String type;

	private String userId;

	private Date startTime;

	private Date endTime;

	private Double distance;

	private Double duration;

	private Double calories;

	private Date createdAt;

}
