package cz.fit.next;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.deaux.fan.FanView;

public class ProjectListFragment extends ListFragment {

	private final static String LOG_TAG = "ProjectListFragment";

	private FanView mFan;


	public ProjectListFragment() {
		super();
	}


	public ProjectListFragment(FanView pFan) {
		super();

		mFan = pFan;
	}


	// @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.content_list_fragment, container, false);
	}



	public void setItems(Cursor cursor) {
		setListAdapter(new ProjectListAdapter(getActivity(), cursor));
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.v(LOG_TAG, "item click");
	}



}
