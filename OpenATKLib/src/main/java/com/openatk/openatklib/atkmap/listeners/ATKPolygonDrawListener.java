package com.openatk.openatklib.atkmap.listeners;

import com.openatk.openatklib.atkmap.views.ATKPolygonView;

public interface ATKPolygonDrawListener {
	public boolean beforeBoundaryChange(ATKPolygonView polygonView);
	public boolean afterBoundaryChange(ATKPolygonView polygonView);
}
