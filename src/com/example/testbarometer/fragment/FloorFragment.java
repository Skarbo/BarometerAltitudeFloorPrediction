package com.example.testbarometer.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

import com.example.testbarometer.R;
import com.example.testbarometer.activity.FloorPressureChartActivity;
import com.example.testbarometer.helper.FloorPredictHelper;
import com.example.testbarometer.helper.FloorPredictHelper.FloorPredictContainer;
import com.example.testbarometer.listener.FloorPressureListener;
import com.example.testbarometer.listener.PressureListener;
import com.example.testbarometer.model.FloorPressure;
import com.example.testbarometer.model.FloorPressures;
import com.example.testbarometer.util.Util;

public class FloorFragment extends Fragment implements PressureListener, FloorPressureListener {

	private static final String TAG = "FoorFragment";
	private static final String BUNDLE_KEY_FLOOR = "floor";
	private static final String BUNDLE_KEY_FLOOR_PREDICT_PREV = "floor_predict_prev";
	private static final String BUNDLE_KEY_FLOOR_PREDICT_CURRENT = "floor_predict_current";
	private static final String BUNDLE_KEY_FLOOR_PREDICT_NEXT = "floor_predict_next";
	private static final String BUNDLE_KEY_FLOOR_HEIGHT = "floor_height";
	private static final String BUNDLE_KEY_FLOOR_SETSELECTED = "set_selected";

	protected HasFragment fragmentContainer;

	private TextView pressureTextView;
	private TextView floorPredictTextView;
	private TextView floorPredictPrevTextView;
	private TextView floorPredictCurrentTextView;
	private TextView floorPredictNextTextView;
	private EditText floorHeightEditText;
	private ExpandableListView floorPressuresListView;

	private FloorPressureAdapter floorPressuresAdapter;
	private FloorPredictHelper floorPredictHelper;
	private double pressure = 0.0;
	private int floorPredict = 0;
	private double floorPredictPrev = 0.0;
	private double floorPredictCurrent = 0.0;
	private double floorPredictNext = 0.0;
	private double floorHeight = 0.0;
	private boolean isStarted = false;
	private int setSelected = -1;

	// GET/SET

	private static int getCalculatedFloorPredict(double pressure, double floorPressure, double floorHeight,
			int floorNumber) {
		double floorAltDiff = pressure - floorPressure;
		int floor = (int) Math.floor(floorAltDiff / floorHeight) + floorNumber;
		return floor;
	}

	// /GET/SET

	// ON

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		isStarted = true;
		List<FloorPressures> floorPressures = new ArrayList<FloorPressures>();
		try {
			fragmentContainer = (HasFragment) getActivity();
			fragmentContainer.setFloorFragment(this);

			pressure = fragmentContainer.getCurrentPressure();
			floorPressures = fragmentContainer.getFloorPressuresList();
		} catch (ClassCastException e) {
			Log.e(TAG, getActivity().toString() + " must implement HasFragment");
		}

		if (savedInstanceState != null) {
			floorPredict = savedInstanceState.getInt(BUNDLE_KEY_FLOOR, 0);
			floorPredictPrev = savedInstanceState.getDouble(BUNDLE_KEY_FLOOR_PREDICT_PREV, 0.0);
			floorPredictCurrent = savedInstanceState.getDouble(BUNDLE_KEY_FLOOR_PREDICT_CURRENT, 0.0);
			floorPredictNext = savedInstanceState.getDouble(BUNDLE_KEY_FLOOR_PREDICT_NEXT, 0.0);
			floorHeight = savedInstanceState.getDouble(BUNDLE_KEY_FLOOR_HEIGHT, 0.0);
			setSelected = savedInstanceState.getInt(BUNDLE_KEY_FLOOR_SETSELECTED, -1);
		}

		// if (floorHeightEditText != null)
		// floorHeightEditText.setText(String.valueOf(floorHeight));

		floorPressuresAdapter = new FloorPressureAdapter(getActivity(), floorPressures);
		if (floorPressuresListView != null)
			floorPressuresListView.setAdapter(floorPressuresAdapter);

