package cz.fit.next.backend.sync;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.services.drive.model.File;

import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.SettingsProvider;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.backend.database.ProjectsDataSource;
import cz.fit.next.backend.database.TasksDataSource;
import cz.fit.next.backend.sync.drivers.GDrive;
import cz.fit.next.backend.sync.drivers.GDrive.UserPerm;

public class SyncService extends Service {

	private String TAG = "NEXT SyncService";
	
	public static final String BROADCAST_SYNC_START = "cz.fit.next.BROADCAST_SYNC_START";
	public static final String BROADCAST_RELOAD = "cz.fit.next.BROADCAST_RELOAD";
	public static final String BROADCAST_SYNC_END = "cz.fit.next.BROADCAST_SYNC_END";

	// Notification types
	private static final int NOTIFICATION_NEW_SHARED = 100;
	private static final int SHARING_ERROR = 101;
	private static final int SYNC_ERROR = 102;
	

	// Self
	private static SyncService sInstance = null;
	private static SyncService sInstanceOld = null;

	// GDrive Driver
	private GDrive drive;

	// private boolean mAuthorized = false;
	private String mAccountName;

	@Override
	public IBinder onBind(Intent arg0) {
		// Don't use binding
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i(TAG, "onStart");

		SettingsProvider sp = new SettingsProvider(getApplicationContext());
		mAccountName = sp.getString(SettingsProvider.PREF_ACCOUNT_NAME, null);

		if (mAccountName != null) {
			Log.e(TAG, "Connected as " + mAccountName);
			// synchronize();
			if (sInstanceOld == null) {
				AlarmReceiver alarm = new AlarmReceiver(
						getApplicationContext(), 10);
				alarm.run();
			}
		}

		sInstanceOld = sInstance;

		// if button pressed ask for username
		if (intent != null) {
			Bundle b = intent.getExtras();

			if (b != null) {
				if (b.getInt("inAuth") == -1) {

					authorizeDone(null, false);

				}
				if (b.getInt("inAuth") == 1) {

					authorizeDone(b.getString("accountName"), true);

				}

				if (b.getInt("SyncAlarm") == 1) {
					synchronize();
				}
				
				if (b.getInt("Share") == 1) {
					String sid = b.getString("ShareID");
					String gmail = b.getString("ShareGmail");
					share(sid,gmail);
				}
			}

		}

		return START_STICKY;
		// return START_NOT_STICKY;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");

		sInstance = this;
		// Log.e(TAG, "onCreate");
		drive = new GDrive();


	}

	public static SyncService getInstance() {
		return sInstance;
	}


	// ////////////////////////////////////////////////////////////////////////////////
	// AUTHORIZATION
	// ////////////////////////////////////////////////////////////////////////////////

	private void authorizeDone(String name, Boolean status) {


		if (status == true) {
			// mAuthorized = true;
			mAccountName = name;
			Log.e(TAG, "Authorized");

			Context context = getApplicationContext();
			CharSequence text = "Logged into GDrive as " + mAccountName;
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();

			synchronize();
		}

	}

	public String getAccountName() {
		return mAccountName;
	}

	/*
	 * Asynctask provides synchronization.
	 */
	private class SynchronizeClass extends AsyncTask<Void, Void, Object> {

		class returnObject {
			public int sharedCount;

			returnObject(int shared) {
				sharedCount = shared;
			}
		}

