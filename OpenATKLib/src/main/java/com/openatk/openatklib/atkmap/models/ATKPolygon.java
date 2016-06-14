package com.openatk.openatklib.atkmap.models;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.openatk.openatklib.atkmap.views.ATKPolygonViewOptions;

public class ATKPolygon extends ATKModel {
	public List<LatLng> boundary;
	//TODO add holes
	public String label;
	public ATKPolygonViewOptions viewOptions;
	
	public ATKPolygon(Object id){
		this.id = id;
		this.boundary = new ArrayList<LatLng>();
		this.label = "";
		this.viewOptions = new ATKPolygonViewOptions();
	}
	public ATKPolygon(Object id, String label){
		this.id = id;
		this.boundary = new ArrayList<LatLng>();
		this.label = "";
		if(label != null){
			this.label = label;
		} else {
			this.label = "";
		}
		this.viewOptions = new ATKPolygonViewOptions();
	}
	public ATKPolygon(Object id, List<LatLng> boundary){
		this.id = id;
		this.boundary = boundary;
		this.label = "";
		this.viewOptions = new ATKPolygonViewOptions();
	}
	public ATKPolygon(Object id, List<LatLng> boundary, ATKPolygonViewOptions options){
		this.id = id;
		this.boundary = boundary;
		this.label = "";
		this.viewOptions = options;
	}
	public ATKPolygon(Object id, List<LatLng> boundary, String label){
		this.id = id;
		this.boundary = boundary;
		if(label != null){
			this.label = label;
		} else {
			this.label = "";
		}
		this.viewOptions = new ATKPolygonViewOptions();
	}
	public ATKPolygon(Object id, List<LatLng> boundary, String label, ATKPolygonViewOptions options){
		this.id = id;
		this.boundary = boundary;
		if(label != null){
			this.label = label;
		} else {
			this.label = "";
		}
		this.viewOptions = options;
	}
}
