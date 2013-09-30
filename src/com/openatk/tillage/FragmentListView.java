package com.openatk.tillage;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.openatk.tillage.db.Category;
import com.openatk.tillage.db.DatabaseHelper;
import com.openatk.tillage.db.Field;
import com.openatk.tillage.db.Job;
import com.openatk.tillage.db.TableFields;
import com.openatk.tillage.db.TableJobs;

public class FragmentListView extends Fragment implements OnClickListener {
	private DatabaseHelper dbHelper;

	ListViewListener listener;

	private String searchText = "";
	
	private TextView tvTitle1;
	private TextView tvTitle2;
	private LinearLayout categoriesLinearLayout;

	private View viewPlanned;
	private View viewStarted;
	private View viewDone;
	private View viewNotPlanned;

	FragmentEditJobPopup fragmentEditField = null;
	
	List<JobHolder> jobLayouts = new ArrayList<JobHolder>();

	// Interface for receiving data
	public interface ListViewListener {
		Integer listViewGetCurrentOperationId();
		String ListViewGetCurrentFieldName();
		void ListViewOnClick(Job currentJob);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.list_view, container, false);

		tvTitle1 = (TextView) view.findViewById(R.id.list_view_tvTitle1);
		tvTitle2 = (TextView) view.findViewById(R.id.list_view_tvTitle2);
		categoriesLinearLayout = (LinearLayout) view
				.findViewById(R.id.list_view_categories);
		dbHelper = new DatabaseHelper(this.getActivity()
				.getApplicationContext());

		LayoutInflater vi = (LayoutInflater) this.getActivity()
				.getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewPlanned = vi.inflate(R.layout.list_view_category, null);
		viewStarted = vi.inflate(R.layout.list_view_category, null);
		viewDone = vi.inflate(R.layout.list_view_category, null);
		viewNotPlanned = vi.inflate(R.layout.list_view_category, null);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getData();

