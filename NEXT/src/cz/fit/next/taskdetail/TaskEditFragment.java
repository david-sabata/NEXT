package cz.fit.next.taskdetail;


import java.util.Calendar;
import java.util.UUID;

import android.app.Fragment;
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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TasksModelService;




public class TaskEditFragment extends Fragment {
	private View taskDetailView;

	/**
	 * Used in Bundle to store ID of task that is being shown
	 */
	private static final String ARG_TASK_ID = "taskID";
	protected static final int DIALOG_EDIT_DATE = 1;
	protected static final int DIALOG_EDIT_TIME = 2;


	/**
	 * Task whose details are showing or NULL when adding a new one
	 */
	private Task mTask;
	private Fragment editFragment;


	/**
	 * 	Setting details of date and time 
	 */
	private DateTime originalDateTime;
	private Boolean wholeDay = false;
	private Boolean someDay = false;

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

		editFragment = this;
	}



	/**
	 * Inflates layout and fills it with data of the loaded task
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		taskDetailView = inflater.inflate(R.layout.task_detail_fragment_edit, container, false);

		// Init Spinner for priority
		Spinner spinnerPriority = (Spinner) taskDetailView.findViewById(R.id.spinnerPriority);
		String[] priorityTexts = getResources().getStringArray(R.array.priorityArray);
		PrioritySpinnerAdapter spinnerAdapter = new PrioritySpinnerAdapter(getActivity(), 0, priorityTexts );
		spinnerPriority.setAdapter(spinnerAdapter);
		
		
		// load the task data into view
		if (mTask != null)
			loadTaskToView(mTask);
		else
			loadDefaults();

		// set time
		TextView time = (TextView) taskDetailView.findViewById(R.id.editTime);
		// We dont want keyboard
		time.setInputType(InputType.TYPE_NULL);
		time.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Calendar c = originalDateTime.toCalendar();
				TaskEditFragmentTimeDialog newFragment =
						new TaskEditFragmentTimeDialog(
								c.get(Calendar.MINUTE),
								c.get(Calendar.HOUR_OF_DAY)
						);

				newFragment.setTargetFragment(editFragment, DIALOG_EDIT_TIME);
				newFragment.show(getActivity().getFragmentManager(), "DialogTime");
			}
		});

		// set date
		TextView date = (TextView) taskDetailView.findViewById(R.id.editDate);
		// We dont want keyboard
		date.setInputType(InputType.TYPE_NULL);
		date.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Calendar c = originalDateTime.toCalendar();


				TaskEditFragmentDateDialog newFragment =
						new TaskEditFragmentDateDialog(
								c.get(Calendar.YEAR),
								c.get(Calendar.MONTH),
								c.get(Calendar.DAY_OF_MONTH));

				newFragment.setTargetFragment(editFragment, DIALOG_EDIT_DATE);
				newFragment.show(getActivity().getFragmentManager(), "DialogDate");
			}
		});

		// Someday switch button
		Switch someDayButton = (Switch) taskDetailView.findViewById(R.id.somedaySwitch);
		someDayButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					someDay = true;
					originalDateTime.setIsSomeday(true);
					// hide rest
					taskDetailView.findViewById(R.id.TaskSubBodyEditDateLayout).setVisibility(View.GONE);
					taskDetailView.findViewById(R.id.wholeDaySwitch).setVisibility(View.GONE);
					taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).setVisibility(View.GONE);
				} else {
					someDay = false;
					originalDateTime.setIsSomeday(false);
					// show rest
					taskDetailView.findViewById(R.id.TaskSubBodyEditDateLayout).setVisibility(View.VISIBLE);
					taskDetailView.findViewById(R.id.wholeDaySwitch).setVisibility(View.VISIBLE);
					if (!wholeDay) {
						taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).setVisibility(View.VISIBLE);
					}
				}

			}
		});

		// Someday switch button
		Switch wholeDayButton = (Switch) taskDetailView.findViewById(R.id.wholeDaySwitch);
		wholeDayButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					wholeDay = true;
					originalDateTime.setIsAllday(true);
					// hide rest
					taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).setVisibility(View.GONE);
				} else {
					wholeDay = false;
					originalDateTime.setIsAllday(false);
					// show rest
					taskDetailView.findViewById(R.id.TaskSubBodyEditTimeLayout).setVisibility(View.VISIBLE);
				}
			}
		});
				
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


		// set time
		TextView time = (TextView) taskDetailView.findViewById(R.id.editTime);
		time.setText(task.getDate().toLocaleTimeString());

		// set date
		TextView date = (TextView) taskDetailView.findViewById(R.id.editDate);
		date.setText(task.getDate().toLocaleDateString());
		originalDateTime = task.getDate();

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


		
		// Set spinner default value from database
		Spinner spinnerPriority = (Spinner) taskDetailView.findViewById(R.id.spinnerPriority);
		spinnerPriority.setSelection(task.getPriority());
	}


	private void loadDefaults() {
		// set date
		DateTime dateTime = new DateTime();
		TextView date = (TextView) taskDetailView.findViewById(R.id.editDate);
		date.setText(dateTime.toLocaleDateString());
		originalDateTime = dateTime;

		TextView time = (TextView) taskDetailView.findViewById(R.id.editTime);
		time.setText(dateTime.toLocaleTimeString());

		// set project
		Cursor cursor = TasksModelService.getInstance().getAllProjectsCursor();
		Spinner spinnerProject = (Spinner) taskDetailView.findViewById(R.id.spinnerProject);
		ProjectsSpinnerAdapter spinnerAdapter = new ProjectsSpinnerAdapter(getActivity(), cursor, 0);
		spinnerProject.setAdapter(spinnerAdapter);
		
		// Set spinner default value from database
		Spinner spinnerPriority = (Spinner) taskDetailView.findViewById(R.id.spinnerPriority);
		spinnerPriority.setSelection(0);

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
			getActivity().getFragmentManager().popBackStack();
			return true;
		}

		// cancel
		if (item.getItemId() == R.id.action_cancel) {
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
		/*RadioGroup priorityGroup = (RadioGroup) taskDetailView.findViewById(R.id.radioPriority);
		int selected = priorityGroup.getCheckedRadioButtonId();
		RadioButton priorityBtn = (RadioButton) taskDetailView.findViewById(selected);
		int priority = Integer.parseInt(priorityBtn.getText().toString());		
		*/
		Spinner spinnerPriority = (Spinner) taskDetailView.findViewById(R.id.spinnerPriority);
		int priority = spinnerPriority.getSelectedItemPosition ();
		
		
		// Set date
		DateTime dateTime = originalDateTime;

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


	/**
	 * Get result from dialog
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);	
		switch(requestCode) {
		case DIALOG_EDIT_DATE:		
			Bundle dateData = data.getExtras();
						
			originalDateTime.setDate(
					dateData.getInt("year"), 
					dateData.getInt("monthOfYear") + 1, 
					dateData.getInt("dayOfMonth"));
			
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


		// Actualize OriginalTime
		//Calendar c = originalDateTime.toCalendar();

		// Date time
		// We have to decide what was changed and value, that wasnt changed parse from originalDateTime

		/*DateTime dateTime = null; 
		if(dateString != null && timeString != null) {
			dateTime = new DateTime(dateString + " " + timeString);
		} else if (dateString != null && timeString == null) {
			dateTime = new DateTime(dateString + " " + originalDateTime.toLocaleTimeString());
		} else if (dateString == null && timeString != null) {
			dateTime = new DateTime(originalDateTime.toDateNumericalString() + " " + timeString);
		} else {
			dateTime = originalDateTime;
		}*/
		

		// Save new OriginalTime
		//originalDateTime = dateTime;

	}




}
