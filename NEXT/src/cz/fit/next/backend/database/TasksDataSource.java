package cz.fit.next.backend.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.widget.FilterQueryProvider;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.Task;
import cz.fit.next.tasklist.Filter;

public class TasksDataSource {

	private static final String LOG_TAG = "TasksDataSource";


	/**
	 * Database connection helper
	 */
	private DbOpenHelper dbHelper;

	/**
	 * Database connection instance
	 */
	private SQLiteDatabase database;


	/**
	 * 
	 */
	public TasksDataSource(Context context) {
		dbHelper = new DbOpenHelper(context);
	}



	/**
	 * Create new (writable) database connection
	 * @throws SQLException
	 */
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	/**
	 * Close current database connection
	 */
	public void close() {
		dbHelper.close();
	}


	/**
	 * Erases all database data calling its onUpgrade method
	 */
	public void wipeDatabaseData() {
		dbHelper.onUpgrade(database, 0, 1);
	}





	/**
	 * Returns query used for Tasks listing
	 */
	public Cursor getAllTasksCursor() {
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		q.setTables(Constants.TABLE_TASKS + " INNER JOIN " + Constants.TABLE_PROJECTS + " ON (" + Constants.TABLE_TASKS + "." + Constants.COLUMN_PROJECTS_ID
				+ " = " + Constants.TABLE_PROJECTS + "." + Constants.COLUMN_ID + ")");

		String[] selectColumns = new String[] { Constants.TABLE_TASKS + "." + Constants.COLUMN_ID, Constants.TABLE_TASKS + "." + Constants.COLUMN_TITLE,
				Constants.TABLE_PROJECTS + "." + Constants.COLUMN_TITLE + " AS " + Constants.COLUMN_ALIAS_PROJECTS_TITLE,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_COMPLETED, Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME_TYPE, Constants.TABLE_TASKS + "." + Constants.COLUMN_PRIORITY,
				Constants.TABLE_PROJECTS + "." + Constants.COLUMN_HISTORY, };

		// crazy ordering - modulo 1000 to get miliseconds and then modulo SOMEDAY_MSECS to set
		// 1 for someday and 0 for allday or exact time
		String order = "(" + Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " % 1000) % " + DateTime.SOMEDAY_MILISECONDS + " ASC,"
				+ Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " ASC";

		Cursor cursor = q.query(database, selectColumns, null, null, null, null, order);

		return cursor;
	}



	public Cursor getTasksInplan() {
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		q.setTables(Constants.TABLE_TASKS + " INNER JOIN " + Constants.TABLE_PROJECTS + " ON (" + Constants.TABLE_TASKS + "." + Constants.COLUMN_PROJECTS_ID
				+ " = " + Constants.TABLE_PROJECTS + "." + Constants.COLUMN_ID + ")");

		String[] selectColumns = new String[] { Constants.TABLE_TASKS + "." + Constants.COLUMN_ID, Constants.TABLE_TASKS + "." + Constants.COLUMN_TITLE,
				Constants.TABLE_PROJECTS + "." + Constants.COLUMN_TITLE + " AS " + Constants.COLUMN_ALIAS_PROJECTS_TITLE,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_COMPLETED, Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME_TYPE, Constants.TABLE_TASKS + "." + Constants.COLUMN_PRIORITY,
				Constants.TABLE_PROJECTS + "." + Constants.COLUMN_HISTORY, };

		String where = Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " % 1000 != " + DateTime.SOMEDAY_MILISECONDS;

		// order by exact time first, allday at the end of the day; someday events are ruled out by WHERE
		String order = Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " % 1000 ASC," + Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME
				+ " ASC";

		Cursor cursor = q.query(database, selectColumns, where, null, null, null, order);

		return cursor;
	}



