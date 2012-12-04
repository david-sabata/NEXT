package cz.fit.next.taskdetail;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;



public class TaskEditFragmentTimeDialog extends DialogFragment  implements TimePickerDialog.OnTimeSetListener {

	private int minute;
	private int hour;
	
	public TaskEditFragmentTimeDialog(int minute, int hours) {
		super();
		this.minute = minute;
		this.hour = hours;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {	     
	    // Create new TimePickerDialog
	    return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		Intent i = new Intent();
		Bundle date = new Bundle();
		date.putInt("hourOfDay", hourOfDay);
		date.putInt("minute", minute);
		i.putExtras(date);
		
		// Call onActivityResult method in parent fragment
		getTargetFragment().onActivityResult(getTargetRequestCode(), 0, i);
	}
	
}
