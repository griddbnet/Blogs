package com.galapea.techblog.fitnesstracking.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.galapea.techblog.fitnesstracking.entity.User;
import com.galapea.techblog.fitnesstracking.model.CreateWorkout;
import com.galapea.techblog.fitnesstracking.model.WorkoutDto;
import com.galapea.techblog.fitnesstracking.service.UserService;
import com.galapea.techblog.fitnesstracking.service.WorkoutService;

import com.toshiba.mwcloud.gs.GSException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class WorkoutControllerTest {

	@Mock
	private WorkoutService workoutService;

	@Mock
	private UserService userService;

	@Mock
	private Model model;

	@Mock
	private BindingResult bindingResult;

	@Mock
	private RedirectAttributes attributes;

	@InjectMocks
	private WorkoutController workoutController;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testWorkouts() throws ParseException, GSException {
		// Mock the service layer
		List<WorkoutDto> workouts = List.of(
				WorkoutDto.builder().id("id1").startTime(new Date()).endTime(new Date()).build(),
				WorkoutDto.builder().id("id2").startTime(new Date()).endTime(new Date()).build());
		when(workoutService.fetchAll()).thenReturn(workouts);
		when(userService.fetchOneById(anyString())).thenReturn(new User());

		// Call the method under test
		String result = workoutController.workouts(model);

		// Verify the results
		verify(model, times(1)).addAttribute("workouts", workouts);
		verify(model, times(1)).addAttribute("users", userService.fetchAll());
		verify(model, times(1)).addAttribute(eq("createWorkout"), any(CreateWorkout.class));
		assertEquals("workouts", result);
	}

	@Test
	void testRecordWorkout() throws ParseException {
		// Mock the services
		List<User> users = new ArrayList<>();
		users.add(new User());
		when(userService.fetchAll()).thenReturn(users);

		// Mock the request
		CreateWorkout createWorkout = new CreateWorkout();
		createWorkout.setTitle("Test Workout");
		createWorkout.setType("RUN");
		createWorkout.setStartTime("2022-01-01T10:00:00");
		createWorkout.setEndTime("2022-01-01T11:00:00");
		createWorkout.setDistance("10.0");
		createWorkout.setCalories("500.0");

		// Call the method under test
		String result = workoutController.recordWorkout(createWorkout, bindingResult, attributes, model);

		// Verify the results
		verify(workoutService, times(1)).create(any(WorkoutDto.class));
		verify(attributes, times(1)).addFlashAttribute(eq("message"), eq("Workout recorded!"));
		assertEquals("redirect:/workouts", result);
	}

}