		// Return from save
		if (savedInstanceState != null) {
			// Populate info
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public void search(String search){
		Log.d("Searching", search);
		searchText = search;
		getData();
	}
	
	public void getData() {
		Log.d("FragmentListView", "getData()");

		// Remove all previous views
		categoriesLinearLayout.removeAllViews();
		jobLayouts.clear();
		
		// Make categories
		Category planned = new Category("Planned");
		Category started = new Category("Started");
		Category done = new Category("Done");
		Category notPlanned = new Category("Not Planned");

		// Add jobs to categories
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		List<String> fieldNames = new ArrayList<String>();
		List<String> doneFields = new ArrayList<String>(); // Used for title1
															// and title2
		
		int currentOperationId = listener.listViewGetCurrentOperationId();
		
		// Find job
		String where = TableJobs.COL_OPERATION_ID + " = " + Integer.toString(currentOperationId) + " AND " + TableJobs.COL_DELETED + " = 0";
		
		if(this.searchText.isEmpty() == false){
			where = where + " AND " + TableJobs.COL_FIELD_NAME + " LIKE '%" + this.searchText + "%'";
		}
		
		Cursor cursor = database.query(TableJobs.TABLE_NAME, TableJobs.COLUMNS,
				where, null, null, null, null);
		while (cursor.moveToNext()) {
			Job newJob = Job.cursorToJob(cursor);
			if (newJob.getStatus() == Job.STATUS_PLANNED) {
				planned.addJob(newJob);
			} else if (newJob.getStatus() == Job.STATUS_STARTED) {
				started.addJob(newJob);
			} else if (newJob.getStatus() == Job.STATUS_DONE) {
				done.addJob(newJob);
				doneFields.add(newJob.getFieldName());
			}
			fieldNames.add(newJob.getFieldName());
		}
		cursor.close();
		// Get all the fields not in fieldNames, this is our notPlanned jobs,
		// also calculate acres
		Integer totalAcres = 0;
		Integer acresDone = 0;
		String[] fieldsColumns = { TableFields.COL_NAME, TableFields.COL_ACRES, TableFields.COL_DELETED };
		String where2 = TableFields.COL_DELETED + " = 0";
		if(this.searchText.isEmpty() == false){
			where2 = where2 + " AND " + TableFields.COL_NAME + " LIKE '%" + this.searchText + "%'";
		}
		
		Cursor cursor2 = database.query(TableFields.TABLE_NAME, fieldsColumns, where2, null, null, null, null);
		while (cursor2.moveToNext()) {
			String curName = cursor2.getString(cursor2
					.getColumnIndex(TableFields.COL_NAME));
			if (fieldNames.contains(curName) == false) {
				Job newJob = new Job();
				newJob.setFieldName(curName);
				newJob.setStatus(Job.STATUS_NOT_PLANNED);
				notPlanned.addJob(newJob);
			} else {
				// Get Acres
				Integer acres = cursor2.getInt(cursor2
						.getColumnIndex(TableFields.COL_ACRES));
				totalAcres = totalAcres + acres;
				if (doneFields.contains(curName)) {
					acresDone = acresDone + acres;
				}
			}
		}
		cursor.close();
		dbHelper.close();

		tvTitle1.setText(Integer.toString(totalAcres - acresDone) + " ac  remaining");
		tvTitle2.setText(Integer.toString((fieldNames.size() - doneFields.size())) + " of "
				+ Integer.toString(fieldNames.size()) + " fields remaining");

		LayoutInflater vi = (LayoutInflater) this.getActivity()
				.getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Integer catJobCount = 0;
		String catTitle = "";
		List<Job> jobList;
		View toAdd;
		for (int i = 0; i < 4; i++) {
			Log.d("FragmentListView", "Adding" + Integer.toString(i));

			if (i == 0) {
				catJobCount = planned.countJobs();
				catTitle = planned.getTitle();
				jobList = planned.getJobs();
				toAdd = viewPlanned;
				RelativeLayout layCategory = (RelativeLayout) toAdd
						.findViewById(R.id.list_view_category_all);
				layCategory
						.setBackgroundResource(R.drawable.list_view_category_planned);
			} else if (i == 1) {
				catJobCount = started.countJobs();
				catTitle = started.getTitle();
				jobList = started.getJobs();
				toAdd = viewStarted;
				RelativeLayout layCategory = (RelativeLayout) toAdd
						.findViewById(R.id.list_view_category_all);
				layCategory
						.setBackgroundResource(R.drawable.list_view_category_started);
			} else if (i == 2) {
				catJobCount = done.countJobs();
				catTitle = done.getTitle();
				jobList = done.getJobs();
				toAdd = viewDone;
				RelativeLayout layCategory = (RelativeLayout) toAdd
						.findViewById(R.id.list_view_category_all);
				layCategory
						.setBackgroundResource(R.drawable.list_view_category_done);
			} else {
				catJobCount = notPlanned.countJobs();
				catTitle = notPlanned.getTitle();
				jobList = notPlanned.getJobs();
				toAdd = viewNotPlanned;
				RelativeLayout layCategory = (RelativeLayout) toAdd
						.findViewById(R.id.list_view_category_all);
				layCategory
						.setBackgroundResource(R.drawable.list_view_category_not_planned);
			}

			// fill in any details dynamically here
			CategoryHolder holder = new CategoryHolder();
			holder.txtTitle = (TextView) toAdd
					.findViewById(R.id.list_view_category_tvTitle);
			holder.butMore = (ImageButton) toAdd
					.findViewById(R.id.list_view_category_butMore);
			holder.butLess = (ImageButton) toAdd
					.findViewById(R.id.list_view_category_butLess);
			holder.listFields = (LinearLayout) toAdd
					.findViewById(R.id.list_view_category_field_list);
			holder.listFields.removeAllViews();

			holder.butMore.setOnClickListener(butMoreListener);
			holder.butMore.setTag(holder);

			holder.butLess.setOnClickListener(butLessListener);
			holder.butLess.setTag(holder);
			// insert into main view
			Log.d("FragmentListView", "Adding Category");
			categoriesLinearLayout.addView(toAdd);
			
			// Add fields to category
			int catAcres = 0;
			for (int j = 0; j < jobList.size(); j++) {
				View viewJob = vi.inflate(R.layout.list_view_job, null);
				TextView jobDate = (TextView) viewJob
						.findViewById(R.id.list_view_job_date);
				TextView jobTitle = (TextView) viewJob
						.findViewById(R.id.list_view_job_tvTitle);
				TextView jobWorker = (TextView) viewJob
						.findViewById(R.id.list_view_job_worker);
				ImageView jobImage = (ImageView) viewJob
						.findViewById(R.id.list_view_job_image);
				RelativeLayout layContent = (RelativeLayout) viewJob
						.findViewById(R.id.list_view_job_content);
				RelativeLayout layTopBar = (RelativeLayout) viewJob
						.findViewById(R.id.list_view_job_topbar);
				RelativeLayout fullJob = (RelativeLayout) viewJob
						.findViewById(R.id.list_view_job_full);

				jobDate.setText(jobList.get(j).getDateOfOperation());
				SQLiteDatabase database2 = dbHelper.getReadableDatabase();
				Field theField = Field.FindFieldByName(database2, jobList
						.get(j).getFieldName());
				String acres = "";
				if (theField != null) {
					if (theField.getAcres() != 0) {
						acres = acres + " - "
								+ Integer.toString(theField.getAcres()) + " ac";
						catAcres = catAcres + theField.getAcres();
					} else {
						jobImage.setBackgroundResource(R.drawable.boundary_none);
					}
				}
				dbHelper.close();
				
				jobTitle.setText(jobList.get(j).getFieldName() + acres);
				jobWorker.setText(jobList.get(j).getWorkerName());

				Log.d("FragmentListView", "Adding Job");
				holder.listFields.addView(viewJob);
				
				JobHolder newJobHolder = new JobHolder();
				newJobHolder.content = layContent;
				newJobHolder.topbar = layTopBar;
				newJobHolder.fieldName = jobList.get(j).getFieldName();
				
				jobLayouts.add(newJobHolder);
				
				fullJob.setTag(jobList.get(j));
				fullJob.setOnClickListener(this);
			}
			
			holder.txtTitle.setText(Integer.toString(catAcres) + " ac in " + Integer.toString(catJobCount) + " fields " + catTitle);
		}
		
		selectJob(listener.ListViewGetCurrentFieldName());
	}

	private OnClickListener butMoreListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			CategoryHolder holder = (CategoryHolder) v.getTag();
			holder.listFields.setVisibility(View.VISIBLE);
			holder.butLess.setVisibility(View.VISIBLE);
			holder.butMore.setVisibility(View.GONE);

			// Find all fields in this list and add them
		}
	};

	static class CategoryHolder {
		TextView txtTitle;
		ImageButton butMore;
		ImageButton butLess;
		LinearLayout listFields;
		List<Job> jobs;
	}

	private OnClickListener butLessListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			CategoryHolder holder = (CategoryHolder) v.getTag();
			holder.listFields.setVisibility(View.GONE);
			holder.butLess.setVisibility(View.GONE);
			holder.butMore.setVisibility(View.VISIBLE);
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof ListViewListener) {
			listener = (ListViewListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implement FragmentListView.ListViewListener");
		}
		Log.d("FragmentListView", "Attached");
	}

	public int getHeight() {
		// Method so close transition can work
		return getView().getHeight();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.list_view_job_full) {
			Job currentJob = (Job) v.getTag();
			listener.ListViewOnClick(currentJob);
		}
	}
	
	public void selectJob(String fieldName){
		for(int i=0; i<jobLayouts.size(); i++){
			JobHolder holder = jobLayouts.get(i);
			if(fieldName != null && holder.fieldName.contentEquals(fieldName)){
				holder.topbar.setBackgroundResource(R.drawable.list_view_job_topbar_selected);
				holder.content.setBackgroundResource(R.drawable.list_view_job_content_selected);
			} else {
				holder.topbar.setBackgroundResource(R.drawable.list_view_job_topbar);
				holder.content.setBackgroundResource(R.drawable.list_view_job_content);
			}
		}
	}
	
	private class JobHolder {
		RelativeLayout topbar;
		RelativeLayout content;
		String fieldName;
	}
}
