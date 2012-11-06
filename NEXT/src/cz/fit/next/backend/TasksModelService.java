package cz.fit.next.backend;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import cz.fit.next.backend.database.ProjectsDataSource;
import cz.fit.next.backend.database.TasksDataSource;

/**
 * @author David Sabata
 */
public class TasksModelService extends Service {

	private static final String LOG_TAG = "TasksModelService";


	/** Instance of self */
	private static TasksModelService mInstance;



	private ProjectsDataSource mProjectsDataSource = null;

	private TasksDataSource mTasksDataSource = null;


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
		Cursor cursor = mProjectsDataSource.getAllProjectsCursor();
		return cursor;
	}



	/**
	 * Saves task to db. If there is already saved a task
	 * with same ID, it will be updated.
	 * If the associated Project object doesn't exist in db, it will
	 * be created.
	 */
	public void saveTask(Task task) {
		// save project first
		if (task.getProject() != null) {
			mProjectsDataSource.saveProject(task.getProject());
		}

		// save task
		mTasksDataSource.saveTask(task);
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
