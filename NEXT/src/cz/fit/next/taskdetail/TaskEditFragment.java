package cz.fit.next.taskdetail;


import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.ServiceReadyListener;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TasksModelService;




public class TaskEditFragment extends Fragment implements ServiceReadyListener {
	private View taskDetailView;

	/**
	 * Used in Bundle to store ID of task that is being shown
	 */
	private static final String ARG_TASK_ID = "taskID";
	protected static final int DIALOG_EDIT_DATE = 1;
	protected static final int DIALOG_EDIT_TIME = 2;
	
	/**
	 * Used in Bundle to store Title, Description, Context (String values)
	 */
	private static final String ARG_TASK_SAVE = "taskTitle";
	


	/**
	 * In case we are editing a task, but service is not ready yet,
	 * so we need to remember ID to load it later
	 */
	private String mTaskId;

	/**
	 * Task whose details are showing or NULL when adding a new one
	 */
	private Task mTask;
	private Fragment editFragment;


	/**
	 * 	Setting details of date and time 
	 */
	private DateTime originalDateTime;

	/**
	 * Detect if task was restored from savedInstance
	 */
	private Boolean restoredTask;
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

		MainActivity activity = (MainActivity) getActivity();
		Bundle args = getArguments();

		if (args != null) {
			mTaskId = args.getString(ARG_TASK_ID);

			if (mTaskId != null && activity.isServiceReady())
				mTask = TasksModelService.getInstance().getTaskById(mTaskId);
		}

		
		
