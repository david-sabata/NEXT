package cz.fit.next.tasklist;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.Filter.FilterListener;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.ListView;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.ServiceReadyListener;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.history.HistoryFragment;
import cz.fit.next.taskdetail.TaskDetailFragment;
import cz.fit.next.taskdetail.TaskEditFragment;


/**
 * @author David
 */
public class TaskListFragment extends ListFragment implements ServiceReadyListener {

	private final static String LOG_TAG = "TaskListFragment";

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


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		TypedArray background = getActivity().getTheme().obtainStyledAttributes(new int[] { R.attr.listViewBackground });
		v.setBackgroundColor(background.getColor(0, Color.WHITE));

		return v;
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

		setEmptyText(getResources().getString(R.string.all_done));
	}


	/**
	 * Reloads tasklist according to current filter
	 */
	public void reload() {
		MainActivity activity = (MainActivity) getActivity();

		if (activity == null || !activity.isServiceReady()) {
			Log.w(LOG_TAG, "cannot reload items, " + (!activity.isServiceReady() ? "service, " : "") + (activity == null ? "activity" : "") + " is not ready");
			return;
		}

		Log.d(LOG_TAG, "reloading items");

		setFilter(mFilter);

		if (mTitleResId > 0) {
			activity.getActionBar().setTitle(mTitleResId);
		} else if (mTitle != null) {
			activity.getActionBar().setTitle(mTitle);
		}
	}



	/**
	 * Set filter to the adapter and reload items; pass null for all items
	 */
	private void setFilter(Filter filter) {
		mFilter = filter;

		FilterQueryProvider provider = TasksModelService.getInstance().getTasksFilterQueryProvider();

		TaskListAdapter adapter = (TaskListAdapter) getListAdapter();

		// initial adapter set
		if (adapter == null) {
			Cursor cursor = TasksModelService.getInstance().getAllTasksCursor();
			setListAdapter(new TaskListAdapter(getActivity(), cursor, 0));
			adapter = (TaskListAdapter) getListAdapter();
		}

		// filter is asynchronous, we need to set callback to get the right item count
		FilterListener filterCb = new FilterListener() {
			@Override
			public void onFilterComplete(int count) {
				// enable touch detection for 'empty' screen; it cannot be
				// attached all the time, because it is blocking clicks on items
				if (count == 0) {
					MainActivity activity = (MainActivity) getActivity();
					FrameLayout frm = (FrameLayout) getListView().getParent();
					frm.setFocusableInTouchMode(true);
					activity.attachGestureDetector(frm);
				}
			}
		};

		adapter.setFilterQueryProvider(provider);
		adapter.getFilter().filter(mFilter != null ? mFilter.toString() : null, filterCb);
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





	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == android.R.id.list) {
			menu.add(Menu.NONE, R.id.action_edit, 0, R.string.action_edit_task);
			menu.add(Menu.NONE, R.id.action_showhistory, 0, R.string.show_history);
			menu.add(Menu.NONE, R.id.action_delete, 1, R.string.action_delete_task);
		}
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		SQLiteCursor cursor = (SQLiteCursor) getListAdapter().getItem(info.position);
		final String taskId = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));

		MainActivity activity = (MainActivity) getActivity();

		switch (item.getItemId()) {

			case R.id.action_edit:
				// switch to edit fragment (skipping the detail fragment)
				TaskEditFragment fTask = TaskEditFragment.newInstance(taskId);
				activity.replaceMainFragment(fTask);
				break;

			case R.id.action_showhistory:
				HistoryFragment fraghist = HistoryFragment.newInstance(HistoryFragment.TASK, taskId);
				activity.replaceMainFragment(fraghist);
				break;

			case R.id.action_delete:
				new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.task_delete)
						.setMessage(R.string.task_delete_confirm_msg).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								TasksModelService.getInstance().deleteTask(taskId);
								//Reload Items
								Fragment f = getFragmentManager().findFragmentById(R.id.appView);
								if (f instanceof TaskListFragment)
									((TaskListFragment) f).reload();
							}
						}).setNegativeButton(android.R.string.no, null).show();
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
			activity.replaceMainFragment(fTask);

			return true;
		}


		return super.onOptionsItemSelected(item);
	}



	/**
	 * Reload data only; the title and other stuff should already be set
	 */
	@Override
	public void onServiceReady(TasksModelService s) {
		// need to check if activity exists - because of async fragment switching 
		// onAttached might not be called yet
		if (getActivity() != null)
			reload();
	}





}
