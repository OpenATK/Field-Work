package com.openatk.openatklib.atkmap.views;


import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.openatk.openatklib.atkmap.listeners.ATKPolygonClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPolygonDrawListener;
import com.openatk.openatklib.atkmap.models.ATKPolygon;

public class ATKPolygonView {
	//Data
	private ATKPolygon polygon;
	private ATKPolygonViewOptions viewOptions;

	//References
	private GoogleMap map;
	
	private Polygon mapPolygon;
	private Marker mapLabelMarker;
	
	private BitmapDescriptor iconLabel;
	private BitmapDescriptor iconLabelSelected;

	private PolygonOptions polygonOptions;
	private ATKPolygonClickListener clickListener;
	private ATKPolygonDrawListener drawListener;

	private Object userData;
	
	private String currentLabel;

	
	public ATKPolygonView(GoogleMap map, ATKPolygon polygon){
		this.map = map;
		this.polygon = polygon;
		this.viewOptions = polygon.viewOptions;
		this.setLabel(polygon.label);
		this.drawPolygon();
	}
	
	public void setOnClickListener(ATKPolygonClickListener clickListener){
		this.clickListener = clickListener;
	}
	
	public void setOnDrawListener(ATKPolygonDrawListener drawListener){ //protected?
		this.drawListener = drawListener;
	}
	
	public ATKPolygonDrawListener getOnDrawListener(){ //protected?
		return this.drawListener;
	}
	
	public ATKPolygon getAtkPolygon(){
		return polygon;
	}
	
	public void setAtkPolygon(ATKPolygon polygon){
		this.polygon = polygon; //If the whole model changed
		this.drawPolygon();
	}
	
	public void update(){
		this.drawPolygon();
	}
	
	public void remove(){
		if(this.mapLabelMarker != null) {
			this.mapLabelMarker.remove();
			this.mapLabelMarker = null;
		}
		if(this.mapPolygon != null){
			this.mapPolygon.remove();
			this.mapPolygon = null;
		}
	}
	
	public void hide(){
		this.viewOptions.setVisible(false);
		if(this.mapPolygon != null){
			this.mapPolygon.setVisible(false);
		}
	}
	
	public void show(){
		this.viewOptions.setVisible(true);
		if(this.mapPolygon != null){
			this.mapPolygon.setVisible(true);
		}
	}
	
	public void setStrokeColor(int color){
		this.viewOptions.setStrokeColor(color);
		if(this.mapPolygon != null) this.mapPolygon.setStrokeColor(this.viewOptions.getStrokeColor());
	}
	
	public void setStrokeColor(float alpha, int red, int green, int blue){
		this.viewOptions.setStrokeColor(Color.argb((int)(alpha * 255),  red, green, blue));
		if(this.mapPolygon != null) this.mapPolygon.setStrokeColor(this.viewOptions.getStrokeColor());
	}
	
	public void setFillColor(int color){
		this.viewOptions.setFillColor(color);
		if(this.mapPolygon != null) this.mapPolygon.setFillColor(this.viewOptions.getFillColor());
	}
	
	public void setFillColor(float alpha, int red, int green, int blue){
		this.viewOptions.setFillColor(Color.argb((int)(alpha * 255),  red, green, blue));
		if(this.mapPolygon != null) this.mapPolygon.setFillColor(this.viewOptions.getFillColor());
	}
	
	public void setStrokeWidth(float width){
		this.viewOptions.setStrokeWidth(width);
		if(this.mapPolygon != null) this.mapPolygon.setStrokeWidth(this.viewOptions.getStrokeWidth());
	}

	public void setOpacity(float opacity){
		int fillcolor = this.viewOptions.getFillColor();
		this.viewOptions.setFillColor(Color.argb((int)(opacity * 255), Color.red(fillcolor), Color.green(fillcolor), Color.blue(fillcolor)));
		if(this.mapPolygon != null) this.mapPolygon.setFillColor(this.viewOptions.getFillColor());
	}
	
	public void setZIndex(float zindex){
		this.viewOptions.setZindex(zindex);
		if(this.mapPolygon != null) this.mapPolygon.setZIndex(this.viewOptions.getZindex());
	}
	
	public float getZIndex(){
		return this.viewOptions.getZindex();
	}
	
	public boolean wasClicked(Point point){ //TODO protected?
		//Returns true if clicked, false otherwise	
		//TODO Speed improvement, store bounding box.
		//Convert latlngs to points
		List<Point> pointsBoundary = new ArrayList<Point>();
		Projection proj = map.getProjection();
		for(int i=0; i<this.polygon.boundary.size(); i++){
			Point aPoint = proj.toScreenLocation(this.polygon.boundary.get(i));
			pointsBoundary.add(aPoint);
		}
		if(isPointInPolygon(point, pointsBoundary)){
			return true;
		}
		return false;
	}
	
