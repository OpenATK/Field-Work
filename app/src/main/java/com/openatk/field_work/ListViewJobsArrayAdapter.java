package com.openatk.field_work;

import java.util.List;

import com.openatk.field_work.db.Category;
import com.openatk.field_work.db.DatabaseHelper;
import com.openatk.field_work.models.Job;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ListViewJobsArrayAdapter extends ArrayAdapter<Job> {
	private final Context context;
	private List<Job> jobs = null;
	private int resId;
		
	public ListViewJobsArrayAdapter(Context context, int layoutResourceId, List<Job> data) {
		super(context, layoutResourceId, data);
		this.resId = layoutResourceId;
		this.context = context;
		this.jobs = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Holder holder = null;
		
		if(row == null){
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(resId, parent, false);
			
			holder = new Holder();
			holder.txtTitle = (TextView) row.findViewById(R.id.list_view_job_tvTitle);
			holder.txtDate = (TextView) row.findViewById(R.id.list_view_job_date);
			holder.txtWorker = (TextView) row.findViewById(R.id.list_view_job_worker);
			holder.image = (ImageView) row.findViewById(R.id.list_view_job_image);
			
			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}
		
		if(jobs == null){
			Log.d("ListViewJobsArrayAdapter", "jobs null");
		} else {
			Log.d("ListViewJobsArrayAdapter", "Length:" + Integer.toString(jobs.size()));
			Log.d("ListViewJobsArrayAdapter", "Pos:" + Integer.toString(position));
		}
		
		if(holder == null){
			Log.d("ListViewJobsArrayAdapter", "holder null");
		} else {
			Job job = jobs.get(position);
			holder.txtTitle.setText(job.getFieldName()); //TODO add acres
			holder.txtDate.setText(DatabaseHelper.dateToStringLocal(job.getDateOfOperation()));
			holder.txtWorker.setText(job.getWorkerName());
		}
		return row;
	}
	
	static class Holder
    {
        TextView txtTitle;
        TextView txtDate;
        TextView txtWorker;
        ImageView image;
    }
}
