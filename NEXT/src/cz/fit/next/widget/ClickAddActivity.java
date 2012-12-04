package cz.fit.next.widget;

import cz.fit.next.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class ClickAddActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Toast.makeText(getApplicationContext(), "Add new task!", Toast.LENGTH_SHORT).show();
		super.onCreate(savedInstanceState);
		// change to our configure view
		// TODO Change to fragment
		setContentView(R.layout.task_detail_fragment_edit);
		// don't call 'this', use 'getApplicationContext()', the activity-object is
		// bigger than just the context because the activity also stores the UI elemtents
		//Toast.makeText(getApplicationContext(), "Add new task!", Toast.LENGTH_SHORT).show();
	}
}