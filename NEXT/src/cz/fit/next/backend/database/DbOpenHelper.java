package cz.fit.next.backend.database;

import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;



public class DbOpenHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = "DbOpenHelper";

	private final Context context;



	public DbOpenHelper(Context context) {
		super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);

		this.context = context;
	}




	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d(LOG_TAG, "Creating database tables");

		// create PROJECTS table
		database.execSQL("CREATE TABLE " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_ID + " text primary key not null, " + Constants.COLUMN_TITLE
				+ " text not null, " + Constants.COLUMN_STARRED + " integer," + Constants.COLUMN_SHARED + " integer," + Constants.COLUMN_HISTORY + " text);");

		// create TASKS table
		database.execSQL("CREATE TABLE " + Constants.TABLE_TASKS + "(" + Constants.COLUMN_ID + " text primary key not null, " + Constants.COLUMN_TITLE
				+ " text not null, " + Constants.COLUMN_DESCRIPTION + " text, " + Constants.COLUMN_DATETIME + " integer, " + Constants.COLUMN_DATETIME_TYPE
				+ " text, " + Constants.COLUMN_PRIORITY + " integer, " + Constants.COLUMN_CONTEXT + " text, " + Constants.COLUMN_PROJECTS_ID + " text, "
				+ Constants.COLUMN_COMPLETED + " integer, " + "FOREIGN KEY (" + Constants.COLUMN_PROJECTS_ID + ") REFERENCES " + Constants.TABLE_PROJECTS
				+ " (" + Constants.COLUMN_ID + ")" + ");");

		// insert implicit project record
		String implUUID = UUID.randomUUID().toString();
		ContentValues implProject = new ContentValues();
		implProject.put(Constants.COLUMN_ID, implUUID);
		implProject.put(Constants.COLUMN_TITLE, Constants.IMPLICIT_PROJECT_NAME);
		database.insertOrThrow(Constants.TABLE_PROJECTS, null, implProject);


		// First install data ==========================================
		String tutorialProjectId = UUID.randomUUID().toString();
		String tutorialProjectTitle = context.getResources().getString(R.string.tutorial_project);

		database.execSQL("INSERT INTO " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_ID + ", " + Constants.COLUMN_TITLE + ", "
				+ Constants.COLUMN_STARRED + ") VALUES ('" + tutorialProjectId + "', '" + tutorialProjectTitle + "', 1)");

		DateTime today = new DateTime();
		today.setIsAllday(true);

		DateTime someday = new DateTime();
		someday.setIsSomeday(true);

		// your first task
		{
			ContentValues task = new ContentValues();
			task.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
			task.put(Constants.COLUMN_TITLE, context.getResources().getString(R.string.tutorial_first_task));
			task.put(Constants.COLUMN_PROJECTS_ID, tutorialProjectId);
			task.put(Constants.COLUMN_DESCRIPTION, context.getResources().getString(R.string.tutorial_first_description));
			task.put(Constants.COLUMN_DATETIME, today.toMiliseconds());
			task.put(Constants.COLUMN_COMPLETED, 0);
			database.insertOrThrow(Constants.TABLE_TASKS, null, task);
		}

		// tasks can be categorized
		{
			ContentValues task = new ContentValues();
			task.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
			task.put(Constants.COLUMN_TITLE, context.getResources().getString(R.string.tutorial_categorize_tasks));
			task.put(Constants.COLUMN_PROJECTS_ID, tutorialProjectId);
			task.put(Constants.COLUMN_DATETIME, today.toMiliseconds());
			task.put(Constants.COLUMN_COMPLETED, 0);
			database.insertOrThrow(Constants.TABLE_TASKS, null, task);
		}

		// share project
		{
			ContentValues task = new ContentValues();
			task.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
			task.put(Constants.COLUMN_TITLE, context.getResources().getString(R.string.tutorial_share_project));
			task.put(Constants.COLUMN_DESCRIPTION, context.getResources().getString(R.string.tutorial_share_description));
			task.put(Constants.COLUMN_PROJECTS_ID, tutorialProjectId);
			task.put(Constants.COLUMN_DATETIME, today.toMiliseconds());
			task.put(Constants.COLUMN_COMPLETED, 0);
			database.insertOrThrow(Constants.TABLE_TASKS, null, task);
		}

		// see the dark skin
		{
			ContentValues task = new ContentValues();
			task.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
			task.put(Constants.COLUMN_TITLE, context.getResources().getString(R.string.tutorial_sidebar));
			task.put(Constants.COLUMN_PROJECTS_ID, tutorialProjectId);
			task.put(Constants.COLUMN_DATETIME, today.toMiliseconds());
			task.put(Constants.COLUMN_COMPLETED, 0);
			database.insertOrThrow(Constants.TABLE_TASKS, null, task);
		}

		// rate the app on google play
		{
			ContentValues task = new ContentValues();
			task.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
			task.put(Constants.COLUMN_TITLE, context.getResources().getString(R.string.tutorial_rate_us));
			task.put(Constants.COLUMN_PROJECTS_ID, tutorialProjectId);
			task.put(Constants.COLUMN_DATETIME, someday.toMiliseconds());
			task.put(Constants.COLUMN_COMPLETED, 0);
			database.insertOrThrow(Constants.TABLE_TASKS, null, task);
		}

		// see the dark skin
		{
			ContentValues task = new ContentValues();
			task.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
			task.put(Constants.COLUMN_TITLE, context.getResources().getString(R.string.tutorial_dark_skin));
			task.put(Constants.COLUMN_PROJECTS_ID, tutorialProjectId);
			task.put(Constants.COLUMN_DATETIME, someday.toMiliseconds());
			task.put(Constants.COLUMN_COMPLETED, 0);
			database.insertOrThrow(Constants.TABLE_TASKS, null, task);
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_TASKS);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_PROJECTS);
		onCreate(db);
	}

}
