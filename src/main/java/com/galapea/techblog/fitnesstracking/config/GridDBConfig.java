package com.galapea.techblog.fitnesstracking.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.galapea.techblog.fitnesstracking.AppConstant;
import com.galapea.techblog.fitnesstracking.entity.HeartRate;
import com.galapea.techblog.fitnesstracking.entity.User;
import com.galapea.techblog.fitnesstracking.entity.Workout;

import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;

@Configuration
public class GridDBConfig {

	@Value("${GRIDDB_NOTIFICATION_MEMBER}")
	private String notificationMember;

	@Value("${GRIDDB_CLUSTER_NAME}")
	private String clusterName;

	@Value("${GRIDDB_USER}")
	private String user;

	@Value("${GRIDDB_PASSWORD}")
	private String password;

	@Bean
	public GridStore gridStore() throws GSException {
		Properties properties = new Properties();
		properties.setProperty("notificationMember", notificationMember);
		properties.setProperty("clusterName", clusterName);
		properties.setProperty("user", user);
		properties.setProperty("password", password);
		GridStore store = GridStoreFactory.getInstance().getGridStore(properties);

		/**
		 * If you try to save an object that is different from the one used to create the
		 * collection, the following error will occur:
		 * com.toshiba.mwcloud.gs.common.GSStatementException:
		 * [60016:DS_DS_CHANGE_SCHEMA_DISABLE] Just delete the collection and redefine it
		 * and it should be ok.
		 **/
		// store.dropCollection(AppConstant.USERS_CONTAINER);
		// store.dropTimeSeries(AppConstant.HEARTRATE_CONTAINER);
		return store;
	}

	@Bean
	public Collection<String, User> userCollection(GridStore gridStore) throws GSException {
		Collection<String, User> collection = gridStore.putCollection(AppConstant.USERS_CONTAINER, User.class);
		collection.createIndex("email");
		return collection;
	}

	@Bean
	public Collection<String, HeartRate> heartRateCollection(GridStore gridStore) throws GSException {
		Collection<String, HeartRate> heartRateCollection = gridStore.putCollection(AppConstant.HEARTRATE_CONTAINER,
				HeartRate.class);
		heartRateCollection.createIndex("workoutId");
		return heartRateCollection;
	}

	@Bean
	public Collection<String, Workout> workoutCollection(GridStore gridStore) throws GSException {
		Collection<String, Workout> workoutCollection = gridStore.putCollection(AppConstant.WORKOUT_CONTAINER,
				Workout.class);
		workoutCollection.createIndex("userId");
		workoutCollection.createIndex("title");
		workoutCollection.createIndex("type");
		return workoutCollection;
	}

}
