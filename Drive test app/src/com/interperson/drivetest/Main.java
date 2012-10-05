package com.interperson.drivetest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class Main extends Activity {
	
	public String TAG = "DriveTest";
	
	// IPC identifiers
	private int CHOOSE_ACCOUNT = 0;
	
		
	SharedPreferences settings;
	public static final String PREF_FILE_NAME = "PrefFile";
	
	private Drive mDriveService;
	
	private String PREF_ACCOUNT_NAME = "account";
	private String PREF_AUTH_TOKEN = "authtoken";
	private String ACCOUNT_TYPE = "com.google";
	private String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/drive";
	//private String API_KEY = "14142925286-fcfcbrsmbdguimr5jb08lhlpnbioag4q.apps.googleusercontent.com";
	private String API_KEY = "14142925286.apps.googleusercontent.com";
	
	private String mAuthToken = null;
	private String mAccountName = null;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG,"Starting up");
        
        // ZAKAZANI STRICTMODE - HODNE HNUSNEJ HACK
        // Treba vytvorit zvlastni thread pro networking a tohle vyhodit
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 
        
                
        // Reload permanent app settings
        settings = getSharedPreferences(PREF_FILE_NAME,MODE_PRIVATE);
        mAccountName = settings.getString(PREF_ACCOUNT_NAME, null);
        mAuthToken = settings.getString(PREF_AUTH_TOKEN, null);
        
        Log.e(TAG, "Stored username: " + mAccountName);
        
        // BUTTON Show my files
        Button bs = (Button)findViewById(R.id.button2);
        bs.setText("Show my filenames");
        bs.setOnClickListener(new View.OnClickListener() {                        
                public void onClick(View v) {   
                	try {
            			//Log.e(TAG,mDriveService.about().get().execute().getName());
                    	FileList flist = mDriveService.files().list().execute();
                    	Log.e(TAG,"Pocet souboru: " + flist.size());
                    	String filelist = new String();
                    	for (int i = 0; i < flist.size(); i++)
                    	{
                    		File f = flist.getItems().get(i);
                    		Log.e(TAG,"Filename: " + f.getTitle() + ", mime: " + f.getMimeType());
                    		filelist = filelist + "\n" + f.getTitle();
                    	}
                    	
                    	TextView tv = (TextView)findViewById(R.id.textView2);
                        tv.setText(filelist);
                    	
            		} catch (IOException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
                }
        });
        
        
        
        Account account = null;
        if (mAccountName != null) // still logged on
        {
        	account = new Account(mAccountName,ACCOUNT_TYPE);
           	chooseGoogleAccount(account);
           	Button b1 = (Button)findViewById(R.id.button2);
            b1.setVisibility(View.VISIBLE);
            
            
            
        }
        else // not logged in
        {
        	Button b1 = (Button)findViewById(R.id.button2);
            b1.setVisibility(View.INVISIBLE);
        	
        	
        	// BUTTON CONNECT TO DRIVE
            Button b = (Button)findViewById(R.id.button1);
            b.setText("Connect to Drive");
            b.setOnClickListener(new View.OnClickListener() {                        
                    public void onClick(View v) {               	
                    	chooseGoogleAccount(null);     
                        
                    }
            });
        	
        }
        
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
    		
    		AuthorizeGoogleDriveClass auth = new AuthorizeGoogleDriveClass();
    		auth.execute((Void)null);
    		   		
    	}
    	
    	
    }
    
    
    private class AuthorizeGoogleDriveClass extends AsyncTask<Void,Void,Void>
    {
    	@Override
    	protected Void doInBackground(Void... arg0) {
    			Account account = new Account(mAccountName,ACCOUNT_TYPE);
    		    mAuthToken = getGoogleAccessToken(Main.this, account);
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
    		Main.this.GoogleDriveReady();
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
    		Main.this.startActivityForResult(intent,CHOOSE_ACCOUNT);
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (GoogleAuthException e) {
			e.printStackTrace();
		}
    	
    	return retval;
    }
    
    
    public void GoogleDriveReady()
    {
    	Log.e(TAG,"After wait.");
		
		// Display info on main activity
        TextView tv = (TextView)findViewById(R.id.textView1);
        tv.setText("Logged as " + mAccountName);
        
        // Perform connection to service
        mDriveService = buildService(mAuthToken,API_KEY);
        Log.e(TAG, "Connection initiated.");
        
        Button b1 = (Button)findViewById(R.id.button2);
        b1.setVisibility(View.VISIBLE);
        
        
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
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

