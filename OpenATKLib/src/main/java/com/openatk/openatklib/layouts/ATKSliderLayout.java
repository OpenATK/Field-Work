package com.openatk.openatklib.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.openatk.openatklib.R;

public class ATKSliderLayout extends ViewGroup implements OnTouchListener {
	
	public static int SIZE_FULLSCREEN = 3;
	public static int SIZE_OPEN = 2;
	public static int SIZE_CLOSED = 1;
	public static int SIZE_HIDDEN = 0;

	//For sliding
	private int sliderPosition = SIZE_CLOSED;
	private int maxHeight;
	private int middleHeight;
	private int tabHeight = 0;
	private int sliderStartDrag = 0;
	private int sliderHeightStart = 0;
	
	private int hSpacing;
	private int vSpacing;
	private int currentHeight;
		
	public ATKSliderLayout(Context context) {
		super(context);
		// From code
	}

	public ATKSliderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// From xml

		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.ATKSliderLayout);
		try {
			hSpacing = a.getDimensionPixelSize(R.styleable.ATKSliderLayout_horizontalSpacing, 0);
			vSpacing = a.getDimensionPixelSize(R.styleable.ATKSliderLayout_horizontalSpacing, 0);
		} finally {
			a.recycle();
		}

		maxHeight = this.getResources().getDisplayMetrics().heightPixels;
		middleHeight = maxHeight / 3;		
		this.setOnTouchListener(this);
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		int width = getPaddingLeft();
		int height = getPaddingTop();

		int currentX = getPaddingLeft();
		
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {			
			View child = getChildAt(i);

			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			this.measureChild(child, widthMeasureSpec, heightMeasureSpec);

			//Position child
			lp.x = currentX;
			lp.y = height;
			
			//Add totals
			if (lp.breakLine) {
				//Tabs are the only ones the contribute to the height
				height += child.getMeasuredHeight();
			}
			width = Math.max(width, child.getMeasuredWidth());
			
			//Get ready for next child
			currentX = getPaddingLeft();
		}

		width += getPaddingRight();
		height += getPaddingBottom();
		
		tabHeight = height; //Update our tabHeight in case it changed
		
		setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();

			child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y + child.getMeasuredHeight());
		}	    
	}

	
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
	    }
	    SavedState ss = (SavedState)state;
	    super.onRestoreInstanceState(ss.getSuperState());
	    this.sliderPosition = ss.sliderPosition;
    	this.setSize(this.sliderPosition, false);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.sliderPosition = this.sliderPosition;
		return ss;
	}

	static class SavedState extends BaseSavedState {
		   int sliderPosition;

		    SavedState(Parcelable superState) {
		      super(superState);
		    }

		    private SavedState(Parcel in) {
		      super(in);
		      this.sliderPosition = in.readInt();
		    }

		    @Override
		    public void writeToParcel(Parcel out, int flags) {
		      super.writeToParcel(out, flags);
		      out.writeInt(this.sliderPosition);
		    }

		    //required field that makes Parcelables from a Parcel
		    public static final Parcelable.Creator<SavedState> CREATOR =
		        new Parcelable.Creator<SavedState>() {
		          public SavedState createFromParcel(Parcel in) {
		            return new SavedState(in);
		          }
		          public SavedState[] newArray(int size) {
		            return new SavedState[size];
		          }
		    };
	}
	
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p.width, p.height);
	}

	public static class LayoutParams extends ViewGroup.LayoutParams {
		public boolean breakLine;

		private int x;
		private int y;

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);

			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.ATKSliderLayout_LayoutParams);
			try {
				breakLine = a.getBoolean(R.styleable.ATKSliderLayout_LayoutParams_shown, false);
			} finally {
				a.recycle();
			}
		}
	}

	
	public void setSize(int size){
		setSize(size, true);
	}
	public void setSize(int size, boolean animate){
		Log.d("ATKSliderLayout", "SetSize:" + Integer.toString(size));
		if(size != ATKSliderLayout.SIZE_OPEN && size != ATKSliderLayout.SIZE_CLOSED && size != ATKSliderLayout.SIZE_FULLSCREEN && size != ATKSliderLayout.SIZE_HIDDEN){
			Log.e("ATKSlider", "Invaild size given for ATKSliderLayout.setSize()");
		} else {
			int newHeight = 0; //SIZE_HIDDEN
			if(size == SIZE_CLOSED){
				newHeight = tabHeight;
			} else if (size == SIZE_OPEN){
				newHeight = middleHeight;
			} else if(size == SIZE_FULLSCREEN) {
				newHeight = maxHeight;
			}
			sliderPosition = size;

			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
			if(animate){
				DropDownAnim an = new DropDownAnim(this, params.height, newHeight);
				an.setDuration(300);
				this.startAnimation(an);
			} else {
				params.height = newHeight;
			}
			this.setLayoutParams(params);
			this.currentHeight = params.height - tabHeight;
		}
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float eventY = event.getRawY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			this.SliderDragDown((int) eventY);
			break;
		}
		case MotionEvent.ACTION_UP: {
			this.SliderDragUp((int) (eventY));
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			this.SliderDragDragging((int) (eventY));
			break;
		}
		}
		return true;
	}
	private void SliderDragDown(int start) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
		sliderStartDrag = maxHeight - start - params.height;
		sliderHeightStart = params.height;
	}

	private void SliderDragDragging(int whereY) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
		if ((maxHeight - whereY - sliderStartDrag) > tabHeight) {
			params.height = maxHeight - whereY - sliderStartDrag;
		} else {
			params.height = tabHeight;
		}
		this.setLayoutParams(params);
	}

	private void SliderDragUp(int whereY) {
		// Find end height
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
		if (params.height > sliderHeightStart) {
			// Make bigger
			SliderGrow();
		} else {
			// Make smaller
			SliderShrink();
		}
	}
	private void SliderShrink() {
		if (sliderPosition == SIZE_OPEN) {
			// Open -> Closed
			this.setSize(SIZE_CLOSED);
		} else if (sliderPosition == SIZE_FULLSCREEN) {
			// Fullscreen -> Open
			this.setSize(SIZE_OPEN);
		}
	}

	private void SliderGrow() {
		if (sliderPosition == SIZE_CLOSED) {
			// Closed -> Open
			this.setSize(SIZE_OPEN);
		} else if (sliderPosition == SIZE_OPEN) {
			// Open -> Fullscreen
			this.setSize(SIZE_FULLSCREEN);
		}
	}
	private class DropDownAnim extends Animation {
		int targetHeight;
		int startHeight;
		View view;

		public DropDownAnim(View view, int startHeight, int targetHeight) {
			this.view = view;
			this.startHeight = startHeight;
			this.targetHeight = targetHeight;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			int newHeight = (int) (startHeight - ((startHeight - targetHeight) * interpolatedTime));
			view.getLayoutParams().height = newHeight;
			view.requestLayout();
		}

		@Override
		public void initialize(int width, int height, int parentWidth,int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
		}

		@Override
		public boolean willChangeBounds() {
			return true;
		}
	}

}
