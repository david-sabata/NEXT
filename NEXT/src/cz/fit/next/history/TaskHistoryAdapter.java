package cz.fit.next.history;

import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.TaskHistory;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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

		author.setText(mData.get(position).getAuthor());

		// Image
		ImageView img = (ImageView) vi.findViewById(R.id.history_image);

		String sub = "";

		for (int i = 0; i < mData.get(position).getChanges().size(); i++) {

			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.TITLE))
					&& (mData.get(position).getChanges().get(i).getOldValue()
							.isEmpty())) {
				sub = sub + "TASK CREATED" + "\n";

				img.setImageResource(R.drawable.action_add);

				break;
			}

			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.COMPLETED))
					&& (mData.get(position).getChanges().get(i).getNewValue()
							.equals("true"))) {
				sub = sub + "TASK MARKED AS COMPLETE" + "\n";

				img.setImageResource(R.drawable.action_accept);
			}

			if ((mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.COMPLETED))
					&& (mData.get(position).getChanges().get(i).getNewValue()
							.equals("false"))) {
				sub = sub + "TASK MARKED AS UNCOMPLETE" + "\n";

				img.setImageResource(R.drawable.action_cancel);
			}

			if (mData.get(position).getChanges().get(i).getName()
					.equals(TaskHistory.DATE)) {
				sub = sub
						+ fieldnames.get(mData.get(position).getChanges()
								.get(i).getName()) + " -> "		
						+ new DateTime(Long.parseLong(mData.get(position).getChanges().get(i).getNewValue())).toLocaleDateTimeString()
						+ "\n";
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
		}

		subtitle.setText(sub);
		return vi;
	}

}