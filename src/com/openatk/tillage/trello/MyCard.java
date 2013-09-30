package com.openatk.tillage.trello;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openatk.tillage.db.Job;

import com.openatk.libtrello.ICard;

public class MyCard implements ICard {
	
	public static int typeJob = 0;
	public static int typeField = 1;
	public static int typeWorker = 2;
	
	//Additional properties
	int type; //(Job, Worker, Field)
	int status;
	
	//Implemented properties
	String trelloId;
	Integer localId;
	Boolean closed;
	Boolean hasLocalChanges;
	String listId;
	String boardId;
	String name;
	String desc;
	Date changedDate;
	
	public MyCard(int type, String trelloId, Integer localId, Boolean closed,
			Boolean hasLocalChanges, String listId, String boardId,
			String name, String desc, Date changedDate) {
		super();
		this.type = type;
		this.trelloId = trelloId;
		this.localId = localId;
		this.closed = closed;
		this.hasLocalChanges = hasLocalChanges;
		this.listId = listId;
		this.boardId = boardId;
		this.name = name;
		this.desc = desc;
		this.changedDate = changedDate;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String getTrelloId() {
		return trelloId;
	}

	@Override
	public Object getLocalId() {
		return localId;
	}

	@Override
	public Boolean getClosed() {
		return closed;
	}

	@Override
	public Boolean hasLocalChanges() {
		return hasLocalChanges;
	}

	@Override
	public void setLocalChanges(Boolean changes) {
		this.hasLocalChanges = changes;
	}

	@Override
	public String getListId() {
		return listId;
	}

	@Override
	public String getBoardId() {
		return boardId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDesc() {
		String theDesc = "";
		if(desc != null){
			theDesc = desc;
		}
		return theDesc;
	}

	@Override
	public List<String> getLabelNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getLabels() {
		List<String> list = new ArrayList<String>();
		if(this.status == Job.STATUS_DONE){
			list.add("green");
		} else if(this.status == Job.STATUS_STARTED){
			list.add("yellow");
		} else if(this.status == Job.STATUS_PLANNED){
			list.add("red");
		} else {
			list = null;
		}
		return list;
	}

	@Override
	public Date getChangedDate() {
		return changedDate;
	}
}
