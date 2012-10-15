package cz.fit.next.services;

import android.accounts.Account;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;

import cz.fit.next.drivers.DriveComm;



public class SyncService extends Service implements SyncServiceCallback {

	private String TAG = "SyncService";
	private static final String PREF_FILE_NAME = "SyncServicePref";
	private static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";

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
		drive.authorize(accountName, act, getApplicationContext(), this);
	}


	public void authorizeDone(String username) {
		mAuthorized = true;
		mAccountName = username;
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
	}


	public void synchronize() {
		drive.synchronize();
	}



}
