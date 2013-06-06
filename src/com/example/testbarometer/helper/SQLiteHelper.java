package com.example.testbarometer.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class SQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_PRESSURE = "pressure";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SET = "set_";
	public static final String COLUMN_PRESSURE = "pressure";
	public static final String COLUMN_PRESSURE_LOW = "low";
	public static final String COLUMN_PRESSURE_HIGH = "high";
	public static final String COLUMN_BUILDING = "building";
	public static final String COLUMN_FLOOR = "floor";
	public static final String COLUMN_SEALEVEL = "sealevel";
	public static final String COLUMN_REGISTERED = "registered";

	private static final String DATABASE_NAME = "pressure.db";
	private static final int DATABASE_VERSION = 3;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_PRESSURE + "(" + COLUMN_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_PRESSURE + " FLOAT NOT NULL, " + COLUMN_PRESSURE_LOW
			+ " FLOAT NOT NULL, " + COLUMN_PRESSURE_HIGH + " FLOAT NOT NULL, " + COLUMN_BUILDING + " TEXT NOT NULL, "
			+ COLUMN_FLOOR + " INTEGER NOT NULL, " + COLUMN_SEALEVEL + " FLOAT NOT NULL, " + COLUMN_SET
			+ " INTEGER NOT NULL, " + COLUMN_REGISTERED + " TIMESTAMP NOT NULL);";
	private Context context;

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(SQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESSURE);
		onCreate(db);
	}

}
