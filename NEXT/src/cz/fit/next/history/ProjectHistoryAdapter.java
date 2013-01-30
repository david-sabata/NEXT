package cz.fit.next.history;

import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.sync.SyncService;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProjectHistoryAdapter extends ArrayAdapter<TaskHistory> {

	ArrayList<TaskHistory> mData;
	Context mContext;

	private HashMap<String, String> fieldnames;
	private HashMap<String, String> titlecache;

	public ProjectHistoryAdapter(Context context, int textViewResourceId,
			ArrayList<TaskHistory> history) {
		super(context, textViewResourceId, history);

		mData = history;
		mContext = context;

		// init titlecache
		titlecache = new HashMap<String, String>();

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

		// Title --> name of task
		Task t = TasksModelService.getInstance().getTaskById(
				mData.get(position).getTaskId());
		if (t != null) {
			title.setText(t.getTitle());
		} else {
			// deleted task - we cant find any info in DB - lets inspect history
			// for some titles
			if (titlecache.get(mData.get(position).getTaskId()) != null) {
				title.setText(titlecache.get(mData.get(position).getTaskId())
						+ " ("
						+ getContext().getResources().getString(
								R.string.deleted) + ")");
			} else {
				// can´t find it in title cache - search in current record
				String tit = null;
				for (int i = 0; i < mData.get(position).getChanges().size(); i++) {
					if (mData.get(position).getChanges().get(i).getName()
							.equals(TaskHistory.TITLE))
						tit = mData.get(position).getChanges().get(i)
								.getNewValue();
				}

				if (tit != null) {
					title.setText(tit
							+ " ("
							+ getContext().getResources().getString(
									R.string.deleted) + ")");
				} else {
					// not found -> fallback
					title.setText("*DELETED TASK*");
				}

			}
		}

		// Author + timestamp
		String sub = "";
		if (SyncService.getInstance().isUserLoggedIn())
			sub = mData.get(position).getAuthor();
		
		sub = sub
				+ "\n"
				+ new DateTime(Long.parseLong(mData.get(position)
						.getTimeStamp())).toLocaleDateTimeString();

		author.setText(sub);

		// Changes in tasks
		sub = "";

		boolean isCreated = false;
		boolean isCompleted = false;
		boolean isUncompleted = false;
		boolean isDeleted = false;

		for (int i = 0; i < mData.get(position).getChanges().size(); i++) {

			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.TITLE))
					&& (mData.get(position).getChanges().get(i).getOldValue()
							.isEmpty())) {
				sub = sub
						+ getContext().getResources().getString(
								R.string.task_created) + "\n";

				isCreated = true;

			}
			
			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.TITLE))
					&& (mData.get(position).getChanges().get(i).getNewValue()
							.matches(TasksModelService.deletedTitlePrefix + ".*"))) {
				sub = sub
						+ getContext().getResources().getString(
								R.string.task_deleted) + "\n";

				isDeleted = true;
				break;

			}

			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.COMPLETED))
					&& (mData.get(position).getChanges().get(i).getNewValue()
							.equals("true"))) {
				sub = sub
						+ getContext().getResources().getString(
								R.string.task_completed) + "\n";

				isCompleted = true;

			}

			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.COMPLETED))
					&& (mData.get(position).getChanges().get(i).getNewValue()
							.equals("false"))) {
				sub = sub
						+ getContext().getResources().getString(
								R.string.task_uncompleted) + "\n";

				isUncompleted = true;

			}

			if (mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.DATE)) {
				sub = sub
						+ fieldnames.get(mData.get(position).getChanges()
								.get(i).getName())
						+ " -> "
						+ new DateTime(Long.parseLong(mData.get(position)
								.getChanges().get(i).getNewValue()))
								.toLocaleDateTimeString() + "\n";
			}

			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.CONTEXT))
					|| (mData.get(position).getChanges().get(i).getName()
							.equals(TaskHistory.PRIORITY))
					|| (mData.get(position).getChanges().get(i).getName()
							.equals(TaskHistory.TITLE))) {

				sub = sub
						+ fieldnames.get(mData.get(position).getChanges()
								.get(i).getName()) + " -> "
						+ mData.get(position).getChanges().get(i).getNewValue()
						+ "\n";
			}

			// fill in title in cache
			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.TITLE))) {
				titlecache.put(mData.get(position).getTaskId(),
						mData.get(position).getChanges().get(i).getNewValue());
			}

		}

		subtitle.setText(sub);

		// Image
		ImageView img = (ImageView) vi.findViewById(R.id.history_image);

		if (isCreated)
			img.setImageResource(R.drawable.action_add);
		else if (isDeleted)
			img.setImageResource(R.drawable.action_discard);
		else if (isCompleted)
			img.setImageResource(R.drawable.action_accept);
		else if (isUncompleted)
			img.setImageResource(R.drawable.action_cancel);
		else
			img.setImageResource(R.drawable.action_edit);

		return vi;
	}
}