		@Override
		protected Object doInBackground(Void... params) {
			try {

			boolean retval = drive.initSync(getApplicationContext(),
					getInstance(), mAccountName);

			if (!retval) {
				return null;
			}


			ProjectsDataSource projdatasource = new ProjectsDataSource(
					getApplicationContext());
			TasksDataSource datasource = new TasksDataSource(
					getApplicationContext());

			Cursor cursor;

			ArrayList<Project> remoteProjects = new ArrayList<Project>();
			ArrayList<Task> remoteTasks = new ArrayList<Task>();
			ArrayList<Project> localProjects = new ArrayList<Project>();
			ArrayList<Project> resultProjects = new ArrayList<Project>();
			ArrayList<Task> resultTasks = new ArrayList<Task>();


			// =========== LOAD FILES FROM STORAGE ==============

			List<File> lf = drive.list(getApplicationContext(), getInstance());
			for (int i = 0; i < lf.size(); i++) {
				Log.i(TAG, "Sync File: " + lf.get(i).getTitle());

				// PERM TEST

				// drive.getUserList(lf.get(i).getId());
				// drive.share(lf.get(i).getId(), "xsychr03@gmail.com",
				// GDrive.READ);

				// drive.lock(lf.get(0).getId());
				// Log.i(TAG,"locked");


				// Download file from storage
				drive.download(getApplicationContext(), lf.get(i).getId());

				// Parse it
				JavaParser parser = new JavaParser();
				parser.setFile(getFilesDir() + "/" + lf.get(i).getTitle());

				Project proj = parser.getProject();
				
				// Handle default project universally
				if (proj.getTitle().equals(Constants.IMPLICIT_PROJECT_NAME)) {
					projdatasource.open();
					proj = projdatasource.getDefaultProject();
					projdatasource.close();
				}
				
				proj.setHistory(parser.getHistory());


				// Determine sharing status
				boolean shared = false;
				List<UserPerm> users = drive.getUserList(lf.get(i).getId());
				for (int j = 0; j < users.size(); j++) {
					if ((users.get(j).mode == GDrive.READ)
							|| (users.get(j).mode == GDrive.WRITE))
						shared = true;
				}
				Log.i(TAG,"SH: " + Boolean.toString(shared));
				proj.setShared(shared);


				// Delete temp file from mobile
				java.io.File f = new java.io.File(getFilesDir() + "/"
						+ lf.get(i).getTitle());
				f.delete();


				// Log.i(TAG,"Projekt id " + proj.getId() + " name " +
				// proj.getTitle());
				remoteTasks.addAll(parser.getTasks(proj));

				/*
				 * for (int j = 0; j < remoteTasks.size(); j++) {
				 * Log.i(TAG,"ID: " + remoteTasks.get(j).getId());
				 * Log.i(TAG,"Task: " + remoteTasks.get(j).getTitle());
				 * Log.i(TAG,"Desc: " + remoteTasks.get(j).getDescription()); }
				 */


				// add project to list of remote projects
				remoteProjects.add(proj);

			}


			// =========== LOAD DATA FROM LOCAL DATABASE ==============

			projdatasource.open();
			cursor = projdatasource.getAllProjectsCursor();
			cursor.moveToPrevious();
			while (cursor.moveToNext()) {
				localProjects.add(new Project(cursor));
			}
			projdatasource.close();



			// ============= MERGE HISTORIES ==========================
			for (int i = 0; i < remoteProjects.size(); i++) {
				for (int j = 0; j < localProjects.size(); j++) {
					if (remoteProjects.get(i).getId()
							.equals(localProjects.get(j).getId())) {
						// project exists in both sides, merge histories
						ArrayList<TaskHistory> merged = mergeHistories(
								remoteProjects.get(i).getHistory(),
								localProjects.get(j).getHistory());
						Project newproj = new Project(localProjects.get(j)
								.getId(), localProjects.get(j).getTitle(),
								localProjects.get(j).isStarred());
						newproj.setShared(remoteProjects.get(i).isShared());
						newproj.setHistory(merged);
						resultProjects.add(newproj);

						remoteProjects.remove(i);
						i--;
						localProjects.remove(j);
						j--;
						break;
					}
				}
			}

			resultProjects.addAll(remoteProjects);
			resultProjects.addAll(localProjects);


			Log.i(TAG, "before regenerate");
			// ============ REGENERATE TASKS ==================

			HashMap<String, Project> regenerate = new HashMap<String, Project>();

			for (int i = 0; i < resultProjects.size(); i++) { // projects
				for (int j = 0; j < resultProjects.get(i).getHistory().size(); j++) { // histories
																						// on
																						// one
																						// project
					String taskid = resultProjects.get(i).getHistory().get(j)
							.getTaskId();

					if (regenerate.get(taskid) == null) {
						regenerate.put(taskid, resultProjects.get(i));

						continue;
					}
				}
			}

			Log.i(TAG, "before store");
			// ============== STORE REGENERATED TASKS INTO DATABASE


			String pId;
			String pTitle;
			String pDescription;
			DateTime pDate = null;
			Integer pPriority;
			Project pProject;
			String pContext;
			Boolean pIsCompleted;


			projdatasource.open();
			datasource.open();

			Iterator<Entry<String, Project>> it = regenerate.entrySet()
					.iterator();

			while (it.hasNext()) {

				Map.Entry<String, Project> pair = it
						.next(); // move to next position

				pId = pair.getKey();

				pTitle = getLastValInHistory(pair.getValue().getHistory(),
						pair.getKey(), TaskHistory.TITLE);
				
				// throw out deleted tasks
				if (pTitle.matches(TasksModelService.deletedTitlePrefix + ".*")) continue;

				pDescription = getLastValInHistory(
						pair.getValue().getHistory(), pair.getKey(),
						TaskHistory.DESCRIPTION);

				pDate = new DateTime(Long.parseLong(getLastValInHistory(pair
						.getValue().getHistory(), pair.getKey(),
						TaskHistory.DATE)));

				pPriority = Integer.parseInt(getLastValInHistory(pair
						.getValue().getHistory(), pair.getKey(),
						TaskHistory.PRIORITY));

				pProject = pair.getValue();

				pContext = getLastValInHistory(pair.getValue().getHistory(),
						pair.getKey(), TaskHistory.CONTEXT);

				if (getLastValInHistory(pair.getValue().getHistory(),
						pair.getKey(), TaskHistory.COMPLETED).equals("true"))
					pIsCompleted = true;
				else
					pIsCompleted = false;

				projdatasource.saveProject(pair.getValue());
				datasource.saveTask(new Task(pId, pTitle, pDescription, pDate,
						pPriority, pProject, pContext, pIsCompleted));

			}
			projdatasource.close();
			datasource.close();

			Log.i(TAG, "before upload");
			
			// Send broadcast to reload gui
			Intent broadcast = new Intent();
			broadcast.setAction(BROADCAST_RELOAD);
			sendBroadcast(broadcast);
			
			// ============ UPDATE FILES ON REMOTE STORAGE ================

			datasource.open();
			for (int i = 0; i < resultProjects.size(); i++) {
				resultTasks.clear();

				cursor = datasource.getProjectTasksCursor(resultProjects.get(i)
						.getId());

				while (cursor.moveToNext()) {
					Task uptask = new Task(cursor);
					resultTasks.add(uptask);
				}

				// call serializer and uploader
				JavaParser parser = new JavaParser();
				parser.setProject(resultProjects.get(i));
				parser.setTasks(resultTasks);
				parser.setHistory(resultProjects.get(i).getHistory());
				try {
					parser.writeFile(getApplicationContext(), getFilesDir()
							+ "/" + resultProjects.get(i).getId());
				} catch (IOException e) {
					displayStatusbarNotification(SyncService.SYNC_ERROR, 1);
					e.printStackTrace();
				} catch (JSONException e) {
					displayStatusbarNotification(SyncService.SYNC_ERROR, 1);
					e.printStackTrace();
				}

				String postfix;
				if (resultProjects.get(i).getTitle().equals(Constants.IMPLICIT_PROJECT_NAME)) {
					postfix = "";
				} else {
					postfix = "-" + resultProjects.get(i).getId();
				}
				
				
				drive.upload(getApplicationContext(), resultProjects.get(i)
						.getTitle()
						+ postfix
						+ ".nextproj.html", resultProjects.get(i).getId());

			}
			datasource.close();


			// ============= SEARCH FOR NEW SHARED FILES ================
			List<File> lsf = drive.listShared(getApplicationContext());
			if (lsf != null) {
				for (int i = 0; i < lsf.size(); i++) {
					Log.i(TAG, "SharedFile: " + lsf.get(i).getTitle());
					drive.move(lsf.get(i).getId());
				}

				returnObject ret = new returnObject(lsf.size());
				return ret;


			} else

				return null;
			
			
			} catch (IOException e) {
				displayStatusbarNotification(SyncService.SYNC_ERROR, 1);
				return null;
			} catch (GoogleAuthException e) {
				displayStatusbarNotification(SyncService.SYNC_ERROR, 1);
				return null;	
			}
		}

