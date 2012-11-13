package cz.fit.next.backend.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import cz.fit.next.backend.Task;

public class TasksDataSource {

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
	 * Returns query used for Tasks listing - does not contain all 
	 * data, only Task.ID, Task.TITLE, Project.TITLE 
	 */
	public Cursor getAllTasksCursor() {
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		q.setTables(Constants.TABLE_TASKS + " INNER JOIN " + Constants.TABLE_PROJECTS + " ON (" + Constants.TABLE_TASKS + "."
				+ Constants.COLUMN_PROJECTS_ID + " = " + Constants.TABLE_PROJECTS + "." + Constants.COLUMN_ID + ")");

		String[] selectColumns = new String[] {
				Constants.TABLE_TASKS + "." + Constants.COLUMN_ID,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_TITLE,
				Constants.TABLE_PROJECTS + "." + Constants.COLUMN_TITLE + " AS " + Constants.COLUMN_ALIAS_PROJECTS_TITLE
		};

		Cursor cursor = q.query(database, selectColumns, null, null, null, null, null);

		return cursor;
	}


	/**
	 * Fetches single task row with all joined tables data
	 */
	public Cursor getSingleTaskCursor(String id) {
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		q.setTables(Constants.TABLE_TASKS + " INNER JOIN " + Constants.TABLE_PROJECTS + " ON (" + Constants.TABLE_TASKS + "."
				+ Constants.COLUMN_PROJECTS_ID + " = " + Constants.TABLE_PROJECTS + "." + Constants.COLUMN_ID + ")");

		String[] selectColumns = new String[] {
				Constants.TABLE_TASKS + "." + Constants.COLUMN_ID,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_TITLE + " AS " + Constants.COLUMN_ALIAS_TASKS_TITLE,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_DESCRIPTION,
				"(strftime('%s', " + Constants.TABLE_TASKS + "." + Constants.COLUMN_DATETIME + ") * 1000) AS " + Constants.COLUMN_DATETIME,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_CONTEXT,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_PRIORITY,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_PROJECTS_ID,
				Constants.TABLE_TASKS + "." + Constants.COLUMN_COMPLETED,
				Constants.TABLE_PROJECTS + "." + Constants.COLUMN_TITLE + " AS " + Constants.COLUMN_ALIAS_PROJECTS_TITLE,
		};

		String where = Constants.TABLE_TASKS + "." + Constants.COLUMN_ID + " = ?";
		String[] args = new String[] { id };

		Cursor cursor = q.query(database, selectColumns, where, args, null, null, null, "1");
		cursor.moveToFirst();

		return cursor;
	}


	/**
	 * Returns Task object based on ID
	 */
	public Task getTaskById(String id) {
		return new Task(getSingleTaskCursor(id));
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
		vals.put(Constants.COLUMN_DATETIME, task.getDate().toString());
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

}
