package com.openatk.field_work;


import java.text.DecimalFormat;
import java.util.Date;

import com.openatk.field_work.db.DatabaseHelper;
import com.openatk.field_work.db.TableFields;
import com.openatk.field_work.models.Field;
import com.openatk.field_work.views.FieldView;
import com.openatk.openatklib.atkmap.listeners.ATKPolygonDrawListener;
import com.openatk.openatklib.atkmap.views.ATKPolygonView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class FragmentAddField extends Fragment implements OnClickListener, OnCheckedChangeListener, ATKPolygonDrawListener {

	private EditText name;
	private EditText acres;
	private CheckBox autoAcres;
	private FragmentAddFieldListener listener;
	private float autoAcresValue;
	
	private View layout;
	
	private FieldView fieldview;
	public boolean keyboardShowing = false;
	
	private static float DECIMAL_ACRES_LIMIT = 3.0f;
	
	// Interface for receiving data
	public interface FragmentAddFieldListener {
		public void FragmentAddField_Undo(); //This -> Listener
		public void FragmentAddField_Init(); //This -> Listener
		public void FragmentAddField_Done(FieldView field);  //This -> Listener
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_field, container,false);

		ImageButton butDone = (ImageButton) view.findViewById(R.id.add_field_done);
		ImageButton butUndo = (ImageButton) view.findViewById(R.id.add_field_undo);
		ImageButton butDelete = (ImageButton) view.findViewById(R.id.add_field_delete);
		layout = (View) view.findViewById(R.id.add_field_layout);
		
		name = (EditText) view.findViewById(R.id.add_field_name);
		acres = (EditText) view.findViewById(R.id.add_field_etAcres);
		autoAcres = (CheckBox) view.findViewById(R.id.add_field_chkAutoAcres);

		layout.setOnClickListener(this);
		
		acres.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (acres.getText().toString().contains("ac")) {
						acres.setText(acres
								.getText()
								.toString()
								.subSequence(0,
										acres.getText().toString().length() - 3));
						acres.selectAll();

						getActivity().getApplicationContext();
						InputMethodManager imm = (InputMethodManager) getActivity()
								.getApplicationContext()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(acres, InputMethodManager.SHOW_FORCED);
					}
				} else {
					if (acres.getText().toString().contains("ac") == false) {
						acres.setText(acres.getText().toString() + " ac");
					}
					if (acres.getText().toString().length() == 3) {
						acres.setText("");
					}
				}
			}
		});

		butDone.setOnClickListener(this);
		butUndo.setOnClickListener(this);
		butDelete.setOnClickListener(this);
		
		autoAcres.setOnCheckedChangeListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		listener.FragmentAddField_Init();
	}

	public void init(FieldView fieldView) {
		if(fieldView != null) {
			this.fieldview = fieldView;
			Field field = fieldView.getField();
			
			if (field != null && field.getId() != null && field.getId() != -1) {
				name.setText(field.getName());
				String strAcres;
				if(field.getAcres() < DECIMAL_ACRES_LIMIT){
					DecimalFormat df = new DecimalFormat("#.#");
					strAcres = df.format(field.getAcres());
				} else {
					DecimalFormat df = new DecimalFormat("#");
					strAcres = df.format(field.getAcres());
				}
				acres.setText(strAcres + " ac");
				autoAcres.setChecked(false); //TODO only turn off if calculated acres != fields acres
				acres.setEnabled(true);
			} else {
				//New field
				name.setText("");
				acres.setText("");
				autoAcres.setChecked(true);
				acres.setEnabled(false);
			}
			
			this.fieldview.getPolygonView().setOnDrawListener(this);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof FragmentAddFieldListener) {
			listener = (FragmentAddFieldListener) activity;
		} else {
			throw new ClassCastException(activity.toString() + " must implement FragmentAddField.FragmentAddFieldListener");
		}
		Log.d("FragmentAddField", "Attached");
	}

	public int getHeight() {
		// Method so close transition can work
		return getView().getHeight();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.add_field_done) {
			Float fltAcres = 0.0f;
			String strAcres = acres.getText().toString();
			strAcres = strAcres.replace(" ", "");
			strAcres = strAcres.replace("ac", "");
			if (strAcres.length() > 0) {
				fltAcres = Float.parseFloat(strAcres);
			}
			
			this.fieldview.getField().setAcres(fltAcres);
			this.fieldview.getField().setName(name.getText().toString().trim());
			fieldview.getPolygonView().setOnDrawListener(null);
			listener.FragmentAddField_Done(this.fieldview);
		} else if (v.getId() == R.id.add_field_undo) {
			listener.FragmentAddField_Undo();
		} else if (v.getId() == R.id.add_field_delete) {
			new AlertDialog.Builder(this.getActivity())
			.setTitle("Delete Field")
			.setMessage("Are you sure you want to delete this field?")
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							fieldview.getField().setDeleted(true);
							fieldview.getPolygonView().setOnDrawListener(null);
							listener.FragmentAddField_Done(fieldview);
						}
					}).setNegativeButton("No", null).show();
		} else {
			this.closeKeyboard();
		}
	}
	
	
	public void autoAcres(float acres){
		Log.d("FragmentAddField", "autoAcres:" + Float.toString(acres));
		autoAcresValue = acres;
		if(this.autoAcres != null && this.autoAcres.isChecked()){
			int newAcres = (int) acres;
			this.acres.setText(Integer.toString(newAcres) + " ac");
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(buttonView.getId() == R.id.add_field_chkAutoAcres){
			if(isChecked){
				Log.d("FragmentAddField", "Checked");
				this.afterBoundaryChange(this.fieldview.getPolygonView());
				this.acres.setEnabled(false);
			} else {
				this.acres.setEnabled(true);
			}
		}
	}

	@Override
	public boolean beforeBoundaryChange(ATKPolygonView polygonView) {
		//If keyboard is up, dont allow the boundary to change, put down keyboard
		if(this.keyboardShowing){
			if(this.keyboardShowing == true) {
				closeKeyboard();
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean afterBoundaryChange(ATKPolygonView polygonView) {
		autoAcresValue = polygonView.getAcres();
		if(this.autoAcres != null && this.autoAcres.isChecked()){
			if(this.autoAcresValue < DECIMAL_ACRES_LIMIT){
				DecimalFormat df = new DecimalFormat("#.#");
				String strAcres = df.format(autoAcresValue);
				this.acres.setText(strAcres + " ac");
			} else {
				this.acres.setText(Integer.toString((int) this.autoAcresValue) + " ac");
			}
		}
		return false;
	}
	
	private void closeKeyboard(){
		InputMethodManager inputManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    //check if no view has focus:
	    View v=this.getActivity().getCurrentFocus();
	    if(v != null){
	    	inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
}
