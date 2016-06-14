package com.openatk.openatklib.atkmap.views;

import android.graphics.Color;
import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.openatk.openatklib.atkmap.listeners.ATKPolylineClickListener;
import com.openatk.openatklib.atkmap.models.ATKPolyline;

public class ATKPolylineView {
	private ATKPolyline polyline;
	private GoogleMap map;
	private float zindex;
	
	private ATKPolylineClickListener clickListener;
	
	public ATKPolylineView(GoogleMap map, ATKPolyline polyline){
		this.map = map;
		this.polyline = polyline;
	}
	
	public void setOnClickListener(ATKPolylineClickListener clickListener){
		this.clickListener = clickListener;
	}
	
	public ATKPolyline getAtkPolyline(){
		return polyline;
	}
	
	public void update(){
		
	}
	
	public void remove(){
		
	}
	
	public void hide(){
		
	}
	
	public void show(){
		
	}
	
	public void setColor(Color color){
		
	}
	
	public void setStrokeWidth(float width){
		
	}
	
	public void setZIndex(float zindex){
		this.zindex = zindex;
	}
	
	public float getZIndex(){
		return this.zindex;
	}
	
	
	public boolean wasClicked(Point point){ //TODO protected?
		//Returns true if clicked false otherwise	
		//TODO check if it was clicked
		if(false) return true;
		return false;
	}
	
	public boolean wasClicked(LatLng point){ //TODO protected?
		//Returns true if clicked false otherwise	
		//TODO check if it was clicked
		if(false) return true;
		return false;
	}
	
	public boolean click(){ //TODO protected?
		//Returns true or false depending if listener consumed the click event		
		if(this.clickListener != null){
			return this.clickListener.onPolylineClick(this); //Return if we consumed the click
		}
		return false;
	}
}
