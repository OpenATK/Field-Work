package com.openatk.field_work.db;

import java.util.Date;

import com.openatk.field_work.models.Worker;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableWorkers {
	// Database table
	public static final String TABLE_NAME = "workers";
	public static final String COL_ID = "_id";
	public static final String COL_REMOTE_ID = "remote_id";
	
	public static final String COL_NAME = "name";
	public static final String COL_NAME_CHANGED = "name_changed";

	public static final String COL_DELETED = "deleted";
	public static final String COL_DELETED_CHANGED = "deleted_changed";
	
	public static String[] COLUMNS = { COL_ID, COL_REMOTE_ID, COL_NAME, COL_NAME_CHANGED, 
		COL_DELETED, COL_DELETED_CHANGED };

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " 
	      + TABLE_NAME
	      + "(" 
	      + COL_ID + " integer primary key autoincrement," 
	      + COL_REMOTE_ID + " text default ''," 
	      + COL_NAME + " text," 
	      + COL_NAME_CHANGED + " text,"
	      + COL_DELETED + " integer default 0," 
	      + COL_DELETED_CHANGED + " text" 
	      + ");";

	public static void onCreate(SQLiteDatabase database) {
	  database.execSQL(DATABASE_CREATE);
	}

	//TODO handle update
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.d("TableWorkers - onUpgrade", "Upgrade from " + Integer.toString(oldVersion) + " to " + Integer.toString(newVersion));
    	int version = oldVersion;
    	switch(version){
    		case 1: //Launch
    			//Do nothing this is the gplay launch version
    		case 2: //V2
    			//Nothing changed in this table
    	}
	    //database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    //onCreate(database);
	}
	
	
	public static Worker cursorToWorker(Cursor cursor) {
		if(cursor != null){
			Integer id = cursor.getInt(cursor.getColumnIndex(TableWorkers.COL_ID));
			String remote_id = cursor.getString(cursor.getColumnIndex(TableWorkers.COL_REMOTE_ID));
			Date dateNameChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableWorkers.COL_NAME_CHANGED)));
			String name = cursor.getString(cursor.getColumnIndex(TableWorkers.COL_NAME));
			Boolean deleted = cursor.getInt(cursor.getColumnIndex(TableWorkers.COL_DELETED)) == 1 ? true : false;;
			Date dateDeletedChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableWorkers.COL_DELETED_CHANGED)));;
			
			Worker worker = new Worker(id, remote_id, dateNameChanged, name, deleted, dateDeletedChanged);
			return worker;
		} else {
			return null;
		}
	}
}
