package cz.fit.next.backend.sync;

import java.io.IOException;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class LoginActivity extends Activity {
	
	
	private String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive";
	
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
			Log.i("yy", "starting picker");
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
			Log.i("yy", "on account activity result");
			mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			
			AuthorizeGoogleDriveClass auth = new AuthorizeGoogleDriveClass();
    		auth.execute((Void)null); 

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
	
	private class AuthorizeGoogleDriveClass extends AsyncTask<Void,Void,String>
    {
    	@Override
    	protected String doInBackground(Void... arg0) {
    			Log.i("yy", "before auth");
    			Account account = new Account(mAccountName,"com.google");
    		    String authToken = getGoogleAccessToken(LoginActivity.this, account);
    		    Log.e("ii","Token is: " + authToken);
    		    return authToken;  			
    			
    		}
    	
    	@Override
    	protected void onPostExecute(String result)
    	{
    		super.onPostExecute(result);
    		
    		if (result != null) {
    			Intent i = new Intent(LoginActivity.this, SyncService.class);
    			Bundle b = new Bundle();
    			Log.i("yy", "post execute good");
    			b.putInt("inAuth", 1);
    			b.putString("accountName", mAccountName);
    			i.putExtras(b);
    			LoginActivity.this.startService(i);
    			
    			finish();
    		} else {
    			Intent i = new Intent(LoginActivity.this, SyncService.class);
    			Bundle b = new Bundle();
    			Log.i("yy", "post execute bad");
    			b.putInt("inAuth", -1);
    			i.putExtras(b);
    			LoginActivity.this.startService(i);
    			finish();
    		}
    	}
    	
    }	
    
    
    
    private String getGoogleAccessToken(Context context, Account account)
    {
    	String retval = null;
    	
    	try
    	{
    		retval = GoogleAuthUtil.getToken(context, account.name, AUTH_TOKEN_TYPE);
    	}
    	catch (UserRecoverableAuthException e)
    	{
    		Log.e("ii","ERROR in authentication");
    		Log.e("ii",e.toString());
    		Intent intent = e.getIntent();
    		LoginActivity.this.startActivityForResult(intent,CHOOSE_ACCOUNT);
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (GoogleAuthException e) {
			e.printStackTrace();
		}
    	
    	return retval;
    } 
	
	
}
