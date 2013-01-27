package cz.fit.next.projectlist;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.ServiceReadyListener;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;

public class ProjectListFragment extends ListFragment implements ServiceReadyListener {

	private final static String LOG_TAG = "ProjectListFragment";


	protected boolean mIsServiceReady = false;



	//	@Override
	//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	//		super.onCreateView(inflater, container, savedInstanceState);
	//
	//		return inflater.inflate(R.layout.content_list_fragment, container, false);
	//	}


	@Override
	public void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume");


		if (mIsServiceReady) {
			reloadItems();
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
	private void setItems(Cursor cursor) {
		setListAdapter(new ProjectListAdapter(getActivity(), cursor, 0));
	}

	/**
	 * Reload projects from service if it is ready
	 */
	public void reloadItems() {
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
			} else {
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

			// delete - show prompt dialog
			case R.id.action_delete:
				new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.project_delete)
						.setMessage(R.string.project_delete_confirm_msg).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								TasksModelService.getInstance().deleteProject(projId);
								reloadItems();
							}
						}).setNegativeButton(android.R.string.no, null).show();
				break;

		}

		return true;
	}




	@Override
	public void onServiceReady(TasksModelService s) {
		mIsServiceReady = true;

		// need to check if activity exists - because of async fragment switching 
		// onAttached might not be called yet
		if (getActivity() != null)
			reloadItems();
	}




}
