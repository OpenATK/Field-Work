package com.openatk.field_work.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.widget.RelativeLayout;

public class RelativeLayoutKeyboardDetect extends RelativeLayout {
	
	private Rect rect;
	private Point size;
	
	public RelativeLayoutKeyboardDetect(Context context, AttributeSet attrs) {
		super(context, attrs);
		rect = new Rect();
		size = new Point();
	}

	public interface KeyboardChangeListener {
        public void onSoftKeyboardShown(boolean isShowing);
    }
    private KeyboardChangeListener listener;
    public void setListener(KeyboardChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Activity activity = (Activity) getContext();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        
        Display display = activity.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        int screenHeight = size.y;
        
        int diff = (screenHeight - statusBarHeight) - height;
        if (listener != null) {
            listener.onSoftKeyboardShown(diff>128); // assume all soft keyboards are at least 128 pixels high
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);       
    }
	
	
}
