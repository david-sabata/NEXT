package cz.fit.next.projectlist;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.fit.next.R;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.TasksModelService;
import cz.fit.next.backend.database.Constants;


public class ProjectListAdapter extends CursorAdapter {

	public ProjectListAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}



	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String id = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));

		// checkbox
		CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox);
		int status = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_STARRED));
		cb.setChecked(status != 0);
		cb.setTag(id);

		// checkbox oncheck
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
				final String projectId = buttonView.getTag().toString();

				// update db on background
				new Thread(new Runnable() {
					@Override
					public void run() {
						Project project = TasksModelService.getInstance().getProjectById(projectId);
						// TODO: History change
						Project newProject = new Project(projectId, project.getTitle(), isChecked, null);

						// save
						TasksModelService.getInstance().saveProject(newProject);
					}
				}).start();
			}
		});
		
		// Share icon
		ImageView shIcon = (ImageView) view.findViewById(R.id.shareProjectIcon);
		


		// task title
		TextView ttl = (TextView) view.findViewById(R.id.title);
		String title = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TITLE));

		int sharedCol = cursor.getColumnIndex(Constants.COLUMN_SHARED);
		if (sharedCol > -1 && cursor.getInt(sharedCol) != 0) {
			shIcon.setVisibility(View.VISIBLE); 
		}
		


		if (title.equals(Constants.IMPLICIT_PROJECT_NAME)) {
			// use localized string
			ttl.setText(R.string.implicit_project);

			// set tag for the parent layout to be able create correct context menu
			view.setTag(Constants.IMPLICIT_PROJECT_NAME);
		}
		else {
			ttl.setText(title);
		}

	}



	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.project_list_item, null, true);
	}




}
