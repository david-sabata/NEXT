package cz.fit.next.backend.sync;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private Context mContext;
    private int mTimeout;
	
	public AlarmReceiver(){ }

    public AlarmReceiver(Context context, int timeoutInSeconds){
    	mContext = context;
    	mTimeout = timeoutInSeconds;
    }
    
    public void run() {
    	AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pendingIntent =
            PendingIntent.getBroadcast(mContext, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.SECOND, mTimeout);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                     pendingIntent);
        Log.i("AlarmManager", "Alarm set.");
    }

     @Override
     public void onReceive(Context context, Intent intent) {
         Log.i("AlarmManager", "ALARM !!!!!!");
         Intent i = new Intent(context, SyncService.class);
         i.putExtra("SyncAlarm", 1);
         context.startService(i);
         
         
     }
}
