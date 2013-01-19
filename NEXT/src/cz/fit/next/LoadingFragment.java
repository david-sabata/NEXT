package cz.fit.next;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Dummy fragment loaded right after app starts. It displays 
 * only loader animation and gets disposed when the main
 * service starts
 * 
 * @author David
 */
public class LoadingFragment extends ListFragment {


	/**
	 * Create a new instance of fragment
	 * 
	 * Use ONLY this method to create a new instance!
	 */
	public static LoadingFragment newInstance() {
		throw new RuntimeException("Dont use me, I'm obsolete");

		//		LoadingFragment frag = new LoadingFragment();

		//		Bundle b = new Bundle();
		//		b.putString(ARG_FILTER, filter != null ? filter.toString() : null);
		//
		//		frag.setArguments(b);

		//		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.loading_fragment, container, false);
	}





}
