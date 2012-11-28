package cz.fit.next.widget;

import cz.fit.next.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NEXTWidget extends AppWidgetProvider {
	
	public static String ACTION_WIDGET_CONFIGURE = "ConfigureWidget";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
		Intent configIntent = new Intent(context, ClickAddActivity.class);
		configIntent.setAction(ACTION_WIDGET_CONFIGURE);
	
		//active.putExtra("msg", "Message for Button 1");
		PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
		//PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.button_add, actionPendingIntent);
		//remoteViews.setOnClickPendingIntent(R.id.button_two, configPendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}


}
