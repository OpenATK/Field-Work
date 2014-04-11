package com.openatk.field_work.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import com.google.android.gms.maps.model.LatLng;
import com.openatk.field_work.models.Field;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableFields {
	// Database table
	public static final String TABLE_NAME = "fields";
	public static final String COL_ID = "_id";
	public static final String COL_REMOTE_ID = "remote_id";
	public static final String COL_NAME = "name";
	public static final String COL_NAME_CHANGED = "name_changed";
	public static final String COL_ACRES = "acres";
	public static final String COL_ACRES_CHANGED = "acres_changed";

	public static final String COL_BOUNDARY = "boundary";
	public static final String COL_BOUNDARY_CHANGED = "boundary_changed";

	public static final String COL_DELETED = "deleted";
	public static final String COL_DELETED_CHANGED = "deleted_changed";


	public static String[] COLUMNS = { COL_ID, COL_REMOTE_ID, COL_NAME, 
		COL_NAME_CHANGED, COL_ACRES, COL_ACRES_CHANGED, COL_BOUNDARY, 
		COL_BOUNDARY_CHANGED, COL_DELETED, COL_DELETED_CHANGED };
	
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " 
	      + TABLE_NAME
	      + "(" 
	      + COL_ID + " integer primary key autoincrement," 
	      + COL_REMOTE_ID + " text default ''," 
	      + COL_NAME + " text," 
	      + COL_NAME_CHANGED + " text,"
	      + COL_ACRES + " integer default 0,"
	      + COL_ACRES_CHANGED + " text,"
	      + COL_BOUNDARY + " text,"
	      + COL_BOUNDARY_CHANGED + " text,"
	      + COL_DELETED + " integer default 0,"
	      + COL_DELETED_CHANGED + " text"
	      + ");";

	public static void onCreate(SQLiteDatabase database) {
	  database.execSQL(DATABASE_CREATE);
	}
	
	//TODO handle upgrade
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.d("TableFields - onUpgrade", "Upgrade from " + Integer.toString(oldVersion) + " to " + Integer.toString(newVersion));
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
	
	public static Field cursorToField(Cursor cursor){
		if(cursor != null){
			Integer id = cursor.getInt(cursor.getColumnIndex(TableFields.COL_ID));
			String remote_id = cursor.getString(cursor.getColumnIndex(TableFields.COL_REMOTE_ID));
			
			String name = cursor.getString(cursor.getColumnIndex(TableFields.COL_NAME));
			Date dateNameChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableFields.COL_NAME_CHANGED)));

			Float acres = cursor.getFloat(cursor.getColumnIndex(TableFields.COL_ACRES));
			Date dateAcresChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableFields.COL_ACRES_CHANGED)));

			Boolean deleted = cursor.getInt(cursor.getColumnIndex(TableFields.COL_DELETED)) == 1 ? true : false;
			Date dateDeleted = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableFields.COL_DELETED_CHANGED)));
			
			List<LatLng> boundary = TableFields.StringToBoundary(cursor.getString(cursor.getColumnIndex(TableFields.COL_BOUNDARY)));
			Date dateBoundaryChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableFields.COL_BOUNDARY_CHANGED)));
			
			Field newField = new Field(id, remote_id, name,
					dateNameChanged, acres, dateAcresChanged,
				    deleted, dateDeleted, boundary, dateBoundaryChanged);
			
			return newField;
		} else {
			return null;
		}
	}
	
	public static List<LatLng> StringToBoundary(String boundary){
		StringTokenizer tokens = new StringTokenizer(boundary, ",");
		List<LatLng> points = new ArrayList<LatLng>();
		while (tokens.hasMoreTokens()) {
			String lat = tokens.nextToken();
			String lng = tokens.nextToken();
			points.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
		}
		return points;
	}
}