	/**
	 * Returns complete tasks from one project
	 */
	public Cursor getProjectTasksCursor(String projectId) {
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		q.setTables(Constants.TABLE_TASKS + " INNER JOIN " + Constants.TABLE_PROJECTS + " ON (" + Constants.TABLE_TASKS + "." + Constants.COLUMN_PROJECTS_ID
				+ " = " + Constants.TABLE_PROJECTS + "." + Constants.COLUMN_ID + ")");

		String[] selectColumns = new String[] { Constants.TABLE_TASKS + "." + Constants.COLUMN_ID,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_TITLE + " AS " + Constants.COLUMN_ALIAS_TASKS_TITLE,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_DESCRIPTION, Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME_TYPE, Constants.TABLE_TASKS + "." + Constants.COLUMN_CONTEXT,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_PRIORITY, Constants.TABLE_TASKS + "." + Constants.COLUMN_PROJECTS_ID,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_COMPLETED,
				Constants.TABLE_PROJECTS + "." + Constants.COLUMN_TITLE + " AS " + Constants.COLUMN_ALIAS_PROJECTS_TITLE,
				Constants.TABLE_PROJECTS + "." + Constants.COLUMN_HISTORY,

		};

		String where = Constants.TABLE_TASKS + "." + Constants.COLUMN_PROJECTS_ID + " = '" + projectId + "'";

		// crazy ordering - modulo 1000 to get miliseconds and then modulo SOMEDAY_MSECS to set
		// 1 for someday and 0 for allday or exact time
		String order = "(" + Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " % 1000) % " + DateTime.SOMEDAY_MILISECONDS + " ASC,"
				+ Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " ASC";

		Cursor cursor = q.query(database, selectColumns, where, null, null, null, order);
		return cursor;
	}


	/**
	 * Fetches single task row with all joined tables data
	 */
	public Cursor getSingleTaskCursor(String id) {
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		q.setTables(Constants.TABLE_TASKS + " INNER JOIN " + Constants.TABLE_PROJECTS + " ON (" + Constants.TABLE_TASKS + "." + Constants.COLUMN_PROJECTS_ID
				+ " = " + Constants.TABLE_PROJECTS + "." + Constants.COLUMN_ID + ")");

		String[] selectColumns = new String[] { Constants.TABLE_TASKS + "." + Constants.COLUMN_ID,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_TITLE + " AS " + Constants.COLUMN_ALIAS_TASKS_TITLE,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_DESCRIPTION, Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME_TYPE, Constants.TABLE_TASKS + "." + Constants.COLUMN_CONTEXT,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_PRIORITY, Constants.TABLE_TASKS + "." + Constants.COLUMN_PROJECTS_ID,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_COMPLETED,
				Constants.TABLE_PROJECTS + "." + Constants.COLUMN_TITLE + " AS " + Constants.COLUMN_ALIAS_PROJECTS_TITLE,
				Constants.TABLE_PROJECTS + "." + Constants.COLUMN_HISTORY, };

		String where = Constants.TABLE_TASKS + "." + Constants.COLUMN_ID + " = ?";
		String[] args = new String[] { id };

		Cursor cursor = q.query(database, selectColumns, where, args, null, null, null, "1");
		cursor.moveToFirst();

		if (cursor.getCount() == 0)
			return null;
		return cursor;
	}


	/**
	 * Returns Task object based on ID 
	 * or NULL if the ID was not found
	 */
	public Task getTaskById(String id) {
		Cursor cursor = getSingleTaskCursor(id);
		if ((cursor != null) && (cursor.getCount() > 0))
			return new Task(cursor);
		else
			return null;

	}



	/**
	 * Saves task to db. If the task already exists
	 * (ID test), it will be updated. Otherwise a new record
	 * will be created.
	 * 
	 * Be sure to check if Project associated to the Task already 
	 * exists, or this method will fail.
	 */
	public void saveTask(Task task) {
		ContentValues vals = new ContentValues();
		vals.put(Constants.COLUMN_TITLE, task.getTitle());
		vals.put(Constants.COLUMN_DESCRIPTION, task.getDescription());
		vals.put(Constants.COLUMN_CONTEXT, task.getContext());
		vals.put(Constants.COLUMN_PROJECTS_ID, task.getProject().getId());
		vals.put(Constants.COLUMN_DATETIME, task.getDate().toMiliseconds());
		vals.put(Constants.COLUMN_PRIORITY, task.getPriority());
		vals.put(Constants.COLUMN_COMPLETED, task.isCompleted() ? 1 : 0);

		Task existing = getTaskById(task.getId());

		// update
		if (existing != null) {
			String where = Constants.COLUMN_ID + " = ?";
			String[] args = new String[] { task.getId() };

			database.update(Constants.TABLE_TASKS, vals, where, args);
			return;
		}

		// add
		vals.put(Constants.COLUMN_ID, task.getId());
		database.insert(Constants.TABLE_TASKS, null, vals);
	}

