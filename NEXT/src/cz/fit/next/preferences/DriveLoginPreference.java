package cz.fit.next.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import cz.fit.next.backend.sync.LoginActivity;

public class DriveLoginPreference extends Preference {

	public DriveLoginPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean shouldDisableDependents() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		String acc = preferences.getString(SettingsFragment.PREF_ACCOUNT_NAME, null);
		return (acc == null);
	}

	@Override
	protected void onClick() {
		Intent i = new Intent(getContext(), LoginActivity.class);
		Bundle b = new Bundle();
		b.putInt("login", 1);
		i.putExtras(b);
		getContext().startActivity(i);
	}





}
