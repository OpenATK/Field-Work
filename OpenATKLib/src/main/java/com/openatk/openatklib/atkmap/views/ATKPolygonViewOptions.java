package com.openatk.openatklib.atkmap.views;

import android.graphics.Color;

public class ATKPolygonViewOptions {
	//View options
	private int strokeColor;
	private int fillColor;
	private float strokeWidth;
	private boolean visible;
	private float zindex;
	private int labelColor;
	private int labelSelectedColor;

	private boolean blnLabelSelected;
	
	public ATKPolygonViewOptions(){
		strokeColor = Color.argb(150, 150, 150, 150);
		fillColor = Color.argb(200, 200, 200, 200);
		strokeWidth = 3.0f;
		visible = true;
		zindex = 1.0f;
		labelColor = Color.BLACK;
		labelSelectedColor = Color.WHITE;
		blnLabelSelected = false;
	}
	
	public int getStrokeColor() {
		return strokeColor;
	}
	public void setStrokeColor(int strokeColor) {
		this.strokeColor = strokeColor;
	}
	public int getFillColor() {
		return fillColor;
	}
	public void setFillColor(int fillColor) {
		this.fillColor = fillColor;
	}
	public float getStrokeWidth() {
		return strokeWidth;
	}
	public void setStrokeWidth(float strokeWidth) {
		this.strokeWidth = strokeWidth;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public float getZindex() {
		return zindex;
	}
	public void setZindex(float zindex) {
		this.zindex = zindex;
	}
	public boolean isBlnLabelSelected() {
		return blnLabelSelected;
	}
	public void setBlnLabelSelected(boolean blnLabelSelected) {
		this.blnLabelSelected = blnLabelSelected;
	}
	public int getLabelColor() {
		return labelColor;
	}
	public void setLabelColor(int labelColor) {
		this.labelColor = labelColor;
	}
	public int getLabelSelectedColor() {
		return labelSelectedColor;
	}
	public void setLabelSelectedColor(int labelSelectedColor) {
		this.labelSelectedColor = labelSelectedColor;
	}	
	
}
