package com.galapea.techblog.fitnesstracking.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.galapea.techblog.fitnesstracking.model.HeartRateDashboard;
import com.galapea.techblog.fitnesstracking.service.HeartRateService;
import com.galapea.techblog.fitnesstracking.service.WorkoutService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/heartrate")
@RequiredArgsConstructor
public class HeartRateController {

	private static final Logger log = LoggerFactory.getLogger(HeartRateController.class);

	private final HeartRateService heartRateService;

	private final WorkoutService workoutService;

	@GetMapping("/{id}")
	String heartRate(Model model, @PathVariable("id") String id) {
		log.info("Fetch heart rate of workout: {}", id);
		HeartRateDashboard heartRateDashboard = heartRateService.fetchForDashboardByWorkoutId(id);
		log.info("heart rate size: {}", heartRateDashboard.heartRates().size());
		model.addAttribute("heartRates", heartRateDashboard.heartRates());
		model.addAttribute("workout", workoutService.fetchById(id));
		model.addAttribute("heartRateZoneSummaries", heartRateDashboard.heartRateZoneSummaries());
		return "heartrate";
	}

}
