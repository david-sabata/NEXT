package cz.fit.next.backend;

import android.content.SharedPreferences;
import android.content.Context;
import android.preference.PreferenceManager;

public class SettingsProvider {
	
	private Context mContext;
	
	public SettingsProvider(Context context) {
		mContext = context;
	}
	
	public void storeBoolean(String key, Boolean content) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, content);
		editor.commit();
	}
	
	public void storeNum(String key, int content) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, content);
		editor.commit();
	}
	
	public void storeString(String key, String content) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, content);
		editor.commit();
	}
	
	
	
	/**
	 * Gets string from given key, if not exists, returns default
	 * @param key
	 * @param def Default value
	 * @return
	 */
	public String getString(String key, String def) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		String val = settings.getString(key, def);
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
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		Boolean val = settings.getBoolean(key, def);
		return val;
		
	}
	
	/**
	 * Gets int from given key, if not exists, returns default
	 * @param key
	 * @param def Default value
	 * @return
	 */
	public int getNum(String key, int def) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		int val = settings.getInt(key, def);
		return val;
	}
	
	/**
	 * Gets long from given key, if not exists, returns default
	 * @param key
	 * @param def Default value
	 * @return
	 */
	public long getLong(String key, int def) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		long val = settings.getLong(key, def);
		return val;
	}
	
}
