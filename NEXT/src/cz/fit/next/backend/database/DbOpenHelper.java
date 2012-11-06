package cz.fit.next.backend.database;

import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



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
				+ Constants.COLUMN_TITLE + " text not null);");

		// create TASKS table
		database.execSQL("CREATE TABLE " + Constants.TABLE_TASKS + "("
				+ Constants.COLUMN_ID + " text primary key not null, "
				+ Constants.COLUMN_TITLE + " text not null, "
				+ Constants.COLUMN_DESCRIPTION + " text, "
				+ Constants.COLUMN_DATETIME + " text, "
				+ Constants.COLUMN_PRIORITY + " integer, "
				+ Constants.COLUMN_CONTEXT_ID + " text, "
				+ Constants.COLUMN_PROJECTS_ID + " text, "
				+ "FOREIGN KEY (" + Constants.COLUMN_PROJECTS_ID + ") REFERENCES " + Constants.TABLE_PROJECTS + " (" + Constants.COLUMN_ID + ")"
				+ ");");


		String tmpUUID = UUID.randomUUID().toString();

		database.execSQL("INSERT INTO " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_ID + ", " + Constants.COLUMN_TITLE
				+ ") VALUES ('" + tmpUUID + "', 'Projekt 1')");
		database.execSQL("INSERT INTO " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_ID + ", " + Constants.COLUMN_TITLE
				+ ") VALUES ('" + UUID.randomUUID() + "', 'Projekt 2')");
		database.execSQL("INSERT INTO " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_ID + ", " + Constants.COLUMN_TITLE
				+ ") VALUES ('" + UUID.randomUUID() + "', 'Projekt 3')");

		ContentValues otherValues = new ContentValues();
		otherValues.put(Constants.COLUMN_ID, UUID.randomUUID().toString());
		otherValues.put(Constants.COLUMN_TITLE, "Dummy task");
		otherValues.put(Constants.COLUMN_PROJECTS_ID, tmpUUID);
		database.insertOrThrow(Constants.TABLE_TASKS, null, otherValues);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_TASKS);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_PROJECTS);
		onCreate(db);
	}

}
