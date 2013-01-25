package cz.fit.next.history;


import java.util.ArrayList;

import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.ProjectsDataSource;
import cz.fit.next.tasklist.Filter;
import cz.fit.next.tasklist.TaskListFragment;

import android.app.ListFragment;
import android.content.Context;

import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;


public class HistoryFragment extends ListFragment {
	
	// History types
	public static final int PROJECT = 0;
	public static final int TASK = 1;
	
	// Bundle identifiers
	private static final String ARG_TYPE = "bundle_type";
	private static final String ARG_ID = "bundle_id";
	
	// Global parameters
	private int mType = PROJECT;
	private String mId;
	private String mTitle;
	
	
	public static HistoryFragment newInstance(int type, String id) {
		HistoryFragment frag = new HistoryFragment();

		Bundle b = new Bundle();
		b.putInt(ARG_TYPE, type);
		b.putString(ARG_ID, id);

		frag.setArguments(b);

		return frag;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mType = args.getInt(ARG_TYPE); // value OR 0
			mId = args.getString(ARG_ID); // value OR null
		}
		
		setHasOptionsMenu(true);
		ArrayList<TaskHistory> adapterData;
		
		
		// get project from database
		Project proj;
		if (mType == PROJECT) {
			proj = TasksModelService.getInstance().getProjectById(mId);
			adapterData = proj.getHistory();
			
			setListAdapter(new ProjectHistoryAdapter(getActivity(), 0, adapterData));
		} else {
			Task t = TasksModelService.getInstance().getTaskById(mId);
			proj = t.getProject();
			adapterData = new ArrayList<TaskHistory>();
			for (int i = 0; i < proj.getHistory().size(); i++) {
				if (proj.getHistory().get(i).getTaskId().equals(t.getId()))
					adapterData.add(proj.getHistory().get(i));
			}
			
			setListAdapter(new TaskHistoryAdapter(getActivity(), 0, adapterData));
		}
				
		
		

		
		
		// set title
		if (mType == PROJECT) mTitle = getResources().getString(R.string.project_history);
		else mTitle = getResources().getString(R.string.task_history);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.history_list, container, false);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		getActivity().getActionBar().setTitle(mTitle);

		// register long click events
		registerForContextMenu(getListView());

		// register for gestures
		((MainActivity) getActivity()).attachGestureDetector(getListView());
	}
	
	
	
}
