package cz.fit.next;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SidebarFragment extends Fragment {

	// Fixed menu items - theirs IDs
	int menuItemsId[] = { R.id.Time_Next, R.id.Time_Today, R.id.Time_InPlan, R.id.Time_Sometimes, R.id.Time_Blocked,
			R.id.Context_Home, R.id.Context_Work, R.id.Context_FreeTime, R.id.Projects_ShowProjects };

	// Hash map for item of menu
	Map<Integer, View> menuItemViews = new HashMap<Integer, View>();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View sideBarView = inflater.inflate(R.layout.sidebar_fragment, container, false);

		// set items of sidebar
		sideBarView = setItemsSidebar(sideBarView);

		return sideBarView;
	}


	/**
	 * Generate menu layout
	 * 
	 * @param sideBarView
	 *            View of sidebar
	 * @return sideBar - laout changed with new items and seetings
	 */
	protected View setItemsSidebar(View pSideBarView) {
		/*
		 * find view by id in sideBarView initialize TitleByTime items
		 */
		for (final int id : menuItemsId) {
			// getView() return root view for fragment
			final TextView item = (TextView) pSideBarView.findViewById(id);

			// set graphic layout of item
			setItemProperties(item);

			// set on click event
			item.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) { // TODO Auto-generated method stub
					resetAllActiveClicks();
					item.setBackgroundResource(R.color.FanItemsBackgroundColor);
					areaOfInterestSelector(id);
				}
			});

			// remember new item settings
			menuItemViews.put(id, item);
		}

		return pSideBarView;

	}


	/**
	 * ActivitySelector
	 * 
	 * @brief This method switch between activities on id basement
	 * @param id
	 */
	protected void areaOfInterestSelector(int id) {
		switch (id) {
		// fixed item of menu
			case R.id.Time_Next:
				Log.i("FixedMenu - Time", "Next");
				break;
			case R.id.Time_Today:
				Log.i("FixedMenu - Time", "Today");
				break;
			case R.id.Time_InPlan:
				Log.i("FixedMenu - Time", "In plan");
				break;
			case R.id.Time_Sometimes:
				Log.i("FixedMenu - Time", "Sometimes");
				break;
			case R.id.Time_Blocked:
				Log.i("FixedMenu - Time", "Blocked");
				break;
			case R.id.Projects_ShowProjects:
				Log.i("FixedMenu - Projects", "Show Projects");
				break;
			default:
				// user defined items of menu
				Log.i("Flow menu - Context", "User defined");
				/*
				 * TODO send id of TextView - area interest -> we have to have
				 * some global variable (maybe HashMap) to stored context
				 * between user defined contexts and area of interests It could
				 * be possible saved in JSON
				 */
				break;
		}

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
	 * Set background of all items in menu to transparent
	 */
	protected void resetAllActiveClicks() {
		for (View item : menuItemViews.values()) {
			item.setBackgroundResource(android.R.color.transparent);
		}
	}
}
