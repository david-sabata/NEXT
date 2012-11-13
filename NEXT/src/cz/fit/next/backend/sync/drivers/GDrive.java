package cz.fit.next.backend.sync.drivers;

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

import cz.fit.next.backend.sync.SyncService;
import cz.fit.next.backend.sync.SyncServiceCallback;

public class GDrive {

	private String TAG = "GDrive Driver";

	private String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive";
	private String API_KEY = "457762972644.apps.googleusercontent.com";
	private String FOLDER_NAME = "NEXT";
	private String LOCK_PREFIX = "nextlock-";

	/* Constants to identify activities called by startActivityForResult */
	public int CHOOSE_ACCOUNT = 100;

	private String mAccountName = null;
	private String mAuthToken = null;
	private Drive mService = null;
	private SyncService mSyncService = null;
	private String mAppFolder = null;
	


	
	/**
	 * Initializes synchronization, performs reauth and 
	 * determines id of application folder
	 */
	public void initSync(Context appcontext) {
		reauth(appcontext);
		mAppFolder = getAppFolderId();
	}

		
	/**
	 * Public method for first authorization after service start
	 */
	public void authorize(String username, Activity main, Context appcontext, SyncService syncserv, SyncServiceCallback cb) {
		mAccountName = username;
		mSyncService = syncserv;
		//mCallback = cb;
		
		Object[] params = new Object[3];
		params[0] = main;
		params[1] = appcontext;
		params[2] = cb;

		AuthorizeGoogleDriveClass auth = new AuthorizeGoogleDriveClass();
		auth.execute(params);
	}
	
	/**
	 * Gets list of files in application folder
	 */
	public List<File> list(Context appcontext, SyncService syncserv, SyncServiceCallback cb) {
		//mSyncService = syncserv;
		//mCallback = cb;
		
		List<File> res = getFileList(mAppFolder);
		return res;		
		
	}
	
	/**
	 * Gets list of files in shared folder
	 */
	public List<File> listShared(Context appcontext, SyncServiceCallback cb) {
		//mSyncService = syncserv;
		//mCallback = cb;
		//reauth(appcontext);
		List<File> res = getSharedFileList(mAppFolder);
		return res;
		
	}
	
	/**
	 * Locks file with given id
	 */
	public Boolean lock(String ident) {
		uploadFile(LOCK_PREFIX + ident,null, mAppFolder);
		
		// TODO: Wait some time and check older locks
		return true;
	}
	
	/**
	 * Download file with given filename to local storage
	 */
	public void download(Context appcontext, SyncServiceCallback cb, String filename) {
		downloadFile(filename, mAppFolder);		
		
	}
	
	/**
	 * Upload file with given local name to storage with other name
	 */
	public void upload(Context appcontext, SyncServiceCallback cb, String filename, String localname) {
		uploadFile(filename, localname, mAppFolder);
		
	}
	
	/**
	 * Unlocks file with given id
	 */
	public void unlock(String ident) {
		String id = getFileIdByName(LOCK_PREFIX + ident);
		deleteFile(id);
	}
	

	
	
	
	/**************************************************/
	/*              ASYNCTASK CLASSES                 */
	/**************************************************/

	/**
	 * Asynctask provides authorization.
	 */
	private class AuthorizeGoogleDriveClass extends AsyncTask<Object, Void, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			Log.e(TAG, "Starting async");
			Account account = new Account(mAccountName, "com.google");
			mAuthToken = getGoogleAccessToken((Activity) params[0], (Context) params[1], account);
			Log.e(TAG, "Token is: " + mAuthToken);

