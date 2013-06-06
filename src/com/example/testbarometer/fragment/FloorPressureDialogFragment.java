package com.example.testbarometer.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.example.testbarometer.R;

public class FloorPressureDialogFragment extends DialogFragment {

	protected static final String TAG = FloorPressureDialogFragment.class.getSimpleName();
	private static final String ARG_SET = "set";
	private static final String ARG_BUILDING = "building";
	private static final String ARG_FLOOR = "floor";

	private NumberPicker floorNumberPicker;
	private NumberPicker setNumberPicker;
	private EditText buildingEditText;

	private HasFragment fragmentContainer;
	private int floor = 0;
	private String building = "";
	private int set = 0;

	public static FloorPressureDialogFragment newInstance(int set, int floor, String building) {
		FloorPressureDialogFragment floorPressureDialogFragment = new FloorPressureDialogFragment();

		Bundle args = new Bundle();
		args.putInt(ARG_SET, set);
		args.putInt(ARG_FLOOR, floor);
		args.putString(ARG_BUILDING, building);
		floorPressureDialogFragment.setArguments(args);

		return floorPressureDialogFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			fragmentContainer = (HasFragment) getActivity();
		} catch (ClassCastException e) {
			Log.e(TAG, getActivity().toString() + " must implement HasFragment");
		}

		set = getArguments().getInt(ARG_SET, 0);
		floor = getArguments().getInt(ARG_FLOOR, 0);
		building = getArguments().getString(ARG_BUILDING, "");

		if (savedInstanceState != null) {
			set = savedInstanceState.getInt(ARG_SET, set);
			building = savedInstanceState.getString(ARG_BUILDING, building);
			floor = savedInstanceState.getInt(ARG_FLOOR, floor);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		Log.d(TAG, "OnSaveInstanceState");
		bundle.putInt(ARG_SET, set);
		bundle.putString(ARG_BUILDING, building);
		bundle.putInt(ARG_FLOOR, floor);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		View view = createView(getActivity().getLayoutInflater());
		builder.setView(view);

		builder.setTitle("Add floor pressure");
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Log.d(TAG, "Add floor: " + set + ", " + building + ", " + floor);
				fragmentContainer.doFloorPressureAdding(set, building.trim(), floor);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		return builder.create();
	}

	public View createView(LayoutInflater inflater) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_floor_pressure_dialog, null);

		setNumberPicker = (NumberPicker) view.findViewById(R.id.setNumberPicker);
		buildingEditText = (EditText) view.findViewById(R.id.floorBuildingEditText);
		floorNumberPicker = (NumberPicker) view.findViewById(R.id.floorNumberPicker);

		setNumberPicker.setMinValue(0);
		setNumberPicker.setMaxValue(100);
		floorNumberPicker.setMinValue(0);
		floorNumberPicker.setMaxValue(100);

		setNumberPicker.setValue(set);
		buildingEditText.setText(building);
		floorNumberPicker.setValue(floor);

		setNumberPicker.setOnValueChangedListener(new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				set = newVal;
			}
		});
		floorNumberPicker.setOnValueChangedListener(new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				floor = newVal;
			}
		});
		buildingEditText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
				building = s.toString().trim();
			}
		});

		return view;
	}
}
