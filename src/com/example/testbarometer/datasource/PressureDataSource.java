package com.example.testbarometer.datasource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.testbarometer.helper.SQLiteHelper;
import com.example.testbarometer.model.FloorPressure;
import com.example.testbarometer.model.FloorPressures;

public class PressureDataSource {

	private SQLiteHelper dbHelper;
	private SQLiteDatabase database;
	private String[] allColumns = { SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_SET, SQLiteHelper.COLUMN_BUILDING,
			SQLiteHelper.COLUMN_FLOOR, SQLiteHelper.COLUMN_PRESSURE, SQLiteHelper.COLUMN_PRESSURE_LOW,
			SQLiteHelper.COLUMN_PRESSURE_HIGH, SQLiteHelper.COLUMN_SEALEVEL, SQLiteHelper.COLUMN_REGISTERED };

	public PressureDataSource(Context context) {
		dbHelper = new SQLiteHelper(context);
	}

	public void open() {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public long createPressure(FloorPressure pressure) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_SET, pressure.getSet());
		values.put(SQLiteHelper.COLUMN_BUILDING, pressure.getBuilding());
		values.put(SQLiteHelper.COLUMN_FLOOR, pressure.getFloor());
		values.put(SQLiteHelper.COLUMN_PRESSURE, pressure.getPressure());
		values.put(SQLiteHelper.COLUMN_PRESSURE_LOW, pressure.getPressureLow());
		values.put(SQLiteHelper.COLUMN_PRESSURE_HIGH, pressure.getPressureHigh());
		values.put(SQLiteHelper.COLUMN_SEALEVEL, pressure.getSealevel());
		values.put(SQLiteHelper.COLUMN_REGISTERED, new Date().getTime());

		return database.insert(SQLiteHelper.TABLE_PRESSURE, null, values);
	}

	public void deletePressure(int id) {
		database.delete(SQLiteHelper.TABLE_PRESSURE, SQLiteHelper.COLUMN_ID + "=" + id, null);
	}

	public List<FloorPressure> getPressures() {
		List<FloorPressure> pressures = new ArrayList<FloorPressure>();

		Cursor cursor = database.query(SQLiteHelper.TABLE_PRESSURE, allColumns, null, null, null, null,
				String.format("%s DESC", SQLiteHelper.COLUMN_REGISTERED));

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			pressures.add(getCreatedPressure(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return pressures;
	}

	public FloorPressures getPressures(int set, String building) {
		FloorPressures pressures = new FloorPressures();

		Cursor cursor = database.query(SQLiteHelper.TABLE_PRESSURE, allColumns, String.format(
				"%s = %s AND %s LIKE '%s'", SQLiteHelper.COLUMN_SET, set, SQLiteHelper.COLUMN_BUILDING, building),
				null, null, null, String.format("%s DESC", SQLiteHelper.COLUMN_REGISTERED));

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			pressures.add(getCreatedPressure(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return pressures;
	}

	private FloorPressure getCreatedPressure(Cursor cursor) {
		FloorPressure pressure = new FloorPressure();
		pressure.setId(cursor.getInt(0));
		pressure.setSet(cursor.getInt(1));
		pressure.setBuilding(cursor.getString(2));
		pressure.setFloor(cursor.getInt(3));
		pressure.setPressure(cursor.getDouble(4));
		pressure.setPressureLow(cursor.getDouble(5));
		pressure.setPressureHigh(cursor.getDouble(6));
		pressure.setSealevel(cursor.getDouble(7));
		pressure.setRegistered(new Date(cursor.getLong(8)));
		return pressure;
	}
}
