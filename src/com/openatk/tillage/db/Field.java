package com.openatk.tillage.db;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.openatk.tillage.drawing.MyPolygon;


public class Field {
	public static float STROKE_WIDTH = 2.0f;
	public static int STROKE_COLOR = Color.BLACK;
	public static int STROKE_SELECTED = Color.WHITE;
	public static int FILL_COLOR = Color.GRAY;
	public static int FILL_COLOR_NOT_PLANNED = Color.parseColor("#babcbe");
	public static int FILL_COLOR_PLANNED = Color.parseColor("#ca575a");
	public static int FILL_COLOR_STARTED = Color.parseColor("#eeb656");
	public static int FILL_COLOR_DONE = Color.parseColor("#80c580");

	private Integer id = null;
	private String remote_id = null;
	private Integer hasChanged = 0;
	private String dateChanged = null;
	private String name = "";
	private Integer acres = 0;
	private List<LatLng> boundary;
	private LatLngBounds boundingBox = null;
	private Double north_lat;
	private Double south_lat;
	private Double west_lng;
	private Double east_lng;
	private Integer deleted = 0;

	private GoogleMap map;
	private MyPolygon polygon = null;

	// TODO holes
	public Field() {
		
	}

	public Field(List<LatLng> boundary, GoogleMap map) {
		this.setBoundary(boundary);
		this.map = map;
	}

	public Boolean wasTouched(LatLng point) {
		if (this.boundingBox != null && this.boundingBox.contains(point)) {
			// Convert to screen coordinate
			Projection proj = map.getProjection();
			Point touchPoint = proj.toScreenLocation(point);

			// Convert boundary to screen coordinate
			List<Point> boundaryPoints = new ArrayList<Point>();
			for (int i = 0; i < boundary.size(); i++) {
				boundaryPoints.add(proj.toScreenLocation(boundary.get(i)));
			}

			// Ray Cast
			return isPointInPolygon(touchPoint, boundaryPoints);
		} else {
			return false;
		}
	}

	private boolean isPointInPolygon(Point tap, List<Point> vertices) {
		int intersectCount = 0;
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

		double m = (aY - bY) / (aX - bX); // Rise over run
		double bee = (-aX) * m + aY; // y = mx + b
		double x = (pY - bee) / m; // algebra is neat!

		return x > pX;
	}
	
	
	public LatLngBounds getBoundingBox(){
		return boundingBox;
	}

	public Integer getId() {
		return id;
	}

	public String getRemote_id() {
		return remote_id;
	}

	public Integer getHasChanged() {
		return hasChanged;
	}

	public String getDateChanged() {
		return dateChanged;
	}

	public String getName() {
		return name;
	}

	public Integer getAcres() {
		return acres;
	}

	public List<LatLng> getBoundary() {
		return boundary;
	}

	public Double getNorth_lat() {
		return north_lat;
	}

	public Double getSouth_lat() {
		return south_lat;
	}

	public Double getWest_lng() {
		return west_lng;
	}

	public Double getEast_lng() {
		return east_lng;
	}
	
	public MyPolygon getPolygon(){
		return this.polygon;
	}
	
	public int getDeleted(){
		return this.deleted;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setRemote_id(String remote_id) {
		this.remote_id = remote_id;
	}

	public void setHasChanged(Integer hasChanged) {
		this.hasChanged = hasChanged;
	}

	public void setDateChanged(String dateChanged) {
		this.dateChanged = dateChanged;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAcres(Integer acres) {
		this.acres = acres;
	}

	public void setBoundary(List<LatLng> boundary) {
		this.boundary = boundary;
		// Init bounds, far north, south, east, west
		// Find corners
		if(boundary != null && boundary.size() > 0){
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (int i = 0; i < boundary.size(); i++) {
				builder.include(boundary.get(i));
			}
	
			// Have corners
			boundingBox = builder.build();
			this.north_lat = boundingBox.northeast.latitude;
			this.south_lat = boundingBox.southwest.latitude;
			this.east_lng = boundingBox.northeast.longitude;
			this.west_lng = boundingBox.southwest.longitude;
		}
	}

	public void setNorth_lat(Double north_lat) {
		this.north_lat = north_lat;
	}

	public void setSouth_lat(Double south_lat) {
		this.south_lat = south_lat;
	}

	public void setWest_lng(Double west_lng) {
		this.west_lng = west_lng;
	}

	public void setEast_lng(Double east_lng) {
		this.east_lng = east_lng;
	}
	
	public void setMap(GoogleMap map) {
		this.map = map;
	}
	
	public void setPolygon(MyPolygon polygon){
		this.polygon = polygon;
	}
	
	public void setDeleted(int deleted){
		this.deleted = deleted;
	}
	
	public static Field cursorToField(Cursor cursor){
		if(cursor != null){
			Field field = new Field();
			field.setId(cursor.getInt(cursor.getColumnIndex(TableFields.COL_ID)));
			field.setRemote_id(cursor.getString(cursor.getColumnIndex(TableFields.COL_REMOTE_ID)));
			field.setHasChanged(cursor.getInt(cursor.getColumnIndex(TableFields.COL_HAS_CHANGED)));
			field.setDateChanged(cursor.getString(cursor.getColumnIndex(TableFields.COL_DATE_CHANGED)));
			String boundary = cursor.getString(cursor.getColumnIndex(TableFields.COL_BOUNDARY));
			field.setBoundary(Field.StringToBoundary(boundary));
			field.setAcres(cursor.getInt(cursor.getColumnIndex(TableFields.COL_ACRES)));
			field.setName(cursor.getString(cursor.getColumnIndex(TableFields.COL_NAME)));
			field.setDeleted(cursor.getInt(cursor.getColumnIndex(TableFields.COL_DELETED)));
			return field;
		} else {
			return null;
		}
	}
	
	public static List<LatLng> StringToBoundary(String boundary){
		StringTokenizer tokens = new StringTokenizer(boundary, ",");
		List<LatLng> points = new ArrayList<LatLng>();
		while (tokens.hasMoreTokens()) {
			String lat = tokens.nextToken();
			String lng = tokens.nextToken();
			points.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
		}
		return points;
	}
	
	public static Field FindFieldByName(SQLiteDatabase database, String name){
		if(name != null){
			//Find current field
			Field theField = null;
			String where = TableFields.COL_NAME + " = '" + name + "'";
			Cursor cursor = database.query(TableFields.TABLE_NAME, TableFields.COLUMNS, where, null, null, null, null);
			if(cursor.moveToFirst()) {
				theField = Field.cursorToField(cursor);
			}
			cursor.close();
			return theField;
		} else {
			return null;
		}
	}
}
