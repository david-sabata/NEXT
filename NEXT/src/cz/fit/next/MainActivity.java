package cz.fit.next;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.deaux.fan.FanView;

public class MainActivity extends FragmentActivity {

	private FanView fan;


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
		} else {
			Log.i("item ID : ", "onOptionsItemSelected Item ID" + id);
			System.out.println("Click on Item");
		}

		return false;
	}

}
