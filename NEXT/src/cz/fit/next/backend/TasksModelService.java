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
	 * Saves task to db. If there is already saved a task
	 * with same ID, it will be updated.
	 * If the associated Project object doesn't exist in db, it will
	 * be created.
	 */
	public void saveTask(Task task) {
				
		// add into history
		Project proj = task.getProject();
		ArrayList<TaskHistory> history = proj.getHistory();
		TaskHistory hist = new TaskHistory();
		hist.setAuthor(SyncService.getInstance().getAccountName());
		if (hist.getAuthor() == null) hist.setAuthor("");
		hist.setTaskId(task.getId());
		hist.setTimeStamp(new DateTime().toString());
		hist.addChange(SyncService.const_title, "", task.getTitle());
		hist.addChange(SyncService.const_completed, "", task.isCompleted() ? "true" : "false" );
		hist.addChange(SyncService.const_context, "", (task.getContext() != null) ? task.getContext() : "");
		hist.addChange(SyncService.const_date, "", task.getDate().toString());
		hist.addChange(SyncService.const_description, "", (task.getDescription() != null) ? task.getDescription() : "");
		hist.addChange(SyncService.const_priority, "", Integer.toString(task.getPriority()));
		if (history == null) history = new ArrayList<TaskHistory>();
		history.add(hist);
		proj.setHistory(history);
		
		// save project first
		if (task.getProject() != null) {
			mProjectsDataSource.saveProject(task.getProject());
		}
		
		// save task
		mTasksDataSource.saveTask(task);
	}


	/**
	 * Saves project to db. If there is already a project with 
	 * the same ID, it will be updated.
	 */
	public void saveProject(Project project) {
		mProjectsDataSource.saveProject(project);
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
	public String getLocalizedTime (Date d) {
		return DateUtils.formatDateTime(mContext,  d.getTime(), DateUtils.FORMAT_SHOW_TIME);
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
			Log.d("ModelServiceBinder", "getService");
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
