package cz.fit.next.history;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TaskHistory.HistoryTaskChange;
import cz.fit.next.backend.TasksModelService;

/**
 * @author Tomáš
 *
 */
public class TaskHistoryTranslator {
	private Context c;
	private TaskHistory dataHistory;
	private SparseArray<Drawable> drawables;

	private String report;
	private Drawable img;

	private Boolean isCreated = false;
	private Boolean isCompleted = false;
	private Boolean isUncompleted = false;
	private Boolean isDeleted = false;

	public TaskHistoryTranslator(Context context, TaskHistory history, SparseArray<Drawable> possibleDrawables) {
		c = context;
		dataHistory = history;
		drawables = possibleDrawables;

		parseData();
	}


	/**
	 * This method parse data, that are included in TaskHistory (history) objekt to readable form
	 */
	private void parseData() {
		String sub = "";
		String subMainAction = ""; // CREATED, COMPLETED, UNCOMPLETED, DELETED
		String subOtherAction = ""; // DESCRIPTION, CONTEXT, DATE, PROJECT, PRIORITY

		for (int i = 0; i < dataHistory.getChanges().size(); i++) {
			HistoryTaskChange changeItem = dataHistory.getChanges().get(i);
			// Created
			if ((changeItem.getName().equals(TaskHistory.TITLE)) && (changeItem.getOldValue().isEmpty())) {

				subMainAction = c.getResources().getString(R.string.history_taskcreated);
				;
				isCreated = true;
				break;
			}

			if (changeItem.getName().equals(TaskHistory.TITLE) && (changeItem.getNewValue().matches(TasksModelService.deletedTitlePrefix + ".*"))) {
				isDeleted = true;
			}

			// Completed
			if ((changeItem.getName().equals(TaskHistory.COMPLETED)) && (changeItem.getNewValue().equals("true"))) {

				subMainAction = c.getResources().getString(R.string.history_taskcompleted);
				isCompleted = true;
			}

			// Uncompleted
			if (changeItem.getName().equals(TaskHistory.COMPLETED) && (changeItem.getNewValue().equals("false"))) {

				subMainAction = c.getResources().getString(R.string.history_taskuncompleted);
				isUncompleted = true;
			}

			// Date
			if (changeItem.getName().equals(TaskHistory.DATE)) {
				subOtherAction += c.getResources().getString(R.string.history_date);
			}

			// Context
			if (changeItem.getName().equals(TaskHistory.CONTEXT)) {
				subOtherAction += c.getResources().getString(R.string.history_context);
			}

			// Priority
			if (changeItem.getName().equals(TaskHistory.PRIORITY)) {
				subOtherAction += c.getResources().getString(R.string.history_priority);
			}

			// Title
			if (changeItem.getName().equals(TaskHistory.TITLE)) {
				subOtherAction += c.getResources().getString(R.string.history_title);
			}

			// Project
			if (changeItem.getName().equals(TaskHistory.PROJECT)) {
				subOtherAction += c.getResources().getString(R.string.history_project);
			}

			// Description
			if (changeItem.getName().equals(TaskHistory.DESCRIPTION)) {
				subOtherAction += c.getResources().getString(R.string.history_description);
			}

			// If no one of main actions wasnt applicated, than add "," to text
			if (!isCreated && !isCompleted && !isUncompleted && !isDeleted && i != dataHistory.getChanges().size() - 1) {
				subOtherAction += ", ";
			}
		}

		// Special texts for special events (if they are unique)
		if (subOtherAction.equals("")) {
			if (isCreated) {
				subMainAction = c.getResources().getString(R.string.history_taskcreated);
			} else if (isCompleted) {
				subMainAction = c.getResources().getString(R.string.history_taskcompleted);
			} else if (isUncompleted) {
				subMainAction = c.getResources().getString(R.string.history_taskuncompleted);
			} else if (isDeleted) {
				subMainAction = c.getResources().getString(R.string.history_taskdeleted);
			}
		} else if (!isCreated && !isCompleted && !isUncompleted && !isDeleted) {
			sub = c.getResources().getString(R.string.history_prechanged);
		} else {
			subMainAction += c.getResources().getString(R.string.history_postchanged);
		}

		sub = sub + subMainAction + subOtherAction;

		if (isCreated)
			sub = c.getResources().getString(R.string.history_taskcreated);
		if (isDeleted)
			sub = c.getResources().getString(R.string.history_taskdeleted);
		

		report = sub;

		if (isCreated)
			img = drawables.get(R.attr.actionAddIcon);
		else if (isDeleted)
			img = drawables.get(R.attr.actionDeletedIcon);
		else if (isCompleted)
			img = drawables.get(R.attr.actionAcceptIcon);
		else if (isUncompleted)
			img = drawables.get(R.attr.actionCancelIcon);
		else
			img = drawables.get(R.attr.actionEditIcon);
	}

	public String getLocaleDateTimeString() {
		return (new DateTime(Long.parseLong(dataHistory.getTimeStamp()))).toLocaleDateTimeString();
	}

	public String getAuthor() {
		return dataHistory.getAuthor();
	}

	public String getReport() {
		return report;
	}

	public Drawable getDrawable() {
		return img;
	}

	public String getTaskId() {
		return dataHistory.getTaskId();
	}
}
