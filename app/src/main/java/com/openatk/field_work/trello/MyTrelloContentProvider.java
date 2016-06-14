package com.openatk.field_work.trello;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import com.openatk.field_work.MainActivity;
import com.openatk.field_work.db.DatabaseHelper;
import com.openatk.field_work.db.TableFields;
import com.openatk.field_work.db.TableJobs;
import com.openatk.field_work.db.TableOperations;
import com.openatk.field_work.db.TableWorkers;
import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Job;
import com.openatk.field_work.models.Operation;
import com.openatk.field_work.models.Worker;
import com.openatk.libtrello.TrelloBoard;
import com.openatk.libtrello.TrelloCard;
import com.openatk.libtrello.TrelloContentProvider;
import com.openatk.libtrello.TrelloList;

public class MyTrelloContentProvider extends TrelloContentProvider {
	
	private DatabaseHelper dbHelper;
	private TrelloHelper trelloHelper;
	
	public MyTrelloContentProvider(){
		
	}

	//Custom implemented in every app
	@Override
	public List<TrelloCard> getCards(String boardTrelloId){
		dbHelper = new DatabaseHelper(getContext());
		trelloHelper = new TrelloHelper(getContext());

		Log.d("MyTrelloContentProvider", "getCards()");
		//Return all custom data as cards
		List<TrelloCard> cards = new ArrayList<TrelloCard>();
		
		//Get all operators, set list id equal to "Settings - Operator List" trello id
		List<Worker> workers = DatabaseHelper.readWorkers(dbHelper);
		for(int i=0; i<workers.size(); i++){
			cards.add(trelloHelper.toTrelloCard(workers.get(i)));
		}
		Log.d("MyTrelloContentProvider - getCards", "# Workers:" + Integer.toString(workers.size()));

		//Get all fields, set list id equal to "Settings - Field List" trello id
		List<Field> fields = DatabaseHelper.readFields(dbHelper);
		for(int i=0; i<fields.size(); i++){
			cards.add(trelloHelper.toTrelloCard(fields.get(i)));
		}
		Log.d("MyTrelloContentProvider - getCards", "# Fields:" + Integer.toString(fields.size()));

		
		//Get all jobs, set list id equal to operation trello id
		List<Operation> operations = DatabaseHelper.readOperations(dbHelper);
		//Create hashmap
		SparseArray<String> map = new SparseArray<String>(operations.size());
		for(int i=0; i<operations.size(); i++){
			map.put(operations.get(i).getId(), operations.get(i).getRemote_id());
		}
		
		List<Job> jobs = DatabaseHelper.readJobs(dbHelper);
		for(int i=0; i<jobs.size(); i++){
			cards.add(trelloHelper.toTrelloCard(jobs.get(i), map.get(jobs.get(i).getOperationId())));
		}
		Log.d("MyTrelloContentProvider - getCards", "# Jobs:" + Integer.toString(jobs.size()));

		
		Log.d("MyTrelloContentProvider - getCards", "# Cards:" + Integer.toString(cards.size()));
		dbHelper.close();
		
		return cards;
	}
	
	@Override
	public TrelloCard getCard(String id){
		//TODO I think this is unused by current version of Trello
		return null;
	}
	
