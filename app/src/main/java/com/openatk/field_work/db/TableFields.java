package com.openatk.field_work.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import com.google.android.gms.maps.model.LatLng;
import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Operation;
import com.openatk.field_work.models.Worker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

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

	private static final String DB_UPGRADE_v3_1 = "create table backup(_id, remote_id, name, acres, boundary);"
			+ "insert into backup select _id, remote_id, name, acres, boundary from fields;"
			+ "drop table fields;"
			+ DATABASE_CREATE
			+ "insert into " + TABLE_NAME + " select, remote_id, name, acres, boundary from backup;"
			+ "drop table backup;";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}
	
	//TODO handle upgrade
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.d("TableFields - onUpgrade", "Upgrade from " + Integer.toString(oldVersion) + " to " + Integer.toString(newVersion));
    	int version = oldVersion + 1;
    	switch(version){
    		case 1: //Launch
    			//Do nothing this is the gplay launch version
    		case 2: //V2
    			//Nothing changed in this table
    		case 3:
    			//Major changes
    			Log.d("TableJobs - onUpgrade", "upgarding to v3");
    			database.beginTransaction();
    			try {
        			database.execSQL("create table backup(_id, remote_id, name, acres, boundary)");
        			database.execSQL("insert into backup select _id, remote_id, name, acres, boundary from fields");
        			database.execSQL("drop table fields");
        			database.execSQL(DATABASE_CREATE);
        			database.execSQL("insert into " + TABLE_NAME + " (_id, remote_id, name, acres, boundary) select _id, remote_id, name, acres, boundary from backup");
        			database.execSQL("drop table backup");
        			database.setTransactionSuccessful();
    			} finally {
    				database.endTransaction();
    			}
    			//Remove last point from all boundaries
    			Cursor cursor = database.query(TableFields.TABLE_NAME, TableFields.COLUMNS, null, null, null, null, null);
    			while(cursor.moveToNext()) {
    				int id = cursor.getInt(cursor.getColumnIndex(TableFields.COL_ID));
    				String strBoundary = cursor.getString(cursor.getColumnIndex(TableFields.COL_BOUNDARY));
    				List<LatLng> boundary = TableFields.StringToBoundary(strBoundary);
    				if(boundary.size() > 3){
    					boundary.remove(boundary.size() - 1);
    				}
    				strBoundary = TableFields.BoundaryToString(boundary);
    				//Update in db
    				ContentValues values = new ContentValues();
    				values.put(TableFields.COL_BOUNDARY, strBoundary);
    				values.put(TableFields.COL_BOUNDARY_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				values.put(TableFields.COL_NAME_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				values.put(TableFields.COL_ACRES_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				values.put(TableFields.COL_DELETED_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				database.update(TableFields.TABLE_NAME, values, TableFields.COL_ID + " = " + Integer.toString(id), null);
    			}
    			cursor.close();
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
		if(boundary == null) return new ArrayList<LatLng>();
		StringTokenizer tokens = new StringTokenizer(boundary, ",");
		List<LatLng> points = new ArrayList<LatLng>();
		while (tokens.countTokens() > 1) {
			String lat = tokens.nextToken();
			String lng = tokens.nextToken();
			points.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
		}
		return points;
	}
	
	public static String BoundaryToString(List<LatLng>  boundary){
		String strNewBoundary = "";
		if(boundary != null && boundary.isEmpty() == false){
			// Generate boundary
			StringBuilder newBoundary = new StringBuilder(boundary.size() * 20);
			for (int i = 0; i < boundary.size(); i++) {
				newBoundary.append(boundary.get(i).latitude);
				newBoundary.append(",");
				newBoundary.append(boundary.get(i).longitude);
				newBoundary.append(",");
			}
			newBoundary.deleteCharAt(newBoundary.length() - 1);
			strNewBoundary = newBoundary.toString();
		}
		return strNewBoundary;
	}
	
	public static Field FindFieldByName(DatabaseHelper dbHelper, String name) {
		if(dbHelper == null) return null;
		
		if (name != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find current field
			Field theField = null;
			String where = TableFields.COL_NAME + " = ? AND " + TableFields.COL_DELETED + " = 0";
			Cursor cursor = database.query(TableFields.TABLE_NAME, TableFields.COLUMNS, where, new String[] {name}, null, null, null);
			if (cursor.moveToFirst()) {
				theField = TableFields.cursorToField(cursor);
			}
			cursor.close();
			database.close();
			dbHelper.close();
			return theField;
		} else {
			return null;
		}
	}

	public static Field FindFieldById(DatabaseHelper dbHelper, Integer id) {
		if(dbHelper == null) return null;

		if (id != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find current field
			Field theField = null;
			String where = TableFields.COL_ID + " = " + Integer.toString(id) + " AND " + TableFields.COL_DELETED + " = 0";
			Cursor cursor = database.query(TableFields.TABLE_NAME,TableFields.COLUMNS, where, null, null, null, null);
			if (cursor.moveToFirst()) {
				theField = TableFields.cursorToField(cursor);
			}
			cursor.close();
			database.close();
			dbHelper.close();
			return theField;
		} else {
			return null;
		}
	}
	
	public static Field FindFieldByRemoteId(DatabaseHelper dbHelper, String remoteId) {
		if(dbHelper == null) return null;

		if (remoteId != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find current field
			Field theField = null;
			String where = TableFields.COL_REMOTE_ID + " = ?";
			Cursor cursor = database.query(TableFields.TABLE_NAME,TableFields.COLUMNS, where,  new String[] {remoteId}, null, null, null);
			if (cursor.moveToFirst()) {
				theField = TableFields.cursorToField(cursor);
			}
			cursor.close();
			database.close();
			dbHelper.close();
			return theField;
		} else {
			return null;
		}
	}
	
	public static boolean updateField(DatabaseHelper dbHelper, Field field){
		//Inserts, updates
		//Only non-null fields are updated
		//Used by both LibTrello and MainActivity to update database data
		
		boolean ret = false;
		
		ContentValues values = new ContentValues();
		if(field.getRemote_id() != null) values.put(TableFields.COL_REMOTE_ID, field.getRemote_id());
		
		if(field.getDateNameChanged() != null) values.put(TableFields.COL_NAME_CHANGED, DatabaseHelper.dateToStringUTC(field.getDateNameChanged()));
		if(field.getName() != null) values.put(TableFields.COL_NAME, field.getName());
		Log.d("TableFields - updateField", "FieldName:" + field.getName());
		
		if(field.getAcres() != null) values.put(TableFields.COL_ACRES, field.getAcres());
		if(field.getDateAcresChanged() != null) values.put(TableFields.COL_ACRES_CHANGED, DatabaseHelper.dateToStringUTC(field.getDateAcresChanged()));

		
		if(field.getBoundary() != null) values.put(TableFields.COL_BOUNDARY, TableFields.BoundaryToString(field.getBoundary()));
		if(field.getDateBoundaryChanged() != null) values.put(TableFields.COL_BOUNDARY_CHANGED, DatabaseHelper.dateToStringUTC(field.getDateBoundaryChanged()));

		if(field.getDeleted() != null) values.put(TableFields.COL_DELETED, (field.getDeleted() == false ? 0 : 1));
		if(field.getDateDeleted() != null) values.put(TableFields.COL_DELETED_CHANGED, DatabaseHelper.dateToStringUTC(field.getDateDeleted()));

		if(values.size() > 0){
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			if(field.getId() == null) {
				//INSERT This is a new worker, has no id's
				int id = (int) database.insert(TableFields.TABLE_NAME, null, values);
				field.setId(id);
				ret = true;
			} else {
				//UPDATE
				String where = TableFields.COL_ID + " = " + Integer.toString(field.getId());
				database.update(TableFields.TABLE_NAME, values, where, null);
				ret = true;
			}
			
			database.close();
			dbHelper.close();
		}
		return ret;
	}
	public static boolean deleteFieldIfNotSynced(DatabaseHelper dbHelper, Field field){
		//Delete field by local id if it has a remote id, ie. has been synced to cloud
		//Used by MainActivity
		boolean ret = false;
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		if(field.getId() == null) {
			//Can't delete without an id
			Log.w("deleteFieldIfNotSynced", "Field has no id cant delete");
			ret = false;
		} else {
			//If have id, lookup by that, it's fastest
			String where = TableFields.COL_ID + " = " + Integer.toString(field.getId()) + " AND " + TableFields.COL_REMOTE_ID + " = ''";
			if(database.delete(TableFields.TABLE_NAME, where, null) > 0){
				ret = true;
			}
		}
		database.close();
		dbHelper.close();
		return ret;
	}
	public static boolean deleteField(DatabaseHelper dbHelper, Field field){
		//Deleted field by local id or Trello id
		//Used by MyTrelloContentProvider
		
		boolean ret = false;
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		if(field.getId() == null && (field.getRemote_id() == null || field.getRemote_id().length() == 0)) {
			//Can't delete without an id
			ret = false;
		} else {
			//UPDATE
			//If have id, lookup by that, it's fastest
			String where;
			String[] args = null;
			if(field.getId() != null){
				where = TableFields.COL_ID + " = " + Integer.toString(field.getId());
			} else {
				where = TableFields.COL_REMOTE_ID + " = ?";
				args = new String[] { field.getRemote_id() };
			}
			database.delete(TableFields.TABLE_NAME, where, args);
			ret = true;
		}
		database.close();
		dbHelper.close();
		return ret;
	}
	public static boolean deleteAll(DatabaseHelper dbHelper){
		//Deleted all fields in the db
		//Used by MyTrelloContentProvider
		
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(TableFields.TABLE_NAME, null, null);
		database.close();
		dbHelper.close();
		return true;
	}
}
