package cz.fit.next.history;

import cz.fit.next.R;
import cz.fit.next.backend.SettingsProvider;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.preferences.SettingsFragment;
import java.util.ArrayList;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import android.graphics.drawable.Drawable;

public class TaskHistoryAdapter extends ArrayAdapter<TaskHistory> {

	ArrayList<TaskHistory> mData;
	Context mContext;
	SparseArray<Drawable> drawables;
	public TaskHistoryAdapter(Context context, int textViewResourceId,
			ArrayList<TaskHistory> history) {
		super(context, textViewResourceId, history);
		mData = history;
		mContext = context;
		
		
		// Get image resources only one 
		int[] iconsAttrs = new int[]{R.attr.actionAcceptIcon,
				R.attr.actionCancelIcon, R.attr.actionAddIcon, 
				R.attr.actionEditIcon, R.attr.actionDeletedIcon};
		
		TypedArray iconResources = mContext.getTheme().obtainStyledAttributes(iconsAttrs);
	    drawables = new SparseArray<Drawable>();
		
	    for (int i = 0; i < iconResources.length(); i++) {
				drawables.put(iconsAttrs[i], iconResources.getDrawable(i));
		}
	    iconResources.recycle();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View vi = convertView;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			vi = inflater.inflate(R.layout.history_item, parent, false);

		// Get references to views
		TextView title = (TextView) vi.findViewById(R.id.title);
		TextView author = (TextView) vi.findViewById(R.id.author);
		TextView subtitle = (TextView) vi.findViewById(R.id.subtitle);
		
		// History translator translate data to readable form
		TaskHistoryTranslator historyTranslator = new TaskHistoryTranslator(mContext, mData.get(position), drawables);
				
		// Set timestamp as title
		title.setText(historyTranslator.getLocaleDateTimeString());

		SettingsProvider sp = new SettingsProvider(getContext().getApplicationContext());
		String aname = sp.getString(SettingsFragment.PREF_ACCOUNT_NAME, null);
		
		// Set author of task
		if (aname != null)
			author.setText(historyTranslator.getAuthor());
		else
			author.setVisibility(0);
		
		// Get report of changes in history task
		subtitle.setText(historyTranslator.getReport());
		
		// Drawable image
		ImageView img = (ImageView) vi.findViewById(R.id.history_image);
		img.setImageDrawable(historyTranslator.getDrawable());

		return vi;
	}

}
