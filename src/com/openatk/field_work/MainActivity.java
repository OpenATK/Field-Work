package com.openatk.field_work;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.openatk.field_work.FragmentAddField.FragmentAddFieldListener;
import com.openatk.field_work.FragmentJob.FragmentJobListener;
import com.openatk.field_work.FragmentListView.ListViewListener;
import com.openatk.field_work.db.DatabaseHelper;
import com.openatk.field_work.db.TableFields;
import com.openatk.field_work.db.TableJobs;
import com.openatk.field_work.db.TableOperations;
import com.openatk.field_work.db.TableWorkers;
import com.openatk.field_work.listeners.DatePickerListener;
import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Job;
import com.openatk.field_work.models.Operation;
import com.openatk.field_work.models.Worker;
import com.openatk.field_work.views.FieldView;
import com.openatk.field_work.views.RelativeLayoutKeyboardDetect;
import com.openatk.field_work.views.RelativeLayoutKeyboardDetect.KeyboardChangeListener;
import com.openatk.libtrello.TrelloContentProvider;
import com.openatk.libtrello.TrelloSyncHelper;
import com.openatk.libtrello.TrelloSyncInfo;
import com.openatk.openatklib.atkmap.ATKMap;
import com.openatk.openatklib.atkmap.ATKSupportMapFragment;
import com.openatk.openatklib.atkmap.listeners.ATKMapClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointDragListener;
import com.openatk.openatklib.atkmap.listeners.ATKPolygonClickListener;
import com.openatk.openatklib.atkmap.views.ATKPointView;
import com.openatk.openatklib.atkmap.views.ATKPolygonView;

