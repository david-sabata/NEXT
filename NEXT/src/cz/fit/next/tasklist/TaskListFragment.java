package cz.fit.next.tasklist;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import cz.fit.next.ContentReloadable;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.taskdetail.TaskDetailFragment;
import cz.fit.next.taskdetail.TaskEditFragment;

public class TaskListFragment extends ListFragment implements ContentReloadable {

	private final static String LOG_TAG = "ContentFragment";




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
			reloadContent();
		} catch (RuntimeException e) {
			// ignore and wait for the next call
		}

		// register long click events
		registerForContextMenu(getListView());
	}




	@Override
	public void reloadContent() {
		TasksModelService modelService = TasksModelService.getInstance();
		if (modelService == null)
			throw new RuntimeException("TasksModelService is not running");

		setItems(modelService.getAllTasksCursor());
	}


	public void setItems(Cursor cursor) {
		Log.d(LOG_TAG, "loading items");

		setListAdapter(new TaskListAdapter(getActivity(), cursor));
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
		activity.getFanView().replaceMainFragment(fTask);
	}





	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == android.R.id.list) {
			menu.add(Menu.NONE, R.id.action_edit, 0, R.string.action_edit_task);
			menu.add(Menu.NONE, R.id.action_delete, 1, R.string.action_delete_task);
		}
		else {
			Log.e(LOG_TAG, "fail");
		}
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		SQLiteCursor cursor = (SQLiteCursor) getListAdapter().getItem(info.position);
		String taskId = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));

		switch (item.getItemId()) {

			case R.id.action_edit:
				// switch to edit fragment (skipping the detail fragment)
				TaskEditFragment fTask = TaskEditFragment.newInstance(taskId);
				MainActivity activity = (MainActivity) getActivity();
				activity.getFanView().replaceMainFragment(fTask);
				break;

			case R.id.action_delete:
				Toast.makeText(getActivity(), "Task deletion not implemented yet", Toast.LENGTH_SHORT).show();
				break;
		}

		return true;
	}



}
