package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.idesign.runnit.Items.FirestoreChannel;

import java.util.ArrayList;
import java.util.List;

public class AdminChannelViewModel extends ViewModel {
  private final MutableLiveData<List<FirestoreChannel>> channels = new MutableLiveData<>();

  public MutableLiveData<List<FirestoreChannel>> getChannels() {
    if (channels.getValue() == null) {
      channels.setValue(new ArrayList<>());
    }
    return channels;
  }

  public void setChannels(List<FirestoreChannel> channels) {
    this.channels.setValue(channels);
  }
}
