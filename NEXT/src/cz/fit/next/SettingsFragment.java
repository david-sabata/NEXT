package cz.fit.next;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
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
		Resources res = getResources();
		
		Preference pref = null;
		Integer idOfStringArray = null;
		Integer indexToStringArray = -1;
		String summary = "";
		
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
            // TODO SET THEME 
        }

		// Set new summary to settings
		if(pref != null && indexToStringArray!= -1) {
            // Get String from array from resources
            String[] entries = res.getStringArray(idOfStringArray);
            // Find String in resources for selected item
            summary = entries[indexToStringArray - 1];
            // Set new summary for setting
			pref.setSummary(summary);
		}
	}

}
