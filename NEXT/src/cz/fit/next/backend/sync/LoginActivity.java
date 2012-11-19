package cz.fit.next.backend.sync;

import com.google.android.gms.common.AccountPicker;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LoginActivity extends Activity {
	
	SyncService mSyncService;
	
	/* Constants to identify activities called by startActivityForResult */
	public int CHOOSE_ACCOUNT = 100;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int CHOOSE_ACCOUNT = 100;

		String accList[] = new String[1];
		accList[0] = "com.google";

		startActivityForResult(
				AccountPicker.newChooseAccountIntent(null, null, accList, true, null, null, null, null),
				CHOOSE_ACCOUNT);
		
	}

	/**
	 * Process result from called activity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((requestCode == CHOOSE_ACCOUNT) && (resultCode == RESULT_OK) && (data != null)) {
			String accountName = new String();
			accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			
			// Execute asynctask with Google Drive Authorizing
			SyncService s = SyncService.getInstance();
			s.authorize(accountName, LoginActivity.this);
			
			//finish();

		} else {
			Log.e("NEXT", "Unexpected result.");
		}



	}
	
	
}
