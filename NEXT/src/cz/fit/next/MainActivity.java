package cz.fit.next;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.deaux.fan.FanView;

import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.TasksModelService.ModelServiceBinder;
import cz.fit.next.backend.sync.LoginActivity;
import cz.fit.next.backend.sync.SyncService;
import cz.fit.next.sidebar.SidebarFragment;
import cz.fit.next.tasklist.TaskListFragment;



public class MainActivity extends Activity {

	private static final String LOG_TAG = "FragmentActivity";

	private final MainActivity self = this;


	protected GestureDetector mGestureDetector;

	protected OnTouchListener mTouchListener;


	protected TasksModelService mModelService;

	protected BroadcastReceiver mReloadReceiver;
	protected BroadcastReceiver mSyncStartReceiver;
	protected BroadcastReceiver mSyncStopReceiver;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.test);



		// setup fan sidebar
		FanView fan = (FanView) findViewById(R.id.fan_view);
		fan.setAnimationDuration(200); // 200ms

		if (savedInstanceState == null) {
			//Fragment fanFrag = new SidebarFragment();
			LoadingFragment fanFrag = LoadingFragment.newInstance();
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
		Intent i = new Intent(this, SyncService.class);
		Bundle b = new Bundle();
		b.putInt("buttonPressed", 0);
		i.putExtras(b);
		this.startService(i);
	}




	@Override
	protected void onStart() {
		super.onStart();

		// restore singleton service reference
		if (mModelService == null && TasksModelService.getInstance() != null)
			mModelService = TasksModelService.getInstance();

		// restore service if needed
		bindModelService();
	}




	@Override
	protected void onResume() {
		super.onResume();

		// hide loading fragment if the service is already ready
		if (mModelService != null)
			hideLoadingFragment();

		// prepare gesture listener for fragments
		mGestureDetector = new GestureDetector(this, new MyGestureDetector(getFanView()));
		mTouchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}
		};
		
		//prepare broadcast receivers	
		mReloadReceiver = new ReloadReceiver();
		IntentFilter filter = new IntentFilter();
        filter.addAction(SyncService.BROADCAST_RELOAD);
        registerReceiver(mReloadReceiver, filter);
        
        mSyncStartReceiver = new SyncStartReceiver();
		IntentFilter filter2 = new IntentFilter();
        filter2.addAction(SyncService.BROADCAST_SYNC_START);
        registerReceiver(mSyncStartReceiver, filter2);
        
        mSyncStopReceiver = new SyncEndReceiver();
		IntentFilter filter3 = new IntentFilter();
        filter3.addAction(SyncService.BROADCAST_SYNC_END);
        registerReceiver(mSyncStopReceiver, filter3);
	}
	
	
	@Override
	protected void onPause() {
		 unregisterReceiver(mReloadReceiver);
		 unregisterReceiver(mSyncStartReceiver);
		 unregisterReceiver(mSyncStopReceiver);
	     super.onPause();
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

		FanView fan = (FanView) findViewById(R.id.fan_view);
		
		switch (item.getItemId()) {
		
			case android.R.id.home:
				fan.showMenu();
				break;

			// Switch to settings fragment
			case R.id.menu_settings:
				Fragment settingsFragment = new SettingsFragment();
				fan.replaceMainFragment(settingsFragment);	
				break;
				
			case R.id.setting_connect_drive:
				// Log.i("Setting", "Google Login");

				// tell synchronization service to choose user account
				Intent i = new Intent(this, LoginActivity.class);
				Bundle b = new Bundle();
				b.putInt("login", 1);
				i.putExtras(b);
				//this.startService(i);
				startActivity(i);


				break;


			case R.id.menu_sync_now:
				// tell synchronization service to start sync
				Intent in = new Intent(this, SyncService.class);
				in.putExtra("SyncAlarm", 1);
				this.startService(in);

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


	/**
	 * Setup gesture detection for given view
	 */
	public void attachGestureDetector(View v) {
		v.setOnTouchListener(mTouchListener);
	}



	/**
	 * Hides the loading fragment if it is visible and replaces it
	 * with default task list fragment, else does nothing
	 */
	public void hideLoadingFragment() {
		FanView fan = (FanView) findViewById(R.id.fan_view);
		Fragment currentFragment = self.getFragmentManager().findFragmentById(R.id.appView);

		if (currentFragment != null && currentFragment instanceof LoadingFragment) {
			// default task list
			TaskListFragment frag = TaskListFragment.newInstance(null, R.string.frag_title_next);

			// replace without history
			fan.replaceMainFragment(frag, false);
		}

		Fragment currentFanFragment = self.getFragmentManager().findFragmentById(R.id.fanView);
		if (currentFanFragment != null && currentFragment instanceof LoadingFragment) {
			//default sidebar
			SidebarFragment sidebar = new SidebarFragment();
			fan.replaceFanFragment(sidebar, false);
		}
	}
	
	
	
	private ServiceConnection modelServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			ModelServiceBinder binder = (ModelServiceBinder) service;

			Log.d(LOG_TAG, "Model service connected");

			// init service objects
			binder.getService().initDataSources(self);

			// reload content if the current fragment is LoadingFragment
			hideLoadingFragment();
		}


		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.d(LOG_TAG, "Model service disconnected");
		}
	};
	
	
	private class ReloadReceiver extends BroadcastReceiver {
		@Override
        public void onReceive(Context context, Intent intent) {
            Fragment f = getFragmentManager().findFragmentById(R.id.appView);
            
            Log.i("BROADCAST", "RELOAD");
            
            if (f instanceof TaskListFragment)
            	((TaskListFragment)f).reload();

        }
	}
	
	private class SyncStartReceiver extends BroadcastReceiver {
		@Override
        public void onReceive(Context context, Intent intent) {
            Log.i("BROADCAST", "START");
        }
	}
	
	private class SyncEndReceiver extends BroadcastReceiver {
		@Override
        public void onReceive(Context context, Intent intent) {
			Log.i("BROADCAST", "STOP");
        }
	}

	
	

}
