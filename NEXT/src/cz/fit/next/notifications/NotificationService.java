package cz.fit.next.notifications;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import cz.fit.next.MainActivity;
import cz.fit.next.R;
import cz.fit.next.backend.DateTime;
import cz.fit.next.backend.SettingsProvider;
import cz.fit.next.backend.Task;
import cz.fit.next.backend.database.TasksDataSource;
import cz.fit.next.notifications.NotificationsAlarmReceiver;
import cz.fit.next.preferences.SettingsFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.util.Log;

public class NotificationService extends Service {

	private final String SP_LAST_NOTIFIED = "SP_LAST_TIME";
	
	public final static String INTENT_TASK_ID = "INTENT_TASK_ID";

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
		// SettingsProvider sp = new SettingsProvider(getApplicationContext());

		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i(TAG, "onStart");

		// SettingsProvider sp = new SettingsProvider(getApplicationContext());

		searchForUpcomingTask();

		return START_NOT_STICKY;
	}

	private void searchForUpcomingTask() {

		class ExecClass extends AsyncTask<Void, Void, Params> {

			@Override
			protected Params doInBackground(Void... arg0) {

				Params p = new Params();
				p.notifications = new ArrayList<Task>();

				SettingsProvider sp = new SettingsProvider(
						getApplicationContext());

				TasksDataSource ds = new TasksDataSource(
						getApplicationContext());
				ds.open();

				Cursor c = ds.getFullAllTasksCursor();

				DateTime upcomingTime = new DateTime(Long.MAX_VALUE);
				String upcomingId = null;
				String lastDateS = sp.getString(SP_LAST_NOTIFIED, null);
				DateTime lastDate;
				if (lastDateS != null)
					lastDate = new DateTime(Long.parseLong(lastDateS));
				else
					lastDate = new DateTime(0);

				DateTime current = new DateTime();

				while (c.moveToNext()) {
					Task t = new Task(c);

					if (t.getDate().isSomeday()) {
						continue;
					}
					
					if (t.isCompleted()) {
						continue;
					}
					if (t.getDate().isAllday()) {
						

						if ((t.getDate().equalsToDay(current))
								&& (!t.isCompleted())
								&& (!t.getDate().equalsToMinute(lastDate))
								&& (isTimeToAlldayNotifications())) {

							p.notifications.add(t);
							DateTime dt = new DateTime();
							sp.storeString(SP_LAST_NOTIFIED, dt.toString());
							
						}
						continue;
					}

					if (t.getDate().equalsToMinute(current) && (!t.getDate().equalsToMinute(lastDate))) {

						if (!t.isCompleted()) {
							p.notifications.add(t);
							DateTime dt = new DateTime();
							sp.storeString(SP_LAST_NOTIFIED, dt.toString());
						}
						continue;
					}

					if (t.getDate().toMiliseconds() < current.toMiliseconds()) {
						continue;
					}
					if (t.getDate().toMiliseconds() > upcomingTime
							.toMiliseconds()) {
						continue;
					}

					upcomingTime = t.getDate();
					upcomingId = t.getId();
				}

				if (upcomingId != null)
					p.upcoming = ds.getTaskById(upcomingId);

				if (p.upcoming == null) {
					// Log.i(TAG, "E3");
				} else {

					Log.i(TAG,
							"Selected upcoming task: " + p.upcoming.getTitle());
				}
				ds.close();

				return p;
			}

			@Override
			protected void onPostExecute(Params result) {
				super.onPostExecute(result);

				if (result != null) {
					if (result.upcoming != null) {
						setAlarm(result.upcoming.getDate().toMiliseconds());
					}
					for (int i = 0; i < result.notifications.size(); i++) {
						showNotification(result.notifications.get(i));
					}
				}

				stopSelf();

			}

		}
		
		ExecClass e = new ExecClass();
		e.execute();

	}

	private void setAlarm(long time) {

		NotificationsAlarmReceiver ar = new NotificationsAlarmReceiver(
				getApplicationContext(), time);
		ar.run();

		Log.i(TAG,
				"Alarm set to: " + new DateTime(time).toLocaleDateTimeString());

	}

	private void showNotification(Task t) {
		
		SettingsProvider sp = new SettingsProvider(getApplicationContext());
		if (!sp.getBoolean(SettingsFragment.PREF_NOTIFICATIONS_ENABLED, false)) {
			return;
		}

		String title = t.getTitle();
		String content = getLocalizedDateTime(t.getDate());
		String ticker = t.getTitle();

		Notification.Builder mBuilder = new Notification.Builder(this)
				.setSmallIcon(R.drawable.menu_next).setContentTitle(title)
				.setContentText(content).setTicker(ticker).setAutoCancel(true);

		Intent resultIntent = new Intent(this, MainActivity.class);
		resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		resultIntent.putExtra(INTENT_TASK_ID, t.getId());
		PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		int notid = 0;
		mNotificationManager.notify(notid, mBuilder.getNotification());
	}
	
	private boolean isTimeToAlldayNotifications() {
		SettingsProvider sp = new SettingsProvider(getApplicationContext());
		
		DateTime dNow = new DateTime();
		DateTime dAllday = new DateTime(Long.parseLong(sp.getString(SettingsFragment.PREF_NOTIFICATIONS_ALLDAYTIME, "1")));
		
		GregorianCalendar now = dNow.toCalendar();
		GregorianCalendar allday = dAllday.toCalendar();
		
		if ((now.get(Calendar.HOUR_OF_DAY) == allday.get(Calendar.HOUR_OF_DAY))
			&& (now.get(Calendar.MINUTE) == allday.get(Calendar.MINUTE))) {
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns localized date and time string
	 */
	public String getLocalizedDateTime(DateTime dt) {
		
		Date d = new Date (dt.toMiliseconds()); 
		return DateUtils.formatDateTime(getApplicationContext(), d.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
	}

}
