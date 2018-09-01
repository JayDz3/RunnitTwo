package com.idesign.runnit.Items;

public class SubscribedUser {

  private String _pushId;
  private String _firstName;
  private String _lastName;
  private boolean _loggedIn;

  public SubscribedUser(){}

  public SubscribedUser(String _pushId, String _firstName, String _lastName, boolean _loggedIn)
  {
    this._pushId = _pushId;
    this._firstName = _firstName;
    this._lastName = _lastName;
    this._loggedIn = _loggedIn;
  }

  public void set_pushId(String _pushId)
  {
    this._pushId = _pushId;
  }

  public void set_firstName(String _firstName)
  {
    this._firstName = _firstName;
  }

  public void set_lastName(String _lastName)
  {
    this._lastName = _lastName;
  }

  public void set_loggedIn(boolean _loggedIn)
  {
    this._loggedIn = _loggedIn;
  }

  public String get_pushId()
  {
    return _pushId;
  }

  public String get_firstName()
  {
    return _firstName;
  }

  public String get_lastName()
  {
    return _lastName;
  }

  public boolean get_loggedIn()
  {
    return _loggedIn;
  }
}
