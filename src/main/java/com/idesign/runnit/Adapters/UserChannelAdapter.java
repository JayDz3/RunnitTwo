package com.idesign.runnit.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.ActiveUser;
import com.idesign.runnit.Items.FirestoreChannel;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.R;

import java.util.List;

public class UserChannelAdapter extends RecyclerView.Adapter<UserChannelAdapter.MyViewHolder>
{
  private List<FirestoreChannel> mChannels;
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
    final DocumentReference userChannelRef = mFirestore.getUserChannelReference(uid, channelId);
    viewHolder.radioButton.setText(channelId);
    viewHolder.radioButton.setOnClickListener(l -> toggleChannelStatus(userChannelRef, channel, viewHolder));

    userChannelRef.get()
    .addOnSuccessListener(channelRef -> {
      if (channelRef.exists())
      {
        viewHolder.radioButton.setChecked(true);
      } else {
        viewHolder.radioButton.setChecked(false);
      }
    })
    .addOnFailureListener(e -> showToast("error: " + e.getMessage()));
    // viewHolder.radioButton.setOnClickListener(l -> toggleChannelStatus(channelRef, channel));
    // viewHolder.deleteButton.setOnClickListener(l -> deleteChannel(channelRef, channel, position));
  }

  public void toggleChannelStatus(final DocumentReference userChannelRef, final FirestoreChannel channel, MyViewHolder viewHolder)
  {
    userChannelRef.get()
    .onSuccessTask(snap ->
    {
      final String uid = mAuth.user().getUid();
      final DocumentReference userRef = mFirestore.getUsers().document(uid);
      if (snap == null || !snap.exists()) {
        viewHolder.radioButton.setChecked(true);
        return mFirestore.addChannelToUserTask(channel, userRef);
      } else {
        viewHolder.radioButton.setChecked(false);
        return mFirestore.removeChannelFromUserTask(snap.getReference());
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
