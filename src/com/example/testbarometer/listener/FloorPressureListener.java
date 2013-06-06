package com.example.testbarometer.listener;

import java.util.List;

import com.example.testbarometer.model.FloorPressures;

public interface FloorPressureListener {

	public void onFloorPressureChanged(List<FloorPressures> pressures);

}
