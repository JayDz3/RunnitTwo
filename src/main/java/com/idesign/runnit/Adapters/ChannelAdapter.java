package com.idesign.runnit.Adapters;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.ActiveUser;
import com.idesign.runnit.Items.FirestoreChannel;

import com.idesign.runnit.Items.User;
import com.idesign.runnit.R;

import java.util.List;
import java.util.Objects;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.AdminChannelViewHolder>
{
  private List<FirestoreChannel> mChannels;
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final Context mContext;
  private AdminChannelAdapterListener mListener;

  private int _open;
  private User mUser;

  class AdminChannelViewHolder extends RecyclerView.ViewHolder
  {
    private TextView channelNameView;
    private Button deleteButton;
    private Button sendNotificationButton;

    private Button cancelDeleteButton;
    private Button confirmDeleteButton;

    AdminChannelViewHolder(View view)
    {
      super(view);
      channelNameView = view.findViewById(R.id.channel_item_admin_text_view);
      deleteButton = view.findViewById(R.id.channel_item_admin_delete_icon);
      sendNotificationButton = view.findViewById(R.id.channel_item_admin_send_notification_icon);
      cancelDeleteButton = view.findViewById(R.id.channel_item_admin_delete_cancel);
      confirmDeleteButton = view.findViewById(R.id.channel_item_admin_delete_confirm);
    }
  }

  public ChannelAdapter(List<FirestoreChannel> channels, Context context, AdminChannelAdapterListener listener, int _open)
  {
    mChannels = channels;
    mContext = context;
    setListener(listener);
    this._open = _open;
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
    final DocumentReference channelRef = mFirestore.getAdminChannel(orgPushId, channelId);

    viewHolder.channelNameView.setText(channel.get_channelId());

    viewHolder.deleteButton.setOnClickListener(l -> deleteChannel(viewHolder, position));
    viewHolder.sendNotificationButton.setOnClickListener(l -> sendNotification(channelRef, viewHolder));

    viewHolder.confirmDeleteButton.setOnClickListener(l -> confirmDeleteChannel(channelRef, channel, viewHolder));
    viewHolder.cancelDeleteButton.setOnClickListener(l -> cancelDeleteChannel(viewHolder));
    if (_open == position) {
      disableButtons(viewHolder);
      showDeleteOptions(viewHolder);
    } else {
      hideDeleteOptions(viewHolder);
      enableButtons(viewHolder);
    }

    // viewHolder.radioButton.setOnClickListener(l -> toggleChannelStatus(channelRef, channel));
    // viewHolder.deleteButton.setOnClickListener(l -> deleteChannel(channelRef, channel, position));
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

  private void sendNotification(DocumentReference channelRef, AdminChannelViewHolder viewHolder)
  {
    final String COLLECTION_ACTIVE_USERS = "ActiveUsers";
    final String uid = mAuth.user().getUid();
    final ActiveUser thisUser = new ActiveUser(uid);
    final CollectionReference activeUsersReference = channelRef.collection(COLLECTION_ACTIVE_USERS);

    disableButtons(viewHolder);
    mListener.disable();

    activeUsersReference.get()
    .onSuccessTask(activeUsers ->
    {
      Task<Void> task = Tasks.forResult(null);
      if (activeUsers == null)
      {
        return task;
      }

      /*
       *  Remove this task in production
       *  adding admin user to active users for testing purposes
       */
      task = activeUsersReference.document(uid)
      .delete()
      .onSuccessTask(ignore -> activeUsersReference.document(uid).set(thisUser));
      // above //

      for (DocumentSnapshot ds : activeUsers)
      {
        final String id = ds.getId();
        final ActiveUser activeUser = new ActiveUser(id);
        if (!id.equals(uid))
        {
          task = ds.getReference().delete()
          .onSuccessTask(ignore -> activeUsersReference.document(id).set(activeUser));
        }
      }
      return task;
    })
    .addOnSuccessListener(l ->
    {
      enableButtons(viewHolder);
      mListener.enable();
    })
    .addOnFailureListener(e ->
    {
      enableButtons(viewHolder);
      mListener.enable();
      showToast("error adding user: " + e.getMessage());
    });
  }

  private void confirmDeleteChannel(final DocumentReference channelRef, final FirestoreChannel channel, AdminChannelViewHolder viewHolder)
  {
    final String channelId = channel.get_channelId();
    final String orgCode = mUser.get_organizationCode();
    hideDeleteOptions(viewHolder);
    mListener.disable();
    _open = -1;
    mListener.setOpen(_open);

    mFirestore.getChannelActiveUsersReference(channelRef).get()
    .onSuccessTask(this::deleteActiveUsersFromChannelBatch)
    .onSuccessTask(ignore -> mFirestore.deleteAdminChannel(channelRef))
    .onSuccessTask(ignore -> mFirestore.getAllOrganizationUsersQuery(orgCode))
    .onSuccessTask(activeUsers -> deleteChannelFromUsersBatch(activeUsers, channelId))
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
   *  Delete channel from all users that have it under UserChannels
   */
  private Task<Void> deleteChannelFromUsersBatch(QuerySnapshot queriedUsers, String channelId)
  {
    final WriteBatch batch = mFirestore.batch();
    if (queriedUsers == null){
      return batch.commit();
    }
    for (DocumentSnapshot ds : queriedUsers)
    {
      final String userId = ds.getId();
      final DocumentReference ref = mFirestore.getUserChannelReference(userId, channelId);
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
  }

  private void enableButtons(AdminChannelViewHolder viewHolder)
  {
    viewHolder.deleteButton.setClickable(true);
    viewHolder.deleteButton.setEnabled(true);
    viewHolder.sendNotificationButton.setClickable(true);
    viewHolder.sendNotificationButton.setEnabled(true);
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
  }
}

  /* public void toggleChannelStatus(final DocumentReference channelRef, final FirestoreChannel channel)
  {
    mFirestore.setUserReference(mAuth.user().getUid());
    if (channel.is_isActive()) {
      mFirestore.deActivateAdminChannel(channelRef)
      .addOnSuccessListener(t ->
      {
        channel.set_isActive(false);
        showToast("Channel deactivated");
        notifyDataSetChanged();
      });
    } else {
      mFirestore.activateAdminChannel(channelRef)
      .onSuccessTask(ignore -> mFirestore.addChannelToUser(channel))
      .addOnSuccessListener(t ->
      {
        channel.set_isActive(true);
        showToast("Channel activated");
        notifyDataSetChanged();
      })
      .addOnFailureListener(e -> showToast("e: " + e.getMessage()));
    }
  } */

/*
 * Called from channel activity to check for redundant channel name
 */
