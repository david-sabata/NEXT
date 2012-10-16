package cz.fit.next.drivers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveRequest;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import cz.fit.next.services.SyncService;

public class DriveComm {

	private String TAG = "GDrive Driver";

	private String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive";
	private String API_KEY = "457762972644.apps.googleusercontent.com";
	private String FOLDER_NAME = "NEXT";

	/* Constants to identify activities called by startActivityForResult */
	public int CHOOSE_ACCOUNT = 100;

	private String mAccountName = null;
	private String mAuthToken = null;
	private Drive mService = null;
	private SyncService mSyncService = null;


	// private Activity mMainActivity = null;



	/*
	 * Performs Google Drive authorization, starts asynctask for this
	 */
	public void authorize(String username, Activity main, Context appcontext, SyncService syncserv) {

		mAccountName = username;
		mSyncService = syncserv;

		auth(main, appcontext);
	}


	/*
	 * Starts synchronization
	 */
	public void synchronize(SyncService srv) {
		mSyncService = srv;

		// Auth token may be expired, lets renew it
		reauth();
	}


	private void synchronizeAfterReauth() {
		// getDriveFolderFileList(FOLDER_NAME);
		// getSharedFileList(FOLDER_NAME);

		uploadFile("battery_history.txt");
	}


	/*
	 * Starts reauthorization
	 */
	private void reauth() {
		AccountManager am = AccountManager.get(mSyncService.getApplicationContext());
		am.invalidateAuthToken("com.google", null);

		ReauthCallbackClass cls = new ReauthCallbackClass();

		Object[] params = new Object[3];
		params[0] = null;
		params[1] = mSyncService.getApplicationContext();
		params[2] = cls;

		AuthorizeGoogleDriveClass auth = new AuthorizeGoogleDriveClass();
		auth.execute(params);


	}


	private void auth(Activity main, Context appcontext) {

		AuthorizationCallbackClass cls = new AuthorizationCallbackClass();

		Object[] params = new Object[3];
		params[0] = main;
		params[1] = appcontext;
		params[2] = cls;

		AuthorizeGoogleDriveClass auth = new AuthorizeGoogleDriveClass();
		auth.execute(params);
	}

	/*
	 * Authorization callback classes
	 */

	interface AuthorizationCallback {
		public void authorized();
	}

	private class AuthorizationCallbackClass implements AuthorizationCallback {
		public void authorized() {
			Log.i(TAG, "Authorized Callback");
			mSyncService.authorizeDone(mAccountName);
		}
	}

	private class ReauthCallbackClass implements AuthorizationCallback {
		public void authorized() {
			Log.i(TAG, "Reauthorized Callback");

			synchronizeAfterReauth();
		}
	}

	/*
	 * Asynctask provides authorization.
	 */
	private class AuthorizeGoogleDriveClass extends AsyncTask<Object, Void, AuthorizationCallback> {
		@Override
		protected AuthorizationCallback doInBackground(Object... params) {
			Log.e(TAG, "Starting async");
			Account account = new Account(mAccountName, "com.google");
			mAuthToken = getGoogleAccessToken((Activity) params[0], (Context) params[1], account);
			Log.e(TAG, "Token is: " + mAuthToken);

			return (AuthorizationCallback) params[2];

		}


		@Override
		protected void onPostExecute(AuthorizationCallback param) {
			super.onPostExecute(param);

			// Build the service object
			mService = buildService(mAuthToken, API_KEY);
			Log.e(TAG, "Connection initiated.");
			if (mAuthToken != null) {
				param.authorized();
			}
		}
	}


