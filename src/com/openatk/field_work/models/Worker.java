package com.openatk.field_work.models;

import java.util.Date;

public class Worker {
	private Integer id = null;
	private String remote_id = null;
	private Date dateNameChanged = null;
	private String name = "";
	
	private Boolean deleted = null;
	private Date dateDeletedChanged = null;

	public Worker() {
		this.remote_id = null;
		this.dateNameChanged = null;
		this. name = null;
		this.deleted = null;
		this.dateDeletedChanged = null;
	}
		
	public Worker(String name) {
		this.name = name;
		this.dateNameChanged = new Date();
	}
	
	public Worker(Date dateNameChanged, String name) {
		this.dateNameChanged = dateNameChanged;
		this.name = name;
	}

	
	public Worker(Integer id, String remote_id, Date dateNameChanged,
			String name, Boolean deleted, Date dateDeletedChanged) {
		super();
		this.id = id;
		this.remote_id = remote_id;
		this.dateNameChanged = dateNameChanged;
		this.name = name;
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
	public Date getDateNameChanged() {
		return dateNameChanged;
	}
	public void setDateNameChanged(Date dateNameChanged) {
		this.dateNameChanged = dateNameChanged;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
