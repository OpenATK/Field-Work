package com.openatk.openatklib.atkmap.models;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class ATKPointCluster extends ATKPoint  {
	public List<ATKPoint> points = new ArrayList<ATKPoint>();

	public Object id = null;
	
	public ATKPointCluster(Object id) {
		super(id);
		points.add(this);
	}

	public ATKPointCluster(Object id, LatLng position){
		super(id, position);
		points.add(this);
	}
	
	public void add(ATKPoint point){
		points.add(point);
	}
	
	public void remote(ATKPoint point){
		points.remove(point);
	}
	
}
