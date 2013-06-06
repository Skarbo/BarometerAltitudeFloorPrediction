package com.example.testbarometer.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;

public class PressureService extends Service implements SensorEventListener {

	protected static final String TAG = "PressureService";
	public static final String SERVICE_PRESSURE_ACTION = "com.example.testbarometer.service.PressureService";
	public static final String BROADCAST_PRESSURE_ACTION = "com.example.testbarometer.pressure";
	public static final String BROADCAST_EXTRA_PRESSURE = "pressure";
	private SensorManager sensorManager;
	private Intent intent;
	private final Handler handler = new Handler();
	private double pressure = 0.0;

	private Runnable sendPressureRunnable = new Runnable() {
		public void run() {
			intent.putExtra(BROADCAST_EXTRA_PRESSURE, pressure);
			sendBroadcast(intent);
			handler.postDelayed(this, 2000); // 2 seconds
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		intent = new Intent(BROADCAST_PRESSURE_ACTION);

		// Pressure sensor
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

		// Handler
		handler.removeCallbacks(sendPressureRunnable);
		handler.postDelayed(sendPressureRunnable, 1000); // 1 second
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(sendPressureRunnable);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		pressure = event.values[0];
	}

}
