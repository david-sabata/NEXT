package cz.fit.next.backend;

import android.database.Cursor;
import cz.fit.next.backend.database.Constants;

public class Project {


	protected long mId;

	protected String mTitle;



	/**
	 * Construct Project from FULL TASK db row, which means the row
	 * contains also all JOINed data
	 */
	public Project(Cursor cursor) {
		this.mId = cursor.getLong(cursor.getColumnIndex(Constants.COLUMN_PROJECTS_ID));
		this.mTitle = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ALIAS_PROJECTS_TITLE));
	}


	public String getTitle() {
		return mTitle;
	}

	public long getId() {
		return mId;
	}





	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (mId ^ (mId >>> 32));
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
		if (mId != other.mId)
			return false;
		if (mTitle == null) {
			if (other.mTitle != null)
				return false;
		} else if (!mTitle.equals(other.mTitle))
			return false;
		return true;
	}

}
