package com.openatk.tillage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.openatk.tillage.db.DatabaseHelper;
import com.openatk.tillage.db.Field;
import com.openatk.tillage.db.Job;
import com.openatk.tillage.db.TableOperations;
import com.openatk.tillage.db.TableWorkers;
import com.openatk.tillage.db.Worker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
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
import android.widget.Spinner;
import android.widget.TextView;

public class FragmentEditJobPopup extends Fragment implements
		OnCheckedChangeListener, OnClickListener, OnItemSelectedListener {

	EditJobListener listener;
	private Field currentField = null;

	private Job currentJob = null;

	private TextView tvName;
	private TextView tvAcres;
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

	// Interface for receiving data
	public interface EditJobListener {
		public void EditJobSave(Job job);

		public void EditJobSave(Job job, Boolean changeState, Boolean unselect);

		public void EditJobDelete();

		public Field EditJobGetCurrentField();

		public Job EditJobGetCurrentJob();

		public void EditJobDateSave(int year, int month, int day);

		public void EditJobEditField();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_job_popup,
				container, false);

		dbHelper = new DatabaseHelper(this.getActivity());

		tvName = (TextView) view.findViewById(R.id.edit_field_tvName);
		tvAcres = (TextView) view.findViewById(R.id.edit_field_tvAcres);
		butEditField = (ImageButton) view
				.findViewById(R.id.edit_field_butEditField);

		chkPlanned = (CheckBox) view.findViewById(R.id.edit_field_chkPlanned);
		chkStarted = (CheckBox) view.findViewById(R.id.edit_field_chkStarted);
		chkDone = (CheckBox) view.findViewById(R.id.edit_field_chkDone);

		tvCalendar = (TextView) view.findViewById(R.id.edit_field_tvCalendar);
		butCalendar = (ImageButton) view
				.findViewById(R.id.edit_field_butCalendar);
		spinWorker = (Spinner) view.findViewById(R.id.edit_field_spinOperator);
		butNewWorker = (Button) view
				.findViewById(R.id.edit_field_butNewOperator);

		butDone = (ImageButton) view.findViewById(R.id.edit_field_butDone);
		etComment = (EditText) view.findViewById(R.id.edit_field_etComment);
		butDelete = (ImageButton) view.findViewById(R.id.edit_field_butDelete);

		chkPlanned.setOnCheckedChangeListener(this);
		chkStarted.setOnCheckedChangeListener(this);
		chkDone.setOnCheckedChangeListener(this);

		butEditField.setOnClickListener(this);
		butCalendar.setOnClickListener(this);

		butDone.setOnClickListener(this);
		etComment.addTextChangedListener(new MyTextWatcher(etComment));
		butDelete.setOnClickListener(this);

		loadWorkerList();
		spinWorker.setOnItemSelectedListener(this);
		spinWorkerAdapter = new ArrayAdapter<Worker>(this.getActivity(),
				android.R.layout.simple_list_item_1, workerList);
		spinWorker.setAdapter(spinWorkerAdapter);
		butNewWorker.setOnClickListener(this);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getData();
		// Return from save
		if (savedInstanceState != null) {
			// Populate info
			currentJob.setStatus(savedInstanceState.getInt("EditJobStatus"));
			currentJob.setWorkerName(savedInstanceState
					.getString("EditJobWorkerName"));
			currentJob.setComments(savedInstanceState
					.getString("EditJobComments"));
			currentJob.setDateOfOperation(savedInstanceState
					.getString("EditJobDateOfOperation"));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (currentJob != null)
			outState.putInt("EditJobStatus", currentJob.getStatus());
		if (currentJob != null)
			outState.putString("EditJobWorkerName", currentJob.getWorkerName());
		if (currentJob != null)
			outState.putString("EditJobComments", currentJob.getComments());
		if (currentJob != null)
			outState.putString("EditJobDateOfOperation",
					currentJob.getDateOfOperation());
		super.onSaveInstanceState(outState);
	}

	private void getData() {
		Log.d("FragmentEditJobPopup", "getData()");
		currentField = listener.EditJobGetCurrentField();
		currentJob = listener.EditJobGetCurrentJob();
		// Grab data from field and populate views
		if (currentField != null) {
			tvAcres.setVisibility(View.VISIBLE);
			butEditField.setVisibility(View.VISIBLE);
			if (currentField.getName().length() != 0)
				tvName.setText(currentField.getName());
			tvAcres.setText(Integer.toString(currentField.getAcres()));
		} else {
			Log.d("FragmentEditJobPopup - getData", "ERROR - current field is null from activity");
			if(currentJob != null){
				//Deleted field but it is still a job, disable field edit and acres
				tvName.setText(currentJob.getFieldName());
				tvAcres.setVisibility(View.GONE);
				butEditField.setVisibility(View.GONE);
			}
		}

		loadWorkerList();

		chkPlanned.setOnCheckedChangeListener(null);
		chkStarted.setOnCheckedChangeListener(null);
		chkDone.setOnCheckedChangeListener(null);
		// Grab data from job and populate views
		if (currentJob != null) {
			chkPlanned.setChecked(false);
			chkDone.setChecked(false);
			chkStarted.setChecked(false);
			if (currentJob.getStatus() == Job.STATUS_PLANNED) {
				chkPlanned.setChecked(true);
			} else if (currentJob.getStatus() == Job.STATUS_STARTED) {
				chkStarted.setChecked(true);
			} else if (currentJob.getStatus() == Job.STATUS_DONE) {
				chkDone.setChecked(true);
			}

			// Spinner
			selectWorkerInSpinner(currentJob.getWorkerName());
			etComment.setText(currentJob.getComments());

			Date d = DatabaseHelper.stringToDateLocal(currentJob
					.getDateOfOperation());
			if (dateIsToday(d)) {
				tvCalendar.setText("Today");
			} else {
				SimpleDateFormat displayFormat = new SimpleDateFormat(
						"MMM, dd", Locale.US);
				tvCalendar.setText(displayFormat.format(d));
			}
		} else if(currentField != null) {
			// New Job
			chkPlanned.setChecked(false);
			chkDone.setChecked(false);
			chkStarted.setChecked(false);
			Log.d("FragmentEditJobPopup - getData",
					"Making new job, currentJob is null");
			currentJob = new Job(currentField.getName());
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this.getActivity()
							.getApplicationContext());
			currentJob.setWorkerName(prefs.getString("WorkerName", ""));
			// set dateChanged and dateOfOperation to Now
			currentJob.setDateOfOperation(DatabaseHelper
					.dateToStringLocal(new Date()));
			currentJob.setDateChanged(DatabaseHelper
					.dateToStringUTC(new Date()));
		}
		chkPlanned.setOnCheckedChangeListener(this);
		chkStarted.setOnCheckedChangeListener(this);
		chkDone.setOnCheckedChangeListener(this);
	}

	public void refreshData() {
		Log.d("FragmentEditJobPopup", "RefreshData");
		getData();
	}

	public void updateDate(int year, int month, int day) {
		// Date from datepicker
		// Month is 0 based, just add 1
		String dateWyear = Integer.toString(year) + "-"
				+ Integer.toString(month + 1) + "-" + Integer.toString(day);
		SimpleDateFormat dateFormaterLocal = new SimpleDateFormat("yyyy-M-d",
				Locale.US);
		Date d;
		try {
			d = dateFormaterLocal.parse(dateWyear);
		} catch (ParseException e) {
			d = new Date(0);
		}
		currentJob.setDateOfOperation(DatabaseHelper.dateToStringLocal(d));
		if (dateIsToday(d)) {
			tvCalendar.setText("Today");
		} else {
			SimpleDateFormat displayFormat = new SimpleDateFormat("MMM, dd",
					Locale.US);
			tvCalendar.setText(displayFormat.format(d));
		}
		flushChangesAndSave(false, false); // Save changes
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof EditJobListener) {
			listener = (EditJobListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implement FragmentEditJob.EditJobListener");
		}
		Log.d("FragmentEditJobPopup", "Attached");
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.edit_field_butDone) {
			// Pass all info back to activity
			// TODO check if has changes
			flushChangesAndSave(true);
		} else if (v.getId() == R.id.edit_field_butDelete) {
			new AlertDialog.Builder(this.getActivity())
					.setTitle("Delete Job")
					.setMessage("Are you sure you want to delete this job?")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									listener.EditJobDelete();
								}
							}).setNegativeButton("No", null).show();
		} else if (v.getId() == R.id.edit_field_butCalendar) {
			DatePickerFragment newFragment = new DatePickerFragment();
			newFragment.setDate(DatabaseHelper.stringToDateLocal(currentJob
					.getDateOfOperation()));
			newFragment.show(this.getActivity().getSupportFragmentManager(),
					"datePicker");
		} else if (v.getId() == R.id.edit_field_butNewOperator) {
			// Create new worker
			createWorker();
		} else if (v.getId() == R.id.edit_field_butEditField) {
			listener.EditJobEditField();
		}
	}

	public void flushChangesAndSave(Boolean changeState, Boolean unselect) {
		flush();
		listener.EditJobSave(currentJob, changeState, unselect);
	}

	public void flushChangesAndSave(Boolean changeState) {
		flush();
		listener.EditJobSave(currentJob, changeState, true);
	}

	private void flush() {
		currentJob.setDateChanged(DatabaseHelper.dateToStringUTC(new Date()));
		currentJob.setHasChanged(1);
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
						flushChangesAndSave(false, false);
					} else if (currentJob.getStatus() == Job.STATUS_STARTED) {
						chkPlanned.setChecked(false);
						chkDone.setChecked(false);
						flushChangesAndSave(false, false);
					} else if (currentJob.getStatus() == Job.STATUS_DONE) {
						chkPlanned.setChecked(false);
						chkStarted.setChecked(false);
						flushChangesAndSave(false, false);
					}
				}
			} else {
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
				currentJob.setComments(text);
			}
		}
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		EditJobListener listener;
		Date date;

		@Override
		public void onAttach(Activity activity) {
			// TODO Auto-generated method stub
			super.onAttach(activity);
			if (activity instanceof EditJobListener) {
				listener = (EditJobListener) activity;
			} else {
				throw new ClassCastException(activity.toString()
						+ " must implement FragmentEditJob.EditJobListener");
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
			listener.EditJobDateSave(year, month, day);
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

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.new_worker_dialog_name);

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
									SQLiteDatabase database = dbHelper
											.getWritableDatabase();
									ContentValues values = new ContentValues();
									values.put(TableOperations.COL_HAS_CHANGED,
											1);
									values.put(TableOperations.COL_NAME, name);
									database.insert(TableWorkers.TABLE_NAME,
											null, values);

									dbHelper.close();
									loadWorkerList();
									selectWorkerInSpinner(name);

									// Save this choice in preferences for next
									// open
									SharedPreferences prefs = PreferenceManager
											.getDefaultSharedPreferences(getActivity()
													.getApplicationContext());
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

	private void loadWorkerList() {
		if (spinWorkerAdapter != null)
			spinWorkerAdapter.clear();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(TableWorkers.TABLE_NAME,
				TableWorkers.COLUMNS, null, null, null, null, null);
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
			if (spinWorkerAdapter != null)
				spinWorkerAdapter.add(worker);

			if (spinWorkerAdapter != null)
				spinWorkerAdapter.notifyDataSetChanged();
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this.getActivity()
							.getApplicationContext());
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
}
