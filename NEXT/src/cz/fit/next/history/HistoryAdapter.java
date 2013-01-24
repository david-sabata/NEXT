package cz.fit.next.history;

import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.TaskHistory;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryAdapter extends ArrayAdapter<TaskHistory> {

	ArrayList<TaskHistory> mData;
	Context mContext;
	
	public HistoryAdapter(Context context, int textViewResourceId,
			ArrayList<TaskHistory> history) {
		super(context, textViewResourceId, history);
		
		mData = history;
		mContext = context;
	}
	
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		
		View vi = convertView;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
        	vi = inflater.inflate(R.layout.task_list_item, parent, false);
        TextView title = (TextView) vi.findViewById(R.id.title);
        TextView subtitle = (TextView) vi.findViewById(R.id.subtitle);
        title.setText(mData.get(position).getAuthor());
        
        String sub = "";
        
        // add timestamp
        sub = sub + "@ " + new DateTime(Long.parseLong(mData.get(position).getTimeStamp())).toLocaleDateTimeString() + "\n";
        
        for (int i = 0; i < mData.get(position).getChanges().size(); i++) {
        	sub = sub + mData.get(position).getChanges().get(i).getName() + ":" +
        			mData.get(position).getChanges().get(i).getOldValue() + " -> " +
        			mData.get(position).getChanges().get(i).getNewValue() + "\n";
        }
        
        subtitle.setText(sub);
        return vi;
	}
	

}
