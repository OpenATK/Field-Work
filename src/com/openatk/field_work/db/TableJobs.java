package com.openatk.field_work.db;

import java.util.Date;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Job;
import com.openatk.field_work.models.Worker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableJobs {
	// Database table
	public static final String TABLE_NAME = "jobs";
	public static final String COL_ID = "_id";
	public static final String COL_REMOTE_ID = "remote_id";
	
	public static final String COL_FIELD_NAME = "field_name";
	public static final String COL_FIELD_NAME_CHANGED = "field_name_changed";

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


	public static String[] COLUMNS = { COL_ID, COL_REMOTE_ID, COL_FIELD_NAME, COL_FIELD_NAME_CHANGED,
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
	      + COL_FIELD_NAME_CHANGED + " text,"
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
    	int version = oldVersion + 1;
    	switch(version){
    		case 1: //Launch
    			//Do nothing this is the gplay launch version
    		case 2: //V2
    			//Added COL_DELETED
    			database.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_DELETED + " integer default 0");
    		case 3:
    			Log.d("TableJobs - onUpgrade", "upgarding to v3");
    			database.beginTransaction();
    			try {
        			database.execSQL("create table backup(_id, remote_id, operation_id, date_of_operation, worker_name, field_name, status, comments, deleted)");
        			database.execSQL("insert into backup select _id, remote_id, operation_id, date_of_operation, worker_name, field_name, status, comments, deleted from jobs");
        			database.execSQL("drop table jobs");
        			database.execSQL(DATABASE_CREATE);
        			database.execSQL("insert into " + TABLE_NAME + " (_id, remote_id, operation_id, date_of_operation, worker_name, field_name, status, comments, deleted) select _id, remote_id, operation_id, date_of_operation, worker_name, field_name, status, comments, deleted from backup");
        			database.execSQL("drop table backup");
        			database.setTransactionSuccessful();
    			} finally {
    				database.endTransaction();
    			}
    			Cursor cursor = database.query(TableJobs.TABLE_NAME, TableJobs.COLUMNS, null, null, null, null, null);
    			while(cursor.moveToNext()) {
    				int id = cursor.getInt(cursor.getColumnIndex(TableJobs.COL_ID));
    				//Update in db
    				ContentValues values = new ContentValues();
    				values.put(TableJobs.COL_COMMENTS_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				values.put(TableJobs.COL_DATE_OF_OPERATION_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				values.put(TableJobs.COL_DELETED_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				values.put(TableJobs.COL_FIELD_NAME_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				values.put(TableJobs.COL_STATUS_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				values.put(TableJobs.COL_WORKER_NAME_CHANGED, DatabaseHelper.dateToStringUTC(new Date(0)));
    				database.update(TableJobs.TABLE_NAME, values, TableJobs.COL_ID + " = " + Integer.toString(id), null);
    			}
    			cursor.close();
    			
    	}
	   //database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	   //onCreate(database);
	}
	
	public static Job cursorToJob(Cursor cursor) {
		if(cursor != null){
			Integer id = cursor.getInt(cursor.getColumnIndex(TableJobs.COL_ID));
			String remote_id = cursor.getString(cursor.getColumnIndex(TableJobs.COL_REMOTE_ID));
			
			String fieldName = cursor.getString(cursor.getColumnIndex(TableJobs.COL_FIELD_NAME));
			Date dateFieldNameChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_FIELD_NAME_CHANGED)));

			Integer operationId = cursor.getInt(cursor.getColumnIndex(TableJobs.COL_OPERATION_ID));
			Date dateOperationIdChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_OPERATION_ID_CHANGED)));

			Date dateOfOperation = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_DATE_OF_OPERATION)));
			Date dateDateOfOperationChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_DATE_OF_OPERATION_CHANGED)));

			String workerName = cursor.getString(cursor.getColumnIndex(TableJobs.COL_WORKER_NAME));
			Date dateWorkerNameChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_WORKER_NAME_CHANGED)));

			
			Integer status = cursor.getInt(cursor.getColumnIndex(TableJobs.COL_STATUS));
			Date dateStatusChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_STATUS_CHANGED)));

			String comments = cursor.getString(cursor.getColumnIndex(TableJobs.COL_COMMENTS));
			Date dateCommentsChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_COMMENTS_CHANGED)));

			Boolean deleted = cursor.getInt(cursor.getColumnIndex(TableJobs.COL_DELETED)) == 1 ? true : false;
			Date dateDeletedChanged = DatabaseHelper.stringToDateUTC(cursor.getString(cursor.getColumnIndex(TableJobs.COL_DELETED_CHANGED)));
			
			Job newJob = new Job(id, remote_id, fieldName, dateFieldNameChanged,
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
	
	
	public static Job FindJobByFieldName(DatabaseHelper dbHelper, String name, int idOperation) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		// Find job
		Job theJob = null;
		String where = TableJobs.COL_FIELD_NAME + "= ? AND " + TableJobs.COL_OPERATION_ID + " = " + Integer.toString(idOperation) + " AND " + TableJobs.COL_DELETED + " = 0";
		Cursor cursor = database.query(TableJobs.TABLE_NAME, TableJobs.COLUMNS, where, new String[]{name}, null, null, null);
		if (cursor.moveToFirst()) {
			theJob = TableJobs.cursorToJob(cursor);
		}
		cursor.close();
		dbHelper.close();
		return theJob;
	}

	public static Job FindJobById(DatabaseHelper dbHelper, Integer id) {
		if (id != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find job
			Job theJob = null;
			String where = TableJobs.COL_ID + " = " + Integer.toString(id) + " AND " + TableJobs.COL_DELETED + " = 0";
			Cursor cursor = database.query(TableJobs.TABLE_NAME, TableJobs.COLUMNS, where, null, null, null, null);
			if (cursor.moveToFirst()) {
				theJob = TableJobs.cursorToJob(cursor);
			}
			cursor.close();
			dbHelper.close();
			return theJob;
		} else {
			return null;
		}
	}
	
	public static Job FindJobByRemoteId(DatabaseHelper dbHelper, String id) {
		if (id != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find job
			Job theJob = null;
			String where = TableJobs.COL_ID + " = '" + id + "'";
			Cursor cursor = database.query(TableJobs.TABLE_NAME, TableJobs.COLUMNS, where, null, null, null, null);
			if (cursor.moveToFirst()) {
				theJob = TableJobs.cursorToJob(cursor);
			}
			cursor.close();
			dbHelper.close();
			return theJob;
		} else {
			return null;
		}
	}
	
	public static boolean updateJobsWithFieldName(DatabaseHelper dbHelper, String oldName, String newName){
		if(oldName == null || newName == null) return false;
		
		ContentValues values = new ContentValues();
		values.put(TableJobs.COL_FIELD_NAME, newName);
		values.put(TableJobs.COL_FIELD_NAME_CHANGED,  DatabaseHelper.dateToStringUTC(new Date()));

		
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		//UPDATE
		String where = TableJobs.COL_FIELD_NAME + " = ?";
		database.update(TableJobs.TABLE_NAME, values, where, new String[] { oldName });
		database.close();
		dbHelper.close();
		
		return true;
	}
	
	public static boolean updateJob(DatabaseHelper dbHelper, Job job){
		//Inserts, updates
		//Only non-null fields are updated
		//Used by both LibTrello and MainActivity to update database data
		
		boolean ret = false;
		
		ContentValues values = new ContentValues();
		if(job.getRemote_id() != null) values.put(TableJobs.COL_REMOTE_ID, job.getRemote_id());
		
		if(job.getFieldName() != null) values.put(TableJobs.COL_FIELD_NAME, job.getFieldName());
		if(job.getDateFieldNameChanged() != null) values.put(TableJobs.COL_FIELD_NAME_CHANGED, DatabaseHelper.dateToStringUTC(job.getDateFieldNameChanged()));
		
		if(job.getDateCommentsChanged() != null) values.put(TableJobs.COL_COMMENTS_CHANGED, DatabaseHelper.dateToStringUTC(job.getDateCommentsChanged()));
		if(job.getComments() != null) values.put(TableJobs.COL_COMMENTS, job.getComments());
		
		if(job.getDateOfOperation() != null) values.put(TableJobs.COL_DATE_OF_OPERATION, DatabaseHelper.dateToStringUTC(job.getDateOfOperation()));
		if(job.getDateDateOfOperationChanged() != null) values.put(TableJobs.COL_DATE_OF_OPERATION_CHANGED, DatabaseHelper.dateToStringUTC(job.getDateDateOfOperationChanged()));
		
		if(job.getOperationId() != null) values.put(TableJobs.COL_OPERATION_ID, job.getOperationId());
		if(job.getDateOperationIdChanged() != null) values.put(TableJobs.COL_OPERATION_ID_CHANGED, DatabaseHelper.dateToStringUTC(job.getDateOperationIdChanged()));
		
		if(job.getWorkerName() != null) values.put(TableJobs.COL_WORKER_NAME, job.getWorkerName());
		if(job.getDateWorkerNameChanged() != null) values.put(TableJobs.COL_WORKER_NAME_CHANGED, DatabaseHelper.dateToStringUTC(job.getDateWorkerNameChanged()));
		
		if(job.getStatus() != null) values.put(TableJobs.COL_STATUS, job.getStatus());
		if(job.getDateStatusChanged() != null) values.put(TableJobs.COL_STATUS_CHANGED, DatabaseHelper.dateToStringUTC(job.getDateStatusChanged()));
		
		if(job.getDeleted() != null) values.put(TableJobs.COL_DELETED, (job.getDeleted() == false ? 0 : 1));
		if(job.getDateDeletedChanged() != null) values.put(TableJobs.COL_DELETED_CHANGED, DatabaseHelper.dateToStringUTC(job.getDateDeletedChanged()));

		if(values.size() > 0){
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			if(job.getId() == null) {
				//INSERT This is a new worker, has no id's
				int id = (int) database.insert(TableJobs.TABLE_NAME, null, values);
				job.setId(id);
				Log.d("TableJobs", "INSERT id:" + Integer.toString(id) +  " fieldname:" + job.getFieldName());
				ret = true;
			} else {
				//UPDATE
				//If have id, lookup by that, it's fastest
				if(job.getId() != null) Log.d("TableJobs", "Update id:" + Integer.toString(job.getId()));
				if(job.getStatus() != null) Log.d("TableJobs", "status:" + Integer.toString(job.getStatus()));
				if(job.getDeleted() != null) Log.d("TableJobs", "deleted:" + Boolean.toString(job.getDeleted()));
	
				String where = TableJobs.COL_ID + " = " + Integer.toString(job.getId());
				
				database.update(TableJobs.TABLE_NAME, values, where, null);
				ret = true;
			}
			
			database.close();
			dbHelper.close();
		}
		return ret;
	}
	public static boolean deleteJob(DatabaseHelper dbHelper, Job job){
		//Delete job by local id or Trello id
		//Used by MyTrelloContentProvider
		
		boolean ret = false;
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		if(job.getId() == null && (job.getRemote_id() == null || job.getRemote_id().length() == 0)) {
			//Can't delete without an id
			ret = false;
		} else {
			//DELETE
			//If have id, lookup by that, it's fastest
			String where;
			String[] args = null;
			if(job.getId() != null){
				where = TableJobs.COL_ID + " = " + Integer.toString(job.getId());
			} else {
				where = TableJobs.COL_REMOTE_ID + " = ?";
				args = new String[] { job.getRemote_id() };
			}
			database.delete(TableJobs.TABLE_NAME, where, args);
			ret = true;
		}
		database.close();
		dbHelper.close();
		return ret;
	}
	public static boolean deleteAll(DatabaseHelper dbHelper){
		//Deleted all jobs in the db
		//Used by MyTrelloContentProvider
		
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(TableJobs.TABLE_NAME, null, null);
		database.close();
		dbHelper.close();
		return true;
	}
	public static boolean deleteAllWithOperationId(DatabaseHelper dbHelper, Integer operationId){
		//Deleted all jobs in the db
		//Used by MyTrelloContentProvider
		String where = TableJobs.COL_OPERATION_ID + " = " + Integer.toString(operationId);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(TableJobs.TABLE_NAME, where, null);
		database.close();
		dbHelper.close();
		return true;
	}
	
}
