/**
 * 
 */
package cz.fit.next.backend.sync;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.TaskHistory;
import cz.fit.next.backend.TaskHistory.HistoryTaskChange;

/**
 * @author xsych_000
 *
 */
public class JavaParser {
	private String mFilePath = null;
	private String inputDataString = "<html><head><title>Test</title><!-- Here we will store our data for tasks --><script id='data' type='x-next/x-json'>{ id: '123456', filename: 'editable.html', projectname: 'Reader for NEXT', data: [{id :'23', title: 'Basic structure', description: 'Create basic layout', date: '1.11.2012', partProject : 'Reader for NEXT', partContexts : ['School','Android'],important: '3',status: true},{ id: '45', title: 'Plug-in modules', description: 'Create generator for tasks layout',date: '1.11.2012',partProject : 'Reader for NEXT',partContexts : ['School','Javascript'],important: '3', status: false}], history: [{timestamp : '1223289348934934',author : 'Tomas Sychra',taskid : '2123434333343434',changes: [{name: 'title',oldvalue: 'aa', newvalue: 'bb'}]}]}</script></head><body></body></html>";

	// It is imporatnt to tell, which input will be used (Temporary String coded above or FileName )
	private Boolean READ_FROM_STRING = false;
	private InputStream input;

	// Regex pattern to mine JSON data from HTML page
	private final String scriptPattern = "(?i)(^.*?)(//begin_of_data)(.+?)(//end_of_data)(.*$)";
	//private final String scriptPattern = "(?i)(.*?)(<script id=\"data\" type=\"x-next/x-json\">)(.+?)(</script>)(.?)";
	//private final String scriptPattern = "^.*(<script id=\"data\" type=\"x-next/x-json\">)(.*)(</script>).*$";

	// Alld information about project
	private JSONObject projectData;

	// Reader 
	private BufferedReader reader;

	private Project mProject = null;
	private ArrayList<Task> mTasksList = null;
	private ArrayList<TaskHistory> mTasksHistory = null;

	private String firstTemplate = null;
	private String secondTemplate = null;

	/*********************************************************/
	/************************* READING ***********************/
	/*********************************************************/
	/**
	 * This method open file input stream and attach reader to stream
	 * @param filePath
	 * @return
	 * @throws Throwable
	 */

