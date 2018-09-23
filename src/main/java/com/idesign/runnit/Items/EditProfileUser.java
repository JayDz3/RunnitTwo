package com.idesign.runnit.Items;

import android.widget.ProgressBar;

public class EditProfileUser {
  private String _firstName;
  private String _lastName;
  private String _email;
  private String _newEmail;
  private String _password;
  private boolean _updateInProgress;

  public EditProfileUser(String _firstName, String _lastName, String _email, String _newEmail, String _password)
  {
    this._firstName = _firstName;
    this._lastName = _lastName;
    this._email = _email;
    this._newEmail = _newEmail;
    this._password = _password;
  }

  public void set_updateInProgress(boolean _updateInProgress)
  {
    this._updateInProgress = _updateInProgress;
  }

  public void set_firstName(String _firstName)
  {
    this._firstName = _firstName;
  }

  public void set_lastName(String _lastName)
  {
    this._lastName = _lastName;
  }

  public void set_email(String _email)
  {
    this._email = _email;
  }

  public void set_newEmail(String _newEmail)
  {
    this._newEmail = _newEmail;
  }

  public void set_password(String _password)
  {
    this._password = _password;
  }

  public String get_firstName()
  {
    return _firstName;
  }

  public String get_lastName()
  {
    return _lastName;
  }

  public String get_email()
  {
    return _email;
  }

  public String get_newEmail()
  {
    return _newEmail;
  }

  public String get_password()
  {
    return _password;
  }

  public boolean get_updateInProgress()
  {
    return _updateInProgress;
  }
}