		this.floorPredictHelper = new FloorPredictHelper();
		if (setSelected != -1) {
			FloorPressures floorPressuresForSet = this.floorPressuresAdapter.getFloorPressuresForSet(this.setSelected);
			if (floorPressuresForSet != null)
				this.floorPredictHelper.setFloorPressures(floorPressuresForSet);
		}

		setRetainInstance(true);
		doUpdate();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_floor, container, false);

		floorPredictTextView = (TextView) rootView.findViewById(R.id.floorTextView);
		pressureTextView = (TextView) rootView.findViewById(R.id.pressureTextView);

		floorPredictPrevTextView = (TextView) rootView.findViewById(R.id.floorPredictPrevTextView);
		floorPredictCurrentTextView = (TextView) rootView.findViewById(R.id.floorPredictCurrentTextView);
		floorPredictNextTextView = (TextView) rootView.findViewById(R.id.floorPredictNextTextView);

		floorHeightEditText = (EditText) rootView.findViewById(R.id.floorHeightEditText);

		floorPressuresListView = (ExpandableListView) rootView.findViewById(R.id.floorPressureslistView);
		setHasOptionsMenu(true);
		// floorHeightEditText.setOnKeyListener(new OnKeyListener() {
		// public boolean onKey(View v, int keyCode, KeyEvent event) {
		// try {
		// floorHeight =
		// Double.parseDouble(floorHeightEditText.getText().toString());
		// } catch (Exception e) {
		// floorHeight = 0.0;
		// }
		// doUpdate();
		// return false;
		// }
		// });

		floorPressuresListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
					int groupPosition = ExpandableListView.getPackedPositionGroup(id);

					if (groupPosition > -1) {
						FloorPressures floorPressures = (FloorPressures) floorPressuresAdapter.getGroup(groupPosition);
						if (floorPressures != null) {
							Intent intent = new Intent(getActivity(), FloorPressureChartActivity.class);
							intent.putExtra(FloorPressureChartActivity.ARG_FLOORPRESSURES, floorPressures.getJSON()
									.toString());
							startActivity(intent);
						}
					}
				}
				if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					int groupPosition = ExpandableListView.getPackedPositionGroup(id);
					int childPosition = ExpandableListView.getPackedPositionChild(id);

					if (groupPosition > -1 && childPosition > -1) {
						FloorPressure floorPressure = (FloorPressure) floorPressuresAdapter.getChild(groupPosition,
								childPosition);
						if (floorPressure != null) {
							doFloorPressureDeleteDialog(floorPressure.getId());
							return true;
						}
					}
				}
				return false;
			}
		});
		floorPressuresListView.setOnGroupExpandListener(new OnGroupExpandListener() {
			public void onGroupExpand(int groupPosition) {
				FloorPressures floorPressures = (FloorPressures) floorPressuresAdapter.getGroup(groupPosition);
				if (floorPressures != null)
					doSetSelected(floorPressures);
			}
		});

		// floorPressuresListView.setOnChildClickListener(new
		// OnChildClickListener() {
		// public boolean onChildClick(ExpandableListView parent, View v, int
		// groupPosition, int childPosition, long id) {
		// FloorPressure floorPressure = (FloorPressure)
		// floorPressuresAdapter.getChild(groupPosition,
		// childPosition);
		// if (floorPressure != null) {
		// doFloorPressureDeleteDialog(floorPressure.getId());
		// return true;
		// }
		// return false;
		// }
		// });

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.add(0, 1, 0, "Push cloud");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			doPushCloud();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(BUNDLE_KEY_FLOOR, floorPredict);
		outState.putDouble(BUNDLE_KEY_FLOOR_PREDICT_PREV, floorPredictPrev);
		outState.putDouble(BUNDLE_KEY_FLOOR_PREDICT_CURRENT, floorPredictCurrent);
		outState.putDouble(BUNDLE_KEY_FLOOR_PREDICT_NEXT, floorPredictNext);
		outState.putDouble(BUNDLE_KEY_FLOOR_PREDICT_NEXT, floorPredictNext);
		outState.putDouble(BUNDLE_KEY_FLOOR_HEIGHT, floorHeight);
		outState.putDouble(BUNDLE_KEY_FLOOR_SETSELECTED, setSelected);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void onCurrentPressure(double currentPressure) {
		this.pressure = currentPressure;
		FloorPredictContainer floorPrediction = this.floorPredictHelper.getFloorPrediction(currentPressure);
		this.floorPredict = floorPrediction.getFloorPredicted();
		this.floorPredictCurrent = floorPrediction.getPressureFloorDiff();
		this.floorPredictPrev = floorPrediction.getPressureFloorPrevDiff();
		this.floorPredictNext = floorPrediction.getPressureFloorNextDiff();
		doUpdate();
	}

	public void onSealevelPressure(double sealevelPressure) {
	}

	public void onFloorPressureChanged(List<FloorPressures> floorPressuresList) {
		floorPressuresAdapter.addPressures(floorPressuresList);
		floorPressuresAdapter.notifyDataSetChanged();
	}

	// /ON

	// DO

	private void doUpdate() {
		if (!isStarted)
			return;

		// floorPredict = getCalculatedFloorPredict(pressure, floorPressure,
		// floorHeight, floorNumber);
		floorPredictTextView.setText(String.format("%d", floorPredict));
		pressureTextView.setText(String.format("%.2f", pressure));
		floorPredictPrevTextView.setText(String.format("%+.2f", floorPredictPrev));
		floorPredictCurrentTextView.setText(String.format("%+.2f", floorPredictCurrent));
		floorPredictNextTextView.setText(String.format("%+.2f", floorPredictNext));

		pressureTextView.setText(String.format("%.2f", pressure));
	}

	private void doFloorPressureDeleteDialog(int id) {
		FragmentManager fm = getFragmentManager();
		DeleteFloorPressureDialogFragment deleteFloorPressureDialogFragment = DeleteFloorPressureDialogFragment
				.newInstance(id);
		deleteFloorPressureDialogFragment.show(fm, "fragment_floor_pressure_delete");
	}

	private void doSetSelected(FloorPressures floorPressures) {
		this.setSelected = floorPressures.getSet();
		this.floorPredictHelper.setFloorPressures(floorPressures);
		doUpdate();
	}

	private void doPushCloud() {
		if (this.fragmentContainer.getDropboxHelper().isLoggedIn()) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					List<FloorPressures> floorPressuresList = fragmentContainer.getFloorPressuresList();
					fragmentContainer.getDropboxHelper().doUploadText("floor_measurement.json", Util.createPressuresJson(floorPressuresList).toString(), null);
				}
			};
			thread.start();

		} else
			Toast.makeText(getActivity(), "Dropbox is not linked", Toast.LENGTH_SHORT).show();
	}

	// /DO

	// CLASS

	public class FloorPressureAdapter extends BaseExpandableListAdapter {

		private Context context;
		private List<FloorPressures> floorPressures;

		public FloorPressureAdapter(Context context, List<FloorPressures> floorPressures) {
			this.context = context;
			this.floorPressures = floorPressures;
		}

		public FloorPressures getFloorPressuresForSet(int set) {
			for (FloorPressures floorPressures : this.floorPressures) {
				if (floorPressures.getSet() == set)
					return floorPressures;
			}
			return null;
		}

		public void addPressures(List<FloorPressures> floorPressuresList) {
			this.floorPressures.clear();
			for (FloorPressures floorPressures : floorPressuresList) {
				this.floorPressures.add(floorPressures);
			}
		}

		public Object getChild(int groupPosition, int childPosition) {
			try {
				return floorPressures.get(groupPosition).get(childPosition);
			} catch (Exception e) {
				return null;
			}
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			FloorPressure floorPressure = (FloorPressure) getChild(groupPosition, childPosition);
			double altitude = Util.getCalculatedAltitude(floorPressure.getPressure(), floorPressure.getSealevel());

			if (floorPressure == null)
				return convertView;
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.fragment_floor_pressures_row, null);
			}

			FloorPressure floorPressureNext = (FloorPressure) getChild(groupPosition, childPosition + 1);
			double pressureDiff = FloorPredictHelper.getAveragePresure(floorPressure, floorPressureNext);
			// if (floorPressureNext != null) {
			// pressureDiff = Math.abs(floorPressure.getPressure() -
			// floorPressureNext.getPressure());
			// if (floorPressure.getFloor() != floorPressureNext.getFloor())
			// pressureDiff = pressureDiff / Math.abs(floorPressure.getFloor() -
			// floorPressureNext.getFloor());
			// }

			TextView dateTextView = (TextView) convertView.findViewById(R.id.floorPressureDateTextView);
			TextView floorTextView = (TextView) convertView.findViewById(R.id.floorPressureFloorTextView);
			TextView pressureTextView = (TextView) convertView.findViewById(R.id.floorPressurePressureTextView);
			TextView altitudeTextView = (TextView) convertView.findViewById(R.id.floorPressureAltitudeTextView);
			TextView diffTextView = (TextView) convertView.findViewById(R.id.floorPressureDiffTextView);

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			dateTextView.setText(sdf.format(floorPressure.getRegistered()));
			floorTextView.setText(String.valueOf(floorPressure.getFloor()));
			pressureTextView.setText(String.format("%.2f", floorPressure.getPressure()));
			altitudeTextView.setText(String.format("%.2f", altitude));
			diffTextView.setText(pressureDiff > 0.0 ? String.format("%.5f", pressureDiff) : "0");

			return convertView;
		}

		public int getChildrenCount(int groupPosition) {
			return ((List) getGroup(groupPosition)).size();
		}

		public Object getGroup(int groupPosition) {
			return this.floorPressures.get(groupPosition);
		}

		public int getGroupCount() {
			return this.floorPressures.size();
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			FloorPressures floorPressures = (FloorPressures) getGroup(groupPosition);

			if (floorPressures == null)
				return convertView;
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) this.context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.fragment_floor_pressures_group, null);
			}

			int count = getChildrenCount(groupPosition);

			TextView setTextView = (TextView) convertView.findViewById(R.id.floorPressureSetTextView);
			TextView buildingTextView = (TextView) convertView.findViewById(R.id.floorPressureBuildingTextView);
			TextView countTextView = (TextView) convertView.findViewById(R.id.floorPressureCountTextView);
			TextView pressureTextView = (TextView) convertView.findViewById(R.id.floorPressurePressureTextView);
			TextView dateTextView = (TextView) convertView.findViewById(R.id.floorPressureDateTextView);

			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM");
			setTextView.setText(String.valueOf(floorPressures.getSet()));
			buildingTextView.setText(floorPressures.getBuilding());
			countTextView.setText(String.format("(%d)", count));
			pressureTextView.setText(String.format("%.5f", floorPressures.getPressureAverage()));
			dateTextView.setText(sdf.format(floorPressures.getDate()));

			if (floorPressures.getSet() == setSelected) {
				convertView.setBackgroundResource(R.color.item_highlight);
			} else {
				convertView.setBackgroundResource(android.R.color.white);
			}

			return convertView;
		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	public static class DeleteFloorPressureDialogFragment extends DialogFragment {

		private static final String ARG_ID = "id";

		public static DeleteFloorPressureDialogFragment newInstance(int id) {
			DeleteFloorPressureDialogFragment deleteFloorPressureDialogFragment = new DeleteFloorPressureDialogFragment();

			Bundle args = new Bundle();
			args.putInt(ARG_ID, id);
			deleteFloorPressureDialogFragment.setArguments(args);

			return deleteFloorPressureDialogFragment;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int floorPressureId = getArguments().getInt(ARG_ID);

			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Delete Floor Pressure")
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Log.d(TAG, "Dialog delete: " + floorPressureId);
							try {
								((HasFragment) getActivity()).doFloorPressureDelete(floorPressureId);
							} catch (ClassCastException e) {
								Log.e(TAG, getActivity().toString() + " must implement HasFragment");
							}
						}
					}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
			// Create the AlertDialog object and return it
			return builder.create();
		}

	}

	// /CLASS

}
