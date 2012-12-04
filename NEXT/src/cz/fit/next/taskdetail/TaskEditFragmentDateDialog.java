package cz.fit.next.taskdetail;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class TaskEditFragmentDateDialog extends DialogFragment  implements DatePickerDialog.OnDateSetListener {

	private int year;
	private int month;
	private int day;
	
	
	public TaskEditFragmentDateDialog(int year, int month, int day) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {	     
	     // Create a new instance of DatePickerDialog and return it
	     return new DatePickerDialog(getActivity(),  this, year, month, day);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		
		Intent i = new Intent();
		Bundle date = new Bundle();
		date.putInt("year", year);
		date.putInt("monthOfYear", monthOfYear);
		date.putInt("dayOfMonth", dayOfMonth);
		i.putExtras(date);
		
		// Call onActivityResult method in parent fragment
		getTargetFragment().onActivityResult(getTargetRequestCode(), 0, i);
	}
	
}

