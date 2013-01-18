package cz.fit.next;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
				
		//setTheme(R.style.Next_PreferenceScreen_Light);
		addPreferencesFromResource(R.xml.preferences);
	}


}
