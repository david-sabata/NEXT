package cz.fit.next.backend.database;


public interface Constants {


	public static final String TABLE_PROJECTS = "projects";

	public static final String TABLE_TASKS = "tasks";


	// project name used for tasks with no project set
	public static final String IMPLICIT_PROJECT_NAME = "default-project";


	// column names as stored in tables	
	public static final String COLUMN_ID = "_id"; // long, needs to be '_id'
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_PROJECTS_ID = "projects_id"; // FK -> TABLE_PROJECTS
	public static final String COLUMN_DESCRIPTION = "description"; // string
	public static final String COLUMN_CONTEXT = "context"; // string
	public static final String COLUMN_DATETIME = "datetime"; // serialized DateTime object
	public static final String COLUMN_DATETIME_TYPE = "datetime_type"; // date/datetime
	public static final String COLUMN_PRIORITY = "priority"; // int
	public static final String COLUMN_COMPLETED = "completed"; // int
	public static final String COLUMN_STARRED = "starred"; // int


	// unique column names to disambiguate names when using JOIN
	// e.g. to set alias COLUMN_ALIAS_PROJECTS_TITLE <==> TABLE_PROJECTS.COLUMN_TITLE
	public static final String COLUMN_ALIAS_PROJECTS_TITLE = "project_title"; // string
	public static final String COLUMN_ALIAS_TASKS_TITLE = "task_title"; // string	




	public static final String DATABASE_NAME = "NEXT.db";

	public static final int DATABASE_VERSION = 14;
}
