package com.galapea.techblog.fitnesstracking.controller;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.galapea.techblog.fitnesstracking.Helper;
import com.galapea.techblog.fitnesstracking.entity.User;
import com.galapea.techblog.fitnesstracking.entity.WorkoutType;
import com.galapea.techblog.fitnesstracking.model.CreateWorkout;
import com.galapea.techblog.fitnesstracking.model.WorkoutDto;
import com.galapea.techblog.fitnesstracking.service.KeyGenerator;
import com.galapea.techblog.fitnesstracking.service.UserService;
import com.galapea.techblog.fitnesstracking.service.WorkoutService;

import com.toshiba.mwcloud.gs.GSException;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/workouts")
@RequiredArgsConstructor
public class WorkoutController {

	private final WorkoutService workoutService;

	private final UserService userService;

	java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

	@GetMapping
	String workouts(Model model) {
		List<WorkoutDto> workouts = workoutService.fetchAll();
		workouts.forEach(workout -> {
			try {
				workout.setUser(userService.fetchOneById(workout.getUserId()));
			}
			catch (GSException e) {
				e.printStackTrace();
			}
			String durationText = Helper.getDurationText(workout.getStartTime(), workout.getEndTime());
			workout.setDurationText(durationText);
		});
		model.addAttribute("workouts", workouts);
		model.addAttribute("createWorkout", new CreateWorkout());
		return "workouts";
	}

	@PostMapping("/save")
	String saveWorkout(@RequestParam("title") String title, @RequestParam("type") String type,
			@RequestParam("calories") String calories, @RequestParam("startTime") String startTime,
			@RequestParam("endTime") String endTime, @RequestParam("distance") String distance,
			final BindingResult bindingResult, RedirectAttributes attributes) throws ParseException {
		List<User> users = userService.fetchAll();
		java.util.Collections.shuffle(users);
		java.util.Date startDate = formatter.parse(startTime);
		java.util.Date endDate = formatter.parse(endTime);
		LocalDateTime start = Helper.convertToLocalDateTime(startDate);
		LocalDateTime end = Helper.convertToLocalDateTime(endDate);
		if (end.isBefore(start)) {
			// throw new IllegalArgumentException("End-date must be after Start-date!");
			// attributes.addAttribute("hasErrors", true);
			bindingResult.rejectValue("endTime", "error.endTime", "End-date must be after Start-date!");
			return "workouts";
		}

		WorkoutDto workoutDto = WorkoutDto.builder()
			.title(title)
			.type(WorkoutType.valueOf(type))
			.userId(users.get(0).getId())
			.distance(Double.parseDouble(distance))
			.startTime(startDate)
			.endTime(endDate)
			.calories(Double.parseDouble(calories))
			.build();
		workoutService.create(workoutDto);
		attributes.addFlashAttribute("message", "Workout recorded!");
		return "redirect:/workouts";
	}

	@PostMapping("/record")
	String recordWorkout(@ModelAttribute("createWorkout") CreateWorkout createWorkout,
			final BindingResult bindingResult, RedirectAttributes attributes, Model model) throws ParseException {
		List<User> users = userService.fetchAll();
		java.util.Collections.shuffle(users);
		java.util.Date startDate = formatter.parse(createWorkout.getStartTime());
		java.util.Date endDate = formatter.parse(createWorkout.getEndTime());
		LocalDateTime start = Helper.convertToLocalDateTime(startDate);
		LocalDateTime end = Helper.convertToLocalDateTime(endDate);
		if (end.isBefore(start)) {
			bindingResult.rejectValue("endTime", "error.endTime", "End-date must be after Start-date!");
			attributes.addAttribute("hasErrors", true);
		}

		if (bindingResult.hasErrors()) {
			return workouts(model);
		}

		WorkoutDto workoutDto = WorkoutDto.builder()
			.title(createWorkout.getTitle())
			.type(WorkoutType.valueOf(createWorkout.getType()))
			.userId(users.get(0).getId())
			.distance(Double.parseDouble(createWorkout.getDistance()))
			.startTime(startDate)
			.endTime(endDate)
			.calories(Double.parseDouble(createWorkout.getCalories()))
			.build();
		workoutService.create(workoutDto);
		attributes.addFlashAttribute("message", "Workout recorded!");
		return "redirect:/workouts";
	}

	@PostMapping("/generate-dummy")
	String generateDummyWorkout(RedirectAttributes attributes) {
		List<User> users = userService.fetchAll();
		List<WorkoutType> types = Arrays.asList(WorkoutType.values());
		Faker faker = new Faker();
		for (int i = 0; i < 2; i++) {
			LocalDateTime startDateTime = LocalDateTime.now().minusMinutes(faker.number().randomNumber(2, true));
			LocalDateTime endDateTime = LocalDateTime.now().minusMinutes(faker.number().numberBetween(1, 3));
			if (endDateTime.isBefore(startDateTime)) {
				continue;
			}

			Collections.shuffle(users);
			Collections.shuffle(types);
			WorkoutDto workoutDto = WorkoutDto.builder()
				.id(KeyGenerator.next("wk_"))
				.title(faker.weather().description() + " - " + faker.address().streetAddress() + ", "
						+ faker.address().cityName())
				.type(types.get(0))
				.userId(users.get(0).getId())
				.distance(Helper.calculateAverageDistanceInKm(startDateTime, endDateTime))
				.calories(faker.number().randomDouble(0, 50, 500))
				.startTime(Date.from(startDateTime.toInstant(ZoneOffset.UTC)))
				.endTime(Date.from(endDateTime.toInstant(ZoneOffset.UTC)))
				.build();
			workoutService.create(workoutDto);
		}
		attributes.addFlashAttribute("message", "Generated dummy workout!");
		return "redirect:/workouts";
	}

}
