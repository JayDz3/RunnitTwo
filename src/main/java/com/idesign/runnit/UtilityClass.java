package com.idesign.runnit;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.idesign.runnit.Fragments.EditCredentialsFragment;
import com.idesign.runnit.Fragments.EditProfileFragment;
import com.idesign.runnit.VIewModels.AppUserViewModel;
import com.idesign.runnit.VIewModels.EditProfileViewModel;

import java.util.Objects;

public class UtilityClass
{

  public UtilityClass() {}

  /*===========*
   *   LOGGER  *
   *===========*/
  public void logMessage(String message)
  {
    Log.d("MAIN ACTIVITY: ", "message: " + message);
  }
  // End Logger //

  /*==============================*
   *  Observe View Models         *
   *  @ if not already observed   *
   *==============================*/
  public <T>void observeViewModel(LifecycleOwner owner, LiveData<T> liveData, Observer<T> observer)
  {
    if (!liveData.hasActiveObservers())
      liveData.observe(owner, observer);
  }
  // End Observe View Models //

  /*==================*
   *  Text Utilities  *
   *==================*/
  public String trimString(String source)
  {
    return source.trim();
  }

  public boolean isEmpty(String source)
  {
    return TextUtils.isEmpty(source);
  }

  public boolean isEmpty(EditText editText)
  {
    return TextUtils.isEmpty(editText.getText());
  }

  public String upperCaseFirstLetter(String source)
  {
    if (source.equals(""))
      return "";
    if (source.length() == 1)
      return source.substring(0, 1).toUpperCase();

    return source.substring(0, 1).toUpperCase() + source.substring(1);
  }

  public String lowercaseString(String source)
  {
    return source.toLowerCase();
  }
  // End Text Utilities //

  /*=============================================*
   *  Check For Profile and Credential Changes   *
   *=============================================*/
  public boolean profileChangedOnGoCredentials(EditProfileFragment mEditProfileFragment, AppUserViewModel mAppUserViewModel)
  {
    final String editedFirst = trimString(mEditProfileFragment.getFirstName());
    final String editedLast = trimString(mEditProfileFragment.getLastName());
    final String currentFirstName = trimString(Objects.requireNonNull(mAppUserViewModel.getmUser().getValue()).get_firstName());
    final String currentLastName = trimString(mAppUserViewModel.getmUser().getValue().get_lastName());
    return !editedFirst.equals(currentFirstName) && !isEmpty(editedFirst) || !editedLast.equals(currentLastName) && !isEmpty(editedLast);
  }

  public boolean profileChangedOnClick(EditProfileFragment mEditProfileFragment, AppUserViewModel mAppUserViewModel)
  {
    final String editedFirst = trimString(mEditProfileFragment.getFirstName());
    final String editedLast = trimString(mEditProfileFragment.getLastName());
    final String currentFirstName = trimString(Objects.requireNonNull(mAppUserViewModel.getmUser().getValue()).get_firstName());
    final String currentLastName = trimString(mAppUserViewModel.getmUser().getValue().get_lastName());
    return !editedFirst.equals(currentFirstName) || !editedLast.equals(currentLastName);
  }

  public boolean credentialsChangedOnGoProfile(EditCredentialsFragment mEditCredentialsFragment, AppUserViewModel mAppUserViewModel)
  {
    final String editEmail = trimString(mEditCredentialsFragment.getNewEmail());
    final String email = trimString(Objects.requireNonNull(mAppUserViewModel.getmUser().getValue()).get_email());
    final String currentEmail = trimString(mEditCredentialsFragment.getCurrentEmail());
    final String password = mEditCredentialsFragment.getPassword();
    return !editEmail.equals("") && !editEmail.equalsIgnoreCase(email) || !isEmpty(currentEmail) || !isEmpty(password);
  }

  public boolean credentialsChangedOnToolbarClick(EditCredentialsFragment mEditCredentialsFragment, AppUserViewModel mAppUserViewModel)
  {
    final String editCurrentEmail = trimString(mEditCredentialsFragment.getCurrentEmail());
    final String editEmail = trimString(mEditCredentialsFragment.getNewEmail());
    final String email = trimString(Objects.requireNonNull(mAppUserViewModel.getmUser().getValue()).get_email());
    final String password = mEditCredentialsFragment.getPassword().trim();
    return !isEmpty(editCurrentEmail) || !isEmpty(editEmail) || !isEmpty(password) || !isEmpty(editCurrentEmail) && !editCurrentEmail.equalsIgnoreCase(email) || !isEmpty(editEmail) && !editEmail.equals(email);
  }
  // END CHECK PROFILE AND CREDENTIAL CHANGES //

  /*=====================================================================*
   *  leaving / entering EditProfile and EditCredential Fragments   *
   *=====================================================================*/
  public void clearProfile(EditProfileViewModel mEditProfileViewModel)
  {
    Objects.requireNonNull(mEditProfileViewModel.getEditProfileUser().getValue()).set_firstName("");
    mEditProfileViewModel.getEditProfileUser().getValue().set_lastName("");
    mEditProfileViewModel.getEditProfileUser().getValue().set_email("");
    mEditProfileViewModel.getEditProfileUser().getValue().set_newEmail("");
    mEditProfileViewModel.getEditProfileUser().getValue().set_password("");
    mEditProfileViewModel.update();
  }

  public void clearCredentials(EditProfileViewModel mEditProfileViewModel)
  {
    Objects.requireNonNull(mEditProfileViewModel.getEditProfileUser().getValue()).set_email("");
    mEditProfileViewModel.getEditProfileUser().getValue().set_newEmail("");
    mEditProfileViewModel.getEditProfileUser().getValue().set_password("");
    mEditProfileViewModel.update();
  }

  public void resetProfileName(EditProfileViewModel mEditProfileViewModel, AppUserViewModel mAppUserViewModel)
  {
    final String firstname = Objects.requireNonNull(mAppUserViewModel.getmUser().getValue()).get_firstName();
    final String lastname = mAppUserViewModel.getmUser().getValue().get_lastName();
    Objects.requireNonNull(mEditProfileViewModel.getEditProfileUser().getValue()).set_firstName(firstname);
    mEditProfileViewModel.getEditProfileUser().getValue().set_lastName(lastname);
    mEditProfileViewModel.update();
  }
  // End leaving / entering profile fragments //

  /*=======================================================================================================*
   *  Update profile ViewModel when going back to HomeFragment from edit profile and credential fragments  *
   *=======================================================================================================*/
  public void updateUserProfileViewModel(EditProfileViewModel mEditProfileViewModel, AppUserViewModel mAppUserViewModel)
  {
    final String firstname = Objects.requireNonNull(mAppUserViewModel.getmUser().getValue()).get_firstName();
    final String lastname = mAppUserViewModel.getmUser().getValue().get_lastName();

    final String editFirstName = Objects.requireNonNull(mEditProfileViewModel.getEditProfileUser().getValue()).get_firstName();
    final String editLastName = mEditProfileViewModel.getEditProfileUser().getValue().get_lastName();

    if (!editFirstName.equals("")) {
      mEditProfileViewModel.getEditProfileUser().getValue().set_firstName(editFirstName);
    } else {
      mEditProfileViewModel.getEditProfileUser().getValue().set_firstName(firstname);
    }

    if (!editLastName.equals("")) {
      mEditProfileViewModel.getEditProfileUser().getValue().set_lastName(editLastName);
    } else {
      mEditProfileViewModel.getEditProfileUser().getValue().set_lastName(lastname);
    }
    mEditProfileViewModel.update();
  }
  // End Update Profile View Model //
}
