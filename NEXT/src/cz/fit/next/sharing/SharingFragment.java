package cz.fit.next.sharing;

import java.io.IOException;
import java.util.ArrayList;

import com.deaux.fan.FanView;

import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.ServiceReadyListener;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.TasksModelService.ModelServiceBinder;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.backend.sync.SyncService;
import cz.fit.next.backend.sync.SyncService.ServiceBinder;
import cz.fit.next.backend.sync.drivers.GDrive.UserPerm;
import cz.fit.next.history.HistoryFragment;
import cz.fit.next.history.ProjectHistoryAdapter;
import cz.fit.next.projectlist.ProjectEditDialog;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class SharingFragment extends ListFragment {

	private static final String ARG_ID = "bundle_id";
	private static final String ARG_TITLE = "bundle_title";
	private String mProjId;
	private String mProjTitle;
	
	private boolean mSyncServiceBound = false;
	private SyncService mSyncService = null;
	
	public static SharingFragment newInstance(String id, String title) {
		SharingFragment frag = new SharingFragment();

		//Log.i("SH", "Starting with " + id + "--->" + title);
		
		Bundle b = new Bundle();
		b.putString(ARG_ID, id);
		b.putString(ARG_TITLE, title);

		frag.setArguments(b);

		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mProjId = args.getString(ARG_ID); // value OR null
			mProjTitle = args.getString(ARG_TITLE);
		}
		
		setHasOptionsMenu(true);
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		getActivity().bindService(new Intent(this.getActivity(), SyncService.class), syncServiceConnection,
		            Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		if (mSyncServiceBound) {
            getActivity().unbindService(syncServiceConnection);
        }
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.sharing_actions, menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// add
		if (item.getItemId() == R.id.action_add) {

			ShareDialog newFragment = new  ShareDialog();
			newFragment.setProjId(mProjId);
			newFragment.show(getActivity().getFragmentManager(), "nextshare");

			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.sharing_list, container, false);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		getActivity().getActionBar().setTitle("Sharing Beta");

		// register long click events
		registerForContextMenu(getListView());

		// register for gestures
		((MainActivity) getActivity()).attachGestureDetector(getListView());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View list, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, list, menuInfo);

		if (list.getId() == android.R.id.list) {
			AdapterContextMenuInfo itemInfo = (AdapterContextMenuInfo) menuInfo;
			View itemLayout = itemInfo.targetView;
			//String tag = itemLayout.getTag() == null ? null : itemLayout.getTag().toString();
			TextView tv = (TextView) itemLayout.findViewById(R.id.permissions);
			
			if (!tv.getText().equals("OWNER")) {
				menu.add(Menu.NONE, R.id.action_unshare, 1, R.string.sharing_unshare);	
			}
		}
	}




	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		UserPerm up = (UserPerm) getListAdapter().getItem(info.position);

		switch (item.getItemId()) {

			case R.id.action_unshare:
	
				if ((SyncService.getInstance().isNetworkAvailable())
						&& (SyncService.getInstance().isUserLoggedIn())) {
					SyncService.getInstance().unshare(mProjId, up.id);
				} else {
					Context context = SyncService.getInstance()
							.getApplicationContext();
					CharSequence text = getResources().getString(R.string.sharing_no_conection);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
	
				break;

		
		}

		return true;
	}
	
	private class ExecTask extends AsyncTask<Void, Void, Void> {

		ArrayList<UserPerm> list;
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				list = mSyncService.getSharingList(mProjId,mProjTitle);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			setListAdapter(new SharingAdapter(getActivity(), 0, list));
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			
			
		}
		
	}
	
	private ServiceConnection syncServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			final ServiceBinder binder = (ServiceBinder) service;

			mSyncService = binder.getService();
			
			ExecTask exe = new ExecTask();
			exe.execute();
			//Log.i("SH","executed");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mSyncServiceBound = false;	
			mSyncService = null;
		}
	};


}
