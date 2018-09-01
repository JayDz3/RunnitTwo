package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.idesign.runnit.Items.User;

import java.util.Objects;

public class UserViewModel extends ViewModel
{
  private final MutableLiveData<User> mUser = new MutableLiveData<>();

  public LiveData<User> getUser()
  {
    if (mUser.getValue() == null)
    {
      mUser.setValue(new User());
    }
    return mUser;
  }


  public void clear()
  {
    mUser.setValue(new User());
  }

  public void setUser(User user)
  {
    mUser.setValue(user);
  }


  /*
   *  GETTERS
   */
  public String getPushId()
  {
    return Objects.requireNonNull(getUser().getValue()).get_pushId();
  }

  public String getFirstName()
  {
    return Objects.requireNonNull(getUser().getValue()).get_firstName();
  }

  public String getLastName()
  {
    return Objects.requireNonNull(getUser().getValue()).get_lastName();
  }

  public String getEmail()
  {
    return Objects.requireNonNull(getUser().getValue()).get_email();
  }

  public String getPhoneNumber()
  {
    return Objects.requireNonNull(getUser().getValue()).get_phoneNumber();
  }

  public boolean getIsAdmin()
  {
    return Objects.requireNonNull(getUser().getValue()).get_isAdmin();
  }

  public boolean getLoggedIn()
  {
    return Objects.requireNonNull(getUser().getValue()).get_loggedIn();
  }

  public void setPushId(String _pushid)
  {
    Objects.requireNonNull(mUser.getValue()).set_pushId(_pushid);
  }

  public String getOrganizationCode()
  {
    return Objects.requireNonNull(getUser().getValue()).get_organizationCode();
  }

  public String getOrganizationPushId()
  {
    return Objects.requireNonNull(getUser().getValue()).get_organizationPushId();
  }

  /*
   * SETTERS
   */
  public void setFirstName(String _firstname)
  {
    Objects.requireNonNull(mUser.getValue()).set_firstName(_firstname);
  }

  public void setLastName(String _lastname)
  {
    Objects.requireNonNull(mUser.getValue()).set_lastName(_lastname);
  }

  public void setEmail(String _email)
  {
    Objects.requireNonNull(mUser.getValue()).set_email(_email);
  }

  public void setPhoneNumber(String _phonenumber)
  {
    Objects.requireNonNull(mUser.getValue()).set_phoneNumber(_phonenumber);
  }

  public void setIsAdmin(boolean isAdmin)
  {
    Objects.requireNonNull(mUser.getValue()).set_isAdmin(isAdmin);
  }

  public void setOrganizationCode(String _organizationCode)
  {
    Objects.requireNonNull(mUser.getValue()).set_organizationCode(_organizationCode);
  }

  public void setOrganizationPushId(String _organizationPushId)
  {
    Objects.requireNonNull(mUser.getValue()).set_organizationPushId(_organizationPushId);
  }

  public void setSendNotification(boolean _sendNotification)
  {
    Objects.requireNonNull(mUser.getValue()).set_sendNotification(_sendNotification);
  }

  public void setInstanceId(String id)
  {
    Objects.requireNonNull(mUser.getValue()).set_instanceId(id);
  }

  public void setLoggedIn(boolean status)
  {
    Objects.requireNonNull(mUser.getValue()).set_loggedIn(status);
  }
}
