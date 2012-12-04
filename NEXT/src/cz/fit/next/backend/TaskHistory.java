package cz.fit.next.backend;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


/**
 * @author xsych_000
 *
 */
public class TaskHistory {    
	
	// String definitions for History object
	public final static String TITLE = "next_hist_title";
	public final static String DESCRIPTION = "next_hist_description";
	public final static String DATE = "next_hist_date";
	public final static String PRIORITY = "next_hist_priority";
	public final static String PROJECT = "next_hist_project";
	public final static String CONTEXT = "next_hist_context";
	public final static String COMPLETED = "next_hist_completed";
	
	
	protected String mTimeStamp;
	protected String mAuthor;
	protected String mTaskId;
	protected ArrayList<HistoryTaskChange> mChanges;
	
	public TaskHistory(JSONObject jsonHistory) {
		try {
			mTimeStamp = jsonHistory.getString("timestamp");
			mAuthor = jsonHistory.getString("author");
			mTaskId = jsonHistory.getString("taskid");
			
			// Parse changes and store them
			mChanges = parseChanges(jsonHistory.getJSONArray("changes"));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public TaskHistory() {
		mTimeStamp = "";
		mAuthor = "";
		mTaskId = "";
		mChanges = new ArrayList<HistoryTaskChange>();
	}

	private ArrayList<HistoryTaskChange> parseChanges(JSONArray jsonArrayChanges) throws JSONException {
		ArrayList<HistoryTaskChange> changes = new ArrayList<HistoryTaskChange>();
		
		for(int i = 0; i < jsonArrayChanges.length(); i++) {
			JSONObject jsonOneChange = jsonArrayChanges.getJSONObject(i);
			HistoryTaskChange oneChange = new HistoryTaskChange(jsonOneChange);
			changes.add(oneChange);
		}		
		return changes;
	}
	
	
	/**
	 * Getter and setters
	 * @return
	 */
	public String getTimeStamp() {
		return mTimeStamp;
	}

	public String getAuthor() {
		return mAuthor;
	}

	public String getTaskId() {
		return mTaskId;
	}

	public ArrayList<HistoryTaskChange> getChanges() {
		return mChanges;
	}
	
	
	
	/**
	 * @param mTimeStamp the mTimeStamp to set
	 */
	public void setTimeStamp(String mTimeStamp) {
		this.mTimeStamp = mTimeStamp;
	}

	/**
	 * @param mAuthor the mAuthor to set
	 */
	public void setAuthor(String mAuthor) {
		this.mAuthor = mAuthor;
	}

	/**
	 * @param mTaskId the mTaskId to set
	 */
	public void setTaskId(String mTaskId) {
		this.mTaskId = mTaskId;
	}

	/**
	 * @param mChanges the mChanges to set
	 */
	public void setChanges(ArrayList<HistoryTaskChange> mChanges) {
		this.mChanges = mChanges;
	}

	public ArrayList<HistoryTaskChange> addChange(String name, String oldvalue, String newvalue) {
		HistoryTaskChange hist = new HistoryTaskChange();
		hist.setName(name);
		hist.setNewValue(newvalue);
		hist.setOldValue(oldvalue);
		
		mChanges.add(hist);
		return mChanges;
	}
	
	public boolean headerequals(TaskHistory second) {
		boolean res = true;
		if (!mTimeStamp.equals(second.getTimeStamp())) res = false;
		if (!mAuthor.equals(second.getAuthor())) res = false;
		if (!mTaskId.equals(second.getTaskId())) res = false;
		
		return res;
	}
	

	/**
	 * Private class for storage "change" information
	 */
	public class HistoryTaskChange {
		private String mName;
		private String mOldValue;
		private String mNewValue;
		
		private HistoryTaskChange(JSONObject change) throws JSONException {
			mName = change.getString("name") ;
			mOldValue = change.getString("oldvalue");
			mNewValue = change.getString("newvalue");
		}

		private HistoryTaskChange() {
			mName = "";
			mOldValue = "";
			mNewValue = "";
		}
		
		/**
		 * @return the mName
		 */
		public String getName() {
			return mName;
		}

		/**
		 * @return the mOldValue
		 */
		public String getOldValue() {
			return mOldValue;
		}

		/**
		 * @return the mNewValue
		 */
		public String getNewValue() {
			return mNewValue;
		}

		public void setName(String mName) {
			this.mName = mName;
		}

		public void setOldValue(String mOldValue) {
			this.mOldValue = mOldValue;
		}

		public void setNewValue(String mNewValue) {
			this.mNewValue = mNewValue;
		}
		
		
		
	}
}





