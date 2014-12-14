package com.synxapps.bluewave.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class NotificationHandler {
	
	private static NotificationHandler instance;
	
	public static NotificationHandler getInstance() {
		if (instance == null) {
			instance = new NotificationHandler();
		}
		return instance;
	}
	
	public int showNotification(Activity parent, Class<?> target, int icon, int vibration, String title, String content, Bundle data) {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(parent)
		        .setSmallIcon(icon)
		        .setContentTitle(title)
		        .setContentText(content)
		        .setAutoCancel(true);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(parent, target);
		if (data != null) {
			resultIntent.putExtras(data);
		}

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(parent);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(target);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
		int nID = 0;
		// mId allows you to update the notification later on.
		mNotificationManager.notify(nID, mBuilder.build());
		
		//Vibrate when displaying the notification for the specified amount of time
		Vibrator v = (Vibrator) parent.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(vibration);
		
		return nID;
	}

}
