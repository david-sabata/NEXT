package cz.fit.next;



import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.deaux.fan.FanView;

import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.TasksModelService.ModelServiceBinder;
import cz.fit.next.backend.sync.JavaParser;
import cz.fit.next.backend.sync.SyncService;
import cz.fit.next.sidebar.SidebarFragment;
import cz.fit.next.tasklist.TaskListFragment;



public class MainActivity extends FragmentActivity {

	private static final String LOG_TAG = "FragmentActivity";

	private final MainActivity self = this;


	protected TasksModelService mModelService;

	protected boolean mIsServiceBound = false;





	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.test);



		// setup fan sidebar
		FanView fan = (FanView) findViewById(R.id.fan_view);
		fan.setAnimationDuration(200); // 200ms

		if (savedInstanceState == null) {
			Fragment fanFrag = new SidebarFragment();

			//			TaskListFragment contentFrag = TaskListFragment.newInstance(null);

			LoadingFragment contentFrag = LoadingFragment.newInstance();

			fan.setFragments(contentFrag, fanFrag);
		} else {
			fan.setViews(-1, -1);
		}



		// always enabled on SDK < 14
		if (getActionBar() != null) {
			getActionBar().setHomeButtonEnabled(true);
		}

		bindModelService();

		// start synchronization service
		//		Intent i = new Intent(this, SyncService.class);
		//		Bundle b = new Bundle();
		//		b.putInt("buttonPressed", 0);
		//		i.putExtras(b);
		//		this.startService(i);

	}



	@Override
	protected void onResume() {
		super.onResume();

		// restore singleton service reference
		if (mModelService == null && TasksModelService.getInstance() != null)
			mModelService = TasksModelService.getInstance();

		// restore service if needed
		bindModelService();

	}



	/**
	 * Binds ModelService to Application context if needed
	 */
	private void bindModelService() {
		if (TasksModelService.getInstance() == null) {
			Intent intent = new Intent(this, TasksModelService.class);
			getApplicationContext().bindService(intent, modelServiceConnection, Context.BIND_AUTO_CREATE);
			Log.d(LOG_TAG, "binding service to app context");
		}
	}




	/**
	 * Inflate menu from XML
	 */
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
				fan.showMenu();
				break;

			case R.id.setting_connect_drive:
				// Log.i("Setting", "Google Login");

				// tell synchronization service to choose user account
				Intent i = new Intent(this, SyncService.class);
				Bundle b = new Bundle();
				b.putInt("buttonPressed", 1);
				i.putExtras(b);
				this.startService(i);


				break;

			case R.id.setting_read_file:
				Log.i(LOG_TAG, "JSON Read File");

				// Just for debugging
				JavaParser parser = new JavaParser();
				parser.setFile("file.html");
				Project project = parser.getProject();
				ArrayList<Task> tasks = parser.getTasks(project);

				ArrayList<TaskHistory> histories = parser.getHistory();

				break;

			case R.id.menu_wipe_db:
				// TODO: temporary, hardcoded
				new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("Wipe database")
						.setMessage("Do you really want to erase all stored data?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								TasksModelService.getInstance().wipeDatabaseData();
								ContentReloadable currentFragment = (ContentReloadable) self.getSupportFragmentManager().findFragmentById(
										R.id.appView);
								if (currentFragment != null) {
									currentFragment.reloadContent();
								}
							}
						})
						.setNegativeButton("No", null)
						.show();
				break;

			default:
				Log.i(LOG_TAG, "onOptionsItemSelected Item " + item.getTitle());
		}

		return false;
	}



	/**
	 * Public FanView getter so the fragments can switch main fragment
	 */
	public FanView getFanView() {
		return (FanView) findViewById(R.id.fan_view);
	}





	private ServiceConnection modelServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			ModelServiceBinder binder = (ModelServiceBinder) service;

			Log.d(LOG_TAG, "Model service connected");

			// init service objects
			binder.getService().initDataSources(self);

			// reload content if the current fragment is LoadingFragment
			Fragment currentFragment = self.getSupportFragmentManager().findFragmentById(R.id.appView);
			if (currentFragment != null && currentFragment instanceof LoadingFragment) {
				FanView fan = (FanView) findViewById(R.id.fan_view);
				TaskListFragment frag = TaskListFragment.newInstance(null, R.string.frag_title_next);

				// replace without history
				fan.replaceMainFragment(frag, false);
			}
			else {
				Log.d(LOG_TAG, "LoadingFragment not current upon ModelService bind. Reload cancelled");
			}
		}


		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.d(LOG_TAG, "Model service disconnected");
		}
	};


}
