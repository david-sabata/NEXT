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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
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
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;

import cz.fit.next.R;
import cz.fit.next.backend.sync.PermissionActivity;
import cz.fit.next.backend.sync.SyncService;


public class GDrive {

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
	private String mAppFolder = null;
	
	
	public static int READ = 0;
	public static int WRITE = 1;
	public static int OWNER = 2;
	


	
	/**
	 * Initializes synchronization, performs reauth and 
	 * determines id of application folder
	 */
	public boolean initSync(Context appcontext, SyncService syncserv, String account) throws IOException,GoogleAuthException {
		
		
		mSyncService = syncserv;
		mAccountName = account;
		
		if (!reauth(appcontext)) return false;
		mAppFolder = getAppFolderId();
		
		return true;
	}

		
	
	/**
	 * Gets list of files in application folder
	 */
	public List<File> list(Context appcontext, SyncService syncserv) throws IOException {
		//mSyncService = syncserv;
		//mCallback = cb;
		
		List<File> res = getFileList(mAppFolder);
		return res;		
		
	}
	
	/**
	 * Gets list of files in shared folder
	 */
	public List<File> listShared(Context appcontext) throws IOException {
		
		List<File> res = getSharedFileList(mAppFolder);
		return res;
		
	}
	
	
	/**
	 * Download file with given filename to local storage
	 */
	public void download(Context appcontext, String id) throws IOException,GoogleJsonResponseException,HttpResponseException {
		downloadFile(id, mAppFolder);		
		
	}
	
	/**
	 * Deletes file on storage 
	 * 	 */
	public void delete(String filename) throws IOException {
		String existing = getFileIdByName(filename, mAppFolder);
		//if (existing != null) Log.i(TAG,"EXISTS!");
		if (existing != null) deleteFile(existing);
	}
	
	/**
	 * Upload file with given local name to storage with other name
	 */
	public void upload(Context appcontext, String filename, String localname) throws IOException {
		String existing = getFileIdByName(filename, mAppFolder);
		//if (existing != null) Log.i(TAG,"EXISTS!");
		if (existing != null) updateFile(existing, localname);
		else uploadFile(filename, localname, mAppFolder);
		
		// Delete local pattern
		java.io.File f = new java.io.File(mSyncService.getFilesDir() + "/" + localname);
		if (!f.delete()) Log.e(TAG, "DELETING ERROR!!!!");
	}
	
	
	
	/**
	 * Starts sharing of file to given google account
	 */
	public boolean share(String file, String user, int mode) throws IOException {
		
		String fileid = getFileIdByName(file, mAppFolder);
		if (fileid == null) return false;
		
		return setPermissions(fileid, user, mode);
		
		
	}
	
	
	/**
	 * Stops sharing of file to given google account
	 */
	public void unshare(String file, String user, int mode) {
		
		
		
	}
	
	
	/**
	 * Move file on remote storage to my folder
	 */
	
	public void move(String fileid) throws IOException {
		moveFile(fileid, mAppFolder);
	}
	
	
	/**
	 * Get userlist of one file
	 */
	public List<UserPerm> getUserList(String fileid) throws IOException {
		List<UserPerm> result = new ArrayList<UserPerm>();
		List<Permission> perms = getPermissions(fileid);
		
		for (int i = 0; i < perms.size(); i++) {
			UserPerm up = new UserPerm();
			up.username = perms.get(i).getName();
			//Log.i("PERM","Name: " + perms.get(i).getName() + ", perm: " + perms.get(i).getRole());
			if (perms.get(i).getRole().equals("writer"))
				up.mode = WRITE;
			else if (perms.get(i).getRole().equals("owner"))
				up.mode = OWNER;
			else if (perms.get(i).getRole().equals("reader"))
				up.mode = READ;
			else continue;
			
			result.add(up);
		}
		
		return result;
	}
	
	
	
	/***************************************/
	/*          HELPER FUNCTIONS           */
	/***************************************/
	
	/**
	 * Invalidates token and makes new one
	 */
	private boolean reauth(Context appcontext) throws IOException,GoogleAuthException,IllegalArgumentException {
		AccountManager am = AccountManager.get(appcontext);
		am.invalidateAuthToken("com.google", null);
		if (mAccountName == null) return false;

		Account account = new Account(mAccountName, "com.google");
		mAuthToken = getGoogleAccessToken(null, appcontext, account);
		Log.e(TAG, "Token is: " + mAuthToken);
		if (mAuthToken == null) return false;
		mService = buildService(mAuthToken, API_KEY);
		return true;
	}


