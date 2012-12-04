package cz.fit.next.projectlist;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import cz.fit.next.R;

public class ProjectEditDialog extends DialogFragment {


	static ProjectEditDialog newInstance() {
		ProjectEditDialog f = new ProjectEditDialog();
		return f;
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setCancelable(true);
	}



	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater factory = LayoutInflater.from(getActivity());
		final View v = factory.inflate(R.layout.project_edit_dialog, null);

		// setup dialog
		return new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.project_title))
				.setView(v)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								Fragment parent = getTargetFragment();

								// care only if parent is project listing
								if (parent instanceof ProjectListFragment) {
									ProjectListFragment projList = (ProjectListFragment) parent;
									EditText text = (EditText) v.findViewById(R.id.text);

									if (text.getText() != null && text.getText().length() > 0) {
										projList.addProject(text.getText().toString());
									}
								}

								// hide sw keyboard
								EditText text = (EditText) v.findViewById(R.id.text);
								InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
							}
						}
				)
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// hide sw keyboard
								EditText text = (EditText) v.findViewById(R.id.text);
								InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
							}
						}
				)
				.create();
	}




}