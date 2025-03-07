package com.galapea.techblog.fitnesstracking;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.galapea.techblog.fitnesstracking.entity.User;
import com.galapea.techblog.fitnesstracking.entity.WorkoutType;
import com.galapea.techblog.fitnesstracking.model.CreateUser;
import com.galapea.techblog.fitnesstracking.model.WorkoutDto;
import com.galapea.techblog.fitnesstracking.service.UserService;
import com.galapea.techblog.fitnesstracking.service.WorkoutService;

import com.toshiba.mwcloud.gs.GSException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppSeeder implements CommandLineRunner {

	private final Logger log = LoggerFactory.getLogger(AppSeeder.class);

	private final UserService userService;

	private final WorkoutService workoutService;

	@Override
	public void run(String... args) throws Exception {
		log.info("run....");
		seedUsers();
		Instant start = Instant.now();
		seedWorkouts();
		Instant end = Instant.now();
		log.info("Seed Workouts execution time: {} seconds", Duration.between(start, end).toSeconds());
	}

	private void seedUsers() throws GSException {
		log.info("seed users....");
		CreateUser user = CreateUser.builder().email("pearl.holden@dunebuh.cr").fullName("Pearl Holden").build();
		userService.create(user);
		user = CreateUser.builder().email("jack.romoli@zamum.bw").fullName("Jack Romoli").build();
		userService.create(user);
		user = CreateUser.builder().email("an.rhodes@wehovuce.gu").fullName("Jean Rhodes").build();
		userService.create(user);
		user = CreateUser.builder().email("garrett.stokes@fef.bg").fullName("Garret Stokes").build();
		userService.create(user);
	}

	private void seedWorkouts() {
		List<User> users = userService.fetchAll();
		WorkoutDto workoutDto = WorkoutDto.builder()
			.id("wk_0GP1MM7GVY8KG")
			.title("running in the morning")
			.type(WorkoutType.RUN)
			.userId(users.get(0).getId())
			.distance(5.0)
			.calories(125.0)
			.startTime(Date.from(LocalDateTime.now().minusMinutes(90).toInstant(ZoneOffset.UTC)))
			.endTime(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
			.build();
		workoutService.create(workoutDto);

		Collections.shuffle(users);
		workoutDto = WorkoutDto.builder()
			.id("wk_0GPYXMRFF8AW6")
			.title("walking under the sun")
			.type(WorkoutType.WALK)
			.userId(users.get(0).getId())
			.distance(15.0)
			.calories(350.0)
			.startTime(Date.from(LocalDateTime.now().minusMinutes(125).toInstant(ZoneOffset.UTC)))
			.endTime(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
			.build();
		workoutService.create(workoutDto);
	}

}
