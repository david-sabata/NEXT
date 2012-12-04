package cz.fit.next.backend.database;

import java.util.GregorianCalendar;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cz.fit.next.backend.DateTime;



public class DbOpenHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = "DbOpenHelper";





	public DbOpenHelper(Context context) {
		super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
	}




	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d(LOG_TAG, "Creating database tables");

		// create PROJECTS table
		database.execSQL("CREATE TABLE " + Constants.TABLE_PROJECTS + "("
				+ Constants.COLUMN_ID + " text primary key not null, "
				+ Constants.COLUMN_TITLE + " text not null, "
				+ Constants.COLUMN_STARRED + " integer,"
				+ Constants.COLUMN_HISTORY + " text);");

		// create TASKS table
		database.execSQL("CREATE TABLE " + Constants.TABLE_TASKS + "("
				+ Constants.COLUMN_ID + " text primary key not null, "
				+ Constants.COLUMN_TITLE + " text not null, "
				+ Constants.COLUMN_DESCRIPTION + " text, "
				+ Constants.COLUMN_DATETIME + " integer, "
				+ Constants.COLUMN_DATETIME_TYPE + " text, "
				+ Constants.COLUMN_PRIORITY + " integer, "
				+ Constants.COLUMN_CONTEXT + " text, "
				+ Constants.COLUMN_PROJECTS_ID + " text, "
				+ Constants.COLUMN_COMPLETED + " integer, "
				+ "FOREIGN KEY (" + Constants.COLUMN_PROJECTS_ID + ") REFERENCES " + Constants.TABLE_PROJECTS + " (" + Constants.COLUMN_ID + ")"
				+ ");");

		// insert implicit project record
		String implUUID = UUID.randomUUID().toString();
		ContentValues implProject = new ContentValues();
		implProject.put(Constants.COLUMN_ID, implUUID);
		implProject.put(Constants.COLUMN_TITLE, Constants.IMPLICIT_PROJECT_NAME);
		database.insertOrThrow(Constants.TABLE_PROJECTS, null, implProject);



		// TEMP ========================================================
		String tmpUUID = UUID.randomUUID().toString();

		database.execSQL("INSERT INTO " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_ID + ", " + Constants.COLUMN_TITLE
				+ ", " + Constants.COLUMN_STARRED + ") VALUES ('" + tmpUUID + "', 'Projekt 1', 0)");
		database.execSQL("INSERT INTO " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_ID + ", " + Constants.COLUMN_TITLE
				+ ", " + Constants.COLUMN_STARRED + ") VALUES ('" + UUID.randomUUID() + "', 'Projekt 2', 1)");
		database.execSQL("INSERT INTO " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_ID + ", " + Constants.COLUMN_TITLE
				+ ", " + Constants.COLUMN_STARRED + ") VALUES ('" + UUID.randomUUID() + "', 'Projekt 3', 1)");

		ContentValues otherValues = new ContentValues();
		otherValues.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
		otherValues.put(Constants.COLUMN_TITLE, "Dummy task");
		otherValues.put(Constants.COLUMN_PROJECTS_ID, tmpUUID);
		otherValues.put(Constants.COLUMN_DESCRIPTION, "Prevelice dlouhy popis projektu. Budeme se modlit aby se nam " +
				"do vypisu vesel a nevylezl nam z okraju nebo nedelal nejake jine neplechy.");
		otherValues.put(Constants.COLUMN_CONTEXT, "Škola");
		otherValues.put(Constants.COLUMN_PRIORITY, 1);
		otherValues.put(Constants.COLUMN_DATETIME, new GregorianCalendar(2012, 12, 21).getTimeInMillis());
		otherValues.put(Constants.COLUMN_COMPLETED, 0);
		database.insertOrThrow(Constants.TABLE_TASKS, null, otherValues);

		ContentValues otherValues2 = new ContentValues();
		otherValues2.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
		otherValues2.put(Constants.COLUMN_TITLE, "Dummy task with veeery long title that will surely go out of the screen");
		otherValues2.put(Constants.COLUMN_PROJECTS_ID, tmpUUID);
		otherValues2.put(Constants.COLUMN_DESCRIPTION, "<Some description would be here>");
		otherValues2.put(Constants.COLUMN_CONTEXT, "Doma");
		otherValues2.put(Constants.COLUMN_PRIORITY, 0);
		otherValues2.put(Constants.COLUMN_DATETIME, new GregorianCalendar(2012, 10, 10).getTimeInMillis());
		otherValues2.put(Constants.COLUMN_COMPLETED, 0);
		database.insertOrThrow(Constants.TABLE_TASKS, null, otherValues2);

		ContentValues otherValues3 = new ContentValues();
		otherValues3.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
		otherValues3.put(Constants.COLUMN_TITLE, "Completed task with no description");
		otherValues3.put(Constants.COLUMN_PROJECTS_ID, tmpUUID);
		otherValues3.put(Constants.COLUMN_CONTEXT, "Doma");
		otherValues3.put(Constants.COLUMN_PRIORITY, 2);
		otherValues3.put(Constants.COLUMN_DATETIME, new GregorianCalendar(2013, 2, 3).getTimeInMillis());
		otherValues3.put(Constants.COLUMN_COMPLETED, 1);
		database.insertOrThrow(Constants.TABLE_TASKS, null, otherValues3);

		ContentValues otherValues4 = new ContentValues();
		otherValues4.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
		otherValues4.put(Constants.COLUMN_TITLE, "No description, no context, no project (implicit)");
		otherValues4.put(Constants.COLUMN_PROJECTS_ID, implUUID);
		otherValues4.put(Constants.COLUMN_PRIORITY, 0);
		otherValues4.put(Constants.COLUMN_DATETIME, new GregorianCalendar(2012, 11, 12).getTimeInMillis());
		otherValues4.put(Constants.COLUMN_COMPLETED, 0);
		database.insertOrThrow(Constants.TABLE_TASKS, null, otherValues4);

		DateTime somedayDate = new DateTime();
		somedayDate.setIsSomeday(true);
		ContentValues otherValues5 = new ContentValues();
		otherValues5.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
		otherValues5.put(Constants.COLUMN_TITLE, "Someday task");
		otherValues5.put(Constants.COLUMN_PROJECTS_ID, implUUID);
		otherValues5.put(Constants.COLUMN_PRIORITY, 0);

		otherValues5.put(Constants.COLUMN_DATETIME, somedayDate.toMiliseconds());
		otherValues5.put(Constants.COLUMN_COMPLETED, 0);
		database.insertOrThrow(Constants.TABLE_TASKS, null, otherValues5);

		ContentValues otherValues6 = new ContentValues();
		otherValues6.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
		otherValues6.put(Constants.COLUMN_TITLE, "Someday task 2");
		otherValues6.put(Constants.COLUMN_PROJECTS_ID, implUUID);
		otherValues6.put(Constants.COLUMN_PRIORITY, 0);
		otherValues6.put(Constants.COLUMN_DATETIME, somedayDate.toMiliseconds());
		otherValues6.put(Constants.COLUMN_COMPLETED, 0);
		database.insertOrThrow(Constants.TABLE_TASKS, null, otherValues6);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_TASKS);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_PROJECTS);
		onCreate(db);
	}

}
