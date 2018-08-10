package com.idesign.runnit.Items;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class ClockedStatus {
  private String _uid;
  private final boolean _clockedIn = true;
  @ServerTimestamp
  private Timestamp _timestamp;

  public ClockedStatus() {}
  public ClockedStatus(String _uid) {
    this._uid = _uid;
  }

  public String get_uid() {
    return _uid;
  }

  public boolean is_clockedIn() {
    return _clockedIn;
  }

  public Timestamp get_timestamp() {
    return _timestamp;
  }
}
