package com.openatk.tillage.trello;

import com.openatk.libtrello.IBoard;

public class MyBoard implements IBoard {

	private String id;
	private String localId;
	private String name;
	private String desc;
	private Boolean closed;
	private Boolean changed;
	
	public MyBoard(String id, String localId, String name, String desc,
			Boolean closed, Boolean changed) {
		super();
		this.id = id;
		this.localId = localId;
		this.name = name;
		this.desc = desc;
		this.closed = closed;
		this.changed = changed;
	}

	@Override
	public String getTrelloId() {
		return id;
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
		return changed;
	}

	@Override
	public void setLocalChanges(Boolean changes) {
		this.changed = changes;	
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDesc() {
		return desc;
	}

}
