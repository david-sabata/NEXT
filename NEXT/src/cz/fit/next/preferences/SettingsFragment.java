package cz.fit.next.preferences;

import cz.fit.next.R;
import cz.fit.next.R.array;
import cz.fit.next.R.xml;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

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
		SharedPreferences sharedPreferences =  PreferenceManager.getDefaultSharedPreferences(getActivity());	
		Preference pref = null;
		
		// Array of settings, we would like to initialize
		String[] summaryPref = {PREF_DESIGN,PREF_SYNC_INTERVAL};
		Integer indexToStringArray = null;
		Integer idOfStringArray = -1;
		for(int i = 0; i < summaryPref.length; i++) {
			pref = findPreference(summaryPref[i]);
			
			// If preference wasnt found, than go to next
			if(pref == null) {
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
			loadSummary(pref,indexToStringArray, idOfStringArray );	
		}
	}
	

	@Override
	public void onStart() {
		super.onStart();
	    getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
	    getPreferenceScreen().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(this);
	}

	
	/*
	 * Catch event if sth changed in preferences
	 * (non-Javadoc)
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = null;
		Integer idOfStringArray = null;
		Integer indexToStringArray = -1;
		
		pref = findPreference(key);
		if(pref == null) {
			return;
		}
		
		// Account name changed
		if (key.equals(PREF_ACCOUNT_NAME)) {
			// TODO set ACCOUNT NAME to APP
        } else if (key.equals(PREF_SYNC_ENABLED)) {
        	// TODO TURN ON SYNC
        } else if (key.equals(PREF_SYNC_INTERVAL)) {
    		indexToStringArray = Integer.parseInt(sharedPreferences.getString(key, "-1"));
            idOfStringArray = R.array.preferenceIntervalEntries;
            // TODO SET INTERVAL OF SYNC
        } else if (key.equals(PREF_DESIGN)) {
    		indexToStringArray = Integer.parseInt(sharedPreferences.getString(key, "-1"));
            idOfStringArray = R.array.preferenceDesignEntries;
            getActivity().recreate();               
        }
		// Reload summary
		loadSummary(pref, indexToStringArray, idOfStringArray);
	}

	/**
	 * This method load summary on preference fragment created
	 */
	private void loadSummary(Preference pref, Integer index, Integer id) {
		Resources res = getResources();
		// Set new summary to settings
		if(pref != null && index!= -1) {
            // Get String from array from resources
            String[] entries = res.getStringArray(id);
            // Find String in resources for selected item
            String summary = entries[index - 1];
            // Set new summary for setting
			pref.setSummary(summary);
		}
	}
	
}