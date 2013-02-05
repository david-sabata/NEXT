package cz.fit.next.inplan;

import android.app.ListFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.ServiceReadyListener;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.taskdetail.TaskDetailFragment;




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





	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// get task id
		SQLiteCursor cursor = (SQLiteCursor) getListAdapter().getItem(position);
		String taskId = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));

		// crate fragment with task detail
		TaskDetailFragment fTask = TaskDetailFragment.newInstance(taskId);

		// replace main fragment with task detail fragment
		MainActivity activity = (MainActivity) getActivity();
		activity.replaceMainFragment(fTask);
	}


}
