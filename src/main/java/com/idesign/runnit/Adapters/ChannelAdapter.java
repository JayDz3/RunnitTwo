package com.idesign.runnit.Adapters;

import android.app.NotificationManager;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.Items.FirestoreChannel;

import com.idesign.runnit.Items.SubscribedUser;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.AdminChannelViewHolder>
{
  private List<FirestoreChannel> mChannels;
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final Context mContext;
  private AdminChannelAdapterListener mListener;

  private int _open;
  private User mUser;
  private String customMessage;
  private boolean enabled = true;


  private final PublishSubject<Boolean> doDestroy = PublishSubject.create();

  class AdminChannelViewHolder extends RecyclerView.ViewHolder
  {
    private TextView channelNameView;
    private TextView channelTimeView;
    private Button deleteButton;
    private Button sendNotificationButton;

    private ImageButton activeUsersButton;

    private Button cancelDeleteButton;
    private Button confirmDeleteButton;

    AdminChannelViewHolder(View view)
    {
      super(view);
      activeUsersButton = view.findViewById(R.id.channel_item_admin_see_active_users_icon);
      channelNameView = view.findViewById(R.id.channel_item_admin_text_view);
      channelTimeView = view.findViewById(R.id.channel_item_admin_channel_sent);
      deleteButton = view.findViewById(R.id.channel_item_admin_delete_icon);
      sendNotificationButton = view.findViewById(R.id.channel_item_admin_send_notification_icon);
      cancelDeleteButton = view.findViewById(R.id.channel_item_admin_delete_cancel);
      confirmDeleteButton = view.findViewById(R.id.channel_item_admin_delete_confirm);
    }
  }

  public ChannelAdapter(List<FirestoreChannel> channels, Context context, AdminChannelAdapterListener listener, int _open, String customMessage)
  {
    mChannels = channels;
    mContext = context;
    setListener(listener);
    this._open = _open;
    this.customMessage = customMessage;
  }

  /*
   * Set from UserViewModel in Channel Activity
   */
  public void setUser(User user)
  {
    mUser = user;
  }

  private void setListener(AdminChannelAdapterListener listener)
  {
    if (mListener == null)
    {
      mListener = listener;
    }
  }

  public void setDoDestroy()
  {
    doDestroy.onNext(true);
  }

  @Override
  @NonNull
  public AdminChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
  {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item_admin, parent, false);
    return new AdminChannelViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull AdminChannelViewHolder viewHolder, final int position)
  {
    final FirestoreChannel channel = mChannels.get(position);
    final String channelId = channel.get_channelId();
    final String orgPushId = channel.get_orgPushId();
    final Timestamp sentAt = channel.get_lastSent();
    final DocumentReference channelRef = mFirestore.getAdminChannel(orgPushId, channelId);
    if (sentAt != null)
    {
      final long sentTime = sentAt.toDate().getTime();
      final String timeString = getMinutes(sentTime);
      viewHolder.channelTimeView.setText(timeString);
      // doSubscribe(sentTime, viewHolder);
    }

    viewHolder.channelNameView.setText(channel.get_channelId());
    viewHolder.deleteButton.setOnClickListener(l -> deleteChannel(viewHolder, position));
    viewHolder.activeUsersButton.setOnClickListener(l -> showActiveUsers(channelRef, viewHolder));
    viewHolder.sendNotificationButton.setOnClickListener(l -> sendNotification(channelRef, viewHolder));
    viewHolder.confirmDeleteButton.setOnClickListener(l -> confirmDeleteChannel(channelRef, channel, viewHolder));
    viewHolder.cancelDeleteButton.setOnClickListener(l -> cancelDeleteChannel(viewHolder));

    if (position == _open) {
      disableButtons(viewHolder);
      showDeleteOptions(viewHolder);

    } else {
      hideDeleteOptions(viewHolder);
      enableButtons(viewHolder);
    }
  }

  /* public void doSubscribe(final long sentTime, AdminChannelViewHolder viewHolder)
  {
    Disposable disposable = Observable.interval(0, 1, TimeUnit.MINUTES)
    .takeUntil(doDestroy)
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(r -> {
      final String timeString = getMinutes(sentTime);
      viewHolder.channelTimeView.setText(timeString);
    });
  } */

  public String getMinutes(final long then)
  {
    final String ago = " minutes";
    final String over = "Over an hour";
    final Date today = new Date();
    final long now = today.getTime();
    final long difference = now - then;
    final long minutes = TimeUnit.MILLISECONDS.toMinutes(difference);
    final String minutesString = String.valueOf(minutes);

    if (minutes > 59) {
      return over;
    } else {
      return minutesString + ago;
    }
  }
  /*
   * Test to add current user to this channel, should trigger notification function
   */

  private void hideDeleteOptions(AdminChannelViewHolder viewHolder)
  {
    viewHolder.confirmDeleteButton.setVisibility(View.GONE);
    viewHolder.cancelDeleteButton.setVisibility(View.GONE);
    viewHolder.sendNotificationButton.setVisibility(View.VISIBLE);
  }

  private void showDeleteOptions(AdminChannelViewHolder viewHolder)
  {
    viewHolder.confirmDeleteButton.setVisibility(View.VISIBLE);
    viewHolder.cancelDeleteButton.setVisibility(View.VISIBLE);
    viewHolder.sendNotificationButton.setVisibility(View.INVISIBLE);
  }

  private void showActiveUsers(DocumentReference channelRef, AdminChannelViewHolder viewHolder)
  {
    if (!enabled)
    {
      return;
    }
    setEnabled(false);
    disableButtons(viewHolder);
    final String chennelId = channelRef.getId();
    final String orgPushId = mUser.get_organizationPushId();
    mListener.getUsers(chennelId, orgPushId);
    enableButtons(viewHolder);
  }

  public void setMessage(String message)
  {
    customMessage = message;
  }

  public void setEnabled(boolean status)
  {
    enabled = status;
  }

  private void sendNotification(final DocumentReference channelRef, AdminChannelViewHolder viewHolder)
  {
    final String COLLECTION_ACTIVE_USERS = "ActiveUsers";
    final CollectionReference activeUsersReference = channelRef.collection(COLLECTION_ACTIVE_USERS);
    final CollectionReference subscribedUsersReference = mFirestore.subscribedUsersReference(channelRef);
    final String text;

    mListener.setMessage();

    if (!customMessage.equals("")) {
      text = customMessage;

    } else {
      text = "Assistance Needed";
    }

    final String _message = text;

    disableButtons(viewHolder);
    mListener.disable();

    subscribedUsersReference.get()
    .continueWithTask(usersSnapshot ->
    {
      List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
      for (final DocumentSnapshot ds : usersSnapshot.getResult().getDocuments())
      {
        final SubscribedUser subscribedUser = mFirestore.toFirestoreObject(ds, SubscribedUser.class);
        final String id = subscribedUser.get_pushId();
        if (subscribedUser.get_loggedIn())
        {
          final DocumentReference activeUserRef = activeUsersReference.document(id);
          /*
           *   Add Task
           */
          tasks.add(activeUserRef.get().addOnSuccessListener(userSnapshot ->
          {
            if (userSnapshot.exists()) {
              activeUserRef.delete()
              .onSuccessTask(ignore -> mFirestore.setActiveUser(activeUsersReference, id, _message));

            } else {
              mFirestore.setActiveUser(activeUsersReference, id, _message);
            }
          }));
          // End Add Task //
        }
      }
      return Tasks.whenAll(tasks);
    })
    .onSuccessTask(ignore -> mFirestore.updateLastSent(channelRef))
    .addOnSuccessListener(l ->
    {
      mListener.enable();
      enableButtons(viewHolder);
    })
    .addOnFailureListener(e ->
    {
      showToast(e.getMessage());
      mListener.enable();
      enableButtons(viewHolder);
    });
  }

  private void confirmDeleteChannel(final DocumentReference channelRef, final FirestoreChannel channel, AdminChannelViewHolder viewHolder)
  {
    final String channelId = channel.get_channelId();
    final CollectionReference activeUsersReference = mFirestore.getChannelActiveUsersReference(channelRef);
    final CollectionReference subscribedUsersReference = mFirestore.subscribedUsersReference(channelRef);
    hideDeleteOptions(viewHolder);
    mListener.disable();
    _open = -1;
    mListener.setOpen(_open);

    activeUsersReference.get()
    .onSuccessTask(this::deleteActiveUsersFromChannelBatch)
    .continueWithTask(ignore -> subscribedUsersReference.get())
    .onSuccessTask(usersSnapshot -> deleteSubscribedUsersBatch(usersSnapshot, channelId))
    .continueWithTask(ignore -> mFirestore.deleteAdminChannel(channelRef))
    .addOnSuccessListener(l ->
    {
      deleteNotificationChannel(channelId);
      enableButtons(viewHolder);
      mListener.enable();
    })
    .addOnFailureListener(e ->
    {
      enableButtons(viewHolder);
      mListener.enable();
      showToast("error deleting channel: " + e.getMessage());
    });
  }

  private void deleteChannel(AdminChannelViewHolder viewHolder, final int position)
  {
    _open = position;
    mListener.setOpen(position);
    disableButtons(viewHolder);
    showDeleteOptions(viewHolder);
    notifyDataSetChanged();
  }

  private void cancelDeleteChannel(AdminChannelViewHolder viewHolder)
  {
    hideDeleteOptions(viewHolder);
    _open = -1;
    mListener.setOpen(-1);
    enableButtons(viewHolder);
  }

  /*
   *  Delete Channel Document's Collection of Active users
   */
  private Task<Void> deleteActiveUsersFromChannelBatch(QuerySnapshot activeUsers)
  {
    final WriteBatch batch = mFirestore.batch();
    if (activeUsers == null)
    {
      return batch.commit();
    }
    for (DocumentSnapshot ds : activeUsers.getDocuments())
    {
      final DocumentReference ref = ds.getReference();
      batch.delete(ref);
    }
    return batch.commit();
  }

  /*
   *  Delete Subscribed Users returned from query of channel SubscribedUsers Collection Reference
   *  @param subscribedUsers : list of documents returned from query
   */
  private Task<Void> deleteSubscribedUsersBatch(QuerySnapshot subscribedUsers, String channelId)
  {
    final WriteBatch batch = mFirestore.batch();
    final String COLLECTION_CHANNELS  = "Channels";

    if (subscribedUsers == null)
    {
      return batch.commit();
    }
    for (DocumentSnapshot ds : subscribedUsers.getDocuments())
    {
      final DocumentReference ref = ds.getReference();
      final DocumentReference userChannelRef = mFirestore.getUsers().document(ds.getId()).collection(COLLECTION_CHANNELS).document(channelId);

      batch.delete(userChannelRef);
      batch.delete(ref);
    }
    return batch.commit();
  }

  private void disableButtons(AdminChannelViewHolder viewHolder)
  {
    viewHolder.deleteButton.setEnabled(false);
    viewHolder.deleteButton.setClickable(false);
    viewHolder.sendNotificationButton.setClickable(false);
    viewHolder.sendNotificationButton.setEnabled(false);
    viewHolder.activeUsersButton.setEnabled(false);
    viewHolder.activeUsersButton.setClickable(false);
  }

  private void enableButtons(AdminChannelViewHolder viewHolder)
  {
    viewHolder.deleteButton.setClickable(true);
    viewHolder.deleteButton.setEnabled(true);
    viewHolder.sendNotificationButton.setClickable(true);
    viewHolder.sendNotificationButton.setEnabled(true);
    viewHolder.activeUsersButton.setEnabled(true);
    viewHolder.activeUsersButton.setClickable(true);
  }

  private void deleteNotificationChannel(String chanelId)
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
      NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
      Objects.requireNonNull(notificationManager).deleteNotificationChannel(chanelId);
    }
  }

  public List<FirestoreChannel> getItems()
  {
    return mChannels;
  }

  public void setItems(List<FirestoreChannel> channels)
  {
    mChannels = channels;
    notifyDataSetChanged();
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

  public interface AdminChannelAdapterListener {
    void disable();
    void enable();
    void setOpen(int open);
    void setMessage();
    void getUsers(final String channelId, final String orgPushId);
  }
}