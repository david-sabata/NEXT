package cz.fit.next.backend;

import android.content.SharedPreferences;
import android.content.Context;

public class SettingsProvider {
	
	/* USAGE:
	 * 
	 * (PREF_ACCOUNT_NAME is example of key used for google account storage)
	 * 
	 * SettingsProvider sp = new SettingsProvider(getApplicationContext());
	 * 
	 * get:
	 * 		String s = sp.getString(SettingsProvider.PREF_ACCOUNT_NAME, default_string_if_key_not_found);
	 * 
	 * set:
	 * 		sp.storeString(SettingsProvider.PREF_ACCOUNT_NAME, string_to_store);
	 * 
	 */

	/* KEYS */
	public static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";
	public static final String PREF_SYNC_ENABLED = "PREF_SYNC_ENABLED";
	public static final String PREF_SYNC_INTERVAL = "PREF_SYNC_INTERVAL";
	
	
	private static final String PREF_FILE_NAME = "NextPreferences";
		
	private Context mContext;
	
	public SettingsProvider(Context context) {
		mContext = context;
	}
	
	/**
	 * Saves content to given key
	 * @param key
	 * @param content
	 */
	private void storePreference(String key, String content) {
		SharedPreferences preferences = mContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, content);
		editor.commit();
	}
	
	/**
	 * Gets content from key
	 * @param key
	 * @return
	 */
	private String getPreference(String key) {
		SharedPreferences settings = mContext.getSharedPreferences(PREF_FILE_NAME,
				Context.MODE_PRIVATE);
		return settings.getString(key, null);
	}
	
	
	/*
	 * Wrappers to store another types of data
	 */
	
	public void storeBoolean(String key, Boolean content) {
		storePreference(key, Boolean.toString(content));
	}
	
	public void storeNum(String key, int content) {
		storePreference(key, Integer.toString(content));
	}
	
	public void storeString(String key, String content) {
		storePreference(key, content);
	}
	
	
	
	/**
	 * Gets string from given key, if not exists, returns default
	 * @param key
	 * @param def Default value
	 * @return
	 */
	public String getString(String key, String def) {
		String val = getPreference(key);
		if (val != null) return val;
		else return def;
		
		
	}	
	
	/**
	 * Gets bool from given key, if not exists, returns default
	 * @param key
	 * @param def Default value
	 * @return
	 */
	public boolean getBoolean(String key, boolean def) {
		String val = getPreference(key);
		if (val != null) return Boolean.valueOf(val);
		else return def;
		
	}
	
	/**
	 * Gets int from given key, if not exists, returns default
	 * @param key
	 * @param def Default value
	 * @return
	 */
	public int getNum(String key, int def) {
		String val = getPreference(key);
		if (val != null) return Integer.parseInt(val);
		else return def;
	}
	
}