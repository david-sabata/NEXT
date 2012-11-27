package cz.fit.next.projectlist;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cz.fit.next.R;
import cz.fit.next.backend.TasksModelService;

public class ProjectListFragment extends ListFragment {

	private final static String LOG_TAG = "ProjectListFragment";




	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.content_list_fragment, container, false);
	}


	@Override
	public void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume");

		// try to reload items; if the call fails, reload will be
		// called by the activity when the service will be running again
		try {
			setItems(TasksModelService.getInstance().getAllProjectsCursor());
		} catch (RuntimeException e) {
			// ignore and wait for the next call
		}
	}



	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.v(LOG_TAG, "item click");
	}




	public void setItems(Cursor cursor) {
		Log.d(LOG_TAG, "loading items");

		setListAdapter(new ProjectListAdapter(getActivity(), cursor, 0));
	}





}