	public boolean wasClicked(LatLng point){ //TODO protected?
		//Returns null if wasn't clicked, true or false if clicked depending if we consumed it		
		Point position = this.map.getProjection().toScreenLocation(point);
		return this.wasClicked(position);
	}
	
	public boolean click(){ //TODO protected?
		//Returns true or false depending if listener consumed the click event		
		if(this.clickListener != null){
			return this.clickListener.onPolygonClick(this); //Return if we consumed the click
		}
		return false;
	}
		
	public boolean labelWasClicked(Marker marker){
		if(mapLabelMarker != null && this.mapLabelMarker.equals(marker)){
			return true;
		}
		return false;
	}
	
	private void drawPolygon(){
		if(this.polygon.boundary != null && this.polygon.boundary.size() > 0){
			if(this.mapPolygon == null){
				//Setup options
				this.polygonOptions = new PolygonOptions();			
				this.polygonOptions.addAll(polygon.boundary);
				this.polygonOptions.strokeColor(this.viewOptions.getStrokeColor());
				this.polygonOptions.strokeWidth(this.viewOptions.getStrokeWidth());
				this.polygonOptions.fillColor(this.viewOptions.getFillColor());
				this.polygonOptions.visible(this.viewOptions.isVisible());
				this.polygonOptions.zIndex(this.viewOptions.getZindex());
				this.mapPolygon = map.addPolygon(this.polygonOptions);
			} else {
				this.mapPolygon.setPoints(this.polygon.boundary);
			}
		} else {
			//Model doesn't have a boundary remove the polygon from the map
			if(this.mapPolygon != null) this.mapPolygon.remove();
			this.mapPolygon = null;
		}
		this.drawLabel();
	}
	
	private void drawLabel(){
		if(this.polygon.label != null && this.polygon.label.length() > 0 && this.iconLabel != null && this.iconLabelSelected != null && this.polygon.boundary != null && this.polygon.boundary.size() > 2){
			if(this.currentLabel.contentEquals(this.polygon.label) == false) setLabel(this.polygon.label);
			
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (int i = 0; i < polygon.boundary.size(); i++) {
				builder.include(polygon.boundary.get(i));
			}
			// Have corners
			LatLngBounds boundingBox = builder.build();
			LatLng where = midPoint(boundingBox.northeast, boundingBox.southwest);

			BitmapDescriptor icon;
			if(this.viewOptions.isBlnLabelSelected() == true){
				icon = this.iconLabelSelected;
			} else {
				icon = this.iconLabel;
			}
			
			if(this.mapLabelMarker == null){
				this.mapLabelMarker = map.addMarker(new MarkerOptions().position(where).icon(icon).draggable(false));
			} else {
				//Move the marker label
				this.mapLabelMarker.setPosition(where);
				this.mapLabelMarker.setIcon(icon);
			}
		} else {
			if(this.mapLabelMarker != null) this.mapLabelMarker.remove();
			this.mapLabelMarker = null;
		}
	}
	public void setLabel(String label){
		this.setLabel(label, false);
	}
	public void setLabel(String label, Boolean selected){
		this.currentLabel = label;
		this.polygon.label = label;
		this.viewOptions.setBlnLabelSelected(selected);
		
		if(label == null || label.trim().length() == 0){
			this.drawLabel();
			return;
		}
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Align.LEFT);
		paint.setTextSize(20);
		paint.setStrokeWidth(12);
		
		Rect bounds = new Rect();
		paint.getTextBounds(label, 0, label.length(), bounds);
		
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bitmapSelected = Bitmap.createBitmap(bounds.width() + 5, bounds.height(), conf);
		Bitmap bitmap = Bitmap.createBitmap(bounds.width() + 5, bounds.height(), conf);
		float x = 0;
		float y = -1.0f * bounds.top + (bitmap.getHeight() * 0.06f);
				
		Canvas canvas = new Canvas(bitmap);
		paint.setColor(this.viewOptions.getLabelColor());

		canvas.drawText(label, x, y, paint);
		
