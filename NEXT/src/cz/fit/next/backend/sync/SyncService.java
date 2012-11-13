package cz.fit.next.backend.sync;

import java.util.List;


import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import cz.fit.next.backend.sync.drivers.GDrive;




public class SyncService extends Service {

	private String TAG = "NEXT SyncService";
	private static final String PREF_FILE_NAME = "SyncServicePref";
	private static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";

	// Notification types
	private static final int NOTIFICATION_NEW_SHARED = 100;

	// Self
	private static SyncService sInstance;
	
	// Flag to determine, if service is connected to some activity, or has been restarted by Android
	// 1 = connected
	int ActivityPresent = 1;

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
			Log.e(TAG, "Auth");
			authorize(mAccountName, null);
		}
		
		// if button pressed ask for username
		if (intent != null) {
			Bundle b = intent.getExtras();
			if (b.getInt("buttonPressed") == 1) {
				
				Intent i = new Intent(this,LoginActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(i);
				
			}
			
		}
		
		//return START_STICKY;
		return START_NOT_STICKY;
	}


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
	
	
	/*
	 * Do first-time authorization after service starts
	 */
	public void authorize(String accountName, Activity act) {
		authorizeDoneHandler ad = new authorizeDoneHandler();
		drive.authorize(accountName, act, getApplicationContext(), this, ad);
	}


	public class authorizeDoneHandler implements SyncServiceCallback { 
	
		public void Done(Object param, Boolean status) {
			if (status == true) {
				//mAuthorized = true;
				mAccountName = (String) param;
				Log.e(TAG, "Authorized");
		
				// save username into permanent storage
				SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(PREF_ACCOUNT_NAME, mAccountName);
				editor.commit();
		
				Context context = getApplicationContext();
				CharSequence text = "Logged into GDrive as " + mAccountName;
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
				
				synchronize();
			} else {
				Context context = getApplicationContext();
				CharSequence text = "Synchronization not available";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		}

		
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
			
			drive.initSync(getApplicationContext());
			
			List<File> lf = drive.list(getApplicationContext(), getInstance(), null);
			for (int i = 0; i < lf.size(); i++) {
				Log.i(TAG,"File: " + lf.get(i).getTitle());
			}
			
			
			
			//drive.lock(lf.get(0).getId());
			//Log.i(TAG,"locked");
			
			// DO SOME SYNCHRONIZATION STUFF
			
			//drive.upload(getApplicationContext(), null, "testovka", null);
			
						
			//drive.unlock(lf.get(0).getId());
			
			
			
			List<File> lsf = drive.listShared(getApplicationContext(), null);
			
			for (int i = 0; i < lsf.size(); i++) {
				Log.i(TAG,"SharedFile: " + lsf.get(i).getTitle());
			}
			
			
			
			returnObject ret = new returnObject(lsf.size());
			return ret;

		}


		@Override
		protected void onPostExecute(Object param) {
			super.onPostExecute(param);

			if (((returnObject)param).sharedCount > 0) {
				displaySharedNotification(((returnObject)param).sharedCount);
			}
		}
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
			ticker = "New shared files found.";
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
