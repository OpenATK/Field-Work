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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.openatk.field_work.models.Field;
import com.openatk.field_work.models.Job;
import com.openatk.field_work.models.Operation;
import com.openatk.field_work.models.Worker;
import com.openatk.libtrello.TrelloCard;
import com.openatk.libtrello.TrelloList;

public class TrelloHelper {
	
	private Context context;
	
	public TrelloHelper(Context context){
		this.context = context;
	}
	
	
	//Field to TrelloCard
	public TrelloCard toTrelloCard(Field field){
		SharedPreferences prefs = this.context.getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		String boardId = prefs.getString("boardTrelloId", "");
		String listIdFields = prefs.getString("listFieldsTrelloId", "");
		
		TrelloCard card = new TrelloCard(null);
		
		card.setId(field.getRemote_id());
		card.setLocalId(Integer.toString(field.getId()));
		card.setBoardId(boardId);
		
		card.setClosed(field.getDeleted());
		card.setClosed_changed(field.getDateDeleted());
		
		card.setName(field.getName());
		card.setName_changed(field.getDateNameChanged());

		//Generate description
		String desc = "Area: " + Float.toString(field.getAcres()) + " ac;"; //QUESTION ???? Needs to handle extra things that we didn't put in ourselves
		List<LatLng> points = field.getBoundary();
		if(points != null){
			desc = desc + "\nBoundary: ";
			for(int i=0; i<points.size(); i++){
				desc = desc + "(" + Double.toString(points.get(i).latitude) + "," + Double.toString(points.get(i).longitude) + "),";
			}
			desc = desc.substring(0, (desc.length()-1));
			desc = desc + ";";
		}
		
		card.setDesc(desc);
		Log.d("tocard","TOCard aname:" + field.getName());
		if(field.getDeleted() != null) Log.d("tocard","TOCard deleted:" + Boolean.toString(field.getDeleted()));

		if(field.getDateAcresChanged() != null && field.getDateBoundaryChanged() != null && field.getDateAcresChanged().after(field.getDateBoundaryChanged())){
			card.setDesc_changed(field.getDateAcresChanged());
		} else {
			card.setDesc_changed(field.getDateBoundaryChanged());
		}
		card.setListId(listIdFields);
		return card;
	}
	
	//Worker to TrelloCard
	public TrelloCard toTrelloCard(Worker worker){
		SharedPreferences prefs = this.context.getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		String boardId = prefs.getString("boardTrelloId", "");
		String listIdWorkers = prefs.getString("listWorkersTrelloId", "");
		
		TrelloCard card = new TrelloCard(null);
		
		card.setId(worker.getRemote_id());
		card.setLocalId(Integer.toString(worker.getId()));
		card.setBoardId(boardId);
		
		card.setClosed(worker.getDeleted());
		card.setClosed_changed(worker.getDateDeletedChanged());
		
		card.setName(worker.getName());
		card.setName_changed(worker.getDateNameChanged());
		
		card.setListId(listIdWorkers);
		return card;
	}
	
