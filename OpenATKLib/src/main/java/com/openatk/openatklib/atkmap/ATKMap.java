package com.openatk.openatklib.atkmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openatk.openatklib.R;
import com.openatk.openatklib.atkmap.listeners.ATKDrawListener;
import com.openatk.openatklib.atkmap.listeners.ATKMapClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointDragListener;
import com.openatk.openatklib.atkmap.listeners.ATKPolygonClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPolygonDrawListener;
import com.openatk.openatklib.atkmap.listeners.ATKPolylineClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKTouchableWrapperListener;
import com.openatk.openatklib.atkmap.models.ATKPoint;
import com.openatk.openatklib.atkmap.models.ATKPolygon;
import com.openatk.openatklib.atkmap.models.ATKPolyline;
import com.openatk.openatklib.atkmap.views.ATKPointView;
import com.openatk.openatklib.atkmap.views.ATKPointViewOptions;
import com.openatk.openatklib.atkmap.views.ATKPolygonView;
import com.openatk.openatklib.atkmap.views.ATKPolylineView;

public class ATKMap implements ATKTouchableWrapperListener {
	//Callbacks to whoever uses this library
	private ATKMapClickListener atkMapClickListener;
	private ATKPointClickListener atkPointClickListener;
	private ATKPointDragListener atkPointDragListener;

	private ATKPolygonClickListener atkPolygonClickListener;
	private ATKPolygonDrawListener atkPolygonDrawListener;
	
	private ATKPolylineClickListener atkPolylineClickListener;
	private ATKDrawListener atkDrawListener;
	
	//Static variables, ie. drawing options
	int colorFillPolygonDrawing = Color.argb(100, 191, 0, 136);
	int colorFillCompletePolygonDrawing = Color.argb(200, 200, 200, 200);
	int colorStrokePolygonDrawing = Color.argb(255, 255, 255, 255);
	int colorStrokeCompletePolygonDrawing = Color.argb(255, 0, 0, 0);
	
	int resIdPointSelectedPolylineDrawing = R.drawable.selected_vertex;
	int PointSelectedPolylineDrawingHeight = 0;
	int PointSelectedPolylineDrawingWidth = 0;

	float anchorVPointSelectedPolylineDrawing = 0.5f;
	float anchorUPointSelectedPolylineDrawing = 0.5f;
	float anchorVPanPointSelectedPolylineDrawing = 0.5f;
	float anchorUPanPointSelectedPolylineDrawing = 0.5f;
	int panWidthPointSelectedPolylineDrawing = 64;
	int panHeightPointSelectedPolylineDrawing = 64;
	
	int resIdPointPolylineDrawing = R.drawable.unselected_vertex;
	int PointPolylineDrawingHeight = 0;
	int PointPolylineDrawingWidth = 0;

	float anchorVPointPolylineDrawing = 0.5f;
	float anchorUPointPolylineDrawing = 0.5f;
	
	int resIdPointSelectedPolygonDrawing = R.drawable.selected_vertex;
	int PointSelectedPolygonDrawingHeight = 0;
	int PointSelectedPolygonDrawingWidth = 0;

	float anchorVPointSelectedPolygonDrawing = 0.5f;
	float anchorUPointSelectedPolygonDrawing = 0.5f;
	float anchorVPanPointSelectedPolygonDrawing = 0.5f;
	float anchorUPanPointSelectedPolygonDrawing = 0.5f;
	int panWidthPointSelectedPolygonDrawing = 64;
	int panHeightPointSelectedPolygonDrawing = 64;

	int resIdPointPolygonDrawing = R.drawable.unselected_vertex;
	int PointPolygonDrawingHeight = 0;
	int PointPolygonDrawingWidth = 0;

	float anchorVPointPolygonDrawing = 0.5f;
	float anchorUPointPolygonDrawing = 0.5f;
	
	//Local variables
	BitmapDescriptor iconPointSelectedPolygonDrawing;
	BitmapDescriptor iconPointPolygonDrawing;
	BitmapDescriptor iconPointSelectedPolylineDrawing;
	BitmapDescriptor iconPointPolylineDrawing;
	
	private GoogleMap map;
	private List<ATKPointView> points = new ArrayList<ATKPointView>();
	private List<ATKPolygonView> polygons = new ArrayList<ATKPolygonView>();
	private List<ATKPolylineView> polylines = new ArrayList<ATKPolylineView>();
	
	private int nextPointId = 0;
	
