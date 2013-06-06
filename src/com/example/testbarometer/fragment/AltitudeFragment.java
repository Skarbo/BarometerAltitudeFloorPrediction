package com.example.testbarometer.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.testbarometer.R;
import com.example.testbarometer.activity.MainActivity;
import com.example.testbarometer.listener.AltitudeListener;
import com.example.testbarometer.listener.PressureListener;

public class AltitudeFragment extends Fragment implements PressureListener, AltitudeListener {

	private static final String TAG = "AltitudeFragment";

	private TextView pressureTextView;
	private EditText sealevelEditText;
	private TextView altitudeTextView;

	private HasFragment fragmentContainer;

	private double currentPressure = 0.0;
	private double sealevelPressure = MainActivity.SEALEVEL_PRESSURE_DEFAULT;
	private double altitude = 0.0;
	private boolean isStarted = false;

	// ON

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		isStarted = true;
		try {
			fragmentContainer = (HasFragment) getActivity();
			fragmentContainer.setAltitudeFragment(this);

			altitude = fragmentContainer.getAltitude();
			currentPressure = fragmentContainer.getCurrentPressure();
			sealevelPressure = fragmentContainer.getSealevelPressure();
		} catch (ClassCastException e) {
			Log.e(TAG, getActivity().toString() + " must implement HasFragment");
		}

		if (sealevelEditText != null)
			sealevelEditText.setText(String.valueOf(sealevelPressure));

		doUpdate();
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_altitude, container, false);
		pressureTextView = (TextView) rootView.findViewById(R.id.pressureTextView);
		sealevelEditText = (EditText) rootView.findViewById(R.id.sealevelEditText);
		altitudeTextView = (TextView) rootView.findViewById(R.id.altitudeTextView);

		sealevelEditText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				try {
					sealevelPressure = Double.parseDouble(sealevelEditText.getText().toString());
				} catch (NumberFormatException e) {
					sealevelPressure = MainActivity.SEALEVEL_PRESSURE_DEFAULT;
				}
				doUpdate();
				return false;
			}
		});
		return rootView;
	}

	public void onCurrentPressure(double currentPressure) {
		this.currentPressure = currentPressure;
		this.altitude = fragmentContainer.getAltitude();
		doUpdate();
	}

	public void onSealevelPressure(double sealevelPressure) {
		this.sealevelPressure = sealevelPressure;
		this.altitude = fragmentContainer.getAltitude();
		if (sealevelEditText != null)
			sealevelEditText.setText(String.valueOf(sealevelPressure));
		doUpdate();
	}

	public void onAltitude(double altitude) {
		this.altitude = altitude;
		doUpdate();
	}

	// /ON

	// DO

	private void doUpdate() {
		if (!isStarted)
			return;

		altitudeTextView.setText(String.format("%.2f", altitude));
		pressureTextView.setText(String.format("%.2f", currentPressure));
	}

	// /DO

}
