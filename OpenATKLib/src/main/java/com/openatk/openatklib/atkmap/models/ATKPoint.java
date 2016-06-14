package com.openatk.openatklib.atkmap.models;

import com.google.android.gms.maps.model.LatLng;

public class ATKPoint extends ATKModel {
	public LatLng position;
	
	public ATKPoint(Object id){
		this.id = id;
	}
	public ATKPoint(Object id, LatLng position){
		this.id = id;
		this.position = position;
	}
}