			return params[2];

		}


		@Override
		protected void onPostExecute(Object param) {
			super.onPostExecute(param);

			// Build the service object
			mService = buildService(mAuthToken, API_KEY);
			Log.e(TAG, "Connection initiated.");
			if (mAuthToken != null) {
				((SyncServiceCallback)param).Done(mAccountName, true);
			} else {
				((SyncServiceCallback)param).Done(mAccountName, false);
			}
		}
	}
	
	
	
	
	
	/***************************************/
	/*          HELPER FUNCTIONS           */
	/***************************************/
	
	/**
	 * Invalidates token and makes new one
	 */
	private void reauth(Context appcontext) {
		AccountManager am = AccountManager.get(mSyncService.getApplicationContext());
		am.invalidateAuthToken("com.google", null);

		Account account = new Account(mAccountName, "com.google");
		mAuthToken = getGoogleAccessToken(null, appcontext, account);
		Log.e(TAG, "Token is: " + mAuthToken);
		mService = buildService(mAuthToken, API_KEY);
	}


	/**
	 * Gets Access Token to Google Drive
	 */
	private String getGoogleAccessToken(Activity main, Context appcontext, Account account) {
		String retval = null;

		try {
			retval = GoogleAuthUtil.getToken(appcontext, account.name, AUTH_TOKEN_TYPE);
			// TODO: Catch network error
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
		
		// Finish login activity, if exists
		if (main != null) main.finish();

		return retval;
	}
	
	
	
	


	/**
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

	/**
	 * Returns id of app folder in storage 
	 * 
	 */
	private String getAppFolderId() {
		
		
		FileList flist = null;
		Files.List request = null;
		String nextDirId = new String();
		
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
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		return nextDirId;
		
	}
	
	
	/**
	 * Returns file id by its name
	 */
	private String getFileIdByName(String name) {
		
		String res = null;
		
		FileList flist = null;
		Files.List request = null;
				
		try {
						
			// Get File By Name
			request = mService.files().list();
			String q = "title = '" + name + "'";
			request = request.setQ(q);

			flist = request.execute();
		
			if (flist.getItems().size() > 0) {
				res = flist.getItems().get(0).getId();
			} else {
				res = null;
			}
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		return res;
	}
	

	/**
	 * Returns filelist of application folder
	 */
	private List<File> getFileList(String appFolder) {

				FileList flist = null;
				Files.List request = null;
		
				List<File> res = new ArrayList<File>();

				try {
					
					// Download filelist
					flist = null;
					request = null;
					;
					request = mService.files().list();
					String q = "'" + appFolder + "' in parents";
					// q = "not 'me' in owners";
					request = request.setQ(q);

					do {
						flist = request.execute();
						res.addAll(flist.getItems());
						request = request.setPageToken(flist.getNextPageToken());
						//Log.e(TAG, "New page token: " + flist.getNextPageToken());
					} while (flist.getNextPageToken() != null && flist.getNextPageToken().length() > 0);

					//Log.e(TAG, "Pocet souboru: " + res.size());

				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				return res;


	}


	/**
	 * Returns list of shared files, which are not in application folder
	 */
	private List<File> getSharedFileList(String appFolder) {

		FileList flist = null;
		Files.List request = null;
		List<File> res = new ArrayList<File>();

		try {

			// Download filelist
			flist = null;
			request = null;

			request = mService.files().list();
			String q = "not 'me' in owners and not '" + appFolder + "' in parents";
			// Log.i(TAG, q);
			request = request.setQ(q);

			do {
				flist = request.execute();
				res.addAll(flist.getItems());
				request = request.setPageToken(flist.getNextPageToken());
				//Log.e(TAG, "New page token: " + flist.getNextPageToken());
			} while (flist.getNextPageToken() != null && flist.getNextPageToken().length() > 0);

			//Log.e(TAG, "Pocet souboru: " + res.size());
			
			// Exclude files with bad names
			for (int i = 0; i < res.size(); i++) {
				String title = res.get(i).getTitle();
				if (!(title.contains(".nextproj.html"))) res.remove(i);
			}

		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		return res;

	}


		
	/*
	 * Downloads file
	 */
	private void downloadFile(String id, String appFolder) {

		File dfile;
		try {
			dfile = mService.files().get(id).execute();
			String token = mService.files().get(id).getOauthToken();
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


	}


	/**
	 * Uploads file from "localname" to "appfolder"/"name" on storage
	 */
	private void uploadFile(String name, String localname, String appFolder) {

				
		try {
			// TODO: Move to app folder

			// Upload file

			// File's metadata.
			File body = new File();
			body.setTitle(name);
			// body.setDescription("");
			body.setMimeType("text/plain");

			java.io.File fileContent = null;
			FileContent mediaContent = null;
			
			// File's content.
			if (localname != null) {
				fileContent = new java.io.File(mSyncService.getFilesDir() + "/" + localname);
				mediaContent = new FileContent("text/plain", fileContent);
				mService.files().insert(body,mediaContent).execute();
			} else {			

				mService.files().insert(body).execute();
			}



		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Delete file in app folder
	 */
	private void deleteFile(String id) {

				
		try {
			// Delete file
			mService.files().delete(id).execute();
			
			Log.i(TAG, "Deleted");



		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}