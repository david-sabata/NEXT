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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import cz.fit.next.R;
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

		taskDetailView = inflater.inflate(R.layout.task_detail_fragment_edit, container, false);

		// load the task data into view
		setDetailTask();

		return taskDetailView;
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

		// TODO implements others like title
		// set description
		TextView descripton = (TextView) taskDetailView.findViewById(R.id.editDescription);
		if (descripton != null) {
			descripton.setText(mTask.getDescription());
		}

		// set date
		TextView date = (TextView) taskDetailView.findViewById(R.id.editDate);
		if (date != null) {
			date.setText(mTask.getDate());
		}
		
		// Set IsCompleted
		CheckBox isCompleted= (CheckBox) taskDetailView.findViewById(R.id.editIsCompleted);
		if(isCompleted != null) {
			isCompleted.setChecked(mTask.isCompleted());
		}
		
		// set project
		TextView project = (TextView) taskDetailView.findViewById(R.id.editProject);
		if (project != null) {
			project.setText(mTask.getProject().getTitle());
		}

		// set context
		TextView context = (TextView) taskDetailView.findViewById(R.id.editContext);
		if (context != null) {
			context.setText(mTask.getContext());
		}

		// set priority
		// Get value of selected RadioButton
		RadioGroup priorityGroup = (RadioGroup) taskDetailView.findViewById(R.id.radioPriority);
		switch(mTask.getPriority()) {
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

		inflater.inflate(R.menu.taskedit_actions, menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// save
		if (item.getItemId() == R.id.action_save) {

			// TODO: save stuff
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



	private void onSaveItem() {
		// TODO Auto-generated method stub
		TextView title = (TextView) taskDetailView.findViewById(R.id.titleTask);
		TextView description = (TextView) taskDetailView.findViewById(R.id.editDescription);
		TextView date = (TextView) taskDetailView.findViewById(R.id.editDate);
		TextView project = (TextView) taskDetailView.findViewById(R.id.editProject);
		TextView context = (TextView) taskDetailView.findViewById(R.id.editContext);
		CheckBox isCompleted= (CheckBox) taskDetailView.findViewById(R.id.editIsCompleted);

		// Get value of selected RadioButton
		RadioGroup priorityGroup = (RadioGroup) taskDetailView.findViewById(R.id.radioPriority);
		int selected =  priorityGroup.getCheckedRadioButtonId();
		RadioButton priority = (RadioButton) taskDetailView.findViewById(selected);
		
		
		// Create new changed task
		Task editedTask = 
				new Task(mTask.getId(),
						title.getText().toString(),
						description.getText().toString(),
						date.getText().toString(),
						Integer.parseInt(priority.getText().toString()),
						mTask.getProject(),
						context.getText().toString(),
						isCompleted.isChecked()
				);
		Log.i("Priority", priority.getText().toString());
		TasksModelService.getInstance().saveTask(editedTask);
		
	}




}
