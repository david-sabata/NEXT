package cz.fit.next.projectlist;


import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import cz.fit.next.R;

public class ProjectEditDialog extends DialogFragment {

	private static final String KEY_PREFILL = "prefill";

	private String mPrefillValue = "";


	/**
	 * Create a new instance of the fragment, providing prefilled value
	 * as an argument.
	 */
	static ProjectEditDialog newInstance(String prefill) {
		ProjectEditDialog f = new ProjectEditDialog();

		Bundle args = new Bundle();
		args.putString(KEY_PREFILL, prefill);
		f.setArguments(args);

		return f;
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefillValue = getArguments().getString(KEY_PREFILL);

		setStyle(STYLE_NO_TITLE, 0);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.project_edit_dialog, container, false);

		Button cancel = (Button) v.findViewById(R.id.cmdCancel);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});

		Button ok = (Button) v.findViewById(R.id.cmdOk);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Fragment parent = getTargetFragment();

				// care only if parent is project listing
				if (parent instanceof ProjectListFragment) {
					ProjectListFragment projList = (ProjectListFragment) parent;
					View text = v.findViewById(R.id.text);

				}
			}
		});

		return v;
	}
}