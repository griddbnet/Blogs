package com.galapea.techblog.fitnesstracking.entity;

import java.util.Date;

import com.toshiba.mwcloud.gs.RowKey;

import lombok.Data;

@Data
public class HeartRate {

	@RowKey
	String id;

	Date timestamp;

	double value;

	String workoutId;

}
