package cz.fit.next.taskdetail;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;



public class TaskEditFragmentTimeDialog extends DialogFragment  implements TimePickerDialog.OnTimeSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Get actual time 
		final Calendar calendar = Calendar.getInstance();
	    int hour = calendar.get(Calendar.HOUR_OF_DAY);
	    int minute = calendar.get(Calendar.MINUTE);
	     
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
