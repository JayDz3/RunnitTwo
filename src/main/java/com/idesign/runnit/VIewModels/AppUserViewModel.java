package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.idesign.runnit.Items.User;

public class AppUserViewModel extends ViewModel {
  private final MutableLiveData<User> mUser = new MutableLiveData<>();

  public MutableLiveData<User> getmUser() {
    if (mUser.getValue() == null)
    {
      mUser.setValue(new User());
    }
    return mUser;
  }

  public void setmUser(User user)
  {
    mUser.setValue(user);
  }

  public void clear()
  {
    mUser.setValue(new User());
  }
}
