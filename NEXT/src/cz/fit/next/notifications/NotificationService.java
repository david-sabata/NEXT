package cz.fit.next.notifications;

import java.util.ArrayList;
import java.util.Calendar;

import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.SettingsProvider;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.database.TasksDataSource;
import cz.fit.next.notifications.NotificationsAlarmReceiver;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class NotificationService extends Service {

	private final String TAG = "NEXT Notification Service";

	private class Params {
		public Task upcoming;
		public ArrayList<Task> notifications;
	};

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
		
		Params p = searchForUpcomingTask();
		setAlarm(p.upcoming.getDate().toMiliseconds());
		
		for (int i = 0; i < p.notifications.size(); i++) {
			showNotification(p.notifications.get(i));
		}
		
		stopSelf();

		return START_NOT_STICKY;
	}

	private Params searchForUpcomingTask() {
		
		Params p = new Params();
		p.notifications = new ArrayList<Task>();
		
		TasksDataSource ds = new TasksDataSource(getApplicationContext());
		ds.open();

		Cursor c = ds.getAllTasksCursor();
		if (!c.moveToFirst()) Log.i(TAG,"E1");
		if (c.moveToPrevious()) Log.i(TAG,"E2");;

		DateTime upcomingTime = new DateTime(0);
		String upcomingId = null;

		DateTime current = new DateTime();

		while (c.moveToNext()) {
			Task t = new Task(c);

			if (t.getDate().isSomeday())
				continue;
			if (t.getDate().isAllday())
				continue; // TODO: ????
			if (t.getDate().toMiliseconds() < current.toMiliseconds())
				continue;
			if (t.getDate().toMiliseconds() > upcomingTime.toMiliseconds())
				continue;

			if ((t.getDate().toCalendar().get(Calendar.HOUR)) == (current
					.toCalendar().get(Calendar.HOUR))
					&& ((t.getDate().toCalendar().get(Calendar.MINUTE)) == (current
							.toCalendar().get(Calendar.MINUTE)))) {
				
				p.notifications.add(t);
			}

			upcomingTime = t.getDate();
			upcomingId = t.getId();
		}

		p.upcoming = ds.getTaskById(upcomingId);
		ds.close();

		Log.i(TAG, "Selected upcoming task: " + p.upcoming.getTitle());

		return p;

	}

	private void setAlarm(long time) {

		NotificationsAlarmReceiver ar = new NotificationsAlarmReceiver(
				getApplicationContext(), time);

	}
	
	private void showNotification(Task t) {
		
		String title = getResources().getString(R.string.upcoming_notification);
		String content = t.getTitle();
		String ticker = getResources().getString(R.string.upcoming_notification);
		
		Notification.Builder mBuilder = new Notification.Builder(this).setSmallIcon(R.drawable.menu_next)
				.setContentTitle(title).setContentText(content)
				.setTicker(ticker).setAutoCancel(true);

		//		Intent resultIntent = new Intent(this, MainActivity.class);
		
		//		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		//		stackBuilder.addParentStack(MainActivity.class);
		//		stackBuilder.addNextIntent(resultIntent);
		//		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		//		mBuilder.setContentIntent(resultPendingIntent);


		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		int notid = 0;
		mNotificationManager.notify(notid, mBuilder.getNotification());
	}

}