	//Custom implemented in every app
	@Override
	public List<TrelloList> getLists(String boardTrelloId){
		dbHelper = new DatabaseHelper(getContext());
		trelloHelper = new TrelloHelper(getContext());

		Log.d("MyTrelloContentProvider", "getLists()");
		//Return all custom data as TrelloLists
		//RockApp has 2 default lists, and a list for each operation
		List<TrelloList> lists = new ArrayList<TrelloList>();
		
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		
		TrelloList listWorkers = new TrelloList();
		if(prefs.contains("listWorkersLocalId")){
			listWorkers.setLocalId(prefs.getString("listWorkersLocalId", "Fields"));
			listWorkers.setId(prefs.getString("listWorkersTrelloId", ""));
			listWorkers.setName(prefs.getString("listWorkersName", ""));
			listWorkers.setName_changed(TrelloContentProvider.stringToDate(prefs.getString("listWorkersName_change", "")));
			listWorkers.setClosed(false);
			listWorkers.setClosed_changed(TrelloContentProvider.stringToDate(prefs.getString("listWorkersClosed_change", "")));
			listWorkers.setBoardId(prefs.getString("boardTrelloId", ""));
		} else {			
			Date theDate = new Date();		//TODO from Internet	
			String workerLocalId = "workers";
			String workerTrelloId = "";
			String workerName = "Settings - Operator List";
			String changeDate = TrelloContentProvider.dateToUnixString(theDate);
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("listWorkersLocalId", workerLocalId);
			editor.putString("listWorkersTrelloId", workerTrelloId);
			editor.putString("listWorkersName", workerName);
			editor.putString("listWorkersName_change", changeDate);
			editor.putString("listWorkersClosed_change", changeDate);
			editor.commit();
			
			listWorkers.setLocalId(workerLocalId);
			listWorkers.setId(workerTrelloId);
			listWorkers.setName(workerName);
			listWorkers.setName_changed(theDate);
			listWorkers.setClosed(false);
			listWorkers.setClosed_changed(theDate);
			listWorkers.setBoardId(prefs.getString("boardTrelloId", ""));
		}
		lists.add(listWorkers);
		
		
		TrelloList listFields = new TrelloList();
		if(prefs.contains("listFieldsLocalId")){			
			listFields.setLocalId(prefs.getString("listFieldsLocalId", "0"));
			listFields.setId(prefs.getString("listFieldsTrelloId", ""));
			listFields.setName(prefs.getString("listFieldsName", ""));
			listFields.setName_changed(TrelloContentProvider.stringToDate(prefs.getString("listFieldsName_change", "")));
			listFields.setClosed_changed(TrelloContentProvider.stringToDate(prefs.getString("listFieldsClosed_change", "")));
			listFields.setBoardId(prefs.getString("boardTrelloId", ""));
		} else {			
			Date theDate = new Date();	//TODO from Internet
			
			String dateChange = TrelloContentProvider.dateToUnixString(theDate);
			String fieldLocalId = "fields";
			String fieldTrelloId = "";
			String fieldName = "Settings - Field List";
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("listFieldsLocalId", fieldLocalId);
			editor.putString("listFieldsTrelloId", fieldTrelloId);
			editor.putString("listFieldsName", fieldName);
			editor.putString("listFieldsName_change", dateChange);
			editor.putString("listFieldsClosed_change", dateChange);
			editor.commit();
			
			listFields.setLocalId(fieldLocalId);
			listFields.setId(fieldTrelloId);
			listFields.setName(fieldName);
			listFields.setName_changed(theDate);
			listFields.setClosed(false);
			listFields.setClosed_changed(theDate);
			listFields.setBoardId(prefs.getString("boardTrelloId", ""));
		}			
		lists.add(listFields);
		
		
		//Add a list for each operation
		List<Operation> operations = DatabaseHelper.readOperations(dbHelper);
		for(int i=0; i<operations.size(); i++){
			lists.add(this.trelloHelper.toTrelloList(operations.get(i)));
		}
		
		return lists;
	}
	
	//Custom implemented in every app
	@Override
	public List<TrelloBoard> getBoards(){
		Log.d("MyTrelloContentProvider", "getBoards()");
		//Return all custom data as boards, always return boards
		//FieldWork has 1 board
		List<TrelloBoard> boards = new ArrayList<TrelloBoard>();
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		TrelloBoard trelloBoard = new TrelloBoard();
		if(prefs.contains("boardLocalId")){
			Log.d("MyTrelloContentProvider", "Has local id, giving current local board");
			Log.d("MyTrelloContentProvider", "Its trelloId:" + prefs.getString("boardTrelloId", ""));

			trelloBoard.setLocalId(prefs.getString("boardLocalId", "0"));
			trelloBoard.setId(prefs.getString("boardTrelloId", ""));
			trelloBoard.setName(prefs.getString("boardName", ""));
			trelloBoard.setName_changed(TrelloContentProvider.stringToDate(prefs.getString("boardName_change", "")));
			trelloBoard.setDesc(prefs.getString("boardDesc", ""));
			trelloBoard.setDesc_changed(TrelloContentProvider.stringToDate(prefs.getString("boardDesc_change", "")));
			trelloBoard.setClosed(false);
			trelloBoard.setClosed_changed(TrelloContentProvider.stringToDate(prefs.getString("boardClosed_change", "")));
			trelloBoard.setOrganizationId(prefs.getString("boardOrganizationId", ""));
			trelloBoard.setOrganizationId_changed(TrelloContentProvider.stringToDate(prefs.getString("boardOrganizationId_change", "")));
			
			trelloBoard.setLastSyncDate(prefs.getString("boardSyncDate", ""));
			trelloBoard.setLastTrelloActionDate(prefs.getString("boardTrelloActionDate", ""));
		} else {		
			Log.d("MyTrelloContentProvider", "No local id, creating new local board");
			Date theDate = new Date();			
			String localId = "0";
			String trelloId = "";
			String name = "OpenATK - Field Work App";
			String dateChange = TrelloContentProvider.dateToUnixString(theDate);
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("boardLocalId", localId);
			editor.putString("boardTrelloId", trelloId);
			editor.putString("boardName", name);
			editor.putString("boardName_change", dateChange);
			editor.putString("boardDesc", "");
			editor.putString("boardDesc_change", dateChange);
			editor.putString("boardClosed_change", dateChange);
			editor.putString("boardOrganizationId", ""); //We don't know the organization id till it's on trello
			editor.putString("boardOrganizationId_change", dateChange);
			editor.putString("boardSyncDate", TrelloContentProvider.dateToUnixString(new Date(0)));
			editor.putString("boardTrelloActionDate", TrelloContentProvider.dateToUnixString(new Date(0)));
			editor.commit();
			trelloBoard.setLocalId(localId);
			trelloBoard.setId(trelloId);
			trelloBoard.setName(name);
			trelloBoard.setName_changed(theDate);
		}
		boards.add(trelloBoard);
		
		return boards;
	}
	
