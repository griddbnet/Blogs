package com.galapea.techblog.fitnesstracking.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.galapea.techblog.fitnesstracking.entity.Workout;
import com.galapea.techblog.fitnesstracking.entity.WorkoutType;
import com.galapea.techblog.fitnesstracking.model.WorkoutDto;

import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class WorkoutServiceTest {

	@InjectMocks
	private WorkoutService workoutService;

	@Mock
	private Collection<String, Workout> workoutCollection;

	@Mock
	private Query<Workout> query;

	@Mock
	private RowSet<Workout> rowSet;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testfetchAll() throws GSException {
		when(workoutCollection.query(anyString())).thenReturn(query);
		when(query.fetch()).thenReturn(rowSet);
		when(rowSet.hasNext()).thenReturn(true, false);

		String id1 = "wk_0GP1MM7GVY8KG";
		Workout newWorkout = Workout.builder()
			.id(id1)
			.title("running in the morning")
			.type(WorkoutType.BIKING.name())
			.userId("randomId")
			.distance(5.0)
			.startTime(Date.from(LocalDateTime.now().minusMinutes(90).toInstant(ZoneOffset.UTC)))
			.endTime(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
			.build();

		when(rowSet.next()).thenReturn(newWorkout);

		List<WorkoutDto> result = workoutService.fetchAll();

		assertEquals(1, result.size());
		assertEquals(WorkoutType.BIKING, result.get(0).getType());
		assertEquals(id1, result.get(0).getId());
	}

}
