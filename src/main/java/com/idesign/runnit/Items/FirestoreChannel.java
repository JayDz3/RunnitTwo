package com.idesign.runnit.Items;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class FirestoreChannel {
  private String _pushId;
  private String _orgPushId;
  private String _channelId;
  private boolean _isAdmin;
  private boolean _isActive;
  @ServerTimestamp
  private Timestamp _created;

  @ServerTimestamp
  private Timestamp _lastSent;

  public FirestoreChannel() {}

  public FirestoreChannel(String _pushId, String _orgPushId, String _channelId, boolean _isAdmin, boolean _isActive, Timestamp _lastSent)
  {
    this._pushId = _pushId;
    this._orgPushId = _orgPushId;
    this._channelId = _channelId;
    this._isAdmin = _isAdmin;
    this._isActive = _isActive;
  }

  public void set_pushId(String _pushId)
  {
    this._pushId = _pushId;
  }

  public void set_orgId(String _orgPushId)
  {
    this._orgPushId = _orgPushId;
  }

  public void set_isActive(boolean _isActive)
  {
    this._isActive = _isActive;
  }

  public void set_lastSent(Timestamp _lastSent)
  {
    this._lastSent = _lastSent;
  }

  public String get_pushId()
  {
    return _pushId;
  }

  public String get_orgPushId()
  {
    return _orgPushId;
  }

  public String get_channelId()
  {
    return _channelId;
  }

  public boolean is_isAdmin()
  {
    return _isAdmin;
  }

  public boolean is_isActive()
  {
    return _isActive;
  }

  public Timestamp get_created()
  {
    return _created;
  }

  public Timestamp get_lastSent() {
    return _lastSent;
  }
}
