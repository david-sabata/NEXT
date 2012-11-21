package cz.fit.next.taskdetail;

import cz.fit.next.R;
import android.content.Context;
import android.database.Cursor;
import android.provider.SyncStateContract.Constants;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProjectsSpinnerAdapter extends CursorAdapter {

	private LayoutInflater inflater;
	public ProjectsSpinnerAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View v, Context c, Cursor cursor) {
		// TODO Auto-generated method stub
		TextView textView = (TextView) v.findViewById(R.id.taskSpinnerText);
		textView.setText(cursor.getString(cursor.getColumnIndex(cz.fit.next.backend.database.Constants.COLUMN_TITLE)));
		textView.setTag(cursor.getString(cursor.getColumnIndex(cz.fit.next.backend.database.Constants.COLUMN_ID)));
	}

	@Override
	public View newView(Context c, Cursor arg1, ViewGroup mainView) {
		// TODO Auto-generated method stub
		View view = (View) inflater.inflate(R.layout.spinner_item, mainView);
	//	TextView view = (TextView) mainView.findViewById(R.id.taskSpinnerText);		
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
