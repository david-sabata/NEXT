package cz.fit.next.widget;
import  cz.fit.next.taskdetail.TaskEditFragment;

import cz.fit.next.R;
import cz.fit.next.taskdetail.TaskEditFragment;
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
		
		TaskEditFragment f = TaskEditFragment.newInstance();
	}
}