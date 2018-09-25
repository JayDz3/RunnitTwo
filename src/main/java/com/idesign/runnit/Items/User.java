package com.idesign.runnit.Items;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

  public class User {
    private String _pushId;
    private String _organizationCode;
    private String _organizationPushId;
    private String _organizationName;
    private String _firstName;
    private String _lastName;
    private String _email;
    private String _phoneNumber;
    private boolean _isAdmin;
    private String _instanceId;
    private boolean _loggedIn;

    @ServerTimestamp
    private Timestamp _timestamp;

    public User() {}

    public void set_pushId(String _pushId)
    {
      this._pushId = _pushId;
    }

    public void set_organizationCode(String _organizationCode)
    {
      this._organizationCode = _organizationCode;
    }

    public void set_organizationPushId(String _organizationPushId)
    {
      this._organizationPushId = _organizationPushId;
    }

    public void set_organizationName(String _organizationName)
    {
      this._organizationName = _organizationName;
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

    public void set_phoneNumber(String _phoneNumber)
    {
      this._phoneNumber = _phoneNumber;
    }

    public void set_isAdmin(boolean _isAdmin)
    {
      this._isAdmin = _isAdmin;
    }

    public void set_instanceId(String _instanceId) {
      this._instanceId = _instanceId;
    }

    public void set_loggedIn(boolean _loggedIn)
    {
      this._loggedIn = _loggedIn;
    }

    public String get_pushId()
    {
      return _pushId;
    }

    public String get_organizationCode()
    {
      return _organizationCode;
    }

    public String get_organizationPushId()
    {
      return _organizationPushId;
    }

    public String get_organizationName()
    {
      return _organizationName;
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

    public String get_phoneNumber()
    {
      return _phoneNumber;
    }

    public boolean get_isAdmin()
    {
      return _isAdmin;
    }

    public String get_instanceId()
    {
      return _instanceId;
    }

    public Timestamp get_timestamp()
    {
      return _timestamp;
    }

    public boolean get_loggedIn()
    {
      return _loggedIn;
    }
  }

