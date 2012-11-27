package cz.fit.next.taskdetail;

import java.util.Calendar;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.DatePicker;

public class TaskEditFragmentDateDialog extends DialogFragment  implements DatePickerDialog.OnDateSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		 final Calendar c = Calendar.getInstance();
	        int year = c.get(Calendar.YEAR);
	        int month = c.get(Calendar.MONTH);
	        int day = c.get(Calendar.DAY_OF_MONTH);

	        // Create a new instance of DatePickerDialog and return it
	        return new DatePickerDialog(getActivity(),  this, year, month, day);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		getFragmentManager().popBackStack();
		
		// TODO Auto-generated method stub
		Log.i("Year: ", Integer.toString(year));
		Log.i("Month: ", Integer.toString(monthOfYear));
	}

}