	//Job to TrelloCard
	public TrelloCard toTrelloCard(Job job, String operationId){
		SharedPreferences prefs = this.context.getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		String boardId = prefs.getString("boardTrelloId", "");
		
		TrelloCard card = new TrelloCard(null);
		
		card.setId(job.getRemote_id());
		card.setLocalId(Integer.toString(job.getId()));
		card.setBoardId(boardId);
		
		card.setClosed(job.getDeleted());
		card.setClosed_changed(job.getDateDeletedChanged());
		
		//Generate name
		SimpleDateFormat dateFormaterLocal = new SimpleDateFormat("MM/dd/yy", Locale.US);
		dateFormaterLocal.setTimeZone(TimeZone.getDefault());
		if(job.getDateOfOperation() == null){
			job.setDateOfOperation(new Date(0));
			Log.w("TrelloHelper - toTrelloCard", "This happens after initial sync, our job doesn't have an operation date. Fix update job status on first sync to fix this.");
		}
		String displayDate = dateFormaterLocal.format(job.getDateOfOperation());
		
		//TODO change this with labels
		String strStatus = "";
		if(job.getStatus() == Job.STATUS_DONE){
			strStatus = "Done";
		} else if(job.getStatus() == Job.STATUS_STARTED){
			strStatus = "Started";
		} else if(job.getStatus() == Job.STATUS_PLANNED){
			strStatus = "Planned";
		}
		String name = strStatus + " " + displayDate + ": " + job.getFieldName();
		if(job.getWorkerName() != null && job.getWorkerName().length() > 0){
			name = name + "-" + job.getWorkerName();
		}
		
		card.setName(name);
		//Name changed, latest of status, field name, and date of operation
		Date latest = job.getDateStatusChanged();
		if(latest == null || (job.getDateDateOfOperationChanged() != null && latest.before(job.getDateDateOfOperationChanged()))) latest = job.getDateDateOfOperationChanged();
		if(latest == null || (job.getDateFieldNameChanged() != null &&latest.before(job.getDateFieldNameChanged()))) latest = job.getDateFieldNameChanged();
		if(latest == null || (job.getDateWorkerNameChanged() != null &&latest.before(job.getDateWorkerNameChanged()))) latest = job.getDateWorkerNameChanged();
		
		if(latest == null) latest = new Date(0);
		card.setName_changed(latest);

		//Generate description
		String desc = "Comments: " + job.getComments() + ";";
		card.setDesc(desc);
		card.setDesc_changed(job.getDateCommentsChanged());
		
		//Lookup operation name
		card.setListId(operationId);

		return card;
	}
	
