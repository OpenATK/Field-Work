package com.openatk.field_work.db;

import java.util.Date;

import com.openatk.field_work.models.Job;
import com.openatk.field_work.models.Operation;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
    	int version = oldVersion + 1;
    	switch(version){
    		case 1: //Launch
    			//Do nothing this is the gplay launch version
    		case 2: //V2
    			//Nothing changed in this table
    		case 3:
    			Log.d("TableOperations - onUpgrade", "upgarding to v3");
    			database.beginTransaction();
    			try {
        			database.execSQL("create table backup(_id, remote_id, name)");
        			database.execSQL("insert into backup select _id, remote_id, name from operations");
        			database.execSQL("drop table operations");
        			database.execSQL(DATABASE_CREATE);
        			database.execSQL("insert into " + TABLE_NAME + " (_id, remote_id, name) select _id, remote_id, name from backup");
        			database.execSQL("drop table backup");
        			database.setTransactionSuccessful();
    			} finally {
    				database.endTransaction();
    			}
    			Cursor cursor = database.query(TableOperations.TABLE_NAME, TableOperations.COLUMNS, null, null, null, null, null);
    			while(cursor.moveToNext()) {
    				int id = cursor.getInt(cursor.getColumnIndex(TableOperations.COL_ID));
    				//Update in db
    				ContentValues values = new ContentValues();
    				values.put(TableOperations.COL_DELETED_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				values.put(TableOperations.COL_NAME_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				database.update(TableOperations.TABLE_NAME, values, TableOperations.COL_ID + " = " + Integer.toString(id), null);
    			}
    			cursor.close();
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
		return TableOperations.FindOperationById(dbHelper, id, true);
	}
	public static Operation FindOperationById(DatabaseHelper dbHelper, Integer id, Boolean notDeleted) {
		if(dbHelper == null) return null;

		if (id != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find current field
			Operation item = null;
			String where = TableOperations.COL_ID + " = " + Integer.toString(id);
			if(notDeleted) where = where + " AND " + TableOperations.COL_DELETED + " = 0";
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
	
	public static Operation FindOperationByTrelloId(DatabaseHelper dbHelper, String id) {
		if(dbHelper == null) return null;

		if (id != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find current field
			Operation item = null;
			String where = TableOperations.COL_REMOTE_ID + " = ?";
			Cursor cursor = database.query(TableOperations.TABLE_NAME, TableOperations.COLUMNS, where, new String[]{id}, null, null, null);
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
	
	public static boolean updateOperation(DatabaseHelper dbHelper, Operation operation){
		//Inserts, updates
		//Only non-null fields are updated
		//Used by both MyTrelloContentProvider and MainActivity to update database data
		
		boolean ret = false;
		
		ContentValues values = new ContentValues();
		if(operation.getRemote_id() != null) values.put(TableOperations.COL_REMOTE_ID, operation.getRemote_id());
		
		if(operation.getName() != null) values.put(TableOperations.COL_NAME, operation.getName());
		if(operation.getDateNameChanged() != null) values.put(TableOperations.COL_NAME_CHANGED, DatabaseHelper.dateToStringUTC(operation.getDateNameChanged()));
		
		if(operation.getDeleted() != null) values.put(TableOperations.COL_DELETED, (operation.getDeleted() == false ? 0 : 1));
		if(operation.getDateDeletedChanged() != null) values.put(TableOperations.COL_DELETED_CHANGED, DatabaseHelper.dateToStringUTC(operation.getDateDeletedChanged()));
		
		if(values.size() > 0){
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			if(operation.getId() == null) {
				//INSERT This is a new worker has no id
				int id = (int) database.insert(TableOperations.TABLE_NAME, null, values);
				operation.setId(id);
				Log.d("TableOperations", "INSERT id:" + Integer.toString(id) +  " name:" + operation.getName());
				ret = true;
			} else {
				//UPDATE
				//If have id, lookup by that, it's fastest
				if(operation.getId() != null) Log.d("TableOperations", "Update id:" + Integer.toString(operation.getId()));
				if(operation.getRemote_id() != null) Log.d("TableOperations", "Update trello id:" + operation.getRemote_id());
	
				String where = TableOperations.COL_ID + " = " + Integer.toString(operation.getId());
				database.update(TableOperations.TABLE_NAME, values, where, null);
				ret = true;
			}
			
			database.close();
			dbHelper.close();
		}
		return ret;
	}
	public static boolean deleteOperation(DatabaseHelper dbHelper, Operation operation){
		//Delete job by local id or Trello id
		//Used by MyTrelloContentProvider
		
		boolean ret = false;
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		if(operation.getId() == null && (operation.getRemote_id() == null || operation.getRemote_id().length() == 0)) {
			//Can't delete without an id
			ret = false;
		} else {
			//DELETE
			//If have id, lookup by that, it's fastest
			String where;
			String[] args = null;
			if(operation.getId() != null){
				where = TableOperations.COL_ID + " = " + Integer.toString(operation.getId());
			} else {
				args = new String[]{operation.getRemote_id()};
				where = TableOperations.COL_REMOTE_ID + " = ?";
			}
			database.delete(TableOperations.TABLE_NAME, where, args);
			ret = true;
		}
		database.close();
		dbHelper.close();
		return ret;
	}
	public static boolean deleteAll(DatabaseHelper dbHelper){
		//Deleted all operations in the db
		//Used by MyTrelloContentProvider
		
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(TableOperations.TABLE_NAME, null, null);
		database.close();
		dbHelper.close();
		return true;
	}
}
