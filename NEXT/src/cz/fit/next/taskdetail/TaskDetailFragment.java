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
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TasksModelService;

/**
 * @author Tomas Sychra
 * 
 */
public class TaskDetailFragment extends Fragment {

	private Task mItem;
	private View taskDetailView;


	public TaskDetailFragment(String id) {

		if (TasksModelService.getInstance() == null)
			throw new RuntimeException("TaskModelService.getInstance() == null");


		// load task
		mItem = TasksModelService.getInstance().getTaskById(id);
	}

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
		TextView descripton = (TextView) taskDetailView.findViewById(R.id.textDescriptionShow);
		if (descripton != null) {
			descripton.setText(mItem.getDescription());
		}
		
		// set date
		TextView date = (TextView) taskDetailView.findViewById(R.id.textDateShow);
		if (date != null) {
			date.setText(mItem.getDate());
		}
		
		// set project
		TextView project = (TextView) taskDetailView.findViewById(R.id.textProjectShow);
		if (project != null) {
			project.setText(mItem.getProject().getTitle());
		}
		
		// set context
		TextView context = (TextView) taskDetailView.findViewById(R.id.textContextShow);
		if (context != null) {
			context.setText(mItem.getContext());
		}
		
		// set priority
		//TextView priority = (TextView) taskDetailView.findViewById(R.id.textPriorityShow);
	}
}
