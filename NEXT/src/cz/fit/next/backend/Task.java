package cz.fit.next.backend;


import org.json.JSONException;
import org.json.JSONObject;
import android.database.Cursor;
import cz.fit.next.backend.database.Constants;


/**
 * @author Tomas Sychra
 * @brief Class for storing data in one task
 */
public class Task {
	/**
	 * Unique ID
	 */
	protected String mId;

	/**
	 * Task title, mandatory
	 */
	protected String mTitle;

	/**
	 * Long description, optional
	 */
	protected String mDescription;

	/**
	 * Date/time info
	 * 
	 * TODO: create specialized object
	 */
	protected String mDate;

	/**
	 * Priority, 0 = normal
	 */
	protected int mPriority = 0;

	/**
	 * Parent project reference, optional
	 */
	protected Project mProject;

	/**
	 * Project context, optional
	 */
	protected String mContext;




	/**
	 * Construct task from DB row
	 */
	public Task(Cursor cursor) {
		mId = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));
		mTitle = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ALIAS_TASKS_TITLE));
		mDescription = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DESCRIPTION));
		mDate = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DATETIME));
		mPriority = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PRIORITY));
		mContext = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_CONTEXT));

		mProject = new Project(cursor);
	}

    /**
     * Constructor to construct Task from JSONObject
     * @param taskJson
     * @throws JSONException 
     */
	public Task(JSONObject taskJson, Project project) throws JSONException {
		this.mTitle = taskJson.getString("title");
		this.mDescription = taskJson.getString("description");
		this.mPriority = taskJson.getInt("important");
		this.mProject = project;
		//this.mStatus = taskJson.getBoolean("status");
		this.mDate = taskJson.getString("date");
	}


	public String getId() {
		return mId;
	}

	public Project getProject() {
		return mProject;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getDescription() {
		return mDescription;
	}

	public String getDate() {
		return mDate;
	}

	public int getPriority() {
		return mPriority;
	}

	public String getContext() {
		return mContext;
	}





	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mContext == null) ? 0 : mContext.hashCode());
		result = prime * result + ((mDate == null) ? 0 : mDate.hashCode());
		result = prime * result + ((mDescription == null) ? 0 : mDescription.hashCode());
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
		result = prime * result + mPriority;
		result = prime * result + ((mProject == null) ? 0 : mProject.hashCode());
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
		Task other = (Task) obj;
		if (mContext == null) {
			if (other.mContext != null)
				return false;
		} else if (!mContext.equals(other.mContext))
			return false;
		if (mDate == null) {
			if (other.mDate != null)
				return false;
		} else if (!mDate.equals(other.mDate))
			return false;
		if (mDescription == null) {
			if (other.mDescription != null)
				return false;
		} else if (!mDescription.equals(other.mDescription))
			return false;
		if (mId == null) {
			if (other.mId != null)
				return false;
		} else if (!mId.equals(other.mId))
			return false;
		if (mPriority != other.mPriority)
			return false;
		if (mProject == null) {
			if (other.mProject != null)
				return false;
		} else if (!mProject.equals(other.mProject))
			return false;
		if (mTitle == null) {
			if (other.mTitle != null)
				return false;
		} else if (!mTitle.equals(other.mTitle))
			return false;
		return true;
	}
	
	

}
