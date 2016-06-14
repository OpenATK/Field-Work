package com.openatk.openatklib.atkmap.listeners;

import com.openatk.openatklib.atkmap.views.ATKPointView;

public interface ATKPointDragListener {
	//For each return true if you want to consume the event
	//return false if you want the event to be pasted to ATKMap's ATKPointDragListener if it is set
	
	public boolean onPointDrag(ATKPointView pointView);
	public boolean onPointDragEnd(ATKPointView pointView);
	public boolean onPointDragStart(ATKPointView pointView);
}
