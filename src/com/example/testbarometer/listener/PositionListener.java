package com.example.testbarometer.listener;

import android.location.Location;

public interface PositionListener {

	public void onLocationRetrieving();
	
	public void onLocationRetrieved(Location location);

}
