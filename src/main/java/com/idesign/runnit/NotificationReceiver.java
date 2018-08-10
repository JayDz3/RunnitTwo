package com.idesign.runnit;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationReceiver extends FirebaseMessagingService
{
  private final String NOTIFICATION_ACTION_FILTER = "Notification_Action";
  private final String NOTIFICATION_CHANNEL_ID = "channel_id";


  @Override
  public void onNewToken(String token)
  {

  }

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage)
  {
    super.onMessageReceived(remoteMessage);
    if (remoteMessage.getData().size() > 0) {
      String channelId = remoteMessage.getData().get("body");
      Intent intent = new Intent(this, NotificationService.class);
      intent.setAction(NOTIFICATION_ACTION_FILTER);
      intent.putExtra(NOTIFICATION_CHANNEL_ID, channelId);
      sendBroadcast(intent);
    }
  }

  @Override
  public void onMessageSent(String msgid)
  {

  }

  @Override
  public void onSendError(String s, Exception e)
  {
    Log.d("MESSAGE SERVICE", "STRING: " + s + " E: " + e.getMessage());
  }

  @Override
  public void onDestroy()
  {
    Log.d("M", "DESTROY");
  }
}