		@Override
		protected void onPostExecute(Object param) {
			super.onPostExecute(param);

			if (param == null) {
				Log.i(TAG, "Bad username");
				return;
			}
			if (param != null) {
				if (((returnObject) param).sharedCount > 0) {
					displaySharedNotification(((returnObject) param).sharedCount);
					AlarmReceiver alarm = new AlarmReceiver(getApplicationContext(), 5);
					alarm.run();
				}
			}

			// Plan next synchronization
			// TODO: Variable interval
			AlarmReceiver alarm = new AlarmReceiver(getApplicationContext(), 1200);
			
			// Send broadcast for indicate end of sync
			Intent broadcast = new Intent();
			broadcast.setAction(BROADCAST_SYNC_END);
			sendBroadcast(broadcast);
			
			// alarm.run();

			// Log.i(TAG, "Killing SyncService.");
			// stopSelf();
		}
	}


	/**
	 * Merge histories, h1 is remote, h2 is local (preffered in merging)
	 */
	public ArrayList<TaskHistory> mergeHistories(ArrayList<TaskHistory> h1,
			ArrayList<TaskHistory> h2) {
		ArrayList<TaskHistory> res = new ArrayList<TaskHistory>();

		if ((h1 != null) && (h2 != null)) {
			for (int i = 0; i < h1.size(); i++) {
				for (int j = 0; j < h2.size(); j++) {
					if (h1.get(i).headerequals(h2.get(j))) {
						res.add(h2.get(i));
						h1.remove(i);
						i--;
						h2.remove(j);
						j--;
						break;
					}
				}
			}
		}

		if (h1 != null)
			res.addAll(h1);
		if (h2 != null)
			res.addAll(h2);


		// Log.i("MERGE","Remote: " + h1.size() + " local: " + h2.size() +
		// " result: " + res.size());

		return res;
	}

