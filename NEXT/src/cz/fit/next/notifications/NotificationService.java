package cz.fit.next.notifications;

import java.util.Calendar;

import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.SettingsProvider;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.database.TasksDataSource;
import cz.fit.next.backend.sync.SyncService;
import cz.fit.next.backend.sync.SyncService.ServiceBinder;
import cz.fit.next.preferences.SettingsFragment;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class NotificationService extends Service {
	
	private final String TAG = "NEXT Notification Service";
	
	public class ServiceBinder extends Binder {
		public NotificationService getService() {
			return NotificationService.this;
		}
	};

	private final IBinder mBinder = new ServiceBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		SettingsProvider sp = new SettingsProvider(getApplicationContext());
		
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i(TAG, "onStart");

		SettingsProvider sp = new SettingsProvider(getApplicationContext());
		
		
		return START_NOT_STICKY;
	}
	
	
	private Task searchForUpcomingTask() {
		TasksDataSource ds = new TasksDataSource(getApplicationContext());
		ds.open();
		
		Cursor c = ds.getAllTasksCursor();
		c.moveToFirst();

		Task t = new Task(c);
		DateTime upcomingTime = t.getDate();
		String upcomingId = t.getId();
		
		DateTime current = new DateTime();
		
		while (c.moveToNext()) {
			t = new Task(c);
			
			if (t.getDate().isSomeday()) continue;
			if (t.getDate().isAllday()) continue; // TODO: ????
			if (t.getDate().toMiliseconds() < current.toMiliseconds()) continue;
			if (t.getDate().toMiliseconds() > upcomingTime.toMiliseconds()) continue;
						
			upcomingTime = t.getDate();
			upcomingId = t.getId();
		}
		
		Task ret = ds.getTaskById(upcomingId);
		ds.close();
		
		Log.i(TAG,"Selected upcoming task: " + ret.getTitle());
		
		return ret;
		
	}
	
	

}