	/*
	 * Gets Access Token to Google Drive
	 */
	private String getGoogleAccessToken(Activity main, Context appcontext, Account account) {
		String retval = null;

		try {
			retval = GoogleAuthUtil.getToken(appcontext, account.name, AUTH_TOKEN_TYPE);
		} catch (UserRecoverableAuthException e) {
			Log.e(TAG, "ERROR in authentication");
			Log.e(TAG, e.toString());
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
			if (main != null)
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
	 * Returns filelist of application folder
	 */
	private void getDriveFolderFileList(String folderName) {

		class Async extends AsyncTask<String, Void, List<File>> {

			@Override
			protected List<File> doInBackground(String... params) {

				FileList flist = null;
				Files.List request = null;
				String nextDirId = new String();
				List<File> res = new ArrayList<File>();

				try {
					// Determine the folder id
					request = mService.files().list();
					String q = "mimeType = 'application/vnd.google-apps.folder' and title = '" + FOLDER_NAME + "'";
					request = request.setQ(q);

					flist = request.execute();
					if (flist.size() > 0) {
						nextDirId = flist.getItems().get(0).getId();
						Log.e(TAG, "ID of NEXT directory is: " + nextDirId);
					} else // Create new NEXT folder
					{
						Log.e(TAG, "NEXT folder not found.");

						// TODO: Create new directory
					}


					// Download filelist
					flist = null;
					request = null;
					;
					request = mService.files().list();
					q = "'" + nextDirId + "' in parents";
					// q = "not 'me' in owners";
					request = request.setQ(q);

					do {
						flist = request.execute();
						res.addAll(flist.getItems());
						request = request.setPageToken(flist.getNextPageToken());
						Log.e(TAG, "New page token: " + flist.getNextPageToken());
					} while (flist.getNextPageToken() != null && flist.getNextPageToken().length() > 0);

					Log.e(TAG, "Pocet souboru: " + res.size());

				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				return res;

			}


			@Override
			protected void onPostExecute(List<File> param) {
				super.onPostExecute(param);

				getDriveFolderFileListDone(param);
			}
		}

		Async task = new Async();
		String[] params = new String[1];
		params[0] = folderName;
		task.execute(params);

	}


	private void getDriveFolderFileListDone(List<File> list) {
		for (int i = 0; i < list.size(); i++) {
			File f = list.get(i);
			Log.i(TAG, "Downloading " + f.getId());
			downloadFile(f.getId());
		}
	}



	/*
	 * Returns list of shared files, which are not in folder
	 */
	private void getSharedFileList(String folderName) {

		class Async extends AsyncTask<String, Void, List<File>> {

			@Override
			protected List<File> doInBackground(String... params) {

				FileList flist = null;
				Files.List request = null;
				List<File> res = new ArrayList<File>();

				try {

					// Download filelist
					flist = null;
					request = null;

					request = mService.files().list();
					String q = "not 'me' in owners and not '" + params[0] + "' in parents";
					// Log.i(TAG, q);
					request = request.setQ(q);

					do {
						flist = request.execute();
						res.addAll(flist.getItems());
						request = request.setPageToken(flist.getNextPageToken());
						Log.e(TAG, "New page token: " + flist.getNextPageToken());
					} while (flist.getNextPageToken() != null && flist.getNextPageToken().length() > 0);

					Log.e(TAG, "Pocet souboru: " + res.size());

				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				return res;

			}


			@Override
			protected void onPostExecute(List<File> param) {
				super.onPostExecute(param);

				inspectSharedFiles(param);
			}
		}

		Async task = new Async();
		String[] params = new String[1];
		params[0] = folderName;
		task.execute(params);

	}


	private void inspectSharedFiles(List<File> list) {
		for (int i = 0; i < list.size(); i++) {
			File f = list.get(i);
			Log.e(TAG, "Filename: " + f.getTitle() + ", mime: " + f.getMimeType());
		}

		if (list.size() > 0)
			mSyncService.displaySharedNotification(list.size());

	}



	private void downloadFile(String id) {

		class Async extends AsyncTask<String, Void, Void> {

			@Override
			protected Void doInBackground(String... params) {

				File dfile;
				try {
					dfile = mService.files().get(params[0]).execute();
					String token = mService.files().get(params[0]).getOauthToken();
					String name = dfile.getTitle();

					Log.i(TAG, "URL: " + dfile.getDownloadUrl());

					if (dfile.getDownloadUrl() != null && dfile.getDownloadUrl().length() > 0) {
						OutputStream os = mSyncService.openFileOutput(name, Context.MODE_PRIVATE);

						// HttpResponse resp = mService.getRequestFactory()
						// .buildGetRequest(new
						// GenericUrl(dfile.getDownloadUrl())).execute();

						HttpClient client = new DefaultHttpClient();
						HttpGet get = new HttpGet(dfile.getDownloadUrl());
						get.setHeader("Authorization", "Bearer " + token);
						org.apache.http.HttpResponse response = client.execute(get);


						InputStream is = response.getEntity().getContent();

						byte[] buffer = new byte[1024];
						int bytesRead;
						while ((bytesRead = is.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}

						os.close();

					}



				} catch (GoogleJsonResponseException e) {
					GoogleJsonError error = e.getDetails();

					Log.e(TAG, "Error code" + error.getCode());
					Log.e(TAG, "Error message: " + error.getMessage());
					// More error information can be retrieved with
					// error.getErrors().
				} catch (HttpResponseException e) {
					// No Json body was returned by the API.
					Log.e(TAG, "HTTP Status code: " + e.getStatusCode());
					Log.e(TAG, "HTTP Reason: " + e.getLocalizedMessage());

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;

			}


			@Override
			protected void onPostExecute(Void param) {
				super.onPostExecute(param);
				Log.e(TAG, "File downloaded.");
			}
		}

		Async task = new Async();
		String[] params = new String[1];
		params[0] = id;
		task.execute(params);

	}



	private void uploadFile(String name) {

		class Async extends AsyncTask<String, Void, Void> {

			@Override
			protected Void doInBackground(String... params) {
				FileList flist = null;
				Files.List request = null;
				String nextDirId = new String();
				List<File> res = new ArrayList<File>();

				try {
					request = mService.files().list();

					String q = "mimeType = 'application/vnd.google-apps.folder' and title = '" + FOLDER_NAME + "'";
					request = request.setQ(q);

					flist = request.execute();
					if (flist.size() > 0) {
						nextDirId = flist.getItems().get(0).getId();
						Log.e(TAG, "ID of NEXT directory is: " + nextDirId);
					} else // Create new NEXT folder
					{
						Log.e(TAG, "NEXT folder not found.");

						// TODO: Create new directory
					}


					// Upload file

					// File's metadata.
					File body = new File();
					body.setTitle(params[0]);
					// body.setDescription("");
					body.setMimeType("text/plain");

					// File's content.
					java.io.File fileContent = new java.io.File(mSyncService.getFilesDir() + "/" + params[0]);
					FileContent mediaContent = new FileContent("text/plain", fileContent);

					mService.files().insert(body, mediaContent).execute();



				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;

			}


			@Override
			protected void onPostExecute(Void param) {
				super.onPostExecute(param);
				Log.e(TAG, "File uploaded.");
			}
		}

		Async task = new Async();
		String[] params = new String[1];
		params[0] = name;
		task.execute(params);

	}

}