		editFragment = this;
	}



	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		
		// Get actual prority
		Spinner spinnerPriority = (Spinner) taskDetailView.findViewById(R.id.spinnerPriority);
		int priority = spinnerPriority.getSelectedItemPosition();
				
		// Get Actual info about project
		String projectId = ((TextView) ((Spinner) taskDetailView.findViewById(R.id.spinnerProject)).getSelectedView().findViewById(R.id.taskSpinnerText))
		.getTag().toString();
		String projectTitle = ((TextView) ((Spinner) taskDetailView.findViewById(R.id.spinnerProject)).getSelectedView().findViewById(R.id.taskSpinnerText))
				.getText().toString();
		Project project = null;
		if (projectId == null || projectId.isEmpty()) {
			project = new Project(projectTitle);
		} else {
			project = new Project(projectId, projectTitle);
		}
		
		Task saveTask = new Task(mTask.getId(),
				((TextView)taskDetailView.findViewById(R.id.titleTask)).getText().toString(),
				((TextView)taskDetailView.findViewById(R.id.editDescription)).getText().toString(),
				originalDateTime,
				priority,
				project,
				((TextView) taskDetailView.findViewById(R.id.editContext)).getText().toString(),
				((CheckBox) taskDetailView.findViewById(R.id.IsCompleted)).isChecked()
				);
	
		outState.putSerializable(ARG_TASK_SAVE, (Serializable) saveTask);
		
	}


	/**
	 * Inflates layout and fills it with data of the loaded task
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);		
		taskDetailView = inflater.inflate(R.layout.task_detail_fragment_edit, container, false);

		/**
		 * Init Spinner for priority
		 */
		Spinner spinnerPriority = (Spinner) taskDetailView.findViewById(R.id.spinnerPriority);
		String[] priorityTexts = getResources().getStringArray(R.array.priorityArray);
		PrioritySpinnerAdapter spinnerAdapter = new PrioritySpinnerAdapter(getActivity(), 0, priorityTexts);
		spinnerPriority.setAdapter(spinnerAdapter);
		
		MainActivity activity = (MainActivity) getActivity();
		
		if(savedInstanceState != null && savedInstanceState.getSerializable(ARG_TASK_SAVE) != null) {		
			// Restore original task if has been change
			mTask = (Task) savedInstanceState.getSerializable(ARG_TASK_SAVE);
			mTaskId = mTask.getId();
			loadTaskToView(mTask);
		} else if (mTaskId != null && activity.isServiceReady()) {
			mTask = TasksModelService.getInstance().getTaskById(mTaskId);
			loadTaskToView(mTask);
			mTaskId = mTask.getId();
		} else {
			loadDefaults();
		}
		




		/**
		 * Init editTime listener
		 */
		TextView time = (TextView) taskDetailView.findViewById(R.id.editTime);
		// We dont want keyboard
		time.setInputType(InputType.TYPE_NULL);
		time.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Calendar c = originalDateTime.toCalendar();
				TaskEditFragmentTimeDialog newFragment = new TaskEditFragmentTimeDialog(c.get(Calendar.MINUTE), c.get(Calendar.HOUR_OF_DAY));

				newFragment.setTargetFragment(editFragment, DIALOG_EDIT_TIME);
				newFragment.show(getActivity().getFragmentManager(), "DialogTime");
			}
		});

		/**
		 * Init editDate listener
		 */
		TextView date = (TextView) taskDetailView.findViewById(R.id.editDate);
		// We dont want keyboard
		date.setInputType(InputType.TYPE_NULL);
		date.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Calendar c = originalDateTime.toCalendar();

				TaskEditFragmentDateDialog newFragment = new TaskEditFragmentDateDialog(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
						.get(Calendar.DAY_OF_MONTH));

				newFragment.setTargetFragment(editFragment, DIALOG_EDIT_DATE);
				newFragment.show(getActivity().getFragmentManager(), "DialogDate");
			}
		});

		/**
		 * Init someday switch listener on switch
		 */
		Switch someDayButton = (Switch) taskDetailView.findViewById(R.id.somedaySwitch);
		someDayButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					Boolean allDay = originalDateTime.isAllday();
					originalDateTime = new DateTime();
					originalDateTime.setIsSomeday(false);
					originalDateTime.setIsAllday(allDay);
				}

				((TextView) taskDetailView.findViewById(R.id.TaskSubBodyEditDateLayout).findViewById(R.id.editDate)).setText(originalDateTime
						.toLocaleDateString());
				((TextView) taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).findViewById(R.id.editTime)).setText(originalDateTime
						.toLocaleTimeString());
				somedayChecked(isChecked);
			}
		});

		/**
		 * Init allday switch listener on switch
		 */
		Switch wholeDayButton = (Switch) taskDetailView.findViewById(R.id.wholeDaySwitch);
		wholeDayButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					originalDateTime.setIsAllday(false);
					originalDateTime = new DateTime();
				}

				((TextView) taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).findViewById(R.id.editTime)).setText(originalDateTime
						.toLocaleTimeString());
				alldayChecked(isChecked);
			}
		});

		return taskDetailView;
	}

	/**
	 * Set content, if someday switch is checked
	 * @param isChecked
	 */
	private void somedayChecked(Boolean isChecked) {
		if (isChecked) {
			originalDateTime.setIsSomeday(true);
			// Hide rest
			taskDetailView.findViewById(R.id.TaskSubBodyEditDateLayout).setVisibility(View.GONE);
			taskDetailView.findViewById(R.id.wholeDaySwitch).setVisibility(View.GONE);
			taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).setVisibility(View.GONE);

		} else {
			// Show rest
			taskDetailView.findViewById(R.id.TaskSubBodyEditDateLayout).setVisibility(View.VISIBLE);
			taskDetailView.findViewById(R.id.wholeDaySwitch).setVisibility(View.VISIBLE);
			if (!originalDateTime.isAllday()) {
				taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Set content, if allday switch is checked
	 * @param isChecked
	 */
	private void alldayChecked(Boolean isChecked) {
		if (isChecked) {
			originalDateTime.setIsAllday(true);

			// Hide rest
			taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).setVisibility(View.GONE);
		} else {
			// Restore all date witch actual time
			DateTime newDateTime = new DateTime();
			Calendar c = originalDateTime.toCalendar();
			newDateTime.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			originalDateTime = newDateTime;
			originalDateTime.setIsAllday(false);

			// Show rest
			taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Sets up the (sub)views according to the task
	 */
	private void loadTaskToView(Task task) {
		// Set Title
		TextView title = (TextView) taskDetailView.findViewById(R.id.titleTask);
		title.setText(task.getTitle());

		// Set description
		TextView descripton = (TextView) taskDetailView.findViewById(R.id.editDescription);
		descripton.setText(task.getDescription());

		// Set time
		TextView time = (TextView) taskDetailView.findViewById(R.id.editTime);
		time.setText(task.getDate().toLocaleTimeString());

		// Set date
		TextView date = (TextView) taskDetailView.findViewById(R.id.editDate);
		date.setText(task.getDate().toLocaleDateString());
		originalDateTime = task.getDate();

		// Set AllDaySwitch and content
		Switch allDayButton = (Switch) taskDetailView.findViewById(R.id.wholeDaySwitch);
		if (originalDateTime.isAllday()) {
			allDayButton.setChecked(true);
			taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).setVisibility(View.GONE);
		} else {
			allDayButton.setChecked(false);
			taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).setVisibility(View.VISIBLE);
		}

		// Set SomeDaySwitch and content
		Switch someDayButton = (Switch) taskDetailView.findViewById(R.id.somedaySwitch);
		if (originalDateTime.isSomeday()) {
			somedayChecked(true);
			someDayButton.setChecked(true);
		} else {
			somedayChecked(false);
			someDayButton.setChecked(false);
		}

		// Set IsCompleted
		CheckBox isCompleted = (CheckBox) taskDetailView.findViewById(R.id.IsCompleted);
		isCompleted.setChecked(task.isCompleted());

		// Set project
		Cursor cursor = TasksModelService.getInstance().getAllProjectsCursor();
		Spinner spinnerProject = (Spinner) taskDetailView.findViewById(R.id.spinnerProject);
		ProjectsSpinnerAdapter spinnerAdapter = new ProjectsSpinnerAdapter(getActivity(), cursor, 0);
		spinnerProject.setAdapter(spinnerAdapter);
		int posSpinner = spinnerAdapter.getPosition(task.getProject().getId(), cursor);
		if (posSpinner != -1) {
			spinnerProject.setSelection(posSpinner);
		} else {
			// Default value
		}

		// Set context
		TextView context = (TextView) taskDetailView.findViewById(R.id.editContext);
		context.setText(task.getContext());

		// Set priority spinner default value from database
		Spinner spinnerPriority = (Spinner) taskDetailView.findViewById(R.id.spinnerPriority);
		spinnerPriority.setSelection(task.getPriority());
	}

	/**
	 * Load defaults value, if new task is created (no data in database)
	 */
	private void loadDefaults() {
		// Set date
		DateTime dateTime = new DateTime();
		TextView date = (TextView) taskDetailView.findViewById(R.id.editDate);
		date.setText(dateTime.toLocaleDateString());
		originalDateTime = dateTime;

		TextView time = (TextView) taskDetailView.findViewById(R.id.editTime);
		time.setText(dateTime.toLocaleTimeString());

		// Set project
		Cursor cursor = TasksModelService.getInstance().getAllProjectsCursor();
		Spinner spinnerProject = (Spinner) taskDetailView.findViewById(R.id.spinnerProject);
		ProjectsSpinnerAdapter spinnerAdapter = new ProjectsSpinnerAdapter(getActivity(), cursor, 0);
		spinnerProject.setAdapter(spinnerAdapter);

		// Set spinner default value from database
		Spinner spinnerPriority = (Spinner) taskDetailView.findViewById(R.id.spinnerPriority);
		spinnerPriority.setSelection(0);

	}



	@Override
	public void onResume() {
		super.onResume();
	}



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.taskedit_actions, menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

		// Save
		if (item.getItemId() == R.id.action_save) {
			onSaveItem();
			imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
			getActivity().getFragmentManager().popBackStack();

			return true;
		}

		// Cancel
		if (item.getItemId() == R.id.action_cancel) {
			imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
			getActivity().getFragmentManager().popBackStackImmediate();

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
		String projectTitle = ((TextView) ((Spinner) taskDetailView.findViewById(R.id.spinnerProject)).getSelectedView().findViewById(R.id.taskSpinnerText))
				.getText().toString();
		String projectId = ((TextView) ((Spinner) taskDetailView.findViewById(R.id.spinnerProject)).getSelectedView().findViewById(R.id.taskSpinnerText))
				.getTag().toString();
		String context = ((TextView) taskDetailView.findViewById(R.id.editContext)).getText().toString();
		boolean isCompleted = ((CheckBox) taskDetailView.findViewById(R.id.IsCompleted)).isChecked();

		// Task id
		String taskId = null;
		if (mTask == null) {
			taskId = UUID.randomUUID().toString();
		} else {
			taskId = mTask.getId();
		}

		// Project
		Project project = null;
		if (projectId == null || projectId.isEmpty()) {
			project = new Project(projectTitle);
		} else {
			project = new Project(projectId, projectTitle);
		}

		// Save priority
		Spinner spinnerPriority = (Spinner) taskDetailView.findViewById(R.id.spinnerPriority);
		int priority = spinnerPriority.getSelectedItemPosition();


		// Set date
		DateTime dateTime = originalDateTime;

		// Create new changed task
		Task editedTask = new Task(taskId, title, description, dateTime, priority, project, context, isCompleted);

		TasksModelService.getInstance().saveTask(editedTask);
	}


	/**
	 * Get result from dialog
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case DIALOG_EDIT_DATE:
				Bundle dateData = data.getExtras();

				originalDateTime.setDate(dateData.getInt("year"), dateData.getInt("monthOfYear") + 1, dateData.getInt("dayOfMonth"));

				// Assign new date to dateView
				TextView dateView = (TextView) taskDetailView.findViewById(R.id.editDate);
				dateView.setText(originalDateTime.toLocaleDateString());
				break;

			case DIALOG_EDIT_TIME:
				Bundle time = data.getExtras();

				originalDateTime.setTime(time.getInt("hourOfDay"), time.getInt("minute"));

				// Assign new date to timeView
				TextView timeView = (TextView) taskDetailView.findViewById(R.id.editTime);
				timeView.setText(originalDateTime.toLocaleTimeString());

				break;

			default:
				Log.i("Unknown dialog request code", Integer.toString(requestCode));
				break;
		}
	}


	@Override
	public void onServiceReady(TasksModelService s) {
		if (mTaskId != null) {
			mTask = TasksModelService.getInstance().getTaskById(mTaskId);
			loadTaskToView(mTask);
		}
	}


}
