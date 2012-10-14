package cz.fit.next;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cz.fit.next.tasks.Task;

public class ContentListAdapter extends ArrayAdapter<Task> {

	private Context context;

	private List<Task> items;


	public ContentListAdapter(Context context, int textViewResourceId, List<Task> objects) {
		super(context, textViewResourceId, objects);

		this.context = context;
		this.items = objects;
	}



	private static class ViewHolder {
		public TextView item1;
		public TextView item2;
	}


	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.content_list_item, null);
			holder = new ViewHolder();
			holder.item1 = (TextView) v.findViewById(R.id.text1);
			holder.item2 = (TextView) v.findViewById(R.id.text2);
			v.setTag(holder);
		} else
			holder = (ViewHolder) v.getTag();

		final Task task = items.get(position);
		if (task != null) {
			holder.item1.setText(task.getTitle());
			holder.item2.setText(task.getDescription());
		}
		return v;
	}

}
