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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.Toast;
import cz.fit.next.ContentReloadable;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.taskdetail.TaskDetailFragment;
import cz.fit.next.taskdetail.TaskEditFragment;


/**
 * This fragment is guaranteed to be instantiated only after 
 * the service has started, so all service calls should be safe
 * 
 * @author David
 */
public class TaskListFragment extends ListFragment implements ContentReloadable {

	private final static String LOG_TAG = "ContentFragment";

	/**
	 * Used in Bundle to store serialized Filter state
	 */
	private static final String ARG_FILTER = "filter";


	/**
	 * Active filter or null for all items
	 */
	protected Filter mFilter;




	/**
	 * Create a new instance of TaskListFragment that will
	 * be initialized with given filter (where null means 'show all items')
	 * 
	 * Use ONLY this method to create a new instance!
	 */
	public static TaskListFragment newInstance(Filter filter) {
		TaskListFragment frag = new TaskListFragment();

		Bundle b = new Bundle();
		b.putString(ARG_FILTER, filter != null ? filter.toString() : null);

		frag.setArguments(b);

		return frag;
	}


	/**
	 * Create default adapter and optionally restore fragment state from Bundle
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		// create deafult adapter - all items
		Cursor cursor = TasksModelService.getInstance().getAllTasksCursor();
		setListAdapter(new TaskListAdapter(getActivity(), cursor));

		Bundle args = getArguments();
		if (args != null) {
			String filterStr = args.getString(ARG_FILTER);
			mFilter = Filter.fromString(filterStr);
		}
	}


	/**
	 * Load custom layout
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.content_list_fragment, container, false);
	}


	@Override
	public void onResume() {
		super.onResume();

		// re-apply filter
		setFilter(mFilter);

		// register long click events
		registerForContextMenu(getListView());
	}




	@Override
	public void reloadContent() {
		Log.d(LOG_TAG, "reloadContent");
		//		TaskListAdapter adapter = (TaskListAdapter) getListAdapter();

		setFilter(mFilter);
	}



	/**
	 * Set filter to the adapter and reload items; pass null for all items
	 */
	public void setFilter(Filter filter) {
		mFilter = filter;

		FilterQueryProvider provider = TasksModelService.getInstance().getTasksFilterQueryProvider();

		TaskListAdapter adapter = (TaskListAdapter) getListAdapter();
		adapter.setFilterQueryProvider(provider);
		adapter.getFilter().filter(mFilter != null ? mFilter.toString() : null);
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




	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.tasklist_actions, menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// edit
		if (item.getItemId() == R.id.action_add) {

			// crate empty edit fragment
			TaskEditFragment fTask = TaskEditFragment.newInstance();

			// replace main fragment with task detail fragment
			MainActivity activity = (MainActivity) getActivity();
			activity.getFanView().replaceMainFragment(fTask);

			return true;
		}


		return super.onOptionsItemSelected(item);
	}


}
