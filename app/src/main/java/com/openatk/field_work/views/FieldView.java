package com.openatk.field_work.views;


import android.graphics.Color;
import android.util.Log;

import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Job;
import com.openatk.openatklib.atkmap.ATKMap;
import com.openatk.openatklib.atkmap.models.ATKPolygon;
import com.openatk.openatklib.atkmap.views.ATKPolygonView;

public class FieldView {
	
	//Defaults move to Constants Class?
	public static float STROKE_WIDTH = 2.0f;
	public static int STROKE_COLOR_NORMAL = Color.BLACK;
	public static int STROKE_COLOR_SELECTED = Color.WHITE;
	
	public static int LABEL_COLOR_NORMAL = Color.BLACK;
	public static int LABEL_COLOR_SELECTED = Color.WHITE;
	
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
		this(state, field, job, null, map);
	}

	public FieldView(int state, Field field, Job job, ATKPolygonView polygonView, ATKMap map) {
		super();
		this.state = state;
		this.field = field;
		this.job = job;
		this.polygonView = polygonView;
		this.map = map;
		if(polygonView != null) {
			this.polygonView.setData(this);
			this.polygonView.setLabelSelectedColor(FieldView.LABEL_COLOR_NORMAL);
			this.polygonView.setLabelSelectedColor(FieldView.LABEL_COLOR_SELECTED);
			this.setState(this.state, true);
		}
		this.update(this.field, this.job, true);
	}
	
	public Integer getFieldId(){
		if(this.field == null)  return null;
		return this.field.getId();
	}
	
	public Field getField(){
		return this.field;
	}
	
	public void setField(Field field){
		this.field = field;
	}
	
	public Job getJob(){
		return this.job;
	}
	
	public ATKPolygonView getPolygonView(){
		return this.polygonView;
	}
	
	public void setState(int newState){
		setState(newState, false);
	}
	public void setState(int newState, boolean force){
		if(this.state != newState || force){
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
			
			this.state = newState;
		}
	}
	public void update(Field newField, Job newJob){
		this.update(newField, newJob, false);
	}
	public void update(Field newField, Job newJob, boolean force){
		//Compares data and updates map accordingly or adds to map if doesn't exist
		if(newField.getId() != -1){
			if(newField.getDeleted()){
				//Remove the field from the map and we are done
				Log.d("FieldView update", "update deleted");
				if(polygonView != null) map.removePolygon(polygonView.getAtkPolygon());
				return;
			}
			
			if(polygonView == null){
				Log.d("FieldView update", "Add polygon to map");
				ATKPolygon atkPoly = new ATKPolygon(newField.getId(), newField.getBoundary(), newField.getName());
				//This adds the polygon to the map so it should be visible after this
				polygonView = map.addPolygon(atkPoly);
				polygonView.setData(this);
				polygonView.setLabelColor(FieldView.LABEL_COLOR_NORMAL);
				polygonView.setLabelSelectedColor(FieldView.LABEL_COLOR_SELECTED);
				setState(this.state, true);
			}
			
			//Check all visual aspects of FieldView for changes
			
			//Field related visual aspects of polygonView
			if(polygonView.getAtkPolygon().boundary.equals(newField.getBoundary()) == false){
				//Update polygonView boundary
				Log.d("FieldView update", "Update boundary");
				polygonView.getAtkPolygon().boundary = newField.getBoundary();
			}
			
			if(polygonView.getAtkPolygon().label.contentEquals(newField.getName()) == false){
				//Update polygonView label
				Log.d("FieldView update", "Update name");
				polygonView.setLabel(newField.getName());
			}
					

			
			//Job related visual aspects of polygonView
			if(newJob == null){
				//Update polygon fillcolor
				Log.d("FieldView update", "Update fillcolor, no job");
	
				polygonView.setFillColor(FieldView.FILL_COLOR_NOT_PLANNED);
			} else {
			
				if(newJob.getDeleted()){
					Log.d("FieldView update", "Update fillcolor deleted");
					//Update polygonView fillcolor
					polygonView.setFillColor(FieldView.FILL_COLOR_NOT_PLANNED);
				}
							
				if(newJob.getDeleted() == false){
					//Update polygonView fillcolor
					Log.d("FieldView update", "Update fillcolor");
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
			
			polygonView.update();
		}
		//Done so update references
		field = newField;
		job = newJob;
	}
}
