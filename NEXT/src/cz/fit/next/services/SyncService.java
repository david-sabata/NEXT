package cz.fit.next.services;

import android.accounts.Account;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;

import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.drivers.DriveComm;



public class SyncService extends Service {

	private String TAG = "SyncService";
	private static final String PREF_FILE_NAME = "SyncServicePref";
	private static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";

	// Notification types
	private static final int NOTIFICATION_NEW_SHARED = 100;


	// GDrive Driver
	private DriveComm drive;

	private boolean mAuthorized = false;
	private String mAccountName;


	/*
	 * Singleton interface
	 */
	private static SyncService mInstance;


	public static SyncService getInstance() {
		return mInstance;
	}



	/*
	 * Bound service interface
	 */
	public class SyncServiceBinder extends Binder {
		public SyncService getService() {
			// Log.e(TAG, "getService");
			return SyncService.this;
		}
	};

	private final IBinder mBinder = new SyncServiceBinder();


	@Override
	public IBinder onBind(Intent arg0) {
		// Log.e(TAG, "onBind");
		return mBinder;
	}


	public void onCreate() {
		mInstance = this;
		// Log.e(TAG, "onCreate");
		drive = new DriveComm();

		// Reload stored preferences
		SharedPreferences settings = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
		mAccountName = settings.getString(PREF_ACCOUNT_NAME, null);

		if (mAccountName != null) {
			Log.e(TAG, "Auth");
			authorize(mAccountName, null);
		}
	}


	public boolean onUnbind(Intent intent) {
		mInstance = null;
		return false;
	}


	/**
	 * Opens activity for choose Google account
	 * 
	 * @param username
	 *            Stored username, will not display account chooser if specified
	 */
	public void chooseGoogleAccount(Activity act) {

		String username = null;

		Account account = null;
		if (username != null) // Logged on
		{
			account = new Account(username, "com.google");
		} else {
			account = null;
		}


		class UserRecover implements Runnable {

			private Account account;
			private Activity activity;


			public UserRecover(Activity act, Account acc) {
				activity = act;
				account = acc;
			}


			public void run() {
				int CHOOSE_ACCOUNT = 100;

				String accList[] = new String[1];
				accList[0] = "com.google";

				this.activity.startActivityForResult(
						AccountPicker.newChooseAccountIntent(account, null, accList, false, null, null, null, null),
						CHOOSE_ACCOUNT);
			}

		}

		act.runOnUiThread(new UserRecover(act, account));
	}


	public void authorize(String accountName, Activity act) {
		authorizeDoneHandler ad = new authorizeDoneHandler();
		drive.authorize(accountName, act, getApplicationContext(), this, ad);
	}


	public class authorizeDoneHandler implements SyncServiceCallback { 
	
		public void Done(Object param) {
			mAuthorized = true;
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
			// intervalSynchronize();
		}

		
	}


	public void synchronize() {
		//drive.synchronize(this);
	}


	public void displaySharedNotification(int count) {
		displayStatusbarNotification(NOTIFICATION_NEW_SHARED, count);
	}


	private void intervalSynchronize() {
		class Async extends AsyncTask<Void, Void, Void> {

			@Override
			protected Void doInBackground(Void... params) {

				return null;
			}


			@Override
			protected void onPostExecute(Void param) {
				super.onPostExecute(param);

			}
		}

		Async task = new Async();
		task.execute();
	}



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
