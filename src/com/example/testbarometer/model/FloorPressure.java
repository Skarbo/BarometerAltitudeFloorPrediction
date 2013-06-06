package com.example.testbarometer.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class FloorPressure {
	int id = 0;
	double pressure = 0.0;
	double pressureLow = 0.0;
	double pressureHigh = 0.0;
	double sealevel = 0.0;
	String building = "";
	int floor = 0;
	int set = 0;
	Date registered = null;

	public FloorPressure() {

	}

	public FloorPressure(double pressure, int floor) {
		super();
		this.pressure = pressure;
		this.floor = floor;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getPressure() {
		return pressure;
	}

	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;

	}

	public Date getRegistered() {
		return registered;
	}

	public void setRegistered(Date registered) {
		this.registered = registered;
	}

	public double getSealevel() {
		return sealevel;
	}

	public void setSealevel(double sealevel) {
		this.sealevel = sealevel;
	}

	public int getSet() {
		return set;
	}

	public void setSet(int set) {
		this.set = set;
	}

	public double getPressureHigh() {
		return pressureHigh;
	}

	public void setPressureHigh(double pressureHigh) {
		this.pressureHigh = pressureHigh;
	}

	public void setPressureLow(double pressureLow) {
		this.pressureLow = pressureLow;
	}

	public double getPressureLow() {
		return pressureLow;
	}

	@Override
	public String toString() {
		return set + "," + building + ", " + floor + ", " + pressure + "," + pressureLow + "," + pressureHigh + ","
				+ sealevel;
	}

	public JSONObject getJSON() {
		JSONObject object = new JSONObject();
		try {
			object.put("id", getId());
			object.put("set", getSet());
			object.put("pressure", getPressure());
			object.put("pressureLow", getPressureLow());
			object.put("pressureHigh", getPressureHigh());
			object.put("sealevel", getSealevel());
			object.put("floor", getFloor());
			object.put("building", getBuilding());
			object.put("time", getRegistered() != null ? getRegistered().getTime() : "");

		} catch (JSONException e) {
		}
		return object;
	}
}
