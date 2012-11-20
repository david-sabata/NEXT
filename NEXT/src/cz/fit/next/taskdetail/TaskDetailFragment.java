/**
 * 
 */
package cz.fit.next.taskdetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TasksModelService;

/**
 * @author Tomas Sychra
 * 
 */
public class TaskDetailFragment extends Fragment {

	private static final String LOG_TAG = "TaskDetailFragment";


	/**
	 * Used in Bundle to store ID of task that is being shown
	 */
	private static final String ARG_TASK_ID = "taskID";

	/**
	 * Task whose details are showing
	 */
	private Task mTask;

	private View taskDetailView;



	/**
	 * Create a new instance of TaskDetailFragment that will
	 * be initialized with task of given ID
	 * 
	 * Use ONLY this method to create a new instance!
	 */
	public static TaskDetailFragment newInstance(String taskId) {
		TaskDetailFragment frag = new TaskDetailFragment();

		Bundle b = new Bundle();
		b.putString(ARG_TASK_ID, taskId);

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
			if (TasksModelService.getInstance() == null)
				throw new RuntimeException("TaskModelService.getInstance() == null");

			String taskId = args.getString(ARG_TASK_ID);
			mTask = TasksModelService.getInstance().getTaskById(taskId);
		}
		else {
			Log.e(LOG_TAG, "onCreate with no arguments (getArguments==null)");
		}
	}



	/**
	 * Inflates layout and fills it with data of the loaded task
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		taskDetailView = inflater.inflate(R.layout.task_detail_fragment_show, container, false);

		// load the task data into view
		setDetailTask();

		return taskDetailView;
	}




	/**
	 * Reload Task data if data in DB has changed
	 */
	@Override
	public void onResume() {
		super.onResume();

		Task task = TasksModelService.getInstance().getTaskById(mTask.getId());
		if (!mTask.equals(task)) {
			mTask = task;
			setDetailTask();
		}
	}


	private void setTitleLayout(LinearLayout titleLayout, TextView textData, String itemType) {
		if (textData != null && titleLayout != null ) {
			String text = null;
			if(itemType.equals("title")) {
				text = mTask.getTitle();
			} else if (itemType.equals("description")) {
				text = mTask.getDescription();
			} else if (itemType.equals("date")) {
				text = mTask.getDate().toLocaleString();
			} else if (itemType.equals("project")) {
				text = mTask.getProject().getTitle();
			} else if (itemType.equals("context")) {
				text = mTask.getContext();
			}
		
			//Log.i("description", text);
			if(text != null && !text.equals("")) {
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
		TextView title = (TextView) taskDetailView.findViewById(R.id.titleTask);
		if (title != null) {
			title.setText(mTask.getTitle());
		}

		// set description
		TextView description = (TextView) taskDetailView.findViewById(R.id.textDescriptionShow);
		LinearLayout descriptionLayout = (LinearLayout) taskDetailView.findViewById(R.id.taskDescriptionLayout);
		setTitleLayout(descriptionLayout, description, "description");

		// set date
		TextView date = (TextView) taskDetailView.findViewById(R.id.textDateShow);
		LinearLayout dateLayout = (LinearLayout) taskDetailView.findViewById(R.id.taskDateLayout);
		setTitleLayout(dateLayout, date, "date");

		// set project
		TextView project = (TextView) taskDetailView.findViewById(R.id.textProjectShow);
		LinearLayout projectLayout = (LinearLayout) taskDetailView.findViewById(R.id.taskProjectLayout);
		setTitleLayout(projectLayout, project, "project");

		// set context
		TextView context = (TextView) taskDetailView.findViewById(R.id.textContextShow);
		LinearLayout contextLayout = (LinearLayout) taskDetailView.findViewById(R.id.taskContextLayout);
		setTitleLayout(contextLayout, context, "context");
		
		// Set IsCompleted
		CheckBox isCompleted = (CheckBox) taskDetailView.findViewById(R.id.IsCompleted);
		if (isCompleted != null) {
			isCompleted.setChecked(mTask.isCompleted());
		}

		// set priority
		// Get value of selected RadioButton
		RadioGroup priorityGroup = (RadioGroup) taskDetailView.findViewById(R.id.radioPriority);
		switch (mTask.getPriority()) {
			case 1:
				priorityGroup.check(R.id.radio0);
				break;
			case 2:
				priorityGroup.check(R.id.radio1);
				break;
			case 3:
				priorityGroup.check(R.id.radio2);
				break;
		}
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
			activity.getFanView().replaceMainFragment(fTask);

			return true;
		}


		return super.onOptionsItemSelected(item);
	}





}
