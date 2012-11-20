package cz.fit.next.backend;

import java.util.ArrayList;
import java.util.UUID;

import android.database.Cursor;
import cz.fit.next.backend.database.Constants;

public class Project {


	protected String mId;

	protected String mTitle;
	
	protected ArrayList<TaskHistory> mHistory = null;



	/**
	 * Construct Project from FULL TASK or PROJECTS db row
	 */
	public Project(Cursor cursor) {
		int projIdCol = cursor.getColumnIndex(Constants.COLUMN_PROJECTS_ID);
		int projTitleCol = cursor.getColumnIndex(Constants.COLUMN_ALIAS_PROJECTS_TITLE);

		// from FULL TASK row
		if (projIdCol > -1 && projTitleCol > -1) {
			this.mId = cursor.getString(projIdCol);
			this.mTitle = cursor.getString(projTitleCol);
		}
		// from PROJECTS row
		else {
			projIdCol = cursor.getColumnIndex(Constants.COLUMN_ID);
			projTitleCol = cursor.getColumnIndex(Constants.COLUMN_TITLE);

			if (projIdCol > -1 && projTitleCol > -1) {
				this.mId = cursor.getString(projIdCol);
				this.mTitle = cursor.getString(projTitleCol);
			}
			else {
				throw new RuntimeException("Instantiating Project from invalid Cursor");
			}
		}
	}

	/**
	 * Create project by title; a new ID will be generated
	 */
	public Project(String title) {
		mId = UUID.randomUUID().toString();
		mTitle = title;
	}

	/**
	 * Create project by id and title
	 */
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

	/**
	 * @return the mHistory
	 */
	public ArrayList<TaskHistory> getHistory() {
		return mHistory;
	}

	/**
	 * @param mHistory the mHistory to set
	 */
	public void setHistory(ArrayList<TaskHistory> mHistory) {
		this.mHistory = mHistory;
	}
}