	/**
	 * Get last value in history
	 */
	private String getLastValInHistory(ArrayList<TaskHistory> history,
			String taskid, String field) {
		long timestamp = 0;
		String res = null;

		for (int i = 0; i < history.size(); i++) {
			for (int j = 0; j < history.get(i).getChanges().size(); j++) {
				if ((history.get(i).getChanges().get(j).getName().equals(field))
						&& (history.get(i).getTaskId().equals(taskid))
						&& (Long.parseLong(history.get(i).getTimeStamp()) > timestamp)) {
					timestamp = Long.parseLong(history.get(i).getTimeStamp());
					res = history.get(i).getChanges().get(j).getNewValue();
				}
			}
		}

		return res;
	}


	/*
	 * Starts synchronization
	 */
	public void synchronize() {

		if (!isNetworkAvailable()) {
			// Plan next synchronization only
			// TODO: Variable interval
			AlarmReceiver alarm = new AlarmReceiver(getApplicationContext(), 1200);
			// alarm.run();
		}
		else
		{
			// Send broadcast for indicate sync
			Intent broadcast = new Intent();
			broadcast.setAction(BROADCAST_SYNC_START);
			sendBroadcast(broadcast);
			
			SynchronizeClass cls = new SynchronizeClass();
			cls.execute();
		}
	}
	
	/**
	 * Shares a file
	 */
	private void share(String id, String user) {
		ProjectsDataSource pds = new ProjectsDataSource(getApplicationContext());
		pds.open();
		Project proj = pds.getProjectById(id);
		String title = proj.getTitle();
		pds.close();
		
		ShareClass cls = new ShareClass(id,title,user);
		cls.execute();
		
		Log.i(TAG,"Sharing after execute");
		
	}
	
