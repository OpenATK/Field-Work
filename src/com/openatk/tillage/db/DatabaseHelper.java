package com.openatk.tillage.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "tillage.db";
	private static final int DATABASE_VERSION = 2;
	
	private static SimpleDateFormat dateFormaterUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	private static SimpleDateFormat dateFormaterLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		dateFormaterUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormaterLocal.setTimeZone(TimeZone.getDefault());
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		TableFields.onCreate(database);
		TableJobs.onCreate(database);
		TableWorkers.onCreate(database);
		TableOperations.onCreate(database);
	}

	// Method is called during an upgrade of the database,
	// e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		TableFields.onUpgrade(database, oldVersion, newVersion);
		TableJobs.onUpgrade(database, oldVersion, newVersion);
		TableWorkers.onUpgrade(database, oldVersion, newVersion);
		TableOperations.onUpgrade(database, oldVersion, newVersion);
	}
	/*
	 * Takes in a date and returns it in a string format
	 */
	public static String dateToStringUTC(Date date) {
		if(date == null){
			return null;
		}
		return DatabaseHelper.dateFormaterUTC.format(date);
	}
	
	/*
	 * Takes in a string formated by dateFormat() and returns the
	 * original date.
	 */
	public static Date stringToDateUTC(String date) {
		if(date == null){
			return null;
		}
		Date d;
		try {
			d = DatabaseHelper.dateFormaterUTC.parse(date);
		} catch (ParseException e) {
			d = new Date(0);
		}
		return d;
	}
	/*
	 * Takes in a date and returns it in a string format
	 */
	public static String dateToStringLocal(Date date) {
		if(date == null){
			return null;
		}
		return DatabaseHelper.dateFormaterLocal.format(date);
	}
	
	/*
	 * Takes in a string formated by dateFormat() and returns the
	 * original date.
	 */
	public static Date stringToDateLocal(String date) {
		if(date == null){
			return null;
		}
		Date d;
		try {
			d = DatabaseHelper.dateFormaterLocal.parse(date);
		} catch (ParseException e) {
			d = new Date(0);
		}
		return d;
	}
}
