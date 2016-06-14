package com.openatk.libtrello;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class TrelloContentProvider extends ContentProvider {
	// used for the UriMacher
	private static final int CARDS = 1;
	private static final int CARD_ID = 2;
	private static final int LISTS = 3;
	private static final int LIST_ID = 4;
	private static final int BOARDS = 5;
	private static final int BOARD_ID = 6;
	
	private static final int JSON_CARD = 7;
	private static final int JSON_LIST = 8;
	private static final int JSON_BOARD = 9;
	private static final int ORG = 10;

	private static final int GET_INFO = 11;
	private static final int SET_INFO = 12;


	private static final String AUTHORITY = "com.openatk.field_work.trello.provider";

	//query
	private static final String BASE_PATH = "todos"; //cards, lists, boards
	private static final String CARD_PATH = "cards"; //cards
	private static final String LIST_PATH = "lists"; //lists
	private static final String BOARD_PATH = "boards"; //boards
	private static final String JSON_GET_INFO = "get_sync_info"; //info query

	//Update
	private static final String JSON_CARD_PATH = "card"; //card
	private static final String JSON_LIST_PATH = "list"; //list
	private static final String JSON_BOARD_PATH = "board"; //board
	private static final String ORG_PATH = "organization"; //organization
	private static final String JSON_SET_INFO = "set_sync_info"; //update


	private static SimpleDateFormat dateFormaterUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/todos";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/todo";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BOARD_PATH, BOARDS);
		sURIMatcher.addURI(AUTHORITY, CARD_PATH, CARDS);
		sURIMatcher.addURI(AUTHORITY, CARD_PATH + "/*", CARD_ID);
		sURIMatcher.addURI(AUTHORITY, LIST_PATH, LISTS);
		sURIMatcher.addURI(AUTHORITY, LIST_PATH + "/*", LIST_ID);
		sURIMatcher.addURI(AUTHORITY, BOARD_PATH + "/*", BOARD_ID);
		
		sURIMatcher.addURI(AUTHORITY, JSON_CARD_PATH, JSON_CARD);
		sURIMatcher.addURI(AUTHORITY, JSON_LIST_PATH, JSON_LIST);
		sURIMatcher.addURI(AUTHORITY, JSON_BOARD_PATH, JSON_BOARD);
		sURIMatcher.addURI(AUTHORITY, ORG_PATH, ORG);
		
		sURIMatcher.addURI(AUTHORITY, JSON_GET_INFO, GET_INFO);
		sURIMatcher.addURI(AUTHORITY, JSON_SET_INFO, SET_INFO);
	}
		
	@Override
	public boolean onCreate() {
		return false;
		
	}

	
	public static void Sync(String packageName){
		Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); // Performing a sync no matter if it's off
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true); // Performing a sync no matter if it's off
        TrelloContentProvider.Sync(packageName, bundle);
	}
	
	public static void Sync(String packageName, Bundle extras){
		Account account = null;
		extras.putString("appPackage", packageName);
        ContentResolver.requestSync(account, "com.openatk.trello.provider", extras);
	}
	
	public static boolean isInstalled(){
		Account account = null;
		return ContentResolver.getIsSyncable(account, "com.openatk.trello.provider") == 1 ? true : false;
	}
	
	public static String dateToStringUTC(Date date) {
		if(date == null){
			return null;
		}
		return TrelloContentProvider.dateFormaterUTC.format(date);
	}
	
	public static Date stringToDateUTC(String date) {
		if(date == null){
			return null;
		}
		Date d;
		try {
			d = TrelloContentProvider.dateFormaterUTC.parse(date);
		} catch (ParseException e) {
			d = new Date(0);
		}
		return d;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		//Query from sync service to get new or changed cards, lists, or boards since some date
		Cursor cursor;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case BOARDS:
				cursor = this.objectsToJsonCursor(this.getBoards());
				break;
			case LISTS:
				cursor = this.objectsToJsonCursor(this.getLists(selection));
				break;
			case CARDS:
				cursor = this.objectsToJsonCursor(this.getCards(selection));
				break;
			case CARD_ID:
				List<TrelloCard> items = new ArrayList<TrelloCard>();
				items.add(this.getCard(uri.getLastPathSegment()));
				cursor = this.objectsToJsonCursor(items);
				break;
			case GET_INFO:
				//Return info as cursor
				cursor = this.objectToJsonCursor(this.getSyncInfo());
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		// make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	
	private Cursor objectToJsonCursor(Object item){
		//One column in cursor, it is the json representation of the card
		//String[] menuCols = TrelloCard.COLUMNS;
		String[] columns = {"json"};
	    MatrixCursor cursor = new MatrixCursor(columns);
		//Convert object to json
		Gson gson = new Gson();
		String json = gson.toJson(item);
		cursor.addRow(new Object[] { json });
		return cursor;
	}
	
	private Cursor objectsToJsonCursor(List<?> items){
		//One column in cursor, it is the json representation of the card
		//String[] menuCols = TrelloCard.COLUMNS;
		String[] columns = {"json"};
	    MatrixCursor cursor = new MatrixCursor(columns);
		for(int i=0; i<items.size(); i++){
			//cursor.addRow(((TrelloObject) items.get(i)).toObjectArray());
			//Convert object to json
			Gson gson = new Gson();
			String json = gson.toJson(items.get(i));
			cursor.addRow(new Object[] { json });
		}
		return cursor;
	}
	
	
	private TrelloSyncInfo getSyncInfo(){
		TrelloSyncInfo theInfo = new TrelloSyncInfo();
		SharedPreferences prefs = this.getContext().getSharedPreferences(AUTHORITY, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
	
		String strDate = prefs.getString("dateLastSync", null);
		Boolean autoSync = prefs.getBoolean("TrelloContentProvider.autoSync", false);
		Boolean sync = prefs.getBoolean("TrelloContentProvider.sync", false);
		Integer interval = prefs.getInt("TrelloContentProvider.interval", 30);

		if(strDate != null) theInfo.setLastSync(TrelloContentProvider.stringToDateUTC(strDate));
		theInfo.setAutoSync(autoSync);
		theInfo.setSync(sync);
		theInfo.setInterval(interval);
		
		return theInfo;
	}
	
	private void setSyncInfo(TrelloSyncInfo newInfo){
		SharedPreferences prefs = this.getContext().getSharedPreferences(AUTHORITY, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = prefs.edit();
		
		if(newInfo.getLastSync() != null) editor.putString("dateLastSync", dateToStringUTC(newInfo.getLastSync()));
		if(newInfo.getAutoSync() != null) editor.putBoolean("TrelloContentProvider.autoSync", newInfo.getAutoSync());
		if(newInfo.getSync() != null) editor.putBoolean("TrelloContentProvider.sync", newInfo.getSync());
		if(newInfo.getInterval() != null) editor.putInt("TrelloContentProvider.interval", newInfo.getInterval());
		editor.commit();
	}
	
	//Custom implemented in every app
	public List<TrelloCard> getCards(String boardTrelloId){
		//Return all custom data as cards
		return null;
	}
	
	public TrelloCard getCard(String id){
		return null;
	}
	
	//Custom implemented in every app
	public List<TrelloList> getLists(String boardTrelloId){
		return null;
	}
	
	//Custom implemented in every app
	public List<TrelloBoard> getBoards(){
		return null;
	}
	
	
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		long id = 0;

		Gson gson = new Gson();
		String json;
		switch (uriType) {
			case CARDS:
				//id = sqlDB.insert(TodoTable.TABLE_TODO, null, values);
				break;
			case JSON_CARD:
				json = values.getAsString("json");
				TrelloCard card = gson.fromJson(json, TrelloCard.class);
				insertCard(card);
				break;
			case JSON_LIST:
				json = values.getAsString("json");
				TrelloList list = gson.fromJson(json, TrelloList.class);
				this.insertList(list);
				break;
			case JSON_BOARD:
				json = values.getAsString("json");
				TrelloBoard board = gson.fromJson(json, TrelloBoard.class);
				this.insertBoard(board);
				break;
			default:
				//throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}
	
	public void insertCard(TrelloCard card){
		
	}
	public void insertList(TrelloList list){
		
	}
	public void insertBoard(TrelloBoard board){
		
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		int rowsDeleted = 0;
		switch (uriType) {
			case CARDS:
				
				break;
			case CARD_ID:
				
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}
	
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		int rowsUpdated = 0;
		Gson gson = new Gson();
		String json;
		
		switch (uriType) {
			case CARDS:
				//Do something to all the cards				
				//rowsUpdated = database.update(TodoTable.TABLE_TODO, values, selection, selectionArgs);
				//this.updateCards(values);
				//TODO
				break;
			case CARD_ID:
				String id = uri.getLastPathSegment();
				break;
			case JSON_CARD:
				json = values.getAsString("json");
				TrelloCard card = gson.fromJson(json, TrelloCard.class);
				Log.d("TrelloContentProvider - update card:", "from json:" + json);
				rowsUpdated = this.updateCard(card);
				break;
			case JSON_LIST:
				json = values.getAsString("json");
				TrelloList list = gson.fromJson(json, TrelloList.class);
				rowsUpdated = this.updateList(list);
				break;
			case JSON_BOARD:
				json = values.getAsString("json");
				TrelloBoard board = gson.fromJson(json, TrelloBoard.class);
				rowsUpdated = this.updateBoard(board);
				break;
			case ORG:
				String oldOrg = values.getAsString("oldOrg");
				String newOrg = values.getAsString("newOrg");
				rowsUpdated = this.updateOrganization(oldOrg, newOrg);
				break;
			case SET_INFO:
				json = values.getAsString("json");
				TrelloSyncInfo theInfo = gson.fromJson(json, TrelloSyncInfo.class);
				Log.d("TrelloContentProvider", json);
				setSyncInfo(theInfo);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	
	public int updateCard(TrelloCard card){
		return 0;
	}
	public int updateList(TrelloList list){
		return 0;
	}
	public int updateBoard(TrelloBoard board){
		return 0;
	}
	public int updateOrganization(String oldOrganizationId, String newOrganizationId){
		return 0;
	}
	
	
	public static Date stringToDate(String date) {
		if(date == null){
			return null;
		}
		Date d;
		try {
			d = new Date((Long.parseLong(date)));
		} catch (Exception e) {
			d = new Date(0);
		}
		return d;
	}
	public static String dateToUnixString(Date date) {
		if(date == null){
			return null;
		}
		return Long.toString((date.getTime()));
	}
	
}
