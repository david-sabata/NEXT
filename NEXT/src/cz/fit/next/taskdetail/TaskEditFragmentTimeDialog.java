package cz.fit.next.taskdetail;

import java.util.Calendar;

import android.app.TimePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
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
		// TODO Auto-generated method stub
		Log.i("Hour: ", Integer.toString(hourOfDay));
		Log.i("Minute: ", Integer.toString(minute));
	}
	
}
