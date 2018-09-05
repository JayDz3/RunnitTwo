package com.idesign.runnit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class NotificationService extends BroadcastReceiver
{
  private final String NOTIFICATION_ACTION_FILTER = "Notification_Action";
  private final String NOTIFICATION_CHANNEL_ID = "channel_id";
  private final String MESSAGE = "message";

  public NotificationService() { }

  @Override
  public void onReceive(Context context, Intent intent)
  {
    final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    final Intent newIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    if (intent.getAction() != null && intent.getAction().equals(NOTIFICATION_ACTION_FILTER))
    {
        final String channelId = intent.getStringExtra(NOTIFICATION_CHANNEL_ID);
        final String message = intent.getStringExtra(MESSAGE);
        final int id = channelId.hashCode();
        final long[] v = {1000, 300, 150, 300, 150, 300, 150, 300};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

          if (notificationManager.getNotificationChannel(channelId) == null) {
              final NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
              channel.enableVibration(true);
              channel.setSound(null, null);
              channel.setVibrationPattern(v);
              channel.setDescription(channelId);
              notificationManager.createNotificationChannel(channel);

              final String _title = "Your first notification";
              final String _content = "on channel: " + channelId;
              createNotification(_title, _content, channelId, id, notificationManagerCompat, context, pendingIntent);

            } else {
              createNotification(channelId, message, channelId, id, notificationManagerCompat, context, pendingIntent);
            }

          } else {
          createNotification(channelId, message, channelId, id, notificationManagerCompat, context, pendingIntent);
        }
    }
  }

  public void createNotification(String _title,
                                 String _content,
                                 String channelId,
                                 int notificationId,
                                 NotificationManagerCompat notificationManagerCompat,
                                 Context context,
                                 PendingIntent pendingIntent)
  {
    final Notification notification = new NotificationCompat.Builder(context, channelId)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setContentTitle(_title)
    .setChannelId(channelId)
    .setContentText(_content)
    .setContentIntent(pendingIntent)
    .setSmallIcon(R.drawable.ic_people_outline_black_24dp)
    .setAutoCancel(true).build();
    notificationManagerCompat.notify(notificationId, notification);
  }
}
