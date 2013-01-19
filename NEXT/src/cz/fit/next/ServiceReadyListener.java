package cz.fit.next;

import cz.fit.next.backend.TasksModelService;

/**
 * Implemented by fragments, so they can be notified 
 * when service is ready and bound. Fragment's parent 
 * Activity is obligated to notify its current Fragment 
 * once a service is re/bound.
 * 
 * TODO: use generic Service (not needed until we have 
 * 		 more than one widely-used service)
 * 
 * @author David
 */
public interface ServiceReadyListener {

	public void onServiceReady(TasksModelService s);

}
