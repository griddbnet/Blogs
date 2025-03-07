package com.galapea.techblog.fitnesstracking;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class Helper {

	private static final double AVERAGE_PACE_PER_KM = 6.0; // minutes per km

	public static double calculateAverageDistanceInKm(LocalDateTime startTime, LocalDateTime endTime) {
		Duration duration = Duration.between(startTime, endTime);
		long durationInSeconds = duration.toSeconds();
		double durationInMinutes = durationInSeconds / 60.0;
		double distanceInKm = durationInMinutes / AVERAGE_PACE_PER_KM;
		return distanceInKm;
	}

	public static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static String getDurationText(Date startTime, Date endTime) {
		LocalDateTime start = Helper.convertToLocalDateTime(startTime);
		LocalDateTime end = Helper.convertToLocalDateTime(endTime);
		long millis = Duration.between(start, end).toMillis();
		String durationText = String.format("%dh %dm %ds", TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		return durationText;
	}

	public static String calculateTimeAgoByTimeGranularity(Date pastTime, TimeGranularity defaultGranularity) {
		long timeDifferenceInMillis = Instant.now().toEpochMilli() - pastTime.getTime();

		if (timeDifferenceInMillis > TimeGranularity.HOURS.toMillis() * 24) {
			return timeDifferenceInMillis / TimeGranularity.DAYS.toMillis() + " "
					+ TimeGranularity.DAYS.name().toLowerCase() + " ago";
		}
		if (timeDifferenceInMillis > TimeGranularity.HOURS.toMillis()) {
			return timeDifferenceInMillis / TimeGranularity.HOURS.toMillis() + " "
					+ TimeGranularity.HOURS.name().toLowerCase() + " ago";
		}
		return timeDifferenceInMillis / defaultGranularity.toMillis() + " " + defaultGranularity.name().toLowerCase()
				+ " ago";
	}

	public enum TimeGranularity {

		SECONDS {
			public long toMillis() {
				return TimeUnit.SECONDS.toMillis(1);
			}
		},
		MINUTES {
			public long toMillis() {
				return TimeUnit.MINUTES.toMillis(1);
			}
		},
		HOURS {
			public long toMillis() {
				return TimeUnit.HOURS.toMillis(1);
			}
		},
		DAYS {
			public long toMillis() {
				return TimeUnit.DAYS.toMillis(1);
			}
		},
		WEEKS {
			public long toMillis() {
				return TimeUnit.DAYS.toMillis(7);
			}
		},
		MONTHS {
			public long toMillis() {
				return TimeUnit.DAYS.toMillis(30);
			}
		},
		YEARS {
			public long toMillis() {
				return TimeUnit.DAYS.toMillis(365);
			}
		},
		DECADES {
			public long toMillis() {
				return TimeUnit.DAYS.toMillis(365 * 10);
			}
		};

		public abstract long toMillis();

	}

}
