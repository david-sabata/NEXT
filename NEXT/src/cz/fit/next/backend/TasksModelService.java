package cz.fit.next.backend;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import cz.fit.next.backend.database.ProjectsDataSource;

/**
 * @author David Sabata
 */
public class TasksModelService extends Service {

	private static final String LOG_TAG = "TasksModelService";


	/** Instance of self */
	private static TasksModelService mInstance;



	private ProjectsDataSource mProjectsDataSource = null;


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
	 * Returns list of dummy items
	 * 
	 * TODO: use Cursor
	 * TODO: get data from db
	 */
	public List<Task> getAllItems() {
		List<Task> items = new ArrayList<Task>();

		for (int i = 0; i < 15; i++) {
			Task task = new Task();
			task.setTitle("polozka " + i);
			items.add(task);
		}

		return items;
	}


	/**
	 * Returns cursor to all projects
	 */
	public Cursor getAllProjectsCursor() {
		Cursor cursor = mProjectsDataSource.getAllProjectsCursor();
		return cursor;
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
