package com.example.testbarometer.activity;

import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.example.testbarometer.R;
import com.example.testbarometer.datasource.PressureDataSource;
import com.example.testbarometer.fragment.AltitudeFragment;
import com.example.testbarometer.fragment.FloorFragment;
import com.example.testbarometer.fragment.FloorPressureDialogFragment;
import com.example.testbarometer.fragment.HasFragment;
import com.example.testbarometer.fragment.WeatherFragment;
import com.example.testbarometer.fragment.WeatherFragment.OnSealevelPressure;
import com.example.testbarometer.fragment.WeatherFragment.Progress;
import com.example.testbarometer.helper.DropboxHelper;
import com.example.testbarometer.model.FloorPressure;
import com.example.testbarometer.model.FloorPressures;
import com.example.testbarometer.model.WeatherEntries;
import com.example.testbarometer.service.PressureService;
import com.example.testbarometer.task.WeatherRetrieveTask;
import com.example.testbarometer.task.WeatherRetrieveTask.OnWeatherRetrieved;
import com.example.testbarometer.util.Util;

public class MainActivity extends FragmentActivity implements HasFragment, OnSealevelPressure {

	protected static final String TAG = "TestBarometer";
	public static final int POSITION_ALTITUDE = 0;
	public static final int POSITION_WEATHER = 1;
	public static final int POSITION_FLOOR = 2;
	public static final int POSITIONS = 3;
	public static final double SEALEVEL_PRESSURE_DEFAULT = 1013.25;

	private View actionBarRefreshingView;
	private ActionBar actionBar;

	private AltitudeFragment altitudeFragment;
	private WeatherFragment weatherFragment;
	private FloorFragment floorFragment;

	private MenuItem actionRefreshItem;
	private MenuItem actionNewItem;
	private SectionsPagerAdapter sectionsPagerAdapter;
	private ViewPager viewPager;
	private Intent pressureIntent;
	private LocationManager locationManager;
	private PressureDataSource pressureDataSource;
	private DropboxHelper dropboxHelper;

	private double sealevelPressure = SEALEVEL_PRESSURE_DEFAULT;
	private double currentPressure = 0.0;
	private double altitude = 0.0;
	private boolean isRetrevingLocation = false;
	private boolean isRetrevingWeather = false;

	// REICEVER/LISTENER

	private BroadcastReceiver pressureBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			currentPressure = intent.getDoubleExtra(PressureService.BROADCAST_EXTRA_PRESSURE, 0.0);
			altitude = Util.getCalculatedAltitude(currentPressure, sealevelPressure);

