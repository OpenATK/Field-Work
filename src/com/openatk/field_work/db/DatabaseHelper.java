package com.openatk.field_work.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.openatk.field_work.drawing.MyPolygon;
import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Job;
import com.openatk.field_work.models.Worker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "field_work.db";
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
	
	
	//Functions to read database
	public List<Field> readFields(){
		return DatabaseHelper.readFields(this);
	}
	public static List<Field> readFields(DatabaseHelper dbHelper){
		List<Field> fields = new ArrayList<Field>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor = database.query(TableFields.TABLE_NAME, TableFields.COLUMNS, null, null, null, null, null);
		while (cursor.moveToNext()) {
			fields.add(TableFields.cursorToField(cursor));
		}
		cursor.close();
		
		database.close();
		dbHelper.close();
		return fields;
	}
	
	public List<Job> readJobs(){
		return DatabaseHelper.readJobs(this);
	}
	public static List<Job> readJobs(DatabaseHelper dbHelper){
		List<Job> jobs = new ArrayList<Job>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor = database.query(TableJobs.TABLE_NAME, TableJobs.COLUMNS, null, null, null, null, null);
		while (cursor.moveToNext()) {
			jobs.add(TableJobs.cursorToJob(cursor));
		}
		cursor.close();
		
		database.close();
		dbHelper.close();
		return jobs;
	}

	
	
	//Workers
	public int updateWorker(Worker worker){
		return updateWorker(this, worker);
	}
	public static int updateWorker(DatabaseHelper dbHelper, Worker worker){
		//Inserts, updates
		//Only non-null fields are updated
		//Used by both LibTrello and MainActivity to update database data

		int ret = -1;
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
				
		if(worker.getRemote_id() != null) values.put(TableWorkers.COL_REMOTE_ID, worker.getRemote_id());
		if(worker.getDateNameChanged() != null) values.put(TableWorkers.COL_NAME_CHANGED, DatabaseHelper.dateToStringUTC(worker.getDateNameChanged()));
		if(worker.getName() != null) values.put(TableWorkers.COL_NAME, worker.getName());
		
		if(worker.getId() == null && worker.getRemote_id() == null) {
			//INSERT This is a new worker, has no id's
			ret = (int) database.insert(TableWorkers.TABLE_NAME, null, values);
		} else {
			//UPDATE
			//If have id, lookup by that, it's fastest
			String where;
			if(worker.getId() != null){
				where = TableWorkers.COL_ID + " = " + Integer.toString(worker.getId());
			} else {
				where = TableWorkers.COL_REMOTE_ID + " = '" + worker.getRemote_id() + "'";
			}
			ret = database.update(TableWorkers.TABLE_NAME, values, where, null);
		}
		
		database.close();
		dbHelper.close();
		return ret;
	}
	
	//TODO delete worker (only called by mainactivity after views are updated from isDeleted)
	
	
	
	
	
	/*public static Field FindFieldByName(SQLiteDatabase database, String name){
		if(name != null){
			//Find current field
			Field theField = null;
			String where = TableFields.COL_NAME + " = '" + name + "'";
			Cursor cursor = database.query(TableFields.TABLE_NAME, TableFields.COLUMNS, where, null, null, null, null);
			if(cursor.moveToFirst()) {
				theField = Field.cursorToField(cursor);
			}
			cursor.close();
			return theField;
		} else {
			return null;
		}
	}*/
	
	
}