		canvas = new Canvas(bitmapSelected);
		paint.setColor(this.viewOptions.getLabelSelectedColor());
		paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);
		canvas.drawText(label, x, y, paint);
		
		this.iconLabel = BitmapDescriptorFactory.fromBitmap(bitmap);
		this.iconLabelSelected = BitmapDescriptorFactory.fromBitmap(bitmapSelected);
		this.drawLabel();
	}

	public void setLabelSelected(boolean selected){
		this.viewOptions.setBlnLabelSelected(selected);
		this.drawLabel();
	}
	
	public void setLabelColor(int color){
		this.viewOptions.setLabelColor(color);
		this.drawLabel();
	}
	public void setLabelColor(float alpha, int red, int green, int blue){
		this.viewOptions.setLabelColor(Color.argb((int)(alpha * 255),  red, green, blue));
		this.drawLabel();
	}
	
	public void setLabelSelectedColor(int color){
		this.viewOptions.setLabelSelectedColor(color);
		this.drawLabel();
	}
	public void setLabelSelectedColor(float alpha, int red, int green, int blue){
		this.viewOptions.setLabelSelectedColor(Color.argb((int)(alpha * 255),  red, green, blue));
		this.drawLabel();
	}
	
	public String getLabel(){
		return this.polygon.label;
	}

	public void setData(Object data){
		this.userData = data;
	}
	
	public Object getData(){
		return this.userData;
	}
	
	private boolean isPointInPolygon(Point tap, List<Point> vertices) {
		int intersectCount = 0;
		if(vertices.size() > 2){
			vertices.add(vertices.get(0)); //End with the first point again so we can check all the sides
		} else {
			return false;
		}
		for (int j = 0; j < vertices.size() - 1; j++) {
			if (rayCastIntersect(tap, vertices.get(j), vertices.get(j + 1))) {
				intersectCount++;
			}
		}
		return ((intersectCount % 2) == 1); // odd = inside, even = outside;
	}

	private boolean rayCastIntersect(Point tap, Point vertA, Point vertB) {
		double aY = vertA.y;
		double bY = vertB.y;
		double aX = vertA.x;
		double bX = vertB.x;
		double pY = tap.y;
		double pX = tap.x;

		if ((aY > pY && bY > pY) || (aY < pY && bY < pY)
				|| (aX < pX && bX < pX)) {
			return false; // a and b can't both be above or below pt.y, and a or
							// b must be east of pt.x
		}
		
		//If both a and b are east of point tapped at this point then we are good to go
		if(aX > pX && bX > pX){
			return true;
		}
		
		//Otherwise we are forced to do math
		double m = (aY - bY) / (aX - bX); // Rise over run
		double bee = (-aX) * m + aY; // y = mx + b
		double x = (pY - bee) / m; // algebra is neat!

		return x > pX;
	}
	
	public static LatLng midPoint(LatLng point1, LatLng point2){
		//Used by drawLabel
	    double dLon = Math.toRadians(point2.longitude - point1.longitude);

	    //convert to radians
	    double lat1 = Math.toRadians(point1.latitude);
	    double lat2 = Math.toRadians(point2.latitude);
	    double lon1 = Math.toRadians(point1.longitude);

	    double Bx = Math.cos(lat2) * Math.cos(dLon);
	    double By = Math.cos(lat2) * Math.sin(dLon);
	    double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
	    double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
	    
	    return(new LatLng(Math.toDegrees(lat3), Math.toDegrees(lon3)));
	}
	
	
	
	public float getAcres(){		
		if(this.polygon.boundary.size() < 3) return 0.0f;
		
		double earth_radius = (double) 6371009.0f; // in meters
		double lat_dist = (double) ((Math.PI * earth_radius) / (double) 180.0f);
		
		Float newArea = 0.0f;
		List<Pair<Double, Double>> xyList = new ArrayList<Pair<Double, Double>>();
		for (int i = 0; i < this.polygon.boundary.size(); i++) {
			//Reproject
			double y = this.polygon.boundary.get(i).latitude * lat_dist;
			double x = this.polygon.boundary.get(i).longitude * lat_dist * Math.cos(Math.toRadians(this.polygon.boundary.get(i).latitude));
			//Save x, y
			xyList.add(new Pair<Double, Double>(x,y));
		}

		boolean haveRef = false;
		double refX = 0.0f;
		double refY = 0.0f;
		double total1 = 0.0f;
		double total2 = 0.0f;
		for(int i = 0; i < (xyList.size()-1); i++){
			Pair<Double, Double> thisVert = xyList.get(i);
			Pair<Double, Double> nextVert = xyList.get(i+1);
			
			if(haveRef == false){
				haveRef = true;
				refX = thisVert.first;
				refY = thisVert.second;
			}
			total1 += ((thisVert.first - refX) * (nextVert.second - refY)); // x(i) * y(i+1)
			total2 += ((thisVert.second - refY) * (nextVert.first - refX)); // y(i) * x(i+1)
		}

		newArea = (float) (total1 - total2);
		newArea = Math.abs(newArea) / (2.0f * 4046.68f);
		
		return newArea;
	}

}