	private class ShareClass extends AsyncTask<Object, Void, Void> {

		private String id;
		private String user;
		private String title;
		
		public ShareClass(String pId, String pTitle, String pUser) {
			id = pId;
			user = pUser;
			title = pTitle;
		}
		
		@Override
		protected Void doInBackground(Object... param) {
			String filename = title + "-" + id + ".nextproj.html";
			Boolean res = false;
			try {
				res = drive.share(filename, user, GDrive.WRITE );
			} catch (IOException e) {
				displayStatusbarNotification(SyncService.SHARING_ERROR, 1);
			}
			Log.i(TAG, "Sharing res" + res.toString());
			if (!res) displayStatusbarNotification(SyncService.SHARING_ERROR, 1);
			
			return null;
		}
		
		
	}
	
	/**
	 * Deletes project
	 */
	public boolean deleteProject(String id) {
		
		if (!isUserLoggedIn()) return true;
		if (!isNetworkAvailable()) return false;
		
		ProjectsDataSource pds = new ProjectsDataSource(getApplicationContext());
		pds.open();
		Project proj = pds.getProjectById(id);
		String title = proj.getTitle();
		pds.close();
		
		DeleteProjectClass cls = new DeleteProjectClass(id,title);
		cls.execute();
		try {
			return cls.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return false;
		}
		
	//	Log.i(TAG,"Project deleting after execute");
		
	}
	
	private class DeleteProjectClass extends AsyncTask<Void, Void, Boolean> {

		private String id;
		private String title;
		
		public DeleteProjectClass(String pId, String pTitle) {
			id = pId;
			title = pTitle;
		}
		
		@Override
		protected Boolean doInBackground(Void... param) {
			
			String filename = title + "-" + id + ".nextproj.html";
			
			try {
				drive.delete(filename);
			} catch (IOException e) {
				//displayStatusbarNotification(SyncService.SHARING_ERROR, 1);
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean status) {
			
			//return status;
		}
		
		
	}
	
	
	


	/*
	 * Display notification to notice about new shared files
	 */
	public void displaySharedNotification(int count) {
		displayStatusbarNotification(NOTIFICATION_NEW_SHARED, count);
	}

	/*
	 * Display statusbar notification
	 */
	private void displayStatusbarNotification(int type, int count) {

		String title = "";
		String content = "";
		String ticker = "";

		// TODO: More languages

		if (type == NOTIFICATION_NEW_SHARED) {
			title = "NEXT Sharing";
			content = "New shared files found on your Drive.";
			ticker = "NEXT: New shared files found.";
		}
		
		if (type == SHARING_ERROR) {
			title = "NEXT Sharing Error";
			content = "Tap here to resolve.";
			ticker = "NEXT: Sharing Error.";
		}
		
		if (type == SYNC_ERROR) {
			title = "NEXT Synchronization Error";
			content = "Tap here to sync again.";
			ticker = "NEXT: Synchronization Error.";
		}



		Notification.Builder mBuilder = new Notification.Builder(this).setSmallIcon(R.drawable.menu_next)
				.setContentTitle(title).setContentText(content)
				.setNumber(count).setTicker(ticker).setAutoCancel(true);

		//		Intent resultIntent = new Intent(this, MainActivity.class);
		
		//		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		//		stackBuilder.addParentStack(MainActivity.class);
		//		stackBuilder.addNextIntent(resultIntent);
		//		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		//		mBuilder.setContentIntent(resultPendingIntent);


		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		int notid = 0;
		mNotificationManager.notify(notid, mBuilder.getNotification());
	}

	
	/**
	 * Determines, if there is functional network connection
	 * @return boolean state
	 */
	public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	         = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}
	
	/**
	 * Determines, if there is stored username in preferences
	 * @return boolean state
	 */
	public boolean isUserLoggedIn() {
				
		if (getAccountName() != null)
			return true;
		else
			return false;
	}
	
}