	private boolean isDraggingPoint = false;

	
	private boolean isDrawingPolygon = false;
	private ATKPolygonView polygonDrawing;
	private List<ATKPointView> pointsPolygonDrawing = new ArrayList<ATKPointView>();
	private ATKPointView pointSelectedPolygonDrawing;

	private boolean isDrawingPolyline = false;
	private ATKPolylineView polylineDrawing;
	private List<ATKPointView> pointsPolylineDrawing = new ArrayList<ATKPointView>();
	private ATKPointView pointSelectedPolylineDrawing;
	
	//Used for dragging
	int panOffsetYPolygonDrawing = 0;
	int panOffsetXPolygonDrawing = 0;
	
	//Used for regular dragging TODO combine this with polygon dragging
	int draggingStartY;
	int draggingStartX;
	ATKPointView draggingPoint;
	
	private Point lastClickPoint; //Used when a marker is clicked but we don't want this marker to be clickable
	
	//Used locally to handle events
	private GoogleMapClickListener googleMapClickListener;
	private GoogleMarkerClickListener googleMarkerClickListener;
	
	private Context context;
	private GestureDetector gestureDetector;

	public ATKMap(GoogleMap map, Context context){
		this.map = map;
		this.context = context;
		this.googleMapClickListener = new GoogleMapClickListener();
		this.googleMarkerClickListener = new GoogleMarkerClickListener();
		map.setOnMapClickListener(googleMapClickListener);
		map.setOnMarkerClickListener(googleMarkerClickListener);
		
		//Get point icons from resources
		this.iconPointSelectedPolygonDrawing = BitmapDescriptorFactory.fromResource(this.resIdPointSelectedPolygonDrawing);
		this.iconPointPolygonDrawing = BitmapDescriptorFactory.fromResource(this.resIdPointPolygonDrawing);
		this.iconPointSelectedPolylineDrawing = BitmapDescriptorFactory.fromResource(this.resIdPointSelectedPolylineDrawing);
		this.iconPointPolylineDrawing = BitmapDescriptorFactory.fromResource(this.resIdPointPolylineDrawing);
		
		//Get dimensions from icon
		BitmapFactory.Options dimensions = new BitmapFactory.Options(); 
		dimensions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(this.context.getResources(), this.resIdPointSelectedPolygonDrawing, dimensions);
		this.PointSelectedPolygonDrawingHeight = dimensions.outHeight;
		this.PointSelectedPolygonDrawingWidth =  dimensions.outWidth;
		
		BitmapFactory.decodeResource(this.context.getResources(), this.resIdPointPolygonDrawing, dimensions);
		this.PointPolygonDrawingHeight = dimensions.outHeight;
		this.PointPolygonDrawingWidth =  dimensions.outWidth;
		
		BitmapFactory.decodeResource(this.context.getResources(), this.resIdPointSelectedPolylineDrawing, dimensions);
		this.PointSelectedPolylineDrawingHeight = dimensions.outHeight;
		this.PointSelectedPolylineDrawingWidth =  dimensions.outWidth;
		
		BitmapFactory.decodeResource(this.context.getResources(), this.resIdPointPolylineDrawing, dimensions);
		this.PointPolylineDrawingHeight = dimensions.outHeight;
		this.PointPolylineDrawingWidth =  dimensions.outWidth;
		
		gestureDetector = new GestureDetector(context, new GestureListener());
	}
		
	public void setOnMapClickListener(ATKMapClickListener listener){
		this.atkMapClickListener = listener;
	}
	
	public void setOnPointClickListener(ATKPointClickListener listener){
		this.atkPointClickListener = listener;
	}
	
	public void setOnPointDragListener(ATKPointDragListener listener){
		this.atkPointDragListener = listener;
	}
	
	public void setOnPolygonClickListener(ATKPolygonClickListener listener){
		this.atkPolygonClickListener = listener;
	}
	
	public void setOnPolygonDrawListener(ATKPolygonDrawListener listener){
		this.atkPolygonDrawListener = listener;
	}
	
	public void clear(){
		this.map.clear();
		this.polygons.clear();
		this.polylines.clear();
		this.points.clear();
		this.pointsPolygonDrawing.clear();
		this.pointsPolylineDrawing.clear();
		this.isDrawingPolygon = false;
		this.isDrawingPolyline = false;
		this.isDraggingPoint = false;
	}
	
	public ATKPointView addPoint(ATKPoint point){
		ATKPointView pointView = new ATKPointView(map,point);
		this.points.add(pointView);
		return pointView;
	}
	
