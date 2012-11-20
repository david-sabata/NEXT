package cz.fit.next.taskdetail;

import android.content.Context;
import android.database.Cursor;
import android.provider.SyncStateContract.Constants;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProjectsSpinnerAdapter extends CursorAdapter {

	public ProjectsSpinnerAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View textView, Context c, Cursor cursor) {
		// TODO Auto-generated method stub
		TextView v = (TextView) textView;
		v.setText(cursor.getString(cursor.getColumnIndex(cz.fit.next.backend.database.Constants.COLUMN_TITLE)));
		v.setTag(cursor.getString(cursor.getColumnIndex(cz.fit.next.backend.database.Constants.COLUMN_ID)));
	}

	@Override
	public View newView(Context c, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		TextView view = new TextView(c);		
		return view;
	}
	
	/**
	 * Find an item in Adapter by ID
	 * @param id
	 * @param cursor
	 * @return
	 */
	public int getPosition(String id, Cursor cursor) {
		for(int i = 0; i < this.getCount(); i++) {
			String itemId = ((Cursor) this.getItem(i)).getString(cursor.getColumnIndex(cz.fit.next.backend.database.Constants.COLUMN_ID));
			if(id.equals(itemId)) {
				return i;
			}
		}
		return -1;
	}
	
	
}
