package com.openatk.field_work.models;
import java.util.Date;


public class Operation {
	private Integer id = null;
	private String remote_id = null;
	private String name = "";
	private Date dateNameChanged = null;
	
	private Boolean deleted = null;
	private Date dateDeletedChanged = null;
	
	
	public Operation(){
		this.remote_id = null;
		this.name = null;
		this.dateNameChanged = null;
		this.deleted = null;
		this.dateDeletedChanged = null;
	}
	
	public Operation(String name) {
		this.id = null;
		this.remote_id = "";
		this.name = name;
		this.dateNameChanged  = new Date();
		this.deleted = false;
		this.dateDeletedChanged = new Date();
	}
	
	public Operation(Integer id, String remote_id, String name,
			Date dateNameChanged, Boolean deleted, Date dateDeletedChanged) {
		super();
		this.id = id;
		this.remote_id = remote_id;
		this.name = name;
		this.dateNameChanged = dateNameChanged;
		this.deleted = deleted;
		this.dateDeletedChanged = dateDeletedChanged;
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getRemote_id() {
		return remote_id;
	}


	public void setRemote_id(String remote_id) {
		this.remote_id = remote_id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Date getDateNameChanged() {
		return dateNameChanged;
	}


	public void setDateNameChanged(Date dateNameChanged) {
		this.dateNameChanged = dateNameChanged;
	}


	public Boolean getDeleted() {
		return deleted;
	}


	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}


	public Date getDateDeletedChanged() {
		return dateDeletedChanged;
	}


	public void setDateDeletedChanged(Date dateDeletedChanged) {
		this.dateDeletedChanged = dateDeletedChanged;
	}

	@Override
	public String toString() {
		return this.getName();
	}
	
	
}
