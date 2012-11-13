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
import android.widget.TextView;
import cz.fit.next.R;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TasksModelService;




public class TaskEditFragment extends Fragment {

	private static final String LOG_TAG = "TaskEditFragment";


	/**
	 * Used in Bundle to store ID of task that is being shown
	 */
	private static final String ARG_TASK_ID = "taskID";


	/**
	 * Task whose details are showing
	 */
	private Task mTask;





	/**
	 * Create a new instance of TaskEditFragment that will
	 * be initialized with task of given ID
	 * 
	 * Use ONLY this method to create a new instance!
	 */
	public static TaskEditFragment newInstance(String taskId) {
		TaskEditFragment frag = new TaskEditFragment();

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

		View taskDetailView = inflater.inflate(R.layout.task_detail_fragment_show, container, false);

		// load the task data into view
		setDetailTask(taskDetailView);

		return taskDetailView;
	}



	/**
	 * Sets up the (sub)views acording to the loaded task
	 */
	private void setDetailTask(View baseView) {
		// set Title
		TextView title = (TextView) baseView.findViewById(R.id.titleTask);
		if (title != null) {
			title.setText(mTask.getTitle());
		}

		// TODO implements others like title
		// set description
		TextView descripton = (TextView) baseView.findViewById(R.id.textDescriptionShow);
		if (descripton != null) {
			descripton.setText(mTask.getDescription());
		}

		// set date
		TextView date = (TextView) baseView.findViewById(R.id.textDateShow);
		if (date != null) {
			date.setText(mTask.getDate().toString());
		}

		// set project
		TextView project = (TextView) baseView.findViewById(R.id.textProjectShow);
		if (project != null) {
			project.setText(mTask.getProject().getTitle());
		}

		// set context
		TextView context = (TextView) baseView.findViewById(R.id.textContextShow);
		if (context != null) {
			context.setText(mTask.getContext());
		}

		// set priority
		//TextView priority = (TextView) taskDetailView.findViewById(R.id.textPriorityShow);
	}



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.taskedit_actions, menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// save
		if (item.getItemId() == R.id.action_save) {

			// TODO: save stuff

			getActivity().getSupportFragmentManager().popBackStack();
			return true;
		}

		// cancel
		if (item.getItemId() == R.id.action_cancel) {
			getActivity().getSupportFragmentManager().popBackStackImmediate();
			return true;
		}


		return super.onOptionsItemSelected(item);
	}




}