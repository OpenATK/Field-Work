package com.openatk.openatklib.atkmap.models;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class ATKPolyline extends ATKModel {
	public List<LatLng> boundary;
	
	public ATKPolyline(Object id){
		this.id = id;
		this.boundary = new ArrayList<LatLng>();
	}
	public ATKPolyline(Object id, List<LatLng> boundary){
		this.id = id;
		this.boundary = boundary;
	}
}
