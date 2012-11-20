package cz.fit.next.tasklist;


import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.database.Constants;


public class TaskListAdapter extends CursorAdapter {

	public TaskListAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}



	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		// checkbox
		CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox);
		int status = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_COMPLETED));
		cb.setChecked(status != 0);

		// task title
		TextView ttl = (TextView) view.findViewById(R.id.title);
		String title = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TITLE));
		ttl.setText(title);

		// date
		TextView dt = (TextView) view.findViewById(R.id.subtitle);
		long date = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DATETIME));
		DateTime datetime = new DateTime(new Date(date));
		dt.setText(datetime.toLocaleString());
	}



	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.task_list_item, null, true);
	}




}
