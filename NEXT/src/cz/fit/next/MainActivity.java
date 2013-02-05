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
import android.preference.PreferenceManager;
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
import cz.fit.next.preferences.SettingsFragment;
import cz.fit.next.preferences.SettingsUtil;
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


	/**
	 * Used to show/hide sync icon in options menu
	 */
	protected boolean isSyncInProgress = false;

	protected boolean isAnimationSet = false;


	protected boolean mIsServiceReady = false;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		// load default pref values from xml to pref object
		// (this will not override user settings)
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		SettingsUtil appSettingsManager = new SettingsUtil(this);
		appSettingsManager.setTheme(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);



		// setup fan sidebar
		FanView fan = (FanView) findViewById(R.id.fan_view);
		fan.setAnimationDuration(200); // 200ms

		if (savedInstanceState == null) {
			Fragment fanFrag = new SidebarFragment();
			TaskListFragment contentFrag = TaskListFragment.newInstance(null, R.string.frag_title_next);

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
		SettingsUtil appSettingsManager = new SettingsUtil(this);
		appSettingsManager.setTheme(this);

		super.onStart();

		// restore singleton service reference
		if (mModelService == null && TasksModelService.getInstance() != null) {
			mModelService = TasksModelService.getInstance();
			mIsServiceReady = true;
		}

		// restore service if needed
		bindModelService();
	}




	@Override
	protected void onResume() {
		SettingsUtil appSettingsManager = new SettingsUtil(this);
		appSettingsManager.setTheme(this);

		super.onResume();

		// notify the current fragment if the service is already ready
		if (mModelService != null) {
			Fragment current = getCurrentFragment();
			if (current instanceof ServiceReadyListener)
				((ServiceReadyListener) current).onServiceReady(mModelService);
		}

		// prepare gesture listener for fragments
		mGestureDetector = new GestureDetector(this, new MyGestureDetector(getFanView()));
		mTouchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				// if the sidebar is open, close it on every touch to content
				FanView fan = getFanView();
				if (fan.isOpen()) {
					fan.showMenu(); // atually means 'toggle'
					return true;
				}

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

		// re-notify fragments about service being ready if so
		if (mIsServiceReady) {
			Log.d(LOG_TAG, "onResume: notifying fragments onServiceReady");

			Fragment side = getSidebarFragment();
			if (side instanceof ServiceReadyListener)
				((ServiceReadyListener) side).onServiceReady(mModelService);

			Fragment curr = getCurrentFragment();
			if (curr instanceof ServiceReadyListener)
				((ServiceReadyListener) curr).onServiceReady(mModelService);
		}
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
		//if (!SyncService.getInstance().isUserLoggedIn())
		//	menu.removeItem(R.id.menu_sync_now);
		return true;
	}




	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem item = menu.findItem(R.id.menu_sync_icon);
		if (item != null) {
			item.setVisible(isSyncInProgress);
		}

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
				// prevent opening sidebar while on settings fragment
				if (!(getCurrentFragment() instanceof SettingsFragment))
					getFanView().showMenu();
				break;

			// Switch to settings fragment
			case R.id.menu_settings:
				SettingsFragment prefFragment = new SettingsFragment();
				FanView fan = getFanView();
				if (fan.isOpen()) {
					fan.showMenu(); // actually toggle
				}
				fan.replaceMainFragment(prefFragment);
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
				new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Wipe database")
						.setMessage("Do you really want to erase all stored data?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								TasksModelService.getInstance().wipeDatabaseData();
							}
						}).setNegativeButton("No", null).show();
				break;
		}

		return false;
	}


	/**
	 * Public FanView getter so the fragments can switch main fragment
	 */
	public FanView getFanView() {
		return (FanView) findViewById(R.id.fan_view);
	}

	public Fragment getCurrentFragment() {
		return getFragmentManager().findFragmentById(R.id.appView);
	}

	public SidebarFragment getSidebarFragment() {
		return (SidebarFragment) getFragmentManager().findFragmentById(R.id.fanView);
	}


	/**
	 * USE THIS INSTEAD OF FanView.replaceMainFragment
	 */
	public void replaceMainFragment(Fragment frag) {
		getFanView().replaceMainFragment(frag);

		if (mIsServiceReady && frag instanceof ServiceReadyListener)
			((ServiceReadyListener) frag).onServiceReady(mModelService);

		Log.d(LOG_TAG, "replace frag to " + frag.getClass().getSimpleName());
	}



	public boolean isServiceReady() {
		return mIsServiceReady;
	}



	/**
	 * Setup gesture detection for given view
	 */
	public void attachGestureDetector(View v) {
		v.setOnTouchListener(mTouchListener);
	}



	private ServiceConnection modelServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			ModelServiceBinder binder = (ModelServiceBinder) service;

			mIsServiceReady = true;

			Log.d(LOG_TAG, "Model service connected");

			// init service objects
			binder.getService().initDataSources(self);

			// reload content if the current fragment is LoadingFragment
			//hideLoadingFragment();

			// notify current fragment (if supported)
			Fragment current = getCurrentFragment();
			if (current instanceof ServiceReadyListener)
				((ServiceReadyListener) current).onServiceReady(binder.getService());

			// notify sidebar fragment
			getSidebarFragment().onServiceReady(binder.getService());
		}


		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.d(LOG_TAG, "Model service disconnected");

			mIsServiceReady = false;
		}
	};




	private class ReloadReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Fragment f = getFragmentManager().findFragmentById(R.id.appView);

			Log.i("BROADCAST", "RELOAD");

			if (f instanceof TaskListFragment)
				((TaskListFragment) f).reload();

		}
	}



	private class SyncStartReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("BROADCAST", "START");
			isSyncInProgress = true;
			invalidateOptionsMenu();
		}
	}



	private class SyncEndReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("BROADCAST", "STOP");
			isSyncInProgress = false;
			invalidateOptionsMenu();
		}
	}




}
