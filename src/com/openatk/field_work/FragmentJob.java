package com.openatk.field_work;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import com.openatk.field_work.db.DatabaseHelper;
import com.openatk.field_work.db.TableJobs;
import com.openatk.field_work.db.TableOperations;
import com.openatk.field_work.db.TableWorkers;
import com.openatk.field_work.listeners.DatePickerListener;
import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Job;
import com.openatk.field_work.models.Operation;
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

public class FragmentJob extends Fragment implements
		OnCheckedChangeListener, OnClickListener, OnItemSelectedListener, DatePickerListener {

	FragmentJobListener listener;
	
	private Field currentField = null;
	private Job currentJob = null;
	private Operation currentOperation = null;

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
	private List<Worker> workerList = new ArrayList<Worker>();
	private ArrayAdapter<Worker> spinWorkerAdapter = null;
	private Spinner spinWorker;
	private Button butNewWorker;

	private EditText etComment;
	private ImageButton butDelete;
	private RelativeLayout layout;
	private RelativeLayout layInfo1;
	private RelativeLayout layInfo2;

	private MyTextWatcher etCommentTextWatcher;
	
	private boolean retained = false;
	
	// Interface for sending data
	public interface FragmentJobListener {
		public void FragmentJob_Init(); //This -> Listener
		public void FragmentJob_EditField(); //This -> Listener
		public void FragmentJob_UpdateJob(Job job); //This -> Listener
		public void FragmentJob_TriggerSync(); //This -> Listener
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof FragmentJobListener) {
			listener = (FragmentJobListener) activity;
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

		etComment = (EditText) view.findViewById(R.id.edit_field_etComment);
		butDelete = (ImageButton) view.findViewById(R.id.edit_field_butDelete);

		layout.setOnClickListener(this);
		
		chkPlanned.setOnCheckedChangeListener(this);
		chkStarted.setOnCheckedChangeListener(this);
		chkDone.setOnCheckedChangeListener(this);

		butEditField.setOnClickListener(this);
		butCalendar.setOnClickListener(this);
		tvCalendar.setOnClickListener(this);

		
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
			listener.FragmentJob_Init();
		} else {
			Log.d("FragmentJob", "Retained");
			currentJob = null;
			currentField = null;
			listener.FragmentJob_Init();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	
	
	@Override
	public void onPause() {
		chkPlanned.setOnCheckedChangeListener(null);
		chkStarted.setOnCheckedChangeListener(null);
		chkDone.setOnCheckedChangeListener(null);
		super.onPause();
	}

	@Override
	public void onResume() {
		chkPlanned.setOnCheckedChangeListener(this);
		chkStarted.setOnCheckedChangeListener(this);
		chkDone.setOnCheckedChangeListener(this);
		super.onResume();
	}

	public void updateJob(Job job) {
		Log.d("FragmentEditJobPopup", "updateJob()");
		if(job == null || job.getDeleted()){
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
			
			if(job.getDateOfOperation() == null) job.setDateOfOperation(new Date());
			
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
			
			if(job.getWorkerName() == null){
				//Select, "select worker" or press to add
			} else if(currentJob == null || currentJob.getWorkerName() == null || currentJob.getWorkerName().contentEquals(job.getWorkerName()) == false){
				if(workerList.size() > 0){
					selectWorkerInSpinner(job.getWorkerName());
				}
			}
			
			if(currentJob == null || (currentJob.getComments() != null && job.getComments() != null && currentJob.getComments().contentEquals(job.getComments()) == false)){
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
	
	
	public void init(DatabaseHelper dbHelper){
		this.dbHelper = dbHelper;
		loadWorkerList();
	}
	
	public void updateField(Field field) {
		Log.d("FragmentEditJobPopup", "updateField()");
		
		if(field == null || field.getDeleted()) {
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
			
			if(currentField == null || field.getName().contentEquals(tvName.getText().toString()) == false) {
				//Update field name
				tvName.setText(field.getName());
			}
			
			if(currentField == null || field.getAcres() != currentField.getAcres()) {
				tvAcres.setText(Float.toString(field.getAcres()));
			}
		}
		
		currentField = field;
	}
	
	public void updateOperation(Operation operation) {
		this.currentOperation = operation;
	}
	
	@Override
	public void SelectDate(Date date) {
		// Date from datepicker		
		if(currentJob.getDateOfOperation() == null) currentJob.setDateOfOperation(new Date());
		if(currentJob.getDateOfOperation().equals(date) == false) {
			currentJob.setDateOfOperation(date);
			if (dateIsToday(currentJob.getDateOfOperation())) {
				tvCalendar.setText("Today");
			} else {
				SimpleDateFormat displayFormat = new SimpleDateFormat("MMM, dd", Locale.US);
				tvCalendar.setText(displayFormat.format(currentJob.getDateOfOperation()));
			}
			
			//Save in db
			Job toUpdate = new Job(null);
			toUpdate.setId(currentJob.getId());
			toUpdate.setDateOfOperation(date);
			toUpdate.setDateDateOfOperationChanged(new Date());
			TableJobs.updateJob(dbHelper, toUpdate);
			
			//Tell MainActivity to sync remotely
			listener.FragmentJob_UpdateJob(currentJob);
		}
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
									
									//Save in db
									Job toUpdate = new Job(null);
									toUpdate.setId(currentJob.getId());
									toUpdate.setDeleted(true);
									toUpdate.setDateDeletedChanged(new Date());
									TableJobs.updateJob(dbHelper, toUpdate);
									listener.FragmentJob_UpdateJob(currentJob);

									currentJob = null;
									updateJob(currentJob);
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
			listener.FragmentJob_EditField();
		}
	}

	public int getHeight() {
		// Method so close transition can work
		return getView().getHeight();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(this.currentField == null || this.currentOperation == null) return;
		if (isChecked) {
			if(currentJob == null) {
				//Create a new job, set all defaults
				currentJob = new Job(this.currentOperation.getId(), currentField.getName());
				currentJob.setFieldName(currentField.getName());
				currentJob.setDateFieldNameChanged(new Date());
				currentJob.setOperationId(this.currentOperation.getId());
				currentJob.setDateOperationIdChanged(new Date());
				
				currentJob.setDateOfOperation(new Date());
				currentJob.setDateDateOfOperationChanged(new Date());
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());				
				
				currentJob.setWorkerName(prefs.getString("defaultWorker", ""));
				currentJob.setDateWorkerNameChanged(new Date());

				currentJob.setDeleted(false);
				currentJob.setDateDeletedChanged(new Date());
				
				//Save in db, this will set it's id
				TableJobs.updateJob(dbHelper, currentJob);
			}
			
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
				
				//Save in db
				Job toUpdate = new Job(null);
				toUpdate.setId(currentJob.getId());
				toUpdate.setStatus(currentJob.getStatus());
				toUpdate.setDateStatusChanged(new Date());
				TableJobs.updateJob(dbHelper, toUpdate);
				
				listener.FragmentJob_UpdateJob(currentJob);
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
			if (view.getId() == R.id.edit_field_etComment && currentJob != null) {
				currentJob.setComments(text);
				
				//Save in db
				Job toUpdate = new Job(null);
				toUpdate.setId(currentJob.getId());
				toUpdate.setComments(currentJob.getComments());
				toUpdate.setDateCommentsChanged(new Date());
				TableJobs.updateJob(dbHelper, toUpdate);
				listener.FragmentJob_TriggerSync();
			}
		}
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		DatePickerListener listener;
		Date date = new Date();

		@Override
		public void onAttach(Activity activity) {
			// TODO Auto-generated method stub
			super.onAttach(activity);
			if (activity instanceof DatePickerListener) {
				listener = (DatePickerListener) activity;
			} else {
				throw new ClassCastException(activity.toString() + " must implement FragmentEditJob.DatePickerListener");
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
			if(date == null) date = new Date();
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
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
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
									newWorker.setDateNameChanged(new Date());
									newWorker.setDeleted(false);
									newWorker.setDateDeletedChanged(new Date());
									//Save in database and get id
									TableWorkers.updateWorker(dbHelper, newWorker);
									//Tell mainactivity to trigger a remote sync
									listener.FragmentJob_TriggerSync();
									loadWorkerList();
									selectWorkerInSpinner(name);									
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

	private void loadWorkerList(){
		if (spinWorkerAdapter != null) spinWorkerAdapter.clear();
		workerList.clear();
		
		List<Worker> workers = dbHelper.readWorkers();
		if (workers.isEmpty() == false) {
			Log.d("loadWorkerList", "Have workers");

			spinWorker.setVisibility(View.VISIBLE);
			butNewWorker.setVisibility(View.GONE);

			Worker worker = new Worker("New Operator");
			worker.setId(null);
			workerList.add(worker);
			
			for(int i =0; i<workers.size(); i++){
				workerList.add(workers.get(i));
			}

			if(spinWorkerAdapter != null) spinWorkerAdapter.notifyDataSetChanged();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
			String toSelect = prefs.getString("defaultWorker", null);
			selectWorkerInSpinner(toSelect);
		} else {
			Log.d("loadWorkerList", "No workers");
			// Show button and hide spinner
			spinWorker.setVisibility(View.GONE);
			butNewWorker.setVisibility(View.VISIBLE);
		}
	}
	
	public void updateWorker(Worker worker) {
		//Update worker list, add, update, or remove
		
		//If removed worker is selected do nothing
		if(worker.getDeleted()){
			if(currentJob == null || currentJob.getWorkerName().contentEquals(worker.getName()) == false){
				//Remove deleted worker from the list
				workerList.remove(worker);
				if(spinWorkerAdapter != null) spinWorkerAdapter.notifyDataSetChanged();
			} else if(currentJob.getWorkerName().contentEquals(worker.getName())){
				//Update worker id as -1
				for(int i=0; i<workerList.size(); i++){
					if(workerList.get(i).getId() == worker.getId()){
						worker.setId(-1);
						workerList.set(i, worker);
						if(spinWorkerAdapter != null) spinWorkerAdapter.notifyDataSetChanged();
						return;
					}
				}
			}
			
			if(workerList.size() == 1){
				spinWorker.setVisibility(View.GONE);
				butNewWorker.setVisibility(View.VISIBLE);
			}
			return;
		}
		
		//Update worker in list
		for(int i=0; i<workerList.size(); i++){
			if(workerList.get(i).getId() == worker.getId()){
				workerList.set(i, worker);
				if(spinWorkerAdapter != null) spinWorkerAdapter.notifyDataSetChanged();
				return;
			}
		}
		
		//Add worker in list
		workerList.add(worker);
		if(spinWorkerAdapter != null) spinWorkerAdapter.notifyDataSetChanged();
	}

	private void selectWorkerInSpinner(String workerName) {
		cleanWorkers(workerName); //Clean out deleted workers
		if(workerName == null){
			//Load from saved choice
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
			workerName = prefs.getString("defaultWorker", null);
		}
		
		if(workerName == null){
			//If still null, could be because of trello syncing or no workers
			if(workerList.size() > 1){
				for(int i=0; i<workerList.size(); i++){
					if(workerList.get(i).getId() != null){
						workerName = workerList.get(i).getName();
						break;
					}
				}
			} else {
				// Show button and hide spinner
				spinWorker.setVisibility(View.GONE);
				butNewWorker.setVisibility(View.VISIBLE);
				return;
			}
		}
		
		if (spinWorkerAdapter != null && workerName != null) {
			boolean found = false;
			for (int i = 0; i < spinWorkerAdapter.getCount(); i++) {
				if (spinWorkerAdapter.getItem(i).getName().contentEquals(workerName)) {
					spinWorker.setSelection(i);
					found = true;
				}
			}
			if(found == false){
				//This worker isn't found in normal dataset, he was deleted but still in this job
				//Add him to the dropdown until it changes.
				Worker worker = new Worker(workerName);
				worker.setId(-1);
				workerList.add(worker);
				if(spinWorkerAdapter != null) spinWorkerAdapter.notifyDataSetChanged();
				selectWorkerInSpinner(workerName);
			}
		}
	}
	
	private void cleanWorkers(String worker){
		//Clean out all deleted workers from worker list that are not "worker"
		if(workerList != null) {			
			Iterator<Worker> iter = workerList.iterator();
			while (iter.hasNext()) {
				Worker aWorker = iter.next();
				if(aWorker.getId() != null && aWorker.getId() == -1 && (worker == null || aWorker.getName().contentEquals(worker) == false)) {
			        iter.remove();
			    }
			}
			if(spinWorkerAdapter != null) spinWorkerAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Worker worker = (Worker) parent.getItemAtPosition(pos);
		Log.d("Selected:", worker.getName());
		if (worker.getId() == null) {
			// Create new worker selected
			// Select original in case of cancel
			if(currentJob != null && currentJob.getWorkerName() != null) selectWorkerInSpinner(currentJob.getWorkerName());
			createWorker();
		} else {
			String newName = worker.getName();
			if(currentJob != null && (currentJob.getWorkerName() == null || currentJob.getWorkerName().contentEquals(newName) == false)){
				currentJob.setWorkerName(newName);
				currentJob.setDateWorkerNameChanged(new Date());
			}
			if(currentJob != null){
				Job toUpdate = new Job(null);
				toUpdate.setId(currentJob.getId());
				toUpdate.setDateWorkerNameChanged(new Date());
				toUpdate.setWorkerName(currentJob.getWorkerName());
				TableJobs.updateJob(dbHelper, toUpdate);
				listener.FragmentJob_TriggerSync();
			}
			
			// Save this choice in preferences for next open
			if(worker.getId() != -1) {
				//If this is not a worker that is deleted, then make it default.
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
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
		InputMethodManager inputManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    //check if no view has focus:
	    View v=this.getActivity().getCurrentFocus();
	    if(v != null){
	    	inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}

	
}
