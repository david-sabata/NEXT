package cz.fit.next.tasklist;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;


public class TaskListAdapter extends CursorAdapter {

	public TaskListAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}



	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final MainActivity activity = (MainActivity) context;
		String id = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));

		// checkbox
		CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox);
		int isCompleted = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_COMPLETED));
		cb.setOnCheckedChangeListener(null); // prevent checking using old listener when reusing adapter
		cb.setChecked(isCompleted != 0);
		cb.setTag(id);

		// checkbox oncheck
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
				final String taskId = buttonView.getTag().toString();

				Task task = TasksModelService.getInstance().getTaskById(taskId);
				Task newTask = new Task(task.getId(), task.getTitle(), task.getDescription(), task.getDate(), task.getPriority(), task.getProject(), task
						.getContext(), isChecked);

				// save
				TasksModelService.getInstance().saveTask(newTask);

				// needed to move completed tasks to the bottom or to hide them
				TaskListFragment frag = (TaskListFragment) activity.getCurrentFragment();
				frag.reload();
			}
		});


		// task title
		TextView ttl = (TextView) view.findViewById(R.id.title);
		String title = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TITLE));
		ttl.setText(title);
		if (isCompleted == 1) {
			ttl.setPaintFlags(ttl.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			ttl.setPaintFlags(ttl.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
		}

		// date
		TextView dt = (TextView) view.findViewById(R.id.subtitle);
		long date = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_DATETIME));
		DateTime datetime = new DateTime(date);
		dt.setText(datetime.toLocaleDateTimeString());

		// priority
		LinearLayout prl = (LinearLayout) view.findViewById(R.id.TasklistItemPriority);
		if (cursor.getColumnIndex(Constants.COLUMN_PRIORITY) != -1) {
			int priority = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PRIORITY));
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

		return inflater.inflate(R.layout.task_list_item, null, true);
	}




}
