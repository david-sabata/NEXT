package cz.fit.next.sharing;



import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import cz.fit.next.R;
import cz.fit.next.backend.sync.SyncService;
import cz.fit.next.backend.sync.SyncService.ServiceBinder;


public class ShareDialog extends DialogFragment {


	private String projId;
	
	private SyncService mSyncService;
	private boolean mSyncServiceBound;

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
								/*
								// DEPRECATED: old call method using intent
								Intent in = new Intent(getActivity().getApplicationContext(), SyncService.class);
								in.putExtra("Share", 1);
								in.putExtra("ShareID", projId);
								in.putExtra("ShareGmail", ((TextView) v.findViewById(R.id.share_textedit)).getText().toString());
								getActivity().startService(in);
								*/
								
								proceed(v);
								
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
	
	private ServiceConnection syncServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			final ServiceBinder binder = (ServiceBinder) service;

			mSyncService = binder.getService();
			mSyncServiceBound = true;
			
			
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mSyncServiceBound = false;	
			mSyncService = null;
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		
		getActivity().getApplicationContext().bindService(new Intent(this.getActivity(), SyncService.class), syncServiceConnection,
	            Context.BIND_AUTO_CREATE);

	}
	
	private void proceed(View v) {
		if (mSyncServiceBound) {
			mSyncService.share(projId, ((TextView)v.findViewById(R.id.share_textedit)).getText().toString());
		}
	}




}
