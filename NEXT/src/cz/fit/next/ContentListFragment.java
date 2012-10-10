package cz.fit.next;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cz.fit.next.tasks.Task;

public class ContentListFragment extends ListFragment {

	private final static String LOG_TAG = "ContentFragment";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.content_list_fragment, container, false);
	}



	public void setItems(List<Task> items) {
		setListAdapter(new ContentListAdapter(getActivity(), android.R.layout.simple_list_item_1, items));
	}



}
