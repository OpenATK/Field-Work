package com.openatk.field_work.models;

import java.util.Date;

public class Job {
	
	public static Integer STATUS_NOT_PLANNED = 0;
	public static Integer STATUS_PLANNED = 1;
	public static Integer STATUS_STARTED = 2;
	public static Integer STATUS_DONE = 3;
	
	private Integer id = null;
	private String remote_id = null;
	
	private String fieldName = null;
	private Date dateFieldNameChanged = null;

	private Integer operationId = null;
	private Date dateOperationIdChanged = null;

	private Date dateOfOperation = null;
	private Date dateDateOfOperationChanged = null;

	private String workerName = null;
	private Date dateWorkerNameChanged = null;

	
	private Integer status = null;
	private Date dateStatusChanged = null;

	private String comments = null;
	private Date dateCommentsChanged = null;

	private Boolean deleted = null;
	private Date dateDeletedChanged = null;
	
	
	public Job(){
		//Used for updating
		this.operationId = null;
		this.status = null;
		this.deleted = null;
		this.comments = null;
		this.workerName = null;
		this.fieldName = null;
	}
	
	public Job(Object makeNull){
		if(makeNull == null){
			this.remote_id = null;
			this.fieldName = null;
			this.dateFieldNameChanged = null;
			this.operationId = null;
			this.dateOperationIdChanged = null;
			this.dateOfOperation = null;
			this.dateDateOfOperationChanged = null;
			this.workerName = null;
			this.dateWorkerNameChanged = null;			
			this.status = null;
			this.dateStatusChanged = null;
			this.comments = null;
			this.dateCommentsChanged = null;
			this.deleted = null;
			this.dateDeletedChanged = null;
		}
	}
	
	public Job(int operationId, String fieldName){
		//Used for creating a new job locally
		this.status = STATUS_NOT_PLANNED;
		this.dateStatusChanged = new Date();
		this.deleted = false;
		this.dateDeletedChanged = new Date();
		this.comments = "";
		this.dateCommentsChanged = new Date();
		this.operationId = operationId;
		this.dateOperationIdChanged = new Date();
		this.fieldName = fieldName;
		this.dateFieldNameChanged = new Date();
	}
	
	public Job(Integer id, String remote_id, String fieldName, Date dateFieldNameChanged, Integer operationId,
			Date dateOperationIdChanged, Date dateOfOperation,
			Date dateDateOfOperationChanged, String workerName,
			Date dateWorkerNameChanged, Integer status,
			Date dateStatusChanged, String comments, Date dateCommentsChanged,
			Boolean deleted, Date dateDeletedChanged) {
		super();
		this.id = id;
		this.remote_id = remote_id;
		this.operationId = operationId;
		this.dateOperationIdChanged = dateOperationIdChanged;
		this.dateOfOperation = dateOfOperation;
		this.dateDateOfOperationChanged = dateDateOfOperationChanged;
		this.workerName = workerName;
		this.dateWorkerNameChanged = dateWorkerNameChanged;
		this.fieldName = fieldName;
		this.dateFieldNameChanged = dateFieldNameChanged;
		this.status = status;
		this.dateStatusChanged = dateStatusChanged;
		this.comments = comments;
		this.dateCommentsChanged = dateCommentsChanged;
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
	public Integer getOperationId() {
		return operationId;
	}
	public void setOperationId(Integer operationId) {
		this.operationId = operationId;
	}
	public Date getDateOperationIdChanged() {
		return dateOperationIdChanged;
	}
	public void setDateOperationIdChanged(Date dateOperationIdChanged) {
		this.dateOperationIdChanged = dateOperationIdChanged;
	}
	public Date getDateOfOperation() {
		return dateOfOperation;
	}
	public void setDateOfOperation(Date dateOfOperation) {
		this.dateOfOperation = dateOfOperation;
	}
	public Date getDateDateOfOperationChanged() {
		return dateDateOfOperationChanged;
	}
	public void setDateDateOfOperationChanged(Date dateDateOfOperationChanged) {
		this.dateDateOfOperationChanged = dateDateOfOperationChanged;
	}
	public String getWorkerName() {
		return workerName;
	}
	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}
	public Date getDateWorkerNameChanged() {
		return dateWorkerNameChanged;
	}
	public void setDateWorkerNameChanged(Date dateWorkerNameChanged) {
		this.dateWorkerNameChanged = dateWorkerNameChanged;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public Date getDateFieldNameChanged() {
		return dateFieldNameChanged;
	}
	public void setDateFieldNameChanged(Date dateFieldNameChanged) {
		this.dateFieldNameChanged = dateFieldNameChanged;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getDateStatusChanged() {
		return dateStatusChanged;
	}
	public void setDateStatusChanged(Date dateStatusChanged) {
		this.dateStatusChanged = dateStatusChanged;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Date getDateCommentsChanged() {
		return dateCommentsChanged;
	}
	public void setDateCommentsChanged(Date dateCommentsChanged) {
		this.dateCommentsChanged = dateCommentsChanged;
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

	
}