			if (altitudeFragment != null)
				altitudeFragment.onCurrentPressure(currentPressure);
			if (floorFragment != null)
				floorFragment.onCurrentPressure(currentPressure);
		}
	};

	private LocationListener locationListener = new LocationListener() {

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onLocationChanged(Location location) {
			if (weatherFragment != null)
				weatherFragment.onLocationRetrieved(location);
			isRetrevingLocation = false;
			locationManager.removeUpdates(this);
			doWeatherRetrieve(location, false);
		}
	};

	private OnWeatherRetrieved weatherListener = new OnWeatherRetrieved() {
		public void onWeatherRetrieved(WeatherEntries weatherEntries) {
			if (weatherFragment != null)
				weatherFragment.onWeatherRetrieved(weatherEntries);
			isRetrevingWeather = false;
			if (actionRefreshItem != null)
				actionRefreshItem.setEnabled(true);
			actionBarRefreshingView.setVisibility(View.GONE);
		}

		public void onWeatherProgress(Progress progress) {
			if (weatherFragment != null)
				weatherFragment.onWeatherProgress(progress);
		}
	};

	// /REICEVER/LISTENER

	// SET/GET

	public void setAltitudeFragment(AltitudeFragment fragment) {
		altitudeFragment = fragment;
	}

	public void setWeatherFragment(WeatherFragment fragment) {
		weatherFragment = fragment;
	}

	public void setFloorFragment(FloorFragment fragment) {
		floorFragment = fragment;
	}

	public double getSealevelPressure() {
		return sealevelPressure;
	}

	public double getCurrentPressure() {
		return currentPressure;
	}

	public double getAltitude() {
		return altitude;
	}

	public List<FloorPressures> getFloorPressuresList() {
		List<FloorPressure> pressures = pressureDataSource.getPressures();

		return FloorPressures.generateFloorPressures(pressures);
	}

	public DropboxHelper getDropboxHelper() {
		return this.dropboxHelper;
	}

	// /SET/GET

	// ON

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Action bar
		// getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		actionBarRefreshingView = getLayoutInflater().inflate(R.layout.action_bar_refreshing, null);
		actionBar = getActionBar();
		actionBar.setCustomView(actionBarRefreshingView, new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE);
		actionBarRefreshingView.setVisibility(View.GONE);

		// Location
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Fragment Pager
		sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(sectionsPagerAdapter);

		viewPager.setCurrentItem(POSITION_FLOOR);

		// Pressure intent
		pressureIntent = new Intent(PressureService.SERVICE_PRESSURE_ACTION);

		// Database
		pressureDataSource = new PressureDataSource(this);
		pressureDataSource.open();

		this.dropboxHelper = new DropboxHelper(this);

		// Location
		doLocationRetrieve();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// if
		// (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER))
		startService(pressureIntent);
		registerReceiver(pressureBroadcastReceiver, new IntentFilter(PressureService.BROADCAST_PRESSURE_ACTION));
		pressureDataSource.open();
		this.dropboxHelper.doLogin();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopService(pressureIntent);
		unregisterReceiver(pressureBroadcastReceiver);
		locationManager.removeUpdates(locationListener);
		pressureDataSource.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);

		// Create Add action bar button
		actionNewItem = menu.add("Add");
		actionNewItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		actionNewItem.setIcon(R.drawable.ic_action_new);

		// Create Refresh action bar button
		actionRefreshItem = menu.add("Refresh");
		actionRefreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		actionRefreshItem.setIcon(R.drawable.ic_action_refresh);

		// Create Settings action bar button
		MenuItem actionSettingsItem = menu.add("Settings");
		actionSettingsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		actionSettingsItem.setIcon(R.drawable.action_settings);

		actionNewItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				doFloorPressureAddDialog();
				return true;
			}
		});

		actionSettingsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				doSettings();
				return true;
			}
		});

		return true;
	}

	public void onSealevelPressure(double pressure) {
		viewPager.setCurrentItem(POSITION_ALTITUDE);
		sealevelPressure = pressure;
		altitude = Util.getCalculatedAltitude(currentPressure, sealevelPressure);
		if (altitudeFragment != null)
			altitudeFragment.onSealevelPressure(sealevelPressure);
	}

	// /ON

	// DO

	private void doLocationRetrieve() {
		if (isRetrevingLocation)
			return;
		Log.d(TAG, "Do location");
		isRetrevingLocation = true;
		if (actionRefreshItem != null)
			actionRefreshItem.setEnabled(false);
		actionBarRefreshingView.setVisibility(View.VISIBLE);

		LocationProvider low = locationManager.getProvider(locationManager.getBestProvider(createCoarseCriteria(),
				false));
		LocationProvider high = locationManager.getProvider(locationManager
				.getBestProvider(createFineCriteria(), false));

		locationManager.requestLocationUpdates(low.getName(), 0, 0f, locationListener);
		locationManager.requestLocationUpdates(high.getName(), 0, 0f, locationListener);
	}

	public void doWeatherRetrieve(Location location, boolean forceRetrieve) {
		if (isRetrevingWeather)
			return;
		isRetrevingWeather = true;
		WeatherRetrieveTask weatherRetrieveTask = new WeatherRetrieveTask(weatherListener, getCacheDir(), forceRetrieve);
		weatherRetrieveTask.execute(location);
	}

	// /DO

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case POSITION_ALTITUDE:
				return new AltitudeFragment();
			case POSITION_WEATHER:
				return new WeatherFragment();
			case POSITION_FLOOR:
				return new FloorFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return POSITIONS;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case POSITION_ALTITUDE:
				return getString(R.string.altitude_title).toUpperCase();
			case POSITION_WEATHER:
				return getString(R.string.weather_title).toUpperCase();
			case POSITION_FLOOR:
				return getString(R.string.floor_title).toUpperCase();
			}
			return null;
		}

	}

	protected void doFloorPressureAddDialog() {
		List<FloorPressure> pressures = pressureDataSource.getPressures();
		String building = "";
		int set = 0;
		int floor = 0;
		if (!pressures.isEmpty()) {
			set = pressures.get(0).getSet();
			floor = pressures.get(0).getFloor();
			building = pressures.get(0).getBuilding();
		}

		FragmentManager fm = getSupportFragmentManager();
		FloorPressureDialogFragment floorPressureDialogFragment = FloorPressureDialogFragment.newInstance(set, floor,
				building);
		floorPressureDialogFragment.show(fm, "fragment_floor_pressure");
	}

	private void doSettings() {
		startActivity(new Intent(this, PreferencesActivity.class));
	}

	public void doFloorPressureAdding(int set, String building, int floor) {
		FragmentManager fm = getSupportFragmentManager();
		AddingFloorPressureDialogFragment addingFloorPressureDialogFragment = AddingFloorPressureDialogFragment
				.newInstance(set, building, floor);
		addingFloorPressureDialogFragment.show(fm, "fragment_floor_pressure_adding");
	}

	public void doFloorPressureAdd(float pressure, float pressureLow, float pressureHigh, int set, String building,
			int floor) {
		if (building == null || building.equalsIgnoreCase(""))
			return;
		FloorPressure floorPressure = new FloorPressure();
		floorPressure.setSet(set);
		floorPressure.setBuilding(building);
		floorPressure.setPressure(pressure);
		floorPressure.setPressureLow(pressureLow);
		floorPressure.setPressureHigh(pressureHigh);
		floorPressure.setFloor(floor);
		floorPressure.setSealevel(getSealevelPressure());

		pressureDataSource.createPressure(floorPressure);

		if (floorFragment != null)
			floorFragment.onFloorPressureChanged(getFloorPressuresList());
	}

	public void doFloorPressureDelete(int id) {
		pressureDataSource.deletePressure(id);

		if (floorFragment != null)
			floorFragment.onFloorPressureChanged(getFloorPressuresList());
	}

	// CLASS

	// ... CRITERIA

	/** this criteria will settle for less accuracy, high power, and cost */
	public static Criteria createCoarseCriteria() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_COARSE);
		c.setAltitudeRequired(false);
		c.setBearingRequired(false);
		c.setSpeedRequired(false);
		c.setCostAllowed(true);
		c.setPowerRequirement(Criteria.POWER_HIGH);
		return c;
	}

	/** this criteria needs high accuracy, high power, and cost */
	public static Criteria createFineCriteria() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		c.setAltitudeRequired(false);
		c.setBearingRequired(false);
		c.setSpeedRequired(false);
		c.setCostAllowed(true);
		c.setPowerRequirement(Criteria.POWER_HIGH);
		return c;
	}

	// ... /CRITERIA

	// ... FRAGMENT

	public static class AddingFloorPressureDialogFragment extends DialogFragment implements SensorEventListener {

		private static final String ARG_SET = "set";
		private static final String ARG_BUILDING = "building";
		private static final String ARG_FLOOR = "floor";
		private static final int DELAY = 5000;

		private SensorManager sensorManager;
		private float pressureSum = 0;
		private int count = 0;
		private int set;
		private String building;
		private int floor;
		private float pressureLow = 0;
		private float pressureHigh = 0;

		public static AddingFloorPressureDialogFragment newInstance(int set, String building, int floor) {
			AddingFloorPressureDialogFragment addingFloorPressureDialogFragment = new AddingFloorPressureDialogFragment();

			Bundle args = new Bundle();
			args.putInt(ARG_SET, set);
			args.putString(ARG_BUILDING, building);
			args.putInt(ARG_FLOOR, floor);
			addingFloorPressureDialogFragment.setArguments(args);

			return addingFloorPressureDialogFragment;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
			set = getArguments().getInt(ARG_SET);
			building = getArguments().getString(ARG_BUILDING);
			floor = getArguments().getInt(ARG_FLOOR);

			// Pressure sensor
			sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
			Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
			sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

			new Handler().postDelayed(new Runnable() {
				public void run() {
					dismiss();
					try {
						float pressure = pressureSum / (float) count;
						((HasFragment) getActivity()).doFloorPressureAdd(pressure, pressureLow, pressureHigh, set,
								building, floor);
					} catch (ClassCastException e) {
						Log.e(TAG, getActivity().toString() + " must implement HasFragment");
					}
				}
			}, DELAY);
		}

		@Override
		public void onPause() {
			super.onPause();
			sensorManager.unregisterListener(this);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Adding Floor Pressure").setCancelable(false).setIcon(R.drawable.ic_action_refresh);
			return builder.create();
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		public void onSensorChanged(SensorEvent event) {
			float pressure = event.values[0];

			if (this.pressureSum == 0.0 || pressureLow > pressure)
				pressureLow = pressure;
			if (this.pressureSum == 0.0 || pressureHigh < pressure)
				pressureHigh = pressure;

			this.pressureSum += pressure;
			this.count++;
		}

	}

	// ... /FRAGMENT

	// /CLASS

}
