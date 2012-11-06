package cz.fit.next.backend;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Tomas Sychra
 * @brief Class for storing data in one task
 */
public class Task {
	protected String title; // title of the task
	protected String description; // it could be long description of task
	protected String date; // contain time too -> TODO create class for object
							// Date
	protected String partProject; // parent project of the task
	protected String partContexts[] = {}; // one task can be part of many
											// contexts
	protected Integer important; // important of the task (1,2,3)
    protected Boolean status; // status of task (Done or not)

    public Task() {
    	// default constructor
    }
    
    /**
     * Constructor to construct Task from JSONObject
     * @param taskJson
     * @throws JSONException 
     */
	public Task(JSONObject taskJson) throws JSONException {
		this.title = taskJson.getString("title");
		this.description = taskJson.getString("description");
		this.important = taskJson.getInt("important");
		this.partProject = taskJson.getString("partProject");
		this.status = taskJson.getBoolean("status");
		this.date = taskJson.getString("date");
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getDate() {
		return date;
	}


	public void setDate(String date) {
		this.date = date;
	}


	public String getPartProject() {
		return partProject;
	}


	public void setPartProject(String partProject) {
		this.partProject = partProject;
	}


	public String[] getPartContexts() {
		return partContexts;
	}


	public void setPartContexts(String[] partContexts) {
		this.partContexts = partContexts;
	}


	public Integer getImportant() {
		return important;
	}


	public void setImportant(Integer important) {
		this.important = important;
	}
	
	public Boolean getStatus() {
		return status;
	}


	public void setStatus(Boolean important) {
		this.status = important;
	}



}
