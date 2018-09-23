package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.idesign.runnit.Items.EditProfileUser;

public class EditProfileViewModel extends ViewModel
{
  private final MutableLiveData<EditProfileUser> editProfileUser = new MutableLiveData<>();

  public LiveData<EditProfileUser> getEditProfileUser()
  {
    if (editProfileUser.getValue() == null)
      editProfileUser.setValue(new EditProfileUser("", "", "", "", ""));
    return editProfileUser;
  }

  public void setEditProfileUser(EditProfileUser user)
  {
    this.editProfileUser.setValue(user);
  }

  public void update()
  {
    final EditProfileUser user = getEditProfileUser().getValue();
    setEditProfileUser(user);
  }
  public void clear()
  {
    EditProfileUser user = new EditProfileUser("", "", "", "", "");
    setEditProfileUser(user);
  }
}
