package cz.fit.next.sidebar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deaux.fan.FanView;

import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.projectlist.ProjectListFragment;
import cz.fit.next.tasklist.Filter;
import cz.fit.next.tasklist.TaskListFragment;

public class SidebarFragment extends Fragment {

	private final static String LOG_TAG = "SidebarFragment";

	/**
	 * IDs of fixed menu items
	 */
	int menuFixedItemsId[] = {
			R.id.Time_Next, R.id.Time_Today, R.id.Time_InPlan, R.id.Time_Someday, R.id.Time_Blocked,
			 R.id.Projects_ShowProjects
	};
	
	int menuFloatItemsId[];




	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View sideBarView = inflater.inflate(R.layout.sidebar_fragment, container, false);
		//TODO generate views for menu fixed items
		sideBarView = setFixedItemsSidebar(sideBarView);
		
		//TODO load contexts from database
		Cursor cursor = TasksModelService.getInstance().getContextsCursor();
		//TODO Generate views for contexts		
		ArrayList<Integer> contextsId = generateContextsView(cursor, sideBarView);


		// load starred projects
		Cursor starredProjects = TasksModelService.getInstance().getStarredProjectsCursor();
		while (!starredProjects.isAfterLast()) {

		}



		return sideBarView;
	}

	/**
	 *  Generate TextView for each contexts from database
	 * @param cursor
	 */
	private ArrayList<Integer> generateContextsView(Cursor cursor, View sideBarView) {
		// get parent layout of contexts
		ArrayList<Integer> contextsId = new ArrayList<Integer>();
		LinearLayout contextsLayout = (LinearLayout) sideBarView.findViewById(R.id.ContextsLayout);
		while (!cursor.isAfterLast()) {
			String context = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_CONTEXT));
			if(context != null) {
				// Create new TextView
				TextView newItem = new TextView(sideBarView.getContext());
				newItem.setText(context);
				// Remember id to textview
				contextsId.add(newItem.getId());
				
				// Add final id to layout
				contextsLayout.addView(newItem);
				
				Log.i("Zpracovavam kuzor", "Ted");
			}
			cursor.moveToNext();
		}
		return contextsId;
	}


	/**
	 * Generate menu layout
	 * 
	 * @param sideBarView View of sidebar
	 * @return sideBar - laout changed with new items and seetings
	 */
	protected View setFixedItemsSidebar(View pSideBarView) {

		for (final int id : menuFixedItemsId) {
			// getView() return root view for fragment
			final TextView item = (TextView) pSideBarView.findViewById(id);
			// set graphic layout of item
			setItemProperties(item);
			//set listener
			setOnItemTouchListener(id, item);
		}
		return pSideBarView;
	}
	
	protected View setFloatItemsSidebar(View pSideBarView) {
		
		//for (final int id : contextsItemId) {
			
	//	}
		return pSideBarView;
	}


	/**
	 * Set onTouchListener to item
	 * @param id R.id of TextView (TextView is one item in list)
	 * @param item (Item in list)
	 */
	protected void setOnItemTouchListener(final Integer id, TextView item) {
		//set on touch event
		item.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					v.setBackgroundColor(Color.parseColor("#00FFFF"));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					v.setBackgroundColor(Color.TRANSPARENT);
					updateContentFromItemClick(id);
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					v.setBackgroundColor(Color.TRANSPARENT);
				}
				return true;
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

				{
					// create filter
					Filter filterToday = new Filter();

					GregorianCalendar from = new GregorianCalendar();
					from.set(Calendar.HOUR_OF_DAY, 0);
					from.set(Calendar.MINUTE, 0);
					from.set(Calendar.SECOND, 0);
					filterToday.setDateFrom(from);

					GregorianCalendar until = new GregorianCalendar();
					until.setTimeInMillis(from.getTimeInMillis());
					until.add(Calendar.HOUR_OF_DAY, 24);
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

					GregorianCalendar from = new GregorianCalendar();
					from.setTimeInMillis(DateTime.SOMEDAY_TIMESTAMP);
					filter.setDateFrom(from);

					GregorianCalendar until = new GregorianCalendar();
					until.setTimeInMillis(DateTime.SOMEDAY_TIMESTAMP + 1);
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
		for (int viewId : menuFixedItemsId) {
			getView().findViewById(viewId).setBackgroundResource(android.R.color.transparent);
		}
	}



}
