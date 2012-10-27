package cz.fit.next.backend.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
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


	public List<Project> getAllProjects() {
		List<Project> projects = new ArrayList<Project>();

		Cursor cursor = getAllProjectsCursor();

		while (!cursor.isAfterLast()) {
			Project comment = cursorToProject(cursor);
			projects.add(comment);
			cursor.moveToNext();
		}

		cursor.close();
		return projects;
	}


	private Project cursorToProject(Cursor cursor) {
		Project project = new Project();
		project.setId(cursor.getLong(0));
		project.setTitle(cursor.getString(1));
		return project;
	}





}