	public ATKPointView addPoint(ATKPointViewOptions pointOptions){
		ATKPointView pointView = new ATKPointView(map,pointOptions);
		this.points.add(pointView);
		return pointView;
	}
	
	public ATKPointView addPoint(ATKPoint point, ATKPointViewOptions pointOptions){
		ATKPointView pointView = new ATKPointView(map, point, pointOptions);
		this.points.add(pointView);
		return pointView;
	}
	
	public ATKPolygonView addPolygon(ATKPolygon polygon){
		ATKPolygonView polygonView = new ATKPolygonView(map, polygon);
		this.polygons.add(polygonView);
		Log.d("ATKMAP", "poly added:" + this.polygons.size());
		return polygonView;
	}
	
	public boolean updatePoint(ATKPoint point){
		if(point.id == null) return false;
		for(int i=0; i<points.size(); i++){
			ATKPointView pointView = points.get(i);
			if(pointView.getAtkPoint().id.equals(point.id)){
				pointView.setAtkPoint(point);
				return true;
			}
		}
		return false;
	}
	public boolean updatePolygon(ATKPolygon polygon){
		if(polygon.id == null) return false;
		for(int i=0; i<polygons.size(); i++){
			if(polygons.get(i).getAtkPolygon().id.equals(polygon.id)){
				polygons.get(i).setAtkPolygon(polygon);
				return true;
			}
		}
		return false;
	}
	public boolean removePolygon(ATKPolygon polygon){
		if(polygon.id == null) return false;
		for(int i=0; i<polygons.size(); i++){
			if(polygons.get(i).getAtkPolygon().id.equals(polygon.id)){
				polygons.get(i).remove();
				polygons.remove(i);
				return true;
			}
		}
		return false;
	}
	public boolean removePoint(ATKPoint point){
		if(point.id == null) return false;
		for(int i=0; i<points.size(); i++){
			ATKPointView pointView = points.get(i);
			if(pointView.getAtkPoint().id.equals(point.id)){
				pointView.remove();
				points.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public ATKPolygonView getPolygonView(Object atkPolygonId){
		if(atkPolygonId == null) return null;
		for(int i=0; i<polygons.size(); i++){
			if(polygons.get(i).getAtkPolygon().id.equals(atkPolygonId)){
				return polygons.get(i);
			}
		}
		return null;
	}
	public List<ATKPolygonView> getPolygonViews(){
		return this.polygons;
	}
	
	public ATKPolylineView getPolylineView(Object atkPolylineId){
		if(atkPolylineId == null) return null;
		for(int i=0; i<polylines.size(); i++){
			if(polylines.get(i).getAtkPolyline().id.equals(atkPolylineId)){
				return polylines.get(i);
			}
		}
		return null;
	}
	public List<ATKPolylineView> getPolylineViews(){
		return this.polylines;
	}
	
	public ATKPointView getPointView(Object atkPointId){
		if(atkPointId == null) return null;
		for(int i=0; i<points.size(); i++){
			if(points.get(i).getAtkPoint().id.equals(atkPointId)){
				return points.get(i);
			}
		}
		return null;
	}
	public List<ATKPointView> getPointViews(){
		return this.points;
	}
	
	public void drawPolygon(ATKPolygonView polygon){
		this.drawPolygon(polygon, null);
	}
	public void drawPolygon(ATKPolygonView polygon, ATKPolygonDrawListener listener){
		//Edit an existing atkPolygon
		this.polygonDrawing = polygon;
		this.polygonDrawing.setFillColor(colorFillPolygonDrawing);
		this.polygonDrawing.setStrokeColor(colorStrokePolygonDrawing);
		this.polygonDrawing.setOnDrawListener(listener);
		this.isDrawingPolygon = true;
		//Add points for all vertexes of polygon
		List<LatLng> boundary = this.polygonDrawing.getAtkPolygon().boundary;
		for(int i=0; i<boundary.size(); i++){
			ATKPoint point = new ATKPoint(nextPointId); //Don't init with position so it won't draw yet
			nextPointId++;
			ATKPointView pointView = new ATKPointView(map, point);
			if(i==boundary.size()-1){
				pointView.setIcon(iconPointSelectedPolygonDrawing, PointSelectedPolygonDrawingHeight, PointSelectedPolygonDrawingWidth);
				pointView.setAnchor(anchorUPointPolygonDrawing, anchorVPointPolygonDrawing);
				pointSelectedPolygonDrawing = pointView;
			} else {
				pointView.setIcon(iconPointPolygonDrawing, PointPolygonDrawingHeight, PointPolygonDrawingWidth);
				pointView.setAnchor(anchorUPointPolygonDrawing, anchorVPointPolygonDrawing);
			}
			point.position = boundary.get(i); //Set position of model
			pointView.update(); //Tell pointView to refresh its view
			pointsPolygonDrawing.add(pointView);
		}
	}
	public ATKPolygonView drawPolygon(Object atkPolygonId){
		return this.drawPolygon(atkPolygonId, null);
	}
	public ATKPolygonView drawPolygon(Object atkPolygonId, ATKPolygonDrawListener listener){
		//atkPolygon id needs to be unique
		//Set to draw mode
		ATKPolygon newPoly = new ATKPolygon(atkPolygonId);
		if(this.isDrawingPolygon != false) Log.w("atkMap", "Drawing of last polygon was not completed, you have lost that polygon.");
		//Create a the polygon that we are currently drawing
		this.polygonDrawing = new ATKPolygonView(map, newPoly);
		this.polygonDrawing.setFillColor(colorFillPolygonDrawing);
		this.polygonDrawing.setStrokeColor(colorStrokePolygonDrawing);
		this.polygonDrawing.setOnDrawListener(listener);
		this.isDrawingPolygon = true;//Add if not already there
		if(this.getPolygonView(atkPolygonId) == null){
			this.polygons.add(this.polygonDrawing);
		}
		return this.polygonDrawing;
	}
	public ATKPolygonView completePolygon(){
		//Complete drawing of current polygon
		if(this.isDrawingPolygon == false) return null;
		this.polygonDrawing.setFillColor(colorFillCompletePolygonDrawing);
		this.polygonDrawing.setStrokeColor(colorStrokeCompletePolygonDrawing);
		//Remove all points
		for (Iterator<ATKPointView> iter = this.pointsPolygonDrawing.iterator(); iter.hasNext();) {
			ATKPointView point = iter.next();
			point.remove(); //Remove from map
			iter.remove(); //Remove from list
		}
		ATKPolygonView toReturn = this.polygonDrawing;
		//Add if not already there
		if(this.getPolygonView(this.polygonDrawing.getAtkPolygon().id) == null){
			this.polygons.add(this.polygonDrawing);
		}
		this.polygonDrawing = null;
		this.pointSelectedPolygonDrawing = null;
		this.isDrawingPolygon = false;
		return toReturn;
	}
	public boolean zoomTo(ATKPolygon polygon){
		return this.zoomTo(polygon, true);
	}
	public boolean zoomTo(ATKPolygon polygon , boolean animate){
		if(polygon == null || polygon.boundary == null || polygon.boundary.size() < 2) return false;
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for(int i=0; i<polygon.boundary.size(); i++){
			builder.include(polygon.boundary.get(i));
		}
		if(animate){
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
		} else {
			map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
		}
		return false;
	}
	public void drawUndo(){
		if(this.isDrawingPolygon == false && this.isDrawingPolyline == false) return;
		
		if(this.isDrawingPolygon == true){
			//Undo last added point
			Integer index = null;				
			if(pointSelectedPolygonDrawing != null) index = pointsPolygonDrawing.indexOf(pointSelectedPolygonDrawing);
			if(index != null){
				this.pointsPolygonDrawing.remove(index.intValue());

				pointSelectedPolygonDrawing.remove();
				//Remove a point to the polygon's model that we are currently drawing				
				polygonDrawing.getAtkPolygon().boundary.remove(index.intValue()); //Update its model
				polygonDrawing.update(); //Tell it to refresh its view
				
				//Select another point if there is any
				if(this.pointsPolygonDrawing.size() > 0){
					if(index >= this.pointsPolygonDrawing.size()){
						index = 0;
					}
					pointSelectedPolygonDrawing = this.pointsPolygonDrawing.get(index);
					pointSelectedPolygonDrawing.setIcon(iconPointSelectedPolygonDrawing, PointSelectedPolygonDrawingWidth, PointSelectedPolygonDrawingHeight);
					pointSelectedPolygonDrawing.setAnchor(anchorUPointSelectedPolygonDrawing, anchorVPointSelectedPolygonDrawing);
				} else {
					pointSelectedPolygonDrawing = null;
				}
				
				boolean consumed = false;
				if(polygonDrawing.getOnDrawListener() != null) {
					consumed = polygonDrawing.getOnDrawListener().afterBoundaryChange(polygonDrawing);
				}
				if(consumed == false && atkPolygonDrawListener != null){
					atkPolygonDrawListener.afterBoundaryChange(polygonDrawing);
				}
			}
		} else {
			//Drawing polyline
			Log.w("ATKMap", "Need to implement undo for polyline drawing...");
		}
	}
	private class GoogleMarkerClickListener implements OnMarkerClickListener {
		@Override
		public boolean onMarkerClick(Marker marker) {
			//Touched a point, find which one if and if we consumed
			if(isDrawingPolygon == true){
				//We are drawing a polygon
				//Check if we clicked any of its points
				ATKPointView clickedPoint = null;
				for(int i=0; i<pointsPolygonDrawing.size(); i++){
					if(pointsPolygonDrawing.get(i).wasClicked(marker) != null){
						clickedPoint = pointsPolygonDrawing.get(i);
						break;
					}
				}
				if(clickedPoint != null){
					//Unselect old point
					if(pointSelectedPolylineDrawing != null){
						pointSelectedPolylineDrawing.setIcon(iconPointPolylineDrawing, PointPolylineDrawingWidth, PointPolylineDrawingHeight);
						pointSelectedPolylineDrawing.setAnchor(anchorUPointPolylineDrawing, anchorVPointPolylineDrawing);
					}
					if(pointSelectedPolygonDrawing != null){
						
						pointSelectedPolygonDrawing.setIcon(iconPointPolygonDrawing, PointPolygonDrawingWidth, PointPolygonDrawingHeight);
						pointSelectedPolygonDrawing.setAnchor(anchorUPointPolygonDrawing, anchorVPointPolygonDrawing);
					}
					//Select this point
					pointSelectedPolygonDrawing = clickedPoint;
					clickedPoint.setIcon(iconPointSelectedPolygonDrawing, PointSelectedPolygonDrawingWidth, PointSelectedPolygonDrawingHeight);
					clickedPoint.setAnchor(anchorUPointSelectedPolygonDrawing, anchorVPointSelectedPolygonDrawing);
				}
				return true; //Consume click
			}
			if(isDrawingPolyline == true){
				//We are drawing a polyline
				//Check if we clicked any of its points
				ATKPointView clickedPoint = null;
				for(int i=0; i<pointsPolylineDrawing.size(); i++){
					if(pointsPolylineDrawing.get(i).wasClicked(marker) == true){
						clickedPoint = pointsPolylineDrawing.get(i);
						break;
					}
				}
				if(clickedPoint != null){
					//Unselect old point
					if(pointSelectedPolylineDrawing != null) pointSelectedPolylineDrawing.setIcon(iconPointPolylineDrawing, PointPolylineDrawingWidth, PointPolylineDrawingHeight);
					if(pointSelectedPolygonDrawing != null) pointSelectedPolygonDrawing.setIcon(iconPointPolygonDrawing, PointPolygonDrawingWidth, PointPolygonDrawingHeight);
					//Select this point
					pointSelectedPolylineDrawing = clickedPoint;
					clickedPoint.setIcon(iconPointSelectedPolylineDrawing, PointSelectedPolylineDrawingWidth, PointSelectedPolylineDrawingHeight);
				}
				return true; //Consume click
			}			
			
			Boolean wasClicked = null;
			ATKPointView point = null;
			for(int i=0; i<points.size(); i++){
				wasClicked = points.get(i).wasClicked(marker); //This does the click event on atkPointClickListener, null if not clicked, true if clicked and consumed, false if clicked and not consumed
				if(wasClicked == null){
					//Not clicked
				} else if(wasClicked == true){
					return true; //Consume the click
				} else if(wasClicked == false){
					point = points.get(i);
					break;
				}
			}
			if(wasClicked != null && atkPointClickListener != null){
				//Was clicked but wasn't consumed, pass to default atkPointClickListener
				atkPointClickListener.onPointClick(point);
			}
			
			//Check if a polygon's label was clicked.
			for(int i=0; i<polygons.size(); i++){
				if(polygons.get(i).labelWasClicked(marker)){
					googleMapClickListener.onMapClick(map.getProjection().fromScreenLocation(lastClickPoint));
					return true;
				}
			}
			
			return false;
		}
	}

	private class GoogleMapClickListener implements OnMapClickListener {
		@Override
		public void onMapClick(LatLng position) {
			//Google map was clicked
			if(isDrawingPolygon == true){
				//We are drawing a polygon
				
				//Check if we want to allow change
				boolean allow = true;
				if(polygonDrawing.getOnDrawListener() != null) {
					allow = polygonDrawing.getOnDrawListener().beforeBoundaryChange(polygonDrawing);
				}
				if(allow == true && atkPolygonDrawListener != null){
					atkPolygonDrawListener.beforeBoundaryChange(polygonDrawing);
				}
				
				if(allow == false) return;
				
				//Add a point to the map to represent the vertex
				int selectedPointIndex = 0;
				if(pointSelectedPolygonDrawing != null) {
					pointSelectedPolygonDrawing.setIcon(iconPointPolygonDrawing, PointPolygonDrawingHeight, PointPolygonDrawingWidth);
					pointSelectedPolygonDrawing.setAnchor(anchorUPointPolygonDrawing, anchorVPointPolygonDrawing);
					selectedPointIndex = pointsPolygonDrawing.indexOf(pointSelectedPolygonDrawing);
				}
				ATKPoint point = new ATKPoint(nextPointId); //Don't init with position so it won't draw yet
				nextPointId++;
				ATKPointView pointView = new ATKPointView(map, point);
				pointView.setIcon(iconPointSelectedPolygonDrawing, PointSelectedPolygonDrawingHeight, PointSelectedPolygonDrawingWidth);
				pointView.setAnchor(anchorUPointSelectedPolygonDrawing, anchorVPointSelectedPolygonDrawing);
				point.position = position; //Set position of model
				pointView.update(); //Tell pointView to refresh its view
				
				pointsPolygonDrawing.add(selectedPointIndex, pointView);
				pointSelectedPolygonDrawing = pointView;
				
				//Add a point to the polygon's model that we are currently drawing				
				polygonDrawing.getAtkPolygon().boundary.add(selectedPointIndex, position); //Update its model
				polygonDrawing.update(); //Tell it to refresh its view
				boolean consumed = false;
				if(polygonDrawing.getOnDrawListener() != null) {
					consumed = polygonDrawing.getOnDrawListener().afterBoundaryChange(polygonDrawing);
				}
				if(consumed == false && atkPolygonDrawListener != null){
					atkPolygonDrawListener.afterBoundaryChange(polygonDrawing);
				}
				
				return; //We don't want polygons or anything to be clicked while we are drawing.
			}

			//Check if polygon was clicked
			Boolean polygonClicked = false;
			ATKPolygonView polygon = null;
			for(int i=0; i<polygons.size(); i++){
				polygonClicked = polygons.get(i).wasClicked(position);
				if(polygonClicked == true){
					polygon = polygons.get(i);
					break;
				}
			}

			//Check if polyline was clicked
			Boolean polylineClicked = false;
			ATKPolylineView polyline = null;
			for(int i=0; i<polylines.size(); i++){
				//polylineClicked is Null if not clicked, true if it has a clickListener, false otherwise
				polylineClicked = polylines.get(i).wasClicked(position); 
				if(polylineClicked == true){
					polyline = polylines.get(i);
					break;
				}
			}
			
			//If both were clicked find out which we should click
			if(polygonClicked == true && polylineClicked == true){
				//Clicked both, find out which one, default polyline over polygon
				if(polygon.getZIndex() >= polyline.getZIndex()){
					polylineClicked = false; //We didn't clicked the polyline
				} else {
					polygonClicked = false; //We didn't clicked the polygon
				}
			} 
			if(polygonClicked == true){
				//Click the polygon and see if the listener consumed it
				boolean consumed = polygon.click();
				if(consumed == false){
					//Pass it to the default listener
					if(atkPolygonClickListener != null) atkPolygonClickListener.onPolygonClick(polygon);
				}
				return;
			}
			if(polylineClicked == true){
				//Was clicked but wasn't consumed
				boolean consumed = polyline.click();
				if(consumed == false){
					//Pass it to the default listener
					if(atkPolylineClickListener != null) atkPolylineClickListener.onPolylineClick(polyline);
				}
				return;
			}			
			
			//Notify them if we didn't click a polygon or polygon, or we didn't consume the click
			if(atkMapClickListener != null) atkMapClickListener.onMapClick(position);
		}
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		//The map was touched, this triggers before GoogleMapClickListener
		lastClickPoint = new Point((int) event.getX(), (int) event.getY());
		
		//If we return true GoogleMapClickListener wont get the touch event
		
		//Drawing Polygon stuff
		if(this.isDrawingPolygon && isDraggingPoint && event.getActionIndex() == 0 && event.getAction() == MotionEvent.ACTION_MOVE){
			//We are dragging the polygons selected point
			Point thePoint = new Point((int)event.getX() + panOffsetXPolygonDrawing, (int)event.getY() + panOffsetYPolygonDrawing);
						
			this.pointSelectedPolygonDrawing.getAtkPoint().position = map.getProjection().fromScreenLocation(thePoint);
			this.pointSelectedPolygonDrawing.update();
			//TODO streamline this?
			List<LatLng> newBoundary = new ArrayList<LatLng>();
			for(int i=0; i<this.pointsPolygonDrawing.size(); i++){
				newBoundary.add(this.pointsPolygonDrawing.get(i).getAtkPoint().position);
			}
			this.polygonDrawing.getAtkPolygon().boundary = newBoundary;
			this.polygonDrawing.update();
			
			boolean consumed = false;
			if(polygonDrawing.getOnDrawListener() != null) {
				consumed = polygonDrawing.getOnDrawListener().afterBoundaryChange(polygonDrawing);
			}
			if(consumed == false && atkPolygonDrawListener != null){
				atkPolygonDrawListener.afterBoundaryChange(polygonDrawing);
			}
			
			return true;
		} else if(this.isDrawingPolygon && isDraggingPoint && event.getActionIndex() == 0 && event.getAction() == MotionEvent.ACTION_UP){
			//Stop dragging polygons selected point
			this.isDraggingPoint = false;
			return true;
		} else if(this.isDrawingPolygon && event.getActionIndex() == 0 && event.getAction() == MotionEvent.ACTION_DOWN){
			if(this.pointSelectedPolygonDrawing != null){
				//Check if we are pressing on the marker icon
				Point markerPoint = map.getProjection().toScreenLocation(this.pointSelectedPolygonDrawing.getAtkPoint().position);
				
				BitmapFactory.Options dimensions = new BitmapFactory.Options(); 
				dimensions.inJustDecodeBounds = true;
				BitmapFactory.decodeResource(this.context.getResources(), this.resIdPointSelectedPolygonDrawing, dimensions);
				int iconHeight = dimensions.outHeight;
				int iconWidth =  dimensions.outWidth;
				
				int y1 = (int) (iconHeight * anchorVPointSelectedPolygonDrawing);
				int x1 = (int) (iconWidth * anchorUPointSelectedPolygonDrawing);
				int y2 = (int) (iconHeight * anchorVPanPointSelectedPolygonDrawing);
				int x2 = (int) (iconWidth * anchorUPanPointSelectedPolygonDrawing);
				
				//panOffsetYPolygonDrawing = y1-y2;
				//panOffsetXPolygonDrawing = x1-x2;
				
				panOffsetYPolygonDrawing = markerPoint.y - (int)event.getY();
				panOffsetXPolygonDrawing = markerPoint.x - (int)event.getX();
												
				/*if((y1-y2 - (panHeightPointSelectedPolygonDrawing/2)) < (markerPoint.y - (int)event.getY())){
					if((y1-y2 + (panHeightPointSelectedPolygonDrawing/2)) > (markerPoint.y - (int)event.getY())){
						if((x1-x2 - (panWidthPointSelectedPolygonDrawing/2)) < (markerPoint.x - (int)event.getX())){
							if((x1-x2 + (panWidthPointSelectedPolygonDrawing/2)) > (markerPoint.x - (int)event.getX())){
								//Move the point and consume the touch event
								isDraggingPoint = true;
								return true;
							}
						}
					}
				}*/
				
				if((markerPoint.x - (iconWidth * anchorUPointSelectedPolygonDrawing)) < (int)event.getX()){
					if((markerPoint.x + (iconWidth * (1.0-anchorUPointSelectedPolygonDrawing))) > (int)event.getX()){
						if((markerPoint.y - (iconHeight * anchorVPointSelectedPolygonDrawing)) < (int)event.getY()){
							if((markerPoint.y + (iconHeight * (1.0-anchorVPointSelectedPolygonDrawing))) > (int)event.getY()){
								//Move the point and consume the touch event
								isDraggingPoint = true;
								return true;
							}
						}
					}
				}
			}
		}
		if(this.isDrawingPolygon){
			if(this.gestureDetector.onTouchEvent(event)){
				//Consume the double tap
				return false;
			}
		}		
		
		//TODO Regular map stuff (We need to handle all causes here, not split between drawing and not drawing)
		if(this.isDraggingPoint && event.getActionIndex() == 0 && event.getAction() == MotionEvent.ACTION_MOVE){
			//During dragging
			Point thePoint = new Point((int)event.getX() + this.draggingStartX, (int)event.getY() + this.draggingStartY);
			this.draggingPoint.getAtkPoint().position = map.getProjection().fromScreenLocation(thePoint);
			this.draggingPoint.update(); //Update on map
			Boolean ret = this.draggingPoint.drag();
			if(ret == null){
				//Point did not have a drag listener, pass to maps point drag listener if exists
				if(this.atkPointDragListener != null) this.atkPointDragListener.onPointDrag(this.draggingPoint);
			} else {
				//Had listener, if not consumed pass to maps point drag listener if exists
				if(ret == false && this.atkPointDragListener != null) this.atkPointDragListener.onPointDrag(this.draggingPoint);
			}
		} else if(this.isDraggingPoint && event.getActionIndex() == 0 && event.getAction() == MotionEvent.ACTION_UP){
			//Stop dragging
			this.isDraggingPoint = false;
			Boolean ret = this.draggingPoint.dragEnd();
			if(ret == null){
				//Point did not have a drag listener, pass to maps point drag listener if exists
				if(this.atkPointDragListener != null) this.atkPointDragListener.onPointDragEnd(this.draggingPoint);
			} else {
				//Had listener, if not consumed pass to maps point drag listener if exists
				if(ret == false && this.atkPointDragListener != null) this.atkPointDragListener.onPointDragEnd(this.draggingPoint);
			}
			this.draggingPoint = null;
		} else if(event.getActionIndex() == 0 && event.getAction() == MotionEvent.ACTION_DOWN) {
			//Start dragging
			//Check all points for super drag option
			for(int i=0; i < points.size(); i++){
				if(points.get(i).getSuperDraggable()){
					//This point is superdraggable, check if we want to drag it
					ATKPointView point = points.get(i);
					Point screenPoint = map.getProjection().toScreenLocation(point.getAtkPoint().position);
					
					//Get dimensions from icon
					int iconHeight = point.getIconHeight();
					int iconWidth =  point.getIconWidth();
					
					
					this.draggingStartY = screenPoint.y - (int)event.getY();
					this.draggingStartX = screenPoint.x - (int)event.getX();
					
					if((screenPoint.x - (iconWidth * point.getAnchorU())) < (int)event.getX()){
						if((screenPoint.x + (iconWidth * (1.0-point.getAnchorU()))) > (int)event.getX()){
							if((screenPoint.y - (iconHeight * point.getAnchorV())) < (int)event.getY()){
								if((screenPoint.y + (iconHeight * (1.0-point.getAnchorV()))) > (int)event.getY()){
									//Move the point and consume the touch event
									this.isDraggingPoint = true;
									this.draggingPoint = point;
									Boolean ret = this.draggingPoint.dragStart();
									if(ret == null){
										//Point did not have a drag listener, pass to maps point drag listener if exists
										if(this.atkPointDragListener != null) this.atkPointDragListener.onPointDragStart(this.draggingPoint);
									} else {
										//Had listener, if not consumed pass to maps point drag listener if exists
										if(ret == false && this.atkPointDragListener != null) this.atkPointDragListener.onPointDragStart(this.draggingPoint);
									}
									return true;
								}
							}
						}
					}
					
					
				}
			}
		}		
		
		
		return false;
	}
	
	 private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		 	private boolean doubleTap = false;
		 		        
	        // event when double tap occurs
	        @Override
	        public boolean onDoubleTap(MotionEvent e) {
	        	Log.d("OpenATKLib", "double tap");
	        	doubleTap = true;
	            return true;
	        }
	        public boolean wasDoubleTap(){
	        	return doubleTap;
	        }
	 }
	 
	 //Google map functions
	 public UiSettings getUiSettings(){
		 return this.map.getUiSettings();
	 }
	public void setMyLocationEnabled(boolean enabled){
		this.map.setMyLocationEnabled(enabled);
	}
	public void setMapType(int type){
		this.map.setMapType(type);
	}
	public void moveCamera(CameraUpdate update){
		this.map.moveCamera(update);
	}
	public Location getMyLocation(){
		return this.map.getMyLocation();
	}
	public CameraPosition getCameraPosition(){
		return this.map.getCameraPosition();
	}
	public Projection getProjection(){
		return this.map.getProjection();
	}
	public float getMaxZoomLevel() {
		return this.map.getMaxZoomLevel();
	}
	public void animateCamera(CameraUpdate update){
		this.map.animateCamera(update);
	}
	public void animateCamera(CameraUpdate update, CancelableCallback callback){
		this.map.animateCamera(update, callback);
	}
	public void animateCamera(CameraUpdate update, int durationMs, CancelableCallback callback){
		this.map.animateCamera(update, durationMs, callback);
	}
}
