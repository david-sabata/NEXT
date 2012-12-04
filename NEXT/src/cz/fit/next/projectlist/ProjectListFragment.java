package cz.fit.next.projectlist;

import android.app.ListFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
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
import android.widget.ListView;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;

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

		setHasOptionsMenu(true);

		// register for gestures
		((MainActivity) getActivity()).attachGestureDetector(getListView());

		// register long click events
		registerForContextMenu(getListView());
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.v(LOG_TAG, "item click");
	}



	/**
	 * Load projects from given adapter
	 * @param cursor
	 */
	public void setItems(Cursor cursor) {
		setListAdapter(new ProjectListAdapter(getActivity(), cursor, 0));
	}


	/**
	 * Callback method called from 'add project' dialog
	 * 
	 * DO NOT CALL FROM ANYWHERE ELSE BUT THE DIALOG
	 * 
	 * @param title
	 */
	public void addProject(String title) {
		Project project = new Project(title);
		TasksModelService.getInstance().saveProject(project);

		// reload items
		setItems(TasksModelService.getInstance().getAllProjectsCursor());
	}




	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.projectlist_actions, menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// add
		if (item.getItemId() == R.id.action_add) {

			ProjectEditDialog dlg = ProjectEditDialog.newInstance();
			dlg.setTargetFragment(this, 0);
			dlg.show(getFragmentManager(), null);

			return true;
		}


		return super.onOptionsItemSelected(item);
	}





	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == android.R.id.list) {
			menu.add(Menu.NONE, R.id.action_share, 0, R.string.proj_share);
		}
		else {
			Log.e(LOG_TAG, "fail");
		}
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		SQLiteCursor cursor = (SQLiteCursor) getListAdapter().getItem(info.position);
		String projId = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));

		switch (item.getItemId()) {

			case R.id.action_share:
				ShareDialog newFragment = new ShareDialog();
				newFragment.setProjId(projId);
				newFragment.show(getActivity().getFragmentManager(), "nextshare");
				break;
		}

		return true;
	}





}
