package com.openatk.openatklib.atkmap.listeners;

import com.openatk.openatklib.atkmap.views.ATKPointView;
import com.openatk.openatklib.atkmap.views.ATKPolygonView;
import com.openatk.openatklib.atkmap.views.ATKPolylineView;

public interface ATKDrawListener {
	public void polygonDrawn(ATKPolygonView polygonView);
	public void polylineDrawn(ATKPolylineView polylineView);
	public void pointDrawn(ATKPointView pointView);
}
