package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.Items.FirestoreChannel;

import java.util.ArrayList;
import java.util.List;

public class AdminChannelViewModel extends ViewModel
{
  private final MutableLiveData<List<FirestoreChannel>> channels = new MutableLiveData<>();
  private final BaseFirestore mFirestore = new BaseFirestore();

  public MutableLiveData<List<FirestoreChannel>> getChannels()
  {
    if (channels.getValue() == null)
      channels.setValue(new ArrayList<>());
    return channels;
  }

  public void setChannelsFromSnapshot(QuerySnapshot channelSnapshot)
  {
    final List<FirestoreChannel> channels = new ArrayList<>();
    for (DocumentSnapshot ds : channelSnapshot.getDocuments())
    {
      final FirestoreChannel channel = mFirestore.toFirestoreObject(ds, FirestoreChannel.class);
      channels.add(channel);
    }
    setChannels(channels);
  }

  public void setChannels(List<FirestoreChannel> channels)
  {
    this.channels.setValue(channels);
  }

  public void clear()
  {
    List<FirestoreChannel> empty = new ArrayList<>();
    setChannels(empty);
  }
}
