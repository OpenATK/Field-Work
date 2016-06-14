package com.openatk.libtrello;

import java.util.Date;

public class TrelloBoard extends TrelloObject {
	String id;
	String localId;
	
	String organizationId;
	Date organizationId_changed;
	
	String name;
	Date name_changed;

	String desc;
	Date desc_changed;
	
	String labelNames;
	Date labelNames_changed;
	
	Boolean closed;
	Date closed_changed;
	
	String lastSyncDate = "";
	String lastTrelloActionDate = "";
	
	TrelloBoard source;
	
	public static final String COL_ID = "id";
	public static final String COL_LOCAL_ID = "local_id";
	
	public static final String COL_ORGANIZATION_ID = "organization_id";
	public static final String COL_ORGANIZATION_ID_CHANGE = "organization_id";

	public static final String COL_NAME = "name";
	public static final String COL_NAME_CHANGE = "name_changed";

	public static final String COL_DESC = "desc";
	public static final String COL_DESC_CHANGE = "desc_changed";

	public static final String COL_LABEL_NAMES = "label_names";
	public static final String COL_LABEL_NAMES_CHANGED = "label_names_changed";

	public static final String COL_CLOSED = "closed";
	
	public static final String COL_LAST_SYNC_DATE = "last_sync";
	public static final String COL_TRELLO_ACTION_DATE = "last_trello_action";
	
	//public static final String[] COLUMNS = new String[] { COL_ID, COL_LOCAL_ID, COL_ORGANIZATION_ID, COL_ORGANIZATION_ID_CHANGE, COL_NAME, COL_NAME_CHANGE, COL_DESC, COL_DESC_CHANGE, COL_LABEL_NAMES, COL_LABEL_NAMES_CHANGE, COL_CLOSED };


	public TrelloBoard(){
		
	}
	
	public TrelloBoard(Object makeNull){
		if(makeNull == null){
			this.name = null;
			this.desc = null;
			this.closed = null;
			this.labelNames = null;
			this.source = null;
		}
	}
	
	/*public Object[] toObjectArray(){		
		return new Object[] { this.getId(), this.getLocalId(), this.getOrganizationId(), TrelloObject.DateToUnix(this.getOrganizationId_change()), this.getName(), TrelloObject.DateToUnix(this.getName_change()), this.getDesc(), TrelloObject.DateToUnix(this.getDesc_change()), this.getLabelNames(), TrelloObject.DateToUnix(this.getLabelNames_change()), this.getClosed() };
	}*/

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}
	
	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public Date getOrganizationId_change() {
		return organizationId_changed;
	}

	public void setOrganizationId_changed(Date organizationId_changed) {
		this.organizationId_changed = organizationId_changed;
	}

	public Date getName_changed() {
		return name_changed;
	}

	public void setName_changed(Date name_changed) {
		this.name_changed = name_changed;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Date getDesc_changed() {
		return desc_changed;
	}

	public void setDesc_changed(Date desc_changed) {
		this.desc_changed = desc_changed;
	}

	public String getLabelNames() {
		return labelNames;
	}

	public void setLabelNames(String labelNames) {
		this.labelNames = labelNames;
	}

	public Date getLabelNames_changed() {
		return labelNames_changed;
	}

	public void setLabelNames_changed(Date labelNames_changed) {
		this.labelNames_changed = labelNames_changed;
	}

	public Date getClosed_changed() {
		return closed_changed;
	}

	public void setClosed_changed(Date closed_changed) {
		this.closed_changed = closed_changed;
	}

	public Date getOrganizationId_changed() {
		return organizationId_changed;
	}

	public String getLastSyncDate() {
		return lastSyncDate;
	}

	public void setLastSyncDate(String lastSyncDate) {
		this.lastSyncDate = lastSyncDate;
	}

	public String getLastTrelloActionDate() {
		return lastTrelloActionDate;
	}

	public void setLastTrelloActionDate(String lastTrelloActionDate) {
		this.lastTrelloActionDate = lastTrelloActionDate;
	}

	public TrelloBoard getSource() {
		return source;
	}

	public void setSource(TrelloBoard source) {
		this.source = source;
	}
	
	
}
