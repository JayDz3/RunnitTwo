package com.idesign.runnit.Items;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class FirestoreOrg {

  private String pushId;
  private String orgName;
  private String _organizationCode;

  @ServerTimestamp
  private Timestamp _timestamp;

  public FirestoreOrg() {}

  public FirestoreOrg(String pushId, String orgName, String _organizationCode) {
    this.pushId = pushId;
    this.orgName = orgName;
    this._organizationCode = _organizationCode;
  }

  public void setPushId(String pushId) {
    this.pushId = pushId;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public void set_organizationCode(String _organizationCode) {
    this._organizationCode = _organizationCode;
  }


  public String getPushId() {
    return pushId;
  }

  public String getOrgName() {
    return orgName;
  }

  public String get_organizationCode() {
    return _organizationCode;
  }

  public Timestamp get_timestamp() {
    return _timestamp;
  }

}
