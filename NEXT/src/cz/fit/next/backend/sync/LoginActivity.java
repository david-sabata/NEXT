package cz.fit.next.backend.sync;


import preferences.SettingsFragment;

import com.google.android.gms.common.AccountPicker;

import cz.fit.next.backend.SettingsProvider;


import android.accounts.AccountManager;
import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LoginActivity extends Activity {
	
	
	private static final String PREF_FILE_NAME = "SyncServicePref";
	private static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";
	
	SyncService mSyncService;
	String mAccountName;
	
	
	
	
	/* Constants to identify activities called by startActivityForResult */
	public int CHOOSE_ACCOUNT = 100;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int CHOOSE_ACCOUNT = 100;

		String accList[] = new String[1];
		accList[0] = "com.google";

		Intent i = getIntent();
		Bundle b = i.getExtras();
		
		if (b.getInt("login") == 1) {
			//Log.i("yy", "starting picker");
			startActivityForResult(
				AccountPicker.newChooseAccountIntent(null, null, accList, true, null, null, null, null),
				CHOOSE_ACCOUNT);
		}
		
	}

	/**
	 * Process result from called activity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("yy", "on activity result");
		if ((requestCode == CHOOSE_ACCOUNT) && (resultCode == RESULT_OK) && (data != null)) {
			//Log.i("yy", "on account activity result");
			mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			
			//AuthorizeGoogleDriveClass auth = new AuthorizeGoogleDriveClass();
    		//auth.execute((Void)null); 
			
			SettingsProvider sp = new SettingsProvider(getApplicationContext());
			sp.storeString(SettingsFragment.PREF_ACCOUNT_NAME, mAccountName);
			
			Intent i = new Intent(LoginActivity.this, SyncService.class);
			Bundle b = new Bundle();
			b.putInt("inAuth", 1);
			b.putString("accountName", mAccountName);
			i.putExtras(b);
			LoginActivity.this.startService(i);
			finish();

		} else {
			Log.e("NEXT", "Unexpected result.");
			Intent i = new Intent(this, SyncService.class);
			Bundle b = new Bundle();
			b.putInt("inAuth", -1);
			i.putExtras(b);
			this.startService(i);
			finish();
		}



	}
	
}
