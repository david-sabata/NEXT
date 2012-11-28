package cz.fit.next.backend.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.json.JSONException;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;

import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;


import com.google.api.services.drive.model.File;

import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.ProjectsDataSource;
import cz.fit.next.backend.database.TasksDataSource;
import cz.fit.next.backend.sync.drivers.GDrive;




public class SyncService extends Service {

	private String TAG = "NEXT SyncService";
	private static final String PREF_FILE_NAME = "SyncServicePref";
	private static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";
	
	// String definitions for History object
	public static String const_title = "next_hist_title";
	public static String const_description = "next_hist_description";
	public static String const_date = "next_hist_date";
	public static String const_priority = "next_hist_priority";
	public static String const_project = "next_hist_project";
	public static String const_context = "next_hist_context";
	public static String const_completed = "next_hist_completed";

	// Notification types
	private static final int NOTIFICATION_NEW_SHARED = 100;

	// Self
	private static SyncService sInstance = null;
	private static SyncService sInstanceOld = null;
	
	// GDrive Driver
	private GDrive drive;

	//private boolean mAuthorized = false;
	private String mAccountName;



	@Override
	public IBinder onBind(Intent arg0) {
		// Don't use binding
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.i(TAG,"onStart");
		
		
		// Reload stored preferences
		SharedPreferences settings = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
		mAccountName = settings.getString(PREF_ACCOUNT_NAME, null);

		if (mAccountName != null) {
			Log.e(TAG, "Connected as " + mAccountName);
			//synchronize();
			if (sInstanceOld == null) {
				AlarmReceiver alarm = new AlarmReceiver(getApplicationContext(), 10);
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
			}
					
		}
		
		return START_STICKY;
		//return START_NOT_STICKY;
	}


	@Override
	public void onCreate() {
		Log.i(TAG,"onCreate");
		
		sInstance = this;
		// Log.e(TAG, "onCreate");
		drive = new GDrive();

		
	}
	
	public static SyncService getInstance() {
		return sInstance;
	}
	


