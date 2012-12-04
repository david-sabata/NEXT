package cz.fit.next.sidebar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deaux.fan.FanView;
import com.deaux.fan.SidebarListener;

import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.projectlist.ProjectListFragment;
import cz.fit.next.tasklist.Filter;
import cz.fit.next.tasklist.TaskListFragment;

public class SidebarFragment extends Fragment {

	private final static String LOG_TAG = "SidebarFragment";
	private View sideBarView;

	/**
	 * IDs of fixed menu items
	 */
	int menuFixedItemsId[] = {
			R.id.Time_Next, R.id.Time_Today, R.id.Time_InPlan, R.id.Time_Someday, R.id.Time_Blocked,
			R.id.Projects_ShowProjects
	};



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Load layout for sidebar
		sideBarView = inflater.inflate(R.layout.sidebar_fragment, container, false);

		// Generate fixed item in sidebar
		sideBarView = setFixedItemsSidebar(sideBarView);

		// Generate context items in sidebar
		initSideBarContextProjects();

		return sideBarView;
	}

	/**
	 * Set options of fixed item in menu sidebar
	 * @param sideBarView View of menu sidebar
	 * @return sideBar - layout changed with new items
	 */
	protected View setFixedItemsSidebar(View pSideBarView) {
		for (final int id : menuFixedItemsId) {
			final TextView item = (TextView) pSideBarView.findViewById(id);

			// Set onClickListener to item -> it will switch fragment
			item.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					updateContentFromItemClick(id);
				}
			});
		}
		return pSideBarView;
	}


	/**
	 * Init sidebar contexts and projects
	 */
	public void initSideBarContextProjects() {
		LayoutInflater inflater = (LayoutInflater) sideBarView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Load contexts from database
		Cursor cursor = TasksModelService.getInstance().getContextsCursor();

		// Get pointer to layout of contexts and clean it before adding new items
		LinearLayout contextsLayout = (LinearLayout) sideBarView.findViewById(R.id.ContextsLayout);
		contextsLayout.removeAllViews();

		// Adding new items to contexts layout
		final Context c = sideBarView.getContext();
		if (cursor != null && cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				final String contextTitle = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_CONTEXT));

				if (contextTitle != null && !contextTitle.equals("")) {
					// Create new TextView
					LinearLayout itemLayout = (LinearLayout) inflater.inflate(R.layout.sidebar_item_layout, null);
					TextView newItem = (TextView) itemLayout.findViewById(R.id.sidebarItem);
					newItem.setText(contextTitle);

					// Set Action on Item click
					newItem.setOnClickListener(mContextOnClickListener);
					// Add final id to layout
					contextsLayout.addView(itemLayout);
				}
				cursor.moveToNext();
			}
		}

		// Load starred projects from database
		Cursor starredProjects = TasksModelService.getInstance().getStarredProjectsCursor();

		// Get pointer to layout of projects and clean it before adding new starred projects
		LinearLayout projectsLayout = (LinearLayout) sideBarView.findViewById(R.id.projects);
		projectsLayout.removeAllViews();

		if (starredProjects != null && starredProjects.getCount() > 0) {
			// Adding new starred projects to projects layout
			while (!starredProjects.isAfterLast()) {
				final String projectTitle = starredProjects.getString(starredProjects.getColumnIndex(Constants.COLUMN_TITLE));
				final String projectId = starredProjects.getString(starredProjects.getColumnIndex(Constants.COLUMN_ID));

				if (projectId != null) {
					// Create new TextView
					LinearLayout itemLayout = (LinearLayout) inflater.inflate(R.layout.sidebar_item_layout, null);
					TextView newItem = (TextView) itemLayout.findViewById(R.id.sidebarItem);
					newItem.setTag(R.id.project, projectId);

					// use localized string for default project
					if (projectTitle.equals(Constants.IMPLICIT_PROJECT_NAME)) {
						newItem.setText(R.string.implicit_project);
					}
					else {
						newItem.setText(projectTitle);
					}

					// Set Action on Item click
					newItem.setOnClickListener(mProjectOnClickListener);
					// Add final id to layout
					projectsLayout.addView(itemLayout);
				}
				starredProjects.moveToNext();
			}
		}
	}




	@Override
	public void onResume() {
		super.onResume();

		MainActivity activity = (MainActivity) getActivity();

		FanView f = activity.getFanView();
		f.setSidebarListener(new SidebarListener() {
			@Override
			public void onSidebarOpen() {

				// Regenerate contexts and projects in sidebar menu
				initSideBarContextProjects();
			}

			@Override
			public void onSidebarClose() {
			}
		});
	}



	/**
	 * ActivitySelector
	 * 
	 * @brief This method switch between activities on id basement
	 * @param id
	 */
	protected void updateContentFromItemClick(int id) {
		FanView fan = ((MainActivity) getActivity()).getFanView();

		switch (id) {
			case R.id.Time_Next:
				Log.i(LOG_TAG, "selection: Next");

				// create new fragment to add to backstack
				TaskListFragment fragNext = TaskListFragment.newInstance(null, R.string.frag_title_next);
				fan.replaceMainFragment(fragNext);

				break;
			case R.id.Time_Today:
				Log.i(LOG_TAG, "selection: Today");

				{
					// create filter
					Filter filterToday = new Filter();

					DateTime from = new DateTime();
					from.setTime(0, 0);
					filterToday.setDateFrom(from);

					GregorianCalendar untilCal = new GregorianCalendar();
					untilCal.setTimeInMillis(from.toCalendar().getTimeInMillis());
					untilCal.add(Calendar.HOUR_OF_DAY, 24);
					DateTime until = new DateTime(untilCal.getTimeInMillis());
					filterToday.setDateUntil(until);

					// create new fragment to add to backstack
					TaskListFragment fragToday = TaskListFragment.newInstance(filterToday, R.string.frag_title_today);
					fan.replaceMainFragment(fragToday);
				}

				break;
			case R.id.Time_InPlan:
				Log.i(LOG_TAG, "selection: In plan");
				break;
			case R.id.Time_Someday:
				Log.i(LOG_TAG, "selection: Someday");
				{
					Filter filter = new Filter();

					DateTime from = new DateTime();
					from.setIsSomeday(true);
					filter.setDateFrom(from);

					DateTime until = new DateTime();
					until.setIsSomeday(true);
					filter.setDateUntil(until);

					TaskListFragment frag = TaskListFragment.newInstance(filter, R.string.frag_title_someday);
					fan.replaceMainFragment(frag);
				}
				break;
			case R.id.Time_Blocked:
				Log.i(LOG_TAG, "selection: Blocked");
				break;
			case R.id.Projects_ShowProjects:
				Log.i(LOG_TAG, "selection: Show Projects");

				// replace current fragment
				fan.replaceMainFragment(new ProjectListFragment());

				break;
			default:

				break;
		}

		// always toggle sidebar
		fan.showMenu();
	}


	/**
	 * Reset background of all items in menu to transparent
	 */
	protected void resetAllActiveClicks() {
		for (int viewId : menuFixedItemsId) {
			getView().findViewById(viewId).setBackgroundResource(android.R.color.transparent);
		}
	}




	/**
	 * Project onclick
	 */
	protected OnClickListener mProjectOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			TextView item = (TextView) v.findViewById(R.id.sidebarItem);
			String projectTitle = item.getText().toString();
			String projectId = item.getTag(R.id.project).toString();

			Filter f = new Filter();
			f.setProjectId(projectId);

			// open new fragment
			TaskListFragment frag = TaskListFragment.newInstance(f, projectTitle);
			FanView fan = ((MainActivity) getActivity()).getFanView();
			fan.replaceMainFragment(frag);
		}
	};

	/**
	 * Context onclick
	 */
	protected OnClickListener mContextOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			TextView item = (TextView) v.findViewById(R.id.sidebarItem);
			String contextTitle = item.getText().toString();

			Filter f = new Filter();
			f.setContext(contextTitle);

			// open new fragment
			TaskListFragment frag = TaskListFragment.newInstance(f, contextTitle);
			FanView fan = ((MainActivity) getActivity()).getFanView();
			fan.replaceMainFragment(frag);
		}
	};




}
