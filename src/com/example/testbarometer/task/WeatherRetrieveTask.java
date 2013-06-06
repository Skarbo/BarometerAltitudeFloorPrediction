package com.example.testbarometer.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.util.ByteArrayBuffer;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.example.testbarometer.fragment.WeatherFragment.Progress;
import com.example.testbarometer.model.WeatherEntries;
import com.example.testbarometer.parser.WeatherXmlParser;

public class WeatherRetrieveTask extends AsyncTask<Location, Progress, WeatherEntries> {

	private static final String CACHE_FILE = "weather.xml";
	private static final String WEATHER_URI = "http://api.met.no/weatherapi/locationforecast/1.8/?lat=%s;lon=%s";
	private static final long CACHE_LIMIT = 1000 * 60 * 60; // 1 hour
	private static final String TAG = "WeatherRetrieveTask";

	private OnWeatherRetrieved weatherListener;
	private boolean forceRetrieve;
	private File cacheDir;

	// INTERFACE

	public interface OnWeatherRetrieved {
		public void onWeatherRetrieved(WeatherEntries weatherEntries);

		public void onWeatherProgress(Progress progress);
	}

	// /INTERFACE

	public WeatherRetrieveTask(OnWeatherRetrieved weatherListener, File cacheDir) {
		this(weatherListener, cacheDir, false);
	}

	public WeatherRetrieveTask(OnWeatherRetrieved weatherListener, File cacheDir, boolean forceRetrieve) {
		this.weatherListener = weatherListener;
		this.cacheDir = cacheDir;
		this.forceRetrieve = forceRetrieve;
	}

	protected WeatherEntries doInBackground(Location... params) {
		WeatherEntries WeatherEntries = null;
		if (params.length >= 1) {
			Location location = params[0];
			WeatherXmlParser weatherXmlParser = new WeatherXmlParser();
			InputStream is;

			try {
				publishProgress(Progress.RETRIEVING);
				File weatherCacheFile = getWeatherCacheFile();
				Log.d(TAG,
						"Weather cache file: "
								+ weatherCacheFile.getAbsolutePath()
								+ ", Exists: "
								+ weatherCacheFile.exists()
								+ ", Modified: "
								+ (weatherCacheFile.exists() ? new SimpleDateFormat("yyyy-MM-dd HH:mm")
										.format(new Date(weatherCacheFile.lastModified())) : "") + ", Diff: "
								+ (System.currentTimeMillis() - weatherCacheFile.lastModified()) + ", Cache limit: "
								+ CACHE_LIMIT);
				if (forceRetrieve || !weatherCacheFile.exists()
						|| (System.currentTimeMillis() - weatherCacheFile.lastModified()) > CACHE_LIMIT) {
					Log.d(TAG, "Retrieve file");
					is = downloadFromUrl(weatherCacheFile, generateWeatherUrl(location));
				} else {
					Log.d(TAG, "Use cached file");
					is = new FileInputStream(weatherCacheFile);
				}
				publishProgress(Progress.PARSING);
				WeatherEntries = weatherXmlParser.parse(is);
			} catch (Exception e) {
				e.printStackTrace();
				cancel(true);
			}
		}
		return WeatherEntries;
	}

	@Override
	protected void onProgressUpdate(Progress... values) {
		if (values.length > 0) {
			Progress progress = values[0];
			weatherListener.onWeatherProgress(progress);
		}
	}

	@Override
	protected void onPostExecute(WeatherEntries result) {
		if (result == null)
			return;
		weatherListener.onWeatherRetrieved(result);
	}

	private String generateWeatherUrl(Location location) {
		return String.format(WEATHER_URI, String.format("%f", location.getLatitude()).replace(",", "."),
				String.format("%f", location.getLongitude()).replace(",", "."));
	}

	// private InputStream downloadUrl(String urlString) throws IOException {
	// URL url = new URL(urlString);
	// Log.d(TAG, "Download url: " + urlString);
	// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	// conn.setReadTimeout(10000 /* milliseconds */);
	// conn.setConnectTimeout(15000 /* milliseconds */);
	// conn.setRequestMethod("GET");
	// conn.setDoInput(true);
	// conn.connect();
	// InputStream stream = conn.getInputStream();
	// return stream;
	// }

	private File getWeatherCacheFile() {
		return new File(cacheDir, CACHE_FILE);
	}

	public InputStream downloadFromUrl(File file, String downloadUrl) {
		try {
			URL url = new URL(downloadUrl);

			URLConnection ucon = url.openConnection();
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			ByteArrayBuffer baf = new ByteArrayBuffer(5000);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.flush();
			fos.close();

			return new FileInputStream(file);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}

}