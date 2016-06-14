package com.openatk.libtrello;

import java.util.Date;

public class TrelloSyncInfo {
	private Boolean autoSync = null;
	private Boolean sync = null;
	private Date lastSync = null;
	private Integer interval = null;
	
	public TrelloSyncInfo() {

	}

	
	public TrelloSyncInfo(Boolean autoSync, Boolean sync, Date lastSync,
			Integer interval) {
		super();
		this.autoSync = autoSync;
		this.sync = sync;
		this.lastSync = lastSync;
		this.interval = interval;
	}


	public Boolean getAutoSync() {
		return autoSync;
	}
	public void setAutoSync(Boolean autoSync) {
		this.autoSync = autoSync;
	}
	public Boolean getSync() {
		return sync;
	}
	public void setSync(Boolean sync) {
		this.sync = sync;
	}
	public Date getLastSync() {
		return lastSync;
	}
	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}
	public Integer getInterval() {
		return interval;
	}
	public void setInterval(Integer interval) {
		this.interval = interval;
	}
	

	
	
}
