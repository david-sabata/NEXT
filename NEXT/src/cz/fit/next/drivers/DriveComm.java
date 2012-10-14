package cz.fit.next.drivers;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

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
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveRequest;
import com.google.api.services.drive.model.FileList;

import cz.fit.next.services.SyncServiceCallback;

public class DriveComm {

	private String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive";
	private String API_KEY = "457762972644.apps.googleusercontent.com";
	private String FOLDER_NAME = "NEXT";

	/* Constants to identify activities called by startActivityForResult */
	public int CHOOSE_ACCOUNT = 100;

	private String mAccountName = null;
	private String mAuthToken = null;
	private Drive mService = null;


	// private Activity mMainActivity = null;



	/*
	 * Performs Google Drive authorization, starts asynctask for this
	 */
	public void authorize(String username, Activity main, SyncServiceCallback cb) {
		Object[] params = new Object[2];
		params[0] = main;
		params[1] = cb;

		mAccountName = username;

		Log.e("NEXT Drive", "zde");
		AuthorizeGoogleDriveClass auth = new AuthorizeGoogleDriveClass();
		auth.execute(params);

		// TODO: Callback
	}


	/*
	 * Returns id of application folder
	 */
	private String getFileList(String folderName) {
		return null;
	}



	/*
	 * Asynctask provides authorization.
	 */
	private class AuthorizeGoogleDriveClass extends AsyncTask<Object, Void, SyncServiceCallback> {
		@Override
		protected SyncServiceCallback doInBackground(Object... params) {
			Log.e("NEXT Drive", "Starting async");
			Account account = new Account(mAccountName, "com.google");
			mAuthToken = getGoogleAccessToken((Activity) params[0], account);
			Log.e("NEXT Drive", "Token is: " + mAuthToken);

			return (SyncServiceCallback) params[1];

		}


		@Override
		protected void onPostExecute(SyncServiceCallback param) {
			super.onPostExecute(param);

			// Build the service object
			mService = buildService(mAuthToken, API_KEY);
			Log.e("NEXT Drive", "Connection initiated.");
			if (mAuthToken != null) {
				param.authorizeDone();
			}
		}
	}


	/*
	 * Gets Access Token to Google Drive
	 */
	private String getGoogleAccessToken(Activity main, Account account) {
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


	/*
	 * Creates object which represents Drive Service
	 */
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


	/*
	 * Returns id of application folder
	 */
	private String getFolderIdPrivate(String folderName) {

		class Async extends AsyncTask<String, Void, String> {

			@Override
			protected String doInBackground(String... params) {

				FileList flist = null;
				Files.List request = null;
				String nextDirId = new String();

				try {
					request = mService.files().list();
					String q = "mimeType = 'application/vnd.google-apps.folder' and title = '" + FOLDER_NAME + "'";
					request = request.setQ(q);

					flist = request.execute();
					if (flist.size() > 0) {
						nextDirId = flist.getItems().get(0).getId();
						Log.e("NEXT Drive", "ID of NEXT directory is: " + nextDirId);
					} else // Create new NEXT folder
					{
						Log.e("NEXT Drive", "NEXT folder not found.");

						// TODO: Create new directory
					}

				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				return nextDirId;

			}


			@Override
			protected void onPostExecute(String param) {
				super.onPostExecute(param);
			}
		}

		Async task = new Async();
		String[] params = new String[1];
		params[0] = folderName;
		task.execute(params);
		String res = null;
		try {
			res = task.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}

}