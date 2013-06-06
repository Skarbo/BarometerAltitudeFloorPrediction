package com.example.testbarometer.fragment;

import java.util.List;

import com.example.testbarometer.helper.DropboxHelper;
import com.example.testbarometer.model.FloorPressure;
import com.example.testbarometer.model.FloorPressures;

public interface HasFragment {

	public void setAltitudeFragment(AltitudeFragment fragment);

	public void setWeatherFragment(WeatherFragment fragment);

	public void setFloorFragment(FloorFragment fragment);

	public double getSealevelPressure();

	public double getCurrentPressure();

	public double getAltitude();

	public void doFloorPressureAdding(int set, String building, int floor);

	public void doFloorPressureAdd(float pressure, float pressureLow, float pressureHigh, int set, String building,
			int floor);

	public void doFloorPressureDelete(int id);

	public List<FloorPressures> getFloorPressuresList();

	public DropboxHelper getDropboxHelper();

}
