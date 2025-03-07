package com.galapea.techblog;

import java.util.Date;
import java.util.Properties;

import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.TimeSeries;
import com.toshiba.mwcloud.gs.TimeUnit;
import com.toshiba.mwcloud.gs.TimestampUtils;

// Storage and extraction of a specific range of time-series data
public class TestGriddb {

	static class Point {

		@RowKey
		Date timestamp;

		boolean active;

		double voltage;

	}

	public static void main(String[] args) throws GSException {

		// Acquiring a GridStore instance
		Properties props = new Properties();
		props.setProperty("notificationMember", "127.0.0.1:10001");
		props.setProperty("clusterName", "myCluster");
		props.setProperty("user", "admin");
		props.setProperty("password", "admin");
		GridStore store = GridStoreFactory.getInstance().getGridStore(props);
		System.out.println("Successfully connected to GridDB");

		// Creating a TimeSeries (Only obtain the specified TimeSeries if it already
		// exists)
		TimeSeries<Point> ts = store.putTimeSeries("point01", Point.class);

		// Preparing time-series element data
		Point point = new Point();
		point.active = false;
		point.voltage = 100;

		System.out.println("Store the time-series element (GridStore sets its timestamp)");
		ts.append(point);

		System.out.println("Extract the specified range of time-series elements: last six hours");
		Date now = TimestampUtils.current();
		Date before = TimestampUtils.add(now, -6, TimeUnit.HOUR);

		RowSet<Point> rs = ts.query(before, now).fetch();

		while (rs.hasNext()) {
			point = rs.next();

			System.out.println("Time=" + TimestampUtils.format(point.timestamp) + " Active=" + point.active
					+ " Voltage=" + point.voltage);
		}

		// Releasing resource
		store.close();
	}

}
