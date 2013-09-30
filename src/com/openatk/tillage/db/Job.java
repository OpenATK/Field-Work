package com.openatk.tillage.db;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Job {
	
	public static Integer STATUS_PLANNED = 3;
	public static Integer STATUS_STARTED = 2;
	public static Integer STATUS_DONE = 1;
	public static Integer STATUS_NOT_PLANNED = 0;

	
	private Integer id = null;
	private String remote_id = null;
	private Integer hasChanged = 0;
	private String dateChanged = null;
	private Integer operationId = 0;
	private String dateOfOperation = null;
	private String workerName = "";

	private String fieldName = "";
	private Integer duration = 0;
	private Float fuelUsed = 0.0f;
	private Integer status = STATUS_NOT_PLANNED;
	private String comments = "";
	private Integer deleted = 0;


	public Job(){
		
	}
	
	public Job(String fieldName){
		this.fieldName = fieldName;		
	}

	public Integer getId() {
		return id;
	}
	

	public String getRemote_id() {
		return remote_id;
	}


	public Integer getHasChanged() {
		return hasChanged;
	}


	public String getDateChanged() {
		return dateChanged;
	}


	public Integer getOperationId() {
		return operationId;
	}


	public String getDateOfOperation() {
		return dateOfOperation;
	}


	public String getWorkerName() {
		return workerName;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public Integer getDuration() {
		return duration;
	}


	public Float getFuelUsed() {
		return fuelUsed;
	}


	public Integer getStatus() {
		return status;
	}


	public String getComments() {
		return comments;
	}
	
	public int getDeleted(){
		return this.deleted;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public void setRemote_id(String remote_id) {
		this.remote_id = remote_id;
	}


	public void setHasChanged(Integer hasChanged) {
		this.hasChanged = hasChanged;
	}


	public void setDateChanged(String dateChanged) {
		this.dateChanged = dateChanged;
	}


	public void setOperationId(Integer operationId) {
		this.operationId = operationId;
	}


	public void setDateOfOperation(String dateOfOperation) {
		this.dateOfOperation = dateOfOperation;
	}


	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}


	public void setDuration(Integer duration) {
		this.duration = duration;
	}


	public void setFuelUsed(Float fuelUsed) {
		this.fuelUsed = fuelUsed;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public void setDeleted(int deleted){
		this.deleted = deleted;
	}
	
	public static Job cursorToJob(Cursor cursor) {
		if(cursor != null){
			Job job = new Job();
			job.setId(cursor.getInt(cursor.getColumnIndex(TableJobs.COL_ID)));
			job.setRemote_id(cursor.getString(cursor.getColumnIndex(TableJobs.COL_REMOTE_ID)));
			job.setHasChanged(cursor.getInt(cursor.getColumnIndex(TableJobs.COL_HAS_CHANGED)));
			job.setDateChanged(cursor.getString(cursor.getColumnIndex(TableJobs.COL_DATE_CHANGED)));
			job.setOperationId(cursor.getInt(cursor.getColumnIndex(TableJobs.COL_OPERATION_ID)));
			job.setDateOfOperation(cursor.getString(cursor.getColumnIndex(TableJobs.COL_DATE_OF_OPERATION)));
			job.setWorkerName(cursor.getString(cursor.getColumnIndex(TableJobs.COL_WORKER_NAME)));
			job.setFieldName(cursor.getString(cursor.getColumnIndex(TableJobs.COL_FIELD_NAME)));
			job.setDuration(cursor.getInt(cursor.getColumnIndex(TableJobs.COL_DURATION)));
			job.setFuelUsed(cursor.getFloat(cursor.getColumnIndex(TableJobs.COL_FUEL_USED)));
			job.setStatus(cursor.getInt(cursor.getColumnIndex(TableJobs.COL_STATUS)));
			job.setComments(cursor.getString(cursor.getColumnIndex(TableJobs.COL_COMMENTS)));
			job.setDeleted(cursor.getInt(cursor.getColumnIndex(TableJobs.COL_DELETED)));
			return job;
		} else {
			return null;
		}
	}
	
}
