package com.openatk.libtrello;

import java.util.Date;

public class TrelloCard extends TrelloObject {
	String id;
	String localId;
	
	String listId;
	Date listId_changed;
	
	String boardId;
	Date boardId_changed;
	
	String name;
	Date name_changed;
	
	String desc;
	Date desc_changed;
	
	String labels;
	Date labels_changed;
	
	Boolean closed;
	Date closed_changed;
	
	Integer pos;
	Date pos_changed;
	
	TrelloCard source;
	
	public static final String COL_ID = "id";
	public static final String COL_LOCAL_ID = "local_id";
	
	public static final String COL_LIST_ID = "list_id";
	public static final String COL_LIST_ID_CHANGED = "list_id_changed";
	public static final String COL_BOARD_ID = "board_id";
	public static final String COL_BOARD_ID_CHANGED = "board_id_changed";
	public static final String COL_NAME = "name";
	public static final String COL_NAME_CHANGED = "name_changed";
	public static final String COL_DESC = "desc";
	public static final String COL_DESC_CHANGED = "desc_changed";
	public static final String COL_LABELS = "labels";
	public static final String COL_LABELS_CHANGED = "labels_changed";
	public static final String COL_CLOSED = "closed";
	public static final String COL_CLOSED_CHANGED = "closed_changed";
	public static final String COL_POS = "pos";
	public static final String COL_POS_CHANGED = "pos_changed";
	
	public TrelloCard(){
		
	}
	
	public TrelloCard(Object makeNull){
		if(makeNull == null){
			this.listId = null;
			this.boardId = null;
			this.name = null;
			this.desc = null;
			this.labels = null;
			this.closed = null;
			this.pos = null;;
			this.source = null;
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getBoardId() {
		return boardId;
	}

	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	public Integer getPos() {
		return pos;
	}

	public void setPos(Integer pos) {
		this.pos = pos;
	}

	public Date getListId_changed() {
		return listId_changed;
	}

	public void setListId_changed(Date listId_changed) {
		this.listId_changed = listId_changed;
	}

	public Date getBoardId_changed() {
		return boardId_changed;
	}

	public void setBoardId_changed(Date boardId_changed) {
		this.boardId_changed = boardId_changed;
	}

	public Date getName_changed() {
		return name_changed;
	}

	public void setName_changed(Date name_changed) {
		this.name_changed = name_changed;
	}

	public Date getDesc_changed() {
		return desc_changed;
	}

	public void setDesc_changed(Date desc_changed) {
		this.desc_changed = desc_changed;
	}

	public Date getLabels_changed() {
		return labels_changed;
	}

	public void setLabels_changed(Date labels_changed) {
		this.labels_changed = labels_changed;
	}

	public Date getClosed_changed() {
		return closed_changed;
	}

	public void setClosed_changed(Date closed_changed) {
		this.closed_changed = closed_changed;
	}

	public Date getPos_changed() {
		return pos_changed;
	}

	public void setPos_changed(Date pos_changed) {
		this.pos_changed = pos_changed;
	}

	public TrelloCard getSource() {
		return source;
	}

	public void setSource(TrelloCard source) {
		this.source = source;
	}

	
	
}
