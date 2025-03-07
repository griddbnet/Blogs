package com.galapea.techblog;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.galapea.techblog.fitnesstracking.entity.WorkoutType;
import com.galapea.techblog.fitnesstracking.model.WorkoutDto;
import com.galapea.techblog.fitnesstracking.service.KeyGenerator;

import com.github.javafaker.Faker;

public class Others {

	public static void main(String[] args) {
		System.out.println("Hello World!");
		String key = KeyGenerator.next("wk_");
		System.out.println("--------- key = " + key);

		List<WorkoutType> types = Arrays.asList(WorkoutType.values());
		Faker faker = new Faker();
		for (int i = 0; i < 50; i++) {
			Collections.shuffle(types);
			WorkoutDto workoutDto = WorkoutDto.builder()
				.id(KeyGenerator.next("wk_"))
				.title(faker.weather().description() + " - " + faker.address().streetAddress() + ", "
						+ faker.address().cityName())
				.type(types.get(0))
				.userId("usr_0GP1MM7GVY8KG")
				.distance(faker.number().randomDouble(1, 1, 30))
				.calories(faker.number().randomDouble(0, 50, 500))
				.startTime(Date.from(LocalDateTime.now()
					.minusMinutes(faker.number().randomNumber(2, false))
					.toInstant(ZoneOffset.UTC)))
				.endTime(Date.from(LocalDateTime.now()
					.minusMinutes(faker.number().randomNumber(1, false))
					.toInstant(ZoneOffset.UTC)))
				.build();

			System.out.println(workoutDto);
			// System.out.println(faker.date().past(i, null, null));
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		}
	}

}
