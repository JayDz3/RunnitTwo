package com.idesign.runnit.Items;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class ActiveUser {
  private String _pushId;

  public ActiveUser() {}

  public ActiveUser(String _pushId) {
    this._pushId = _pushId;
  }

  public String get_pushId() {
    return _pushId;
  }
}
