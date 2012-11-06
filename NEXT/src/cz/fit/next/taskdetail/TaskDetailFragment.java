/**
 * 
 */
package cz.fit.next.taskdetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.fit.next.R;
import cz.fit.next.R.id;
import cz.fit.next.R.layout;
import cz.fit.next.backend.Task;

/**
 * @author Tomas Sychra
 * 
 */
public class TaskDetailFragment extends Fragment {

	private Task mItem;
	private View taskDetailView;


	public TaskDetailFragment(Task pItem) {
		mItem = pItem;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);

		taskDetailView = inflater.inflate(R.layout.task_detail_fragment_show, container, false);

		// set new item info to our task
		setTaskSetting();

		// return complete fragment
		return taskDetailView;
	}


	private void setTaskSetting() {
		// set Title
		TextView title = (TextView) taskDetailView.findViewById(R.id.titleTask);
		if (title != null) {
			title.setText(mItem.getTitle());
		}

		// TODO implements others like title
		// set description

		// set date

		// set project

		// set context

		// set priority

	}
}
