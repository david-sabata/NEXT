package cz.fit.next.history;

import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.SettingsProvider;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.preferences.SettingsFragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskHistoryAdapter extends ArrayAdapter<TaskHistory> {

	ArrayList<TaskHistory> mData;
	Context mContext;

	private HashMap<String, String> fieldnames;

	public TaskHistoryAdapter(Context context, int textViewResourceId,
			ArrayList<TaskHistory> history) {
		super(context, textViewResourceId, history);

		mData = history;
		mContext = context;

		// fill in field names translator
		fieldnames = new HashMap<String, String>();

		fieldnames.put(TaskHistory.TITLE, getContext().getResources()
				.getString(R.string.history_title));
		fieldnames.put(TaskHistory.CONTEXT, getContext().getResources()
				.getString(R.string.history_context));
		fieldnames.put(TaskHistory.DATE,
				getContext().getResources().getString(R.string.history_date));
		fieldnames.put(TaskHistory.DESCRIPTION, getContext().getResources()
				.getString(R.string.history_description));
		fieldnames.put(TaskHistory.PRIORITY, getContext().getResources()
				.getString(R.string.history_priority));

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
			// Created
			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.TITLE))
					&& (mData.get(position).getChanges().get(i).getOldValue()
							.isEmpty())) {
				subMainAction = "Task has been created";
				isCreated = true;
				break;
			}

			// Completed
			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.COMPLETED))
					&& (mData.get(position).getChanges().get(i).getNewValue()
							.equals("true"))) {
				subMainAction = "Task has been marked as completed";
				isCompleted = true;
			}

			// Uncompleted
			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.COMPLETED))
					&& (mData.get(position).getChanges().get(i).getNewValue()
							.equals("false"))) {
				subMainAction = "Task has been marked as uncompleted";
				isUncompleted = true;
			}

			if (mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.DATE)) {
				subOtherAction +=  getContext().getResources().getString(R.string.task_history_date);
				
			}

			if (mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.CONTEXT)) {
				subOtherAction +=  getContext().getResources().getString(R.string.task_history_context);
			}
			
			if (mData.get(position).getChanges().get(i).getName()
							.equals(TaskHistory.PRIORITY)) {
				subOtherAction =  getContext().getResources().getString(R.string.task_history_priority);
			}
			
			if (mData.get(position).getChanges().get(i).getName()
							.equals(TaskHistory.TITLE)) {

				subOtherAction += getContext().getResources().getString(R.string.task_history_title);
			}
			
			if (!isCreated && 
				!isCompleted && 
				!isUncompleted && 
				!isDeleted && 
				i != mData.get(position).getChanges().size() -1 ){
				subOtherAction += ", ";
			}
			
			
						
		}
		
		// Special texts for special events (if they are unique)
		if(subOtherAction.equals("")) {
			if(isCreated) {
				subMainAction = "Task has been created"; // TODO change to variable string
			} else if(isCompleted) {
				subMainAction = "Task has been marked as completed"; // TODO change to variable string
			} else if(isUncompleted) {
				subMainAction = "Task has been marked as uncompleted"; // TODO change to variable string
			} else if(isDeleted) {
				subMainAction = "Task has been deleted"; // TODO change to variable string
			}	
		} else if (!isCreated && !isCompleted && !isUncompleted && !isDeleted){
			sub = "Changed in ";
		} else {
			subMainAction += " and changed in ";
		}
		
		sub = sub + subMainAction + subOtherAction  +  ".";
		
		subtitle.setText(sub);

		// Image
		ImageView img = (ImageView) vi.findViewById(R.id.history_image);

		if (isCreated)
			img.setImageResource(R.drawable.action_add_light);
		else if (isDeleted)
			img.setImageResource(R.drawable.action_discard_light);
		else if (isCompleted)
			img.setImageResource(R.drawable.action_accept_light);
		else if (isUncompleted)
			img.setImageResource(R.drawable.action_cancel_light);
		else
			img.setImageResource(R.drawable.action_edit_light);

		return vi;
	}

}
