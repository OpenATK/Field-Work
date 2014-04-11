package com.openatk.field_work.views;


import android.graphics.Color;

import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Job;
import com.openatk.openatklib.atkmap.ATKMap;
import com.openatk.openatklib.atkmap.listeners.ATKPolygonClickListener;
import com.openatk.openatklib.atkmap.models.ATKPolygon;
import com.openatk.openatklib.atkmap.views.ATKPolygonView;

public class FieldView {
	
	//Defaults move to Constants Class?
	public static float STROKE_WIDTH = 2.0f;
	public static int STROKE_COLOR_NORMAL = Color.BLACK;
	public static int STROKE_COLOR_SELECTED = Color.WHITE;
	
	public static int FILL_COLOR_NOT_PLANNED = Color.argb(128, 186, 188, 190);
	public static int FILL_COLOR = Color.argb(128, 186, 188, 190);
	public static int FILL_COLOR_PLANNED = Color.argb(128, 202, 87, 90);
	public static int FILL_COLOR_STARTED = Color.argb(128, 238, 182, 86);
	public static int FILL_COLOR_DONE = Color.argb(128, 128, 197, 128);
	
	public static int STATE_NORMAL = 0;
	public static int STATE_SELECTED = 1;
	public static int STATE_EDITING = 2;
	
	private int state = STATE_NORMAL;
	private Field field;
	private Job job;
	
	private ATKPolygonView polygonView;
	private ATKMap map;
	
	public FieldView(Field field, Job job, ATKMap map) {
		this(STATE_NORMAL, field, job, map);
	}
	
	public FieldView(int state, Field field, Job job, ATKMap map) {
		this(state, field,job, null, map);
	}
	
	public FieldView(int state, Field field, Job job,
			ATKPolygonView polygonView, ATKMap map) {
		super();
		this.state = state;
		this.field = field;
		this.job = job;
		this.polygonView = polygonView;
		this.map = map;
	}

	public void setState(int newState){
		if(this.state != newState){
			if(this.state == FieldView.STATE_EDITING && newState != FieldView.STATE_EDITING){
				//Stop drawing polygonView
				map.completePolygon();
			}
			
			if(newState == FieldView.STATE_NORMAL){
				//Set stroke color
				this.polygonView.setStrokeColor(FieldView.STROKE_COLOR_NORMAL);
				this.polygonView.setLabelSelected(false);
			} else if(newState == FieldView.STATE_SELECTED){
				//Set stroke color and select label
				this.polygonView.setStrokeColor(FieldView.STROKE_COLOR_SELECTED);
				this.polygonView.setLabelSelected(true);
			} else if(newState == FieldView.STATE_EDITING) {
				//Edit polygon
				map.drawPolygon(this.polygonView);
			}
		}
	}
	
	public void update(Field newField, Job newJob){
		//Compares data and updates map accordingly or adds to map if doesn't exist
		if(newField.getDeleted()){
			//Remove the field from the map and we are done
			if(polygonView != null) polygonView.remove();
			return;
		}
		
		if(polygonView == null){
			ATKPolygon atkPoly = new ATKPolygon(newField.getId(), newField.getBoundary());
			//This adds the polygon to the map so it should be visible after this
			polygonView = map.addPolygon(atkPoly);
		}
		
		//Check all visual aspects of FieldView for changes
		
		//Field related visual aspects of polygonView
		if(field.getBoundary() != newField.getBoundary()){
			//Update polygonView boundary
			polygonView.getAtkPolygon().boundary = newField.getBoundary();
			polygonView.update();
		}
		
		if(field.getName() != newField.getName()){
			//Update polygonView label
			polygonView.getAtkPolygon().label = newField.getName();
		}
				
		
		//Job related visual aspects of polygonView
		if(newJob == null){
			//Update polygon fillcolor
			polygonView.setFillColor(FieldView.FILL_COLOR_NOT_PLANNED);
		} else {
		
			if(job.getDeleted() != newJob.getDeleted()){
				if(newJob.getDeleted()){
					//Update polygonView fillcolor
					polygonView.setFillColor(FieldView.FILL_COLOR_NOT_PLANNED);
				}
			}
			
			if(newJob.getDeleted() == false && job.getStatus() != newJob.getStatus()){
				//Update polygonView fillcolor
				if(newJob.getStatus() == Job.STATUS_NOT_PLANNED){
					polygonView.setFillColor(FieldView.FILL_COLOR_NOT_PLANNED);
				} else if (newJob.getStatus() == Job.STATUS_PLANNED){
					polygonView.setFillColor(FieldView.FILL_COLOR_PLANNED);
				} else if (newJob.getStatus() == Job.STATUS_STARTED){
					polygonView.setFillColor(FieldView.FILL_COLOR_STARTED);
				} else if (newJob.getStatus() == Job.STATUS_DONE){
					polygonView.setFillColor(FieldView.FILL_COLOR_DONE);
				}
			}
		}
		
		//Done so update references
		field = newField;
		job = newJob;
	}
}
