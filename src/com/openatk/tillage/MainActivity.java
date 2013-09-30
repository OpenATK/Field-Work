package com.openatk.tillage;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.openatk.libcommon.rock.Rock;
import com.openatk.libtrello.TrelloController;
import com.openatk.tillage.FragmentAddField.AddFieldListener;
import com.openatk.tillage.FragmentEditJobPopup.EditJobListener;
import com.openatk.tillage.FragmentListView.ListViewListener;
import com.openatk.tillage.db.DatabaseHelper;
import com.openatk.tillage.db.Field;
import com.openatk.tillage.db.Job;
import com.openatk.tillage.db.Operation;
import com.openatk.tillage.db.TableFields;
import com.openatk.tillage.db.TableJobs;
import com.openatk.tillage.db.TableOperations;
import com.openatk.tillage.drawing.MyPolygon;
import com.openatk.tillage.drawing.MyPolygon.MyPolygonListener;
import com.openatk.tillage.trello.SyncController;
import com.openatk.tillage.trello.SyncController.SyncControllerListener;

public class MainActivity extends FragmentActivity implements OnClickListener,
		OnMapClickListener, AddFieldListener, EditJobListener,
		OnItemSelectedListener, ListViewListener, OnMarkerClickListener,
		OnMarkerDragListener, MyPolygonListener, SyncControllerListener {
	private GoogleMap map;
	private UiSettings mapSettings;
	
    //Startup position
 	private static final float START_LAT = 40.428712f;
 	private static final float START_LNG = -86.913819f;
 	private static final float START_ZOOM = 17.0f;

	private ActionBar actionBar = null;
	private EditText actionBarSearch = null;
	private DatabaseHelper dbHelper;
	private Menu menu;
	
	private int mCurrentState = 0;
	private int editIsShowing = 0;
	private int addIsShowing = 0;
	private static final int STATE_DEFAULT = 0;
	private static final int STATE_LIST_VIEW = 1;

	private boolean showingHazards = false;
	private List<Marker> hazards = new ArrayList<Marker>();
	
	private Field currentField = null;
	private Job currentJob = null;
	private MyPolygon currentPolygon = null;
	private Integer currentOperationId = 0;

	private List<Field> FieldsOnMap = null;
	private List<Operation> operationsList = null;
	private ArrayAdapter<Operation> spinnerMenuAdapter = null;
	private Spinner spinnerMenu = null;

	FragmentEditJobPopup fragmentEditField = null;
	FragmentListView fragmentListView = null;
	FragmentAddField fragmentAddField = null;
	String addingBoundary = "";
	
	//Trello
    SyncController syncController;
    TrelloController trelloController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dbHelper = new DatabaseHelper(this);
		
		FragmentManager fm = getSupportFragmentManager();
		SupportMapFragment f = (SupportMapFragment) fm
				.findFragmentById(R.id.map);

		if (savedInstanceState == null) {
			// First incarnation of this activity.
			f.setRetainInstance(true);
		} else {
			// Reincarnated activity. The obtained map is the same map instance
			// in the previous
			// activity life cycle. There is no need to reinitialize it.
			map = f.getMap();
		}
		checkGPS();

		actionBar = getActionBar();
		// Specify that a dropdown list should be displayed in the action bar.
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Hide the title
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);

		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		View view;
		LayoutInflater inflater = (LayoutInflater) this.getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.action_bar, null);
		RelativeLayout item = (RelativeLayout) view
				.findViewById(R.id.action_bar_layout);
		spinnerMenu = (Spinner) view
				.findViewById(R.id.action_bar_operation_spinner);
		actionBarSearch = (EditText) view
				.findViewById(R.id.action_bar_search_box);
		actionBar.setCustomView(item, new ActionBar.LayoutParams(
				ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
						| Gravity.LEFT));

		actionBarSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == false) {
					InputMethodManager imm = (InputMethodManager) v
							.getContext().getSystemService(
									Context.INPUT_METHOD_SERVICE);
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
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
			}			
		});
		

		Fragment fragment = fm.findFragmentById(R.id.list_view);
		FragmentTransaction ft = fm.beginTransaction();
		ft.hide(fragment);
		ft.commit();
		
		 //Trello
        trelloController = new TrelloController(getApplicationContext());
        syncController = new SyncController(getApplicationContext(), trelloController, this);
        trelloController.setSyncController(syncController);

		// Get last selected operation
		if (savedInstanceState != null) {
			// Find current field
			currentField = FindFieldById(savedInstanceState
					.getInt("currentField"));
			
			// Find current job
			currentJob = FindJobById(savedInstanceState.getInt("currentJob"));

			// Find current operation
			currentOperationId = savedInstanceState.getInt("currentOperationId");
			
			editIsShowing = savedInstanceState.getInt("editIsShowing");
			addIsShowing = savedInstanceState.getInt("addIsShowing");
			
			this.addingBoundary = savedInstanceState.getString("drawingBoundary", "");

			
			// Switch to correct state
			setState(savedInstanceState.getInt("mCurrentState"), false);
		}

		// Load operations from database
		loadOperations();

		// Specify a SpinnerAdapter to populate the dropdown list.
		spinnerMenuAdapter = new ArrayAdapter<Operation>(this,
				R.layout.operation_list_item, operationsList);
		spinnerMenu.setAdapter(spinnerMenuAdapter);
		// Load current from preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		currentOperationId = prefs.getInt("currentOperationId", 0);
		this.showingHazards = prefs.getBoolean("showingHazards", false);
		selectCurrentOperationInSpinner();
		spinnerMenu.setOnItemSelectedListener(this);
		
		setUpMapIfNeeded();		
		
		Intent intent = this.getIntent();
		String todo = intent.getStringExtra("todo");
		if(todo != null){
			if(todo.contentEquals("sync")){
				trelloController.sync();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(addIsShowing == 1){
			//Save current polygon
			List<LatLng> points = this.currentPolygon.getPoints();
			Boolean wasAnEdit = false;
			if (currentField == null) {
				//Save to outState				
				points = this.currentPolygon.getMarkers();
			} else {
				currentField.setBoundary(points);
				wasAnEdit = true;
			}

			String strNewBoundary = "";
			if(points != null && points.isEmpty() == false){
				// Generate boundary
				StringBuilder newBoundary = new StringBuilder(
						points.size() * 20);
				for (int i = 0; i < points.size(); i++) {
					newBoundary.append(points.get(i).latitude);
					newBoundary.append(",");
					newBoundary.append(points.get(i).longitude);
					newBoundary.append(",");
				}
				newBoundary.deleteCharAt(newBoundary.length() - 1);
				strNewBoundary = newBoundary.toString();
			}
			if(wasAnEdit){
				// Save this field to the db
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put(TableFields.COL_BOUNDARY, strNewBoundary);
				database.update(TableFields.TABLE_NAME,values,TableFields.COL_ID + " = "+ Integer.toString(currentField.getId()),null);
				dbHelper.close();
			} else {
				outState.putString("drawingBoundary", strNewBoundary);
			}
		}
		
		if (currentField != null)
			outState.putInt("currentField", currentField.getId());
		if (currentJob != null)
			outState.putInt("currentJob", currentJob.getId());
		if (currentOperationId != null)
			outState.putInt("currentOperationId", currentOperationId);
		
		outState.putInt("mCurrentState", mCurrentState);
		outState.putInt("editIsShowing",editIsShowing);
		outState.putInt("addIsShowing",addIsShowing);

		super.onSaveInstanceState(outState);
	}

	private void setUpMapIfNeeded() {
		if (map == null) {
			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
		}
		// markerHandler = new MarkerHandler(map, this, mCurrentRockSelected);
		// slideMenu.setMarkerHandler(markerHandler);
		if (map != null) {
			mapSettings = map.getUiSettings();
			mapSettings.setZoomControlsEnabled(false);
			mapSettings.setMyLocationButtonEnabled(false);
			mapSettings.setTiltGesturesEnabled(false);
			
			map.setOnMapClickListener(this);
			map.setOnMarkerClickListener(this);
			map.setOnMarkerDragListener(this);
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			map.setMyLocationEnabled(true);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    		Float startLat = prefs.getFloat("StartupLat", START_LAT);
    		Float startLng = prefs.getFloat("StartupLng", START_LNG);
    		Float startZoom = prefs.getFloat("StartupZoom", START_ZOOM);
    		map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(startLat,startLng) , startZoom));
		}
		drawMap();
	}

	
	
	@Override
	protected void onPause() {
		super.onPause();
        trelloController.stopAutoSync();
        
        CameraPosition myCam = map.getCameraPosition();
		if(myCam != null){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
		checkGPS();
		drawHazards();
        trelloController.startAutoSync();   
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String todo = intent.getStringExtra("todo");
		if(todo != null){
			if(todo.contentEquals("sync")){
				trelloController.sync();
			}
		}
	}

	public void ListViewOnClick(Job clickedJob) {
		if(addIsShowing == 0){
			if (fragmentEditField != null) {
				fragmentEditField.flushChangesAndSave(false); // Save changes to
																// current field
			}
			if (this.currentPolygon != null) {
				// Set back to unselected if one is selected
				this.currentPolygon.unselect();
			}
			if(fragmentListView != null) fragmentListView.selectJob(clickedJob.getFieldName());
			
			// Load field and job data and show edit menu
			currentJob = clickedJob;
			currentField = FindFieldByName(currentJob.getFieldName());
			if (currentJob.getStatus() == Job.STATUS_NOT_PLANNED)
				currentJob = null;
	
			if(currentField != null){
				for (int i = 0; i < FieldsOnMap.size(); i++) {
					if (FieldsOnMap.get(i).getId() == currentField.getId()) {
						currentPolygon = FieldsOnMap.get(i).getPolygon();
						LatLngBounds bounds = FieldsOnMap.get(i).getBoundingBox();
						if(bounds != null){
							map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
						}
					}
				}
			}
	
			if (this.currentPolygon != null) currentPolygon.setStrokeColor(Field.STROKE_SELECTED);
			if (currentField == null) {
				Log.d("MainActivity - ListViewOnClick", "unable to find field by id, deleted?");
			}
	
			if (fragmentEditField != null) {
				fragmentEditField.refreshData(); // Refresh data in edit
													// menu if its
													// already open
			}
	
			if (operationsList.isEmpty()) {
				// Show dialog to create operation
				createOperation(new Callable<Void>() {
					public Void call() {
						return showEdit(true);
					}
				});
			} else {
				showEdit(true);
			}
		}
	}

	@Override
	public void onMapClick(LatLng position) {
		if (addIsShowing == 1) {
			// Add points to polygon
			this.currentPolygon.addPoint(position);
		} else if (mCurrentState == STATE_DEFAULT) {
			// Map view
			if (fragmentEditField != null) {
				fragmentEditField.flushChangesAndSave(false); // Save changes to
																// current field
			}

			// Check if touched a field
			Boolean touched = false;
			for (int i = 0; i < FieldsOnMap.size(); i++) {
				Field curField = FieldsOnMap.get(i);
				if (curField.wasTouched(position)) {
					// Touched this field
					touched = true;

					if (this.currentPolygon != null) {
						// Set back to unselected if one is selected
						this.currentPolygon.unselect();
					}

					// Load field and job data and show edit menu
					currentField = FindFieldById(curField.getId());
					currentPolygon = curField.getPolygon();
					this.currentPolygon.select();
					if (currentField == null) {
						Log.d("MainActivity - onMapClick", "unable to find field by id");
					}
					
					if (this.fragmentListView != null) {
						this.fragmentListView.selectJob(currentField.getName());
					}

					// Load Job data
					currentJob = FindJobByFieldName(currentField.getName());

					if (fragmentEditField != null) {
						fragmentEditField.refreshData(); // Refresh data in edit
															// menu if its
															// already open
					}

					if (operationsList.isEmpty()) {
						// Show dialog to create operation
						createOperation(new Callable<Void>() {
							public Void call() {
								return showEdit(true);
							}
						});
					} else {
						showEdit(true);
					}
					break;
				}
			}
			if (touched == false) {
				// Close menu, save edits
				Log.d("MainActivity - onMapClick", "Close");
				if (this.currentPolygon != null) {
					// Set back to unselected
					this.currentPolygon.unselect();
				}
				hideEdit(true);
				this.currentField = null;
				this.currentJob = null;
			}
		}
	}

	private void drawMap() {
		map.clear();
		drawFields();
		drawHazards();
	}

	private void drawHazards(){
		//Remove all hazards
		Iterator<Marker> iterator = hazards.iterator();
		while (iterator.hasNext()) {
			Marker curMarker = iterator.next();
			curMarker.remove();
			iterator.remove();
		}
		if(this.showingHazards == true){
			//Add all hazards
			ArrayList<Rock> rockList = Rock.getRocks(getApplicationContext());
			Iterator<Rock> iterator3 = rockList.iterator();
			while (iterator3.hasNext()) {
				Rock curRock = iterator3.next();
				if (curRock.isPicked() == false) {
					hazards.add(map.addMarker(new MarkerOptions().position(new LatLng(curRock.getLat(), curRock.getLon())).icon(BitmapDescriptorFactory.fromResource(R.drawable.rock)).draggable(false)));
				}
				iterator3.remove();
			}
		}
	}
	
	private void drawFields() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String[] columns = { TableFields.COL_ID, TableFields.COL_BOUNDARY,
				TableFields.COL_NAME, TableFields.COL_DELETED };
		String where = TableFields.COL_DELETED + " = 0";
		Cursor cursor = database.query(TableFields.TABLE_NAME, columns, where,
				null, null, null, null);

		FieldsOnMap = new ArrayList<Field>();
		Log.d("MainActivity- DrawFields",
				"Op Id:" + Integer.toString(currentOperationId));

		while (cursor.moveToNext()) {
			String boundary = cursor.getString(cursor
					.getColumnIndex(TableFields.COL_BOUNDARY));
			List<LatLng> points = Field.StringToBoundary(boundary);
			
			if(points.size() == 0) points = null;
			
			// Add to list so we can catch click events
			Field newField = new Field();
			newField.setId(cursor.getInt(cursor
					.getColumnIndex(TableFields.COL_ID)));
			newField.setMap(map);
			newField.setBoundary(points);
			newField.setName(cursor.getString(cursor
					.getColumnIndex(TableFields.COL_NAME)));

			// Find status of this field
			// Find job
			Job theJob = null;
			String where2 = TableJobs.COL_FIELD_NAME + " = '"
					+ newField.getName() + "'" + " AND "
					+ TableJobs.COL_OPERATION_ID + " = "
					+ Integer.toString(currentOperationId) +  " AND " + TableJobs.COL_DELETED + " = 0";
			Cursor cursor2 = database.query(TableJobs.TABLE_NAME,
					TableJobs.COLUMNS, where2, null, null, null, null);
			if (cursor2.moveToFirst()) {
				theJob = Job.cursorToJob(cursor2);
			}
			cursor2.close();

			// Now draw this field
			// Create polygon
			if(points != null && points.isEmpty() == false) {
				PolygonOptions polygonOptions = new PolygonOptions();
				if (theJob == null || theJob.getStatus() == Job.STATUS_NOT_PLANNED) {
					polygonOptions.fillColor(Field.FILL_COLOR_NOT_PLANNED);
				} else if (theJob.getStatus() == Job.STATUS_PLANNED) {
					polygonOptions.fillColor(Field.FILL_COLOR_PLANNED);
				} else if (theJob.getStatus() == Job.STATUS_STARTED) {
					polygonOptions.fillColor(Field.FILL_COLOR_STARTED);
				} else if (theJob.getStatus() == Job.STATUS_DONE) {
					polygonOptions.fillColor(Field.FILL_COLOR_DONE);
				}
				polygonOptions.strokeWidth(Field.STROKE_WIDTH);
				polygonOptions.strokeColor(Field.STROKE_COLOR);
				for (int i = 0; i < points.size(); i++) {
					polygonOptions.add(points.get(i));
				}
				newField.setPolygon(new MyPolygon(map, map.addPolygon(polygonOptions), this));
				if (currentField != null
						&& newField.getId() == currentField.getId()) {
					this.currentPolygon = newField.getPolygon();
					this.currentPolygon.setLabel(newField.getName(), true);
				} else {
					newField.getPolygon().setLabel(newField.getName());
				}
			}
			FieldsOnMap.add(newField);
		}
		cursor.close();
		dbHelper.close();
		if(addIsShowing == 1){
			if(this.currentPolygon != null && currentField != null){
				this.currentPolygon.edit();
			}
			
			if(this.addingBoundary.length() > 0){
				List<LatLng> points = Field.StringToBoundary(this.addingBoundary);
				this.currentPolygon = new MyPolygon(map, this);
				Log.d("Hi", "hello" + points.size());
				for(int i=0; i<(points.size()-1); i++){
					this.currentPolygon.addPoint(points.get(i));
				}
			}
		}
	}

	public void loadOperations() {
		if (spinnerMenuAdapter != null)
			spinnerMenuAdapter.clear();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(TableOperations.TABLE_NAME,
				TableOperations.COLUMNS, null, null, null, null, null);
		operationsList = new ArrayList<Operation>();
		while (cursor.moveToNext()) {
			Operation operation = Operation.cursorToOperation(cursor);
			if (operation != null)
				operationsList.add(operation);
			if (spinnerMenuAdapter != null) {
				if (operation != null)
					spinnerMenuAdapter.add(operation);
			}
		}
		cursor.close();
		dbHelper.close();
		if (operationsList.isEmpty() == false) {
			Operation operation = new Operation();
			operation.setId(null);
			operation.setName("New Operation");
			operationsList.add(operation);
			if (spinnerMenuAdapter != null)
				spinnerMenuAdapter.add(operation);
		} else {
			// Don't display any operation
			// TODO hide spinner menu
		}
		if (spinnerMenuAdapter != null)
			spinnerMenuAdapter.notifyDataSetChanged();
		selectCurrentOperationInSpinner();
	}

	private void selectCurrentOperationInSpinner() {
		if (spinnerMenuAdapter != null && currentOperationId != null
				&& actionBar != null) {
			for (int i = 0; i < spinnerMenuAdapter.getCount(); i++) {
				if (spinnerMenuAdapter.getItem(i).getId() == currentOperationId) {
					Log.d("MainActivity - selectCurrentOperationInSpinner",
							"Found");
					spinnerMenu.setSelection(i);
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
									// Create new operation
									SQLiteDatabase database = dbHelper
											.getWritableDatabase();
									ContentValues values = new ContentValues();
									values.put(TableOperations.COL_HAS_CHANGED,
											1);
									values.put(TableOperations.COL_NAME, name);
									currentOperationId = (int) database.insert(
											TableOperations.TABLE_NAME, null,
											values);

									Log.d("MainActivity - createOperation",
											Integer.toString(currentOperationId));

									dbHelper.close();
									loadOperations();
									// selectCurrentOperationInSpinner();

									// Save this choice in preferences for next
									// open
									SharedPreferences prefs = PreferenceManager
											.getDefaultSharedPreferences(getApplicationContext());
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putInt("currentOperationId",
											currentOperationId);
									editor.commit();

									// Continue what we were doing with callback
									if (myFunc != null) {
										try {
											myFunc.call();
										} catch (Exception e) {
											Log.d("MainActivity - createOperation",
													"Failed to call return method");
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
		if(addIsShowing == 1) {
			if(mCurrentState == STATE_LIST_VIEW){
				getMenuInflater().inflate(R.menu.add_field_list_view, menu);
			} else {
				getMenuInflater().inflate(R.menu.add_field, menu);
			}
		} else if(mCurrentState == STATE_LIST_VIEW){
			getMenuInflater().inflate(R.menu.list_view, menu);
		} else {
			getMenuInflater().inflate(R.menu.main, menu);
		}
		this.menu = menu;
		MenuItem layers = menu.findItem(R.id.main_menu_layers);
		if(layers != null){
			if(showingHazards == true){
				layers.setTitle("Hide Hazards");
			} else {
				layers.setTitle("Show Hazards");
			}
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
			if(showingHazards == false){
				showingHazards = true;
				layers.setTitle("Hide Hazards");
			} else {
				showingHazards = false;
				layers.setTitle("Show Hazards");
			}
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs
					.edit();
			editor.putBoolean("showingHazards",showingHazards);
			editor.commit();
			drawHazards();
		} else if (item.getItemId() == R.id.main_menu_list_view) {
			if (mCurrentState == STATE_LIST_VIEW) {
				// Show map view
				Log.d("MainActivity", "Showing map view");
				setState(STATE_DEFAULT);
				//item.setIcon(R.drawable.list_view);
				this.invalidateOptionsMenu();
			} else {
				// Show list view
				Log.d("MainActivity", "Showing list view");
				setState(STATE_LIST_VIEW);
				//item.setIcon(R.drawable.map_view);
				this.invalidateOptionsMenu();
			}
		} else if(item.getItemId() == R.id.main_menu_help){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
	        alert.setTitle("Help");
	        WebView wv = new WebView(this);
	        wv.loadUrl("file:///android_asset/Help.html");
	        wv.getSettings().setSupportZoom(true);
	        wv.getSettings().setBuiltInZoomControls(true);
	        wv.setWebViewClient(new WebViewClient()
	        {
	            @Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url)
	            {
	                view.loadUrl(url);
	                return true;
	            }
	        });
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
		currentJob = null;
		currentField = null;
		showAdd(true);
		this.currentPolygon = new MyPolygon(map, this);
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
			FragmentListView fragmentListView = (FragmentListView) fm
					.findFragmentById(R.id.list_view);
			fragmentListView.getData();
			FragmentTransaction ft = fm.beginTransaction();
			ft.show(fragmentListView);
			ft.commit();

			this.fragmentListView = fragmentListView;
		}
		// Officially in new state
		mCurrentState = newState;
		this.invalidateOptionsMenu();
	}

	private Void showEdit(Boolean transition) {
		if (editIsShowing == 0) {
			hideAdd(false);
			editIsShowing = 1;
			FrameLayout layout = (FrameLayout) findViewById(R.id.fragment_container_edit_job);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout
					.getLayoutParams();
			params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
			layout.setLayoutParams(params);

			FragmentManager fm = getSupportFragmentManager();
			FragmentEditJobPopup fragment = new FragmentEditJobPopup();
			FragmentTransaction ft = fm.beginTransaction();
			if (transition)
				ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
			ft.add(R.id.fragment_container_edit_job, fragment, "edit_job");
			ft.commit();

			fragmentEditField = fragment;
		}
		return null;
	}

	private void hideEdit(Boolean transition) {
		if (editIsShowing == 1) {
			editIsShowing = 0;
			FragmentManager fm = getSupportFragmentManager();
			FragmentEditJobPopup fragment = (FragmentEditJobPopup) fm
					.findFragmentByTag("edit_job");
			// Set height so transition works
			FrameLayout layout = (FrameLayout) findViewById(R.id.fragment_container_edit_job);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout
					.getLayoutParams();
			params.height = fragment.getHeight();
			layout.setLayoutParams(params);
			// Do transition
			FragmentTransaction ft = fm.beginTransaction();
			if (transition)
				ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
			ft.remove(fragment);
			ft.commit();
			fragmentEditField = null;
		}
	}

	private Void showAdd(Boolean transition) {
		if (addIsShowing == 0) {
			addIsShowing = 1;
			hideEdit(false);
			// Set height back to wrap, in case add buttons or something
			FrameLayout layout = (FrameLayout) findViewById(R.id.fragment_container_add_field);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout
					.getLayoutParams();
			params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
			layout.setLayoutParams(params);

			FragmentManager fm = getSupportFragmentManager();
			FragmentAddField fragment = new FragmentAddField();
			FragmentTransaction ft = fm.beginTransaction();
			if (transition)
				ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
			ft.add(R.id.fragment_container_add_field, fragment, "add_field");
			ft.commit();
			fragmentAddField = fragment;
		}
		this.invalidateOptionsMenu();
		return null;
	}

	private void hideAdd(Boolean transition) {
		if (addIsShowing == 1) {
			addIsShowing = 0;
			FragmentManager fm = getSupportFragmentManager();
			FragmentAddField fragment = (FragmentAddField) fm
					.findFragmentByTag("add_field");
			// Set height so transition works
			FrameLayout layout = (FrameLayout) findViewById(R.id.fragment_container_add_field);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout
					.getLayoutParams();
			params.height = fragment.getHeight();
			layout.setLayoutParams(params);
			// Do transition
			FragmentTransaction ft = fm.beginTransaction();
			if (transition)
				ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
			ft.remove(fragment);
			ft.commit();
			fragmentAddField = null;
		}
		this.invalidateOptionsMenu();
	}
	
	
	
	@Override
	public void SyncControllerRefreshOperations() {
		this.loadOperations();
	}

	@Override
	public void SyncControllerUpdateField(Integer localId) {
		//Check if field still exists, if so redraw boundary
		Field localField = this.FindFieldById(localId);
		if(localField != null) {
			MyPolygon polygon = null;
			for (int i = 0; i < FieldsOnMap.size(); i++) {
				if (FieldsOnMap.get(i).getId() == localField.getId()) {
					polygon = FieldsOnMap.get(i).getPolygon();
				}
			}
			
			if(polygon != null){
				//Redraw polygon
				polygon.updatePoints(localField.getBoundary());
			}
		}		
	}

	@Override
	public void SyncControllerUpdateJob(Integer localId) {
		//Check if job exists, if so redraw status
		Job localJob = this.FindJobById(localId);
		if(localJob != null) {
			Field localField = this.FindFieldByName(localJob.getFieldName());
			if(localField != null) {
				MyPolygon polygon = null;
				for (int i = 0; i < FieldsOnMap.size(); i++) {
					if (FieldsOnMap.get(i).getId() == localField.getId()) {
						polygon = FieldsOnMap.get(i).getPolygon();
					}
				}
				
				if(polygon != null){
					//Redraw polygon color
					if (localJob.getStatus() == Job.STATUS_NOT_PLANNED) {
						polygon.setFillColor(Field.FILL_COLOR_NOT_PLANNED);
					} else if (localJob.getStatus() == Job.STATUS_PLANNED) {
						polygon.setFillColor(Field.FILL_COLOR_PLANNED);
					} else if (localJob.getStatus() == Job.STATUS_STARTED) {
						polygon.setFillColor(Field.FILL_COLOR_STARTED);
					} else if (localJob.getStatus() == Job.STATUS_DONE) {
						polygon.setFillColor(Field.FILL_COLOR_DONE);
					}
				}
			}
		}
	}
	
	@Override
	public void SyncControllerDeleteField(Integer localId){
		//Check if field still exists, if so redraw boundary
		Field localField = this.FindFieldById(localId);
		if(localField != null) {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			database.delete(TableFields.TABLE_NAME, TableFields.COL_ID + " = " + Integer.toString(localId), null);
			dbHelper.close();
			
			MyPolygon polygon = null;
			for(int i=0; i<FieldsOnMap.size(); i++){
				if(FieldsOnMap.get(i).getId() == localField.getId()){
					polygon = FieldsOnMap.get(i).getPolygon();
					FieldsOnMap.remove(i);
				}
			}
			if(polygon != null){
				//Remove polygon
				polygon.remove();
			}
			
			if(this.currentField != null && this.currentField.getId() == localField.getId()){
				if(editIsShowing == 1) hideEdit(true);
				if(addIsShowing == 1) hideAdd(true);
				this.currentField = null;
				//Remove polygon
				if(this.currentPolygon != null){
					this.currentPolygon.delete();
					this.currentPolygon = null;
				}
			}
			if(this.fragmentListView != null) this.fragmentListView.getData();
		}
	}
	
	@Override
	public void SyncControllerDeleteJob(Integer localId){
		//Check if job exists, if so redraw status
		Job localJob = this.FindJobById(localId);
		if(localJob != null) {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			database.delete(TableJobs.TABLE_NAME, TableJobs.COL_ID + " = " + Integer.toString(localId), null);
			dbHelper.close();
			
			Field localField = this.FindFieldByName(localJob.getFieldName());
			if(localField != null) {
				MyPolygon polygon = null;
				for (int i = 0; i < FieldsOnMap.size(); i++) {
					if (FieldsOnMap.get(i).getId() == localField.getId()) {
						polygon = FieldsOnMap.get(i).getPolygon();
					}
				}
				
				if(polygon != null){
					//Redraw polygon color
					polygon.setFillColor(Field.FILL_COLOR_NOT_PLANNED);
				}
			}
		}
		if (this.fragmentListView != null) this.fragmentListView.getData();
	}
	
	@Override
	public void SyncControllerAddField(Integer localId){
		//Check if field still exists, if so redraw boundary
		Field localField = this.FindFieldById(localId);
		if(localField != null) {
			// Add to list so we can catch click events
			localField.setMap(map);
			List<LatLng> points = localField.getBoundary();
			
			// Now draw this field
			// Create polygon
			if(points != null && points.isEmpty() == false) {
				Job theJob = FindJobByFieldName(localField.getName());
				PolygonOptions polygonOptions = new PolygonOptions();
				if (theJob == null || theJob.getStatus() == Job.STATUS_NOT_PLANNED) {
					polygonOptions.fillColor(Field.FILL_COLOR_NOT_PLANNED);
				} else if (theJob.getStatus() == Job.STATUS_PLANNED) {
					polygonOptions.fillColor(Field.FILL_COLOR_PLANNED);
				} else if (theJob.getStatus() == Job.STATUS_STARTED) {
					polygonOptions.fillColor(Field.FILL_COLOR_STARTED);
				} else if (theJob.getStatus() == Job.STATUS_DONE) {
					polygonOptions.fillColor(Field.FILL_COLOR_DONE);
				}
				polygonOptions.strokeWidth(Field.STROKE_WIDTH);
				polygonOptions.strokeColor(Field.STROKE_COLOR);
				for (int i = 0; i < points.size(); i++) {
					polygonOptions.add(points.get(i));
				}
				localField.setPolygon(new MyPolygon(map, map.addPolygon(polygonOptions), this));
				if (currentField != null && localField.getId() == currentField.getId()) {
					this.currentPolygon = localField.getPolygon();
					this.currentPolygon.setLabel(localField.getName(), true);
				} else {
					localField.getPolygon().setLabel(localField.getName());
				}
			}
			FieldsOnMap.add(localField);
		}
		if (this.fragmentListView != null) this.fragmentListView.getData();
	}
	
	@Override
	public void SyncControllerAddJob(Integer localId){
		//Check if job exists, if so redraw status
		Job localJob = this.FindJobById(localId);
		if(localJob != null) {
			Field localField = this.FindFieldByName(localJob.getFieldName());
			if(localField != null) {
				MyPolygon polygon = null;
				for (int i = 0; i < FieldsOnMap.size(); i++) {
					if (FieldsOnMap.get(i).getId() == localField.getId()) {
						polygon = FieldsOnMap.get(i).getPolygon();
					}
				}
				if(polygon != null){
					//Redraw polygon color
					if (localJob.getStatus() == Job.STATUS_NOT_PLANNED) {
						polygon.setFillColor(Field.FILL_COLOR_NOT_PLANNED);
					} else if (localJob.getStatus() == Job.STATUS_PLANNED) {
						polygon.setFillColor(Field.FILL_COLOR_PLANNED);
					} else if (localJob.getStatus() == Job.STATUS_STARTED) {
						polygon.setFillColor(Field.FILL_COLOR_STARTED);
					} else if (localJob.getStatus() == Job.STATUS_DONE) {
						polygon.setFillColor(Field.FILL_COLOR_DONE);
					}
				}
			}
		}
		if (this.fragmentListView != null) this.fragmentListView.getData();
	}

	@Override
	public void SyncControllerChangeOrganizations(){
		drawMap();
	}
	
	@Override
	public void EditJobEditField() {
		showAdd(true);
		
		// Edit this fields points
		if(this.currentPolygon == null){
			this.currentPolygon = new MyPolygon(map, this);
		} else {
			this.currentPolygon.edit();
		}
	}

	@Override
	public void EditJobSave(Job job) {
		EditJobSave(job, true, true);
	}

	@Override
	public void EditJobSave(Job job, Boolean changeState, Boolean unselect) {
		currentJob = job;
		

		if (unselect && this.currentPolygon != null){
			this.currentPolygon.unselect();
		}
		if (job != null && job.getStatus() != Job.STATUS_NOT_PLANNED) {
			// Save new job in db
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(TableJobs.COL_WORKER_NAME, job.getWorkerName());
			if(currentField == null){
				values.put(TableJobs.COL_FIELD_NAME, currentJob.getFieldName());
			} else {
				values.put(TableJobs.COL_FIELD_NAME, currentField.getName());
			}
			values.put(TableJobs.COL_STATUS, job.getStatus());
			values.put(TableJobs.COL_COMMENTS, job.getComments());
			values.put(TableJobs.COL_DATE_OF_OPERATION,
					job.getDateOfOperation());
			values.put(TableJobs.COL_HAS_CHANGED, job.getHasChanged());
			values.put(TableJobs.COL_DATE_CHANGED, job.getDateChanged());
			values.put(TableJobs.COL_OPERATION_ID, currentOperationId);

			if (job.getId() == null) {
				Integer insertId = (int) database.insert(TableJobs.TABLE_NAME,
						null, values);
				currentJob.setId(insertId);
				if (job.getWorkerName().isEmpty() == false) {
					// Save this choice in preferences for next open
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("WorkerName", job.getWorkerName());
					editor.commit();
				}
				Log.d("MainActivity - EditJobSave",
						"Adding new job to db:" + job.getFieldName()
								+ " - Field Id:" + Integer.toString(insertId)
								+ ", Op Id:" + currentOperationId);
			} else {
				// Update job
				String where = TableJobs.COL_ID + " = " + job.getId() +  " AND " + TableJobs.COL_DELETED + " = 0";;
				database.update(TableJobs.TABLE_NAME, values, where, null);
				Log.d("MainActivity - EditJobSave",
						"Updating job in db:" + job.getFieldName()
								+ " - Field Id:"
								+ Integer.toString(job.getId()) + ", Op Id:"
								+ currentOperationId);
			}
			dbHelper.close();
			// Set fill according to status
			if(this.currentPolygon != null){
				if (currentJob.getStatus() == Job.STATUS_NOT_PLANNED) {
					this.currentPolygon.setFillColor(Field.FILL_COLOR_NOT_PLANNED);
				} else if (currentJob.getStatus() == Job.STATUS_PLANNED) {
					this.currentPolygon.setFillColor(Field.FILL_COLOR_PLANNED);
				} else if (currentJob.getStatus() == Job.STATUS_STARTED) {
					this.currentPolygon.setFillColor(Field.FILL_COLOR_STARTED);
				} else if (currentJob.getStatus() == Job.STATUS_DONE) {
					this.currentPolygon.setFillColor(Field.FILL_COLOR_DONE);
				}
			}
		} else {
			currentJob = null;
		}
		if (changeState)
			hideEdit(true);

		if(unselect) {
			this.currentField = null;
			this.currentJob = null;
		}
		if (this.fragmentListView != null)
			this.fragmentListView.getData();
		
		this.trelloController.syncDelayed();
	}

	@Override
	public void EditJobDelete() {
		// Find and delete job if exists
		if (currentJob != null) {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(TableJobs.COL_DELETED, 1);
			values.put(TableJobs.COL_HAS_CHANGED, 1);
			values.put(TableJobs.COL_DATE_CHANGED, DatabaseHelper.dateToStringUTC(new Date()));
			String where = TableJobs.COL_ID + " = "+ Integer.toString(currentJob.getId())  + " AND " + TableJobs.COL_DELETED + " = 0";
			database.update(TableJobs.TABLE_NAME, values, where, null);
			dbHelper.close();
			currentJob = null;
			drawMap();
			
			if(this.currentField == null){
				//Close edit
				hideEdit(true);
			} else {
				if (this.fragmentEditField != null) this.fragmentEditField.refreshData();
			}
			
			if (this.fragmentListView != null)
				this.fragmentListView.getData();
			this.trelloController.syncDelayed();
		}
	}

	@Override
	public Field EditJobGetCurrentField() {
		if (currentField == null) {
			Log.d("MainActivity - EditJobGetCurrentField",
					"currentfield is null");
		} else {
			Log.d("MainActivity - EditJobGetCurrentField",
					"currentfield is vaild");
		}
		return this.currentField;
	}

	@Override
	public Job EditJobGetCurrentJob() {
		if (currentJob == null) {
			Log.d("MainActivity - EditJobGetCurrentJob", "currentJob is null");
		} else {
			Log.d("MainActivity - EditJobGetCurrentJob", "currentJob is vaild");
		}
		return currentJob;
	}

	@Override
	public void EditJobDateSave(int year, int month, int day) {
		if (fragmentEditField != null) {
			fragmentEditField.updateDate(year, month, day);
		}
	}

	@Override
	public Field AddFieldGetCurrentField() {
		return this.currentField;
	}

	@Override
	public void AddFieldUndo() {
		this.currentPolygon.undo();
	}

	@Override
	public void AddFieldDone(String name, Integer acres) {
		// Check if field name is valid and doesn't exist already
		if (name.length() == 0) {
			// Tell them to input a name
			// TODO add this message to R.strings
			Toast.makeText(this, "Field name cannot be blank.",
					Toast.LENGTH_LONG).show();
		} else {
			// Check if field name already exists in db
			if (FindFieldByName(name) != null && currentField == null) {
				Toast.makeText(
						this,
						"A field with this name already exists. Field names must be unique.",
						Toast.LENGTH_LONG).show();
			} else {
				this.currentPolygon.complete();
				this.currentPolygon.setLabel(name, true);
				if (currentJob == null) {
					this.currentPolygon
							.setFillColor(Field.FILL_COLOR_NOT_PLANNED);
				} else {
					if (currentJob.getStatus() == Job.STATUS_NOT_PLANNED) {
						this.currentPolygon
								.setFillColor(Field.FILL_COLOR_NOT_PLANNED);
					} else if (currentJob.getStatus() == Job.STATUS_PLANNED) {
						this.currentPolygon
								.setFillColor(Field.FILL_COLOR_PLANNED);
					} else if (currentJob.getStatus() == Job.STATUS_STARTED) {
						this.currentPolygon
								.setFillColor(Field.FILL_COLOR_STARTED);
					} else if (currentJob.getStatus() == Job.STATUS_DONE) {
						this.currentPolygon.setFillColor(Field.FILL_COLOR_DONE);
					}
				}

				List<LatLng> points = this.currentPolygon.getPoints();
				Boolean wasAnEdit = false;
				if (currentField == null) {
					currentField = new Field(points, map);
				} else {
					currentField.setBoundary(points);
					wasAnEdit = true;
				}
				currentField.setName(name);
				currentField.setAcres(acres);

				Log.d("MainActivity", "Acres:" + Integer.toString(acres));
				String strNewBoundary = "";
				if(points != null && points.isEmpty() == false){
					// Generate boundary
					StringBuilder newBoundary = new StringBuilder(
							points.size() * 20);
					for (int i = 0; i < points.size(); i++) {
						newBoundary.append(points.get(i).latitude);
						newBoundary.append(",");
						newBoundary.append(points.get(i).longitude);
						newBoundary.append(",");
					}
					newBoundary.deleteCharAt(newBoundary.length() - 1);
					strNewBoundary = newBoundary.toString();
				}
				// Save this field to the db
				SQLiteDatabase database = dbHelper.getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(TableFields.COL_NAME, currentField.getName());
				values.put(TableFields.COL_ACRES, currentField.getAcres());
				values.put(TableFields.COL_BOUNDARY, strNewBoundary);
				
				//TODO only update if something changed
				values.put(TableFields.COL_HAS_CHANGED, 1);
				values.put(TableFields.COL_DATE_CHANGED, DatabaseHelper.dateToStringUTC(new Date()));

				if (wasAnEdit == false) {
					Integer insertId = (int) database.insert(
							TableFields.TABLE_NAME, null, values);
					currentField.setId(insertId);
				} else {
					database.update(
							TableFields.TABLE_NAME,
							values,
							TableFields.COL_ID + " = "
									+ Integer.toString(currentField.getId()),
							null);
				}
				dbHelper.close();

				// Add to list so we can catch click events
				currentField.setPolygon(this.currentPolygon);

				if (wasAnEdit == false) {
					FieldsOnMap.add(currentField);
				} else {
					for (int i = 0; i < FieldsOnMap.size(); i++) {
						if (FieldsOnMap.get(i).getId() == currentField.getId()) {
							FieldsOnMap.get(i).setName(name);
							FieldsOnMap.get(i).setPolygon(this.currentPolygon);
							FieldsOnMap.get(i).setAcres(acres);
							FieldsOnMap.get(i).setBoundary(points);
						}
					}
				}
				
				// add or update in list view
				if (this.fragmentListView != null) this.fragmentListView.getData();
				
				// Check to see if we have any operations
				if (operationsList.isEmpty() == false) {
					// Check if any operation selected
					if (currentOperationId != 0) {
						showEdit(true);
					} else {
						// Make them select an operation
						// TODO popup list??
					}
				} else {
					// Add an operation
					createOperation(new Callable<Void>() {
						public Void call() {
							return showEdit(true);
						}
					});
				}
				this.trelloController.syncDelayed();
			}
		}
	}

	@Override
	public void AddFieldDelete() {
		//Delete the current field
		if(this.currentField != null){
			//Delete field from database
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(TableFields.COL_DELETED, 1);
			values.put(TableFields.COL_HAS_CHANGED, 1);
			values.put(TableFields.COL_DATE_CHANGED, DatabaseHelper.dateToStringUTC(new Date()));
			String where = TableFields.COL_ID + " = "+ Integer.toString(currentField.getId());
			database.update(TableFields.TABLE_NAME, values, where, null);
			
			dbHelper.close();
			for(int i=0; i<FieldsOnMap.size(); i++){
				if(FieldsOnMap.get(i).getId() == currentField.getId()){
					FieldsOnMap.remove(i);
				}
			}
			currentField = null;
			this.trelloController.syncDelayed();
		}		
		//Remove polygon
		if(this.currentPolygon != null){
			this.currentPolygon.delete();
			this.currentPolygon = null;
		}
		hideAdd(true);
		if (this.fragmentListView != null)
			this.fragmentListView.getData();
	}
	
	@Override
	public Integer listViewGetCurrentOperationId() {
		return currentOperationId;
	}

	@Override
	public String ListViewGetCurrentFieldName() {
		if(this.currentField == null){
			return null;
		} else {
			return this.currentField.getName();
		}
	}
	
	private Job FindJobByFieldName(String name) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		// Find job
		Job theJob = null;
		String where = TableJobs.COL_FIELD_NAME + " = '" + name + "'" + " AND "
				+ TableJobs.COL_OPERATION_ID + " = "
				+ Integer.toString(currentOperationId) + " AND " + TableJobs.COL_DELETED + " = 0";
		Cursor cursor = database.query(TableJobs.TABLE_NAME, TableJobs.COLUMNS,
				where, null, null, null, null);
		if (cursor.moveToFirst()) {
			theJob = Job.cursorToJob(cursor);
		}
		cursor.close();
		dbHelper.close();
		return theJob;
	}

	private Job FindJobById(Integer id) {
		if (id != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find job
			Job theJob = null;
			String where = TableJobs.COL_ID + " = " + Integer.toString(id) + " AND " + TableJobs.COL_DELETED + " = 0";
			Cursor cursor = database.query(TableJobs.TABLE_NAME,
					TableJobs.COLUMNS, where, null, null, null, null);
			if (cursor.moveToFirst()) {
				theJob = Job.cursorToJob(cursor);
			}
			cursor.close();
			dbHelper.close();
			return theJob;
		} else {
			return null;
		}
	}

	private Field FindFieldByName(String name) {
		if (name != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find current field
			Field theField = null;
			String where = TableFields.COL_NAME + " = '" + name + "' AND " + TableFields.COL_DELETED + " = 0";
			Cursor cursor = database.query(TableFields.TABLE_NAME,
					TableFields.COLUMNS, where, null, null, null, null);
			if (cursor.moveToFirst()) {
				theField = Field.cursorToField(cursor);
				theField.setMap(map);
			}
			cursor.close();
			dbHelper.close();
			return theField;
		} else {
			return null;
		}
	}

	private Field FindFieldById(Integer id) {
		if (id != null) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			// Find current field
			Field theField = null;
			String where = TableFields.COL_ID + " = " + Integer.toString(id) + " AND " + TableFields.COL_DELETED + " = 0";;
			Cursor cursor = database.query(TableFields.TABLE_NAME,
					TableFields.COLUMNS, where, null, null, null, null);
			if (cursor.moveToFirst()) {
				theField = Field.cursorToField(cursor);
				theField.setMap(map);
			}
			cursor.close();
			dbHelper.close();
			return theField;
		} else {
			return null;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// Spinner selected item
		Operation operation = (Operation) parent.getItemAtPosition(pos);
		if (operation.getId() == null) {
			// Create new operation
			selectCurrentOperationInSpinner(); // Go back to original for now,
												// in case cancel
			createOperation(null);
		} else {
			currentOperationId = operation.getId();
			Log.d("Set HERE2",
					"onItemSelected - OpID:"
							+ Integer.toString(currentOperationId));
			// Save this choice in preferences for next open
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("currentOperationId", currentOperationId);
			editor.commit();
		}
		if (this.fragmentListView != null) this.fragmentListView.getData();
		drawMap();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// Spinner, select disappear, selection removed from adapter TODO
		Log.d("MainMenu - onNothingSelected", "Nothing selected");
	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		if (this.currentPolygon != null) {
			this.currentPolygon.onMarkerDrag(arg0);
		}
	}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		if (this.currentPolygon != null) {
			this.currentPolygon.onMarkerDragEnd(arg0);
		}
	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		if (this.currentPolygon != null) {
			this.currentPolygon.onMarkerDragStart(arg0);
		}
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		Boolean found = false;
		if (this.currentPolygon != null) {
			found = this.currentPolygon.onMarkerClick(arg0);
		}
		if(found == false){
			this.onMapClick(arg0.getPosition());
		}
		return false;
	}

	@Override
	public void MyPolygonUpdateAcres(Float acres) {
		if(this.fragmentAddField != null){
			this.fragmentAddField.autoAcres(acres);
		}
	}
	
	private void checkGPS(){
		final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
	    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        buildAlertMessageNoGps();
	    }
	}
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
		               startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
		                dialog.cancel();
		           }
		       });
		final AlertDialog alert = builder.create();
		alert.show();
	}
}
