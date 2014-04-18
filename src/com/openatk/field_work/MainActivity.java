package com.openatk.field_work;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
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
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolygonOptions;
import com.openatk.field_work.FragmentAddField.FragmentAddFieldListener;
import com.openatk.field_work.FragmentJob.FragmentJobListener;
import com.openatk.field_work.FragmentListView.ListViewListener;
import com.openatk.field_work.db.DatabaseHelper;
import com.openatk.field_work.db.TableFields;
import com.openatk.field_work.db.TableJobs;
import com.openatk.field_work.db.TableOperations;
import com.openatk.field_work.listeners.DatePickerListener;
import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Job;
import com.openatk.field_work.models.Operation;
import com.openatk.field_work.models.Worker;
import com.openatk.field_work.views.FieldView;
import com.openatk.openatklib.atkmap.ATKMap;
import com.openatk.openatklib.atkmap.ATKSupportMapFragment;
import com.openatk.openatklib.atkmap.listeners.ATKMapClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointDragListener;
import com.openatk.openatklib.atkmap.listeners.ATKPolygonClickListener;
import com.openatk.openatklib.atkmap.views.ATKPointView;
import com.openatk.openatklib.atkmap.views.ATKPolygonView;

public class MainActivity extends FragmentActivity implements OnClickListener, FragmentAddFieldListener, FragmentJobListener, DatePickerListener,
		OnItemSelectedListener, ListViewListener, ATKPointDragListener, ATKMapClickListener, ATKPolygonClickListener, ATKPointClickListener {
	
	public static final String INTENT_FIELD_UPDATED = "com.openatk.fieldwork.field.UPDATED";
	public static final String INTENT_JOB_UPDATED = "com.openatk.fieldwork.job.UPDATED";
	public static final String INTENT_OPERATION_UPDATED = "com.openatk.fieldwork.operation.UPDATED";
	public static final String INTENT_WORKER_UPDATED = "com.openatk.fieldwork.worker.UPDATED";
	
	public static final int ID_FIELD_DRAWING = -100;

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
	

	private List<Operation> operationsList = new ArrayList<Operation>();;
	private ArrayAdapter<Operation> spinnerMenuAdapter = null;
	private Spinner spinnerMenu = null;

	FragmentJob fragmentJob = null;
	FragmentListView fragmentListView = null;
	FragmentAddField fragmentAddField = null;
	
	private List<FieldView> fieldViews = null;
	private int currentOperationId = -1;
	private int currentFieldId = -1;
	
	private FieldView currentFieldView;

	
	private Field currentField;
	private Job currentJob;
	private Operation currentOperation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dbHelper = new DatabaseHelper(this);
		
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
					InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
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
		

		// Get last selected operation
		if (savedInstanceState != null) {
			// Find current field
			currentFieldId = savedInstanceState.getInt("currentFieldId", -1);		
		}

		// Load operations from database
		loadOperations();

		// Specify a SpinnerAdapter to populate the dropdown list.
		spinnerMenuAdapter = new ArrayAdapter<Operation>(this, R.layout.operation_list_item, operationsList);
		spinnerMenu.setAdapter(spinnerMenuAdapter);
		
		// Find current operation
		SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE);
		currentOperationId = prefs.getInt("currentOperationId", -1);

		selectCurrentOperationInSpinner();
		spinnerMenu.setOnItemSelectedListener(this); //Be sure to do this after previous so we don't reload the map
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setUpMapIfNeeded();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("currentField", currentFieldId);		
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

			//Old map we need to get all our data from it
			//Get the FieldViews from ATKMap
			List<ATKPolygonView> polygonViews =  map.getPolygonViews();
			for(int i=0; i<polygonViews.size(); i++){
				this.fieldViews.add((FieldView) polygonViews.get(i).getData());
			}
			
			//Check if database was updated while we were gone
			SharedPreferences prefs = this.getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    		Boolean databaseChanged = prefs.getBoolean("databaseChanged", false);
    		
    		//dsfjasldf //TODO
		}
		
		//Setup stuff for new activity
		map.setOnMapClickListener(this);
		map.setOnPolygonClickListener(this);
		map.setOnPointClickListener(this);
		map.setOnPointDragListener(this);
	}

	private void setUpMap(){
		Log.d("MainActivity", "setUpMap()");
		mapSettings = map.getUiSettings();
		mapSettings.setZoomControlsEnabled(false);
		mapSettings.setMyLocationButtonEnabled(false);
		mapSettings.setTiltGesturesEnabled(false);
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setMyLocationEnabled(true);

		fieldViews = createFieldViews();
	}
	
	
	private List<FieldView> createFieldViews(){
		//Read database and get all the fields and make the FieldViews
		List<FieldView> fieldViews = new ArrayList<FieldView>();
		
		//Read all the fields
		List<Field> fields = dbHelper.readFields();
		
		//Read all the jobs for the current operation
		List<Job> jobs = dbHelper.readJobs();

		//Now create fieldviews jobs to fields
		for(int i=0; i<fields.size(); i++){
			Field field = fields.get(i);			
			
			//Find the job for this field
			Job job = null;
			Iterator<Job> jobIterator = jobs.iterator();
		    while(jobIterator.hasNext()){
		    	Job curJob = jobIterator.next();
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
		    if(field.getId() == currentFieldId){
		    	state = FieldView.STATE_SELECTED;
		    }
		    
		    if(job != null) {
		    	//Setup a blank job for this field, it doesn't have one yet
		    	Log.d("MainActivity", "Job Status:" + Integer.toString(job.getStatus()));
		    }
		    		    
		    //Finally, create our new FieldView
		    fieldViews.add(new FieldView(state, field, job, map));
		}
		return fieldViews;
	}
	private FieldView updateFieldView(Field field){
		return this.updateFieldView(field, null);
	}
	private FieldView updateFieldView(Field field, Job job){
		//Look for field in current list of fieldViews
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
		
		if(fieldView == null){
			Log.d("updateFieldView", "FieldView not found, making a new field.");
		    //Finally, create our new FieldView
		    fieldView = new FieldView(FieldView.STATE_NORMAL, field, null, map);
		    fieldViews.add(fieldView);
		} else {
			Log.d("updateFieldView", "Updating existing fieldview");
			//Update existing field view
			if(job == null) job = fieldView.getJob();
			fieldView.update(field, job);
		}
		return fieldView;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
        
        CameraPosition myCam = map.getCameraPosition();
		if(myCam != null){
			SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			LatLng where = myCam.target;
			editor.putFloat("StartupLat", (float) where.latitude);
			editor.putFloat("StartupLng",(float) where.longitude); 
			editor.putFloat("StartupZoom",(float) map.getCameraPosition().zoom); 
			editor.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, new IntentFilter(MainActivity.INTENT_FIELD_UPDATED));
		//drawHazards();
	}
	
	
	private BroadcastReceiver broadcastReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("MainActivity BroadcastReceiver", "Recieved intent");
		}
	};


	
	public void loadOperations() {
		if (spinnerMenuAdapter != null) spinnerMenuAdapter.clear();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(TableOperations.TABLE_NAME, TableOperations.COLUMNS, null, null, null, null, null);
		while (cursor.moveToNext()) {
			Operation operation = TableOperations.cursorToOperation(cursor);
			if (operation != null) operationsList.add(operation);
		}
		cursor.close();
		database.close();
		dbHelper.close();
		
		if (operationsList.isEmpty() == false) {
			Operation operation = new Operation();
			operation.setId(null);
			operation.setName("New Operation");
			operationsList.add(operation);
		} else {
			// Don't display any operation
			// TODO hide spinner menu
		}
		
		if (spinnerMenuAdapter != null) spinnerMenuAdapter.notifyDataSetChanged();
		selectCurrentOperationInSpinner();
	}

	private void selectCurrentOperationInSpinner() {
		if (spinnerMenuAdapter != null && currentOperationId != -1 && actionBar != null) {
			for (int i = 0; i < spinnerMenuAdapter.getCount(); i++) {
				if (spinnerMenuAdapter.getItem(i).getId() == currentOperationId) {
					Log.d("MainActivity - selectCurrentOperationInSpinner", "Found");
					spinnerMenu.setSelection(i);
					currentOperation = spinnerMenuAdapter.getItem(i);
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
									currentOperationId = newOp.getId();
									currentOperation = newOp;

									Log.d("MainActivity - createOperation", Integer.toString(currentOperationId));

									dbHelper.close();
									loadOperations();
									// selectCurrentOperationInSpinner();

									// Save this choice in preferences for next
									// open
									SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = prefs.edit();
									editor.putInt("currentOperationId", currentOperationId);
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
		if(this.fragmentAddField!= null && this.fragmentAddField.isVisible()) {
			if(this.fragmentListView.isVisible()){
				getMenuInflater().inflate(R.menu.add_field_list_view, menu);
			} else {
				getMenuInflater().inflate(R.menu.add_field, menu);
			}
		} else if(this.fragmentListView != null && this.fragmentListView.isVisible()){
			getMenuInflater().inflate(R.menu.list_view, menu);
		} else {
			getMenuInflater().inflate(R.menu.main, menu);
		}
		this.menu = menu;
		MenuItem layers = menu.findItem(R.id.main_menu_layers);
		if(layers != null){
			/*if(showingHazards == true){
				layers.setTitle("Hide Hazards");
			} else {
				layers.setTitle("Show Hazards");
			}*/
		}
		return true;
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
				CameraPosition newPos = new CameraPosition(new LatLng(myLoc.getLatitude(), myLoc.getLongitude()), map.getMaxZoomLevel(), oldPos.tilt, oldPos.bearing);
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
		} else if (item.getItemId() == R.id.main_menu_list_view) {
			if (this.fragmentListView.isVisible()) {
				// Show map view
				Log.d("MainActivity", "Showing map view");
				//TODO
				//setState(STATE_DEFAULT);
				this.invalidateOptionsMenu();
			} else {
				// Show list view
				Log.d("MainActivity", "Showing list view");
				//TODO
				//setState(STATE_LIST_VIEW);
				this.invalidateOptionsMenu();
			}
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
		}
		return true;
	}

	private Void addFieldMapView() {
		// Add field (Polygon)
		
		//Allow the user to draw a polygon on the map
		ATKPolygonView polygonBeingDrawn = map.drawPolygon(ID_FIELD_DRAWING);
		//Set some settings for what it should appear like when being drawn
		polygonBeingDrawn.setFillColor(0.7f, 0, 255, 0); //Opacity, Red, Green, Blue
		
		this.currentJob = null;
		showFragmentAddField(true);
		return null;
	}

	private Void setState(int newState) {
		setState(newState, true);
		return null;
	}

	private void setState(int newState, boolean transition) {
		Log.d("SetState!!", "Setting state:" + Integer.toString(newState));
		/*if (mCurrentState == newState) {
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
		this.invalidateOptionsMenu();*/
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
			FragmentTransaction ft = fm.beginTransaction();
			if (transition) ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
			ft.add(R.id.fragment_container_add_field, fragment, "add_field");
			ft.commit();
			fragmentAddField = fragment;
			fragment.setRetainInstance(true);
		}
		this.invalidateOptionsMenu();
		return null;
	}

	private void hideFragmentAddField(Boolean transition) {
		if (this.fragmentAddField != null && this.fragmentAddField.isVisible() == true) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentAddField fragment = (FragmentAddField) fm.findFragmentByTag("add_field");
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
			fragmentAddField = null;
		}
		this.invalidateOptionsMenu();
	}

	@Override
	public void FragmentAddField_Init() {
		//Send data to FragmentAddField
		if(fragmentAddField != null) fragmentAddField.init(this.currentField);
	}
	@Override
	public void FragmentAddField_Undo() {
		
	}

	@Override
	public void FragmentAddField_Done(Field field) {
		//Check if we deleted the field b4 we made it, ie. not editing a field
		if(field == null || field.getId() == null && field.getDeleted() == true){
			map.completePolygon();
			return;
		}
		
		// Check if field name is valid and doesn't exist already
		if (field.getName().length() == 0) {
			// Tell them to input a name
			// TODO add this message to R.strings
			Toast.makeText(this, "Field name cannot be blank.", Toast.LENGTH_LONG).show();
		} else {
			
			// Check if field name already exists in db
			Field oldField = null;
			if(field.getId() != null) oldField = TableFields.FindFieldById(this.dbHelper, field.getId());
			
			if(oldField == null){
				//New field
				field.setId(null); //Make sure null, so it creates the field again if it was deleted when we were editing it.
				if(TableFields.FindFieldByName(this.dbHelper, field.getName()) != null){
					Toast.makeText(this,"A field with this name already exists. Field names must be unique.", Toast.LENGTH_LONG).show();
					return;
				}
			}			
			
			ATKPolygonView polygon = map.completePolygon();
			field.setBoundary(polygon.getAtkPolygon().boundary);
						
			//Setup values to add or update
			Field toUpdate = new Field();
			
			ContentValues values = new ContentValues();
			Boolean changes = false;
			if(oldField == null || oldField.getName().contentEquals(field.getName()) == false) {
				toUpdate.setName(field.getName());
				toUpdate.setDateNameChanged(new Date());
				changes = true;
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
			
			if(changes){
				// Save this field to the db
				TableFields.updateField(dbHelper, toUpdate);
				
				if(toUpdate.getId() != null) field.setId(toUpdate.getId()); //Update id of fieldview field if was insert
				
				if(oldField == null){
					//More efficient, use this polygon so we don't have to delete and redraw
				    //Finally, create our new FieldView
					polygon.getAtkPolygon().id = field.getId();
					FieldView fieldView = new FieldView(FieldView.STATE_SELECTED, field, null, polygon, map);
				    fieldViews.add(fieldView);
					this.currentJob = null;
				} else {
					//Go ahead and update the field on the map, we were editing
					FieldView fieldView = this.updateFieldView(field);
					this.currentJob = fieldView.getJob();
				}
				
				// add or update in list view
				//if (this.fragmentListView != null) this.fragmentListView.getData();
				//this.trelloController.syncDelayed();
			}
			this.currentField = field;

				
			// Check to see if we have any operations
			if (operationsList.isEmpty() == false) {
				// Check if any operation selected
				if (currentOperationId != 0) {
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

	

	
	@Override
	public Integer listViewGetCurrentOperationId() {
		return currentOperationId;
	}

	@Override
	public String ListViewGetCurrentFieldName() {
		return null;
	}
	

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		hideFragmentJob(true);
		
		// Spinner selected item
		Operation operation = (Operation) parent.getItemAtPosition(pos);
		if (operation.getId() == null) {
			// Create new operation
			selectCurrentOperationInSpinner(); // Go back to original for now,
												// in case cancel
			createOperation(null);
		} else {
			currentOperationId = operation.getId();
			currentOperation = operation;
			
			// Save this choice in preferences for next open
			SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("currentOperationId", currentOperationId);
			editor.commit();
		}
		if (this.fragmentListView != null) this.fragmentListView.getData();
		//drawMap();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// Spinner, select disappear, selection removed from adapter TODO
		Log.d("MainMenu - onNothingSelected", "Nothing selected");
	}
	
	
	@Override
	public void onMapClick(LatLng position) {
		if(currentFieldView != null) currentFieldView.setState(FieldView.STATE_NORMAL);
		currentFieldView = null;
		currentField = null;
		currentJob = null;
		if(this.fragmentJob != null) this.hideFragmentJob(true);
	}

	@Override
	public boolean onPointClick(ATKPointView pointView) {

		return false;
	}

	@Override
	public boolean onPolygonClick(ATKPolygonView polygonView) {
		//Select field view that was clicked
		FieldView clicked = (FieldView) polygonView.getData();
		clicked.setState(FieldView.STATE_SELECTED);
		
		
		if(currentFieldView != null) currentFieldView.setState(FieldView.STATE_NORMAL);
		currentFieldView = clicked;
		
		currentField = currentFieldView.getField();
		currentJob = currentFieldView.getJob();
		
		
	
		//Update info in fragmentJob if already up
		if(this.fragmentJob != null){
			this.fragmentJob.updateJob(currentJob);
			this.fragmentJob.updateField(currentField);
		}
		
		//Bring up fragmentJob if not already
		this.showFragmentJob(true);

		
		return true;
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
			this.fragmentJob.updateField(this.currentField);
			this.fragmentJob.updateJob(this.currentJob);
			this.fragmentJob.updateOperation(this.currentOperation);
		}
	}


	@Override
	public void FragmentJob_UpdateJob(Job job) {
		//Update the selected polygons job
		this.updateFieldView(this.currentField, job);
	}

	@Override
	public void FragmentJob_UpdateWorker(Worker worker) {
		
	}


	@Override
	public void ListViewOnClick(Job currentJob) {
		
	}

	@Override
	public void FragmentJob_EditField() {
		
	}

	@Override
	public void SelectDate(Date selectedDate) {
		//This can be moved fully inside the fragment but I forget how and I'm short on time.
		if(this.fragmentJob != null) this.fragmentJob.SelectDate(selectedDate);
	}



}
