package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.idesign.runnit.Items.LoginData;

import java.util.Objects;

public class LoginDataViewModel extends ViewModel
{
  private final MutableLiveData<LoginData> loginData = new MutableLiveData<>();

  public LiveData<LoginData> getLoginData()
  {
    if (loginData.getValue() == null)
    {
      loginData.setValue(new LoginData());
    }
    return loginData;
  }

  public void setNavigationState(int state)
  {
    Objects.requireNonNull(getLoginData().getValue()).setNavigationState(state);
  }

  public void setLoginEmail(String email)
  {
    Objects.requireNonNull(getLoginData().getValue()).setEmail(email);
  }

  public void setPassword(String password)
  {
    Objects.requireNonNull(getLoginData().getValue()).setPassword(password);
  }

  public void setResetEmail(String email)
  {
    Objects.requireNonNull(getLoginData().getValue()).setResetEmail(email);
  }

  public void setLoggingIn(boolean loggingIn)
  {
    Objects.requireNonNull(getLoginData().getValue()).setLoggingIn(loggingIn);
  }

  public void updateLoginData()
  {
    loginData.setValue(getLoginData().getValue());
  }

  public void clear()
  {
    loginData.setValue(new LoginData());
  }

  public boolean getLoggingIn()
  {
    return Objects.requireNonNull(loginData.getValue()).getLoggingIn();
  }

  public int getNavigationState()
  {
    return Objects.requireNonNull(loginData.getValue()).getNavigationState();
  }
}