	//////////////////////////////////////////////////////////////////////////////////
	// AUTHORIZATION	
	//////////////////////////////////////////////////////////////////////////////////
	
	
	private void authorizeDone(String name, Boolean status) { 
	
		if (status == true) {
			//mAuthorized = true;
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
			
			boolean retval = drive.initSync(getApplicationContext(), getInstance(), mAccountName);
			if (!retval) {
				return null;
			}
			
			ProjectsDataSource projdatasource = new ProjectsDataSource(getApplicationContext());
			TasksDataSource datasource;
			Cursor cursor;
			
			ArrayList<Project> remoteProjects = new ArrayList<Project>();
			ArrayList<Project> dirtyProjects = new ArrayList<Project>(); 
			ArrayList<ArrayList<TaskHistory>> dirtyProjectsHistories = new ArrayList<ArrayList<TaskHistory>>();
			
			// SYNCHRONIZATION STAGE ONE - from remote to local
			
			// loop walking through files on storage
			List<File> lf = drive.list(getApplicationContext(), getInstance());
			for (int i = 0; i < lf.size(); i++) {
				Log.i(TAG,"Sync File: " + lf.get(i).getTitle());
				
				// PERM TEST
				drive.getUserList(lf.get(i).getId());
				//drive.share(lf.get(i).getId(), "xsychr03@gmail.com", GDrive.READ);
				
				//drive.lock(lf.get(0).getId());
				//Log.i(TAG,"locked");
				
				// Download file from storage
				drive.download(getApplicationContext(), lf.get(i).getId());
				
				// Parse it
				JavaParser parser = new JavaParser();
				parser.setFile(getFilesDir() + "/" + lf.get(i).getTitle());
				
				Project proj = parser.getProject();
				ArrayList<Task> remoteTasks = parser.getTasks(proj);
				ArrayList<TaskHistory> remoteHistory = parser.getHistory();
				
				// Delete temp file from mobile
				java.io.File f = new java.io.File(getFilesDir() + "/" + lf.get(i).getTitle());
				f.delete();
				
				
				Log.i(TAG,"Projekt id " + proj.getId() + " name " + proj.getTitle());
				
				/*for (int j = 0; j < remoteTasks.size(); j++)  {
					Log.i(TAG,"ID: " + remoteTasks.get(j).getId());
					Log.i(TAG,"Task: " + remoteTasks.get(j).getTitle());
					Log.i(TAG,"Desc: " + remoteTasks.get(j).getDescription());
				}*/
				
				projdatasource.open();
				projdatasource.saveProject(proj);
				projdatasource.close();
				
				// add project to list of remote projects
				remoteProjects.add(proj);
				
				// load remote tasks of this project
				datasource = new TasksDataSource(getApplicationContext());
				datasource.open();
				cursor = datasource.getProjectTasksCursor(proj.getId());
				
				if (cursor != null) {							
					while (cursor.moveToNext()) {
						
						//Log.i(TAG, "CURSOR: pos " + cursor.getPosition() + " size " + cursor.getCount());
						Task task = new Task(cursor);
						//Log.i(TAG, "po tasku");
						
						for (int j = 0; j < remoteTasks.size(); j++) {
							if (task.getId().equals(remoteTasks.get(j).getId())) {
								// SYNCHRONIZATION LOGIC BETWEEN LOCAL AND REMOTE STORAGE
								Log.i(TAG,"Twoway sync : " + task.getTitle());
								Task result = twoWaySync(task, remoteTasks.get(j));
								//TasksModelService.getInstance().saveTask(result);
								
								// remote task was processed, delete it from array
								remoteTasks.remove(j);
								j--;
								// go to next local task
								break;
							} else {
								if (j == remoteTasks.size() - 1) {
									// Tasks are not in remote, but only in local
									// Project is dirty
									dirtyProjects.add(task.getProject());
									dirtyProjectsHistories.add(remoteHistory);
								}
							}
						}
						
						
					}
				}
				
				// Create tasks, that are not in local, but only in remote
				for (int j = 0; j < remoteTasks.size(); j++) {
					Task newtask = new Task(remoteTasks.get(j).getId(),
							remoteTasks.get(j).getTitle(),
							remoteTasks.get(j).getDescription(),
							remoteTasks.get(j).getDate(),
							remoteTasks.get(j).getPriority(),
							remoteTasks.get(j).getProject(),
							remoteTasks.get(j).getContext(),
							remoteTasks.get(j).isCompleted());
					
					datasource.saveTask(newtask);
				}
				
				
				
				datasource.close();
				
				
				
				
				//drive.unlock(lf.get(0).getId());
				
				
			} // end of remote projects cycle
			
			
			// SYNCHRONIZATION STAGE TWO - from local to remote 
			
			ArrayList<Project> localProjects = new ArrayList<Project>();
			
			// get list of local projects
			projdatasource.open();
			cursor = projdatasource.getAllProjectsCursor();
			cursor.moveToPrevious();
						
			while (cursor.moveToNext()) {
				Project p = new Project(cursor);
				localProjects.add(p);
			}
			
			projdatasource.close();
			
			// filter completed projects away (from stage 1)			
			for (int i = 0; i < localProjects.size(); i++) {
				for (int j = 0; j < remoteProjects.size(); j++) {
					//Log.i(TAG, "Porovnani " + p.getId() + " " + remoteProjects.get(k).getId());
					if (localProjects.get(i).getId().equals(remoteProjects.get(j).getId())) {
						localProjects.remove(i);
						i--;
						break;
					}
				}
			}
			
			int posOfDirtyProjects = localProjects.size();
			
			// add dirty projects to local
			localProjects.addAll(dirtyProjects);
			
			// Send local projects to remote
			for (int i = 0; i < localProjects.size(); i++) {	
				
					if (i >= posOfDirtyProjects) {
						Log.i(TAG, "Dirty project: " + localProjects.get(i).getTitle());
					} else {
						Log.i(TAG, "Only local project: " + localProjects.get(i).getTitle());
					}
					
					// write all the tasks from project to remote storage
					datasource = new TasksDataSource(getApplicationContext());
					datasource.open();
					
					// make history for new project, or recycle for dirty projects
					ArrayList<TaskHistory> history;
					if (localProjects.get(i).getHistory() != null) history = localProjects.get(i).getHistory(); 
					else history = new ArrayList<TaskHistory>();
					
					
					Cursor cursor2 = datasource.getProjectTasksCursor(localProjects.get(i).getId());
					ArrayList<Task> tasklist = new ArrayList<Task>();
					while(cursor2.moveToNext()) {
						Task addtask = new Task(cursor2);
						tasklist.add(addtask);
						
//						TaskHistory hist = new TaskHistory();
//						hist.setAuthor(mAccountName);
//						hist.setTimeStamp(new DateTime().toString());
//						hist.setTaskId(addtask.getId());
//						
//						hist.addChange(const_title, "", (addtask.getTitle() != null) ? addtask.getTitle() : "");
//						hist.addChange(const_description, "", (addtask.getDescription() != null) ? addtask.getDescription() : "");
//						hist.addChange(const_context, "", (addtask.getContext() != null) ? addtask.getContext() : "");
//						hist.addChange(const_date, "", addtask.getDate().toString());
//						hist.addChange(const_priority, "", Integer.toString(addtask.getPriority()));
//						hist.addChange(const_project, "", addtask.getProject().getId());
//						if (addtask.isCompleted()) hist.addChange(const_completed, "", "yes");
//						else hist.addChange(const_completed, "", "no");
//						
//						history.add(hist);
						
						ArrayList<TaskHistory> histories = getTaskHistories(addtask);
						history.addAll(histories);
						
					}
					
					datasource.close();
					
					
					
					
					
					
					// call serializer and uploader
					JavaParser parser = new JavaParser();
					parser.setProject(localProjects.get(i));
					parser.setTasks(tasklist);
					parser.setHistory(history);
					try {
						parser.writeFile(getApplicationContext(), getFilesDir() + "/" + localProjects.get(i).getId());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					drive.upload(getApplicationContext(), localProjects.get(i).getTitle() + "-" + localProjects.get(i).getId() + ".nextproj.html", localProjects.get(i).getId());
					
			}
				
			
			
			
			
			
			
			List<File> lsf = drive.listShared(getApplicationContext());
			if (lsf != null) {
				for (int i = 0; i < lsf.size(); i++) {
					Log.i(TAG,"SharedFile: " + lsf.get(i).getTitle());
					drive.move(lsf.get(i).getId());
				}
			
				returnObject ret = new returnObject(lsf.size());
				return ret;

			}
			else return null;
		}


		@Override
		protected void onPostExecute(Object param) {
			super.onPostExecute(param);
			
			if (param == null)  {
				Log.i(TAG, "Bad username");
				return;
			}
			if (param != null)	{
				if (((returnObject)param).sharedCount > 0) {
					displaySharedNotification(((returnObject)param).sharedCount);
				}
			}
			
			// Plan next synchronization
			// TODO: Variable interval
			AlarmReceiver alarm = new AlarmReceiver(getApplicationContext(), 20);
			//alarm.run();
			
			//Log.i(TAG, "Killing SyncService.");
			//stopSelf();
		}
	}
	
	
	/**
	 * Compares two tasks and returns newer
	 */
	public Task twoWaySync(Task first, Task second) {
		ArrayList<TaskHistory> hist1 = first.getProject().getHistory();
		ArrayList<TaskHistory> hist2 = second.getProject().getHistory();
		
		TaskHistory last1 = null;
		TaskHistory last2 = null;
		
		if (hist1 != null)
			for (int i = 0; i < hist1.size(); i++) {
				if (hist1.get(i).getTaskId().equals(first.getId()))
					if ((last1 == null) || (Long.parseLong(last1.getTimeStamp()) < Long.parseLong(hist1.get(i).getTimeStamp())))
						last1 = hist1.get(i);
			}
		
		if (hist2 != null)
			for (int i = 0; i < hist2.size(); i++) {
				if (hist2.get(i).getTaskId().equals(second.getId()))
					if ((last2 == null) || (Long.parseLong(last2.getTimeStamp()) < Long.parseLong(hist2.get(i).getTimeStamp())))
						last2 = hist2.get(i);
			}
		
		// TODO: Merge histories
		
		if ((last1 != null) && (last2 != null)) {
			String time1 = last1.getTimeStamp();
			String time2 = last2.getTimeStamp();
			
			if (Long.decode(time1) < Long.decode(time2)) {
				Log.i(TAG, "SECOND");
				return second;
			}
			else {
				Log.i(TAG, "FIRST");
				return first;
			}
		}
		
		if (last1 == null) {
			Log.i(TAG, "SECOND");
			return second;
		}
		
		Log.i(TAG, "FIRST");
		return first;
	}
	
	
	/**
	 * Get history of one Task from project
	 */
	
	ArrayList<TaskHistory> getTaskHistories(Task task) {
		ArrayList<TaskHistory> result = new ArrayList<TaskHistory>();
		
		Project proj = task.getProject();
		for (int i = 0; i < proj.getHistory().size(); i++) {
			if (proj.getHistory().get(i).getTaskId().equals(task.getId()))
				result.add(proj.getHistory().get(i));
		}
		
		return result;
	}
	

	/*
	 * Starts synchronization
	 */
	public void synchronize() {
		
		SynchronizeClass cls = new SynchronizeClass();
		cls.execute();
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
		
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.menu_next)
				.setContentTitle(title).setContentText(content)
				.setNumber(count).setTicker(ticker).setAutoCancel(true);

		Intent resultIntent = new Intent(this, MainActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		int notid = 0;
		mNotificationManager.notify(notid, mBuilder.build());

	}


	

}
