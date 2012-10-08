package cz.fit.next;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SidebarFragment extends Fragment {

	// Fixed menu items - theirs IDs
	int fixMenuTimeItems[] = { R.id.Time_Next, R.id.Time_Today, R.id.Time_InPlan, R.id.Time_Sometimes,
			R.id.Time_Blocked, R.id.Context_Home, R.id.Context_Work, R.id.Context_FreeTime };

	// Hash map for item of menu
	Map<Integer, View> menuItemViews = new HashMap<Integer, View>();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View sideBarView = inflater.inflate(R.layout.sidebar_fragment, container, false);

		/*
		 * find view by id in sideBarView initialize TitleByTime items
		 */
		for (final int id : fixMenuTimeItems) {
			// getView() return root view for fragment
			final View item = (View) sideBarView.findViewById(id);

			// set padding of item
			item.setPadding(15, 0, 0, 0);

			// set on click event
			item.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) { // TODO Auto-generated method stub
					Log.i("OnClickListenerItems", "TimeItem");
					resetAllActiveClicks();
					item.setBackgroundResource(R.color.FanItemsBackgroundColor);
				}
			});

			// remember new item settings
			menuItemViews.put(id, item);
		}

		/*
		 * find view by id in sideBarView initialize TitleByContext items
		 */
		/*
		 * for (final int id : fixMenuContextItems) { // getView() return root
		 * view for fragment final View item = (View)
		 * sideBarView.findViewById(id);
		 * 
		 * // set padding of item item.setPadding(15, 0, 0, 0);
		 * 
		 * // set on click event item.setOnClickListener(new
		 * View.OnClickListener() { public void onClick(View v) { // TODO
		 * Auto-generated method stub Log.i("OnClickListenerItems",
		 * "ContextItem"); resetAllActiveClicks();
		 * item.setBackgroundResource(R.color.FanItemsBackgroundColor); } });
		 * 
		 * // remember new item settings menuItemViews.put(id, item); }
		 */

		return sideBarView;
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
