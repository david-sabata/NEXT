package cz.fit.next.backend;

import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONException;

import android.database.Cursor;

import cz.fit.next.backend.database.Constants;
import cz.fit.next.backend.sync.JavaParser;

public class Project {


	protected String mId;

	protected String mTitle;

	protected boolean mIsStarred;
	protected boolean mIsShared;

	protected ArrayList<TaskHistory> mHistory = null;
	
	
	


	/**
	 * Construct Project from FULL TASK or PROJECTS db row
	 */
	public Project(Cursor cursor) {
		JavaParser parser = new JavaParser();
		
		int projIdCol = cursor.getColumnIndex(Constants.COLUMN_PROJECTS_ID);
		int projTitleCol = cursor.getColumnIndex(Constants.COLUMN_ALIAS_PROJECTS_TITLE);
		int projHistoryCol = cursor.getColumnIndex(Constants.COLUMN_HISTORY);
		int sharedCol = cursor.getColumnIndex(Constants.COLUMN_SHARED);

		// from FULL TASK row
		if (projIdCol > -1 && projTitleCol > -1) {
			this.mId = cursor.getString(projIdCol);
			this.mTitle = cursor.getString(projTitleCol);
			try {
				this.mHistory = parser.parseHistory(cursor.getString(projHistoryCol));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// from PROJECTS row
		else {
			projIdCol = cursor.getColumnIndex(Constants.COLUMN_ID);
			projTitleCol = cursor.getColumnIndex(Constants.COLUMN_TITLE);

			if (projIdCol > -1 && projTitleCol > -1) {
				this.mId = cursor.getString(projIdCol);
				this.mTitle = cursor.getString(projTitleCol);
				try { 
					this.mHistory = parser.parseHistory(cursor.getString(projHistoryCol));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else {
				throw new RuntimeException("Instantiating Project from invalid Cursor");
			}
		}

		int starredCol = cursor.getColumnIndex(Constants.COLUMN_STARRED);
		this.mIsStarred = starredCol > -1 && cursor.getInt(starredCol) != 0;
		
		this.mIsShared = sharedCol > -1 && cursor.getInt(sharedCol) != 0;
	}

	/**
	 * Create project by title; a new ID will be generated
	 */
	public Project(String title) {
		this(UUID.randomUUID().toString(), title);
	}

	/**
	 * Create project by id and title
	 */
	public Project(String id, String title) {
		this(id, title, false, null);
	}

	
	/**
	 * Create project by id and title and starred
	 */
	public Project(String id, String title, boolean starred) {
		this.mId = id;
		this.mTitle = title;
		this.mIsStarred = starred;
	}
	
	
	
	/**
	 * Create project by id and title and starred and history
	 */
	public Project(String id, String title, boolean starred, ArrayList<TaskHistory> history) {
		this.mId = id;
		this.mTitle = title;
		this.mIsStarred = starred;
		this.mHistory = history;
	}





	public String getTitle() {
		return mTitle;
	}

	public String getId() {
		return mId;
	}

	public boolean isStarred() {
		return mIsStarred;
	}
	
	public boolean isShared() {
		return mIsShared;
	}
	
	public void setShared(boolean b) {
		mIsShared = b;
	}
	
	
	/**
	 * @return the mHistory
	 */
	public ArrayList<TaskHistory> getHistory() {
		return mHistory;
	}
	
	public String getSerializedHistory() {
		JavaParser parser = new JavaParser();
		parser.setHistory(mHistory);
		return parser.generateHistoryString();
	}

	/**
	 * @param mHistory the mHistory to set
	 */
	public void setHistory(ArrayList<TaskHistory> mHistory) {
		this.mHistory = mHistory;
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mHistory == null) ? 0 : mHistory.hashCode());
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
		result = prime * result + (mIsStarred ? 1231 : 1237);
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
		if (mHistory == null) {
			if (other.mHistory != null)
				return false;
		} else if (!mHistory.equals(other.mHistory))
			return false;
		if (mId == null) {
			if (other.mId != null)
				return false;
		} else if (!mId.equals(other.mId))
			return false;
		if (mIsStarred != other.mIsStarred)
			return false;
		if (mTitle == null) {
			if (other.mTitle != null)
				return false;
		} else if (!mTitle.equals(other.mTitle))
			return false;
		return true;
	}
	


}
