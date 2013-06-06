package com.example.testbarometer.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherEntry implements Serializable {
	private static final long serialVersionUID = 1148494715784804687L;
	public double temperature;
	public Date from;
	public Date to;
	public double pressure;

	public String toString() {
		return from + "," + to + "," + pressure;
	}

	public static Date parseDate(String date) {
		// 2012-11-09T20:00:00Z
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'");
		df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
		try {
			return df.parse(date);
		} catch (ParseException e) {
			return new Date();
		}
	}
}
