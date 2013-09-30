package com.openatk.tillage.drawing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.openatk.tillage.R;
import com.openatk.tillage.db.Field;


public class MyPolygon {
	// TODO
	// Prevent Self Intersection

	// Wrapper class for google polygon
	private static float STROKE_WIDTH = 2.0f;
	private static int STROKE_COLOR = Color.BLACK;
	private static int FILL_COLOR = Color.argb(128, 74, 80, 255);

	private Polygon polygon = null;
	private Polyline polyline = null;

	private List<Marker> markers;
	private GoogleMap map;

	private Integer markerSelected = null;

	private BitmapDescriptor iconSelected;
	private BitmapDescriptor icon;

	private MyPolygonListener listener;
	private Marker textMarker = null;
	private String textMarkerString = "";

	
	public interface MyPolygonListener {
		public void MyPolygonUpdateAcres(Float acres);
	}
	
	public MyPolygon(GoogleMap map, Polygon polygon, Activity activity) {
		this.markers = new ArrayList<Marker>();
		this.map = map;
		this.polygon = polygon;
		iconSelected = BitmapDescriptorFactory
				.fromResource(R.drawable.selected_vertex);
		icon = BitmapDescriptorFactory.fromResource(R.drawable.unselected_vertex);
		
		if(activity instanceof MyPolygonListener){
			listener = (MyPolygonListener) activity;
		} else {
			throw new ClassCastException("MyPolygon - activity needs to implement MyPolygonListener");
		}
	}

	public MyPolygon(GoogleMap map, Activity activity) {
		this.markers = new ArrayList<Marker>();
		this.map = map;
		iconSelected = BitmapDescriptorFactory
				.fromResource(R.drawable.selected_vertex);
		icon = BitmapDescriptorFactory.fromResource(R.drawable.unselected_vertex);
		
		if(activity instanceof MyPolygonListener){
			listener = (MyPolygonListener) activity;
		} else {
			throw new ClassCastException("MyPolygon - activity needs to implement MyPolygonListener");
		}
	}

	// Custom functions
	public void select() {
		setStrokeColor(Field.STROKE_SELECTED);
		selectLabel(true);
	}
	public void unselect() {
		setStrokeColor(Field.STROKE_COLOR);
		selectLabel(false);
	}
	public void undo() {
		// Remove selected marker
		if (this.markers.size() != 0) {
			this.markers.get(markerSelected.intValue()).remove();
			this.markers.remove(markerSelected.intValue());
			Integer newSelect = null;
			if (this.markers.size() != 0) {
				newSelect = markerSelected.intValue() - 1;
				if (newSelect < 0) {
					newSelect = 0;
				}
			}
			markerSelected = null;
			if (newSelect != null) {
				selectMarker(newSelect);
			}
			updateShape();
		}
	}

	public void complete() {
		// Remove all markers, and maybe change fill?
		for (int i = 0; i < this.markers.size(); i++) {
			this.markers.get(i).remove();
		}
		this.markers.clear();
		markerSelected = null;
		if(this.polygon != null){
			this.polygon.setStrokeColor(Field.STROKE_SELECTED);
		}
	}

	public void edit() {
		if (polygon != null) {
			List<LatLng> points = polygon.getPoints();
			// Draw markers
			for (int i = 0; i < (points.size() - 1); i++) {
				this.markers.add(map.addMarker(new MarkerOptions()
						.position(points.get(i)).icon(icon).draggable(true).anchor(0.5f, 0.5f)));
			}
			if (this.markers.size() > 0)
				selectMarker(this.markers.size() - 1);
		}
		updateShape();
	}

	public void onMarkerDrag(Marker marker) {
		updateShape();
	}

	public void onMarkerDragEnd(Marker marker) {
		updateShape();
	}

	public void onMarkerDragStart(Marker marker) {
		
	}

	public Boolean onMarkerClick(Marker marker) {
		Integer markerIndex = null;
		for (int i = 0; i < markers.size(); i++) {
			if (markers.get(i).equals(marker)) {
				markerIndex = i;
				break;
			}
		}
		if(markerIndex != null){
			selectMarker(markerIndex);
			return true;
		} else {
			return false;
		}
	}

	public void delete() {
		this.remove();
		if(this.markers != null){
			for (int i = 0; i < this.markers.size(); i++) {
				this.markers.get(i).remove();
			}
			this.markers.clear();
		}
		if(this.polyline != null) this.polyline.remove();
		if(this.textMarker != null) this.textMarker.remove();
	}

