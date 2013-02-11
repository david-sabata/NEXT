package cz.fit.next.history;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TasksModelService;

public class ProjectHistoryAdapter extends ArrayAdapter<TaskHistory> {

	ArrayList<TaskHistory> mData;
	Context mContext;
	SparseArray<Drawable> drawables;


	private HashMap<String, String> titlecache;

	public ProjectHistoryAdapter(Context context, int textViewResourceId, ArrayList<TaskHistory> history) {
		super(context, textViewResourceId, history);

		mData = history;
		mContext = context;

		// Init titlecache
		titlecache = new HashMap<String, String>();

		// Get image resources only one 
		int[] iconsAttrs = new int[] { R.attr.actionAcceptIcon, R.attr.actionCancelIcon, R.attr.actionAddIcon, R.attr.actionEditIcon, R.attr.actionDeletedIcon };

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
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			vi = inflater.inflate(R.layout.history_item, parent, false);


		TaskHistoryTranslator historyTranslator = new TaskHistoryTranslator(mContext, mData.get(position), drawables);

		TextView title = (TextView) vi.findViewById(R.id.title);
		TextView author = (TextView) vi.findViewById(R.id.author);
		TextView subtitle = (TextView) vi.findViewById(R.id.subtitle);


		/**
		 * For Tomsa -> i would like to do it more effective, but i really 
		 * dont't know how. Please try it. Tomas
		 */
		// Title --> name of task
		Task t = TasksModelService.getInstance().getTaskById(mData.get(position).getTaskId());
		if (t != null) {
			title.setText(t.getTitle());
		} else {
			// deleted task - we cant find any info in DB - lets inspect history
			// for some titles
			if (titlecache.get(mData.get(position).getTaskId()) != null) {
				title.setText(titlecache.get(mData.get(position).getTaskId()) + " (" + getContext().getResources().getString(R.string.deleted) + ")");
			} else {
				// can´t find it in title cache - search in current record
				String tit = null;
				for (int i = 0; i < mData.get(position).getChanges().size(); i++) {
					if (mData.get(position).getChanges().get(i).getName().equals(TaskHistory.TITLE))
						tit = mData.get(position).getChanges().get(i).getNewValue();
				}

				if (tit != null) {
					title.setText(tit + " (" + getContext().getResources().getString(R.string.deleted) + ")");
				} else {
					// not found -> fallback
					title.setText("*DELETED TASK*");
				}

			}
		}

		// Author + timestamp
		String sub = "";

		if (historyTranslator.getAuthor() != null && !historyTranslator.getAuthor().equals(""))
			sub += historyTranslator.getAuthor() + "\n";

		sub += new DateTime(Long.parseLong(mData.get(position).getTimeStamp())).toLocaleDateTimeString();

		author.setText(sub);


		// Set report
		subtitle.setText(historyTranslator.getReport());

		// Set image
		ImageView img = (ImageView) vi.findViewById(R.id.history_image);
		img.setImageDrawable(historyTranslator.getDrawable());

		return vi;
	}
}
