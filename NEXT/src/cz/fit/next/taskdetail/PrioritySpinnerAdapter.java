package cz.fit.next.taskdetail;

import cz.fit.next.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PrioritySpinnerAdapter extends ArrayAdapter<String> {

	private LayoutInflater inflater;
	private String[] priorityTexts;
	
	public PrioritySpinnerAdapter(Context context, int textViewResourceId, String[] datas) {
		super(context, textViewResourceId, datas);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		priorityTexts = datas;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	private RelativeLayout getCustomView(int position, View convertView, ViewGroup parent) {
		RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.spinner_item, parent, false);
		
		TextView textView = (TextView) view.findViewById(R.id.taskSpinnerText);
		textView.setText(priorityTexts[position]);
		
		return view;
	}
	
}
