package com.example.testbarometer.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.example.testbarometer.model.FloorPressures;

public class Util {

	private static final String TAG = Util.class.getSimpleName();

	public static double getCalculatedAltitude(double currentPressure, double sealevelPressure) {
		if (currentPressure == 0.0 || sealevelPressure == 0.0) {
			return 0.0;
		}

		double heightFeet = ((Math.pow(10, Math.log10(currentPressure / sealevelPressure) / 5.2558797) - 1) / (-6.8755856 * 0.000001));
		return heightFeet * 0.3048f;
	}

	public static JSONObject createPressuresJson(List<FloorPressures> floorPressuresList) {		
		JSONObject json = new JSONObject();
		for (FloorPressures floorPressures : floorPressuresList) {
			try {
				json.putOpt(String.valueOf(floorPressures.getSet()), floorPressures.getJSON());
			} catch (JSONException e) {
				Log.e(TAG, "CreatePressuresJson: " + e.getMessage());
			}
		}		
		return json;
	}

}