	@Override
	public int updateCard(TrelloCard tcard){
		dbHelper = new DatabaseHelper(getContext());
		trelloHelper = new TrelloHelper(getContext());

		//Update a card
		Log.d("MyTrelloContentProvider - updateCard", "updating card local id:" + tcard.getSource().getLocalId());
		Log.d("MyTrelloContentProvider - updateCard", "updating card trello id:" + tcard.getId());
		
		//First check list id of source to see what type it used to be
		int typeField = 0;
		int typeWorker = 1;
		int typeJob = 2;
		int oldType = 0;
		
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		String listWorkersTrelloId = prefs.getString("listWorkersTrelloId", "");
		String listFieldsTrelloId = prefs.getString("listFieldsTrelloId", "");
		
		if(tcard.getSource().getListId() != null){
			if(tcard.getSource().getListId().contentEquals(listWorkersTrelloId)){
				Log.d("MyTrelloContentProvider getType", "was a worker");
				oldType = typeWorker;
			} else if(tcard.getSource().getListId().contentEquals(listFieldsTrelloId)){
				Log.d("MyTrelloContentProvider getType", "was a field");
				oldType = typeField;
			} else {
				Log.d("MyTrelloContentProvider getType", "was a job");
				oldType = typeJob;
			}
		}
		
		//Now see what type it is now
		int newType = 0;
		if(tcard.getListId() != null){
			//Possible type change
			if(tcard.getListId().contentEquals(listWorkersTrelloId)){
				Log.d("MyTrelloContentProvider getType", "is a worker");
				newType = typeWorker;
			} else if(tcard.getListId().contentEquals(listFieldsTrelloId)){
				Log.d("MyTrelloContentProvider getType", "is a field");
				newType = typeField;
			} else {
				Log.d("MyTrelloContentProvider getType", "is a job");
				newType = typeJob;
			}
			Log.w("MyTrelloContentProvider", "Update card, Card has a list id");
		} else {
			//No listid, then it didn't change
			newType = oldType;
		}
		
		//Was a worker
		if(oldType == typeWorker){
			if(newType != typeWorker){
				//This card used to be a worker but now isn't, delete it
				Worker worker = trelloHelper.toWorker(tcard.getSource());
				TableWorkers.deleteWorker(dbHelper, worker);
				Intent toSend = new Intent(MainActivity.INTENT_WORKER_DELETED);
				toSend.putExtra("id", worker.getId());
				toSend.putExtra("workerName", worker.getName());
				LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
			} else {
				//Same type so we know the local id
				tcard.setLocalId(tcard.getSource().getLocalId());
			}
		}
		//Was a field
		if(oldType == typeField){
			if(newType != typeField){
				//This card used to be a worker but now isn't, delete it
				Field field = trelloHelper.toField(tcard.getSource());
				TableFields.deleteField(dbHelper, field);
				Intent toSend = new Intent(MainActivity.INTENT_FIELD_DELETED);
				toSend.putExtra("id", field.getId());
				LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
			} else {
				//Same type so we know the local id
				tcard.setLocalId(tcard.getSource().getLocalId());
			}
		}
		//Was a job
		if(oldType == typeJob){
			if(newType != typeJob){
				//This card used to be a worker but now isn't, delete it
				Job job = trelloHelper.toJob(tcard.getSource());
				TableJobs.deleteJob(dbHelper, job);
				Intent toSend = new Intent(MainActivity.INTENT_JOB_DELETED);
				toSend.putExtra("id", job.getId());
				toSend.putExtra("fieldName", job.getFieldName());
				Log.d("FieldName", "Fieldname:" + job.getFieldName());
				LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
			} else {
				//Same type so we know the local id
				tcard.setLocalId(tcard.getSource().getLocalId());
			}
		}
		
		//Is a worker
		if(newType == typeWorker){
			//Try to convert new card to worker
			Worker worker = trelloHelper.toWorker(tcard);
			if(worker != null){
				if(worker.getDeleted() != null && worker.getDeleted()){
					if(newType == oldType){
						//Worker is now deleted, remove it
						Worker oldWorker = trelloHelper.toWorker(tcard.getSource());
						TableWorkers.deleteWorker(dbHelper, worker);
						Intent toSend = new Intent(MainActivity.INTENT_WORKER_DELETED);
						toSend.putExtra("id", worker.getId());
						toSend.putExtra("workerName", oldWorker.getName());
						LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
					}
				} else {
					//Update in the database accordingly
					Log.d("MyTrelloContentProvider", "update worker");
					Boolean updated = TableWorkers.updateWorker(dbHelper, worker);
					if(updated){
						Intent toSend = new Intent(MainActivity.INTENT_WORKER_UPDATED);
						toSend.putExtra("id", worker.getId());
						//No need to pass name since its an update and not a delete
						LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
					}
				}
			}
		}
		
		//Is a field
		if(newType == typeField){
			//Try to convert new card to field
			Field field = trelloHelper.toField(tcard);
			if(field != null){
				if(field.getDeleted() != null && field.getDeleted()){
					if(newType == oldType){
						//field is now deleted, remove it
						TableFields.deleteField(dbHelper, field);
						Intent toSend = new Intent(MainActivity.INTENT_FIELD_DELETED);
						toSend.putExtra("id", field.getId());
						LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
					}
				} else {
					//Update in the database accordingly
					Log.d("MyTrelloContentProvider", "update field");
					Boolean updated = TableFields.updateField(dbHelper, field);
					if(updated){
						Intent toSend = new Intent(MainActivity.INTENT_FIELD_UPDATED);
						toSend.putExtra("id", field.getId());
						LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
					}
				}
			}
		}
		
		//Is a job
		if(newType == typeJob){
			//Try to convert new card to job
			Job job = trelloHelper.toJob(tcard);
			if(job != null){
				if(job.getDeleted() != null && job.getDeleted() == true){
					if(newType == oldType){
						Job oldJob = trelloHelper.toJob(tcard.getSource());
						//Worker is now deleted, remove it
						TableJobs.deleteJob(dbHelper, job);
						Intent toSend = new Intent(MainActivity.INTENT_JOB_DELETED);
						toSend.putExtra("fieldName", oldJob.getFieldName());
						toSend.putExtra("id", job.getId());
						LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
					}
				} else {
					//Update in the database accordingly
					//Get operation if operation changed, this isn't handled in toJob TODO?...
					Integer oldOperationId = -1;
					if(tcard.getListId() != null){
						Operation operation = TableOperations.FindOperationByTrelloId(dbHelper, tcard.getListId());
						if(operation != null){
							job.setOperationId(operation.getId());
							job.setDateOperationIdChanged(tcard.getListId_changed());
							//Save old operation id to pass to mainactivity to update field views
							oldOperationId = TableJobs.FindJobById(dbHelper, job.getId()).getOperationId();
						} else {
							//If operation is null it's not on a operation list, should never happen
							Log.w("MyTrellContentProvider - updateCard", "Job doesn't have a valid operation id");
							Log.w("MyTrellContentProvider - updateCard", "Job listid:" + tcard.getListId());
						}
					}
					Log.d("MyTrelloContentProvider", "update job");
					Boolean updated = TableJobs.updateJob(dbHelper, job);
					if(updated){
						Intent toSend = new Intent(MainActivity.INTENT_JOB_UPDATED);

						//If field name changed we need to update old field
						if(job.getFieldName() != null) {
							//Field name changed, we need to pass this so we can update the status of the old field
							String oldFieldName = trelloHelper.toJob(tcard.getSource()).getFieldName();
							if(oldFieldName != null) toSend.putExtra("oldFieldName", oldFieldName);
						}
						//If operation id changed we need to update the field status
						if(job.getOperationId() != null) {
							//Field name changed, we need to pass this so we can update the status of the old field
							if(oldOperationId != null && oldOperationId != job.getOperationId()){
								toSend.putExtra("oldOperationId", oldOperationId);
							} else {
								Log.d("MyTrelloContent", "Old operation id null");
							}
						} else {
							Log.d("MyTrelloContent", "new operation id null");
						}
						
						toSend.putExtra("id", job.getId());
						LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
					}
				}
			}
		}
		
		//Notify the activity TODO, switch to job by job or whatever
		//LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_JOBS_UPDATED));
		return 1;
	}
	
