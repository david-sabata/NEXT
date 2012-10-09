package cz.fit.next;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.deaux.fan.FanView;
import com.google.android.gms.common.AccountPicker;

public class MainActivity extends FragmentActivity {

	private FanView fan;

	/* Constants to identify activities called by startActivityForResult */
	private int CHOOSE_ACCOUNT = 100;


	@Override
	@TargetApi(14)
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		fan = (FanView) findViewById(R.id.fan_view);

		Fragment fanFrag = new SidebarFragment();
		Fragment contentFrag = new ListFragment();
		fan.setFragments(contentFrag, fanFrag);

		// always enabled on SDK < 14
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			getActionBar().setHomeButtonEnabled(true);
		}
	}


	/**
	 * Closes sidebar
	 * 
	 * @param v
	 */
	public void unclick(View v) {
		System.out.println("CLOSE");
		fan.showMenu();
	}


	/**
	 * Opens sidebar
	 * 
	 * @param v
	 */
	public void click(View v) {
		System.out.println("OPEN");
		fan.showMenu();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);

		return true;
	}


	/**
	 * On menu item clicked It can be either classic menu item or app icon in
	 * action bar
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		int id = item.getItemId();
		if (id == android.R.id.home) {
			System.out.println("Click on Home icon");
			if (fan.isOpen()) {
				unclick(null);
			} else {
				click(null);
			}
		} else if (id == R.id.setting_connect_drive) {
			// Log.i("Setting", "Google Login");
			// TODO: If some account is stored in perm storage, enable it here
			chooseGoogleAccount(null);

		} else {
			Log.i("item ID : ", "onOptionsItemSelected Item ID" + id);
			System.out.println("Click on Item");
		}

		return false;
	}


	/**
	 * Opens activity for choose Google account
	 * 
	 * @param username
	 *            Stored username, will not display account chooser if specified
	 */
	void chooseGoogleAccount(String username) {

		Account account = null;
		if (username != null) // Logged on
		{
			account = new Account(username, "com.google");
		} else {
			account = null;
		}

		String accList[] = new String[1];
		accList[0] = "com.google";

		startActivityForResult(
				AccountPicker.newChooseAccountIntent(account, null, accList, false, null, null, null, null),
				CHOOSE_ACCOUNT);
	}


	/**
	 * Process result from called activity
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((requestCode == CHOOSE_ACCOUNT) && (resultCode == RESULT_OK) && (data != null)) {
			String accountName = new String();
			accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			Log.i("NEXT Drive", "Selected account: " + accountName);
			// TODO: Store account name to permanent storage


			// Execute asynctask with Google Drive Authorizing
			// AuthorizeGoogleDriveClass auth = new AuthorizeGoogleDriveClass();
			// auth.execute((Void)null);

		}


	}



}
