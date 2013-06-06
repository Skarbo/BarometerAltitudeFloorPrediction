package com.example.testbarometer.model;

import java.io.Serializable;
import java.util.ArrayList;


public class WeatherEntries extends ArrayList<WeatherEntry> implements Serializable {
	private static final long serialVersionUID = -4137238059242905097L;
	public double altitude = 0.0;
	public float latitude = 0.0f;
	public float longitude = 0.0f;

	public String toString() {
		return altitude + "," + latitude + "," + longitude + ",size: " + size();
	}
}