	private void openDataFile() throws Throwable {
		// Open the file 
		try {
			input = new FileInputStream(mFilePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (input == null) {
			throw new Throwable("Cant open file");
		}
	}

	private BufferedReader attachReader() {
		// Attach Reader and set it to UTF-8 encoding
		BufferedReader reader;
		if (READ_FROM_STRING) {
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
		StringBuilder wholeFile = new StringBuilder();
		String readLine = null;
		while ((readLine = reader.readLine()) != null) {
			wholeFile.append(readLine);
		}

		String jsonString = wholeFile.toString().replaceAll(scriptPattern, "$3");
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
			if (!READ_FROM_STRING) {
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

			//Log.i("Project ID: ", projectId);
			//Log.i("Project Title: ", projectName);

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
			//Log.i("Task id:", newTask.getId());
			//Log.i("Task title:", newTask.getTitle());

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
	 * This method create a History of tasks from JSON code in file
	 * @param jsonArrayHistory
	 * @param project
	 * @return
	 * @throws JSONException 
	 */
	private ArrayList<TaskHistory> createHistoryList(JSONArray jsonArrayHistory) throws JSONException {
		ArrayList<TaskHistory> historyList = new ArrayList<TaskHistory>();

		for (int i = 0; i < jsonArrayHistory.length(); i++) {
			JSONObject historyJson = jsonArrayHistory.getJSONObject(i);
			TaskHistory newHistory = new TaskHistory(historyJson);

			// Debug history
			//Log.i("TaskId: ", newHistory.getTaskId());
			//Log.i("TaskAuthor: ", newHistory.getAuthor());
			//Log.i("Task TimeStamp: ", newHistory.getTimeStamp());
			historyList.add(newHistory);
		}
		return historyList;
	}


	/**
	 * getHistory
	 * @return History
	 */
	public ArrayList<TaskHistory> getHistory() {
		JSONArray jsonArrayHistory = null;
		ArrayList<TaskHistory> historyList = null;

		try {
			jsonArrayHistory = projectData.getJSONArray("history");
			historyList = createHistoryList(jsonArrayHistory);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return historyList;

	}

	public ArrayList<TaskHistory> parseHistory(String histstr) {

		JSONArray json;
		ArrayList<TaskHistory> historyList = null;

		if (histstr == null)
			return new ArrayList<TaskHistory>();
		if (histstr.equals(""))
			return new ArrayList<TaskHistory>();

		try {
			json = new JSONArray(histstr);
			historyList = createHistoryList(json);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}


		return historyList;

	}


	/*********************************************************/
	/************************* WRITING ***********************/
	/*********************************************************/
	public void setProject(Project pProject) {
		mProject = pProject;
	}

	public void setTasks(ArrayList<Task> pNewTask) {
		mTasksList = pNewTask;
	}

	public void setHistory(ArrayList<TaskHistory> pTaskHistory) {
		mTasksHistory = pTaskHistory;
	}

	private JSONObject convertTaskToJSONObject(Task task) throws JSONException {
		JSONObject jsonTask = new JSONObject();

		// Fill JSONObject with Task data
		jsonTask.put("id", task.getId());
		jsonTask.put("title", task.getTitle());
		jsonTask.put("description", task.getDescription());
		jsonTask.put("date", task.getDate().toString());
		jsonTask.put("partProject", task.getProject().getTitle());
		jsonTask.put("partContexts", task.getContext());
		jsonTask.put("important", task.getPriority());
		jsonTask.put("status", task.isCompleted());

		return jsonTask;
	}

	private JSONObject convertHistoryToJSONObject(TaskHistory taskHistory) throws JSONException {
		JSONObject jsonHistory = new JSONObject();

		// Fill JSONObject with Task data
		jsonHistory.put("timestamp", taskHistory.getTimeStamp());
		jsonHistory.put("author", taskHistory.getAuthor());
		jsonHistory.put("taskid", taskHistory.getTaskId());

		// Generate changes
		JSONArray jsonChanges = new JSONArray();
		for (int i = 0; i < taskHistory.getChanges().size(); i++) {
			JSONObject jsonOneChange = new JSONObject();
			HistoryTaskChange oneChange = taskHistory.getChanges().get(i);
			jsonOneChange.put("name", oneChange.getName());
			jsonOneChange.put("oldvalue", oneChange.getOldValue());
			jsonOneChange.put("newvalue", oneChange.getNewValue());
			jsonChanges.put(jsonOneChange);
		}
		jsonHistory.put("changes", jsonChanges);

		return jsonHistory;
	}

	private String generateJSONStringProject() throws JSONException {
		JSONObject projectData = new JSONObject();

		// Fill JSON data
		projectData.put("id", mProject.getId());
		projectData.put("projectname", mProject.getTitle());

		// Generate array of JSONObject for tasks
		JSONArray jsonTasksArray = new JSONArray();
		for (int i = 0; i < mTasksList.size(); i++) {
			jsonTasksArray.put(convertTaskToJSONObject(mTasksList.get(i)));
		}
		projectData.put("data", jsonTasksArray);

		// Generate JSONObject for history 
		JSONArray jsonHistoryArray = new JSONArray();
		for (int i = 0; i < mTasksHistory.size(); i++) {
			jsonHistoryArray.put(convertHistoryToJSONObject(mTasksHistory.get(i)));
		}
		projectData.put("history", jsonHistoryArray);

		return projectData.toString();
	}

	private String readFileFromResource(Context c, Integer id) throws IOException {
		InputStream input = c.getResources().openRawResource(id);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));

		String eol = System.getProperty("line.separator");
		StringBuilder wholeFile = new StringBuilder();
		String readLine = null;

		while ((readLine = reader.readLine()) != null) {
			wholeFile.append(readLine);
			wholeFile.append(eol);
		}
		// Close the InputStream and BufferedReader
		input.close();
		reader.close();
		return wholeFile.toString();
	}

	/**
	 *  Load Templates from resources
	 */
	private void loadTemplates(Context c) throws IOException, JSONException {
		// Prepare data to write
		firstTemplate = readFileFromResource(c, cz.fit.next.R.raw.templatehtmlfirst);
		secondTemplate = readFileFromResource(c, cz.fit.next.R.raw.templatehtmlsecond);
	}

	/**
	 * Write a new File with JSON data in Template
	 * @param c
	 * @param pFileName
	 * @throws IOException
	 * @throws JSONException
	 */
	public void writeFile(Context c, String pFileName) throws IOException, JSONException {
		FileOutputStream fileOut = new FileOutputStream(pFileName);
		OutputStreamWriter fileStreamWriter = new OutputStreamWriter(fileOut);

		// Load Templates if it is write firsttime
		if (firstTemplate == null || secondTemplate == null) {
			loadTemplates(c);
		}

		String jsonStringToWrite = generateJSONStringProject();

		// Write new data to file
		fileStreamWriter.write(firstTemplate +
				jsonStringToWrite +
				secondTemplate);


		fileStreamWriter.flush();
		fileStreamWriter.close();
	}

	public String generateHistoryString() {
		JSONArray jsonHistoryArray = new JSONArray();

		if (mTasksHistory == null)
			return "";

		try {
			for (int i = 0; i < mTasksHistory.size(); i++) {
				jsonHistoryArray.put(convertHistoryToJSONObject(mTasksHistory.get(i)));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonHistoryArray.toString();
	}
}