	/**
	 * Delete task identified by ID.
	 * 
	 * @param taskId
	 */
	public void deleteTask(String taskId) {
		String args[] = { taskId };

		// delete tasks
		database.delete(Constants.TABLE_TASKS, Constants.COLUMN_ID + " = ?", args);
	}




	public FilterQueryProvider getFilterQueryProvider() {

		return new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence constraint) {
				Filter filter = null;

				if (constraint != null)
					filter = Filter.fromString(constraint.toString());

				SQLiteQueryBuilder q = new SQLiteQueryBuilder();
				q.setTables(Constants.TABLE_TASKS + " INNER JOIN " + Constants.TABLE_PROJECTS + " ON (" + Constants.TABLE_TASKS + "."
						+ Constants.COLUMN_PROJECTS_ID + " = " + Constants.TABLE_PROJECTS + "." + Constants.COLUMN_ID + ")");

				String[] selectColumns = new String[] { Constants.TABLE_TASKS + "." + Constants.COLUMN_ID,
						Constants.TABLE_TASKS + "." + Constants.COLUMN_TITLE,
						Constants.TABLE_PROJECTS + "." + Constants.COLUMN_TITLE + " AS " + Constants.COLUMN_ALIAS_PROJECTS_TITLE,
						Constants.TABLE_TASKS + "." + Constants.COLUMN_COMPLETED, Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME,
						Constants.TABLE_TASKS + "." + Constants.COLUMN_PRIORITY };

				String where = "";

				// crazy ordering - modulo 1000 to get miliseconds and then divide by SOMEDAY_MSECS to set
				// 1 for someday and 0 for allday or exact time
				String msecs = "(" + Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " % 1000)";
				String order = msecs + " / " + DateTime.SOMEDAY_MILISECONDS + " ASC," + Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " ASC";

				// datetime				
				if (filter != null && filter.getDateFrom() != null && filter.getDateUntil() != null) {

					// someday should be consistently set in both dateFrom and dateUntil
					if (filter.getDateFrom().isSomeday() || filter.getDateUntil().isSomeday()) {
						where += Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " % 1000 = " + DateTime.SOMEDAY_MILISECONDS;
					} else {
						where += Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " >= " + filter.getDateFrom().toMiliseconds();
						where += " AND ";
						where += Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + " < " + filter.getDateUntil().toMiliseconds();
					}
				}

				// project
				if (filter != null && filter.getProjectId() != null) {
					if (where.length() > 0)
						where += " AND ";

					where += Constants.TABLE_TASKS + "." + Constants.COLUMN_PROJECTS_ID + " = '" + filter.getProjectId() + "'";
				}

				// context
				if (filter != null && filter.getContext() != null) {
					if (where.length() > 0)
						where += " AND ";

					where += Constants.TABLE_TASKS + "." + Constants.COLUMN_CONTEXT + " = '" + filter.getContext() + "'";
				}

				Cursor cursor = q.query(database, selectColumns, where, null, null, null, order);
				Log.i(LOG_TAG, "after filter: " + cursor.getCount() + " items");
				return cursor;
			}
		};

	}

	/**
	 * getContexts
	 * @return
	 */
	public Cursor getContexts() {
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		q.setTables(Constants.TABLE_TASKS);
		q.setDistinct(true);
		String[] selectColumns = new String[] { Constants.TABLE_TASKS + "." + Constants.COLUMN_CONTEXT };

		Cursor cursor = q.query(database, selectColumns, null, null, null, null, null, null);
		cursor.moveToFirst();

		if (cursor.getCount() == 0)
			return null;
		return cursor;
	}


}
