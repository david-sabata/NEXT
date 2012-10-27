package cz.fit.next.services;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import cz.fit.next.database.ProjectsDataSource;
import cz.fit.next.tasks.Task;

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
	public void onCreate() {
		Log.d(LOG_TAG, "service created");

		mInstance = this;
	}


	/**
	 * Service is shutting down
	 */
	public void onDestroy() {
		mInstance = null;

		// close database connection
		if (mProjectsDataSource != null)
			mProjectsDataSource.close();

		Log.d(LOG_TAG, "service destroyed");
	}

	public boolean onUnbind(Intent intent) {
		mInstance = null;

		Log.d(LOG_TAG, "service unbound");

		return false;
	}




	private void initProjectsDataSource(Context context) {
		if (mProjectsDataSource == null) {
			mProjectsDataSource = new ProjectsDataSource(context);
			mProjectsDataSource.open();
		}
	}


	// --------------------------------------------------------





	public List<Task> getAllItems() {
		List<Task> items = new ArrayList<Task>();

		for (int i = 0; i < 15; i++) {
			Task task = new Task();
			task.setTitle("polozka " + i);
			items.add(task);
		}

		return items;
	}



	public Cursor getAllProjectsCursor(Context context) {
		if (mProjectsDataSource == null)
			initProjectsDataSource(context);

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
