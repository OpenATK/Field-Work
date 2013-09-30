package com.openatk.tillage;

import com.openatk.tillage.db.Field;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class FragmentAddField extends Fragment implements OnClickListener, OnCheckedChangeListener {

	private EditText name;
	private EditText acres;
	private CheckBox autoAcres;
	private AddFieldListener listener;
	private float autoAcresValue;
	
	
	// Interface for receiving data
	public interface AddFieldListener {
		public void AddFieldUndo();

		public void AddFieldDone(String name, Integer acres);

		public void AddFieldDelete();

		public Field AddFieldGetCurrentField();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_field, container,
				false);

		ImageButton butDone = (ImageButton) view
				.findViewById(R.id.add_field_done);
		ImageButton butUndo = (ImageButton) view
				.findViewById(R.id.add_field_undo);
		ImageButton butDelete = (ImageButton) view
				.findViewById(R.id.add_field_delete);

		name = (EditText) view.findViewById(R.id.add_field_name);
		acres = (EditText) view.findViewById(R.id.add_field_etAcres);
		autoAcres = (CheckBox) view.findViewById(R.id.add_field_chkAutoAcres);
		
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

						InputMethodManager imm = (InputMethodManager) getActivity()
								.getApplicationContext()
								.getSystemService(
										getActivity().getApplicationContext().INPUT_METHOD_SERVICE);
						imm.showSoftInput(acres,
								InputMethodManager.SHOW_IMPLICIT);
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
		getData();
	}

	public void getData() {
		if (listener == null) {
			Log.d("NULL", "NULL");
		}
		Field field = listener.AddFieldGetCurrentField();
		if (field != null) {
			name.setText(field.getName());
			acres.setText(Integer.toString(field.getAcres()) + " ac");
			autoAcres.setChecked(false);
			acres.setEnabled(true);
		} else {
			name.setText("");
			acres.setText("");
			autoAcres.setChecked(true);
			acres.setEnabled(false);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof AddFieldListener) {
			listener = (AddFieldListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implement FragmentAddField.OnClickListener");
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
			Integer intAcres = 0;
			String strAcres = acres.getText().toString();
			strAcres = strAcres.replace(" ", "");
			strAcres = strAcres.replace("ac", "");
			if (strAcres.length() > 0) {
				intAcres = Integer.parseInt(strAcres);
			}
			listener.AddFieldDone(name.getText().toString(), intAcres);
		} else if (v.getId() == R.id.add_field_undo) {
			listener.AddFieldUndo();
		} else if (v.getId() == R.id.add_field_delete) {
			new AlertDialog.Builder(this.getActivity())
			.setTitle("Delete Field")
			.setMessage("Are you sure you want to delete this field?")
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							listener.AddFieldDelete();
						}
					}).setNegativeButton("No", null).show();
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
		Log.d("FragmentAddField", "Check click");
		if(buttonView.getId() == R.id.add_field_chkAutoAcres){
			Log.d("FragmentAddField", "Check Auto acres");
			if(isChecked){
				Log.d("FragmentAddField", "Checked");
				int newAcres = (int) autoAcresValue;
				this.acres.setText(Integer.toString(newAcres) + " ac");
				this.acres.setEnabled(false);
			} else {
				this.acres.setEnabled(true);
			}
		}
	}
}
