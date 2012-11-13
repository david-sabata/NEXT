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
	protected String mTimeStamp;
	protected String mAuthor;
	protected String mTaskId;
	protected ArrayList<HistoryTaskChange> changes;
	
	public TaskHistory(JSONObject jsonHistory) {
		try {
			mTimeStamp = jsonHistory.getString("timestamp");
			mAuthor = jsonHistory.getString("author");
			mTaskId = jsonHistory.getString("taskid");
			
			// Parse changes and store them
			changes = parseChanges(jsonHistory.getJSONArray("changes"));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
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
		return changes;
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
	}
}





