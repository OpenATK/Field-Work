package com.openatk.tillage.trello;

import com.openatk.libtrello.IList;

public class MyList implements IList {
	
	private String id;
	private Integer localId;
	private String boardId;
	private String name;
	private Boolean closed;
	private Boolean changed;
	
	public MyList(String id, Integer localId, String boardId, String name,
			Boolean closed, Boolean changed) {
		super();
		this.id = id;
		this.localId = localId;
		this.boardId = boardId;
		this.name = name;
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
	public String getBoardId() {
		return this.boardId;
	}

	@Override
	public String getName() {
		return this.name;
	}

}
