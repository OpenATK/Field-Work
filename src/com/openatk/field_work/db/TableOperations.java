package com.openatk.field_work.db;

import java.util.Date;

import com.openatk.field_work.models.Operation;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableOperations {
	// Database table
	public static final String TABLE_NAME = "operations";
	public static final String COL_ID = "_id";
	public static final String COL_REMOTE_ID = "remote_id";
	public static final String COL_NAME = "name";
	public static final String COL_NAME_CHANGED = "name_changed";
	public static final String COL_DELETED = "deleted";
	public static final String COL_DELETED_CHANGED = "deleted_changed";
	
	public static String[] COLUMNS = { COL_ID, COL_REMOTE_ID, COL_NAME_CHANGED, COL_NAME, COL_DELETED, COL_DELETED_CHANGED };
	
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " 
	      + TABLE_NAME
	      + "(" 
	      + COL_ID + " integer primary key autoincrement," 
	      + COL_REMOTE_ID + " text default ''," 
	      + COL_NAME_CHANGED + " text," 
	      + COL_NAME + " text,"
	      + COL_DELETED + " integer default 0,"
	      + COL_DELETED_CHANGED + " text"
	      + ");";

	public static void onCreate(SQLiteDatabase database) {
	  database.execSQL(DATABASE_CREATE);
	}

	//TODO
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.d("TableOperations - onUpgrade", "Upgrade from " + Integer.toString(oldVersion) + " to " + Integer.toString(newVersion));
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
	
	
	public static Operation cursorToOperation(Cursor cursor) {
		if(cursor != null){
			Operation operation = new Operation();
			operation.setId(cursor.getInt(cursor.getColumnIndex(TableOperations.COL_ID)));
			operation.setRemote_id(cursor.getString(cursor.getColumnIndex(TableOperations.COL_REMOTE_ID)));
			operation.setName(cursor.getString(cursor.getColumnIndex(TableOperations.COL_NAME)));
			operation.setDateNameChanged(DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableOperations.COL_NAME_CHANGED))));
			operation.setDeleted(cursor.getInt(cursor.getColumnIndex(TableOperations.COL_DELETED)) == 1 ? true : false);
			operation.setDateDeletedChanged(DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableOperations.COL_DELETED_CHANGED))));
			return operation;
		} else {
			return null;
		}
	}
	
	public static Operation FindOperationById(DatabaseHelper dbHelper, Integer id) {
		if(dbHelper == null) return null;

		if (id != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find current field
			Operation item = null;
			String where = TableOperations.COL_ID + " = " + Integer.toString(id) + " AND " + TableOperations.COL_DELETED + " = 0";
			Cursor cursor = database.query(TableOperations.TABLE_NAME, TableOperations.COLUMNS, where, null, null, null, null);
			if (cursor.moveToFirst()) {
				item = TableOperations.cursorToOperation(cursor);
			}
			cursor.close();
			database.close();
			dbHelper.close();
			return item;
		} else {
			return null;
		}
	}
	
	
	
	
	
	public static void updateOperation(DatabaseHelper dbHelper, Operation operation){
		TableOperations.updateOperation(dbHelper, operation, false);
	}
	public static boolean updateOperation(DatabaseHelper dbHelper, Operation operation, Boolean notify){
		//Add/update operation
		Operation oldOperation = null;
		if(operation.getId() == null){
			//New operation
			oldOperation = TableOperations.FindOperationById(dbHelper, operation.getId());
		}

		
		Boolean updated = false;
		ContentValues values = new ContentValues();
		if(operation.getName() != null && (oldOperation == null || oldOperation.getName().contentEquals(operation.getName()) == false)){
			updated = true;
			values.put(TableOperations.COL_NAME, operation.getName());
			values.put(TableOperations.COL_NAME_CHANGED, DatabaseHelper.dateToStringUTC(new Date()));
		}
		if(operation.getDeleted() != null && (oldOperation == null || oldOperation.getDeleted() != operation.getDeleted())){
			updated = true;
			values.put(TableOperations.COL_DELETED, operation.getDeleted());
			values.put(TableOperations.COL_DELETED_CHANGED, DatabaseHelper.dateToStringUTC(new Date()));
		}
		
		if(updated){
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			if(oldOperation == null) {
				//Insert
				Integer insertId = (int) database.insert(TableOperations.TABLE_NAME, null, values);
				operation.setId(insertId); 
			} else {
				database.update(TableOperations.TABLE_NAME, values, TableOperations.COL_ID + " = " + Integer.toString(operation.getId()), null);
			}
			database.close();
			dbHelper.close();
			
			if(notify){
				//Send broadcast of this change
				//TODO
			}
		}
		return updated;
	}
}
