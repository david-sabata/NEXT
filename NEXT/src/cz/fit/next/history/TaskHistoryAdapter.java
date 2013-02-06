package cz.fit.next.history;

import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.SettingsProvider;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TaskHistory.HistoryTaskChange;
import cz.fit.next.preferences.SettingsFragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import android.graphics.drawable.Drawable;

public class TaskHistoryAdapter extends ArrayAdapter<TaskHistory> {

	ArrayList<TaskHistory> mData;
	Context mContext;
	HashMap<Integer, Drawable> drawables;
	public TaskHistoryAdapter(Context context, int textViewResourceId,
			ArrayList<TaskHistory> history) {
		super(context, textViewResourceId, history);
		mData = history;
		mContext = context;
		
		
		int[] iconsAttrs = new int[]{R.attr.actionAcceptIcon,
				R.attr.actionCancelIcon, R.attr.actionAddIcon, 
				R.attr.actionEditIcon, R.attr.actionDeletedIcon};
		
		TypedArray iconResources = mContext.getTheme().obtainStyledAttributes(iconsAttrs);
		
	    drawables = new HashMap<Integer, Drawable>();
		
		
		for (int i = 0; i < iconResources.length(); i++) {
				drawables.put(iconsAttrs[i], iconResources.getDrawable(i));
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View vi = convertView;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			vi = inflater.inflate(R.layout.history_item, parent, false);

		TextView title = (TextView) vi.findViewById(R.id.title);
		TextView author = (TextView) vi.findViewById(R.id.author);
		TextView subtitle = (TextView) vi.findViewById(R.id.subtitle);

		// timestamp as title
		title.setText(new DateTime(Long.parseLong(mData.get(position)
				.getTimeStamp())).toLocaleDateTimeString());

		SettingsProvider sp = new SettingsProvider(getContext().getApplicationContext());
		String aname = sp.getString(SettingsFragment.PREF_ACCOUNT_NAME, null);
		
		if (aname != null)
			author.setText(mData.get(position).getAuthor());
		else
			author.setVisibility(0);
		
		

		boolean isCreated = false;
		boolean isCompleted = false;
		boolean isUncompleted = false;
		boolean isDeleted = false;

		String sub = "";
		String subMainAction = ""; // CREATED, COMPLETED, UNCOMPLETED, DELETED
		String subOtherAction = ""; // DESCRIPTION, CONTEXT, DATE, PROJECT, PRIORITY

		for (int i = 0; i < mData.get(position).getChanges().size(); i++) {
			HistoryTaskChange changeItem = mData.get(position).getChanges().get(i); 
			// Created
			if ((changeItem.getName().equals(TaskHistory.TITLE)) 
					&& (changeItem.getOldValue().isEmpty())) {
				
				subMainAction = getContext().getResources().getString(R.string.history_taskcreated);;
				isCreated = true;
				break;
			}

			// Completed
			if ((changeItem.getName().equals(TaskHistory.COMPLETED))
					&& (changeItem.getNewValue().equals("true"))) {
				
				subMainAction = getContext().getResources().getString(R.string.history_taskcompleted);
				isCompleted = true;
			}

			// Uncompleted
			if (changeItem.getName().equals(TaskHistory.COMPLETED)
					&& (changeItem.getNewValue().equals("false"))) {
				
				subMainAction = getContext().getResources().getString(R.string.history_taskuncompleted); 
				isUncompleted = true;
			}

			// Date
			if (changeItem.getName().equals(TaskHistory.DATE)) {
				subOtherAction +=  getContext().getResources().getString(R.string.history_date);
			}

			// Context
			if (changeItem.getName().equals(TaskHistory.CONTEXT)) {
				subOtherAction +=  getContext().getResources().getString(R.string.history_context);
			}
			
			// Priority
			if (changeItem.getName().equals(TaskHistory.PRIORITY)) {
				subOtherAction +=  getContext().getResources().getString(R.string.history_priority);
			}
			
			// Title
			if (changeItem.getName().equals(TaskHistory.TITLE)) {
				subOtherAction += getContext().getResources().getString(R.string.history_title);
			}
			
			// Project
			if (changeItem.getName().equals(TaskHistory.PROJECT)) {
				subOtherAction += getContext().getResources().getString(R.string.history_project);
			}
			
			// Description
			if (changeItem.getName().equals(TaskHistory.DESCRIPTION)) {
				subOtherAction += getContext().getResources().getString(R.string.history_description);
			}
			
			// If no one of main actions wasnt applicated, than add "," to text
			if (!isCreated && 
				!isCompleted && 
				!isUncompleted && 
				!isDeleted && 
				i != mData.get(position).getChanges().size() - 1 ){
				subOtherAction += ", ";
			}
		}
		
		// Special texts for special events (if they are unique)
		if(subOtherAction.equals("")) {
			if(isCreated) {
				subMainAction = getContext().getResources().getString(R.string.history_taskcreated); 
			} else if(isCompleted) {
				subMainAction = getContext().getResources().getString(R.string.history_taskcompleted); 
			} else if(isUncompleted) {
				subMainAction = getContext().getResources().getString(R.string.history_taskuncompleted); 
			} else if(isDeleted) {
				subMainAction = getContext().getResources().getString(R.string.history_taskdeleted); 
			}	
		} else if (!isCreated && !isCompleted && !isUncompleted && !isDeleted){
			sub = getContext().getResources().getString(R.string.history_prechanged);
		} else {
			subMainAction += getContext().getResources().getString(R.string.history_postchanged);
		}
		
		sub = sub + subMainAction + subOtherAction  +  ".";
		
		subtitle.setText(sub);

		// Images
		ImageView img = (ImageView) vi.findViewById(R.id.history_image);

		if (isCreated)
			img.setImageDrawable(drawables.get(R.attr.actionAddIcon));
		else if (isDeleted)
			img.setImageDrawable(drawables.get(R.attr.actionDeletedIcon));
		else if (isCompleted)
			img.setImageDrawable(drawables.get(R.attr.actionAcceptIcon));
		else if (isUncompleted)
			img.setImageDrawable(drawables.get(R.attr.actionCancelIcon));
		else
			img.setImageDrawable(drawables.get(R.attr.actionEditIcon));

		return vi;
	}

}
