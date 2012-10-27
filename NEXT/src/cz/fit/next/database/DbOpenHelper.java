package cz.fit.next.database;

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
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);

		// FIXME: debug only - always regenerate db
		onUpgrade(db, 0, 1);
	}



	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d(LOG_TAG, "Creating database tables");

		// create PROJECTS table
		database.execSQL("CREATE TABLE " + Constants.TABLE_PROJECTS + "("
				+ Constants.COLUMN_ID + " integer primary key autoincrement, "
				+ Constants.COLUMN_TITLE + " text not null);");

		database.execSQL("INSERT INTO " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_TITLE + ") VALUES ('Projekt 1')");
		database.execSQL("INSERT INTO " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_TITLE + ") VALUES ('Projekt 2')");
		database.execSQL("INSERT INTO " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_TITLE + ") VALUES ('Projekt 3')");
	}



	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_PROJECTS);
		onCreate(db);
	}

}
