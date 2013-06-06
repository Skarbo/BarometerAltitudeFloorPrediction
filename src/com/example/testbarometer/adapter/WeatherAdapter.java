package com.example.testbarometer.adapter;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.testbarometer.R;
import com.example.testbarometer.model.WeatherEntries;
import com.example.testbarometer.model.WeatherEntry;

public class WeatherAdapter extends ArrayAdapter<WeatherEntry> {

	private static final String TAG = "WeatherAdapter";
	private WeatherEntries weatherEntries;

	public WeatherAdapter(Context context, int textViewResourceId, WeatherEntries weatherEntries) {
		super(context, textViewResourceId, weatherEntries);
		this.weatherEntries = weatherEntries;
	}

	public WeatherEntries getWeatherEntries() {
		return weatherEntries;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		if (view == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(R.layout.fragment_weather_list_row, null);
		}

		WeatherEntry weatherEntry = getItem(position);
		if (weatherEntry != null) {
			TextView hourFromTextView = (TextView) view.findViewById(R.id.weatherHourTextView);
			TextView dateFromTextView = (TextView) view.findViewById(R.id.weatherDateTextView);
			TextView pressureToTextView = (TextView) view.findViewById(R.id.weatherPressureTextView);
			TextView temperatureToTextView = (TextView) view.findViewById(R.id.weatherTemperatureTextView);

			SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM");
			hourFromTextView.setText(sdfHour.format(weatherEntry.from));
			dateFromTextView.setText(sdfDate.format(weatherEntry.from));
			pressureToTextView.setText(String.format("%.2f", weatherEntry.pressure));
			temperatureToTextView.setText(String.format("%.2f", weatherEntry.temperature));
		}
		return view;
	}

	public void addEntry(WeatherEntry weatherEntry) {
		weatherEntries.add(weatherEntry);
	}

}