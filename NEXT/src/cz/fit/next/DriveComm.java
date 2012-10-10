package cz.fit.next;

import java.io.IOException;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveRequest;

public class DriveComm {

	private String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive";
	private String API_KEY = "457762972644.apps.googleusercontent.com";

	/* Constants to identify activities called by startActivityForResult */
	public int CHOOSE_ACCOUNT = 100;

	private boolean mAuthorized = false;
	private String mAccountName = null;
	private String mAuthToken = null;
	private Drive mService = null;


	// private Activity mMainActivity = null;


	/*
	 * Returns true if drive authorization process is done
	 */
	public boolean isAuthorized() {
		return mAuthorized;
	}


	/*
	 * Performs Google Drive authorization, starts asynctask for this
	 */
	public void authorize(String username, Activity main) {
		Object[] params = new Object[2]; // 0 = main, 1 = parent
		params[0] = main;
		params[1] = this;

		mAccountName = username;

		// Log.e("NEXT Drive", "zde");
		AuthorizeGoogleDriveClass auth = new AuthorizeGoogleDriveClass();
		auth.execute(params);
	}


	/*
	 * Asynctask provides authorization.
	 */
	private class AuthorizeGoogleDriveClass extends AsyncTask<Object, Void, DriveComm> {
		@Override
		protected DriveComm doInBackground(Object... params) {
			// Log.e("NEXT Drive", "Starting async");
			DriveComm parent = (DriveComm) params[1];
			Account account = new Account(mAccountName, "com.google");
			mAuthToken = getGoogleAccessToken((Activity) params[0], parent, account);
			Log.e("NEXT Drive", "Token is: " + mAuthToken);

			return null;

		}


		@Override
		protected void onPostExecute(DriveComm param) {
			super.onPostExecute(param);

			// Build the service object
			mService = buildService(mAuthToken, API_KEY);
			Log.e("NEXT Drive", "Connection initiated.");
			if (mAuthToken != null)
				mAuthorized = true;

		}
	}


	private String getGoogleAccessToken(Activity main, DriveComm parent, Account account) {
		String retval = null;

		try {
			retval = GoogleAuthUtil.getToken(main.getBaseContext(), account.name, AUTH_TOKEN_TYPE);
		} catch (UserRecoverableAuthException e) {
			Log.e("NEXT Drive", "ERROR in authentication");
			Log.e("NEXT Drive", e.toString());
			Intent intent = e.getIntent();

			class UserRecover implements Runnable {

				private Activity activity;
				private Intent intent;


				public UserRecover(Activity act, Intent intnt) {
					activity = act;
					intent = intnt;
				}


				public void run() {
					this.activity.startActivityForResult(this.intent, CHOOSE_ACCOUNT);
				}

			}

			main.runOnUiThread(new UserRecover(main, intent));


		} catch (IOException e) {
			e.printStackTrace();
		} catch (GoogleAuthException e) {
			e.printStackTrace();
		}

		return retval;
	}


	private Drive buildService(final String AuthToken, final String ApiKey) {
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();

		Drive.Builder b = new Drive.Builder(httpTransport, jsonFactory, null);
		b.setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {

			public void initialize(JsonHttpRequest request) throws IOException {
				DriveRequest driveRequest = (DriveRequest) request;
				driveRequest.setPrettyPrint(true);
				driveRequest.setKey(ApiKey);
				driveRequest.setOauthToken(AuthToken);
			}
		});

		return b.build();
	}

}