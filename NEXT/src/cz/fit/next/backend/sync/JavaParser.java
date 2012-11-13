/**
 * 
 */
package cz.fit.next.backend.sync;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.util.Log;

import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TaskHistory;

/**
 * @author xsych_000
 *
 */
public class JavaParser {
	private String mFilePath = null;
	private String inputDataString = "<html><head><title>Test</title><!-- Here we will store our data for tasks --><script id='data' type='x-next/x-json'>{ id: '123456', filename: 'editable.html', projectname: 'Reader for NEXT', data: [{id :'23', title: 'Basic structure', description: 'Create basic layout', date: '1.11.2012', partProject : 'Reader for NEXT', partContexts : ['School','Android'],important: '3',status: true},{ id: '45', title: 'Plug-in modules', description: 'Create generator for tasks layout',date: '1.11.2012',partProject : 'Reader for NEXT',partContexts : ['School','Javascript'],important: '3', status: false}]}</script></head><body></body></html>";
	
	// It is imporatnt to tell, which input will be used (Temporary String coded above or FileName )
	private Boolean  READ_FROM_STRING = true;
	private InputStream input;
	
	// Regex pattern to mine JSON data from HTML page
	private final String scriptPattern = "(?i)(.*?)(<script id='data' type='x-next/x-json'>)(.+?)(</script>)(.*?)";
	
	// Alld information about project
	private JSONObject projectData;
	
	// Reader 
	private BufferedReader reader;
	


	/**
	 * This method open file input stream and attach reader to stream
	 * @param filePath
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unused")
	private void openDataFile () throws Throwable {
		// Open the file 
		try {
			input = new FileInputStream(mFilePath);	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(input == null) {
			throw new Throwable("Cant open file");
		}
	}	

	private BufferedReader attachReader() {
		// Attach Reader and set it to UTF-8 encoding
		BufferedReader reader;
		if(READ_FROM_STRING) {
			reader = new BufferedReader(new StringReader(inputDataString));
		} else {
			reader = new BufferedReader(
									new InputStreamReader(
											input, Charset.forName("UTF-8")));
		}
		return reader;
	}

	
	/**
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private void parseJsonData(BufferedReader reader) throws IOException, JSONException {
		// Load data to String
		int c;
		String outString = new String();
		
		while((c = reader.read()) != -1) {
			char character = (char) c;
			outString += character;
		}
		
		// Parse data to JSON
		String jsonString = outString.replaceAll(scriptPattern, "$3");
		projectData = new JSONObject(jsonString);
	}
	
	/**
	 * This method set source file to Parser
	 * @param pFilePath
	 */
	public void setFile(String pFilePath) {
		mFilePath = pFilePath;
		try {	
			// If we are opening file
			if(!READ_FROM_STRING)  {
				openDataFile();
				reader = attachReader();
			} 
			// for testing (without file)
			else {
				reader = attachReader();
			}
			
			// parse JSON text to JSONObject
			parseJsonData(reader);
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Return project, that is parent of all task in HTML file
	 * @return Project, or null if Project cant be created
	 */
	public Project getProject() {
		Project project = null;
		try {
			// parse FileName and ProjectName from JSONObject
			String projectId = projectData.getString("id");
			String projectName = projectData.getString("projectname");
			
			Log.i("Project ID: ", projectId);
			Log.i("Project Title: ", projectName);
			
			project = new Project(projectId, projectName);
			
			return project;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return project;
	}
	
	/**
	 * Generate Task for every JSON Object task and add Task to taskArrayList
	 * @param jsonArrayTask
	 * @return 
	 * @throws JSONException 
	 */
	private ArrayList<Task> createTaskList(JSONArray jsonArrayTasks, Project project) throws JSONException {	
		ArrayList<Task> tasksList = new ArrayList<Task>();
		for (int i = 0; i < jsonArrayTasks.length(); i++) {
			JSONObject taskJson = jsonArrayTasks.getJSONObject(i);
			Task newTask = new Task(taskJson, project);
			
			// Debug Task
			Log.i("Task id:", newTask.getId());
			Log.i("Task title:", newTask.getTitle());
			
			tasksList.add(newTask);
		}	
		return tasksList;
	}
	
	/**
	 * Generate array of tasks
	 * @param project
	 * @return Array of Task or null if error occured
	 */
	public ArrayList<Task> getTasks(Project project) {
		JSONArray jsonArrayTasks = null;
		ArrayList<Task> tasksList = null;
		// Add tasks to list from HTML file
		try {
			jsonArrayTasks = projectData.getJSONArray("data");
			tasksList = createTaskList(jsonArrayTasks, project);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tasksList;
	}
	
	
	/**
	 * This method create a History of tasks in project
	 * @param jsonArrayHistory
	 * @param project
	 * @return
	 */
	private ArrayList<TaskHistory> createHistoryList(JSONArray jsonArrayHistory,
			Project project) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public ArrayList<TaskHistory> getHistory(Project project) {
		JSONArray jsonArrayHistory = null;
		ArrayList<TaskHistory> historyList = null;
		
		try {
			jsonArrayHistory = projectData.getJSONArray("history");
			historyList = createHistoryList(jsonArrayHistory, project);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return historyList;
	
	}


	
}


