package com.example.testbarometer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.testbarometer.helper.FloorPredictHelper;

import android.util.Log;

public class FloorPressures extends ArrayList<FloorPressure> {
	private static final long serialVersionUID = -2034478840924388596L;
	private static final String TAG = FloorPressures.class.getSimpleName();

	public static List<FloorPressures> generateFloorPressures(List<FloorPressure> pressures) {
		List<FloorPressures> floorPressuresList = new ArrayList<FloorPressures>();

		Collections.sort(pressures, new Comparator<FloorPressure>() {
			public int compare(FloorPressure lhs, FloorPressure rhs) {
				return Integer.valueOf(lhs.getFloor()).compareTo(Integer.valueOf(rhs.getFloor()));
			}
		});

		FloorPressures floorPressures = null;
		int index = -1;
		for (FloorPressure floorPressure : pressures) {
			index = containsFloorPressure(floorPressuresList, floorPressure);
			if (index > -1)
				floorPressures = floorPressuresList.get(index);
			else {
				floorPressures = new FloorPressures();
				floorPressuresList.add(floorPressures);
			}
			floorPressures.add(floorPressure);
		}

		return floorPressuresList;
	}

	public String getBuilding() {
		if (this.isEmpty())
			return "";
		return this.get(0).getBuilding();
	}

	public int getSet() {
		if (this.isEmpty())
			return 0;
		return this.get(0).getSet();
	}

	public double getPressureAverage() {
		double averagePressure = 0.0;
		int floorDiff = 0, count = 0;
		for (int i = 1; i < this.size(); i++) {
			floorDiff = FloorPredictHelper.getFloorSpan(this.get(i).getFloor(), this.get(i - 1).getFloor());
			if (floorDiff != 0.0) {
				averagePressure += FloorPredictHelper.getAveragePresure(this.get(i), this.get(i - 1));
				count++;
			}
		}
		return count != 0 ? averagePressure / count : 0.0;
	}

	public Date getDate() {
		if (this.isEmpty())
			return null;
		return this.get(0).getRegistered();
	}

	public static int containsFloorPressure(List<FloorPressures> floorPressuresList, FloorPressure floorPressure) {
		int i = 0;
		for (FloorPressures floorPressures : floorPressuresList) {
			if (!floorPressures.isEmpty() && floorPressures.getBuilding().equalsIgnoreCase(floorPressure.getBuilding())
					&& floorPressures.getSet() == floorPressure.getSet())
				return i;
			i++;
		}
		return -1;
	}

	public JSONObject getJSON() {
		JSONObject object = new JSONObject();
		try {
			object.put("set", getSet());
			object.put("pressureAverage", getPressureAverage());
			object.put("building", getBuilding());
			object.put("date", getDate() != null ? getDate().getTime() : "");
			JSONArray array = new JSONArray();
			for (FloorPressure floorPressure : this) {
				array.put(floorPressure.getJSON());
			}
			object.put("pressures", array);
		} catch (JSONException e) {
			Log.d(TAG, "GetJSON: " + e.getMessage());
		}
		return object;
	}

}
