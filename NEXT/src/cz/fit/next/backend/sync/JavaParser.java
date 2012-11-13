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

import cz.fit.next.backend.Project;
import cz.fit.next.backend.Task;

/**
 * @author xsych_000
 *
 */
public class JavaParser {
	private String inputDataString = "<html><head><title>Test</title><!-- Here we will store our data for tasks --><script id='data' type='x-next/x-json'>{ filename: 'editable.html', projectname: 'Reader for NEXT', data: [{title: 'Basic structure', description: 'Create basic layout', date: '1.11.2012', partProject : 'Reader for NEXT', partContexts : ['School','Android'],important: '3',status: true},{ title: 'Plug-in modules', description: 'Create generator for tasks layout',date: '1.11.2012',partProject : 'Reader for NEXT',partContexts : ['School','Javascript'],important: '3', status: false}]}</script></head><body></body></html>";
	
	// It is imporatnt to tell, which input will be used (Temporary String coded above or FileName )
	private Boolean  READ_FROM_STRING = true;
	private InputStream input;
	
	// Regex pattern to mine JSON data from HTML page
	private final String scriptPattern = "(?i)(.*?)(<script id='data' type='x-next/x-json'>)(.+?)(</script>)(.*?)";
	
	// All tasks in project
	ArrayList<Task> tasksList = new ArrayList<Task>();
	
	// Project info
	private String projectName;
	private String fileName;
	
	public ArrayList<Task> getTasksFromHTML(String filePath, Project project) {
		try {
			BufferedReader reader;
			if(!READ_FROM_STRING)  {
				openDataFile(filePath);
				reader = attachReader();
			} else {
				reader = attachReader();
			}
			
			/**
			 *  Get array with JSON Objects
			 *  One JSON Objects is one task
			 */
			JSONArray projectsArray  = parseJsonData(reader);
			
			// Create Tasks from JSON Objects
			addTaskToTaskList(projectsArray, project);
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tasksList;
	}
	
	/**
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private JSONArray parseJsonData(BufferedReader reader) throws IOException, JSONException {
		// Load data to String
		int c;
		String outString = new String();
		
		while((c = reader.read()) != -1) {
			char character = (char) c;
			outString += character;
		}
		
		// Parse data to JSON
		String jsonString = outString.replaceAll(scriptPattern, "$3");
		JSONObject projectData = new JSONObject(jsonString);
		
		// parse FileName and ProjectName from JSONObject
		projectName = projectData.getString("projectname");
		fileName = projectData.getString("filename");	
		// Parse projects data from Object
		JSONArray projectsArray = projectData.getJSONArray("data");
		
		return projectsArray;
	}

	/**
	 * Generate Task for every JSON Object task and add Task to taskArrayList
	 * @param jsonArrayTask
	 * @throws JSONException 
	 */
	private void addTaskToTaskList(JSONArray jsonArrayTask, Project project) throws JSONException {
		
		for (int i = 0; i < jsonArrayTask.length(); i++) {
			JSONObject taskJson = jsonArrayTask.getJSONObject(i);
			Task newTask = new Task(taskJson, project);
			tasksList.add(newTask);
		}	
	}
	
	
	/**
	 * This method open file input stream and attach reader to stream
	 * @param filePath
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unused")
	private void openDataFile (String filePath) throws Throwable {
		// Open the file 
		try {
			input = new FileInputStream(filePath);	
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
}


