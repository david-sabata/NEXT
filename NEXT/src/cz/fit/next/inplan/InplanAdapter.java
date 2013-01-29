package cz.fit.next.inplan;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;


/**
 * Lists all planned events (which means it EXCLUDES someday events) grouped 
 * into sections for each day
 * 
 * @author David
 */
public class InplanAdapter extends CursorAdapter implements ListAdapter {

	private static final String LOG_TAG = "InplanAdapter";


	public InplanAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}




	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		Cursor cursor = getCursor();

		long currDate = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DATETIME));
		DateTime currDatetime = new DateTime(currDate);
		int currYear = currDatetime.toCalendar().get(Calendar.YEAR);
		int currDay = currDatetime.toCalendar().get(Calendar.DAY_OF_YEAR);

		// always for the first item
		boolean showHeader = cursor.isFirst();

		// check date differences for others
		if (!showHeader) {
			cursor.moveToPrevious();

			long prevDate = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DATETIME));
			DateTime prevDatetime = new DateTime(prevDate);
			int prevYear = prevDatetime.toCalendar().get(Calendar.YEAR);
			int prevDay = prevDatetime.toCalendar().get(Calendar.DAY_OF_YEAR);

			// return cursor back to actual position
			cursor.moveToPosition(position);

			showHeader = (currDay != prevDay || currYear != prevYear);
		}

		// show/hide section header
		View secHeader = view.findViewById(R.id.sectionHeader);
		if (showHeader) {
			secHeader.setVisibility(View.VISIBLE);
			TextView text = (TextView) secHeader;
			text.setText(currDatetime.toLocaleDateString());
		} else {
			secHeader.setVisibility(View.GONE);
		}

		return view;
	}



	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String id = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));

		// checkbox
		CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox);
		int status = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_COMPLETED));
		cb.setChecked(status != 0);
		cb.setTag(id);

		// checkbox oncheck
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
				final String taskId = buttonView.getTag().toString();

				// update db on background
				new Thread(new Runnable() {
					@Override
					public void run() {
						Task task = TasksModelService.getInstance().getTaskById(taskId);
						Task newTask = new Task(task.getId(), task.getTitle(), task.getDescription(), task.getDate(), task.getPriority(), task.getProject(),
								task.getContext(), isChecked);

						// save
						TasksModelService.getInstance().saveTask(newTask);
					}
				}).start();
			}
		});


		// task title
		TextView ttl = (TextView) view.findViewById(R.id.title);
		String title = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TITLE));
		ttl.setText(title);

		// date
		TextView dt = (TextView) view.findViewById(R.id.subtitle);
		long date = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DATETIME));
		DateTime datetime = new DateTime(date);

		if (!datetime.isAllday()) {
			dt.setText(datetime.toLocaleTimeString());
		}

		// priority
		LinearLayout prl = (LinearLayout) view.findViewById(R.id.TasklistItemPriority);
		if (cursor.getColumnIndex(Constants.COLUMN_PRIORITY) != -1) {
			Integer priority = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PRIORITY)));
			switch (priority) {
				case 1:
					prl.setBackgroundColor(context.getResources().getColor(R.color.priority_1));
					break;
				case 2:
					prl.setBackgroundColor(context.getResources().getColor(R.color.priority_2));
					break;
				case 3:
					prl.setBackgroundColor(context.getResources().getColor(R.color.priority_3));
					break;
				default:
					prl.setBackgroundColor(Color.TRANSPARENT);
			}
		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.task_list_inplan_item, null, true);
	}



}
