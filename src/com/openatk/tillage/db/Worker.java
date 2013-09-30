package com.openatk.tillage.db;
import android.database.Cursor;

public class Worker {
	private Integer id = null;
	private String remote_id = null;
	private Integer hasChanged = 0;
	private String dateChanged = null;
	private String name = "";

	public Worker(){
		
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

	public String getName() {
		return name;
	}
	
	public String getDateChanged() {
		return dateChanged;
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

	public void setName(String name) {
		this.name = name;
	}
	
	public void setDateChanged(String dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	public static Worker cursorToWorker(Cursor cursor) {
		if(cursor != null){
			Worker me = new Worker();
			me.setId(cursor.getInt(cursor.getColumnIndex(TableWorkers.COL_ID)));
			me.setRemote_id(cursor.getString(cursor.getColumnIndex(TableWorkers.COL_REMOTE_ID)));
			me.setHasChanged(cursor.getInt(cursor.getColumnIndex(TableWorkers.COL_HAS_CHANGED)));
			me.setName(cursor.getString(cursor.getColumnIndex(TableWorkers.COL_NAME)));
			me.setDateChanged(cursor.getString(cursor.getColumnIndex(TableWorkers.COL_DATE_CHANGED)));
			return me;
		} else {
			return null;
		}
	}
	
	public String toString(){
		return name;
	}
}
