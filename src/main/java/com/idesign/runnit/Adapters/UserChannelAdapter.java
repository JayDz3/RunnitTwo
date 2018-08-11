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

import com.google.firebase.firestore.DocumentReference;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.FirestoreChannel;
import com.idesign.runnit.R;

import java.util.List;

public class UserChannelAdapter extends RecyclerView.Adapter<UserChannelAdapter.MyViewHolder> {
  private List<FirestoreChannel> mChannels;
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final Context mContext;

  class MyViewHolder extends RecyclerView.ViewHolder
  {
    private RadioButton radioButton;
    private ImageButton deleteButton;
    MyViewHolder(View view)
    {
      super(view);
      radioButton = view.findViewById(R.id.channel_item_radio_button);
      deleteButton = view.findViewById(R.id.channel_item_delete_icon);
    }
  }
  public UserChannelAdapter(List<FirestoreChannel> channels, Context context)
  {
    mChannels = channels;
    mContext = context;
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
    final DocumentReference channelRef = mFirestore.getUserChannelReference(uid, channelId);
    channelRef.get()
    .addOnSuccessListener(snap ->
    {
      if (snap == null || !snap.exists()) {
        viewHolder.radioButton.setChecked(false);
      } else {
        viewHolder.radioButton.setChecked(true);
      }
    })
    .addOnFailureListener(e -> Toast.makeText(mContext, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    viewHolder.radioButton.setOnClickListener(l -> toggleChannelStatus(channelRef, channel, viewHolder));

    // viewHolder.radioButton.setOnClickListener(l -> toggleChannelStatus(channelRef, channel));
    // viewHolder.deleteButton.setOnClickListener(l -> deleteChannel(channelRef, channel, position));
  }

  public void toggleChannelStatus(final DocumentReference channelRef, final FirestoreChannel channel, MyViewHolder viewHolder)
  {
    channelRef.get()
    .onSuccessTask(snap ->
    {
      if (snap == null || !snap.exists()) {
        viewHolder.radioButton.setChecked(true);
        return mFirestore.addChannelToUserTask(channel);
      } else {
        viewHolder.radioButton.setChecked(false);
        return mFirestore.removeChannelFromUserTask(snap.getReference());
      }
    })
    .addOnSuccessListener(t ->
    {
      showToast("Channel removed");
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
