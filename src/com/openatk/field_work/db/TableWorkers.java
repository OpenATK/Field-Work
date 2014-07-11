package com.openatk.field_work.db;

import java.util.Date;

import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Operation;
import com.openatk.field_work.models.Worker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
    	int version = oldVersion + 1;
    	switch(version){
    		case 1: //Launch
    			//Do nothing this is the gplay launch version
    		case 2: //V2
    			//Nothing changed in this table
    		case 3:
    			Log.d("TableWorkers - onUpgrade", "upgarding to v3");
    			database.beginTransaction();
    			try {
        			database.execSQL("create table backup(_id, remote_id, name)");
        			database.execSQL("insert into backup select _id, remote_id, name from workers");
        			database.execSQL("drop table workers");
        			database.execSQL(DATABASE_CREATE);
        			database.execSQL("insert into " + TABLE_NAME + " (_id, remote_id, name) select _id, remote_id, name from backup");
        			database.execSQL("drop table backup");
        			database.setTransactionSuccessful();
    			} finally {
    				database.endTransaction();
    			}
    			Cursor cursor = database.query(TableWorkers.TABLE_NAME, TableWorkers.COLUMNS, null, null, null, null, null);
    			while(cursor.moveToNext()) {
    				int id = cursor.getInt(cursor.getColumnIndex(TableWorkers.COL_ID));
    				//Update in db
    				ContentValues values = new ContentValues();
    				values.put(TableWorkers.COL_DELETED_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				values.put(TableWorkers.COL_NAME_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				database.update(TableWorkers.TABLE_NAME, values, TableWorkers.COL_ID + " = " + Integer.toString(id), null);
    			}
    			cursor.close();
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
	
	
	public static Worker FindWorkerById(DatabaseHelper dbHelper, Integer id) {
		if(dbHelper == null) return null;

		if (id != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find current field
			Worker item = null;
			String where = TableWorkers.COL_ID + " = " + Integer.toString(id) + " AND " + TableWorkers.COL_DELETED + " = 0";
			Cursor cursor = database.query(TableWorkers.TABLE_NAME, TableWorkers.COLUMNS, where, null, null, null, null);
			if (cursor.moveToFirst()) {
				item = TableWorkers.cursorToWorker(cursor);
			}
			cursor.close();
			database.close();
			dbHelper.close();
			return item;
		} else {
			return null;
		}
	}
	
	public static Worker FindWorkerByRemoteId(DatabaseHelper dbHelper, String remoteId) {
		if(dbHelper == null) return null;

		if (remoteId != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			Worker item = null;
			String where = TableWorkers.COL_REMOTE_ID + " = ?";
			Cursor cursor = database.query(TableWorkers.TABLE_NAME, TableWorkers.COLUMNS, where, new String[]{remoteId}, null, null, null);
			if (cursor.moveToFirst()) {
				item = TableWorkers.cursorToWorker(cursor);
			}
			cursor.close();
			database.close();
			dbHelper.close();
			return item;
		} else {
			return null;
		}
	}
	
	public static boolean updateWorker(DatabaseHelper dbHelper, Worker worker){
		//Inserts, updates
		//Only non-null fields are updated
		//Used by both LibTrello and MainActivity to update database data
		
		boolean ret = false;
		
		ContentValues values = new ContentValues();
		if(worker.getRemote_id() != null) values.put(TableWorkers.COL_REMOTE_ID, worker.getRemote_id());
		
		if(worker.getDateNameChanged() != null) values.put(TableWorkers.COL_NAME_CHANGED, DatabaseHelper.dateToStringUTC(worker.getDateNameChanged()));
		if(worker.getName() != null) values.put(TableWorkers.COL_NAME, worker.getName());
		
		if(worker.getDeleted() != null) values.put(TableWorkers.COL_DELETED, (worker.getDeleted() == false ? 0 : 1));
		if(worker.getDateDeletedChanged() != null) values.put(TableWorkers.COL_DELETED_CHANGED, DatabaseHelper.dateToStringUTC(worker.getDateDeletedChanged()));

		if(values.size() > 0){
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			if(worker.getId() == null) {
				//INSERT This is a new worker, has no id's
				int id = (int) database.insert(TableWorkers.TABLE_NAME, null, values);
				worker.setId(id);
				ret = true;
			} else {
				//UPDATE
				
				String where = TableWorkers.COL_ID + " = " + Integer.toString(worker.getId());
				database.update(TableWorkers.TABLE_NAME, values, where, null);
				ret = true;
			}
			
			database.close();
			dbHelper.close();
		}
		return ret;
	}
	public static boolean deleteWorker(DatabaseHelper dbHelper, Worker worker){
		//Delete worker by local id or Trello id
		//Used by MyTrelloContentProvider
		
		boolean ret = false;
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		if(worker.getId() == null && (worker.getRemote_id() == null || worker.getRemote_id().length() == 0)) {
			//Can't delete without an id
			ret = false;
		} else {
			//DELETE
			//If have id, lookup by that, it's fastest
			String where;
			String[] args = null;
			if(worker.getId() != null){
				where = TableWorkers.COL_ID + " = " + Integer.toString(worker.getId());
			} else {
				args = new String[]{worker.getRemote_id()};
				where = TableWorkers.COL_REMOTE_ID + " = ?";
			}
			database.delete(TableWorkers.TABLE_NAME, where, args);
			ret = true;
		}
		database.close();
		dbHelper.close();
		return ret;
	}
	public static boolean deleteAll(DatabaseHelper dbHelper){
		//Deleted all workers in the db
		//Used by MyTrelloContentProvider
		
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(TableWorkers.TABLE_NAME, null, null);
		database.close();
		dbHelper.close();
		return true;
	}
}
