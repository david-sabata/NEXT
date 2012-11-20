package cz.fit.next.sidebar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deaux.fan.FanView;

import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.projectlist.ProjectListFragment;
import cz.fit.next.tasklist.Filter;
import cz.fit.next.tasklist.TaskListFragment;

public class SidebarFragment extends Fragment {

	private final static String LOG_TAG = "SidebarFragment";
	private TextView lastSelected = null;

	/**
	 * IDs of fixed menu items
	 */
	int menuItemsId[] = {
			R.id.Time_Next, R.id.Time_Today, R.id.Time_InPlan, R.id.Time_Sometimes, R.id.Time_Blocked,
			R.id.Context_Home, R.id.Context_Work, R.id.Context_FreeTime, R.id.Projects_ShowProjects
	};




	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View sideBarView = inflater.inflate(R.layout.sidebar_fragment, container, false);
		sideBarView = setItemsSidebar(sideBarView);

		return sideBarView;
	}


	/**
	 * Generate menu layout
	 * 
	 * @param sideBarView View of sidebar
	 * @return sideBar - laout changed with new items and seetings
	 */
	protected View setItemsSidebar(View pSideBarView) {

		for (final int id : menuItemsId) {
			// getView() return root view for fragment
			final TextView item = (TextView) pSideBarView.findViewById(id);

			// set graphic layout of item
			setItemProperties(item);

			// set on click event
			item.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// disabled - David
					//resetAllActiveClicks();
					//item.setBackgroundResource(R.color.FanItemsBackgroundColor);
					updateContentFromItemClick(id);
				}
			});
		}

		return pSideBarView;
	}


	/**
	 * ActivitySelector
	 * 
	 * @brief This method switch between activities on id basement
	 * @param id
	 */
	protected void updateContentFromItemClick(int id) {
		FanView fan = ((MainActivity) getActivity()).getFanView();

		FragmentManager fragmentMgr = getActivity().getSupportFragmentManager();
		Fragment currentFragment = fragmentMgr.findFragmentById(R.id.appView);

		switch (id) {
			case R.id.Time_Next:
				Log.i(LOG_TAG, "selection: Next");

				// create new fragment to add to backstack
				TaskListFragment fragNext = TaskListFragment.newInstance(null, R.string.frag_title_next);
				fan.replaceMainFragment(fragNext);

				break;
			case R.id.Time_Today:
				Log.i(LOG_TAG, "selection: Today");

				// create filter
				Filter filter = new Filter();

				GregorianCalendar from = new GregorianCalendar();
				from.set(Calendar.HOUR_OF_DAY, 0);
				from.set(Calendar.MINUTE, 0);
				from.set(Calendar.SECOND, 0);

				filter.setDateFrom(from);

				// create new fragment to add to backstack
				TaskListFragment fragToday = TaskListFragment.newInstance(filter, R.string.frag_title_today);
				fan.replaceMainFragment(fragToday);

				break;
			case R.id.Time_InPlan:
				Log.i(LOG_TAG, "selection: In plan");
				break;
			case R.id.Time_Sometimes:
				Log.i(LOG_TAG, "selection: Sometimes");
				break;
			case R.id.Time_Blocked:
				Log.i(LOG_TAG, "selection: Blocked");
				break;
			case R.id.Projects_ShowProjects:
				Log.i(LOG_TAG, "selection: Show Projects");

				// create new fragment only if needed
				if (!(currentFragment instanceof ProjectListFragment)) {
					fan.replaceMainFragment(new ProjectListFragment());
				}
				else {
					((ProjectListFragment) currentFragment).reloadContent();
				}

				break;
			default:
				// user defined items of menu
				Log.i(LOG_TAG, "selection: User defined");
				/*
				 * TODO send id of TextView - area interest -> we have to have
				 * some global variable (maybe HashMap) to stored context
				 * between user defined contexts and area of interests It could
				 * be possible saved in JSON
				 */
				break;
		}
		
		// Reset color of last checked to transparent
		if(lastSelected != null) {
			lastSelected.setBackgroundColor(Color.TRANSPARENT);
		}
		
		// Set background of actual selected item to blue
		TextView itemSelected = (TextView) fan.findViewById(id);
		itemSelected.setBackgroundColor(Color.parseColor("#8FB5D0"));

		lastSelected = itemSelected;
		// always toggle sidebar
		fan.showMenu();
	}

	protected void setItemProperties(TextView item) {

		// set dafault icon if nothing is set
		/*
		 * if (item.getDrawableState().length == 1) {
		 * item.setCompoundDrawablesWithIntrinsicBounds
		 * (R.drawable.menu_sometimes, 0, 0, 0); }
		 */

		// set padding of item
		item.setPadding(15, 0, 0, 0);
		// set gravity to vertical center
		item.setGravity(Gravity.CENTER_VERTICAL);
	}


	/**
	 * Reset background of all items in menu to transparent
	 */
	protected void resetAllActiveClicks() {
		for (int viewId : menuItemsId) {
			getView().findViewById(viewId).setBackgroundResource(android.R.color.transparent);
		}
	}



}
