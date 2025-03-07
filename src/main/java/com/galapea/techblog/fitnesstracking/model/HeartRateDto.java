package com.galapea.techblog.fitnesstracking.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HeartRateDto {

	private String timestampLabel;

	private double value;

}
