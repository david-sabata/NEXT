package cz.fit.next.preferences;

import cz.fit.next.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class SettingsUtil  {
	Resources res = null;
	SharedPreferences preferences = null;
	Context c = null;
	String[] themeStrings = null;
	int[] intervalTime = null;
	
	public SettingsUtil(Context pc) {
		c = pc;
		preferences = PreferenceManager.getDefaultSharedPreferences(c);	
		res = c.getResources();
	}
	
	/**
	 * Set theme of activity reffered by context
	 * @param actContext
	 */
	public void setTheme(Context actContext) {
		String themeStringIndex = preferences.getString(SettingsFragment.PREF_DESIGN, "1");
	    String theme = (res.getStringArray(R.array.preferenceDesignValuesForAndroid))[Integer.parseInt(themeStringIndex) - 1];
	    
        if(theme.equals("holo_light")) {
        	actContext.setTheme(R.style.ThemeNext_Light);
        } else if(theme.equals("holo_dark")) {
        	actContext.setTheme(R.style.ThemeNext_Dark);
        } else {
        	actContext.setTheme(R.style.ThemeNext_Light);
        }
	}

}
