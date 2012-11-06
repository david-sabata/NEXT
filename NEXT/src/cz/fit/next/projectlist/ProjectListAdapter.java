package cz.fit.next.projectlist;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import cz.fit.next.R;
import cz.fit.next.backend.database.Constants;

public class ProjectListAdapter extends SimpleCursorAdapter {


	public ProjectListAdapter(Context context, Cursor cursor) {
		super(context, R.layout.content_list_item, cursor,
				new String[] { Constants.COLUMN_TITLE, Constants.COLUMN_ID },
				new int[] { R.id.text1, R.id.text2 },
				0);
	}
}
