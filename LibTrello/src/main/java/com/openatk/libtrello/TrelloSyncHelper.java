package com.openatk.libtrello;

import com.google.gson.Gson;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class TrelloSyncHelper {
	
	Runnable delayAutoSync = null;
	Runnable delaySync = null;
	Handler handler = new Handler();
	
	
	private static final String AUTHORITY = "com.openatk.rockapp.trello.sync_helper";

	public void autoSyncDelayed(final Context context){		
		if(delayAutoSync != null) handler.removeCallbacks(delayAutoSync);
		final Runnable r = new Runnable() {
		    public void run() {
		    	autoSync(context);
		    	delayAutoSync = null;
		    }
		};
		delayAutoSync = r;
        handler.postDelayed(delayAutoSync, 3000);
	}
	
	public void syncDelayed(final Context context){		
		if(delaySync != null) handler.removeCallbacks(delaySync);
		final Runnable r = new Runnable() {
		    public void run() {
		    	sync(context);
		        delaySync = null;
		    }
		};
		delaySync = r;
        handler.postDelayed(delaySync, 3000);
	}
	
	public void sync(Context context){
		Log.d("TrelloSyncHelper", "Syncing");		
		TrelloContentProvider.Sync(context.getApplicationContext().getPackageName());	
	}
	public void autoSyncOn(Context context){
		autoSyncOn(context, null);
	}
	public void autoSyncOn(Context context, Integer interval){
		//Set AutoSync flag, in preferences then trigger sync so it will pick it up
		ContentValues toPass = new ContentValues();
		Gson gson = new Gson();
		TrelloSyncInfo newInfo = new TrelloSyncInfo();
		newInfo.setAutoSync(true);
		if(interval != null) newInfo.setInterval(interval);
		String json = gson.toJson(newInfo);
		toPass.put("json", json);
		Uri uri = Uri.parse("content://" + context.getApplicationContext().getPackageName() + ".trello.provider/set_sync_info");
		context.getContentResolver().update(uri, toPass, null, null);  
		
		TrelloContentProvider.Sync(context.getApplicationContext().getPackageName());	
	}
	
	public void autoSyncOff(Context context){
		//Set AutoSync flag
		ContentValues toPass = new ContentValues();
		Gson gson = new Gson();
		TrelloSyncInfo newInfo = new TrelloSyncInfo();
		newInfo.setAutoSync(false);
		String json = gson.toJson(newInfo);
		toPass.put("json", json);
		Uri uri = Uri.parse("content://" + context.getApplicationContext().getPackageName() + ".trello.provider/set_sync_info");
		context.getContentResolver().update(uri, toPass, null, null);  
	}
	
	private void autoSync(Context context){
		TrelloSyncInfo syncInfo = this.getSyncInfo(context);
    	if(syncInfo != null && syncInfo.getAutoSync() == true){
			Bundle bundle = new Bundle();
	        bundle.putBoolean("isAutoSyncRequest", true);
	        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); // Performing a sync no matter if it's off
	        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true); // Performing a sync no matter if it's off
			TrelloContentProvider.Sync(context.getApplicationContext().getPackageName(), bundle);	
    	}
	}
	
	public TrelloSyncInfo getSyncInfo(Context context){
		Uri uri = Uri.parse("content://" +  context.getApplicationContext().getPackageName() + ".trello.provider/get_sync_info");
    	Cursor cursor = null;
    	boolean failed = false;
    	try {
    		cursor = context.getContentResolver().query(uri, null, null, null, null);
    	} catch(Exception e) {
    		failed = true;
    	}
    	
		TrelloSyncInfo syncInfo = null;
    	if(failed == false){
	    	Gson gson = new Gson();
	    	if(cursor != null){
	    		while(cursor.moveToNext()){
	    			//Only 1 item for now
	    			if(cursor.getColumnCount() > 0 && cursor.getColumnIndex("json") != -1){
		    			String json = cursor.getString(cursor.getColumnIndex("json"));
		    			try {
		    				syncInfo = gson.fromJson(json, TrelloSyncInfo.class);
		    			} catch (Exception e){
		    				Log.d("Failed to convert json to info:", json);
		    			}
	    			}
	    		}
	    		cursor.close();
	    	}
    	}
    	return syncInfo;
	}
	
	public void onResume(Context context){
		//Trello app will look to see if autosync is on and sync accordingly
		Log.d("TrelloSyncHelper", "onResume");		
		Bundle bundle = new Bundle();
        bundle.putBoolean("isAutoSyncRequest", true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); // Performing a sync no matter if it's off
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true); // Performing a sync no matter if it's off
		TrelloContentProvider.Sync(context.getApplicationContext().getPackageName(), bundle);
		
		//Check for developer mode presentation sync intervals, ie. intervals under 60 seconds while open.
		checkDevAutoSync(context);
	}
	
	public void onPause(){
		if(delayDevAutoSync != null) handler.removeCallbacks(delayDevAutoSync);
		delayDevAutoSync = null;
	}
	
	public void devAutoSyncOn(Context context, int interval){
		if(interval < 0) return;
		SharedPreferences prefs = context.getSharedPreferences(AUTHORITY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("devAutoInterval", interval);
		editor.commit();
		checkDevAutoSync(context);
	}
	public void devAutoSyncOff(Context context){
		if(delayDevAutoSync != null) handler.removeCallbacks(delayDevAutoSync);
		delayDevAutoSync = null;
		SharedPreferences prefs = context.getSharedPreferences(AUTHORITY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("devAutoInterval", 0);
		editor.commit();
	}
	private void checkDevAutoSync(Context context){
		SharedPreferences prefs = context.getSharedPreferences(AUTHORITY, Context.MODE_PRIVATE);
		int devInterval = prefs.getInt("devAutoInterval", 0);
		if(devInterval != 0){
			startDevAutoSync(context, devInterval);
		}
	}
	Runnable delayDevAutoSync = null;
	private void startDevAutoSync(final Context context, final int interval){
		//Auto sync for presentations, allows intervals under 60 sec androids syncprovider minimum.
		if(delayDevAutoSync != null) handler.removeCallbacks(delayDevAutoSync);
		final Runnable r = new Runnable() {
		    public void run() {
		    	autoSync(context);
		        if(delayDevAutoSync != null) handler.postDelayed(delayDevAutoSync, interval*1000);
		    }
		};
		delayDevAutoSync = r;
        handler.postDelayed(delayDevAutoSync, interval*1000);
	}
}
