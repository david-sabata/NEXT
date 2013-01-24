package cz.fit.next.projectlist;

import com.deaux.fan.FanView;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.backend.sync.SyncService;
import cz.fit.next.history.HistoryFragment;
import cz.fit.next.tasklist.TaskListFragment;

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
		
		// reload title
		getActivity().getActionBar().setTitle(getResources().getString(R.string.projects));

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
	 * Reload projects from service if it is ready
	 */
	public void reloadItems() {
		if (TasksModelService.getInstance() != null)
			setItems(TasksModelService.getInstance().getAllProjectsCursor());
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

		reloadItems();
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
	public void onCreateContextMenu(ContextMenu menu, View list, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, list, menuInfo);

		if (list.getId() == android.R.id.list) {
			AdapterContextMenuInfo itemInfo = (AdapterContextMenuInfo) menuInfo;
			View itemLayout = itemInfo.targetView;
			String tag = itemLayout.getTag() == null ? null : itemLayout.getTag().toString();

			if (tag != Constants.IMPLICIT_PROJECT_NAME) {
				menu.add(Menu.NONE, R.id.action_share, 0, R.string.project_share);
				menu.add(Menu.NONE, R.id.action_delete, 1, R.string.project_delete);
				menu.add(Menu.NONE, R.id.action_showhistory, 1, R.string.show_history);
			}
			else {
				menu.add(Menu.NONE, 0, 0, R.string.no_actions);
			}
		}
	}




	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		SQLiteCursor cursor = (SQLiteCursor) getListAdapter().getItem(info.position);
		final String projId = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));

		switch (item.getItemId()) {

		// share - show sharing fragment
			case R.id.action_share:
				ShareDialog newFragment = new ShareDialog();
				newFragment.setProjId(projId);
				newFragment.show(getActivity().getFragmentManager(), "nextshare");
				break;
				
			case R.id.action_showhistory:
				FanView fan = ((MainActivity) getActivity()).getFanView();
				HistoryFragment fraghist = HistoryFragment.newInstance(HistoryFragment.PROJECT, projId);
				fan.replaceMainFragment(fraghist);
				break;

			// delete - show prompt dialog
			case R.id.action_delete:
				new AlertDialog.Builder(getActivity())
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.project_delete)
						.setMessage(R.string.project_delete_confirm_msg)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								boolean ret = SyncService.getInstance().deleteProject(projId);
								if (ret) { 
									TasksModelService.getInstance().deleteProject(projId);
								} else {
									Context context = SyncService.getInstance().getApplicationContext();
									CharSequence text = "Error while project deleting, check your connection.";
									int duration = Toast.LENGTH_SHORT;
									Toast toast = Toast.makeText(context, text, duration);
									toast.show();
								}
										
								reloadItems();
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.show();
				break;

		}

		return true;
	}





}
