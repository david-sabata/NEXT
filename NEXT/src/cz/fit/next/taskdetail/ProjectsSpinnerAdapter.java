package cz.fit.next.taskdetail;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cz.fit.next.R;
import cz.fit.next.backend.database.Constants;

public class ProjectsSpinnerAdapter extends CursorAdapter {

	private LayoutInflater inflater;
	private String noProjectConstant;
	
	public ProjectsSpinnerAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		noProjectConstant = context.getResources().getString(R.string.implicit_project);
	}

	@Override
	public void bindView(View v, Context c, Cursor cursor) {
		// Text setting
		TextView textView = (TextView) v.findViewById(R.id.taskSpinnerText);
		String projectText = cursor.getString(cursor.getColumnIndex(cz.fit.next.backend.database.Constants.COLUMN_TITLE));		
		if(projectText.equals(Constants.IMPLICIT_PROJECT_NAME)) { 
			projectText = noProjectConstant;
		}
		
		textView.setText(projectText);
		textView.setTag(cursor.getString(cursor.getColumnIndex(cz.fit.next.backend.database.Constants.COLUMN_ID)));
	}

	@Override
	public View newView(Context c, Cursor arg1, ViewGroup mainView) {
		RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.spinner_item, mainView, false);
		return view;
	}

	/**
	 * Find an item in Adapter by ID
	 * @param id
	 * @param cursor
	 * @return
	 */
	public int getPosition(String id, Cursor cursor) {
		for (int i = 0; i < this.getCount(); i++) {
			String itemId = ((Cursor) this.getItem(i)).getString(cursor.getColumnIndex(cz.fit.next.backend.database.Constants.COLUMN_ID));
			if (id.equals(itemId)) {
				return i;
			}
		}
		return -1;
	}


}
