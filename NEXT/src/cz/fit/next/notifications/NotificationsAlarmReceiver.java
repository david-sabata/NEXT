package cz.fit.next.notifications;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationsAlarmReceiver extends BroadcastReceiver {

    private Context mContext;
    private long mTime;
	
	public NotificationsAlarmReceiver(){ }

    public NotificationsAlarmReceiver(Context context, long time){
    	mContext = context;
    	mTime = time;
    }
    
    public void run() {
    	if (mTime == 0) {
    		return;
    	}
    	
    	AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, NotificationsAlarmReceiver.class);
        PendingIntent pendingIntent =
            PendingIntent.getBroadcast(mContext, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(mTime);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                     pendingIntent);
        Log.i("AlarmManager", "Alarm set.");
    }
    
    public void reset() {
    	AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, NotificationsAlarmReceiver.class);
        PendingIntent pendingIntent =
            PendingIntent.getBroadcast(mContext, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.cancel(pendingIntent);
        Log.i("AlarmManager","reset");
    }

     @Override
     public void onReceive(Context context, Intent intent) {
         Log.i("AlarmManager", "ALARM !!!!!!");
         Intent i = new Intent(context, NotificationService.class);
         //i.putExtra("SyncAlarm", 1);
         context.startService(i);
         
         
     }
}
