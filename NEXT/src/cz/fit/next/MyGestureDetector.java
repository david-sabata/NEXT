package cz.fit.next;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import com.deaux.fan.FanView;

public class MyGestureDetector extends SimpleOnGestureListener {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 100;


	private FanView mFanView;


	public MyGestureDetector(FanView fan) {
		mFanView = fan;
	}


	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			//			Log.d("FROM", e1.getX() + " | " + e1.getY());
			//			Log.d("TO", e2.getX() + " | " + e2.getY());

			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			// right to left swipe
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				if (mFanView.isOpen()) {
					mFanView.showMenu();
				}
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				if (!mFanView.isOpen()) {
					mFanView.showMenu();
				}
			}


		} catch (Exception e) {
			// nothing
		}
		return false;
	}
}
