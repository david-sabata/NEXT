package cz.fit.next.backend;

import java.util.ArrayList;
import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.FilterQueryProvider;
import cz.fit.next.R;
import cz.fit.next.backend.database.ProjectsDataSource;
import cz.fit.next.backend.database.TasksDataSource;
import cz.fit.next.backend.sync.SyncService;

/**
 * @author David Sabata
 */
public class TasksModelService extends Service {

	public static final String deletedTitlePrefix = "@%deleted_";
	private static final String LOG_TAG = "TasksModelService";

	/** Instance of self */
	private static TasksModelService mInstance;

	private ProjectsDataSource mProjectsDataSource = null;

	private TasksDataSource mTasksDataSource = null;

	/**
	 * Application context
	 */
	private Context mContext;

	// ---------------------------------------------------------------------------------

	/**
	 * Instance getter
	 */
	public static TasksModelService getInstance() {
		return mInstance;
	}

	/**
	 * Service has just been created
	 */
	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "service created");

		mInstance = this;
	}

	/**
	 * Service is shutting down
	 */
	@Override
	public void onDestroy() {
		mInstance = null;

		// close database connection
		if (mProjectsDataSource != null)
			mProjectsDataSource.close();

		Log.d(LOG_TAG, "service destroyed");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mInstance = null;

		Log.d(LOG_TAG, "service unbound");

		return false;
	}

	/**
	 * Initialize projects datasource
	 * 
	 * @param context
	 */
	private void initProjectsDataSource(Context context) {
		if (mProjectsDataSource == null) {
			mProjectsDataSource = new ProjectsDataSource(context);
			mProjectsDataSource.open();
		}

		if (mTasksDataSource == null) {
			mTasksDataSource = new TasksDataSource(context);
			mTasksDataSource.open();
		}
	}

	// ---------------------------------------------------------------------------------
	// All public calls assumes that initDataSources has been
	// called when activity was bound to the service
	// --------------------------------------------------------

	/**
	 * Called from activity upon binding to prepare dataSource
	 * 
	 * @param context
	 */
	public void initDataSources(Context context) {
		mContext = context.getApplicationContext();

		initProjectsDataSource(context);
	}

	/**
	 * Erases all database data by calling its onUpgrade method
	 */
	public void wipeDatabaseData() {
		mProjectsDataSource.wipeDatabaseData();
	}

	/**
	 * Returns cursor to all tasks
	 */
	public Cursor getAllTasksCursor() {
		Cursor cursor = mTasksDataSource.getAllTasksCursor();
		return cursor;
	}

	/**
	 * Returns single task with all data inicialized
	 */
	public Task getTaskById(String id) {
		Cursor cursor = mTasksDataSource.getSingleTaskCursor(id);
		if (cursor == null)
			return null;
		Task task = new Task(cursor);
		return task;
	}

	/**
	 * Returns cursor to all projects
	 */
	public Cursor getAllProjectsCursor() {
		return mProjectsDataSource.getAllProjectsCursor();
	}

	/**
	 * Returns cursor to starred projects only
	 */
	public Cursor getStarredProjectsCursor() {
		return mProjectsDataSource.getStarredProjectsCursor();
	}

	/**
	 * Return cursor to contexts
	 */
	public Cursor getContextsCursor() {
		Cursor cursor = mTasksDataSource.getContexts();
		return cursor;
	}

	/**
	 * Returns single project object
	 */
	public Project getProjectById(String id) {
		return mProjectsDataSource.getProjectById(id);
	}

	/**
	 * Saves task to db. If there is already saved a task with same ID, it will
	 * be updated. If the associated Project object doesn't exist in db, it will
	 * be created.
	 */
	public void saveTask(Task task) {


		Project proj = mProjectsDataSource.getProjectById(task.getProject().getId());

		ArrayList<TaskHistory> history = proj.getHistory();

		//Log.i("PREHIST", proj.getSerializedHistory());

		// generate history record
		TaskHistory hist = new TaskHistory();
		hist.setAuthor(SyncService.getInstance().getAccountName());
		if (hist.getAuthor() == null)
			hist.setAuthor("");
		hist.setTaskId(task.getId());
		hist.setTimeStamp(new DateTime().toString());

		Task old = getTaskById(task.getId());
		if ((old == null) || !old.getProject().getId().equals(proj.getId())) {

			hist.addChange(TaskHistory.TITLE, "", task.getTitle());
			hist.addChange(TaskHistory.COMPLETED, "", task.isCompleted() ? "true" : "false");
			hist.addChange(TaskHistory.CONTEXT, "", (task.getContext() != null) ? task.getContext() : "");
			hist.addChange(TaskHistory.DATE, "", task.getDate().toString());
			hist.addChange(TaskHistory.DESCRIPTION, "", (task.getDescription() != null) ? task.getDescription() : "");
			hist.addChange(TaskHistory.PRIORITY, "", Integer.toString(task.getPriority()));

		} else {

			// Task exists - use differential history

			// TITLE
			if (task.getTitle() != null) {
				if (old.getTitle() == null && task.getTitle() != null) {
					hist.addChange(TaskHistory.TITLE, "", task.getTitle());
				} else if (!old.getTitle().equals(task.getTitle())) {
					hist.addChange(TaskHistory.TITLE, old.getTitle(), task.getTitle());
				}
			}

			// DESCRIPTION
			if (task.getDescription() != null) {
				if (old.getDescription() == null && task.getDescription() != null) {
					hist.addChange(TaskHistory.DESCRIPTION, "", task.getDescription());
				} else if (!old.getDescription().equals(task.getDescription())) {
					hist.addChange(TaskHistory.DESCRIPTION, old.getTitle(), task.getDescription());
				}
			}

			// DATE
			if (!old.getDate().toString().equals(task.getDate().toString())) {
				hist.addChange(TaskHistory.DATE, old.getDate().toString(), task.getDate().toString());
			}

			// PRIORITY
			if (old.getPriority() != task.getPriority()) {
				hist.addChange(TaskHistory.PRIORITY, Integer.toString(old.getPriority()), Integer.toString(task.getPriority()));
			}

			// CONTEXT
			if (task.getContext() != null) {
				if (old.getContext() == null && task.getContext() != null) {
					hist.addChange(TaskHistory.CONTEXT, "", task.getContext());
				} else if (!old.getContext().equals(task.getContext())) {
					hist.addChange(TaskHistory.CONTEXT, old.getContext(), task.getContext());
				}
			}

			// COMPLETED
			if (old.isCompleted() != task.isCompleted()) {
				hist.addChange(TaskHistory.COMPLETED, old.isCompleted() ? "true" : "false", task.isCompleted() ? "true" : "false");
			}

		}

		if (history == null)
			history = new ArrayList<TaskHistory>();
		history.add(hist);
		proj.setHistory(history);

		mProjectsDataSource.saveProject(proj);


		// save task
		mTasksDataSource.saveTask(task);
	}

	/**
	 * Saves project to db. If there is already a project with the same ID, it
	 * will be updated.
	 */
	public void saveProject(Project project) {
		mProjectsDataSource.saveProject(project);
	}


	/**
	 * Delete project by its ID. Make sure the deletion has been confirmed 
	 * by the user!
	 * 
	 * @param projectId
	 */
	public void deleteProject(String projectId) {
		mProjectsDataSource.deleteProject(projectId);
	}
	
	/**
	 * Delete task identified by ID.
	 * 
	 * @param taskId
	 */
	public void deleteTask(String taskId) {
		
		Task deleting = getTaskById(taskId);
		
		// write deletion to the project history
		Project proj = mProjectsDataSource.getProjectById(deleting.getProject().getId());

		ArrayList<TaskHistory> history = proj.getHistory();
		
		// generate history record
		TaskHistory hist = new TaskHistory();
		hist.setAuthor(SyncService.getInstance().getAccountName());
		if (hist.getAuthor() == null)
			hist.setAuthor("");
		hist.setTaskId(deleting.getId());
		hist.setTimeStamp(new DateTime().toString());
		
		hist.addChange(TaskHistory.TITLE, deleting.getTitle(), deletedTitlePrefix + deleting.getTitle());
		
		if (history == null)
			history = new ArrayList<TaskHistory>();
		history.add(hist);
		proj.setHistory(history);

		mProjectsDataSource.saveProject(proj);
		
		
		// delete task from database
		mTasksDataSource.deleteTask(taskId);
	}




	/**
	 * Returns localized date string
	 */
	public String getLocalizedDate(Date d) {
		return DateUtils.formatDateTime(mContext, d.getTime(), DateUtils.FORMAT_SHOW_DATE);
	}

	/**
	 * Returns localized date and time string
	 */
	public String getLocalizedDateTime(Date d) {
		return DateUtils.formatDateTime(mContext, d.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
	}

	/**
	 * Returns localized time string
	 */
	public String getLocalizedTime(Date d) {

		return DateUtils.formatDateTime(mContext, d.getTime(), DateUtils.FORMAT_SHOW_TIME);

	}

	/**
	 * Returns localized label for 'someday'
	 */
	public String getLocalizedSomedayTime() {
		return mContext.getResources().getString(R.string.someday);
	}

	/**
	 * Returns tasks filter query provider used to filter tasks
	 */
	public FilterQueryProvider getTasksFilterQueryProvider() {
		return mTasksDataSource.getFilterQueryProvider();
	}

	// ---------------------------------------------------------------------------------

	// Bound Service
	// ------------------------------------------------------------------

	public class ModelServiceBinder extends Binder {
		public TasksModelService getService() {
			return TasksModelService.this;
		}
	};

	private final IBinder mBinder = new ModelServiceBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(LOG_TAG, "onBind");

		return mBinder;
	}

	// --------------------------------------------------------------------------------

}
