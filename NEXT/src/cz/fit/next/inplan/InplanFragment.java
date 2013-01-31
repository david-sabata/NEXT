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

		MainActivity activity = (MainActivity) getActivity();

		// re-apply filter if service is ready (else wait for event)
		if (activity.isServiceReady()) {
			reload();
		}

		// register long click events
		registerForContextMenu(getListView());

		// register for gestures
		activity.attachGestureDetector(getListView());

		// set title
		activity.getActionBar().setTitle(R.string.TitleByTime_InPlan);
	}





	/**
	 * Reloads tasklist according to current filter
	 */
	public void reload() {
		Log.d(LOG_TAG, "reloading items");

		Cursor cursor = TasksModelService.getInstance().getTasksInplanCursor();
		setListAdapter(new InplanAdapter(getActivity(), cursor, 0));
	}




	@Override
	public void onServiceReady(TasksModelService s) {
		if (getActivity() != null)
			reload();
	}

}
