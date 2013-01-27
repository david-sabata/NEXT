package cz.fit.next.projectlist;



import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import cz.fit.next.R;
import cz.fit.next.backend.sync.SyncService;


public class ShareDialog extends DialogFragment {


	private String projId;

	public void setProjId(String id) {
		projId = id;
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		LayoutInflater factory = LayoutInflater.from(getActivity());
		final View v = factory.inflate(R.layout.share, null);

		// setup dialog
		return new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.share_prompt))
				.setView(v)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								// set sharing
								//Toast.makeText(getActivity(), projId + ((TextView) v.findViewById(R.id.share_textedit)).getText(), Toast.LENGTH_SHORT).show();
								Intent in = new Intent(getActivity().getApplicationContext(), SyncService.class);
								in.putExtra("Share", 1);
								in.putExtra("ShareID", projId);
								in.putExtra("ShareGmail", ((TextView) v.findViewById(R.id.share_textedit)).getText().toString());
								getActivity().startService(in);

								// hide sw keyboard
								EditText text = (EditText) v.findViewById(R.id.share_textedit);
								InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
							}
						}
				)
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								// hide sw keyboard
								EditText text = (EditText) v.findViewById(R.id.share_textedit);
								InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
							}
						}
				)
				.create();

	}



}
