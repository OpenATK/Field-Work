package com.openatk.field_work;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.openatk.field_work.db.DatabaseHelper;
import com.openatk.field_work.db.TableOperations;
import com.openatk.field_work.db.TableWorkers;
import com.openatk.field_work.listeners.DatePickerListener;
import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Job;
import com.openatk.field_work.models.Worker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class FragmentEditJobPopup extends Fragment implements
		OnCheckedChangeListener, OnClickListener, OnItemSelectedListener, DatePickerListener {

	EditJobListener listener;
	
	private Field currentField = null;
	private Job currentJob = null;

	private TextView tvName;
	private TextView tvAcres;
	private TextView tvAcresLabel;
	private ImageButton butEditField;

	private CheckBox chkPlanned;
	private CheckBox chkStarted;
	private CheckBox chkDone;

	private TextView tvCalendar;
	private ImageButton butCalendar;

	private DatabaseHelper dbHelper;
	private List<Worker> workerList = null;
	private ArrayAdapter<Worker> spinWorkerAdapter = null;
	private Spinner spinWorker;
	private Button butNewWorker;

	private ImageButton butDone;
	private EditText etComment;
	private ImageButton butDelete;
	private RelativeLayout layout;
	private RelativeLayout layInfo1;
	private RelativeLayout layInfo2;

	private MyTextWatcher etCommentTextWatcher;
	
	private boolean retained = false;
	
	// Interface for receiving data
	public interface EditJobListener {
		public void FragmentEditJob_UpdateJob(Job job);
		public void FragmentEditJob_UpdateWorker(Worker worker);
		public void FragmentEditJob_Init();
		public void FragmentEditJob_EditField();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof EditJobListener) {
			listener = (EditJobListener) activity;
		} else {
			throw new ClassCastException(activity.toString() + " must implement FragmentEditJob.EditJobListener");
		}
		Log.d("FragmentEditJobPopup", "Attached");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_job_popup,container, false);
		
		layout = (RelativeLayout) view.findViewById(R.id.edit_field_layout);
		layInfo1 = (RelativeLayout) view.findViewById(R.id.edit_field_layInfo1);
		layInfo2 = (RelativeLayout) view.findViewById(R.id.edit_field_layInfo2);
		tvName = (TextView) view.findViewById(R.id.edit_field_tvName);
		tvAcres = (TextView) view.findViewById(R.id.edit_field_tvAcres);
		tvAcresLabel = (TextView) view.findViewById(R.id.edit_field_label_acres);
		butEditField = (ImageButton) view.findViewById(R.id.edit_field_butEditField);

		chkPlanned = (CheckBox) view.findViewById(R.id.edit_field_chkPlanned);
		chkStarted = (CheckBox) view.findViewById(R.id.edit_field_chkStarted);
		chkDone = (CheckBox) view.findViewById(R.id.edit_field_chkDone);

		tvCalendar = (TextView) view.findViewById(R.id.edit_field_tvCalendar);
		butCalendar = (ImageButton) view.findViewById(R.id.edit_field_butCalendar);
		spinWorker = (Spinner) view.findViewById(R.id.edit_field_spinOperator);
		butNewWorker = (Button) view.findViewById(R.id.edit_field_butNewOperator);

		butDone = (ImageButton) view.findViewById(R.id.edit_field_butDone);
		etComment = (EditText) view.findViewById(R.id.edit_field_etComment);
		butDelete = (ImageButton) view.findViewById(R.id.edit_field_butDelete);

		layout.setOnClickListener(this);
		
		chkPlanned.setOnCheckedChangeListener(this);
		chkStarted.setOnCheckedChangeListener(this);
		chkDone.setOnCheckedChangeListener(this);

		butEditField.setOnClickListener(this);
		butCalendar.setOnClickListener(this);
		tvCalendar.setOnClickListener(this);

		butDone.setOnClickListener(this);
		
		etCommentTextWatcher = new MyTextWatcher(etComment);
		etComment.addTextChangedListener(etCommentTextWatcher); //TODO wait till after populate?
		butDelete.setOnClickListener(this);
		
		spinWorker.setOnItemSelectedListener(this);
		spinWorkerAdapter = new ArrayAdapter<Worker>(this.getActivity(), android.R.layout.simple_list_item_1, workerList);
		spinWorker.setAdapter(spinWorkerAdapter);
		butNewWorker.setOnClickListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(retained == false) {
			retained = true;
			//Get data to populate views
			listener.FragmentEditJob_Init();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	private void updateJob(Job job) {
		Log.d("FragmentEditJobPopup", "updateJob()");

		if(job.getDeleted() || job.getId() == null){
			//Job deleted or new Job
			//Set field to not planned
			chkPlanned.setOnCheckedChangeListener(null);
			chkStarted.setOnCheckedChangeListener(null);
			chkDone.setOnCheckedChangeListener(null);
			
			chkPlanned.setChecked(false);
			chkDone.setChecked(false);
			chkStarted.setChecked(false);
			
			chkPlanned.setOnCheckedChangeListener(this);
			chkStarted.setOnCheckedChangeListener(this);
			chkDone.setOnCheckedChangeListener(this);
			
			//Hide comment, data, and operator until they make a job
			this.layInfo1.setVisibility(View.INVISIBLE);
			this.layInfo2.setVisibility(View.INVISIBLE);			
		} else {
			this.layInfo1.setVisibility(View.VISIBLE);
			this.layInfo2.setVisibility(View.VISIBLE);
			
			if(currentJob == null || currentJob.getStatus() != job.getStatus()){
				chkPlanned.setOnCheckedChangeListener(null);
				chkStarted.setOnCheckedChangeListener(null);
				chkDone.setOnCheckedChangeListener(null);
				
				chkPlanned.setChecked(false);
				chkDone.setChecked(false);
				chkStarted.setChecked(false);
				if(job.getStatus() == Job.STATUS_NOT_PLANNED){
					//Hide comment, data, and operator until they make a job
					this.layInfo1.setVisibility(View.INVISIBLE);
					this.layInfo2.setVisibility(View.INVISIBLE);	
				} else if(job.getStatus() == Job.STATUS_PLANNED){
					chkPlanned.setChecked(true);
				} else if (job.getStatus() == Job.STATUS_STARTED){
					chkStarted.setChecked(true);
				} else if (job.getStatus() == Job.STATUS_DONE){
					chkDone.setChecked(true);
				}
				
				chkPlanned.setOnCheckedChangeListener(this);
				chkStarted.setOnCheckedChangeListener(this);
				chkDone.setOnCheckedChangeListener(this);
			}
			
			if(currentJob == null || currentJob.getDateOfOperation() != job.getDateOfOperation()){
				if (dateIsToday(job.getDateOfOperation())) {
					tvCalendar.setText("Today");
				} else {
					SimpleDateFormat displayFormat = new SimpleDateFormat("MMM, dd", Locale.US);
					tvCalendar.setText(displayFormat.format(job.getDateOfOperation()));
				}
			}
			
			if(currentJob == null || currentJob.getWorkerName().contentEquals(job.getWorkerName()) == false){
				selectWorkerInSpinner(job.getWorkerName());
			}
			
			if(currentJob == null || currentJob.getComments().contentEquals(job.getComments()) == false){
				etComment.removeTextChangedListener(etCommentTextWatcher);
				etComment.setText(job.getComments());
				etComment.addTextChangedListener(etCommentTextWatcher);
			}
			
			if(currentField == null && (currentJob == null || currentJob.getFieldName().contentEquals(job.getFieldName()) == false)){
				//Only populate field name from job if field doesn't exist
				tvName.setText(job.getFieldName());
			}
		}
		
		currentJob = job;
	}
	private void updateField(Field field) {
		Log.d("FragmentEditJobPopup", "updateField()");
		
		if(field.getDeleted()) {
			//Field was deleted
			//Get data from job, hide edit button and acres
			if(currentJob != null && currentJob.getId() != null){
				//Deleted field but it is still a job, disable field edit and acres
				tvName.setText(currentJob.getFieldName());
				tvAcres.setVisibility(View.GONE);
				butEditField.setVisibility(View.GONE);
				tvAcresLabel.setVisibility(View.GONE);
			}
		} else {
			tvAcres.setVisibility(View.VISIBLE);
			butEditField.setVisibility(View.VISIBLE);
			tvAcresLabel.setVisibility(View.VISIBLE);
			
			if(currentField == null || field.getName().contentEquals(currentField.getName()) == false) {
				//Update field name
				tvName.setText(field.getName());
			}
			
			if(currentField == null || field.getAcres() != currentField.getAcres()) {
				tvAcres.setText(Float.toString(field.getAcres()));
			}
		}
		
		currentField = field;
	}
	
	@Override
	public void SelectDate(Date date) {
		// Date from datepicker
		currentJob.setDateOfOperation(date);
		if (dateIsToday(currentJob.getDateOfOperation())) {
			tvCalendar.setText("Today");
		} else {
			SimpleDateFormat displayFormat = new SimpleDateFormat("MMM, dd", Locale.US);
			tvCalendar.setText(displayFormat.format(currentJob.getDateOfOperation()));
		}
		listener.FragmentEditJob_UpdateJob(currentJob);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.edit_field_layout) {
			//Clicks anywhere on the popup
			this.closeKeyboard();
		} else if (v.getId() == R.id.edit_field_butDelete) {
			this.closeKeyboard();
			new AlertDialog.Builder(this.getActivity())
					.setTitle("Delete Job")
					.setMessage("Are you sure you want to delete this job?")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									currentJob.setDeleted(true);
									listener.FragmentEditJob_UpdateJob(currentJob);
								}
							}).setNegativeButton("No", null).show();
		} else if (v.getId() == R.id.edit_field_butCalendar || v.getId() == R.id.edit_field_tvCalendar) {
			this.closeKeyboard();
			DatePickerFragment newFragment = new DatePickerFragment();
			newFragment.setDate(currentJob.getDateOfOperation());
			newFragment.show(this.getActivity().getSupportFragmentManager(), "datePicker");
		} else if (v.getId() == R.id.edit_field_butNewOperator) {
			// Create new worker
			this.closeKeyboard();
			createWorker();
		} else if (v.getId() == R.id.edit_field_butEditField) {
			this.closeKeyboard();
			listener.FragmentEditJob_EditField();
		}
	}

	public int getHeight() {
		// Method so close transition can work
		return getView().getHeight();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (currentJob != null) {
			if (isChecked) {
				Boolean changed = false;
				if (buttonView.getId() == R.id.edit_field_chkPlanned) {
					if(currentJob.getStatus() != Job.STATUS_PLANNED){
						changed = true;
						currentJob.setStatus(Job.STATUS_PLANNED);
					}
				} else if (buttonView.getId() == R.id.edit_field_chkStarted) {
					if(currentJob.getStatus() != Job.STATUS_STARTED){
						changed = true;
						currentJob.setStatus(Job.STATUS_STARTED);
					}
				} else if (buttonView.getId() == R.id.edit_field_chkDone) {
					if(currentJob.getStatus() != Job.STATUS_DONE){
						changed = true;
						currentJob.setStatus(Job.STATUS_DONE);
					}
				}
				if(changed) {
					if (currentJob.getStatus() == Job.STATUS_PLANNED) {
						chkDone.setChecked(false);
						chkStarted.setChecked(false);
					} else if (currentJob.getStatus() == Job.STATUS_STARTED) {
						chkPlanned.setChecked(false);
						chkDone.setChecked(false);
					} else if (currentJob.getStatus() == Job.STATUS_DONE) {
						chkPlanned.setChecked(false);
						chkStarted.setChecked(false);
					}
					listener.FragmentEditJob_UpdateJob(currentJob);
				}
				this.layInfo1.setVisibility(View.VISIBLE);
				this.layInfo2.setVisibility(View.VISIBLE);
			} else {
				//Stay checked if we pressed the same button over again
				if (buttonView.getId() == R.id.edit_field_chkPlanned && currentJob.getStatus() == Job.STATUS_PLANNED) {
					chkPlanned.setChecked(true);
				} else if (buttonView.getId() == R.id.edit_field_chkStarted && currentJob.getStatus() == Job.STATUS_STARTED) {
					chkStarted.setChecked(true);
				} else if (buttonView.getId() == R.id.edit_field_chkDone && currentJob.getStatus() == Job.STATUS_DONE) {
					chkDone.setChecked(true);
				}
			}
		}
	}

	private class MyTextWatcher implements TextWatcher {
		private View view;

		private MyTextWatcher(View view) {
			this.view = view;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1,
				int i2) {
		}

		public void onTextChanged(CharSequence charSequence, int i, int i1,
				int i2) {
		}

		public void afterTextChanged(Editable editable) {
			String text = editable.toString();
			if (view.getId() == R.id.edit_field_etComment) {
				Log.d("Here", "TextWatcher!");
				currentJob.setComments(text);
			}
		}
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		DatePickerListener listener;
		Date date;

		@Override
		public void onAttach(Activity activity) {
			// TODO Auto-generated method stub
			super.onAttach(activity);
			if (activity instanceof DatePickerListener) {
				listener = (DatePickerListener) activity;
			} else {
				throw new ClassCastException(activity.toString() + " must implement FragmentEditJob.EditJobListener");
			}
			Log.d("FragmentEditJobPopup", "Attached");
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			// Month is 0 based, just add 1
			String dateWyear = Integer.toString(year) + "-" + Integer.toString(month + 1) + "-" + Integer.toString(day);
			SimpleDateFormat dateFormaterLocal = new SimpleDateFormat("yyyy-M-d", Locale.US);
			Date d;
			try {
				d = dateFormaterLocal.parse(dateWyear);
			} catch (ParseException e) {
				d = new Date(0);
			}
			listener.SelectDate(d);
		}
	}

	private Boolean dateIsToday(Date compare) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.US);
		String str1 = dateFormat.format(compare);
		String str2 = dateFormat.format(new Date());
		Log.d("FragmentEditJob", "Date:" + str1 + "=" + str2);
		if (str1.contentEquals(str2)) {
			return true;
		}
		return false;
	}

	// Worker Spinner
	private void createWorker() {
		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(this.getActivity());
		View promptsView = li.inflate(R.layout.new_worker_dialog, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this.getActivity());
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView.findViewById(R.id.new_worker_dialog_name);

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Add",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Create the operation
								String name = userInput.getText().toString();
								if (name.isEmpty() == false) {
									// Create new worker
								
									Worker newWorker = new Worker(name);
									listener.FragmentEditJob_UpdateWorker(newWorker);
									
									loadWorkerList();
									selectWorkerInSpinner(name);

									// Save this choice in preferences for next
									// open
									SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("defaultWorker", name);
									editor.commit();
								}
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	private void updateWorker(Worker worker) {
		//Update worker list, add, update, or remove
		
		if (spinWorkerAdapter != null) spinWorkerAdapter.clear();
		
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(TableWorkers.TABLE_NAME,TableWorkers.COLUMNS, null, null, null, null, null);
		workerList = new ArrayList<Worker>();
		while (cursor.moveToNext()) {
			Worker worker = Worker.cursorToWorker(cursor);
			if (worker != null)
				workerList.add(worker);
			if (spinWorkerAdapter != null) {
				if (worker != null)
					spinWorkerAdapter.add(worker);
			}
		}
		cursor.close();
		dbHelper.close();

		// Add create
		if (workerList.isEmpty() == false) {
			spinWorker.setVisibility(View.VISIBLE);
			butNewWorker.setVisibility(View.GONE);

			Worker worker = new Worker();
			worker.setId(null);
			worker.setName("New Operator");
			workerList.add(worker);
			if (spinWorkerAdapter != null) spinWorkerAdapter.add(worker);

			if (spinWorkerAdapter != null)
				spinWorkerAdapter.notifyDataSetChanged();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
			String toSelect = prefs.getString("defaultWorker", null);
			selectWorkerInSpinner(toSelect);
		} else {
			// Show button and hide spinner
			spinWorker.setVisibility(View.GONE);
			butNewWorker.setVisibility(View.VISIBLE);
		}
	}

	private void selectWorkerInSpinner(String workerName) {
		if (spinWorkerAdapter != null && workerName != null) {
			Boolean found = false;
			Boolean selectOperatorFound = false;
			for (int i = 0; i < spinWorkerAdapter.getCount(); i++) {
				if (spinWorkerAdapter.getItem(i).getName()
						.contentEquals(workerName)) {
					spinWorker.setSelection(i);
					found = true;
					break;
				} else if(spinWorkerAdapter.getItem(i).getName()
						.contentEquals("Select Operator")){
					selectOperatorFound = true;
				}
			}
			if (found == false) {
				// Add this worker and select
				Worker newWorker = null;
				if(workerName.isEmpty() && selectOperatorFound == false){
					workerName = "Select Operator";
					newWorker = new Worker();
					newWorker.setName(workerName);
					newWorker.setId(-1);
				} else if(workerName.isEmpty() == false) {
					newWorker = new Worker();
					newWorker.setName(workerName);
					newWorker.setId(-2);
				} else if(workerName.isEmpty()) {	
					selectWorkerInSpinner("Select Operator");
				}
				if (newWorker != null) {
					workerList.add(newWorker);
					if (spinWorkerAdapter != null) {
						spinWorkerAdapter.add(newWorker);
					}
					selectWorkerInSpinner(workerName);
				}				
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		Worker worker = (Worker) parent.getItemAtPosition(pos);
		Log.d("Selected:", worker.getName());
		if (worker.getId() == null) {
			// Create new operation
			selectWorkerInSpinner(currentJob.getWorkerName()); // Go back to
																// original for
																// now, in case
																// cancel
			createWorker();
		} else {
			String newName = worker.getName();
			if(worker.getId() == -1) newName = ""; //"Select Operator" selected
			currentJob.setWorkerName(newName);
			if(worker.getId() > 0){
				// Save this choice in preferences for next open
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(this.getActivity()
								.getApplicationContext());
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("defaultWorker", worker.getName());
				editor.commit();
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
	
	public void closing(){
		closeKeyboard();
	}
	
	private void closeKeyboard(){
		InputMethodManager imm =  (InputMethodManager)getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.etComment.getWindowToken(), 0);
	}


	
}
