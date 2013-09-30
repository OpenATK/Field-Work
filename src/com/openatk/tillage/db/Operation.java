package com.openatk.tillage.db;
import android.database.Cursor;

public class Operation {
	private Integer id = null;
	private String remote_id = null;
	private Integer hasChanged = 0;
	private String name = "";

	public Operation(){
		
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

	public static Operation cursorToOperation(Cursor cursor) {
		if(cursor != null){
			Operation operation = new Operation();
			operation.setId(cursor.getInt(cursor.getColumnIndex(TableOperations.COL_ID)));
			operation.setRemote_id(cursor.getString(cursor.getColumnIndex(TableOperations.COL_REMOTE_ID)));
			operation.setHasChanged(cursor.getInt(cursor.getColumnIndex(TableOperations.COL_HAS_CHANGED)));
			operation.setName(cursor.getString(cursor.getColumnIndex(TableOperations.COL_NAME)));
			return operation;
		} else {
			return null;
		}
	}
	
	public String toString(){
		return name;
	}
}
