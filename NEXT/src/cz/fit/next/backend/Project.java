package cz.fit.next.backend;

import android.database.Cursor;
import cz.fit.next.backend.database.Constants;

public class Project {


	protected String mId;

	protected String mTitle;



	/**
	 * Construct Project from FULL TASK or PROJECTS db row
	 */
	public Project(Cursor cursor) {
		try {
			// from FULL TASK row
			this.mId = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PROJECTS_ID));
			this.mTitle = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ALIAS_PROJECTS_TITLE));
		} catch (IllegalStateException e) {
			// from PROJECTS row
			this.mId = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));
			this.mTitle = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TITLE));
		}
	}
	
	public Project(String pId, String pTitle) {
		this.mId = pId;
		this.mTitle = pTitle;
	}


	public String getTitle() {
		return mTitle;
	}

	public String getId() {
		return mId;
	}





	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
		result = prime * result + ((mTitle == null) ? 0 : mTitle.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Project other = (Project) obj;
		if (mId == null) {
			if (other.mId != null)
				return false;
		} else if (!mId.equals(other.mId))
			return false;
		if (mTitle == null) {
			if (other.mTitle != null)
				return false;
		} else if (!mTitle.equals(other.mTitle))
			return false;
		return true;
	}

}