	/**
	 * Gets Access Token to Google Drive
	 */
	private String getGoogleAccessToken(Activity main, Context appcontext, Account account) throws IOException,GoogleAuthException,IllegalArgumentException {
		String retval = null;

		try {
			retval = GoogleAuthUtil.getToken(appcontext, account.name, AUTH_TOKEN_TYPE);
			// TODO: Catch network error
		} catch (UserRecoverableAuthException e) {
			Log.e(TAG, "ERROR in authentication");
			Log.e(TAG, e.toString());
			handleAuthException(e.getIntent());

		}
		
		// Finish login activity, if exists
		//if (main != null) main.finish();

		return retval;
	}
	
	private void handleAuthException(Intent intent) {
		final Context context = mSyncService.getApplicationContext();
	    
	    final Intent secondaryIntent = new Intent(context, 
	            PermissionActivity.class);

	    secondaryIntent.putExtra("intent", intent);
	    secondaryIntent.putExtra("account", mAccountName);
	   
	    final String title = "NEXT: Problem in authentication.";
	    final String text = "Tap here to solve it.";
	    

	    
	   /* Create the notification specifying the intent to use to start
	    * the activity as a PendingIntent and show the notification.
	    */
	    final Notification notification =  new Notification.Builder(context)
	            .setSmallIcon(R.drawable.menu_next)
	            .setAutoCancel(true)
	            .setContentTitle(title)
	            .setTicker(title)
	            .setContentText(text)
	            .setContentIntent(PendingIntent.getActivity(
	                    context,
	                    0 /* requestCode*/,
	                    secondaryIntent,
	                    PendingIntent.FLAG_CANCEL_CURRENT))
	            .getNotification();
	    final NotificationManager manager = (NotificationManager) context
	            .getSystemService(Context.NOTIFICATION_SERVICE);
	    manager.notify("syncerr", 0, notification);
	    
	}
	
	
	
	