	private void selectMarker(Integer markerIndex) {
		if (markerSelected != null) {
			Log.d("MarkerSelected:", Integer.toString(markerSelected));

			// Change icon of last selected marker
			Marker oldMarker = markers.get(markerSelected.intValue());
			this.markers.get(markerSelected.intValue()).remove();
			this.markers.remove(markerSelected.intValue());
			this.markers.add(
					markerSelected.intValue(),
					map.addMarker(new MarkerOptions()
							.position(oldMarker.getPosition()).icon(icon)
							.draggable(true).anchor(0.5f, 0.5f)));
		}
		if (markerIndex != null) {
			Log.d("MarkerIndex:", Integer.toString(markerIndex));
			// Change icon on new selected marker
			Marker oldMarker = this.markers.get(markerIndex.intValue());
			markers.get(markerIndex.intValue()).remove();
			this.markers.remove(markerIndex.intValue());
			this.markers.add(
					markerIndex.intValue(),
					map.addMarker(new MarkerOptions()
							.position(oldMarker.getPosition())
							.icon(iconSelected).draggable(true).anchor(0.5f, 0.5f)));

		}
		Log.d("MarkerSize:", Integer.toString(this.markers.size()));
		markerSelected = markerIndex;
	}

	public void addPoint(LatLng point) {
		// Add marker
		int location = this.markers.size();
		if (markerSelected != null)
			location = markerSelected.intValue() + 1;
		this.markers.add(
				location,
				map.addMarker(new MarkerOptions().position(point).icon(icon)
						.draggable(true).anchor(0.5f, 0.5f)));
		Log.d("MarkersSize:", Integer.toString(this.markers.size()));
		selectMarker(location);
		updateShape();
	}

	public void updatePoints(List<LatLng> arrayLoc){
		if(arrayLoc.size() < 2){
			if (polygon != null) {
				// Remove polygon
				polygon.remove();
				polygon = null;
			}
			if (polyline != null) {
				// remove polyline
				polyline.remove();
				polyline = null;
			}
		} if(arrayLoc.size() == 2) {
			if (polyline == null) {
				// Create polyline
				PolylineOptions lineOptions = new PolylineOptions();
				lineOptions.color(STROKE_COLOR);
				lineOptions.width(STROKE_WIDTH);
				for (int i = 0; i < arrayLoc.size(); i++) {
					lineOptions.add(arrayLoc.get(i));
				}
				// Get back the mutable Polyline
				polyline = map.addPolyline(lineOptions);
			} else {
				// Update polyline
				// Get back the mutable Polyline
				polyline.setPoints(arrayLoc);
			}
			if (polygon != null) {
				// Remove polygon
				polygon.remove();
				polygon = null;
			}
		} else if(arrayLoc.size() > 2){
			if (polygon == null) {
				// Create polygon
				PolygonOptions polygonOptions = new PolygonOptions();
				polygonOptions.fillColor(FILL_COLOR);
				polygonOptions.strokeWidth(STROKE_WIDTH);
				polygonOptions.strokeColor(STROKE_COLOR);
				for (int i = 0; i < arrayLoc.size(); i++) {
					polygonOptions.add(arrayLoc.get(i));
				}
				polygon = map.addPolygon(polygonOptions);
			} else {
				// Update polygon
				polygon.setPoints(arrayLoc);
			}
		}
	}
	
