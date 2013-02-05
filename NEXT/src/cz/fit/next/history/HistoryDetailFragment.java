package cz.fit.next.history;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.backend.sync.SyncService;
import cz.fit.next.taskdetail.TaskDetailFragment;
import cz.fit.next.taskdetail.TaskEditFragment;

public class HistoryDetailFragment extends Fragment {
	private static final String TAG = "HistoryDetailFragment";


	/**
	 * Used in Bundle to store ID of task that is being shown
	 */
	private static final String ARG_TASK_ID = "taskID";
	private static final String ARG_TIMESTAMP = "timestamp";
	private static final String ARG_PROJECT_ID = "projID";

	/**
	 * In case we know only ID and service was not ready to load data yet
	 */
	private String mTaskId;

	/**
	 * Task whose details are showing
	 */
	private Task mTask;
	private Project mProject;
	
	private long mTimestamp;
	private String mProjectId;




	public static HistoryDetailFragment newInstance(String taskId, String projId, long timestamp) {
		HistoryDetailFragment frag = new HistoryDetailFragment();

		Bundle b = new Bundle();
		b.putString(ARG_TASK_ID, taskId);
		b.putLong(ARG_TIMESTAMP, timestamp);
		b.putString(ARG_PROJECT_ID, projId);

		frag.setArguments(b);

		return frag;
	}



	/**
	 * Initializes fragment from the saved state 
	 * 
	 * Arguments of the fragment have already been restored 
	 * so it is safe to call getArguments and get the data
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		Bundle args = getArguments();
		if (args != null) { 
			mTaskId = args.getString(ARG_TASK_ID);
			mTimestamp = args.getLong(ARG_TIMESTAMP);
			mProjectId = args.getString(ARG_PROJECT_ID);
		}
		
	}



	/**
	 * Inflates layout and fills it with data of the loaded task
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.history_detail_fragment, container, false);
	}




	/**
	 * Always reload Task data - we might be resuming from edit fragment
	 */
	@Override
	public void onResume() {
		super.onResume();

		MainActivity activity = (MainActivity) getActivity();

		if (mTaskId != null && activity.isServiceReady()) {
			//mTask = TasksModelService.getInstance().getTaskById(mTaskId);
			mProject = TasksModelService.getInstance().getProjectById(mProjectId);
			
			mTask = regenerateTask(mProject, mTaskId, mTimestamp);
			
			setDetailTask();
		}

		// register for gestures
		View v = getView().findViewById(R.id.scrollView);
		activity.attachGestureDetector(v);
	}
	
	private Task regenerateTask(Project proj, String taskId, long timestamp) {
		ArrayList<TaskHistory> history = proj.getHistory();
		
		boolean after = false;
		
		for (int i = 0; i < history.size(); i++) {
			if (after) {
				history.remove(i);
				i--;
			}
			
			if (Long.parseLong(history.get(i).getTimeStamp()) == timestamp)
				after = true;
		}
		
		Task t = new Task(taskId,
				SyncService.getLastValInHistory(history, taskId, TaskHistory.TITLE),
				SyncService.getLastValInHistory(history, taskId, TaskHistory.DESCRIPTION),
				new DateTime(Long.parseLong(SyncService.getLastValInHistory(history, taskId, TaskHistory.DATE))),
				Integer.parseInt(SyncService.getLastValInHistory(history, taskId, TaskHistory.PRIORITY)),
				mProject,
				SyncService.getLastValInHistory(history, taskId, TaskHistory.CONTEXT),
				Boolean.parseBoolean(SyncService.getLastValInHistory(history, taskId, TaskHistory.COMPLETED)));
		
		return t;
	}


	private void setTitleLayout(LinearLayout titleLayout, TextView textData, String itemType) {
		if (textData != null && titleLayout != null) {
			String text = null;
			if (itemType.equals("title")) {
				text = mTask.getTitle();
			} else if (itemType.equals("description")) {
				text = mTask.getDescription();
			} else if (itemType.equals("date")) {
				DateTime date = mTask.getDate();
				if (date.isSomeday()) {
					text = "Someday";
				} else if (date.isAllday()) {
					text = date.toLocaleDateString();
				} else {
					text = mTask.getDate().toLocaleDateTimeString();
				}
			} else if (itemType.equals("project")) {
				String projectText = mTask.getProject().getTitle();
				if (!(projectText.equals(Constants.IMPLICIT_PROJECT_NAME))) {
					text = mTask.getProject().getTitle();
				}
			} else if (itemType.equals("context")) {
				text = mTask.getContext();
			}

			//Log.i("description", text);
			if (text != null && !text.equals("")) {
				textData.setText(text);
				titleLayout.setVisibility(View.VISIBLE);
			} else {
				titleLayout.setVisibility(View.GONE);
			}
		}

	}

	/**
	 * Sets up the (sub)views acording to the loaded task
	 */
	private void setDetailTask() {
				
		// set Title
		TextView title = (TextView) getView().findViewById(R.id.titleTask);
		if (title != null) {
			title.setText(mTask.getTitle());
		}

		// set description
		TextView description = (TextView) getView().findViewById(R.id.textDescriptionShow);
		LinearLayout descriptionLayout = (LinearLayout) getView().findViewById(R.id.taskDescriptionLayout);
		setTitleLayout(descriptionLayout, description, "description");

		// set date
		TextView date = (TextView) getView().findViewById(R.id.textDateShow);
		LinearLayout dateLayout = (LinearLayout) getView().findViewById(R.id.taskDateLayout);
		setTitleLayout(dateLayout, date, "date");

		// set project
		TextView project = (TextView) getView().findViewById(R.id.textProjectShow);
		LinearLayout projectLayout = (LinearLayout) getView().findViewById(R.id.taskProjectLayout);
		setTitleLayout(projectLayout, project, "project");

		// set context
		TextView context = (TextView) getView().findViewById(R.id.textContextShow);
		LinearLayout contextLayout = (LinearLayout) getView().findViewById(R.id.taskContextLayout);
		setTitleLayout(contextLayout, context, "context");

		// Set IsCompleted
		CheckBox isCompleted = (CheckBox) getView().findViewById(R.id.IsCompleted);
		if (isCompleted != null) {
			isCompleted.setChecked(mTask.isCompleted());
		}

		// set priority
		TextView priority = (TextView) getView().findViewById(R.id.showPriorityText);
		String[] priorityStrings = getResources().getStringArray(R.array.priorityArray);
		priority.setText(priorityStrings[mTask.getPriority()]);

	}



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.taskdetail_actions, menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// edit
		if (item.getItemId() == R.id.action_edit) {

			// crate fragment with task edit
			TaskEditFragment fTask = TaskEditFragment.newInstance(mTask.getId());

			// replace main fragment with task detail fragment
			MainActivity activity = (MainActivity) getActivity();
			activity.replaceMainFragment(fTask);

			return true;
		}


		return super.onOptionsItemSelected(item);
	}
}
