package com.idesign.runnit.Adapters;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.FirestoreChannel;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.Items.UserChannel;
import com.idesign.runnit.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserChannelAdapter extends RecyclerView.Adapter<UserChannelAdapter.MyViewHolder>
{
  private List<FirestoreChannel> mChannels;
  private List<UserChannel> mUserChannels;
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final Context mContext;
  private User mUser;

  class MyViewHolder extends RecyclerView.ViewHolder
  {
    private RadioButton radioButton;
    MyViewHolder(View view)
    {
      super(view);
      radioButton = view.findViewById(R.id.channel_item_radio_button);
    }
  }

  public UserChannelAdapter(List<FirestoreChannel> channels, Context context)
  {
    mChannels = channels;
    mContext = context;
  }

  public void setItems(List<FirestoreChannel> channels)
  {
    mChannels = channels;
    notifyDataSetChanged();
  }

  public void setUserChannels(List<UserChannel> mUserChannels)
  {
    this.mUserChannels = mUserChannels;
  }

  public List<FirestoreChannel> getItems()
  {
    return mChannels;
  }

  public void setUser(User user)
  {
    mUser = user;
  }

  @Override
  @NonNull
  public UserChannelAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
  {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item, parent, false);
    return new UserChannelAdapter.MyViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull UserChannelAdapter.MyViewHolder viewHolder, final int position)
  {
    final FirestoreChannel channel = mChannels.get(position);
    final String uid = mAuth.user().getUid();
    final String channelId = channel.get_channelId();
    final DocumentReference channelReference = mFirestore.getAdminChannel(channel.get_orgPushId(), channelId);
    final DocumentReference subscribedUserRef = mFirestore.subscribedUserReference(channelReference, uid);

    viewHolder.radioButton.setText(channelId);
    viewHolder.radioButton.setOnClickListener(l -> toggleChannelStatus(channelReference, subscribedUserRef, viewHolder));

    final UserChannel compareTo = new UserChannel(channelId);
    final List<UserChannel> exists = new ArrayList<>();

    for (UserChannel c : mUserChannels)
    {
      if (c.get_pushId().equals(compareTo.get_pushId()))
      {
        exists.add(compareTo);
      }
    }

    if (exists.size() > 0) {
      viewHolder.radioButton.setChecked(true);

    } else {
      viewHolder.radioButton.setChecked(false);
    }
  }

  private void toggleChannelStatus(final DocumentReference channelRef, final DocumentReference subscribedUserRef, MyViewHolder viewHolder)
  {
    final String uid = mAuth.user().getUid();
    final String firstName = mUser.get_firstName();
    final String lastName = mUser.get_lastName();
    final String channelPushId = channelRef.getId();

    subscribedUserRef.get()
    .onSuccessTask(snap ->
    {
      if (snap == null || !snap.exists()) {
        viewHolder.radioButton.setChecked(true);
        createNotificationChannel(channelPushId);
        return mFirestore.addSubscribedUserTask(channelRef, firstName, lastName, uid);

      } else {
        viewHolder.radioButton.setChecked(false);
        deleteNotificationChannel(channelPushId);
        return mFirestore.deleteSubscribedUserTask(channelRef, uid);

      }
    })
    .onSuccessTask(ignore ->
    {
      final boolean checked = viewHolder.radioButton.isChecked();
      if (checked) {
        return mFirestore.addUserChannel(uid, channelPushId);

      } else {
        return mFirestore.deleteUserChannel(uid, channelPushId);
      }
    })
    .addOnSuccessListener(t ->
    {
      final boolean isChecked = viewHolder.radioButton.isChecked();
      if (isChecked) {
        showToast("Subscribed to channel");

      } else {
        showToast("Unsubscribed from channel");

      }
      notifyDataSetChanged();
    })
    .addOnFailureListener(e -> showToast("e: " + e.getMessage()));
  }

  private void deleteNotificationChannel(String channelId)
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
      NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
      if (Objects.requireNonNull(notificationManager).getNotificationChannel(channelId) != null)
      {
        notificationManager.deleteNotificationChannel(channelId);
      }
    }
  }

  private void createNotificationChannel(String channelId)
  {
    final long[] v = {1000, 300, 150, 300, 150, 300, 150, 300};
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
      NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
      if (Objects.requireNonNull(notificationManager).getNotificationChannel(channelId) == null)
      {
        final NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableVibration(true);
        channel.setSound(null, null);
        channel.setVibrationPattern(v);
        channel.setDescription(channelId);
        notificationManager.createNotificationChannel(channel);
      }
    }
  }


  @Override
  public int getItemCount()
  {
    return mChannels.size();
  }

  public void showToast(CharSequence message)
  {
    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
  }
}
