package cz.fit.next.backend;

import android.content.SharedPreferences;
import android.content.Context;

public class SettingsProvider {

	private static final String PREF_FILE_NAME = "NextPreferences";
	private static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";
	
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
	 * Wrappers to store or get another types of data
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
	
	
	public String getString(String key) {
		
	}	
	
	public Boolean getBoolean(String key) {
		
	}
	
	public int getNum(String key) {
		
	}
	
}
