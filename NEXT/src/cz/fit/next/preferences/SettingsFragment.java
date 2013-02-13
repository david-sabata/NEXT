package cz.fit.next.preferences;


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
import cz.fit.next.R;
import cz.fit.next.backend.sync.SyncService;
import cz.fit.next.backend.sync.SyncService.ServiceBinder;


public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private boolean mSyncServiceBound = false;
	private SyncService mSyncService = null;

	/* KEYS */
	public static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";
	public static final String PREF_SYNC_ENABLED = "PREF_SYNC_ENABLED";
	public static final String PREF_SYNC_INTERVAL = "PREF_SYNC_INTERVAL";
	public static final String PREF_DESIGN = "PREF_DESIGN";
	public static final String PREF_SYNC_WIFI = "PREF_SYNC_WIFI";
	public static final String PREF_NOTIFICATIONS_ENABLED = "PREF_NOTIFICATIONS_ENABLED";
	public static final String PREF_NOTIFICATIONS_ALLDAYTIME = "PREF_NOTIFICATIONS_ALLDAYTIME";
	public static final String PREF_SHOW_COMPLETED_TASKS = "PREF_SHOW_COMPLETED_TASKS";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onStart() {
		super.onStart();

		getActivity().getApplicationContext().bindService(new Intent(this.getActivity(), SyncService.class), syncServiceConnection, Context.BIND_AUTO_CREATE);

		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}


	@Override
	public void onResume() {
		super.onResume();

		// fill views with actual values - these can changed when opening
		// account connection dialog
		initViews();
	}

	/**
	 * Initialize views according to preferences values
	 */
	protected void initViews() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

		// account name
		String accName = sharedPreferences.getString(PREF_ACCOUNT_NAME, null);
		if (accName != null) {
			Preference accPref = findPreference(PREF_ACCOUNT_NAME);
			accPref.setTitle(accName);
			accPref.setSummary(R.string.change_connected_account);
			accPref.notifyDependencyChange(false);
		}

		// sync interval
		int intervalValueIndex = Integer.parseInt(sharedPreferences.getString(PREF_SYNC_INTERVAL, "-1"));
		Preference intervalPref = findPreference(PREF_SYNC_INTERVAL);
		refreshSummary(intervalPref, intervalValueIndex, R.array.preferenceIntervalEntries);
		intervalPref.setEnabled(accName != null);


		// design
		int designValueIndex = Integer.parseInt(sharedPreferences.getString(PREF_DESIGN, "-1"));
		Preference designPref = findPreference(PREF_DESIGN);
		refreshSummary(designPref, designValueIndex, R.array.preferenceDesignEntries);
		designPref.setEnabled(accName != null);
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
			// implemented custom onclick in DriveLoginPreference
		} else if (key.equals(PREF_SYNC_WIFI)) {
			// no action
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

			if ((mSyncServiceBound) && (sharedPreferences.getBoolean(PREF_SYNC_ENABLED, false))) {
				mSyncService.resetAlarm();
				mSyncService.setAlarmFromPreferences();
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
	private void refreshSummary(Preference pref, int index, int id) {
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