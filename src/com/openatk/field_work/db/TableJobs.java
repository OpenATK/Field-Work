package com.openatk.field_work.db;

import java.util.Date;

import com.openatk.field_work.models.Job;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableJobs {
	// Database table
	public static final String TABLE_NAME = "jobs";
	public static final String COL_ID = "_id";
	public static final String COL_REMOTE_ID = "remote_id";
	public static final String COL_FIELD_NAME = "field_name";

	
	public static final String COL_OPERATION_ID = "operation_id";
	public static final String COL_OPERATION_ID_CHANGED = "operation_id_changed";

	public static final String COL_DATE_OF_OPERATION = "date_of_operation";
	public static final String COL_DATE_OF_OPERATION_CHANGED = "date_of_operation_changed";

	public static final String COL_WORKER_NAME = "worker_name";
	public static final String COL_WORKER_NAME_CHANGED = "worker_name_changed";

	public static final String COL_STATUS = "status";
	public static final String COL_STATUS_CHANGED = "status_changed";

	public static final String COL_COMMENTS = "comments";
	public static final String COL_COMMENTS_CHANGED = "comments_changed";

	public static final String COL_DELETED = "deleted";
	public static final String COL_DELETED_CHANGED = "deleted_changed";


	public static String[] COLUMNS = { COL_ID, COL_REMOTE_ID, COL_FIELD_NAME, 
		COL_OPERATION_ID, COL_OPERATION_ID_CHANGED, COL_DATE_OF_OPERATION, 
		COL_DATE_OF_OPERATION_CHANGED, COL_WORKER_NAME, COL_WORKER_NAME_CHANGED, 
		COL_STATUS, COL_STATUS_CHANGED, COL_COMMENTS, COL_COMMENTS_CHANGED,
		COL_DELETED, COL_DELETED_CHANGED };

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " 
	      + TABLE_NAME
	      + "(" 
	      + COL_ID + " integer primary key autoincrement," 
	      + COL_REMOTE_ID + " text default ''," 
	      + COL_FIELD_NAME + " text," 
	      + COL_OPERATION_ID + " integer,"
	      + COL_OPERATION_ID_CHANGED + " text,"
	      + COL_DATE_OF_OPERATION + " text,"
	      + COL_DATE_OF_OPERATION_CHANGED + " text,"
	      + COL_WORKER_NAME + " text,"
	      + COL_WORKER_NAME_CHANGED + " text,"
	      + COL_STATUS + " integer,"
	      + COL_STATUS_CHANGED + " text,"
	      + COL_COMMENTS + " text,"
	      + COL_COMMENTS_CHANGED + " text,"
	      + COL_DELETED + " integer default 0,"
	      + COL_DELETED_CHANGED + " text"
	      + ");";

	public static void onCreate(SQLiteDatabase database) {
	  database.execSQL(DATABASE_CREATE);
	}

	//TODO handle upgrade
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.d("TableJobs - onUpgrade", "Upgrade from " + Integer.toString(oldVersion) + " to " + Integer.toString(newVersion));
    	int version = oldVersion;
    	switch(version){
    		case 1: //Launch
    			//Do nothing this is the gplay launch version
    		case 2: //V2
    			//Added COL_DELETED
    			database.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_DELETED + " integer default 0");
    	}
	   //database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	   //onCreate(database);
	}
	
	public static Job cursorToJob(Cursor cursor) {
		if(cursor != null){
			Integer id = cursor.getInt(cursor.getColumnIndex(TableJobs.COL_ID));
			String remote_id = cursor.getString(cursor.getColumnIndex(TableJobs.COL_ID));
			String fieldName = cursor.getString(cursor.getColumnIndex(TableJobs.COL_ID));

			Integer operationId = cursor.getInt(cursor.getColumnIndex(TableJobs.COL_ID));
			Date dateOperationIdChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_OPERATION_ID_CHANGED)));

			Date dateOfOperation = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_DATE_OF_OPERATION)));
			Date dateDateOfOperationChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_DATE_OF_OPERATION_CHANGED)));

			String workerName = cursor.getString(cursor.getColumnIndex(TableJobs.COL_ID));
			Date dateWorkerNameChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_WORKER_NAME_CHANGED)));

			
			Integer status = cursor.getInt(cursor.getColumnIndex(TableJobs.COL_ID));
			Date dateStatusChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_STATUS_CHANGED)));

			String comments = cursor.getString(cursor.getColumnIndex(TableJobs.COL_ID));
			Date dateCommentsChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_COMMENTS_CHANGED)));

			Boolean deleted = cursor.getInt(cursor.getColumnIndex(TableJobs.COL_DELETED)) == 1 ? true : false;
			Date dateDeletedChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_DELETED_CHANGED)));
			
			Job newJob = new Job(id, remote_id, fieldName,
					operationId, dateOperationIdChanged,
					dateOfOperation, dateDateOfOperationChanged,
					workerName, dateWorkerNameChanged,
					status, dateStatusChanged, comments,
					dateCommentsChanged, deleted, dateDeletedChanged);
			
			return newJob;
		} else {
			return null;
		}
	}
	
	
}