public class MainActivity extends FragmentActivity implements OnClickListener, FragmentAddFieldListener, FragmentJobListener, DatePickerListener,
		OnItemSelectedListener, ListViewListener, ATKPointDragListener, ATKMapClickListener, ATKPolygonClickListener, ATKPointClickListener, KeyboardChangeListener {
	
	public static final String INTENT_FIELD_UPDATED = "com.openatk.fieldwork.field.UPDATED";
	public static final String INTENT_JOB_UPDATED = "com.openatk.fieldwork.job.UPDATED";
	public static final String INTENT_OPERATION_UPDATED = "com.openatk.fieldwork.operation.UPDATED";
	public static final String INTENT_WORKER_UPDATED = "com.openatk.fieldwork.worker.UPDATED";
	
	public static final String INTENT_FIELD_DELETED = "com.openatk.fieldwork.field.DELETED";
	public static final String INTENT_JOB_DELETED = "com.openatk.fieldwork.job.DELETED";
	public static final String INTENT_OPERATION_DELETED = "com.openatk.fieldwork.operation.DELETED";
	public static final String INTENT_WORKER_DELETED = "com.openatk.fieldwork.worker.DELETED";
	
	public static final String INTENT_ALL_WORKERS_DELETED = "com.openatk.fieldwork.workers.DELETED";
	public static final String INTENT_ALL_FIELDS_DELETED = "com.openatk.fieldwork.fields.DELETED";
	
	public static final String INTENT_EVERYTHING_DELETED = "com.openatk.fieldwork.everything.DELETED";

	public static final String PREF_GONE = "Gone";
	public static final int PREF_GONE_HERE = 0;
	public static final int PREF_GONE_NO_UPDATE = 1;
	public static final Integer PREF_GONE_UPDATE = 2;
	
	
	public static final int ID_FIELD_DRAWING = -100;

	public static final int STATE_DEFAULT = 0;
	public static final int STATE_LIST_VIEW = 1;

	
	private ATKMap map;
	private UiSettings mapSettings;
	private ATKSupportMapFragment atkMapFragment;
	
    //Startup position
 	private static final float START_LAT = 40.428712f;
 	private static final float START_LNG = -86.913819f;
 	private static final float START_ZOOM = 17.0f;

	private ActionBar actionBar = null;
	private EditText actionBarSearch = null;
	private DatabaseHelper dbHelper;
	private Menu menu;
	
	private int mCurrentState;
	private boolean keyboardIsShowing = false;

	private List<Operation> operationsList = new ArrayList<Operation>();;
	private ArrayAdapter<Operation> spinnerMenuAdapter = null;
	private Spinner spinnerMenu = null;

	FragmentJob fragmentJob = null;
	FragmentListView fragmentListView = null;
	FragmentAddField fragmentAddField = null;
	
	private List<FieldView> fieldViews = new ArrayList<FieldView>();

	private Bundle savedInstanceState;
	
	private FieldView currentFieldView;	
	private Operation currentOperation;
	
	private TrelloSyncHelper syncHelper;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dbHelper = new DatabaseHelper(this);
		syncHelper = new TrelloSyncHelper();
		
		//Get fragments
		FragmentManager fm = getSupportFragmentManager();
		atkMapFragment = (ATKSupportMapFragment) fm.findFragmentById(R.id.map);
		fragmentJob = (FragmentJob) fm.findFragmentByTag("edit_job");
		fragmentAddField = (FragmentAddField) fm.findFragmentByTag("add_field");
		
		//Show and hide listview really fast. TODO is there a better way to do this
		//I would like this to retain if possible...
		fragmentListView = (FragmentListView) fm.findFragmentById(R.id.list_view);
		FragmentTransaction ft = fm.beginTransaction();
		ft.hide(fragmentListView); //TODO check state, are we in list view state? if so don't hide
		ft.commit();
		
		if (savedInstanceState == null) {
			// First incarnation of this activity.
			atkMapFragment.setRetainInstance(true);
			fragmentListView.setRetainInstance(true);
			if(fragmentJob != null) fragmentJob.setRetainInstance(true);
			if(fragmentAddField != null) fragmentAddField.setRetainInstance(true);
			
			fragmentJob = null;
			fragmentAddField = null;
		} else {
			// Reincarnated activity. The obtained map is the same map instance
			// in the previous
			// activity life cycle. There is no need to reinitialize it.
			map = atkMapFragment.getAtkMap();
		}

		actionBar = getActionBar();
		// Specify that a dropdown list should be displayed in the action bar.
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Hide the title
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,ActionBar.DISPLAY_SHOW_CUSTOM);
		
		LayoutInflater inflater = (LayoutInflater) this.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.action_bar, null);
		
		RelativeLayout item = (RelativeLayout) view.findViewById(R.id.action_bar_layout);
		
		spinnerMenu = (Spinner) view.findViewById(R.id.action_bar_operation_spinner);
		actionBarSearch = (EditText) view.findViewById(R.id.action_bar_search_box);
		actionBar.setCustomView(item, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.LEFT));

		actionBarSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == false) {
					closeKeyboard();
				}
			}
		});
		actionBarSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if(fragmentListView != null) fragmentListView.search(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}			
		});
		

		// Get last selected items on rotate
		this.savedInstanceState = savedInstanceState;



		// Specify a SpinnerAdapter to populate the dropdown list.
		spinnerMenuAdapter = new ArrayAdapter<Operation>(this, R.layout.operation_list_item, operationsList);
		spinnerMenu.setAdapter(spinnerMenuAdapter);
		
		
		RelativeLayoutKeyboardDetect layout = (RelativeLayoutKeyboardDetect) this.findViewById(R.id.mainActivity); 
		layout.setListener(this);
 		
		setUpMapIfNeeded();
		if(this.savedInstanceState != null){
			int viewState = savedInstanceState.getInt("currentViewState", STATE_DEFAULT);
			setState(viewState);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(currentFieldView != null) outState.putInt("currentField", currentFieldView.getFieldId());	
		outState.putInt("currentViewState", mCurrentState);	
		super.onSaveInstanceState(outState);
	}

	private void setUpMapIfNeeded() {
		if (map == null) {
			//TODO IDK if we need this, check beginning of onCreate
			map = ((ATKSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getAtkMap();
		}
		
		if (atkMapFragment.getRetained() == false) {
			Log.d("setUpMapIfNeeded", "New map need to set it up");
			
			//New map, we need to set it up
			setUpMap();
			
			SharedPreferences prefs = this.getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE);
    		Float startLat = prefs.getFloat("StartupLat", START_LAT);
    		Float startLng = prefs.getFloat("StartupLng", START_LNG);
    		Float startZoom = prefs.getFloat("StartupZoom", START_ZOOM);
    		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startLat,startLng) , startZoom));
    		
    		//TODO do stuff for hazards
    		//this.showingHazards = prefs.getBoolean("showingHazards", false);
		} else {
			Log.d("setUpMapIfNeeded", "Old map, get everything from it");

			
			//Get the current field and job
			Integer selectedField = -100;
			if(this.savedInstanceState != null){
				selectedField = savedInstanceState.getInt("currentField", -100);
			}
			
			SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE  | Context.MODE_MULTI_PROCESS);
			//Check if sync occurred while we were gone.
			if(prefs.getInt(MainActivity.PREF_GONE, MainActivity.PREF_GONE_NO_UPDATE) == MainActivity.PREF_GONE_UPDATE){
				//We need to update the entire screen... Cloud sync occurred while we were gone and it made changes.
				//We will do this in onResume after we register our receivers.
			} else {
				//Old map we need to get all our data from it
				//Get the FieldViews from ATKMap
				List<ATKPolygonView> polygonViews =  map.getPolygonViews();
				for(int i=0; i<polygonViews.size(); i++){
					FieldView fieldView = (FieldView) polygonViews.get(i).getData();
					this.fieldViews.add(fieldView);
					Log.d("setUpMapIfNeeded", "selected field:" + Integer.toString(selectedField));
					Log.d("setUpMapIfNeeded", "fieldView:" + Integer.toString(fieldView.getFieldId()));
					if(fieldView.getFieldId() == selectedField){
						this.currentFieldView = fieldView;
					}
				}
			}
		}
		Log.d("setUpMapIfNeeded", "map was setup");
		//Setup stuff for new activity
		map.setOnMapClickListener(this);
		map.setOnPolygonClickListener(this);
		map.setOnPointClickListener(this);
		map.setOnPointDragListener(this);
		this.updateCurrentOperation();
	}

	private void setUpMap(){
		Log.d("MainActivity", "setUpMap()");
		mapSettings = map.getUiSettings();
		mapSettings.setZoomControlsEnabled(false);
		mapSettings.setMyLocationButtonEnabled(false);
		mapSettings.setTiltGesturesEnabled(false);
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setMyLocationEnabled(true);

		createFieldViews();
	}
	
	
	private void updateCurrentOperation(){
		// Load operations from database
		loadOperations();
		
		// Find current operation
		currentOperation = null;
		SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE);
		int operationId = prefs.getInt("currentOperationId", -1);
		if(operationId != -1){
			currentOperation = TableOperations.FindOperationById(dbHelper, operationId);
			if(currentOperation != null) selectCurrentOperationInSpinner();
		}
		spinnerMenu.setOnItemSelectedListener(this);
	}
	private void createFieldViews(){
		createFieldViews(null);
	}
	private void createFieldViews(Integer selectedFieldViewId){
		//Read database and get all the fields and make the FieldViews		
		fieldViews.clear();

		//Read all the fields
		List<Field> fields = dbHelper.readFields();
		//Read all the jobs for the current operation
		List<Job> jobs; 
		if(currentOperation == null) {
			jobs = new ArrayList<Job>(); //No jobs
		} else {
			jobs = dbHelper.readJobsByOperationId(this.currentOperation.getId());
		}

		//Now create fieldviews jobs to fields
		for(int i=0; i<fields.size(); i++){
			Field field = fields.get(i);			
			
			//Find the job for this field
			Job job = null;
			Iterator<Job> jobIterator = jobs.iterator();
		    while(jobIterator.hasNext()){
		    	Job curJob = jobIterator.next();
		    	if(curJob.getDeleted() == true){
		    		jobIterator.remove();
		    		continue;
		    	}
		    	if(curJob.getFieldName() != null && curJob.getFieldName().contentEquals(field.getName())){
		    		//Found a match, link it then remove from array so its faster next time
		    		job = curJob;
		    		jobIterator.remove(); //Remove from jobs arraylist
		    		break;
		    	}
		    	if(curJob.getFieldName() == null){
		    		Log.w("MainActivity", "Have a job with a null field name");
		    	}
		    }
		    
		    int state = FieldView.STATE_NORMAL;
		    if(currentFieldView != null && field.getId() == currentFieldView.getFieldId()){
		    	state = FieldView.STATE_SELECTED;
		    }
		    
		    if(job != null) {
		    	//Setup a blank job for this field, it doesn't have one yet
		    	Log.d("MainActivity", "Job Status:" + Integer.toString(job.getStatus()));
		    }
		    		    
		    //Finally, create our new FieldView
		    FieldView newFieldView = new FieldView(state, field, job, map);
		    if(selectedFieldViewId != null){
		    	if(newFieldView.getFieldId() == selectedFieldViewId){
		    		currentFieldView = newFieldView;
		    	}
		    }
		    fieldViews.add(newFieldView);
		}
	}
	private void updateOperation(){
		if(fieldViews != null){
			List<Job> jobs; 
			if(currentOperation == null) {
				jobs = new ArrayList<Job>(); //No jobs
			} else {
				jobs = dbHelper.readJobsByOperationId(this.currentOperation.getId());
			}
						
			for(int i=0; i<this.fieldViews.size(); i++){
				FieldView fview = fieldViews.get(i);
				if(currentOperation == null){
					Log.d("updateOperation", "current op null");
					fview.update(fview.getField(), null);
				} else if(fview.getJob() == null || fview.getJob().getOperationId() != currentOperation.getId()){
					Boolean found = false;
					Iterator<Job> jobIterator = jobs.iterator();
				    while(jobIterator.hasNext()){
				    	Job curJob = jobIterator.next();
				    	if(curJob.getDeleted() == true){
				    		jobIterator.remove();
				    		continue;
				    	}
				    	if(curJob.getFieldName() != null && curJob.getFieldName().contentEquals(fview.getField().getName())){
				    		//Found a match, link it then remove from array so its faster next time
				    		fview.update(fview.getField(), curJob);
				    		jobIterator.remove(); //Remove from jobs arraylist
				    		found = true;
				    		break;
				    	}
				    }
					Log.d("updateOperation", "Found:" + Boolean.toString(found));

				    if(found == false){
				    	//No job for this field with this operation
						fview.update(fview.getField(), null);
				    }
				}
			}
		}
	}
	private FieldView updateFieldView(Field field){
		return this.updateFieldView(field, null);
	}
	private FieldView updateFieldView(Field field, Job job){
		//Look for field in current list of fieldViews
		FieldView fieldView = null;
		boolean foundFieldView = false;
		if(field.getId() != null){
			for(int i=0; i<fieldViews.size(); i++){
				fieldView = fieldViews.get(i);
				if(fieldView.getFieldId() != null && fieldView.getFieldId() == field.getId()){
					fieldView = fieldViews.get(i);
					foundFieldView = true;
					Log.d("updateFieldView", "Found the fieldview name:" + field.getName());
					Log.d("updateFieldView", "Found the fieldview id:" + Integer.toString(fieldView.getFieldId()));
					break;
				}
			}
		}
		
		if(foundFieldView == false){
			Log.d("updateFieldView", "FieldView not found, making a new field.");
		    //Finally, create our new FieldView
		    fieldView = new FieldView(FieldView.STATE_NORMAL, field, null, map);
		    if(job == null) job = fieldView.getJob();
			fieldView.update(field, job);
		    fieldViews.add(fieldView);
		} else {
			Log.d("updateFieldView", "Updating existing fieldview");
			//Update existing field view
			if(job == null) job = fieldView.getJob();
			if(job != null && job.getDeleted() == true) job = null;
			if(job != null && job.getOperationId() != this.currentOperation.getId()) job = null;
			fieldView.update(field, job);
			if(field.getDeleted()){
				fieldViews.remove(fieldView);
				if(this.currentFieldView == fieldView) this.currentFieldView = null;
			}
		}
		return fieldView;
	}
	private boolean removeFieldView(FieldView toRemove){
		//Look for field in current list of fieldViews
		if(toRemove == null) return false;
		Boolean ret = false;
		if(toRemove.getFieldId() != null){
			for(int i=0; i<fieldViews.size(); i++){
				FieldView fieldView = fieldViews.get(i);
				if(fieldView.getFieldId() != null && fieldView.getFieldId() == toRemove.getFieldId()){
					fieldViews.remove(i);
					Log.d("removeFieldView", "Removed from field views");
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
	private FieldView findFieldView(Field field){
		FieldView fieldView = null;
		if(field.getId() != null){
			for(int i=0; i<fieldViews.size(); i++){
				fieldView = fieldViews.get(i);
				if(fieldView.getFieldId() != null && fieldView.getFieldId() == field.getId()){
					fieldView = fieldViews.get(i);
					Log.d("updateFieldView", "Found the fieldview");
					break;
				}
			}
		}
		return fieldView;
	}
	@Override
	protected void onPause() {
		super.onPause();
        
        CameraPosition myCam = map.getCameraPosition();
		if(myCam != null){
			SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE  | Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor editor = prefs.edit();
			LatLng where = myCam.target;
			editor.putFloat("StartupLat", (float) where.latitude);
			editor.putFloat("StartupLng",(float) where.longitude); 
			editor.putFloat("StartupZoom",(float) map.getCameraPosition().zoom); 
			editor.putInt(MainActivity.PREF_GONE,MainActivity.PREF_GONE_NO_UPDATE); 
			editor.commit();
		}
		//Set pref so everyone knows MainActivity is no longer here (listening)
		SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE  | Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(MainActivity.PREF_GONE, MainActivity.PREF_GONE_NO_UPDATE);
		editor.commit();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReciever);
		
		syncHelper.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, new IntentFilter(MainActivity.INTENT_FIELD_UPDATED));
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, new IntentFilter(MainActivity.INTENT_JOB_UPDATED));
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, new IntentFilter(MainActivity.INTENT_OPERATION_UPDATED));
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, new IntentFilter(MainActivity.INTENT_WORKER_UPDATED));
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, new IntentFilter(MainActivity.INTENT_FIELD_DELETED));
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, new IntentFilter(MainActivity.INTENT_JOB_DELETED));
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, new IntentFilter(MainActivity.INTENT_OPERATION_DELETED));
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, new IntentFilter(MainActivity.INTENT_WORKER_DELETED));
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, new IntentFilter(MainActivity.INTENT_EVERYTHING_DELETED));

		this.syncHelper.onResume(this); //Startup autosync if it is on
		
		SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE  | Context.MODE_MULTI_PROCESS);
		
		Boolean syncOccured = false;
		//Didn't do a full reload, check if sync occurred while we were gone.
		if(prefs.getInt(MainActivity.PREF_GONE, MainActivity.PREF_GONE_NO_UPDATE) == MainActivity.PREF_GONE_UPDATE){
			//We need to update the entire screen... Cloud sync occurred while we were gone and it made changes.
			//Clear atkmaps data
			Log.w("MainActivity - onResume", "Sync occured while we were gone. Reload everything.");
			map.clear();
			
			//Get the current selected field if there is one
			Integer selectedField = null;
			if(this.savedInstanceState != null){
				selectedField = savedInstanceState.getInt("currentField", -100);
				if(selectedField == -100) selectedField = null;
			}
			
			this.currentFieldView = null;
			
			//Reload all fields from the db
			this.createFieldViews(selectedField);
		
			//Reload the operations
			this.updateCurrentOperation();
			
			//Update fragmentJob if its up
			this.updateFragmentJob();
			
			//Reload the list view data
			if(this.fragmentListView != null) this.fragmentListView.getData();
			syncOccured = true;
		}
		
		//Set pref so everyone knows MainActivity is active
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(MainActivity.PREF_GONE, MainActivity.PREF_GONE_HERE);
		editor.commit();
		if(syncOccured) {
			//Sync again in case this is a new organization
			this.syncHelper.sync(this);
		}
		//drawHazards();
	}
	
	
	private BroadcastReceiver broadcastReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("MainActivity BroadcastReceiver", "Recieved intent");
			Bundle extras = intent.getExtras();
			if(intent.getAction().contentEquals(MainActivity.INTENT_FIELD_UPDATED)){
				int id = intent.getIntExtra("id", -1);
				if(id != -1){
					Log.d("MainActivity BroadcastReceiver", "INTENT_FIELD_UPDATED");

					Job job = null;

					//Load this field from db and update accordingly
					Field theField = TableFields.FindFieldById(dbHelper, id);
					if(theField == null){
						theField = new Field();
						theField.setId(id);
						theField.setDeleted(true);
						//Since this is a new field we should check for jobs
						if(currentOperation != null) job = TableJobs.FindJobByFieldName(dbHelper, theField.getName(), currentOperation.getId());
					} else {
						/*if(intent.getBooleanExtra("insert", false) && currentOperation != null){
							//New field, check if it has any jobs so we can color it on the map
							job = TableJobs.FindJobByFieldName(dbHelper, theField.getName(), currentOperation.getId());
						}*/
						//Check in all cases, because if field name changed we could of loaded the job changes first
						if(currentOperation != null){
							job = TableJobs.FindJobByFieldName(dbHelper, theField.getName(), currentOperation.getId());
							if(job == null){
								//Make it clear the status, no job exists for this field name anymore
								job = new Job();
								job.setDeleted(true);
							}
						}
					}
					
					updateFieldView(theField, job);
					updateFragmentJob();
				}
			} else if(intent.getAction().contentEquals(MainActivity.INTENT_FIELD_DELETED)){
				int id = intent.getIntExtra("id", -1);
				if(id != -1){
					Log.d("MainActivity BroadcastReceiver", "INTENT_FIELD_DELETED");

					Field theField = new Field();
					theField.setId(id);
					theField.setDeleted(true);
						
					updateFieldView(theField);
					updateFragmentJob();
				}
			} else if(intent.getAction().contentEquals(MainActivity.INTENT_JOB_UPDATED)){
				int id = intent.getIntExtra("id", -1);
				if(id != -1 && currentOperation != null){
					Log.d("MainActivity BroadcastReceiver", "INTENT_JOB_UPDATED");

					//Load this field from db and update accordingly
					Job theJob = TableJobs.FindJobById(dbHelper, id);
					if(theJob == null) {
						Log.w("MainActivity - BroadcastReceiver onReceive", "INTENT_JOB_UPDATED - no job found with this id:" + Integer.toString(id));
					} else {
						//Update old field if fieldname changed or if operation id changed
						if(intent.getStringExtra("oldFieldName") != null) {
							//Make job of old field null
							Field theField = TableFields.FindFieldByName(dbHelper, intent.getStringExtra("oldFieldName"));
							Job job = new Job();
							job.setDeleted(true); //So it clears status on fieldview
							if(theField != null) updateFieldView(theField, job);
						}
						if(intent.getIntExtra("oldOperationId", -1) != -1) {
							Integer oldOpId = intent.getIntExtra("oldOperationId", -1);
							if(currentOperation != null && oldOpId == currentOperation.getId() && theJob.getOperationId() != currentOperation.getId()){
								//Update job of fieldview, job now belongs to another operation, no longer in our active one, look for other jobs for this field
								Field theField = TableFields.FindFieldByName(dbHelper, theJob.getFieldName());
								
								Job job = null;
								if(theField != null) job = TableJobs.FindJobByFieldName(dbHelper, theField.getName(), currentOperation.getId());								
								if(job == null) {
									job = new Job();
									job.setDeleted(true); //So it clears status on fieldview
								}
								if(theField != null) updateFieldView(theField, job);
							}
						}
						if(theJob.getOperationId() != null && currentOperation != null && theJob.getOperationId() == currentOperation.getId()){
							Field theField = TableFields.FindFieldByName(dbHelper, theJob.getFieldName());
							if(theField != null){
								Log.d("MainActivity", "Set fieldview to new job");
								updateFieldView(theField, theJob);
							} else {
								Log.w("MainActivity - BroadcastReceiver onReceive", "INTENT_JOB_UPDATED - no field found with field name:" + theJob.getFieldName());
							}
						}
					}
					if(theJob.getOperationId() != null && currentOperation != null && theJob.getOperationId() == currentOperation.getId()) updateFragmentJob();
				}
			} else if(intent.getAction().contentEquals(MainActivity.INTENT_JOB_DELETED)){
				Log.d("MainActivity BroadcastReceiver", "INTENT_JOB_DELETED");
				int id = intent.getIntExtra("id", -1);
				String fieldName = intent.getStringExtra("fieldName");
				if(id != -1 && fieldName != null){
					//Load this field from db and update accordingly
					Field theField = TableFields.FindFieldByName(dbHelper, fieldName);
					if(theField != null){
						Job job = new Job();
						job.setId(id);
						job.setDeleted(true);
						updateFieldView(theField, job);
					} else {
						Log.w("MainActivity - BroadcastReceiver onReceive", "INTENT_JOB_DELETED - no field given.");
					}
					updateFragmentJob();
				} else {
					Log.w("MainActivity - BroadcastReceiver onReceive", "INTENT_JOB_DELETED - id was null or fieldname was not given.");
				}
			} else if(intent.getAction().contentEquals(MainActivity.INTENT_OPERATION_UPDATED)){
				//This could be a new operation or a name update
				//Find operation in db and add it to the list, update name if its a name update
				int id = intent.getIntExtra("id", -1);
				if(id != -1){
					Operation operation = TableOperations.FindOperationById(dbHelper, id);
					//Check if it is a new operation, update name if it isn't
					boolean newOp = true;
					for(int i=0; i<operationsList.size(); i++){
						if(operationsList.get(i).getId() != null && id == operationsList.get(i).getId()){
							//Update name in list
							operationsList.set(i, operation);
							if (spinnerMenuAdapter != null) spinnerMenuAdapter.notifyDataSetChanged();
							newOp = false;
							break;
						}
					}
					if(newOp){
						//If it was previously empty add "New Opertion" to the list
						if(operationsList.size() == 0) {
							Operation newOperationButton = new Operation();
							newOperationButton.setName("New Operation");
							operationsList.add(newOperationButton);
						}
						//Add the operation to the list
						operationsList.add(operation);
						if (spinnerMenuAdapter != null) spinnerMenuAdapter.notifyDataSetChanged();
					}
				}
			} else if(intent.getAction().contentEquals(MainActivity.INTENT_OPERATION_DELETED)){
				//If it isn't currently selected remove it from the list, if it is selected then
				//Show toast message, and select another if there is one
				int id = intent.getIntExtra("id", -1);
				
				//Delete it from the list
				int it = -1;
				for(int i=0; i<operationsList.size(); i++){
					if(operationsList.get(i).getId() != null && id == operationsList.get(i).getId()){
						it = i;
						break;
					}
				}
				if(it != -1){
					operationsList.remove(it);
				}
				
				
				if(currentOperation.getId() != null && currentOperation.getId() == id){
					hideFragmentJob(true);
					
					Toast.makeText(getApplicationContext(), "Current operation was deleted remotely.", Toast.LENGTH_LONG).show();
					currentOperation = null;
					for(int i=0; i<operationsList.size(); i++){
						if(operationsList.get(i).getId() != null){
							currentOperation = operationsList.get(i);
							if(spinnerMenuAdapter != null) spinnerMenuAdapter.notifyDataSetChanged();
							selectCurrentOperationInSpinner();
							break;
						}
					}
					if(currentOperation == null){
						loadOperations(); //No operations left....
						updateOperation();
					}
				} else {
					if(spinnerMenuAdapter != null) spinnerMenuAdapter.notifyDataSetChanged();
				}

			} else if(intent.getAction().contentEquals(MainActivity.INTENT_WORKER_UPDATED)){
				Log.d("MainActivity BroadcastReceiver", "INTENT_WORKER_UPDATED");
				int id = intent.getIntExtra("id", -1);
				if(id != -1){
					Worker toUpdate = TableWorkers.FindWorkerById(dbHelper, id);
					if(fragmentJob != null) fragmentJob.updateWorker(toUpdate);
				}  else {
					Log.w("MainActivity - BroadcastReceiver onReceive", "INTENT_WORKER_UPDATED - id was null.");
				}
			} else if(intent.getAction().contentEquals(MainActivity.INTENT_WORKER_DELETED)){
				Log.d("MainActivity BroadcastReceiver", "INTENT_WORKER_DELETED");
				int id = intent.getIntExtra("id", -1);
				String workerName = intent.getStringExtra("workerName");
				if(id != -1 && workerName != null){
					Worker toUpdate = new Worker();
					toUpdate.setName(workerName);
					toUpdate.setDeleted(true);
					toUpdate.setId(id);
					if(fragmentJob != null) fragmentJob.updateWorker(toUpdate);
				} else {
					Log.w("MainActivity - BroadcastReceiver onReceive", "INTENT_WORKER_DELETED - id was null or fieldname was not given.");
				}
			} else if(intent.getAction().contentEquals(MainActivity.INTENT_EVERYTHING_DELETED)){
				//Organization or board has changed, delete everything
				TableOperations.deleteAll(dbHelper);
				TableFields.deleteAll(dbHelper);
				TableJobs.deleteAll(dbHelper);
				TableWorkers.deleteAll(dbHelper);
				//Now we need to update the map to reflect the changes
				currentOperation = null;
				currentFieldView = null;
				loadOperations(); //No operations left....
				updateOperation();
				hideFragmentJob(true);
				
				for(int i=0; i<fieldViews.size();i++){
					Field field = fieldViews.get(i).getField();
					field.setDeleted(true);
					fieldViews.get(i).update(field, null);
				}
				fieldViews.clear();
			}
		}
	};


	
	public void loadOperations() {	
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(TableOperations.TABLE_NAME, TableOperations.COLUMNS, null, null, null, null, null);
		while (cursor.moveToNext()) {
			Operation operation = TableOperations.cursorToOperation(cursor);
			if (operation != null) operationsList.add(operation);
		} 
		cursor.close();
		database.close();
		dbHelper.close();
		
		List<Operation> operations = dbHelper.readOperations();
		operationsList.clear();
		operationsList.addAll(operations);
				
		if(operations.isEmpty() == false){
			//Add the "New Operation" button
			Operation operation = new Operation();
			operation.setName("New Operation");
			operationsList.add(operation);
		} else {
			//Dont display any operations
			//Hide?
		}
		
		if (spinnerMenuAdapter != null) spinnerMenuAdapter.notifyDataSetChanged();
		selectCurrentOperationInSpinner();
	}

	private void selectCurrentOperationInSpinner() {
		if (spinnerMenuAdapter != null && currentOperation != null && actionBar != null) {
			for (int i = 0; i < spinnerMenuAdapter.getCount(); i++) {
				if (spinnerMenuAdapter.getItem(i).getId() == this.currentOperation.getId()) {
					Log.d("MainActivity - selectCurrentOperationInSpinner", "Found");
					spinnerMenu.setSelection(i);
					currentOperation = spinnerMenuAdapter.getItem(i);
					this.updateOperation();
					break;
				}
			}
		}
	}

	private void createOperation(final Callable<Void> myFunc) {
		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.new_operation_dialog, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.new_operation_dialog_name);

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Add",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Create the operation
								String name = userInput.getText().toString();
								if (name.isEmpty() == false) {
									
									Operation newOp = new Operation(name);
									TableOperations.updateOperation(dbHelper, newOp); //Add operation to db
									currentOperation = newOp;

									Log.d("MainActivity - createOperation", currentOperation.getName());

									dbHelper.close();
									//Add to operations list
									operationsList.add(0, newOp);
									if (spinnerMenuAdapter != null) spinnerMenuAdapter.notifyDataSetChanged();
									selectCurrentOperationInSpinner();

									// Save this choice in preferences for next open
									SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = prefs.edit();
									editor.putInt("currentOperationId", currentOperation.getId());
									editor.commit();

									// Continue what we were doing with callback
									if (myFunc != null) {
										try {
											myFunc.call();
										} catch (Exception e) {
											Log.d("MainActivity - createOperation", "Failed to call return method");
											e.printStackTrace();
										}
									}
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

	@Override
	public void onClick(View v) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		return true;
	}
	

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean installed = TrelloContentProvider.isInstalled();
		
		if(this.fragmentAddField != null && this.fragmentAddField.isHidden() == false) {
			if(mCurrentState == STATE_LIST_VIEW){
				Log.d("MainActivity", "pInflating add field list view");
				menu.findItem(R.id.main_menu_list_view).setVisible(false);
				menu.findItem(R.id.main_menu_map_view).setVisible(true);
				
				menu.findItem(R.id.main_menu_add).setVisible(false);
				menu.findItem(R.id.main_menu_sync).setVisible(false);
				menu.findItem(R.id.main_menu_current_location).setVisible(false);
			} else {
				Log.d("MainActivity", "pInflating add field map view");
				menu.findItem(R.id.main_menu_list_view).setVisible(true);
				menu.findItem(R.id.main_menu_map_view).setVisible(false);
				
				menu.findItem(R.id.main_menu_add).setVisible(false);
				menu.findItem(R.id.main_menu_sync).setVisible(false);
				
				menu.findItem(R.id.main_menu_current_location).setVisible(true);
			}
		} else if(mCurrentState == STATE_LIST_VIEW){
			Log.d("MainActivity", "pInflating list view");
			menu.findItem(R.id.main_menu_list_view).setVisible(false);
			menu.findItem(R.id.main_menu_map_view).setVisible(true);
			
			menu.findItem(R.id.main_menu_add).setVisible(true);
			if(installed) {
				menu.findItem(R.id.main_menu_sync).setVisible(true);
				menu.findItem(R.id.main_menu_autosync).setVisible(true);
			} else {
				menu.findItem(R.id.main_menu_sync).setVisible(false);
				menu.findItem(R.id.main_menu_autosync).setVisible(false);
			}
			
			menu.findItem(R.id.main_menu_current_location).setVisible(false);
		} else {
			Log.d("MainActivity", "pInflating main menu");
			menu.findItem(R.id.main_menu_list_view).setVisible(true);
			menu.findItem(R.id.main_menu_map_view).setVisible(false);
			
			menu.findItem(R.id.main_menu_add).setVisible(true);
			if(installed) {
				menu.findItem(R.id.main_menu_sync).setVisible(true);
				menu.findItem(R.id.main_menu_autosync).setVisible(true);
			} else {
				menu.findItem(R.id.main_menu_sync).setVisible(false);
				menu.findItem(R.id.main_menu_autosync).setVisible(false);
			}
			
			menu.findItem(R.id.main_menu_current_location).setVisible(true);
		}	
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.main_menu_add) {
			// Check if have an operation
			if (operationsList.isEmpty()) {
				// Show dialog to create operation
				createOperation(new Callable<Void>() {
					public Void call() {
						//Do this after we create an operation
						return addFieldMapView();
					}
				});
			} else {
				addFieldMapView();
			}
		} else if (item.getItemId() == R.id.main_menu_current_location) {
			Location myLoc = map.getMyLocation();
			if(myLoc == null){
				Toast.makeText(this, "Still searching for your location", Toast.LENGTH_SHORT).show();
			} else {
				CameraPosition oldPos = map.getCameraPosition();
				CameraPosition newPos = new CameraPosition(new LatLng(myLoc.getLatitude(), myLoc.getLongitude()), map.getMaxZoomLevel()-5.0f, oldPos.tilt, oldPos.bearing);
				map.animateCamera(CameraUpdateFactory.newCameraPosition(newPos));
			}
		} else if (item.getItemId() == R.id.main_menu_layers) {
			MenuItem layers = menu.findItem(R.id.main_menu_layers);
			//TODO hazards
			/*if(showingHazards == false){
				showingHazards = true;
				layers.setTitle("Hide Hazards");
			} else {
				showingHazards = false;
				layers.setTitle("Show Hazards");
			}
			SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("showingHazards",showingHazards);
			editor.commit();
			drawHazards();*/
		} else if (item.getItemId() == R.id.main_menu_sync) {
			this.syncHelper.sync(this);
		} else if (item.getItemId() == R.id.main_menu_list_view) {
			// Show list view
			Log.d("MainActivity", "Showing list view");
			setState(STATE_LIST_VIEW);
		}  else if (item.getItemId() == R.id.main_menu_map_view) {
			// Show map view
			Log.d("MainActivity", "Showing map view");
			setState(STATE_DEFAULT);
			closeKeyboard();
		} else if(item.getItemId() == R.id.main_menu_help){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
	        alert.setTitle("Help");
	        WebView wv = new WebView(this);
	        wv.loadUrl("file:///android_asset/Help.html");
	        wv.getSettings().setSupportZoom(true);
	        wv.getSettings().setBuiltInZoomControls(true);
	        /*wv.setWebViewClient(new WebViewClient(){
	            @Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url)
	            {
	                view.loadUrl(url);
	                return true;
	            }
	        });*/
	        alert.setView(wv);
	        alert.setNegativeButton("Close", null);
	        alert.show();
		} else if(item.getItemId() == R.id.main_menu_legal){
			CharSequence licence= "The MIT License (MIT)\n" +
	                "\n" +
	                "Copyright (c) 2013 Purdue University\n" +
	                "\n" +
	                "Permission is hereby granted, free of charge, to any person obtaining a copy " +
	                "of this software and associated documentation files (the \"Software\"), to deal " +
	                "in the Software without restriction, including without limitation the rights " +
	                "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell " +
	                "copies of the Software, and to permit persons to whom the Software is " +
	                "furnished to do so, subject to the following conditions:" +
	                "\n" +
	                "The above copyright notice and this permission notice shall be included in " +
	                "all copies or substantial portions of the Software.\n" +
	                "\n" +
	                "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR " +
	                "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, " +
	                "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE " +
	                "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER " +
	                "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, " +
	                "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN " +
	                "THE SOFTWARE.\n";
			new AlertDialog.Builder(this)
				.setTitle("Legal")
				.setMessage(licence)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton("Close", null).show();
		} else if(item.getItemId() == R.id.main_menu_autosync){
			TrelloSyncInfo syncInfo = syncHelper.getSyncInfo(getApplicationContext());
			
			int selected = -1;
			if(syncInfo != null){
				if(syncInfo.getAutoSync() != null && syncInfo.getAutoSync() == false){
					selected = 0;
				} else if(syncInfo.getInterval() != null) {
					Log.d("MainActivity", "syncInfo interval:" + syncInfo.getInterval());
					if(syncInfo.getInterval() == 60) selected = 1;
					if(syncInfo.getInterval() == 60*5) selected = 2;
					if(syncInfo.getInterval() == 60*10) selected = 3;
					if(syncInfo.getInterval() == 60*30) selected = 4;
				}
			}
			String[] options = { 
					"Never", "1 min", "5 min", "10 min", "30 min"
				};
			new AlertDialog.Builder(this).setTitle("Autosync Interval").setSingleChoiceItems(options, selected, 
					new DialogInterface.OnClickListener() {
						Integer devAutoSyncOnTrigger = 0;
						public void onClick(DialogInterface dialog, int which) {
							if(which == 0){
								syncHelper.autoSyncOff(getApplicationContext());
							} else if(which == 1) {
								syncHelper.autoSyncOn(getApplicationContext(), 60);
							} else if(which == 2) {
								syncHelper.autoSyncOn(getApplicationContext(), 60*5);
							} else if(which == 3) {
								syncHelper.autoSyncOn(getApplicationContext(), 60*10);
							} else if(which == 4) {
								syncHelper.autoSyncOn(getApplicationContext(), 60*30);
								devAutoSyncOnTrigger++;
								if(devAutoSyncOnTrigger > 9){
									if(devAutoSyncOnTrigger % 2 == 0){
										syncHelper.devAutoSyncOn(getApplicationContext(), 3);
										Toast.makeText(getApplicationContext(), "Presentation auto sync on, every 3 seconds.", Toast.LENGTH_SHORT).show();
									} else {
										syncHelper.devAutoSyncOff(getApplicationContext());
										Toast.makeText(getApplicationContext(), "Presentation auto sync off.", Toast.LENGTH_SHORT).show();
									}
								}
							}
			           }
					}).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			             @Override
			             public void onClick(DialogInterface dialog, int id) {
			                 
			             }
			         }).show();
		}
		return true;
	}

	private Void addFieldMapView() {
		// Add field (Polygon)
		
		//Allow the user to draw a polygon on the map
		ATKPolygonView polygonBeingDrawn = map.drawPolygon(ID_FIELD_DRAWING);
		//Set some settings for what it should appear like when being drawn
		polygonBeingDrawn.setFillColor(0.7f, 0, 255, 0); //Opacity, Red, Green, Blue
		
		Field newField = new Field();
		newField.setId(-1);
		newField.setDeleted(false);
		
		this.currentFieldView = new FieldView(FieldView.STATE_SELECTED, newField, null, polygonBeingDrawn, map);	
	
		showFragmentAddField(true);
		return null;
	}

	private Void setState(int newState) {
		setState(newState, true);
		return null;
	}

	private void setState(int newState, boolean transition) {
		Log.d("SetState!!", "Setting state:" + Integer.toString(newState));
		if (mCurrentState == newState) {
			return;
		}
		// Exit current state
		if (mCurrentState == STATE_DEFAULT) {

		} else if (mCurrentState == STATE_LIST_VIEW) {
			FragmentManager fm = getSupportFragmentManager();
			// Hide list
			actionBarSearch.setVisibility(View.GONE);
			Fragment fragment = fm.findFragmentById(R.id.list_view);
			FragmentTransaction ft = fm.beginTransaction();
			ft.hide(fragment);
			ft.commit();
			fragmentListView = null;
		}

		// Enter new state
		if (newState == STATE_DEFAULT) {

		} else if (newState == STATE_LIST_VIEW) {
			FragmentManager fm = getSupportFragmentManager();
			// Show List
			actionBarSearch.setVisibility(View.VISIBLE);
			FragmentListView fragmentListView = (FragmentListView) fm.findFragmentById(R.id.list_view);
			fragmentListView.getData();
			FragmentTransaction ft = fm.beginTransaction();
			ft.show(fragmentListView);
			ft.commit();

			this.fragmentListView = fragmentListView;
		}
		// Officially in new state
		mCurrentState = newState;
		invalidateOptionsMenu();
	}

	private Void showFragmentJob(Boolean transition) {
		if (this.fragmentJob == null || this.fragmentJob.isVisible() == false) {
			hideFragmentAddField(false);
			FrameLayout layout = (FrameLayout) findViewById(R.id.fragment_container_edit_job);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
			params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
			layout.setLayoutParams(params);

			FragmentManager fm = getSupportFragmentManager();
			FragmentJob fragment = new FragmentJob();
			FragmentTransaction ft = fm.beginTransaction();
			if (transition) ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
			ft.add(R.id.fragment_container_edit_job, fragment, "edit_job");
			ft.commit();

			fragmentJob = fragment;
			fragment.setRetainInstance(true);
		}
		return null;
	}

	private void hideFragmentJob(Boolean transition) {
		if (this.fragmentJob != null && this.fragmentJob.isVisible()) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentJob fragment = (FragmentJob) fm.findFragmentByTag("edit_job");
			
			if(fragment != null){
				fragment.closing();
			}
			
			// Set height so transition works
			FrameLayout layout = (FrameLayout) findViewById(R.id.fragment_container_edit_job);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
			params.height = fragment.getHeight();
			layout.setLayoutParams(params);
			// Do transition
			FragmentTransaction ft = fm.beginTransaction();
			if (transition) ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
			ft.hide(fragment);
			ft.commit();
			fragmentJob = null;
		}
	}

	private Void showFragmentAddField(Boolean transition) {
		if (fragmentAddField == null || fragmentAddField.isVisible() == false) {
			hideFragmentJob(false);
			// Set height back to wrap, in case add buttons or something
			FrameLayout layout = (FrameLayout) findViewById(R.id.fragment_container_add_field);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout
					.getLayoutParams();
			params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
			layout.setLayoutParams(params);

			FragmentManager fm = getSupportFragmentManager();
			FragmentAddField fragment = new FragmentAddField();
			fragmentAddField = fragment;
			FragmentTransaction ft = fm.beginTransaction();
			if (transition) ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
			ft.add(R.id.fragment_container_add_field, fragment, "add_field");
			ft.commit();
			fragment.setRetainInstance(true);
		}
		this.invalidateOptionsMenu();
		return null;
	}

	private void hideFragmentAddField(Boolean transition) {
		if (this.fragmentAddField != null && this.fragmentAddField.isVisible() == true) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentAddField fragment = (FragmentAddField) fm.findFragmentByTag("add_field");
			fragmentAddField = null;
			// Set height so transition works
			FrameLayout layout = (FrameLayout) findViewById(R.id.fragment_container_add_field);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout
					.getLayoutParams();
			params.height = fragment.getHeight();
			layout.setLayoutParams(params);
			// Do transition
			FragmentTransaction ft = fm.beginTransaction();
			if (transition) ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
			ft.hide(fragment);
			ft.commit();
		}
		this.invalidateOptionsMenu();
	}

	@Override
	public void FragmentAddField_Init() {
		//Send data to FragmentAddField
		if(this.currentFieldView == null) Log.w("FragmentAddField_Init", "currentFieldView is null");
		if(fragmentAddField != null) fragmentAddField.init(this.currentFieldView);
	}
	@Override
	public void FragmentAddField_Undo() {
		this.map.drawUndo();
	}

	@Override
	public void FragmentAddField_Done(FieldView fieldview) {
		//Check if we deleted the field b4 we made it, ie. add field, immediately delete.
		Field field = fieldview.getField();
		closeKeyboard();
		
		if(field == null || field.getId() == -1 && field.getDeleted() == true){
			Log.w("FragmentAddField_Done", "Deleted a field before we were done making it.");
			ATKPolygonView polygon = map.completePolygon();
			map.removePolygon(polygon.getAtkPolygon());
			this.hideFragmentAddField(true);
			this.currentFieldView = null;
			return;
		}
		
		// Check if field name is valid and doesn't exist already
		if (field.getName().trim().length() == 0) {
			// Tell them to input a name
			// TODO add this message to R.strings
			Toast.makeText(this, "Field name cannot be blank.", Toast.LENGTH_LONG).show();
		} else {
			
			// Check if field name already exists in db
			Field oldField = null;
			if(field.getId() != -1) oldField = TableFields.FindFieldById(this.dbHelper, field.getId());
			
			if(oldField == null){
				//New field
				field.setId(null); //Make sure null, so it creates the field again if it was deleted when we were editing it.
				if(TableFields.FindFieldByName(this.dbHelper, field.getName()) != null){
					Toast.makeText(this,"A field with this name already exists. Field names must be unique.", Toast.LENGTH_LONG).show();
					return;
				}
			} else {
				field.setId(oldField.getId());
			}
			
			ATKPolygonView polygon = map.completePolygon();
			field.setBoundary(polygon.getAtkPolygon().boundary);
			Log.d("FragmentAddField_Done", "boundary size:" + Integer.toString(field.getBoundary().size()));
		
			//Setup values to add or update
			Field toUpdate = new Field(null);
			
			Boolean changes = false;
			if(oldField == null || oldField.getName().contentEquals(field.getName()) == false) {
				toUpdate.setName(field.getName());
				toUpdate.setDateNameChanged(new Date());
				changes = true;
				
				if(oldField != null) {
					//Update all the jobs with this field name to the new field name
					TableJobs.updateJobsWithFieldName(dbHelper, oldField.getName(), field.getName());
				}
			}
			if(oldField == null || oldField.getAcres() != field.getAcres()) {
				toUpdate.setAcres(field.getAcres());
				toUpdate.setDateAcresChanged(new Date());
				changes = true;
			}
			if(oldField == null ||  oldField.getBoundary().equals(field.getBoundary()) == false){
				toUpdate.setBoundary(field.getBoundary());
				toUpdate.setDateBoundaryChanged(new Date());
				changes = true;
			}
			if(oldField != null && field.getDeleted() == true){
				//Deleted
				//If not trello synced then delete from db
			
				//Update db and remove from map
				toUpdate.setDeleted(true);
				toUpdate.setDateDeleted(new Date());
			}
			
			if(changes){
				// Save this field to the db
				toUpdate.setId(field.getId()); //Set it's id if it has one
				
				Boolean deleted = false;
				if(field.getDeleted() == true){
					//Delete from db if hasn't synced to cloud yet. Otherwise we have to mark it as deleted in db so cloud will delete it on next sync
					deleted = TableFields.deleteFieldIfNotSynced(dbHelper, oldField);
				}
				if(deleted == false) {
					Log.d("FragmentAddField_Done", "Saving Field to local db. Name: " + toUpdate.getName());
					if(toUpdate.getId() != null) Log.d("FragmentAddField_Done", "Saving Field to local db. id:" + Integer.toString(field.getId()));
					
					TableFields.updateField(dbHelper, toUpdate);
					if(toUpdate.getId() != null) field.setId(toUpdate.getId()); //Update id of fieldview field if was insert
				}
				
				if(oldField == null){
					//More efficient, use this polygon so we don't have to delete and redraw
				    //Finally, create our new FieldView
					polygon.getAtkPolygon().id = field.getId();
					fieldview.update(field, fieldview.getJob());
				    fieldViews.add(fieldview);
				} else {
					//Go ahead and update the field on the map, we were editing
					fieldview.update(field, fieldview.getJob());
					if(field.getDeleted() == true){
						Log.d("FragmentAddField_Done", "Deleted field, removing from fieldViews.");
						this.removeFieldView(fieldview);
						fieldview = null;
					}
				}
				
				//Add or update in list view
				if (this.fragmentListView != null) this.fragmentListView.getData();
				this.syncHelper.autoSyncDelayed(this);
			}
			
			this.currentFieldView = fieldview;
			
			// Check to see if we have any operations
			if(field.getDeleted()){
				this.hideFragmentAddField(true);
			} else {
				if(operationsList.isEmpty() == false) {
					// Check if any operation selected
					if (currentOperation != null) {
						showFragmentJob(true);
					} else {
						// Make them select an operation
						// TODO popup list??
					}
				} else {
					// Add an operation
					createOperation(new Callable<Void>() {
						public Void call() {
							return showFragmentJob(true);
						}
					});
				}
			}
		}
	}

	

	
	@Override
	public Integer listViewGetCurrentOperationId() {
		if(currentOperation == null) return -1;
		return currentOperation.getId();
	}

	@Override
	public String ListViewGetCurrentFieldName() {
		return null;
	}
	

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		
		// Spinner selected item
		Operation operation = (Operation) parent.getItemAtPosition(pos);
		if (operation.getId() == null) {
			// Create new operation
			selectCurrentOperationInSpinner(); // Go back to original for now,
												// in case cancel
			createOperation(null);
		} else {
			if(currentOperation == null || currentOperation.getId() != operation.getId()) hideFragmentJob(true);
			
			currentOperation = operation;
			
			// Save this choice in preferences for next open
			SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("currentOperationId", currentOperation.getId());
			editor.commit();
		}
		
		updateOperation();
		if(this.fragmentListView != null) this.fragmentListView.getData();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// Spinner, select disappear, selection removed from adapter TODO
		Log.d("MainMenu - onNothingSelected", "Nothing selected");
	}
	
	
	@Override
	public void onMapClick(LatLng position) {
		Log.d("MainActivity", "OnMapClick");
		if(this.keyboardIsShowing == true) {
			closeKeyboard();
		}
		if(currentFieldView != null) currentFieldView.setState(FieldView.STATE_NORMAL);
		currentFieldView = null;
		if(this.fragmentJob != null) this.hideFragmentJob(true);
	}

	@Override
	public boolean onPointClick(ATKPointView pointView) {

		return false;
	}

	@Override
	public boolean onPolygonClick(final ATKPolygonView polygonView) {
		Log.d("MainActivity", "OnPolygonClick");

		if(this.keyboardIsShowing == true) {
			closeKeyboard();
		}
		
		if(this.currentOperation != null){
			//Select field view that was clicked
			FieldView clicked = (FieldView) polygonView.getData();
			clicked.setState(FieldView.STATE_SELECTED);
			
			if(currentFieldView != clicked){
				if(currentFieldView != null) currentFieldView.setState(FieldView.STATE_NORMAL);
				currentFieldView = clicked;
			}
		
			updateFragmentJob();
			
			//Bring up fragmentJob if not already
			this.showFragmentJob(true);
		} else {
			createOperation(new Callable<Void>() {
				public Void call() {
					//Do this after we create an operation
					onPolygonClick(polygonView);
					return null;
				}
			});
		}
		return true;
	}
	
	private void updateFragmentJob(){
		//Update info in fragmentJob if already up
		Job curJob = null;
		Field curField = null;
		if(currentFieldView != null){
			curJob = currentFieldView.getJob();
			curField = currentFieldView.getField();
		}
		if(this.fragmentJob != null){
			this.fragmentJob.updateJob(curJob);
			this.fragmentJob.updateField(curField);
			this.fragmentJob.updateOperation(this.currentOperation);
		}
		if(curJob == null && curField == null){
			//Hide fragmentJob, nothing to show
			this.hideFragmentJob(true);
		}
	}

	@Override
	public boolean onPointDrag(ATKPointView pointView) {

		return false;
	}

	@Override
	public boolean onPointDragEnd(ATKPointView pointView) {

		return false;
	}

	@Override
	public boolean onPointDragStart(ATKPointView pointView) {
		
		return false;
	}

	@Override
	public void FragmentJob_Init() {
		if(this.fragmentJob != null){
			this.fragmentJob.init(dbHelper);
			
			updateFragmentJob();
		}
	}


	@Override
	public void FragmentJob_UpdateJob(Job job) {
		//Update the selected polygons job
		Field field = null;
		if(this.currentFieldView != null) field = this.currentFieldView.getField();
		this.updateFieldView(field, job);
		
		//Update listView if it is visible
		if(this.fragmentListView != null && this.fragmentListView.isVisible()){
			//TODO get listview if it is null, could happen if rotate when list view is up.
			this.fragmentListView.getData();
		}
		
		//Remote sync jobs, they have changed...
		this.syncHelper.autoSyncDelayed(this);
	}

	@Override
	public void FragmentJob_TriggerSync() {
		//Trigger a sync
		this.syncHelper.autoSyncDelayed(this);
	}


	@Override
	public void ListViewOnClick(Job currentJob) {
		//List view picked a job, select it
		Field field = null;
		if(currentJob != null && currentJob.getFieldName() != null){
			field = TableFields.FindFieldByName(dbHelper, currentJob.getFieldName());
		}
		
		if(currentFieldView != null) currentFieldView.setState(FieldView.STATE_NORMAL);

		FieldView clicked = null;
		if(field != null) clicked = this.findFieldView(field);
		if(clicked != null){
			clicked.setState(FieldView.STATE_SELECTED);
			if(clicked.getPolygonView() != null) this.map.zoomTo(clicked.getPolygonView().getAtkPolygon(), false);
		}
		
		currentFieldView = clicked;
		updateFragmentJob();
		
		if(currentFieldView == null){
			this.hideFragmentJob(true);
		} else {
			//Bring up fragmentJob if not already
			this.showFragmentJob(true);
		}
	}

	@Override
	public void FragmentJob_EditField() {
		//Bring the field into edit mode
		if(this.currentFieldView != null){
			this.map.drawPolygon(this.currentFieldView.getPolygonView());
			this.currentFieldView.getPolygonView().setFillColor(0.7f, 0, 255, 0); //Opacity, Red, Green, Blue
			showFragmentAddField(true);
		}
	}

	@Override
	public void SelectDate(Date selectedDate) {
		//This can be moved fully inside the fragment but I forget how and I'm short on time.
		if(this.fragmentJob != null) this.fragmentJob.SelectDate(selectedDate);
	}

	@Override
	public void onSoftKeyboardShown(boolean isShowing) {
		//On touch map dismiss keyboard
		keyboardIsShowing = isShowing;
		if(this.fragmentAddField != null) this.fragmentAddField.keyboardShowing = isShowing;
	}

	
	private void closeKeyboard(){
		InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
	    //check if no view has focus:
	    View v=this.getCurrentFocus();
	    if(v != null){
	    	inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}


}
