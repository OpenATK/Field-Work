package com.openatk.openatklib.fragments;
import com.openatk.openatklib.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class ATKSliderFragment extends Fragment implements OnTouchListener {
	
	private static final String TAG = ATKSliderFragment.class.getSimpleName();

	
	private View container = null;
	
	private Boolean initialCreate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Use this to get views to fill it this.getArguments().get

		if(savedInstanceState == null){
			initialCreate = true;
		} else {
			initialCreate = false;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_slider_layout, container, false);
		view.setOnTouchListener(this);
	
		// If this is the first creation of the fragment, add child view
		if (initialCreate) {
			initialCreate = false;
			
			//add views to there layouts
			FrameLayout contentContainer = (FrameLayout) view.findViewById(R.id.fragment_slider_content_container);
			
			
		}
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d("FragmentSlider", "Attached");
	}	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	public int getHeight() {
		// Method so close transition can work
		return getView().getHeight();
	}
	
	public int oneNoteHeight() {
		//TODO return get from FragmentNoteList
		return 0;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float eventY = event.getRawY();
		
		switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
            	this.SliderDragDown((int)eventY);
               break; 
            }
            case MotionEvent.ACTION_UP:
            {     
            	 this.SliderDragUp((int)(eventY));
                 break;
            }
            case MotionEvent.ACTION_MOVE:
            {
            	this.SliderDragDragging((int)(eventY));
                break;
            }
        }
        return true;
	}
	
	private int sliderStartDrag = 0;
	private int sliderHeightStart = 0;
	private void SliderDragDown(int start) {
		if(container != null){
			int height = container.getHeight();
			FrameLayout layout = (FrameLayout) this.getView().findViewById(R.id.fragment_slider_content_container);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
			sliderStartDrag = height - start - params.height;
			sliderHeightStart = params.height;
		}
	}

	private void SliderDragDragging(int whereY) {
		if(container != null){
			int height = container.getHeight();
		
			FrameLayout layout = (FrameLayout) this.getView().findViewById(R.id.fragment_slider_content_container);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
			
			if((height - whereY - sliderStartDrag) > 0){
				params.height = height - whereY - sliderStartDrag;
			} else {
				params.height = 0;
			}
			layout.setLayoutParams(params);
		}
	}
	
	private void SliderDragUp(int whereY) {
		//Slider done dragging snap to 1 of 3 positions
		if(container != null){
			int oneThirdHeight = container.getHeight() / 3;
			if(whereY < oneThirdHeight){
				//Fullscreen
				Log.d("SliderDragUp", "fullscreen");
			} else if(whereY < oneThirdHeight * 2) {
				//Middle
				Log.d("SliderDragUp", "middle");
	
			} else {
				//Closed
				Log.d("SliderDragUp", "closed");
			}
			//Find end height
			FrameLayout layout = (FrameLayout) this.getView().findViewById(R.id.fragment_slider_content_container);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
			if(params.height > sliderHeightStart){
				//Make bigger
				SliderGrow();
			} else {
				//Make smaller
				SliderShrink();
			}
		}
	}
	
	private int sliderPosition = 0;
	private void SliderShrink(){
		if(container != null){
			int oneThirdHeight = container.getHeight() / 3;
			FrameLayout layout = (FrameLayout) this.getView().findViewById(R.id.fragment_slider_content_container);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
			if(sliderPosition == 2 || sliderPosition == 1){
				//Middle -> Small
				//OneNote -> Small
				DropDownAnim an = new DropDownAnim(layout, params.height, 0);
				an.setDuration(300);
				layout.startAnimation(an);
				sliderPosition = 0;
			} else if(sliderPosition == 3){
				//Fullscreen -> Middle if has notes
				//Fullscreen -> Small if no notes
				if(true){
					DropDownAnim an = new DropDownAnim(layout, params.height, oneThirdHeight);
					an.setDuration(300);
					layout.startAnimation(an);
					sliderPosition = 2;
				} else {
					DropDownAnim an = new DropDownAnim(layout, params.height, 0);
					an.setDuration(300);
					layout.startAnimation(an);
					sliderPosition = 0;
				}
			}
			layout.setLayoutParams(params);
		}
	}
	private void SliderGrow(){
		if(container != null){
			int oneThirdHeight = container.getHeight() / 3;	
			RelativeLayout relAdd = (RelativeLayout) this.getView().findViewById(R.id.fragment_slider_tab_container);
			Log.d("layMenu:", Integer.toString(relAdd.getHeight()));
			FrameLayout layout = (FrameLayout) this.getView().findViewById(R.id.fragment_slider_content_container);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
			if(sliderPosition == 0 || sliderPosition == 1){
				//Small -> Middle
				//OneNote -> Middle
				DropDownAnim an = new DropDownAnim(layout, params.height, oneThirdHeight);
				an.setDuration(300);
				layout.startAnimation(an);
				sliderPosition = 2;
			} else if(sliderPosition == 2){
				//Middle -> Fullscreen
				DropDownAnim an = new DropDownAnim(layout, params.height, (container.getHeight() - relAdd.getHeight()));
				an.setDuration(300);
				layout.startAnimation(an);
				sliderPosition = 3;
			}
			layout.setLayoutParams(params);
		}
	}
	private void SliderOneNote(){
		RelativeLayout relAdd = (RelativeLayout) this.getView().findViewById(R.id.fragment_slider_tab_container);
		Log.d("layMenu:", Integer.toString(relAdd.getHeight()));
		FrameLayout layout = (FrameLayout) this.getView().findViewById(R.id.fragment_slider_content_container);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
		
		DropDownAnim an = new DropDownAnim(layout, params.height, this.oneNoteHeight());
		an.setDuration(300);
		layout.startAnimation(an);
		sliderPosition = 1;
		
		layout.setLayoutParams(params);
	}
	
	public void SliderSizeMiddle(){
		if(sliderPosition == 3){
			this.SliderShrink();
		} else if(sliderPosition == 0){
			this.SliderGrow();
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
	    protected void applyTransformation(float interpolatedTime, Transformation t) {
	        int newHeight = (int) (startHeight - ((startHeight - targetHeight) * interpolatedTime));
	        view.getLayoutParams().height = newHeight;
	        view.requestLayout();
	    }

	    @Override
	    public void initialize(int width, int height, int parentWidth,
	            int parentHeight) {
	        super.initialize(width, height, parentWidth, parentHeight);
	    }

	    @Override
	    public boolean willChangeBounds() {
	        return true;
	    }
	}
}