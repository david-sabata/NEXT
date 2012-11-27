package cz.fit.next.taskdetail;


import java.util.UUID;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TasksModelService;




public class TaskEditFragment extends Fragment {

	private static final String LOG_TAG = "TaskEditFragment";
	private View taskDetailView;

	/**
	 * Used in Bundle to store ID of task that is being shown
	 */
	private static final String ARG_TASK_ID = "taskID";


	/**
	 * Task whose details are showing or NULL when adding a new one
	 */
	private Task mTask;




	/**
	 * Create an empty instance of TaskEditFragment with 
	 * no prefilled values - use this to add a new task
	 */
	public static TaskEditFragment newInstance() {
		TaskEditFragment frag = new TaskEditFragment();

		return frag;
	}


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
	}



	/**
	 * Inflates layout and fills it with data of the loaded task
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		taskDetailView = inflater.inflate(R.layout.task_detail_fragment_edit, container, false);

		// load the task data into view
		if (mTask != null)
			loadTaskToView(mTask);
		else
			loadDefaults();

		return taskDetailView;
	}




	@Override
	public void onResume() {
		super.onResume();

		// register for gestures
		View v = getView().findViewById(R.id.scrollView);
		((MainActivity) getActivity()).attachGestureDetector(v);
	}




	/**
	 * Sets up the (sub)views according to the task
	 */
	private void loadTaskToView(Task task) {
		// set Title
		TextView title = (TextView) taskDetailView.findViewById(R.id.titleTask);
		title.setText(task.getTitle());

		// set description
		TextView descripton = (TextView) taskDetailView.findViewById(R.id.editDescription);
		descripton.setText(task.getDescription());

		// set date
		TextView date = (TextView) taskDetailView.findViewById(R.id.editDate);
		date.setText(task.getDate().toString());

		// Set IsCompleted
		CheckBox isCompleted = (CheckBox) taskDetailView.findViewById(R.id.editIsCompleted);
		isCompleted.setChecked(task.isCompleted());

		// set project
		Cursor cursor = TasksModelService.getInstance().getAllProjectsCursor();
		Spinner spinnerProject = (Spinner) taskDetailView.findViewById(R.id.spinnerProject);
		ProjectsSpinnerAdapter spinnerAdapter = new ProjectsSpinnerAdapter(getActivity(), cursor, 0);
		spinnerProject.setAdapter(spinnerAdapter);
		int posSpinner = spinnerAdapter.getPosition(task.getProject().getId(), cursor);
		if (posSpinner != -1) {
			spinnerProject.setSelection(posSpinner);
		} else {
			//default value
		}


		// set context
		TextView context = (TextView) taskDetailView.findViewById(R.id.editContext);
		context.setText(task.getContext());

		// set priority
		// Get value of selected RadioButton
		RadioGroup priorityGroup = (RadioGroup) taskDetailView.findViewById(R.id.radioPriority);
		switch (task.getPriority()) {
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



	private void loadDefaults() {
		// set date
		DateTime dateTime = new DateTime();
		TextView date = (TextView) taskDetailView.findViewById(R.id.editDate);
		date.setText(dateTime.toString());

		// set project
		Cursor cursor = TasksModelService.getInstance().getAllProjectsCursor();
		Spinner spinnerProject = (Spinner) taskDetailView.findViewById(R.id.spinnerProject);
		ProjectsSpinnerAdapter spinnerAdapter = new ProjectsSpinnerAdapter(getActivity(), cursor, 0);
		spinnerProject.setAdapter(spinnerAdapter);

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
			onSaveItem();
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



	/**
	 * Save current task or create a new one if no task was preloaded
	 */
	private void onSaveItem() {
		String title = ((TextView) taskDetailView.findViewById(R.id.titleTask)).getText().toString();
		String description = ((TextView) taskDetailView.findViewById(R.id.editDescription)).getText().toString();
		String projectTitle = ((TextView) ((Spinner) taskDetailView.findViewById(R.id.spinnerProject)).getSelectedView().findViewById(
				R.id.taskSpinnerText)).getText().toString();
		String projectId = ((TextView) ((Spinner) taskDetailView.findViewById(R.id.spinnerProject)).getSelectedView().findViewById(
				R.id.taskSpinnerText)).getTag().toString();
		String context = ((TextView) taskDetailView.findViewById(R.id.editContext)).getText().toString();
		boolean isCompleted = ((CheckBox) taskDetailView.findViewById(R.id.editIsCompleted)).isChecked();

		// task id
		String taskId = null;
		if (mTask == null) {
			taskId = UUID.randomUUID().toString();
		}
		else {
			taskId = mTask.getId();
		}

		// project
		Project project = null;
		if (projectId == null || projectId.isEmpty()) {
			project = new Project(projectTitle);
		}
		else {
			project = new Project(projectId, projectTitle);
		}

		// priority
		RadioGroup priorityGroup = (RadioGroup) taskDetailView.findViewById(R.id.radioPriority);
		int selected = priorityGroup.getCheckedRadioButtonId();
		RadioButton priorityBtn = (RadioButton) taskDetailView.findViewById(selected);
		int priority = Integer.parseInt(priorityBtn.getText().toString());

		// date time
		String dateString = ((TextView) taskDetailView.findViewById(R.id.editDate)).getText().toString();
		long timestamp = Long.parseLong(dateString);
		DateTime dateTime = new DateTime(timestamp);

		// Create new changed task
		Task editedTask =
				new Task(taskId,
						title,
						description,
						dateTime,
						priority,
						project,
						context,
						isCompleted
				);

		TasksModelService.getInstance().saveTask(editedTask);
	}




}
