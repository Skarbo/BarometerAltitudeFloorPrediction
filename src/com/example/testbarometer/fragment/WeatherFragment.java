package com.example.testbarometer.fragment;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.testbarometer.R;
import com.example.testbarometer.adapter.WeatherAdapter;
import com.example.testbarometer.listener.PositionListener;
import com.example.testbarometer.model.WeatherEntries;
import com.example.testbarometer.model.WeatherEntry;
import com.example.testbarometer.task.WeatherRetrieveTask.OnWeatherRetrieved;

public class WeatherFragment extends Fragment implements PositionListener, OnWeatherRetrieved {

	private static final String TAG = "WeatherFragment";
	private static final String BUNDLE_KEY_WEATHER = "weather";
	private static final String BUNDLE_KEY_LOCATION = "location";

	public enum Progress {
		IDLING, LOCATION, RETRIEVING, PARSING, CACHING
	};

	private TextView locationLatCurrentTextView;
	private TextView locationLongCurrentTextView;
	private TextView locationLongSourceTextView;
	private TextView locationLatSourceTextView;
	private TextView locationAltCurrentTextView;
	private TextView locationAltSourceTextView;
	private TextView locationDistanceSourceTextView;
	private TextView locationProviderSourceTextView;
	private ListView weatherListView;
	private TextView progressTextView;
	private ImageView mapImageView;

	private HasFragment fragmentContainer;
	private OnSealevelPressure pressureListener;
	private WeatherAdapter weatherAdapter;

	private Location locationLast;
	private boolean isStarted = false;
	private Progress progress = Progress.IDLING;

	// INTERFACE

	public static interface OnSealevelPressure {
		public void onSealevelPressure(double pressure);
	}

	// /INTERFACE

	// ON

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		isStarted = true;
		try {
			fragmentContainer = (HasFragment) getActivity();
			fragmentContainer.setWeatherFragment(this);
			pressureListener = (OnSealevelPressure) getActivity();
		} catch (ClassCastException e) {
			Log.e(TAG, getActivity().toString() + " must implement HasFragment & OnSealevelPressure");
		}
		WeatherEntries weatherEntries = new WeatherEntries();
		if (savedInstanceState != null) {
			weatherEntries = (WeatherEntries) savedInstanceState.getSerializable(BUNDLE_KEY_WEATHER);
			locationLast = (Location) savedInstanceState.getParcelable(BUNDLE_KEY_LOCATION);
		}
		weatherAdapter = new WeatherAdapter(getActivity(), R.layout.fragment_weather_list_row, weatherEntries);
		if (weatherListView != null)
			weatherListView.setAdapter(weatherAdapter);
		
