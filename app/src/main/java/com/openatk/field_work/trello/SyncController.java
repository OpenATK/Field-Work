package com.openatk.field_work.trello;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.maps.model.LatLng;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.openatk.field_work.MainActivity;
import com.openatk.field_work.db.DatabaseHelper;
import com.openatk.field_work.db.TableFields;
import com.openatk.field_work.db.TableJobs;
import com.openatk.field_work.db.TableOperations;
import com.openatk.field_work.db.TableWorkers;
import com.openatk.field_work.models.Operation;

public class SyncController  {

//	private Context AppContext;
//	private TrelloController trelloController;	
//	private DatabaseHelper dbHelper;
//	
//	private SyncControllerListener listener;
//	
//	private List<Integer> fieldsChanged = new ArrayList<Integer>();
//	private List<Integer> jobsChanged = new ArrayList<Integer>();
//	private List<Integer> fieldsDeleted = new ArrayList<Integer>();
//	private List<Integer> jobsDeleted = new ArrayList<Integer>();
//	private List<Integer> fieldsAdded = new ArrayList<Integer>();
//	private List<Integer> jobsAdded = new ArrayList<Integer>();
//	private Boolean updateOperations = false;
//	private Boolean changeOrganizations = false;
//
//	// Interface for receiving data
//	public interface SyncControllerListener {
//		public void SyncControllerRefreshOperations();
//		public void SyncControllerUpdateField(Integer localId);
//		public void SyncControllerUpdateJob(Integer localId);
//		public void SyncControllerDeleteField(Integer localId);
//		public void SyncControllerDeleteJob(Integer localId);
//		public void SyncControllerAddField(Integer localId);
//		public void SyncControllerAddJob(Integer localId);
//		public void SyncControllerChangeOrganizations();
//	}
//	
//	
//	public SyncController(Context appContext, TrelloController trelloController, SyncControllerListener listener) {
//		super();
//		AppContext = appContext;
//		this.trelloController = trelloController;
//		this.dbHelper = new DatabaseHelper(AppContext);
//		this.listener = listener;
//	}
//	
//	@Override
//	public void changeOrganization() {
//		//Organization changed
//		Log.d("changeOrganization", "Organization has been changed.");
//		//Delete everything
//		SQLiteDatabase database = dbHelper.getWritableDatabase();
//		database.delete(TableJobs.TABLE_NAME, null, null);
//		database.delete(TableOperations.TABLE_NAME, null, null);
//		database.delete(TableWorkers.TABLE_NAME, null, null);
//		database.delete(TableFields.TABLE_NAME, null, null);
//		dbHelper.close();
//		
//		updateOperations = true; //Reload operations
//		changeOrganizations = true;
//
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//		SharedPreferences.Editor editor = prefs.edit();
//		editor.putString("BoardId", "");
//		editor.putString("FieldList", "");
//		editor.putString("OperatorList", "");
//		editor.commit();
//	}
//	
//	@Override
//	public void updateBoard(IBoard localBoard, IBoard trelloBoard) {
//		if(trelloBoard.getName().contentEquals("OpenATK - Field Work App") == false || trelloBoard.getClosed()){
//			//Delete all jobs, operations
//			//QUESTION ???? (Delete Fields and workers?)
//			SQLiteDatabase database = dbHelper.getWritableDatabase();
//			database.delete(TableJobs.TABLE_NAME, null, null);
//			database.delete(TableOperations.TABLE_NAME, null, null);
//			
//			//Change all fields and workers to changed so they upload again
//			ContentValues values = new ContentValues();
//			values.put(TableFields.COL_REMOTE_ID, "");
//			values.put(TableFields.COL_HAS_CHANGED, 1);
//			database.update(TableFields.TABLE_NAME, values, null, null);
//			
//			ContentValues values2 = new ContentValues();
//			values.put(TableWorkers.COL_REMOTE_ID, "");
//			values2.put(TableWorkers.COL_HAS_CHANGED, 1);
//			database.update(TableWorkers.TABLE_NAME, values, null, null);
//			
//			dbHelper.close();
//			
//			updateOperations = true; //Reload operations
//
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//			SharedPreferences.Editor editor = prefs.edit();
//			editor.putString("BoardId", "");
//			editor.putString("FieldList", "");
//			editor.putString("OperatorList", "");
//			editor.commit();
//		}
//	}
//
//	@Override
//	public void addBoard(IBoard trelloBoard) {
//		if(trelloBoard.getName().contentEquals("OpenATK - Field Work App")){
//			//Delete all jobs, operations
//			SQLiteDatabase database = dbHelper.getWritableDatabase();
//			
//			//Change all fields and workers to changed so they upload again
//			ContentValues values = new ContentValues();
//			values.put(TableFields.COL_REMOTE_ID, "");
//			values.put(TableFields.COL_HAS_CHANGED, 1);
//			database.update(TableFields.TABLE_NAME, values, null, null);
//			
//			ContentValues values2 = new ContentValues();
//			values.put(TableWorkers.COL_REMOTE_ID, "");
//			values2.put(TableWorkers.COL_HAS_CHANGED, 1);
//			database.update(TableWorkers.TABLE_NAME, values, null, null);
//			
//			dbHelper.close();
//			
//			updateOperations = true; //Reload operations
//
//			
//			//Save new board id, moving to this new one
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//			SharedPreferences.Editor editor = prefs.edit();
//			editor.putString("BoardId", trelloBoard.getTrelloId().trim());
//			editor.commit();
//		}
//	}
//
//	@Override
//	public void setBoardTrelloId(IBoard localBoard, String newId) {
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//		SharedPreferences.Editor editor = prefs.edit();
//		editor.putString("BoardId", newId);
//		editor.commit();
//	}
//	
//	@Override
//	public void setBoardLocalChanges(IBoard localBoard, Boolean changes) {
//		//Unused
//	}
//
//	@Override
//	public void updateList(IList localList, IList trelloList) {
//		if(localList.getName().contentEquals("Settings - Operator List") && (trelloList.getName().contentEquals(localList.getName()) == false || trelloList.getClosed() == true)){
//			//Remove all workers
//			SQLiteDatabase database = dbHelper.getWritableDatabase();
//			database.delete(TableWorkers.TABLE_NAME, null, null);
//			dbHelper.close();
//			
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//			SharedPreferences.Editor editor = prefs.edit();
//			editor.putString("OperatorList", "");
//			editor.commit();
//		} else if(localList.getName().contentEquals("Settings - Field List")  && (trelloList.getName().contentEquals(localList.getName()) == false || trelloList.getClosed() == true)){
//			//Remove all fields
//			SQLiteDatabase database = dbHelper.getWritableDatabase();
//			database.delete(TableFields.TABLE_NAME, null, null);
//			dbHelper.close();
//			
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//			SharedPreferences.Editor editor = prefs.edit();
//			editor.putString("FieldList", "");
//			editor.commit();
//		} else if(trelloList.getClosed() == true){
//			//Operation that is archived on trello
//			//Delete local operation from table
//			SQLiteDatabase database = dbHelper.getWritableDatabase();
//			database.delete(TableOperations.TABLE_NAME, TableOperations.COL_ID + " = " + Integer.toString((Integer)localList.getLocalId()), null);
//			
//			//Delete all jobs with this operation id
//			database.delete(TableJobs.TABLE_NAME, TableJobs.COL_OPERATION_ID + " = " + Integer.toString((Integer)localList.getLocalId()), null);
//			dbHelper.close();
//			
//			updateOperations = true; //Reload operations TODO what if this operation is selected
//		} else if(trelloList.getName().contentEquals(localList.getName()) == false) {
//			//Operation that changed its name
//			//Update local to match trello
//			SQLiteDatabase database = dbHelper.getWritableDatabase();
//			String where = TableOperations.COL_ID + " = " + Integer.toString((Integer)localList.getLocalId());
//			ContentValues values = new ContentValues();
//			values.put(TableOperations.COL_NAME, trelloList.getName());
//			database.update(TableOperations.TABLE_NAME, values, where, null);
//			dbHelper.close();
//			
//			updateOperations = true; //Reload operations
//		}
//	}
//
//	@Override
//	public void addList(IList trelloList) {
//		Log.d("SyncController - addList", trelloList.getName());
//
//		if(trelloList.getClosed() == false){
//			if(trelloList.getName().contentEquals("Settings - Operator List")){
//				//QUESTION ???? what if operation list already exists
//				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//				SharedPreferences.Editor editor = prefs.edit();
//				editor.putString("OperatorList", trelloList.getTrelloId().trim());
//				editor.commit();
//			} else if(trelloList.getName().contentEquals("Settings - Field List")){
//				//QUESTION ???? what if field list already exists
//				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//				SharedPreferences.Editor editor = prefs.edit();
//				editor.putString("FieldList", trelloList.getTrelloId().trim());
//				editor.commit();
//			} else {
//				if(trelloList.getName().contentEquals("To Do") == false && trelloList.getName().contentEquals("Doing") == false && trelloList.getName().contentEquals("Done") == false){
//					//New operation, add it to the operation table
//					SQLiteDatabase database = dbHelper.getWritableDatabase();
//					ContentValues values = new ContentValues();
//					values.put(TableOperations.COL_NAME, trelloList.getName());
//					values.put(TableOperations.COL_HAS_CHANGED, 0);
//					values.put(TableOperations.COL_REMOTE_ID, trelloList.getTrelloId().trim());
//					database.insert(TableOperations.TABLE_NAME,null, values);
//					dbHelper.close();
//					
//					updateOperations = true; //Reload operations
//				}
//			}
//		}
//	}
//
//	@Override
//	public void setListTrelloId(IList localList, String newId) {
//		Log.d("SyncController - setListTrelloId", "setListTrelloId");
//		if(localList.getName().contentEquals("Settings - Operator List")){
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//			SharedPreferences.Editor editor = prefs.edit();
//			editor.putString("OperatorList", newId);
//			editor.commit();
//		} else if(localList.getName().contentEquals("Settings - Field List")){
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//			SharedPreferences.Editor editor = prefs.edit();
//			editor.putString("FieldList", newId);
//			editor.commit();
//		} else {
//			SQLiteDatabase database = dbHelper.getWritableDatabase();
//			String where = TableOperations.COL_ID + " = " + Integer.toString((Integer)localList.getLocalId());
//			ContentValues values = new ContentValues();
//			values.put(TableOperations.COL_REMOTE_ID, newId);
//			database.update(TableOperations.TABLE_NAME, values, where, null);
//			dbHelper.close();
//		}
//	}
//
//	@Override
//	public void setListLocalChanges(IList localList, Boolean changes) {
//		if(localList.getName().contentEquals("Settings - Operator List")){
//			
//		} else if(localList.getName().contentEquals("Settings - Field List")){
//			
//		} else {
//			SQLiteDatabase database = dbHelper.getWritableDatabase();
//			String where = TableOperations.COL_ID + " = " + Integer.toString((Integer)localList.getLocalId());
//			ContentValues values = new ContentValues();
//			
//			Integer hasChanged = 0;
//			if(changes) hasChanged = 1;
//			
//			values.put(TableOperations.COL_HAS_CHANGED, hasChanged);
//			database.update(TableOperations.TABLE_NAME, values, where, null);
//			dbHelper.close();
//		}
//	}
//	
//	@Override
//	public void updateCard(ICard localCard, ICard trelloCard) {
//		//Card from Trello, update if needed
//		Boolean needsUpdate = false;
//		if(localCard.getClosed() != trelloCard.getClosed()){
//			//Delete
//			needsUpdate = true;
//		}
//		if(trelloCard.getClosed() == false){
//			if(localCard.getListId().contentEquals(trelloCard.getListId()) == false){
//				//Switched list
//				needsUpdate = true;
//			}
//			if(localCard.getName().contentEquals(trelloCard.getName()) == false){
//				//Name changed
//				needsUpdate = true;
//			}
//			if(localCard.getDesc().contentEquals(trelloCard.getDesc()) == false){
//				//Desc changed
//				needsUpdate = true;
//			}
//		}
//		
//		if(needsUpdate){
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//			String fieldsListId = prefs.getString("FieldList", "");
//			String operatorListId = prefs.getString("OperatorList", "");
//			
//			
//			Boolean convertionFailed = false;
//			if(trelloCard.getClosed() == false){
//				if(trelloCard.getListId().contentEquals(fieldsListId)){
//					//Is in fields list, try to convert to field
//					SQLiteDatabase database = dbHelper.getWritableDatabase();
//					
//					Boolean add = false;
//					int localType = ((MyCard)localCard).getType() ;
//					if(localType != MyCard.typeField){
//						//If wasn't in fields list before remove it from 
//						if(localType == MyCard.typeJob){
//							//Remove from jobs
//							database.delete(TableJobs.TABLE_NAME, TableJobs.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId()), null);
//						} else if(localType == MyCard.typeWorker){
//							//Remove from workers
//							database.delete(TableWorkers.TABLE_NAME, TableWorkers.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId()), null);
//						}
//						add = true;
//					}
//					
//					//Parse area and boundary from desc
//					Integer area = 0;
//					List<LatLng> boundary = new ArrayList<LatLng>();
//					
//					Pattern p = Pattern.compile("Area:[ ]?([0-9]+) ac;");
//					Matcher m = p.matcher(trelloCard.getDesc());
//					if(m.find()){
//						area = Integer.parseInt(m.group(1));
//					}
//					Pattern p2 = Pattern.compile("Boundary:[ ]?(.*);");
//					Matcher m2 = p2.matcher(trelloCard.getDesc());
//					if(m2.find()){
//						Log.d("SyncController", "updateCard - Found boundary");
//						
//						Pattern p3 = Pattern.compile("[(]([-]?[0-9]+[.]?[0-9]*)[,]([-]?[0-9]+[.]?[0-9]*)[)]");
//						Matcher m3 = p3.matcher(m2.group(1));
//						while(m3.find()){
//							//Each coordinate
//							Double lat = Double.parseDouble(m3.group(1));
//							Double lng = Double.parseDouble(m3.group(2));
//							boundary.add(new LatLng(lat, lng));
//							
//							Log.d("SyncController", "Point:" + Double.toString(lat) + "," + Double.toString(lng));
//						}
//					}
//					String strBoundary = "";
//					if(boundary.size() > 0){
//						Log.d("SyncController", "Boundary not empty");
//
//						for(int i=0; i<boundary.size(); i++){
//							strBoundary = strBoundary + boundary.get(i).latitude + "," + boundary.get(i).longitude + ",";
//						}
//						//Add first point again
//						strBoundary = strBoundary + boundary.get(0).latitude + "," + boundary.get(0).longitude;
//					}
//					
//					if(add){
//						//Add to fields
//						ContentValues values = new ContentValues();
//						values.put(TableFields.COL_NAME, trelloCard.getName());
//						values.put(TableFields.COL_ACRES, area);
//						values.put(TableFields.COL_BOUNDARY, strBoundary);
//						values.put(TableFields.COL_HAS_CHANGED, 0);
//						database.insert(TableFields.TABLE_NAME, null, values);
//					} else {
//						//Update local field
//						String where = TableFields.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId());
//						ContentValues values = new ContentValues();
//						values.put(TableFields.COL_NAME, trelloCard.getName());
//						values.put(TableFields.COL_ACRES, area);
//						values.put(TableFields.COL_BOUNDARY, strBoundary);
//						values.put(TableFields.COL_HAS_CHANGED, 0);
//						database.update(TableFields.TABLE_NAME, values, where, null);
//					}
//					dbHelper.close();
//				} else if(trelloCard.getListId().contentEquals(operatorListId)) {
//					//Is in worker list, try to convert to worker
//					SQLiteDatabase database = dbHelper.getWritableDatabase();
//
//					Boolean add = false;
//					int localType = ((MyCard)localCard).getType() ;
//					if(localType != MyCard.typeWorker){
//						//If wasn't in fields list before remove it from 
//						if(localType == MyCard.typeJob){
//							//Remove from jobs
//							database.delete(TableJobs.TABLE_NAME, TableJobs.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId()), null);
//						} else if(localType == MyCard.typeField){
//							//Remove from fields
//							database.delete(TableFields.TABLE_NAME, TableFields.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId()), null);
//						}
//						add = true;
//					}
//					
//					if(add){
//						//Add to workers
//						ContentValues values = new ContentValues();
//						values.put(TableWorkers.COL_NAME, trelloCard.getName());
//						values.put(TableWorkers.COL_HAS_CHANGED, 0);
//						database.insert(TableWorkers.TABLE_NAME, null, values);
//					} else {
//						//Update local workers
//						String where = TableWorkers.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId());
//						ContentValues values = new ContentValues();
//						values.put(TableWorkers.COL_NAME, trelloCard.getName());
//						values.put(TableWorkers.COL_HAS_CHANGED, 0);
//						database.update(TableWorkers.TABLE_NAME, values, where, null);
//					}
//					dbHelper.close();
//				} else {
//					//Try to convert to job
//					SQLiteDatabase database = dbHelper.getWritableDatabase();
//
//					Boolean add = false;
//					int localType = ((MyCard)localCard).getType();
//					if(localType != MyCard.typeJob){
//						//If wasn't in fields list before remove it from 
//						if(localType == MyCard.typeWorker){
//							//Remove from jobs
//							database.delete(TableWorkers.TABLE_NAME, TableWorkers.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId()), null);
//						} else if(localType == MyCard.typeField){
//							//Remove from fields
//							database.delete(TableFields.TABLE_NAME, TableFields.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId()), null);
//						}
//						add = true;
//					}
//					
//					String strStatus = "";
//					String strDate = "";
//					String name = "";
//					String worker = "";
//					int status = Job.STATUS_PLANNED;
//
//					
//					Pattern p = Pattern.compile("^(.+)[ ]([0-9]{1,2})[/]([0-9]{1,2})[/]([0-9]{2,4})[:][ ]?([^-]+)[-]?(.*)");
//					Matcher m = p.matcher(trelloCard.getName());
//					if(m.find()){		
//						strStatus = m.group(1);
//						
//						if(strStatus.contentEquals("Done")){
//							status = Job.STATUS_DONE;
//						} else if(strStatus.contentEquals("Started")){
//							status = Job.STATUS_STARTED;
//						} else if(strStatus.contentEquals("Planned")){
//							status = Job.STATUS_PLANNED;
//						} else {
//							convertionFailed = true;
//						}
//						if(convertionFailed == false){
//							int month = Integer.parseInt(m.group(2));
//							int day = Integer.parseInt(m.group(3));
//							int year = Integer.parseInt(m.group(4));
//							if(year < 2000){
//								year = year + 2000;
//							}
//							String strDateToParse = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);						
//							SimpleDateFormat dateFormaterLocal = new SimpleDateFormat("yyyy-M-d", Locale.US);
//							dateFormaterLocal.setTimeZone(TimeZone.getDefault());
//							Date d;
//							try {
//								d = dateFormaterLocal.parse(strDateToParse);
//							} catch (ParseException e) {
//								d = new Date(0);
//							}
//							
//							strDate = DatabaseHelper.dateToStringLocal(d);
//							name = m.group(5);
//							worker = m.group(6);
//						}
//					} else {
//						convertionFailed = true;
//					}
//					
//					String comments = "";
//					Pattern p2 = Pattern.compile("Comments:[ ]?(.*);");
//					Matcher m2 = p2.matcher(trelloCard.getDesc());
//					if(m2.find()){						
//						comments = m2.group(1);						
//					}
//					
//					/*  TODO CHANGE TO USING LABELS
//					int status = Job.STATUS_PLANNED;
//					if(trelloCard.getLabels().contains("green")){
//						status = Job.STATUS_DONE;
//					} else if(trelloCard.getLabels().contains("yellow")){
//						status = Job.STATUS_STARTED;
//					} else if(trelloCard.getLabels().contains("red")){
//						status = Job.STATUS_PLANNED;
//					}*/
//					
//					if(convertionFailed == false){
//						if(add){
//							//Add to jobs
//							ContentValues values = new ContentValues();
//							values.put(TableJobs.COL_FIELD_NAME, name);
//							values.put(TableJobs.COL_DATE_OF_OPERATION, strDate);
//							values.put(TableJobs.COL_WORKER_NAME, worker);
//							values.put(TableJobs.COL_STATUS, status);
//							values.put(TableJobs.COL_COMMENTS, comments);
//							values.put(TableJobs.COL_HAS_CHANGED, 0);
//							values.put(TableJobs.COL_DATE_CHANGED, DatabaseHelper.dateToStringUTC(trelloCard.getChangedDate()));
//							database.insert(TableJobs.TABLE_NAME, null, values);
//						} else {
//							//Update local job
//							String where = TableJobs.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId());
//							ContentValues values = new ContentValues();
//							values.put(TableJobs.COL_FIELD_NAME, name);
//							values.put(TableJobs.COL_DATE_OF_OPERATION, strDate);
//							values.put(TableJobs.COL_WORKER_NAME, worker);
//							values.put(TableJobs.COL_STATUS, status);
//							values.put(TableJobs.COL_HAS_CHANGED, 0);
//							values.put(TableJobs.COL_COMMENTS, comments);
//							database.update(TableJobs.TABLE_NAME, values, where, null);
//						}
//					}
//					dbHelper.close();
//				}
//			}
//			
//			Boolean delete = false;
//
//			if(trelloCard.getClosed() || convertionFailed){
//				delete = true;
//				SQLiteDatabase database = dbHelper.getWritableDatabase();
//				//Delete local field, job, or worker
//				int localType = ((MyCard)localCard).getType();
//				if(localType == MyCard.typeField){
//					fieldsDeleted.add((Integer)localCard.getLocalId());
//				} else if(localType == MyCard.typeJob){
//					jobsDeleted.add((Integer)localCard.getLocalId());
//				} else if(localType == MyCard.typeWorker){
//					database.delete(TableWorkers.TABLE_NAME, TableWorkers.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId()), null);
//				}
//				dbHelper.close();
//			}
//			int localType = ((MyCard)localCard).getType();
//			if(localType == MyCard.typeField){
//				if(delete == false) fieldsChanged.add(((Integer)localCard.getLocalId()));
//			} else if(localType == MyCard.typeJob){
//				if(delete == false) jobsChanged.add(((Integer)localCard.getLocalId()));
//			}
//		}
//	}
//
//	@Override
//	public void addCard(ICard trelloCard) {
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//		String fieldsListId = prefs.getString("FieldList", "");
//		String operatorListId = prefs.getString("OperatorList", "");
//		Log.d("AddCard:", trelloCard.getName());
//		Boolean convertionFailed = false;
//		if(trelloCard.getClosed() == false){
//			if(trelloCard.getListId().contentEquals(fieldsListId)){
//				//Is in fields list, try to convert to field
//				SQLiteDatabase database = dbHelper.getWritableDatabase();
//				
//				//Parse area and boundary from desc
//				Integer area = 0;
//				List<LatLng> boundary = new ArrayList<LatLng>();
//				
//				Pattern p = Pattern.compile("Area:[ ]?([0-9]+)[ ]?ac;");
//				Matcher m = p.matcher(trelloCard.getDesc());
//				if(m.find()){
//					area = Integer.parseInt(m.group(1));
//				}
//				Pattern p2 = Pattern.compile("Boundary:[ ]?(.*);");
//				Matcher m2 = p2.matcher(trelloCard.getDesc());
//				if(m2.find()){
//					Log.d("SyncController - addCard", "Found boundary");
//					Pattern p3 = Pattern.compile("[(]([-]?[0-9]+[.]?[0-9]*)[,]([-]?[0-9]+[.]?[0-9]*)[)]");
//					Matcher m3 = p3.matcher(m2.group(1));
//					while(m3.find()){
//						//Each coordinate
//						Log.d("SyncController - addCard", "Lat:" + m3.group(1));
//						Log.d("SyncController - addCard", "Lng:" + m3.group(2));
//						Double lat = Double.parseDouble(m3.group(1));
//						Double lng = Double.parseDouble(m3.group(2));
//						boundary.add(new LatLng(lat, lng));
//					}
//				}
//				String strBoundary = "";
//				if(boundary.size() > 0){
//					for(int i=0; i<boundary.size(); i++){
//						strBoundary = strBoundary + boundary.get(i).latitude + "," + boundary.get(i).longitude + ",";
//					}
//					//Add first point again
//					strBoundary = strBoundary + boundary.get(0).latitude + "," + boundary.get(0).longitude;
//				}
//				Log.d("SyncController - addCard", "String boundary:" + strBoundary);
//
//				//Add to fields
//				ContentValues values = new ContentValues();
//				values.put(TableFields.COL_NAME, trelloCard.getName());
//				values.put(TableFields.COL_ACRES, area);
//				values.put(TableFields.COL_BOUNDARY, strBoundary);
//				values.put(TableFields.COL_HAS_CHANGED, 0);
//				values.put(TableFields.COL_REMOTE_ID, trelloCard.getTrelloId().trim());
//				long id = database.insert(TableFields.TABLE_NAME, null, values);
//				dbHelper.close();
//				fieldsAdded.add((int) id);
//			} else if(trelloCard.getListId().contentEquals(operatorListId)) {
//				//Is in workers list, try to convert to worker
//				SQLiteDatabase database = dbHelper.getWritableDatabase();
//
//				//Add to workers
//				ContentValues values = new ContentValues();
//				values.put(TableWorkers.COL_NAME, trelloCard.getName());
//				values.put(TableWorkers.COL_HAS_CHANGED, 0);
//				values.put(TableWorkers.COL_REMOTE_ID, trelloCard.getTrelloId().trim());
//				database.insert(TableWorkers.TABLE_NAME, null, values);
//				dbHelper.close();
//			} else {
//				//Try to convert to job
//				SQLiteDatabase database = dbHelper.getWritableDatabase();
//
//				String strDate = "";
//				String name = "";
//				String worker = "";
//				String strStatus = "";
//				int status = Job.STATUS_PLANNED;
//
//				Pattern p = Pattern.compile("^(.+)[ ]([0-9]{1,2})[/]([0-9]{1,2})[/]([0-9]{2,4})[:][ ]?([^-]+)[-]?(.*)");
//				Matcher m = p.matcher(trelloCard.getName());
//				if(m.find()){
//					
//					strStatus = m.group(1);
//					
//					if(strStatus.contentEquals("Done")){
//						status = Job.STATUS_DONE;
//					} else if(strStatus.contentEquals("Started")){
//						status = Job.STATUS_STARTED;
//					} else if(strStatus.contentEquals("Planned")){
//						status = Job.STATUS_PLANNED;
//					} else {
//						convertionFailed = true;
//					}
//					if(convertionFailed == false){
//						int month = Integer.parseInt(m.group(2));
//						int day = Integer.parseInt(m.group(3));
//						int year = Integer.parseInt(m.group(4));
//						if(year < 2000){
//							year = year + 2000;
//						}
//						String strDateToParse = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);						
//						SimpleDateFormat dateFormaterLocal = new SimpleDateFormat("yyyy-M-d", Locale.US);
//						dateFormaterLocal.setTimeZone(TimeZone.getDefault());
//						Date d;
//						try {
//							d = dateFormaterLocal.parse(strDateToParse);
//						} catch (ParseException e) {
//							d = new Date(0);
//						}
//						
//						strDate = DatabaseHelper.dateToStringLocal(d);
//						name = m.group(5);
//						worker = m.group(6);
//					}
//				} else {
//					convertionFailed = true;
//				}
//				
//				String comments = "";
//				Pattern p2 = Pattern.compile("Comments:[ ]?(.*);");
//				Matcher m2 = p2.matcher(trelloCard.getDesc());
//				if(m2.find()){						
//					comments = m2.group(1);						
//				}
//				
//				//Find the operation id of the operation
//				String where = TableOperations.COL_REMOTE_ID + " = '" + trelloCard.getListId().trim() + "'";
//				Cursor cursor2 = database.query(TableOperations.TABLE_NAME, TableOperations.COLUMNS,
//						where, null, null, null, null);
//				Operation theOperation = null;
//				if (cursor2.moveToFirst()) {
//					theOperation = Operation.cursorToOperation(cursor2);
//				}
//				cursor2.close();
//				
//				/* TODO change to labels
//				int status = Job.STATUS_PLANNED;
//				if(trelloCard.getLabels().contains("green")){
//					status = Job.STATUS_DONE;
//				} else if(trelloCard.getLabels().contains("yellow")){
//					status = Job.STATUS_STARTED;
//				} else if(trelloCard.getLabels().contains("red")){
//					status = Job.STATUS_PLANNED;
//				}*/
//				
//				if(convertionFailed == false && theOperation != null){
//					//Add to jobs
//					ContentValues values = new ContentValues();
//					values.put(TableJobs.COL_FIELD_NAME, name);
//					values.put(TableJobs.COL_DATE_OF_OPERATION, strDate);
//					values.put(TableJobs.COL_WORKER_NAME, worker);
//					values.put(TableJobs.COL_STATUS, status);
//					values.put(TableJobs.COL_COMMENTS, comments);
//					values.put(TableJobs.COL_HAS_CHANGED, 0);
//					values.put(TableJobs.COL_REMOTE_ID, trelloCard.getTrelloId().trim());
//					values.put(TableJobs.COL_OPERATION_ID, theOperation.getId());
//					long id = database.insert(TableJobs.TABLE_NAME, null, values);
//					jobsAdded.add((int) id);
//				}
//				dbHelper.close();
//			}
//		}
//	}
//
//	@Override
//	public void setCardTrelloId(ICard localCard, String newId) {
//		//Update local trello id
//		SQLiteDatabase database = dbHelper.getWritableDatabase();
//		int localType = ((MyCard)localCard).getType();
//		if(localType == MyCard.typeField){
//			String where = TableFields.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId());
//			ContentValues values = new ContentValues();
//			values.put(TableFields.COL_REMOTE_ID, newId);
//			database.update(TableFields.TABLE_NAME, values, where, null);
//		} else if(localType == MyCard.typeWorker){
//			String where = TableWorkers.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId());
//			ContentValues values = new ContentValues();
//			values.put(TableWorkers.COL_REMOTE_ID, newId);
//			database.update(TableWorkers.TABLE_NAME, values, where, null);
//		} else if (localType == MyCard.typeJob){
//			String where = TableJobs.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId());
//			ContentValues values = new ContentValues();
//			values.put(TableJobs.COL_REMOTE_ID, newId);
//			database.update(TableJobs.TABLE_NAME, values, where, null);
//		}
//		dbHelper.close();
//	}
//	
//	@Override
//	public void setCardLocalChanges(ICard localCard, Boolean changes) {
//		//Update local has changed
//		SQLiteDatabase database = dbHelper.getWritableDatabase();
//		int localType = ((MyCard)localCard).getType();
//		Integer hasChanged = 0;
//		if(changes) hasChanged = 1;
//		if(localType == MyCard.typeField){
//			String where = TableFields.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId());
//			if(localCard.getClosed() == true && changes == false){
//				//Card now closed on trello, remove it from database, since it just has delete flag now
//				database.delete(TableFields.TABLE_NAME, where, null);
//			} else {
//				ContentValues values = new ContentValues();
//				values.put(TableFields.COL_HAS_CHANGED, hasChanged);
//				database.update(TableFields.TABLE_NAME, values, where, null);
//			}
//		} else if(localType == MyCard.typeWorker){
//			String where = TableWorkers.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId());
//			ContentValues values = new ContentValues();
//			values.put(TableWorkers.COL_HAS_CHANGED, hasChanged);
//			database.update(TableWorkers.TABLE_NAME, values, where, null);
//		} else if (localType == MyCard.typeJob){
//			String where = TableJobs.COL_ID + " = " + Integer.toString((Integer)localCard.getLocalId());
//			if(localCard.getClosed() == true && changes == false){
//				//Card now closed on trello, remove it from database, since it just has delete flag now
//				database.delete(TableJobs.TABLE_NAME, where, null);
//			} else {
//				ContentValues values = new ContentValues();
//				values.put(TableJobs.COL_HAS_CHANGED, hasChanged);
//				database.update(TableJobs.TABLE_NAME, values, where, null);
//			}
//		}
//		dbHelper.close();
//	}
//
//	@Override
//	public List<ICard> getLocalCards() {
//		//Now get all jobs, fields, and workers and convert them to MyCard's
//		List<ICard> cardList = new ArrayList<ICard>();
//		
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//		String boardId = prefs.getString("BoardId", "");
//		String fieldListId = prefs.getString("FieldList", "");
//		String operatorListId = prefs.getString("OperatorList", "");
//		
//		SQLiteDatabase database = dbHelper.getReadableDatabase();
//		//Jobs
//		Cursor cursor = database.query(TableJobs.TABLE_NAME, TableJobs.COLUMNS,
//				null, null, null, null, null);
//		while (cursor.moveToNext()) {
//			Job newJob = Job.cursorToJob(cursor);
//
//			//Find list trello id (trello id of operation)
//			String where = TableOperations.COL_ID + " = " + newJob.getOperationId();
//			Cursor cursor2 = database.query(TableOperations.TABLE_NAME, TableOperations.COLUMNS,
//					where, null, null, null, null);
//			Operation theOperation = null;
//			if (cursor2.moveToFirst()) {
//				theOperation = Operation.cursorToOperation(cursor2);
//			}
//			cursor2.close();
//			
//			String listId = theOperation.getRemote_id();
//			
//			SimpleDateFormat dateFormaterLocal = new SimpleDateFormat("MM/dd/yy", Locale.US);
//			dateFormaterLocal.setTimeZone(TimeZone.getDefault());
//			Date date = DatabaseHelper.stringToDateLocal(newJob.getDateOfOperation());
//			String displayDate = dateFormaterLocal.format(date);
//			
//			//TODO change this with labels
//			String strStatus = "";
//			if(newJob.getStatus() == Job.STATUS_DONE){
//				strStatus = "Done";
//			} else if(newJob.getStatus() == Job.STATUS_STARTED){
//				strStatus = "Started";
//			} else if(newJob.getStatus() == Job.STATUS_PLANNED){
//				strStatus = "Planned";
//			}
//			String name = strStatus + " " + displayDate + ": " + newJob.getFieldName();
//			if(newJob.getWorkerName().length() > 0){
//				name = name + "-" + newJob.getWorkerName();
//			}
//			String desc = "Comments: " + newJob.getComments() + ";"; //QUESTION ???? Needs to handle extra things that we didn't put in ourselves
//			Date dateChanged;
//			dateChanged = DatabaseHelper.stringToDateUTC(newJob.getDateChanged());
//			
//			Boolean hasChanged = false;
//			if(newJob.getHasChanged() == 1) hasChanged = true;
//			Boolean closed = false;
//			if(newJob.getDeleted() == 1){
//				closed = true;
//				Log.d("Closed:", newJob.getFieldName());
//			}
//			MyCard newCard = new MyCard(MyCard.typeJob, newJob.getRemote_id(), newJob.getId(), closed, hasChanged, listId, boardId, name, desc, dateChanged);
//			//TODO newCard.setStatus(newJob.getStatus());
//			cardList.add(newCard);
//		}
//		cursor.close();
//		
//		//Fields
//		cursor = database.query(TableFields.TABLE_NAME, TableFields.COLUMNS,
//				null, null, null, null, null);
//		while (cursor.moveToNext()) {
//			Field newField = Field.cursorToField(cursor);
//			//Generate description
//			String desc = "Area: " + Integer.toString(newField.getAcres()) + " ac;"; //QUESTION ???? Needs to handle extra things that we didn't put in ourselves
//			List<LatLng> points = newField.getBoundary();
//			if(points != null){
//				desc = desc + "\nBoundary: ";
//				for(int i=0; i<(points.size()-1); i++){
//					desc = desc + "(" + Double.toString(points.get(i).latitude) + "," + Double.toString(points.get(i).longitude) + "),";
//				}
//				desc = desc.substring(0, (desc.length()-1));
//				desc = desc + ";";
//			}
//			Date dateChanged;
//			dateChanged = DatabaseHelper.stringToDateUTC(newField.getDateChanged());
//			Boolean hasChanged = false;
//			if(newField.getHasChanged() == 1) hasChanged = true;
//			Boolean closed = false;
//			if(newField.getDeleted() == 1){
//				closed = true;
//				Log.d("Closed:", newField.getName());
//			}
//			MyCard newCard = new MyCard(MyCard.typeField, newField.getRemote_id(), newField.getId(), closed, hasChanged, fieldListId, boardId, newField.getName(), desc, dateChanged);
//			cardList.add(newCard);
//		}
//		cursor.close();
//		
//		//Workers
//		cursor = database.query(TableWorkers.TABLE_NAME, TableWorkers.COLUMNS,
//				null, null, null, null, null);
//		while (cursor.moveToNext()) {
//			Worker newWorker = Worker.cursorToWorker(cursor);
//			//Generate description
//			String desc = null;
//			Date dateChanged;
//			dateChanged = DatabaseHelper.stringToDateUTC(newWorker.getDateChanged());
//			Boolean hasChanged = false;
//			if(newWorker.getHasChanged() == 1) hasChanged = true;
//			
//			MyCard newCard = new MyCard(MyCard.typeWorker, newWorker.getRemote_id(), newWorker.getId(), false, hasChanged, operatorListId, boardId, newWorker.getName(), desc, dateChanged);
//			cardList.add(newCard);
//		}
//		cursor.close();
//		dbHelper.close();
//	
//		return cardList;
//	}
//
//	@Override
//	public List<IList> getLocalLists() {
//		//Now get all operations and 2 static settings lists and convert them to MyList's
//		List<IList> listList = new ArrayList<IList>();
//		
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//		String boardId = prefs.getString("BoardId", "");
//		String fieldsListId = prefs.getString("FieldList", "");
//		String operatorListId = prefs.getString("OperatorList", "");
//		
//		//Operations
//		SQLiteDatabase database = dbHelper.getReadableDatabase();
//		//Jobs
//		Cursor cursor = database.query(TableOperations.TABLE_NAME, TableOperations.COLUMNS,
//				null, null, null, null, null);
//		while (cursor.moveToNext()) {
//			Operation newOperation = Operation.cursorToOperation(cursor);
//			
//			Boolean hasChanged = false;
//			if(newOperation.getHasChanged() == 1) hasChanged = true;
//			
//			if(newOperation.getRemote_id() == null){
//				Log.d("Problem***********", "Problem***********");
//			}
//			
//			MyList newList = new MyList(newOperation.getRemote_id(), newOperation.getId(), boardId, newOperation.getName(), false, hasChanged);
//			listList.add(newList);
//		}
//		
//		MyList pickedList = new MyList(fieldsListId, 0, boardId, "Settings - Field List", false, false);
//		MyList notPickedList = new MyList(operatorListId, 0, boardId, "Settings - Operator List", false, false);
//			
//		listList.add(pickedList);
//		listList.add(notPickedList);
//		
//		return listList;
//	}
//
//	@Override
//	public List<IBoard> getLocalBoards() {
//		//Get static board and convert it to a MyBoard
//		List<IBoard> boardList = new ArrayList<IBoard>();
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppContext);
//		String boardId = prefs.getString("BoardId", "");
//		MyBoard board = new MyBoard(boardId, "", "OpenATK - Field Work App", "", false, false);
//		boardList.add(board);
//		
//		return boardList;
//	}
//
//	@Override
//	public void finishedSync() {
//		Log.d("SyncController", "Finished syncing");
//		//Do stuff on main thread
//		
//		//Updated
//		for(int i=0; i<fieldsChanged.size(); i++){
//			listener.SyncControllerUpdateField(fieldsChanged.get(i));
//		}
//		fieldsChanged.clear();
//		for(int i=0; i<jobsChanged.size(); i++){
//			listener.SyncControllerUpdateJob(jobsChanged.get(i));
//		}
//		jobsChanged.clear();
//		
//		//Deleted
//		for(int i=0; i<fieldsDeleted.size(); i++){
//			listener.SyncControllerDeleteField(fieldsDeleted.get(i));
//		}
//		fieldsDeleted.clear();
//		for(int i=0; i<jobsDeleted.size(); i++){
//			listener.SyncControllerDeleteJob(jobsDeleted.get(i));
//		}
//		jobsDeleted.clear();
//		
//		//Added
//		for(int i=0; i<fieldsAdded.size(); i++){
//			listener.SyncControllerAddField(fieldsAdded.get(i));
//		}
//		fieldsAdded.clear();
//		for(int i=0; i<jobsAdded.size(); i++){
//			listener.SyncControllerAddJob(jobsAdded.get(i));
//		}
//		jobsAdded.clear();
//				
//		if(updateOperations){
//			updateOperations = false;
//			listener.SyncControllerRefreshOperations();
//		}
//		
//		if(changeOrganizations){
//			listener.SyncControllerChangeOrganizations();
//		}
//	}
}
