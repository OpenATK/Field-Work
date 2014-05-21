package com.openatk.field_work.models;

import java.util.Date;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.openatk.openatklib.atkmap.models.ATKPolygon;

public class Field {
	
	private Integer id = null;
	private String remote_id = null;
	
	private String name = "";
	private Date dateNameChanged = null;

	private Float acres = 0.0f;
	private Date dateAcresChanged = null;

	private Boolean deleted = null;
	private Date dateDeleted = null;
	
	private List<LatLng> boundary;
	private Date dateBoundaryChanged = null;

	
	public Field(){
		
	}
	
	public Field(Object makeNull){
		if(makeNull == null){
			this.remote_id = null;
			this.name = null;
			this.dateNameChanged = null;
			this.acres = null;
			this.dateAcresChanged = null;
			this.deleted = null;
			this.dateDeleted = null;
			this.boundary = null;
			this.dateBoundaryChanged = null;
		}
	}
	
	
	public Field(Integer id, String remote_id, String name,
			Date dateNameChanged, Float acres, Date dateAcresChanged,
			Boolean deleted, Date dateDeleted, List<LatLng> boundary,
			Date dateBoundaryChanged) {
		super();
		this.id = id;
		this.remote_id = remote_id;
		this.name = name;
		this.dateNameChanged = dateNameChanged;
		this.acres = acres;
		this.dateAcresChanged = dateAcresChanged;
		this.deleted = deleted;
		this.dateDeleted = dateDeleted;
		this.boundary = boundary;
		this.dateBoundaryChanged = dateBoundaryChanged;
	}




	// Other Getters and setters
	public ATKPolygon getATKPolygon(){
		return new ATKPolygon(this.id, this.boundary);
	}
	
	//---------- Auto generated Getters and setters -------------
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


	public Float getAcres() {
		return acres;
	}


	public void setAcres(Float acres) {
		this.acres = acres;
	}


	public Date getDateAcresChanged() {
		return dateAcresChanged;
	}


	public void setDateAcresChanged(Date dateAcresChanged) {
		this.dateAcresChanged = dateAcresChanged;
	}


	public Boolean getDeleted() {
		return deleted;
	}


	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}


	public Date getDateDeleted() {
		return dateDeleted;
	}


	public void setDateDeleted(Date dateDeleted) {
		this.dateDeleted = dateDeleted;
	}


	public List<LatLng> getBoundary() {
		return boundary;
	}


	public void setBoundary(List<LatLng> boundary) {
		this.boundary = boundary;
	}


	public Date getDateBoundaryChanged() {
		return dateBoundaryChanged;
	}


	public void setDateBoundaryChanged(Date dateBoundaryChanged) {
		this.dateBoundaryChanged = dateBoundaryChanged;
	}


}
