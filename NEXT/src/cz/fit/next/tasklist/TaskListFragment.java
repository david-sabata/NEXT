package cz.fit.next.tasklist;

import android.app.ListFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.Toast;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.ServiceReadyListener;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.taskdetail.TaskDetailFragment;
import cz.fit.next.taskdetail.TaskEditFragment;


/**
 * @author David
 */
public class TaskListFragment extends ListFragment implements ServiceReadyListener {

	private final static String LOG_TAG = "ContentFragment";

	/**
	 * Key to Bundle to store serialized Filter state
	 */
	private static final String ARG_FILTER = "filter";

	/**
	 * Key to Bundle to store fragment title resource ID
	 */
	private static final String ARG_TITLE_RES = "title_res";

	/**
	 * Key to Bundle to store fragment title
	 */
	private static final String ARG_TITLE = "title";


	/**
	 * Active filter or null for all items
	 */
	protected Filter mFilter;

	/**
	 * Title of this fragment - shown in action bar if set.
	 * Either this or mTitle should be set
	 */
	protected int mTitleResId = 0;

	/**
	 * Title of this fragment - show in action bar if set.
	 * Either this or mTitleResId should be set
	 */
	protected String mTitle;


	protected boolean mIsServiceReady = false;



	/**
	 * Create a new instance of TaskListFragment that will
	 * be initialized with given filter (where null means 'show all items')
	 * 
	 * Use ONLY this method to create a new instance!
	 */
	public static TaskListFragment newInstance(Filter filter, int titleResId) {
		TaskListFragment frag = new TaskListFragment();

		Bundle b = new Bundle();
		b.putString(ARG_FILTER, filter != null ? filter.toString() : null);
		b.putInt(ARG_TITLE_RES, titleResId);

		frag.setArguments(b);

		return frag;
	}

	/**
	 * Create a new instance of TaskListFragment that will
	 * be initialized with given filter (where null means 'show all items')
	 * 
	 * Use ONLY this method to create a new instance!
	 */
	public static TaskListFragment newInstance(Filter filter, String title) {
		TaskListFragment frag = new TaskListFragment();

		Bundle b = new Bundle();
		b.putString(ARG_FILTER, filter != null ? filter.toString() : null);
		b.putString(ARG_TITLE, title);

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

		Bundle args = getArguments();
		if (args != null) {
			mTitleResId = args.getInt(ARG_TITLE_RES); // value OR 0
			mTitle = args.getString(ARG_TITLE); // value OR null
			mFilter = Filter.fromString(args.getString(ARG_FILTER));
		}
	}

	/**
	 * Load custom layout
	 */
	// TODO: disabled to use default layout with
	//	@Override
	//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	//		super.onCreateView(inflater, container, savedInstanceState);
	//
	//		return inflater.inflate(R.layout.content_list_fragment, container, false);
	//	}



	@Override
	public void onResume() {
		super.onResume();

		// re-apply filter if service is ready (else wait for event)
		if (mIsServiceReady)
			reload();

		// update actionbar title
		//		if (mTitleResId > 0) {
		//			getActivity().getActionBar().setTitle(mTitleResId);
		//		} else if (mTitle != null) {
		//			getActivity().getActionBar().setTitle(mTitle);
		//		}

		// register long click events
		registerForContextMenu(getListView());

		// register for gestures
		((MainActivity) getActivity()).attachGestureDetector(getListView());
	}


	/**
	 * Reloads tasklist according to current filter
	 */
	public void reload() {
		setFilter(mFilter);

		if (mTitleResId > 0) {
			getActivity().getActionBar().setTitle(mTitleResId);
		} else if (mTitle != null) {
			getActivity().getActionBar().setTitle(mTitle);
		}
	}

	/**
	 * Set filter to the adapter and reload items; pass null for all items
	 */
	public void setFilter(Filter filter) {
		mFilter = filter;

		FilterQueryProvider provider = TasksModelService.getInstance().getTasksFilterQueryProvider();

		TaskListAdapter adapter = (TaskListAdapter) getListAdapter();

		// initial adapter set
		if (adapter == null) {
			Cursor cursor = TasksModelService.getInstance().getAllTasksCursor();
			setListAdapter(new TaskListAdapter(getActivity(), cursor, 0));
			adapter = (TaskListAdapter) getListAdapter();
		}

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



	/**
	 * Reload data only; the title and other stuff should already be set
	 */
	@Override
	public void onServiceReady(TasksModelService s) {
		mIsServiceReady = true;
		reload();
	}





}
