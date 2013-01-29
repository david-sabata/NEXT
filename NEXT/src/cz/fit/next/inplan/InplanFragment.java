package cz.fit.next.inplan;

import android.app.ListFragment;
import android.database.Cursor;
import android.util.Log;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.ServiceReadyListener;
import cz.fit.next.backend.TasksModelService;




public class InplanFragment extends ListFragment implements ServiceReadyListener {

	private static final String LOG_TAG = "InplanFragment";

	protected boolean mIsServiceReady = false;



	/**
	 * Use ONLY this method to create a new instance!
	 */
	public static InplanFragment newInstance() {
		InplanFragment frag = new InplanFragment();
		return frag;
	}





	@Override
	public void onResume() {
		super.onResume();

		// re-apply filter if service is ready (else wait for event)
		if (mIsServiceReady) {
			reload();
		}

		// register long click events
		registerForContextMenu(getListView());

		// register for gestures
		((MainActivity) getActivity()).attachGestureDetector(getListView());

		// set title
		getActivity().getActionBar().setTitle(R.string.TitleByTime_InPlan);
	}



	@Override
	public void onPause() {
		super.onPause();

		// assume the service will get disconnected
		mIsServiceReady = false;
	}




	/**
	 * Reloads tasklist according to current filter
	 */
	public void reload() {
		if (!mIsServiceReady || getActivity() == null) {
			Log.w(LOG_TAG, "cannot reload items, " + (!mIsServiceReady ? "service, " : "") + (getActivity() == null ? "activity" : "") + " is not ready");
			return;
		}

		Log.d(LOG_TAG, "reloading items");


		Cursor cursor = TasksModelService.getInstance().getTasksInplanCursor();
		setListAdapter(new InplanAdapter(getActivity(), cursor, 0));
	}




	@Override
	public void onServiceReady(TasksModelService s) {
		mIsServiceReady = true;
	}

}
