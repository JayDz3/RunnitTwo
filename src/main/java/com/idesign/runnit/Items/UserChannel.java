package com.idesign.runnit.Items;

public class UserChannel
{
  private String _pushId;

  public UserChannel(){}

  public UserChannel(String _pushId)
  {
    this._pushId = _pushId;
  }

  public void set_pushId(String _pushId)
  {
    this._pushId = _pushId;
  }

  public String get_pushId()
  {
    return _pushId;
  }
}
