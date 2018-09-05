package com.idesign.runnit.Items;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class ActiveUser
{
  private String _pushId;
  private String _message;

  public ActiveUser() {}

  public ActiveUser(String _pushId, String _message)
  {
    this._pushId = _pushId;
    this._message = _message;
  }

  public String get_pushId()
  {
    return _pushId;
  }

  public String get_message()
  {
    return _message;
  }
}
