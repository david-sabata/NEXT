package cz.fit.next.history;


import java.util.ArrayList;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.ServiceReadyListener;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TasksModelService;


public class HistoryFragment extends ListFragment implements ServiceReadyListener {

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
	private boolean mIsResumeDone = false;


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

		// set title
		if (mType == PROJECT)
			mTitle = getResources().getString(R.string.project_history);
		else
			mTitle = getResources().getString(R.string.task_history);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.history_list, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		MainActivity activity = (MainActivity) getActivity();

		activity.getActionBar().setTitle(mTitle);

		if (activity.isServiceReady())
			loadData();

		// register long click events
		registerForContextMenu(getListView());

		// register for gestures
		activity.attachGestureDetector(getListView());
	}



	@Override
	public void onServiceReady(TasksModelService s) {
		if (getActivity() != null)
			loadData();
	}


	/**
	 * Assumes the service is ready
	 */
	private void loadData() {
		ArrayList<TaskHistory> adapterData;

		// get project from database
		Project proj;
		if (mType == PROJECT) {
			proj = TasksModelService.getInstance().getProjectById(mId);
			adapterData = proj.getHistory();
			for (int i = 0; i < adapterData.size(); i++) {
				if (adapterData.get(i).getChanges().size() == 0) {
					adapterData.remove(i);
					i--;
				}
			}

			setListAdapter(new ProjectHistoryAdapter(getActivity(), 0, adapterData));
		} else {
			Task t = TasksModelService.getInstance().getTaskById(mId);
			proj = t.getProject();
			adapterData = new ArrayList<TaskHistory>();
			for (int i = 0; i < proj.getHistory().size(); i++) {
				if (proj.getHistory().get(i).getTaskId().equals(t.getId()))
					if (proj.getHistory().get(i).getChanges().size() > 0)
						adapterData.add(proj.getHistory().get(i));
			}

			setListAdapter(new TaskHistoryAdapter(getActivity(), 0, adapterData));
		}
	}

}
