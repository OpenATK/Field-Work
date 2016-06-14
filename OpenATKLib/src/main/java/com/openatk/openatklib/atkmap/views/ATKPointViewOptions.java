package com.openatk.openatklib.atkmap.views;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.openatk.openatklib.atkmap.listeners.ATKPointClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointDragListener;
import com.openatk.openatklib.atkmap.models.ATKPoint;

public class ATKPointViewOptions {
	

	private ATKPoint atkPoint;
	private Object data;
	private ATKPointViewIconAnchor anchor;
	private BitmapDescriptor icon;
	private Integer iconWidth;
	private Integer iconHeight;
	private ATKPointClickListener pointClickListener;
	private ATKPointDragListener pointDragListener;
	private Boolean superDraggable;
	private Boolean visible;

	public ATKPointViewOptions(){
		anchor = null;
		atkPoint = null;
		data = null;
		icon = null;
		iconWidth = null;
		iconHeight = null;
		pointClickListener = null;
		pointDragListener = null;
		superDraggable = null;
		visible = null;
	}
	
	class ATKPointViewIconAnchor {
		private float anchorHorizontal;
		private float anchorVertical;
		
		ATKPointViewIconAnchor(){
			this.anchorHorizontal = 0.5f;
			this.anchorVertical = 0.5f;
		}
		ATKPointViewIconAnchor(float horizontal, float vertical){
			this.anchorHorizontal = horizontal;
			this.anchorVertical = vertical;
		}
		public float getAnchorHorizontal() {
			return anchorHorizontal;
		}
		public void setAnchorHorizontal(float anchorHorizontal) {
			this.anchorHorizontal = anchorHorizontal;
		}
		public float getAnchorVertical() {
			return anchorVertical;
		}
		public void setAnchorVertical(float anchorVertical) {
			this.anchorVertical = anchorVertical;
		}
	}

	public ATKPointViewIconAnchor getAnchor() {
		return anchor;
	}

	public void setAnchor(ATKPointViewIconAnchor anchor) {
		this.anchor = anchor;
	}
	
	public void setAnchor(float horizontal, float vertical) {
		this.anchor = new ATKPointViewIconAnchor(horizontal, vertical);
	}

	public ATKPoint getAtkPoint() {
		return atkPoint;
	}

	public void setAtkPoint(ATKPoint atkPoint) {
		this.atkPoint = atkPoint;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public BitmapDescriptor getIcon() {
		return icon;
	}
	
	public void setIcon(BitmapDescriptor icon, int width, int height){
		this.iconHeight = height;
		this.iconWidth = width;
		this.icon = icon;
	}
	
	public void setIcon(Bitmap bitmapIcon){
		this.iconHeight = bitmapIcon.getHeight();
		this.iconWidth = bitmapIcon.getWidth();
		BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmapIcon);
		this.icon = icon;
	}

	public Integer getIconWidth() {
		return iconWidth;
	}

	public void setIconWidth(Integer iconWidth) {
		this.iconWidth = iconWidth;
	}

	public Integer getIconHeight() {
		return iconHeight;
	}

	public void setIconHeight(Integer iconHeight) {
		this.iconHeight = iconHeight;
	}

	public ATKPointClickListener getClickListener() {
		return pointClickListener;
	}

	public void setClickListener(ATKPointClickListener pointClickListener) {
		this.pointClickListener = pointClickListener;
	}

	public ATKPointDragListener getDragListener() {
		return pointDragListener;
	}

	public void setDragListener(ATKPointDragListener pointDragListener) {
		this.pointDragListener = pointDragListener;
	}

	public Boolean getSuperDraggable() {
		return superDraggable;
	}

	public void setSuperDraggable(Boolean superDraggable) {
		this.superDraggable = superDraggable;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

}
