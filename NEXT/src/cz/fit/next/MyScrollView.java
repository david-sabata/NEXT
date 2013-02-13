package cz.fit.next;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.deaux.fan.FanView;

public class MyScrollView extends ScrollView {

	private boolean ignoreGesture = false;


	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}




	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		MainActivity activity = (MainActivity) getContext();
		FanView fan = activity.getFanView();

		// close sidebar, set flag to ignore all events until UP
		// and return false which means all other events of current 
		// gesture will be intercepted and processed here instead 
		// of child views
		if (fan.isOpen()) {
			fan.showMenu(); // means toggle
			ignoreGesture = true;
			return false;
		}

		if (ignoreGesture && ev.getAction() == MotionEvent.ACTION_UP) {
			ignoreGesture = false;
			return false;
		}

		if (ignoreGesture) {
			return false;
		}

		return true;
	}


}
