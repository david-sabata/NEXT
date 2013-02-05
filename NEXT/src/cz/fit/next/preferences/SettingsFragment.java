package cz.fit.next.preferences;


import cz.fit.next.R;
import cz.fit.next.backend.sync.SyncService;
import cz.fit.next.backend.sync.SyncService.ServiceBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;


public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private boolean mSyncServiceBound = false;
	private SyncService mSyncService = null;
	
	/* KEYS */
	public static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";
	public static final String PREF_SYNC_ENABLED = "PREF_SYNC_ENABLED";
	public static final String PREF_SYNC_INTERVAL = "PREF_SYNC_INTERVAL";
	public static final String PREF_DESIGN = "PREF_DESIGN";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);


		addPreferencesFromResource(R.xml.preferences);

		// Initialize prefference
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		Preference pref = null;

		// Array of settings, we would like to initialize
		String[] summaryPref = { PREF_DESIGN, PREF_SYNC_INTERVAL };
		Integer indexToStringArray = null;
		Integer idOfStringArray = -1;
		for (int i = 0; i < summaryPref.length; i++) {
			pref = findPreference(summaryPref[i]);

			// If preference wasnt found, than go to next
			if (pref == null) {
				continue;
			}

			// Settings to initialize 
			if (summaryPref[i].equals(PREF_ACCOUNT_NAME)) {
			} else if (summaryPref[i].equals(PREF_SYNC_ENABLED)) {
			} else if (summaryPref[i].equals(PREF_SYNC_INTERVAL)) {
				indexToStringArray = Integer.parseInt(sharedPreferences.getString(summaryPref[i], "-1"));
				idOfStringArray = R.array.preferenceIntervalEntries;
			} else if (summaryPref[i].equals(PREF_DESIGN)) {
				indexToStringArray = Integer.parseInt(sharedPreferences.getString(summaryPref[i], "-1"));
				idOfStringArray = R.array.preferenceDesignEntries;
			}
			refreshSummary(pref, indexToStringArray, idOfStringArray);
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		
		getActivity().getApplicationContext().bindService(new Intent(this.getActivity(), SyncService.class), syncServiceConnection,
	            Context.BIND_AUTO_CREATE);
		
	    getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onStop() {
		super.onStop();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}


	/*
	 * Catch event if sth changed in preferences
	 * (non-Javadoc)
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = null;
		
		pref = findPreference(key);
		if (pref == null) {
			return;
		}

		// Account name changed
		if (key.equals(PREF_ACCOUNT_NAME)) {
			// TODO set ACCOUNT NAME to APP

        } else if (key.equals(PREF_SYNC_ENABLED)) {
        	if (sharedPreferences.getBoolean(key, false)) {
        		if (mSyncServiceBound) {
        			mSyncService.setAlarmFromPreferences();
        		} else {
        			Log.i("Settings", "Service is not bound !");
        		}
        	}
        	if (!sharedPreferences.getBoolean(key, false)) {
        		if (mSyncServiceBound) {
                	mSyncService.resetAlarm();
        		} else {
        			Log.i("Settings", "Service is not bound !");
        		}
        	}
        } else if (key.equals(PREF_SYNC_INTERVAL)) {
    		int indexToStringArray = Integer.parseInt(sharedPreferences.getString(key, "-1"));
            int idOfStringArray = R.array.preferenceIntervalEntries;
            
            // Reload summary
    		refreshSummary(pref, indexToStringArray, idOfStringArray);
           
            if (mSyncServiceBound) {
            	mSyncService.resetAlarm();
    			mSyncService.setAlarmFromPreferences();
    		} else {
    			Log.i("Settings", "Service is not bound !");
    		}
            
        } else if (key.equals(PREF_DESIGN)) {
    		int indexToStringArray = Integer.parseInt(sharedPreferences.getString(key, "-1"));
            int idOfStringArray = R.array.preferenceDesignEntries;
            // Reload summary
    		refreshSummary(pref, indexToStringArray, idOfStringArray);
            getActivity().recreate();               
        }

		
	}

	/**
	 * This method load summary on preference fragment created
	 */
	private void refreshSummary(Preference pref, Integer index, Integer id) {
		Resources res = getResources();

		// Set new summary to settings
		if (pref != null && index != 0) {
			// Get String from array from resources
			String[] entries = res.getStringArray(id);
			// Find String in resources for selected item
			String summary = entries[index - 1];
			// Set new summary for setting
			pref.setSummary(summary);
		}
	}

	
	private ServiceConnection syncServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			final ServiceBinder binder = (ServiceBinder) service;

			mSyncService = binder.getService();
			mSyncServiceBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mSyncServiceBound = false;	
			mSyncService = null;
		}
	};


}