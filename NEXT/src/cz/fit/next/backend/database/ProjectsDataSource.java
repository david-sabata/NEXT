package cz.fit.next.backend.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import cz.fit.next.backend.Project;

public class ProjectsDataSource {

	/**
	 * Database connection helper
	 */
	private DbOpenHelper dbHelper;

	/**
	 * Database connection instance
	 */
	private SQLiteDatabase database;

	/**
	 * Columns to be fetched from the table
	 */
	private String[] allColumns = { Constants.COLUMN_ID, Constants.COLUMN_TITLE, Constants.COLUMN_STARRED, Constants.COLUMN_HISTORY };


	/**
	 * 
	 */
	public ProjectsDataSource(Context context) {
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




	public Cursor getAllProjectsCursor() {
		Cursor cursor = database.query(Constants.TABLE_PROJECTS, allColumns, null, null, null, null, null);
		cursor.moveToFirst();

		return cursor;
	}


	public Cursor getStarredProjectsCursor() {
		String where = Constants.TABLE_PROJECTS + "." + Constants.COLUMN_STARRED + " != 0";

		Cursor cursor = database.query(Constants.TABLE_PROJECTS, allColumns, where, null, null, null, null);
		cursor.moveToFirst();

		return cursor;
	}



	/**
	 * Returns Project object of specified ID or null 
	 * if there is no such Project ID
	 */
	public Project getProjectById(String id) {
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		q.setTables(Constants.TABLE_PROJECTS);

		Cursor cursor = q.query(database, null, Constants.COLUMN_ID + " = ?", new String[] { id }, null, null, null);
		cursor.moveToFirst();

		if (cursor.getCount() > 0)
			return new Project(cursor);
		else
			return null;
	}



	/**
	 * Saves project to db. If the project already exists
	 * (ID test), it will be updated. Otherwise a new record
	 * will be created.
	 */
	public void saveProject(Project project) {
		ContentValues vals = new ContentValues();
		Project existing = getProjectById(project.getId());

		//		Log.i("PROJ HIST", project.getSerializedHistory());

		// update
		if (existing != null) {
			vals.put(Constants.COLUMN_STARRED, project.isStarred() ? 1 : 0);
			vals.put(Constants.COLUMN_TITLE, project.getTitle());
			vals.put(Constants.COLUMN_HISTORY, project.getSerializedHistory());
			String where = Constants.COLUMN_ID + " = ?";
			String[] args = new String[] { project.getId() };

			database.update(Constants.TABLE_PROJECTS, vals, where, args);

			return;
		}

		// add
		vals.put(Constants.COLUMN_ID, project.getId());
		vals.put(Constants.COLUMN_TITLE, project.getTitle());
		vals.put(Constants.COLUMN_STARRED, project.isStarred() ? 1 : 0);
		vals.put(Constants.COLUMN_HISTORY, project.getSerializedHistory());
		database.insert(Constants.TABLE_PROJECTS, null, vals);
	}

}