	/**
	 * Creates object which represents Drive Service
	 */
	private Drive buildService(final String AuthToken, final String ApiKey) {
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();

		Drive.Builder b = new Drive.Builder(httpTransport, jsonFactory, null);
		b.setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {

			@Override
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
	private String getAppFolderId() throws IOException {
		
		
		FileList flist = null;
		Files.List request = null;
		String nextDirId = new String();
		
	
		// Determine the folder id
		request = mService.files().list();
		String q = "mimeType = 'application/vnd.google-apps.folder' and title = '" + FOLDER_NAME + "'";
		request = request.setQ(q);

		flist = request.execute();
		if (flist.getItems().size() > 0) {
			nextDirId = flist.getItems().get(0).getId();
			Log.e(TAG, "ID of NEXT directory is: " + nextDirId);
		} else // Create new NEXT folder
		{
			Log.e(TAG, "NEXT folder not found, creating.");

			File body = new File();
			body.setTitle(FOLDER_NAME);
			// body.setDescription("");
			body.setMimeType("application/vnd.google-apps.folder");
			mService.files().insert(body).execute();
			getAppFolderId();				
		}

		
		mAppFolder = nextDirId;
		return nextDirId;
		
	}
	
	
	/**
	 * Returns file id by its name
	 */
	private String getFileIdByName(String name, String appFolder) throws IOException {
		
		String res = null;
		
		FileList flist = null;
		Files.List request = null;
				
		
					
		// Get File By Name
		request = mService.files().list();
		String q = "title = '" + name + "' and '" + appFolder + "' in parents and trashed = false";
		request = request.setQ(q);

		flist = request.execute();
	
		if (flist.getItems().size() > 0) {
			res = flist.getItems().get(0).getId();
		} else {
			res = null;
		}
			
		

		return res;
	}
	

	/**
	 * Returns filelist of application folder
	 */
	private List<File> getFileList(String appFolder) throws IOException {

		FileList flist = null;
		Files.List request = null;

		List<File> res = new ArrayList<File>();

		// Download filelist
		flist = null;
		request = null;
		;
		request = mService.files().list();
		String q = "'" + appFolder + "' in parents and trashed = false";
		// q = "not 'me' in owners";
		request = request.setQ(q);

		do {
			flist = request.execute();
			res.addAll(flist.getItems());
			request = request.setPageToken(flist.getNextPageToken());
			//Log.e(TAG, "New page token: " + flist.getNextPageToken());
		} while (flist.getNextPageToken() != null && flist.getNextPageToken().length() > 0);

		//Log.e(TAG, "Pocet souboru: " + res.size());

	

		return res;


	}


	/**
	 * Returns list of shared files, which are not in application folder
	 */
	private List<File> getSharedFileList(String appFolder) throws IOException {

		FileList flist = null;
		Files.List request = null;
		List<File> res = new ArrayList<File>();
		List<File> filtered = new ArrayList<File>();

	
		// Download filelist
		flist = null;
		request = null;

		request = mService.files().list();
		String q = "not 'me' in owners and not '" + appFolder + "' in parents and trashed = false";
		// Log.i(TAG, q);
		request = request.setQ(q);

		do {
			flist = request.execute();
			res.addAll(flist.getItems());
			request = request.setPageToken(flist.getNextPageToken());
			//Log.e(TAG, "New page token: " + flist.getNextPageToken());
		} while (flist.getNextPageToken() != null && flist.getNextPageToken().length() > 0);

		//Log.e(TAG, "Pocet sdilenych souboru: " + res.size());
		
		// Exclude files with bad names
		for (int i = 0; i < res.size(); i++) {
			String title = res.get(i).getTitle();
			Log.i(TAG, title);
			if (title.contains(".nextproj.html")) 
			{
				filtered.add(res.get(i));
			}
		}

		return filtered;

	}


		
	/*
	 * Downloads file
	 */
	private void downloadFile(String id, String appFolder) throws IOException,GoogleJsonResponseException,HttpResponseException {

		File dfile;
		dfile = mService.files().get(id).execute();
		String token = mService.files().get(id).getOauthToken();
		String name = dfile.getTitle();

		//Log.i(TAG, "URL: " + dfile.getDownloadUrl());

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


	}


	/**
	 * Uploads file from "localname" to "appfolder"/"name" on storage
	 */
	private void uploadFile(String name, String localname, String appFolder) throws IOException {

				
		// Upload file
	
		// File's metadata.
		File body = new File();
		body.setTitle(name);
		// body.setDescription("");
		body.setMimeType("text/plain");
		List<ParentReference> parents = new ArrayList<ParentReference>();
		parents.add((new ParentReference()).setId(mAppFolder));
		body.setParents(parents);
	
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

	}
	
	
	/**
	 * Uploads file from "localname" to "appfolder"/"name" on storage
	 */
	private void updateFile(String fileid, String localname) throws IOException {

				
	
			
		// Update file

		java.io.File fileContent = null;
		FileContent mediaContent = null;
		
		// File's content.
		if (localname != null) {
			fileContent = new java.io.File(mSyncService.getFilesDir() + "/" + localname);
			mediaContent = new FileContent("text/plain", fileContent);
			mService.files().update(fileid, null, mediaContent).execute();
		}



	}
	
	
	/**
	 * Moves file to "appfolder"-id on storage
	 */
	private void moveFile(String fileid, String appfolder)  throws IOException {

				
	
		// get old file
		File dfile = mService.files().get(fileid).execute();
		
		// Change file's metadata.
		List<ParentReference> parents = dfile.getParents();
		parents.add((new ParentReference()).setId(appfolder));
		dfile.setParents(parents);
		
		mService.files().update(fileid, dfile).execute();

	

	}
	
	
	
	/**
	 * Delete file in app folder
	 */
	private void deleteFile(String id) throws IOException {

				
			// Delete file
			mService.files().delete(id).execute();
			
			Log.i(TAG, "Deleted ");

	}
	
	/**
	 * Set/update permission on file
	 */
	
	private boolean setPermissions(String fileid, String gmail, int mode) throws IOException {
		Permission newPermission = new Permission();

	    newPermission.setValue(gmail);
	    newPermission.setType("user");
	    if (mode == READ) {
	    	newPermission.setRole("reader"); //owner,writer,reader
	    } else {
	    	newPermission.setRole("writer"); //owner,writer,reader
	    }
	    
	    mService.permissions().insert(fileid, newPermission).execute();
	   
	    
	    return true;
	  

	}
	
	/**
	 * Get permissions of file
	 */
	
	List<Permission> getPermissions(String fileid) throws IOException {
		PermissionList permissions = null;
		
		
		permissions = mService.permissions().list(fileid).execute();
		return permissions.getItems();
		

	}
	
	
	
	
	
	/***************************************/
	/*        SUBCLASSES DEFINITION        */
	/***************************************/
	
	public class UserPerm {
		public String username;
		public int mode;
	}


}