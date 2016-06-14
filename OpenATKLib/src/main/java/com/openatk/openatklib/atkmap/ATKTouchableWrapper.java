package com.openatk.openatklib.atkmap;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.openatk.openatklib.atkmap.listeners.ATKTouchableWrapperListener;

public class ATKTouchableWrapper extends FrameLayout {
	
	private List<ATKTouchableWrapperListener> listeners;
	public ATKTouchableWrapper(Context context) {
		super(context);
		listeners = new ArrayList<ATKTouchableWrapperListener>();
	}
	
	public void addListener(ATKTouchableWrapperListener listener){
		this.listeners.add(listener);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		for(int i=0; i<listeners.size(); i++){
			if(listeners.get(i).onTouch(event) == true){
				return true; //Touch was consumed
			}
		}		
		return super.dispatchTouchEvent(event);
	}
	
	
}
