package com.idesign.runnit.Items;

public class SubscribedUser {

  private String _pushId;
  private boolean _loggedIn;

  public SubscribedUser(){}

  public SubscribedUser(String _pushId, boolean _loggedIn)
  {
    this._pushId = _pushId;
    this._loggedIn = _loggedIn;
  }

  public void set_pushId(String _pushId)
  {
    this._pushId = _pushId;
  }

  public void set_loggedIn(boolean _loggedIn)
  {
    this._loggedIn = _loggedIn;
  }

  public String get_pushId()
  {
    return _pushId;
  }

  public boolean get_loggedIn()
  {
    return _loggedIn;
  }
}
