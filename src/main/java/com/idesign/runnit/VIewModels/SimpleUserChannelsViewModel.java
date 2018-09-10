package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.Items.UserChannel;

import java.util.ArrayList;
import java.util.List;

public class SimpleUserChannelsViewModel extends ViewModel {
  private final MutableLiveData<List<UserChannel>> channels = new MutableLiveData<>();

  public MutableLiveData<List<UserChannel>> getChannels()
  {
    if (channels.getValue() == null)
    {
      channels.setValue(new ArrayList<>());
    }
    return channels;
  }

  private void setChannels(List<UserChannel> channels)
  {
    this.channels.setValue(channels);
  }

  public void setUserChannelsFromSnapshot(QuerySnapshot snapshot)
  {
    final List<UserChannel> userChannels = new ArrayList<>();
    for (DocumentSnapshot ds : snapshot.getDocuments())
    {
      final String pushId = ds.getId();
      UserChannel channel = new UserChannel(pushId);
      userChannels.add(channel);
    }
    setChannels(userChannels);
  }
}
