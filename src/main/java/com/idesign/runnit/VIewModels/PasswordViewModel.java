package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class PasswordViewModel extends ViewModel
{
  private final MutableLiveData<String> mPassword = new MutableLiveData<>();

  public LiveData<String> getPassword()
  {
    if (mPassword.getValue() == null)
      mPassword.setValue("");
    return mPassword;
  }

  public void setPassword(String password)
  {
    mPassword.setValue(password);
  }
}
