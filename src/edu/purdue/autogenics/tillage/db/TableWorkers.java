package edu.purdue.autogenics.tillage.db;

import android.database.sqlite.SQLiteDatabase;

public class TableWorkers {
	// Database table
	public static final String TABLE_NAME = "workers";
	public static final String COL_ID = "_id";
	public static final String COL_REMOTE_ID = "remote_id";
	public static final String COL_HAS_CHANGED = "has_changed";
	public static final String COL_DATE_CHANGED = "date_changed";
	public static final String COL_NAME = "name";
	
	public static String[] COLUMNS = { COL_ID, COL_REMOTE_ID, COL_HAS_CHANGED, COL_DATE_CHANGED, COL_NAME };

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " 
	      + TABLE_NAME
	      + "(" 
	      + COL_ID + " integer primary key autoincrement," 
	      + COL_REMOTE_ID + " text default ''," 
	      + COL_HAS_CHANGED + " integer," 
	      + COL_DATE_CHANGED + " text,"
	      + COL_NAME + " text"
	      + ");";

	public static void onCreate(SQLiteDatabase database) {
	  database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
	  database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	  onCreate(database);
	}
}
