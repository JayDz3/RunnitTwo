package com.idesign.runnit.Adapters;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.ActiveUser;
import com.idesign.runnit.Items.FirestoreChannel;
import com.idesign.runnit.Items.FirestoreOrg;
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
  private final String COLLECTION_ACTIVE_USERS = "ActiveUsers";

  class AdminChannelViewHolder extends RecyclerView.ViewHolder
  {
    private TextView channelNameView;
    private ImageButton deleteButton;
    private ImageButton sendNotificationButton;

    AdminChannelViewHolder(View view)
    {
      super(view);
      channelNameView = view.findViewById(R.id.channel_item_admin_text_view);
      deleteButton = view.findViewById(R.id.channel_item_admin_delete_icon);
      sendNotificationButton = view.findViewById(R.id.channel_item_admin_send_notification_icon);
    }
  }

  public ChannelAdapter(List<FirestoreChannel> channels, Context context, AdminChannelAdapterListener listener)
  {
    mChannels = channels;
    mContext = context;
    setListener(listener);
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

    viewHolder.deleteButton.setOnClickListener(l -> deleteChannel(channelRef, channel, position, viewHolder));
    viewHolder.sendNotificationButton.setOnClickListener(l -> sendNotification(channelRef, channel, viewHolder));

    // viewHolder.radioButton.setOnClickListener(l -> toggleChannelStatus(channelRef, channel));
    // viewHolder.deleteButton.setOnClickListener(l -> deleteChannel(channelRef, channel, position));
  }

  /*
   * Test to add current user to this channel, should trigger notification function
   */
  private void sendNotification(DocumentReference channelRef, final FirestoreChannel channel, AdminChannelViewHolder viewHolder)
  {
    final String uid = mAuth.user().getUid();
    final ActiveUser thisUser = new ActiveUser(uid);
    disableButtons(viewHolder);
    mListener.disable();
    final CollectionReference activeUsersReference = channelRef.collection(COLLECTION_ACTIVE_USERS);
    activeUsersReference.get()
    .onSuccessTask(activeUsers -> {
      Task<Void> task = Tasks.forResult(null);
      if (activeUsers == null) {
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

      for (DocumentSnapshot ds : activeUsers) {
        String id = ds.getId();
        ActiveUser activeUser = new ActiveUser(id);
        if (!id.equals(uid)) {
          task = ds.getReference()
          .delete()
          .onSuccessTask(ignore -> activeUsersReference.document(id).set(activeUser));
        }
      }
      return task;
    })
    .addOnSuccessListener(l -> {
      enableButtons(viewHolder);
      mListener.enable();
    })
    .addOnFailureListener(e -> {
      enableButtons(viewHolder);
      mListener.enable();
      showToast("error adding user: " + e.getMessage());
    });
  }


  private void deleteChannel(final DocumentReference channelRef,
                             final FirestoreChannel channel,
                             final int position,
                             AdminChannelViewHolder viewHolder)
  {
    disableButtons(viewHolder);
    mListener.disable();
    final String uid = mAuth.user().getUid();
    final String channelId = channel.get_channelId();

    mFirestore.getChannelActiveUsersReference(channelRef).get()
    .onSuccessTask(activeUsers -> deleteActiveUsersFromChannel(activeUsers))
    .onSuccessTask(ignore -> mFirestore.deleteAdminChannel(channelRef))
    .onSuccessTask(ignore -> mFirestore.getUsers().document(uid).get())
    .onSuccessTask(userRef ->
    {
      final User user = mFirestore.toFirestoreObject(userRef, User.class);
      final String orgPushid = user.get_organizationPushId();
      return mFirestore.getOrgSnapshotTask(orgPushid);
    })
    .onSuccessTask(orgRef ->
    {
      final FirestoreOrg org = mFirestore.toFirestoreObject(orgRef, FirestoreOrg.class);
      return mFirestore.getAllOrganizationUsersQuery(org.get_organizationCode());
    })
    .onSuccessTask(activeUsers -> deleteChannelFromUsers(activeUsers, channelId))
    .addOnSuccessListener(l ->
    {
      showToast("channel deleted");
      deleteNotificationChannel(channelId);
      enableButtons(viewHolder);
      mChannels.remove(position);
      setItems(mChannels);
      mListener.enable();
    })
    .addOnFailureListener(e ->
    {
      showToast("error deleting channel: " + e.getMessage());
      enableButtons(viewHolder);
      mListener.enable();
    });
  }

  private Task<Void> deleteActiveUsersFromChannel(QuerySnapshot activeUsers)
  {
    Task<Void> task = Tasks.forResult(null);
    if (activeUsers == null)
    {
      return task;
    }
    for (DocumentSnapshot ds : activeUsers.getDocuments())
    {
      task = ds.getReference().delete();
    }
    return task;
  }

  private Task<Void> deleteChannelFromUsers(QuerySnapshot queriedUsers, String channelId)
  {
    Task<Void> task = Tasks.forResult(null);
    if (queriedUsers == null)
    {
      return task;
    }
    for (DocumentSnapshot ds : queriedUsers)
    {
      final User user = mFirestore.toFirestoreObject(ds, User.class);
      String userId = user.get_pushId();
      DocumentReference ref = mFirestore.getUserChannelReference(userId, channelId);
      task = ref.delete();
    }
    return task;
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
  }
}
