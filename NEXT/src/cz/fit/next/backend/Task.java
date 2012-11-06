package cz.fit.next.backend;

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



	protected Project mProject;



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



}
