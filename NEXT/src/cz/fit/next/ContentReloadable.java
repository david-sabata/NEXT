package cz.fit.next;

/**
 * Interface for all fragments
 * 
 * The functionality cannot be implemented using base class
 * because fragments already have parent classes which are 
 * not always the same
 * 
 * @author David
 */
public interface ContentReloadable {

	/**
	 * Reload fragment content
	 */
	public void reloadContent();

}