		doUpdate();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_weather, container, false);

		locationLatCurrentTextView = (TextView) rootView.findViewById(R.id.locationLatCurrentTextView);
		locationLatSourceTextView = (TextView) rootView.findViewById(R.id.locationLatSourceTextView);
		locationLongCurrentTextView = (TextView) rootView.findViewById(R.id.locationLongCurrentTextView);
		locationLongSourceTextView = (TextView) rootView.findViewById(R.id.locationLongSourceTextView);
		locationAltCurrentTextView = (TextView) rootView.findViewById(R.id.locationAltCurrentTextView);
		locationAltSourceTextView = (TextView) rootView.findViewById(R.id.locationAltSourceTextView);
		locationDistanceSourceTextView = (TextView) rootView.findViewById(R.id.locationDistanceSourceTextView);
		locationProviderSourceTextView = (TextView) rootView.findViewById(R.id.locationProviderSourceTextView);
		progressTextView = (TextView) rootView.findViewById(R.id.progressTextView);
		weatherListView = (ListView) rootView.findViewById(R.id.weatherListView);
		mapImageView = (ImageView) rootView.findViewById(R.id.mapImageView);

		weatherListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				WeatherEntry entry = weatherAdapter.getItem(position);
				if (entry != null) {
					pressureListener.onSealevelPressure(entry.pressure);
				}
			}
		});

		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(BUNDLE_KEY_LOCATION, locationLast);
		if (weatherAdapter != null) {
			outState.putSerializable(BUNDLE_KEY_WEATHER, weatherAdapter.getWeatherEntries());
		}
	}

	public void onLocationRetrieving() {
		Log.d(TAG, "On location retreving");
	}

	public void onLocationRetrieved(Location location) {
		if (location != null) {
			locationLast = location;
			doUpdate();
		}
	}

	public void onWeatherProgress(Progress progress) {
		this.progress = progress;
		doUpdate();
	}

	public void onWeatherRetrieved(WeatherEntries weatherEntries) {
		weatherAdapter.clear();
		weatherAdapter.getWeatherEntries().altitude = weatherEntries.altitude;
		weatherAdapter.getWeatherEntries().longitude = weatherEntries.longitude;
		weatherAdapter.getWeatherEntries().latitude = weatherEntries.latitude;
		for (WeatherEntry weatherEntry : weatherEntries) {
			weatherAdapter.addEntry(weatherEntry);
		}
		weatherAdapter.notifyDataSetChanged();
		progress = Progress.IDLING;

		String uri = "http://maps.googleapis.com/maps/api/staticmap?size=%dx%d&maptype=roadmap&markers=size:mid|color:red|%s,%s&markers=size:mid|color:blue|%s,%s&sensor=false";
		String imageUrl = String.format(uri, mapImageView.getWidth(), mapImageView.getHeight(),
				String.format("%.5f", locationLast.getLatitude()).replace(",", "."),
				String.format("%.5f", locationLast.getLongitude()).replace(",", "."),
				String.format("%.5f", weatherEntries.latitude).replace(",", "."),
				String.format("%.5f", weatherEntries.longitude).replace(",", "."));
		Log.d(TAG, imageUrl);
		String test = "http://maps.googleapis.com/maps/api/staticmap?size=464x75&maptype=roadmap&markers=size:mid%7Ccolor:red%7C60.38429,5.33321%7C60.38430,5.33320&sensor=false";
		// Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new
		// URL(test).getContent());
		// mapImageView.setImageBitmap(bitmap);
		new DownloadImageTask(mapImageView).execute(imageUrl);

		doUpdate();
	}

	// /ON

	// DO

	private void doUpdate() {
		if (!isStarted)
			return;

		// Weather
		WeatherEntries weatherEntries = weatherAdapter.getWeatherEntries();
		Log.d(TAG, "Do update: " + weatherEntries.toString());
		if (!weatherEntries.isEmpty()) {
			locationLatSourceTextView.setText(String.format("%.5f", weatherEntries.latitude));
			locationLongSourceTextView.setText(String.format("%.5f", weatherEntries.longitude));
			locationAltSourceTextView.setText(String.format("%.2f", weatherEntries.altitude));
		} else {
			locationLatSourceTextView.setText(String.format("%.1f", 0.0));
			locationLongSourceTextView.setText(String.format("%.1f", 0.0));
			locationAltSourceTextView.setText(String.format("%.1f", 0.0));
		}

		// Location
		if (locationLast != null) {
			locationLatCurrentTextView.setText(String.format("%.5f", locationLast.getLatitude()));
			locationLongCurrentTextView.setText(String.format("%.5f", locationLast.getLongitude()));
			locationAltCurrentTextView.setText(String.format("%.2f",
					locationLast.hasAltitude() ? locationLast.getAltitude() : 0.0));
			locationProviderSourceTextView.setText(locationLast.getProvider());

			locationDistanceSourceTextView.setText(String.format(
					"%.2f",
					weatherEntries.latitude > 0 && weatherEntries.latitude > 0 ? distanceFrom(
							locationLast.getLatitude(), locationLast.getLongitude(), weatherEntries.latitude,
							weatherEntries.longitude) : 0.0));
		} else {
			locationLatCurrentTextView.setText(String.format("%.1f", 0.0));
			locationLongCurrentTextView.setText(String.format("%.1f", 0.0));
			locationAltCurrentTextView.setText(String.format("%.1f", 0.0));
			locationProviderSourceTextView.setText("");
			locationDistanceSourceTextView.setText(String.format("%.1f", 0.0));
		}

		// Progress
		switch (progress) {
		case RETRIEVING:
			progressTextView.setText("Retrieving");
			break;
		case PARSING:
			progressTextView.setText("Parsing");
			break;
		case CACHING:
			progressTextView.setText("Caching");
			break;
		default:
			progressTextView.setText("Idling");
			break;
		}
	}

	// /DO

	public static double distanceFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		int meterConversion = 1609;

		return dist * meterConversion;
	}

	// CLASS

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
		}
	}

	// /CLASS

}
