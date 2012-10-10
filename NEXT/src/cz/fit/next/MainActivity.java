package cz.fit.next;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.deaux.fan.FanView;

import cz.fit.next.tasks.Task;

public class MainActivity extends FragmentActivity {

	private static final String LOG_TAG = "FragmentActivity";


	@Override
	@TargetApi(14)
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.test);
		FanView fan = (FanView) findViewById(R.id.fan_view);


		fan.setAnimationDuration(200); // 200ms

		if (savedInstanceState == null) {
			Fragment fanFrag = new SidebarFragment();

			// TODO this is valid code!! Delete comments after debug tasks
			// ContentListFragment contentFrag = new ContentListFragment();
			// fan.setFragments(contentFrag, fanFrag);

			TaskDetailFragment taskFrag = new TaskDetailFragment();
			fan.setFragments(taskFrag, fanFrag);

		} else {
			fan.setViews(-1, -1);
		}


		// always enabled on SDK < 14
		if (android.os.Build.VERSION.SDK_INT >= 14 && getActionBar() != null) {
			getActionBar().setHomeButtonEnabled(true);
		}
	}



	@Override
	protected void onResume() {
		super.onResume();

		List<Task> items = new ArrayList<Task>();
		for (int i = 0; i < 15; i++) {
			Task task = new Task();
			task.setTitle("polozka " + i);
			items.add(task);
		}

		/**
		 * Fragment is stored by FragmentManager even after its parent activity
		 * is destroyed and recreated. The fragment is reattached, but its data
		 * have to be reinitialized (fragment loses its adapter) which is done
		 * in setItems
		 */
		Fragment content = getSupportFragmentManager().findFragmentById(R.id.appView);
		if (content != null && content instanceof ContentListFragment) {
			ContentListFragment contentFragment = (ContentListFragment) content;
			contentFragment.setItems(items);
		} else {
			Log.e(LOG_TAG, "onResume: content fragment is null");
		}

	}


	/**
	 * Closes sidebar
	 * 
	 * @param v
	 */
	public void unclick(View v) {
		System.out.println("CLOSE");

		FanView fan = (FanView) findViewById(R.id.fan_view);
		fan.showMenu();
	}


	/**
	 * Opens sidebar
	 * 
	 * @param v
	 */
	public void click(View v) {
		System.out.println("OPEN");

		FanView fan = (FanView) findViewById(R.id.fan_view);
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

		switch (item.getItemId()) {
			case android.R.id.home:
				FanView fan = (FanView) findViewById(R.id.fan_view);
				if (fan.isOpen()) {
					unclick(null);
				} else {
					click(null);
				}
				break;

			case R.id.setting_connect_drive:
				Log.i(LOG_TAG, "Google Login");
				break;

			default:
				Log.i(LOG_TAG, "onOptionsItemSelected Item " + item.getTitle());
				System.out.println("Click on Item");
		}

		return false;
	}



}
