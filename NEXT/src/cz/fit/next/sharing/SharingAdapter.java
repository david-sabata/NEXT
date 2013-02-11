package cz.fit.next.sharing;

import java.util.ArrayList;

import cz.fit.next.R;
import cz.fit.next.backend.sync.drivers.GDrive;
import cz.fit.next.backend.sync.drivers.GDrive.UserPerm;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SharingAdapter extends ArrayAdapter<UserPerm> {
	
	private ArrayList<UserPerm> mData;
	private Context mContext;
	
	public SharingAdapter(Context context, int textViewResourceId,
			ArrayList<UserPerm> input) {
		
		super(context, textViewResourceId, input);
		mData = input;
		mContext = context;
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			vi = inflater.inflate(R.layout.sharing_item, parent, false);

		TextView login = (TextView) vi.findViewById(R.id.login);
		TextView permissions = (TextView) vi.findViewById(R.id.permissions);

		login.setText(mData.get(position).username);
		
		ImageView img = (ImageView) vi.findViewById(R.id.status_image);
		
		if (mData.get(position).mode == GDrive.OWNER) {
			permissions.setText("OWNER");
			img.setImageResource(R.drawable.action_edit_light);
		}
		else if (mData.get(position).mode == GDrive.WRITE) {
			permissions.setText("WRITE");
			img.setImageResource(R.drawable.action_edit_light);
		}
		else if (mData.get(position).mode == GDrive.READ) {
			permissions.setText("READ");
			img.setImageResource(R.drawable.action_cancel_light);
		}
		else permissions.setText("UNKNOWN");
		
		return vi;
	}
	
	
}
