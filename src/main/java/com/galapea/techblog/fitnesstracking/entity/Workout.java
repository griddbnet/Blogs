package com.galapea.techblog.fitnesstracking.entity;

import java.util.Date;

import com.toshiba.mwcloud.gs.RowKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workout {

	@RowKey
	public String id;

	public String title;

	public String type;

	public String userId;

	public Date startTime;

	public Date endTime;

	public Double distance;

	public Double duration;

	public Double calories;

	public Date createdAt;

}
