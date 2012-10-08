package cz.fit.next;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
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



import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class MainActivity extends Activity {

	
public String TAG = "NEXT - Drive Comm";
	
	// IPC identifiers
	private int CHOOSE_ACCOUNT = 0;
	
		
	SharedPreferences settings;
	public static final String PREF_FILE_NAME = "PrefFile";
	
	private Drive mDriveService;
	
	private String PREF_ACCOUNT_NAME = "account";
	private String PREF_AUTH_TOKEN = "authtoken";
	private String ACCOUNT_TYPE = "com.google";
	private String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive";
	private String API_KEY = "457762972644.apps.googleusercontent.com";
	
	
	private String mAuthToken = null;
	private String mAccountName = null;
	private Boolean mAuthorized = false;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // ZAKAZANI STRICTMODE - HODNE HNUSNEJ HACK
        // Treba vytvorit zvlastni thread pro networking a tohle vyhodit
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 
        
        chooseGoogleAccount(null);
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    void chooseGoogleAccount(Account savedAccount) {
    	String accList[] = new String[1];
    	accList[0] = ACCOUNT_TYPE;
    	Log.e(TAG, "Pred zobrazenim");
    	startActivityForResult(AccountPicker.newChooseAccountIntent(savedAccount,null,accList,false,null,null,null,null),CHOOSE_ACCOUNT);
    }
    
   
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if ((requestCode == CHOOSE_ACCOUNT) && (resultCode == RESULT_OK) && (data != null))
    	{
    		mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
    		Log.i(TAG, "Selected account: " + mAccountName);
    		
    		mAuthorized = false;
    		AuthorizeGoogleDriveClass auth = new AuthorizeGoogleDriveClass();
    		auth.execute((Void)null);
    		   		
    	}
    	
    	
    }
    
    
    private class AuthorizeGoogleDriveClass extends AsyncTask<Void,Void,Void>
    {
    	@Override
    	protected Void doInBackground(Void... arg0) {
    			Account account = new Account(mAccountName,ACCOUNT_TYPE);
    		    mAuthToken = getGoogleAccessToken(MainActivity.this, account);
    		    Log.e(TAG,"Token is: " + mAuthToken);
    		    if (mAuthToken != null)
    		    {
    		    	// save username and authtoken into permanent storage
    		    	SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
    		        SharedPreferences.Editor editor = preferences.edit();
    		        editor.putString(PREF_ACCOUNT_NAME, mAccountName);
    		        editor.putString(PREF_AUTH_TOKEN, mAuthToken);
    		        editor.commit();
    		     	        
    		        
    		    }
    			return null;    			
    			
    		}
    	
    	@Override
    	protected void onPostExecute(Void result)
    	{
    		super.onPostExecute(result);
            
            // Perform connection to service
            mDriveService = buildService(mAuthToken,API_KEY);
            Log.e(TAG, "Connection initiated.");
            mAuthorized = true;
            
            // GET ID OF FOLDER NEXT
            FileList flist = null;
            Files.List request = null;
            String nextDirId = new String();
            
            try {
				request = mDriveService.files().list();
				String q = "mimeType = 'application/vnd.google-apps.folder' and title = 'NEXT'";
				request = request.setQ(q);
				
				flist = request.execute();
				if (flist.size() > 0)
				{
					nextDirId = flist.getItems().get(0).getId();
					Log.e(TAG, "ID of NEXT directory is: " + nextDirId);
				}
				else // Create new NEXT folder
				{
					Log.e(TAG, "NEXT folder not found.");
					
					// TODO: Create new directory
				}
				
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
            
            
            
            
            
            // DOWNLOAD FILELIST IN FOLDER "NEXT"
            flist = null;
            request = null;
            List<File> res = new ArrayList<File>();
            try {
				request = mDriveService.files().list();
				request = request.setMaxResults(50);
				String q = "'" + nextDirId + "' in parents";
				request = request.setQ(q);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

            do
            {
	    		try {
	    			flist = request.execute();
	    			res.addAll(flist.getItems());
	    			request = request.setPageToken(flist.getNextPageToken());
	    			Log.e(TAG, "New page token: " + flist.getNextPageToken());
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    			break;
	    		}
            } while (flist.getNextPageToken() != null && flist.getNextPageToken().length() > 0);
            
    		Log.e(TAG,"Pocet souboru: " + res.size());
        	String filelist = new String();
        	for (int i = 0; i < res.size(); i++)
        	{
        		File f = res.get(i);
        		Log.e(TAG,"Filename: " + f.getTitle() + ", mime: " + f.getMimeType());
        		filelist = filelist + "\n" + f.getTitle();
        	}
        	
        	TextView tv = (TextView)findViewById(R.id.tv1);
            tv.setText(filelist);
        		
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
    		Log.e(TAG,"ERROR in authentication");
    		Log.e(TAG,e.toString());
    		Intent intent = e.getIntent();
    		MainActivity.this.startActivityForResult(intent,CHOOSE_ACCOUNT);
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
