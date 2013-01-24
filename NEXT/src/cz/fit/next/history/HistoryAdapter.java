package cz.fit.next.history;

import cz.fit.next.backend.TaskHistory;
import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HistoryAdapter extends ArrayAdapter<TaskHistory> {

	public HistoryAdapter(Context context, int textViewResourceId,
			ArrayList<TaskHistory> history) {
		super(context, textViewResourceId, history);
		
	}
	
	@Override
	public ListView getView (int position, View convertView, ViewGroup parent) {
		
		
		return new ListView(getContext());
	}

}