	@Override
	public int updateList(TrelloList tlist){
		dbHelper = new DatabaseHelper(getContext());
		trelloHelper = new TrelloHelper(getContext());

		Log.d("MyTrelloContentProvider", "updateList()");
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		String listWorkersTrelloId = prefs.getString("listWorkersTrelloId", "");
		String listFieldsTrelloId = prefs.getString("listFieldsTrelloId", "");
		String listWorkersId = prefs.getString("listWorkersLocalId", "");
		String listFieldsId = prefs.getString("listFieldsLocalId", "");
		
		String boardId = prefs.getString("boardTrelloId", "");
		
		//Update our 2 static lists, notice these id's are different than operations
		Boolean isFieldList = false;
		Boolean isWorkerList = false;
		if(tlist.getSource() != null){
			if(tlist.getSource().getLocalId().contentEquals(listFieldsId)){
				isFieldList = true;
			} else if(tlist.getSource().getLocalId().contentEquals(listWorkersId)){
				isWorkerList = true;
			}
		}
		
		if(tlist.getId() != null){
			if(tlist.getId().contentEquals(listFieldsTrelloId)){
				isFieldList = true;
			} else if(tlist.getId().contentEquals(listWorkersTrelloId)){
				isWorkerList = true;
			}
		}
		
		if(isFieldList){
			//Update field list accordingly
			SharedPreferences.Editor editor = prefs.edit();

			boolean deleted = false;
			if(tlist.getClosed() != null && tlist.getClosed()) deleted = true;
			if(tlist.getBoardId() != null && tlist.getBoardId().contentEquals(boardId) == false) deleted = true;
			if(tlist.getId() != null) editor.putString("listFieldsTrelloId", tlist.getId());
			if(tlist.getName() != null && tlist.getName().contentEquals(prefs.getString("listFieldsName", "")) == false) deleted = true;
			
			if(deleted){
				 //Remove this so fieldlist will be recreated
				editor.remove("listFieldsLocalId");
				//Delete all the fields in the db
				TableFields.deleteAll(dbHelper);
				Intent toSend = new Intent(MainActivity.INTENT_ALL_FIELDS_DELETED);
				LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
			}
			editor.commit();
		}
		
		if(isWorkerList){
			//Update worker list accordingly
			SharedPreferences.Editor editor = prefs.edit();

			boolean deleted = false;
			if(tlist.getClosed() != null && tlist.getClosed()) deleted = true;
			if(tlist.getBoardId() != null && tlist.getBoardId().contentEquals(boardId) == false) deleted = true;
			if(tlist.getId() != null) editor.putString("listWorkersTrelloId", tlist.getId());
			if(tlist.getName() != null && tlist.getName().contentEquals(prefs.getString("listWorkersName", "")) == false) deleted = true;
			
			if(deleted){
				 //Remove this so fieldlist will be recreated
				editor.remove("listWorkersLocalId");
				//Delete all the workers in the db
				TableWorkers.deleteAll(dbHelper);
				Intent toSend = new Intent(MainActivity.INTENT_ALL_WORKERS_DELETED);
				LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
			}
			editor.commit();
		}
		
		if(isWorkerList == false && isFieldList == false){
			//Update the other lists (Operations), if it is one of them, just call update if its not it will handle it
			//Update necessary info
			Operation toUpdate = new Operation();
			Boolean deleted = false;
			
			toUpdate.setId(Integer.parseInt(tlist.getSource().getLocalId()));
			if(tlist.getClosed() != null) deleted = tlist.getClosed();
			if(tlist.getId() != null) toUpdate.setRemote_id(tlist.getId());
			if(tlist.getName() != null) toUpdate.setName(tlist.getName());
			if(tlist.getName_changed() != null) toUpdate.setDateNameChanged(tlist.getName_changed());
			if(tlist.getBoardId() != null && tlist.getBoardId().contentEquals(boardId) == false) deleted = true;
			
			if(deleted != null && deleted == true){
				//Delete all jobs with this operation
				TableJobs.deleteAllWithOperationId(dbHelper, toUpdate.getId());
				//Delete the operation
				TableOperations.deleteOperation(dbHelper, toUpdate);
				Intent toSend = new Intent(MainActivity.INTENT_OPERATION_DELETED);
				toSend.putExtra("id", toUpdate.getId());
				LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
			} else {
				TableOperations.updateOperation(dbHelper, toUpdate);
				Intent toSend = new Intent(MainActivity.INTENT_OPERATION_UPDATED);
				toSend.putExtra("id", toUpdate.getId());
				LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
			}
		}
		return 1;
	}
	
	
	@Override
	public int updateOrganization(String oldOrganizationId, String newOrganizationId){
		dbHelper = new DatabaseHelper(getContext());
		trelloHelper = new TrelloHelper(getContext());

		Log.d("MyTrelloContentProvider", "updateOrganization()");
		Log.d("MyTrelloContentProvider", "updateOrganization old:" + oldOrganizationId + " new:" + newOrganizationId);
		//Organization has changed, delete everything
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = prefs.edit();
		//Delete boards
		editor.remove("boardLocalId"); //Will cause it to be remade
		
		//Delete lists (Fields, Workers, Operations)
		editor.remove("listFieldsLocalId"); //Will cause it to be remade
		editor.remove("listWorkersLocalId"); //Will cause it to be remade
		editor.commit();
		TableOperations.deleteAll(dbHelper);

		//Delete cards (Workers, Fields, and Jobs)
		TableWorkers.deleteAll(dbHelper);
		TableFields.deleteAll(dbHelper);
		TableJobs.deleteAll(dbHelper);
		
		
		if(prefs.getInt(MainActivity.PREF_GONE, MainActivity.PREF_GONE_NO_UPDATE) == MainActivity.PREF_GONE_NO_UPDATE){
			//MainActivity is away, tell it that there has been an update
			editor = prefs.edit();
			editor.putInt(MainActivity.PREF_GONE, MainActivity.PREF_GONE_UPDATE);
			editor.commit();
			Log.d("MyTreloContentProvider", "MainActivity is gone, set update.");
		} else {
			Log.d("MyTreloContentProvider", "MainActivity is present.");
		}
		//Tell MainActivity if it is around that this is an update
		LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_EVERYTHING_DELETED));
		return 0;
	}
	
	@Override
	public int updateBoard(TrelloBoard tBoard){
		Log.d("MyTrelloContentProvider", "updateBoard()");
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		Boolean isIt = false;
		if(tBoard.getSource() != null){
			if(tBoard.getSource().getLocalId().contentEquals(prefs.getString("boardLocalId", "something"))){
				isIt = true;
			}
		} else if(tBoard.getId() != null){
			if(tBoard.getId().contentEquals(prefs.getString("boardTrelloId", "something"))){
				isIt = true;
			}
		}
		if(isIt){
			Boolean delete = false;
			SharedPreferences.Editor editor = prefs.edit();
			if(tBoard.getId() != null && tBoard.getId().contentEquals(prefs.getString("boardTrelloId", "something")) == false){
				editor.putString("boardTrelloId", tBoard.getId());
				Log.d("MyTrelloContentProvider", "updateBoard new trello id:" + tBoard.getId());
			}
			//Name
			if(tBoard.getName() != null) {
				if(tBoard.getName().contentEquals(prefs.getString("boardName", "")) == false){
					delete = true;
				}
				editor.putString("boardName_change", TrelloContentProvider.dateToUnixString(tBoard.getName_changed()));
			}
			//Desc
			if(tBoard.getDesc() != null){
				editor.putString("boardDesc", tBoard.getDesc());
				editor.putString("boardDesc_change", TrelloContentProvider.dateToUnixString(tBoard.getDesc_changed()));
			}
			//Closed
			if(tBoard.getClosed() != null){
				if(tBoard.getClosed() == true){
					delete = true;
				}
				editor.putString("boardClosed_change", TrelloContentProvider.dateToUnixString(tBoard.getClosed_changed()));
			}
			//Organization Id
			if(tBoard.getOrganizationId() != null){
				if(prefs.getString("boardOrganizationId", "").length() == 0){
					//Just added the board on trello, its sending back trello id and organization id 
					editor.putString("boardOrganizationId", tBoard.getOrganizationId());
					editor.putString("boardOrganizationId_change", TrelloContentProvider.dateToUnixString(tBoard.getOrganizationId_change()));
				} else if(tBoard.getOrganizationId().contentEquals(prefs.getString("boardOrganizationId", "")) == false){
					//Means it's organization id changed... delete the board
					delete = true;
				}
			}
			//LastSync
			if(tBoard.getLastSyncDate() != null){
				editor.putString("boardSyncDate", tBoard.getLastSyncDate());
			}
			//LastTrelloAction
			if(tBoard.getLastTrelloActionDate() != null){			
				editor.putString("boardTrelloActionDate", tBoard.getLastTrelloActionDate());
			}
			//TODO labels, etc, don't need for this i guess but could add...
			
			if(delete){
				Log.d("MyTrelloContentProvider", "updateBoard deleting it");
				//Delete everything
				//Delete boards
				editor.remove("boardLocalId"); //Will cause it to be remade
				
				//Delete lists (Fields, Workers, Operations)
				editor.remove("listFieldsLocalId"); //Will cause it to be remade
				editor.remove("listWorkersLocalId"); //Will cause it to be remade
				TableOperations.deleteAll(dbHelper);

				//Delete cards (Workers, Fields, and Jobs)
				TableWorkers.deleteAll(dbHelper);
				TableFields.deleteAll(dbHelper);
				TableJobs.deleteAll(dbHelper);
			}
			editor.commit();
			
			//LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_ROCKS_UPDATED));
		} else {
			return 0;
		}
		return 1;
	}
	
	@Override
	public void insertCard(TrelloCard tcard){
		dbHelper = new DatabaseHelper(getContext());
		trelloHelper = new TrelloHelper(getContext());

		Log.d("MyTrelloContentProvider","insertCard()");
		Log.d("MyTrelloContentProvider","insert Card name: " + tcard.getName());
		Log.d("MyTrelloContentProvider","insert Card desc: " + tcard.getDesc());

		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		String listWorkersTrelloId = prefs.getString("listWorkersTrelloId", "");
		String listFieldsTrelloId = prefs.getString("listFieldsTrelloId", "");
		String listWorkersId = prefs.getString("listWorkersLocalId", "");
		String listFieldsId = prefs.getString("listFieldsLocalId", "");
		String boardId = prefs.getString("boardTrelloId", "");
		
		//Figure out if its a Field, Worker, or Job
		//First check list id to see what type it is
		if(tcard.getListId() == null || tcard.getListId().length() == 0){
			Log.w("MyTrelloContentProvider - insertCard", "Card does not have a trello list id."); //Should never happen
		} else {
			if(tcard.getListId().contentEquals(listWorkersTrelloId)){
				//This card is in the worker list
				//Try to convert to worker
				Worker worker = trelloHelper.toWorker(tcard);
				if(worker == null){
					//Card is no longer a valid worker, delete it
					//This can't happen, all cards are valid workers
					Log.w("MyTrelloContentProvider - insertCard", "Invalid worker.");
				} else {
					if(worker.getDeleted() == null || worker.getDeleted() == false){
						//Insert in the database accordingly
						Log.d("MyTrelloContentProvider - insertCard", "Inserted as worker");
						TableWorkers.updateWorker(dbHelper, worker);
						
						Intent toSend = new Intent(MainActivity.INTENT_WORKER_UPDATED);
						toSend.putExtra("id", worker.getId()); //This is set in updateWorker()
						LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
					}
				}
			} else if(tcard.getListId().contentEquals(listFieldsTrelloId)){
				//This card is in the field list
				//Try to convert it to a field
				Field field = trelloHelper.toField(tcard);
				if(field == null) {
					//Card is not a valid field, don't insert it
					Log.w("MyTrelloContentProvider - insertCard", "Invalid field.");
				} else {
					if(field.getDeleted() == null || field.getDeleted() == false){
						//Insert field in database accordingly
						Log.d("MyTrelloContentProvider - insertCard", "Inserted as field");
						TableFields.updateField(dbHelper, field);
						
						Intent toSend = new Intent(MainActivity.INTENT_FIELD_UPDATED);
						toSend.putExtra("id", field.getId()); //This is set in updateField()
						toSend.putExtra("insert", true); //So we know to look for a job on this field.
						LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
					}
				}
			} else {
				//This card is in another list
				//Try to convert it to a job
				Job job = trelloHelper.toJob(tcard);
				if(job == null){
					//Card is not a valid job, don't insert it
					Log.w("MyTrelloContentProvider - insertCard", "Invalid job.");
				} else {
					if(job.getDeleted() == null || job.getDeleted() == false){
						//Get operation if operation changed
						Operation operation = TableOperations.FindOperationByTrelloId(dbHelper, tcard.getListId());
						if(operation != null){
							//Insert job in database accordingly
							job.setOperationId(operation.getId());
							job.setDateOperationIdChanged(tcard.getListId_changed());
							Log.d("MyTrelloContentProvider - insertCard", "Inserted as job");
							TableJobs.updateJob(dbHelper, job); //Insert job into db
							
							Intent toSend = new Intent(MainActivity.INTENT_JOB_UPDATED);
							toSend.putExtra("id", job.getId()); //This is set in updateJob()
							LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(toSend);
						} else {
							//If operation is null it's not on a operation list, should never happen
							Log.w("MyTrellContentProvider - updateCard", "Job doesn't have a valid operation id");
						}
					}
				}
			}
		}	
		//LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_ROCKS_UPDATED));
	}
	
	@Override
	public void insertList(TrelloList tlist){
		dbHelper = new DatabaseHelper(getContext());
		trelloHelper = new TrelloHelper(getContext());

		//New operation or new Worker List or Field List
		//TODO ask to merge???????
		
		Log.d("MyTrelloContentProvider", "insertList()");
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		String listWorkersTrelloId = prefs.getString("listWorkersTrelloId", "");
		String listFieldsTrelloId = prefs.getString("listFieldsTrelloId", "");
		String listWorkersId = prefs.getString("listWorkersLocalId", "");
		String listFieldsId = prefs.getString("listFieldsLocalId", "");
		String boardId = prefs.getString("boardTrelloId", "");
		
		//Try to convert to field list or worker list
		if(tlist.getClosed() == null || tlist.getClosed() == false){
			if(tlist.getName() != null){
				if(tlist.getName().contentEquals("Settings - Operator List")){
					//Add this worker list if we don't already have one
					if(listWorkersTrelloId.length() == 0){
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("listWorkersTrelloId", tlist.getId().trim());
						
						editor.commit();
					}
				} else if(tlist.getName().contentEquals("Settings - Field List")){
					//Add this field list if we don't already have one
					if(listFieldsTrelloId.length() == 0){
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("listFieldsTrelloId", tlist.getId().trim());
						editor.commit();
					}
				} else {
					if(tlist.getName().contentEquals("To Do") == false && tlist.getName().contentEquals("Doing") == false && tlist.getName().contentEquals("Done") == false){
						//New operation, add it to the operation table
						Operation toAdd = new Operation();
						Boolean deleted = false;
						
						if(tlist.getClosed() != null && tlist.getClosed() == true) deleted = true;
						if(tlist.getId() != null) toAdd.setRemote_id(tlist.getId());
						if(tlist.getName() != null) toAdd.setName(tlist.getName());
						if(tlist.getName_changed() != null) toAdd.setDateNameChanged(tlist.getName_changed());
						if(tlist.getBoardId() != null && tlist.getBoardId().contentEquals(boardId) == false) deleted = true;
						if(deleted){
							//Don't add to the db
						} else {
							//Add to the db
							TableOperations.updateOperation(dbHelper, toAdd);
						}
					}
				}
			}
		}
		//LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_ROCKS_UPDATED));
	}
	
	@Override
	public void insertBoard(TrelloBoard tBoard){
		Log.d("MyTrelloContentProvider", "insertBoard()");
		SharedPreferences prefs = this.getContext().getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		String boardId = prefs.getString("boardTrelloId", "");
		
		//Check if this is our board if we don't have it already
		if(tBoard.getClosed() == false && tBoard.getName().contentEquals("OpenATK - Field Work App")){
			Log.d("MyTrelloContentProvider - insertBoard", "Found new board on trello named the same as ours.");
			if(prefs.getString("boardTrelloId", "").length() == 0){
				//TODO prompt to merge etc...
				Log.d("MyTrelloContentProvider - insertBoard", "This is our trello board. Should we use it or make our own? PROMPT");
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("boardTrelloId", tBoard.getId());
				editor.putString("boardName", tBoard.getName());
				editor.putString("boardName_change", TrelloContentProvider.dateToUnixString(tBoard.getName_changed()));
				editor.putString("boardClosed_change", TrelloContentProvider.dateToUnixString(tBoard.getClosed_changed()));
				editor.putString("boardSyncDate", tBoard.getLastSyncDate());
				editor.putString("boardTrelloActionDate", tBoard.getLastTrelloActionDate());
				editor.commit();
			} else {
				Log.d("MyTrelloContentProvider - insertBoard", "We are already syncing to a Trello board. Ignoring new board.");
			}
		}
		//LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(new Intent(MainActivity.INTENT_ROCKS_UPDATED));
	}
}
