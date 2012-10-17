package cz.fit.next;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.deaux.fan.FanView;

import cz.fit.next.tasks.Task;

public class ContentListFragment extends ListFragment {

	private final static String LOG_TAG = "ContentFragment";
	private FanView mFan;


	public ContentListFragment(FanView pFan) {
		mFan = pFan;
	}


	// @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.content_list_fragment, container, false);
	}



	public void setItems(List<Task> items) {
		setListAdapter(new ContentListAdapter(getActivity(), android.R.layout.simple_list_item_1, items));
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Log.i("Position", "" + position);

		TaskDetailFragment fTask = new TaskDetailFragment();
		mFan.replaceMainFragment(fTask);
	}



}