	public TrelloList toTrelloList(Operation operation){
		SharedPreferences prefs = this.context.getSharedPreferences("com.openatk.field_work", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		String boardId = prefs.getString("boardTrelloId", "");
		
		TrelloList list = new TrelloList(null);
		list.setLocalId(Integer.toString(operation.getId()));
		list.setId(operation.getRemote_id());
		list.setBoardId(boardId);
		
		list.setName(operation.getName());
		list.setName_changed(operation.getDateNameChanged());
		
		list.setClosed(operation.getDeleted());
		list.setClosed_changed(operation.getDateDeletedChanged());
		
		return list;
	}
	
	public Field toField(TrelloCard tcard){
		Field field = new Field(null);
		
		if(tcard.getId() != null){
			field.setRemote_id(tcard.getId());
		}
		
		if(tcard.getLocalId() != null){
			field.setId(Integer.parseInt(tcard.getLocalId()));
		}
		
		if(tcard.getName() != null){
			field.setName(tcard.getName());
			field.setDateNameChanged(tcard.getName_changed());
		}
		
		//Parse area and boundary from desc
		if(tcard.getDesc() != null){
			Float acres = 0.0f;
			Pattern p = Pattern.compile("(?s)Area:[ ]?([0-9]+[.]?[0-9]*) ac;");
			Matcher m = p.matcher(tcard.getDesc());
			if(m.find()){
				acres = Float.parseFloat(m.group(1));
			} else {
				Log.w("TrelloHelper", "Cant convert card desc to field acres.");
				return null;
			}
			field.setAcres(acres);
			field.setDateAcresChanged(tcard.getDesc_changed());
			
			List<LatLng> boundary = new ArrayList<LatLng>();
			Pattern p2 = Pattern.compile("(?s)Boundary:[ ]?(.*);");
			Matcher m2 = p2.matcher(tcard.getDesc());
			if(m2.find()){
				Log.d("TrelloHelper", "TrelloHelper - Found boundary");
				
				Pattern p3 = Pattern.compile("[(]([-]?[0-9]+[.]?[0-9]*)[,]([-]?[0-9]+[.]?[0-9]*)[)]");
				Matcher m3 = p3.matcher(m2.group(1));
				while(m3.find()){
					//Each coordinate
					Double lat = Double.parseDouble(m3.group(1));
					Double lng = Double.parseDouble(m3.group(2));
					boundary.add(new LatLng(lat, lng));
					
					Log.d("MyTrelloContentProvider", "Point:" + Double.toString(lat) + "," + Double.toString(lng));
				}
			} else {
				Log.w("TrelloHelper", "Cant convert card desc to field boundary.");
				return null; //Can't convert card
			}
			field.setBoundary(boundary);
			field.setDateBoundaryChanged(tcard.getDesc_changed());
		} else {
			Log.w("TrelloHelper", "Field does not have a desc, no boundary.");
		}
		
		if(tcard.getClosed() != null){
			field.setDeleted(tcard.getClosed());
			field.setDateDeleted(tcard.getClosed_changed());
		}
		return field;
	}
	public Worker toWorker(TrelloCard tcard){
		Worker worker = new Worker();
		
		if(tcard.getId() != null){
			worker.setRemote_id(tcard.getId());
		}
		
		if(tcard.getLocalId() != null){
			worker.setId(Integer.parseInt(tcard.getLocalId()));
		}
		
		if(tcard.getName() != null){
			worker.setName(tcard.getName());
			worker.setDateNameChanged(tcard.getName_changed());
		}
				
		if(tcard.getClosed() != null){
			worker.setDeleted(tcard.getClosed());
			worker.setDateDeletedChanged(tcard.getClosed_changed());
		}
		return worker;
	}
	
	public Job toJob(TrelloCard tcard){
		Job job = new Job(null);
		
		if(tcard.getId() != null){
			job.setRemote_id(tcard.getId());
		}
		
		if(tcard.getLocalId() != null){
			job.setId(Integer.parseInt(tcard.getLocalId()));
		}
				
		if(tcard.getName() != null){
			//Try to parse status, date, field, and worker from name
			Pattern p = Pattern.compile("^(.+)[ ]([0-9]{1,2})[/]([0-9]{1,2})[/]([0-9]{2,4})[:][ ]?([^-]+)[-]?(.*)");
			Matcher m = p.matcher(tcard.getName());
			if(m.find()){		
				String strStatus = m.group(1);
				
				job.setDateStatusChanged(tcard.getName_changed());
				if(strStatus.contentEquals("Done")){
					job.setStatus(Job.STATUS_DONE);
				} else if(strStatus.contentEquals("Started")){
					job.setStatus(Job.STATUS_STARTED);
				} else if(strStatus.contentEquals("Planned")){
					job.setStatus(Job.STATUS_PLANNED);
				} else {
					Log.w("TrelloHelper", "Cant convert card name to job status.");
					return null; //Failed to parse status, invalid job
				}
				
				int month = Integer.parseInt(m.group(2));
				int day = Integer.parseInt(m.group(3));
				int year = Integer.parseInt(m.group(4));
				if(year < 2000){
					year = year + 2000;
				}
				String strDateToParse = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);						
				SimpleDateFormat dateFormaterLocal = new SimpleDateFormat("yyyy-M-d", Locale.US);
				dateFormaterLocal.setTimeZone(TimeZone.getDefault());
				Date d;
				try {
					d = dateFormaterLocal.parse(strDateToParse);
				} catch (ParseException e) {
					d = new Date(0);
				}
				
				job.setDateOfOperation(d);
				job.setDateDateOfOperationChanged(tcard.getName_changed());
				
				job.setFieldName(m.group(5));
				Log.w("TrelloHelper", "fieldname:" + job.getFieldName());
				if(job.getFieldName() == null) Log.w("TrelloHelper", "fieldname is null:" + tcard.getName());
				job.setDateFieldNameChanged(tcard.getName_changed());
				job.setWorkerName(m.group(6));
				job.setDateWorkerNameChanged(tcard.getName_changed());
			} else {
				Log.w("TrelloHelper", "Cant convert card name to job.");
				return null; //Failed to convert to valid job
			}
		}
		
		if(tcard.getDesc() != null){
			//Try to parse comments from desc
			Pattern p2 = Pattern.compile("(?s)Comments:[ ]?(.*);");
			Matcher m2 = p2.matcher(tcard.getDesc());
			if(m2.find()){						
				job.setComments(m2.group(1));	
				job.setDateCommentsChanged(tcard.getDesc_changed());
			} else {
				Log.w("TrelloHelper", "Cant convert card desc to job comments. Desc:" + tcard.getDesc());
				return null; //Failed to convert to valid job
			}
		}
		
		if(tcard.getClosed() != null){
			job.setDeleted(tcard.getClosed());
			job.setDateDeletedChanged(tcard.getClosed_changed());
		}
		return job;
	}
}