	public void updateShape() {
		List<LatLng> arrayLoc = new ArrayList<LatLng>();
		// Create polyline or update polyline
		for (int i = 0; i < this.markers.size(); i++) {
			Marker curMarker = this.markers.get(i);
			LatLng curPos = curMarker.getPosition();
			arrayLoc.add(curPos);
		}
		updatePoints(arrayLoc);

		if(markers.size() > 2) {
			//Update area
			Float newArea = 0.0f;
			List<Pair<Double, Double>> xyList = new ArrayList<Pair<Double, Double>>();
			for (int i = 0; i < this.markers.size(); i++) {
				Marker curMarker = this.markers.get(i);
				LatLng curPos = curMarker.getPosition();
				
				//Reproject
				double earth_radius = (double) 6371009.0f; // in meters
				double lat_dist = (double) ((Math.PI * earth_radius) / (double) 180.0f);
				double y = curPos.latitude * lat_dist;
				double x = curPos.longitude * lat_dist * Math.cos(Math.toRadians(curPos.latitude));
				//Save x, y
				xyList.add(new Pair<Double, Double>(x,y));
				
				Log.d("MyPolygon", "x:" + Double.toString(x) + " y:" + Double.toString(y));
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
				
				Log.d("MyPolygon", "This Vert x:" + Double.toString((thisVert.first - refX)) + " y:" + Double.toString((thisVert.second - refY)));
				Log.d("MyPolygon", "Next Vert x:" + Double.toString((nextVert.first - refX)) + " y:" + Double.toString((nextVert.second - refY)));

				total1 += ((thisVert.first - refX) * (nextVert.second - refY)); // x(i) * y(i+1)
				total2 += ((thisVert.second - refY) * (nextVert.first - refX)); // y(i) * x(i+1)
			}
			
			newArea = (float) (total1 - total2);
			newArea = Math.abs(newArea) / (2.0f * 4046.68f);

			listener.MyPolygonUpdateAcres(newArea);

			if (polyline != null) {
				// remove polyline
				polyline.remove();
				polyline = null;
			}
		}
		
		if(markers.size() < 3){
			listener.MyPolygonUpdateAcres(0.0f);
		}
		
		
	}

	public void setLabel(String label){
		setLabel(label, false);
	}
	public void setLabel(String label, Boolean selected){
		if(this.textMarker != null) this.textMarker.remove();
		if(label == null) label = "No Name";
		this.textMarker = null;
		textMarkerString = label;
		
		if(this.polygon != null &&  this.polygon.getPoints().size() > 0){
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			if(selected){
				paint.setColor(Color.WHITE);
				paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);
			} else {
				paint.setColor(Color.BLACK);
				paint.setShadowLayer(1f, 0f, 1f, Color.LTGRAY);
			}
			paint.setTextAlign(Align.LEFT);
			paint.setTextSize(20);
			paint.setStrokeWidth(12);
			
			Rect bounds = new Rect();
			paint.getTextBounds(label, 0, label.length(), bounds);
			
			Bitmap.Config conf = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = Bitmap.createBitmap(bounds.width() + 5, bounds.height(), conf); //TODO create blank new bitmap
			float x = 0;
			float y = -1.0f * bounds.top + (bitmap.getHeight() * 0.06f);
					
			Canvas canvas = new Canvas(bitmap);
			canvas.drawText(label, x, y, paint);
			MarkerOptions options = new MarkerOptions();
			
			LatLng where;
			if(this.polygon.getPoints().size() == 1){
				where = polygon.getPoints().get(0);
			} else {
				LatLngBounds.Builder builder = new LatLngBounds.Builder();
				for (int i = 0; i < polygon.getPoints().size(); i++) {
					builder.include(polygon.getPoints().get(i));
				}
				// Have corners
				LatLngBounds boundingBox = builder.build();
				where = midPoint(boundingBox.northeast, boundingBox.southwest);
			}

			this.textMarker = map.addMarker(options.position(where).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
		}
	}
	
	public void selectLabel(Boolean selected){
		setLabel(textMarkerString, selected);
	}
	
	public void setTextMarker(Marker textMarker){
		this.textMarker = textMarker;
	}
	
	public Marker getTextMarker(){
		return this.textMarker;
	}
	
	// Wrapper Functions
	public List<LatLng> getPoints() {
		if(polygon != null) {
			return polygon.getPoints();
		} else {
			return null;
		}
	}
	
	public List<LatLng> getMarkers() {
		List<LatLng> points = new ArrayList<LatLng>();
		for (int i = 0; i < this.markers.size(); i++) {
			points.add(this.markers.get(i).getPosition());
		}
		if(this.markers.size() > 0){
			points.add(this.markers.get(this.markers.size()-1).getPosition());
		}
		return points;
	}

	public List<List<LatLng>> getHoles() {
		if(polygon != null) {
			return polygon.getHoles();
		} else {
			return null;
		}
	}

	public void setPoints(List<LatLng> points) {
		if(polygon != null) polygon.setPoints(points);
	}

	public void remove() {
		if(polygon != null) polygon.remove();
		if(textMarker != null) textMarker.remove();
	}

	public void setVisible(boolean visible) {
		if(polygon != null) polygon.setVisible(visible);
	}

	public void setFillColor(int color) {
		if(polygon != null) polygon.setFillColor(color);
	}

	public void setStrokeColor(int color) {
		if(polygon != null) polygon.setStrokeColor(color);
	}
	
	public static LatLng midPoint(LatLng point1, LatLng point2){
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
}
