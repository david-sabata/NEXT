package cz.fit.next.tasklist;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cz.fit.next.ContentReloadable;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.R.layout;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.taskdetail.TaskDetailFragment;

public class ContentListFragment extends ListFragment implements ContentReloadable {

	private final static String LOG_TAG = "ContentFragment";




	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.content_list_fragment, container, false);
	}


	public void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume");

		// try to reload items; if the call fails, reload will be
		// called by the activity when the service will be running again
		try {
			reloadContent();
		} catch (RuntimeException e) {
			// ignore and wait for the next call
		}
	}




	public void reloadContent() {
		TasksModelService modelService = TasksModelService.getInstance();
		if (modelService == null)
			throw new RuntimeException("TasksModelService is not running");

		setItems(modelService.getAllItems());
	}


	public void setItems(List<Task> items) {
		Log.d(LOG_TAG, "loading items");

		setListAdapter(new ContentListAdapter(getActivity(), android.R.layout.simple_list_item_1, items));
	}



	public void onListItemClick(ListView l, View v, int position, long id) {

		// get Task
		Task item = (Task) getListAdapter().getItem(position);

		// crate fragment with task detail
		TaskDetailFragment fTask = new TaskDetailFragment(item);

		// replace main fragment with task detail fragment
		MainActivity activity = (MainActivity) getActivity();
		activity.getFanView().replaceMainFragment(fTask);
	}



}
