package cz.fit.next.backend.database;

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
	private String[] allColumns = { Constants.COLUMN_ID, Constants.COLUMN_TITLE };


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


	//	public Comment createComment(String comment) {
	//		ContentValues values = new ContentValues();
	//		values.put(DbOpenHelper.COLUMN_COMMENT, comment);
	//		long insertId = database.insert(DbOpenHelper.TABLE_COMMENTS, null, values);
	//		Cursor cursor = database.query(DbOpenHelper.TABLE_COMMENTS, allColumns, DbOpenHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
	//		cursor.moveToFirst();
	//		Comment newComment = cursorToComment(cursor);
	//		cursor.close();
	//		return newComment;
	//	}
	//
	//	public void deleteComment(Comment comment) {
	//		long id = comment.getId();
	//		System.out.println("Comment deleted with id: " + id);
	//		database.delete(DbOpenHelper.TABLE_COMMENTS, DbOpenHelper.COLUMN_ID + " = " + id, null);
	//	}




	public Cursor getAllProjectsCursor() {
		Cursor cursor = database.query(Constants.TABLE_PROJECTS, allColumns, null, null, null, null, null);
		cursor.moveToFirst();

		return cursor;
	}



	private Project getProjectById(String id) {
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		q.setTables(Constants.TABLE_PROJECTS);

		Cursor cursor = q.query(database, null, Constants.COLUMN_ID + " = ?", new String[] { id }, null, null, null);
		cursor.moveToFirst();

		return new Project(cursor);
	}

}
