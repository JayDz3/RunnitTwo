package com.idesign.runnit.FirestoreTasks;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.idesign.runnit.Constants;
import com.idesign.runnit.Items.ActiveUser;
import com.idesign.runnit.Items.FirestoreChannel;
import com.idesign.runnit.Items.FirestoreOrg;

public class BaseFirestore {
  private final String IS_ADMIN = "_isAdmin";
  private final String IS_ACTIVE_ADMIN_CHANNEL = "_isActive";
  private final String ORG_CODE = "_organizationCode";
  private final String ORG__PUSHID = "_organizationPushId";

  private final String ENABLE_NOTIFICATIONS = "_sendNotification";
  private final String INSTANCE_ID = "_instanceId";
  private final String COLLECTION_CHANNELS = "Channels";
  private final String COLLECTION_ACTIVE_USERS = "ActiveUsers";
  private final String COLLECTION_USER_CHANNELS = "UserChannels";
  private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

  private final CollectionReference users = mFirestore.collection(Constants.COLLECTION_USERS);
  private final CollectionReference orgs = mFirestore.collection(Constants.COLLECTION_ORGS);

  public BaseFirestore() { }


  public CollectionReference getUsers()
  {
    return users;
  }

  public CollectionReference getOrgs()
  {
    return orgs;
  }

  /*
   *   CHANNELS
   */
  public DocumentReference getAdminChannel(String orgPushId, String channelId)
  {
    return orgs.document(orgPushId).collection(COLLECTION_CHANNELS).document(channelId);
  }

  public DocumentReference getChannelActiveUserReference(DocumentReference channelRef, String uid)
  {
    return channelRef.collection(COLLECTION_ACTIVE_USERS).document(uid);
  }

  public DocumentReference getUserChannelReference(String uid, String channelId)
  {
    return getUsers().document(uid).collection(COLLECTION_USER_CHANNELS).document(channelId);
  }

  public CollectionReference getChannelActiveUsersReference(DocumentReference channelRef)
  {
    return channelRef.collection(COLLECTION_ACTIVE_USERS);
  }

  public CollectionReference getAdminChannelsReference(String orgPushId)
  {
    return orgs.document(orgPushId).collection(COLLECTION_CHANNELS);
  }

  public Task<Void> activateAdminChannel(DocumentReference docRef)
  {
   return docRef.update(IS_ACTIVE_ADMIN_CHANNEL, true);
  }

  public Task<Void> deActivateAdminChannel(DocumentReference docRef)
  {
    return docRef.update(IS_ACTIVE_ADMIN_CHANNEL, false);
  }

  public Task<Void> deleteAdminChannel(DocumentReference documentReference)
  {
    return documentReference.delete();
  }

  public Task<Void> addChannelAdmin(DocumentReference orgRef, String orgPushId, String newChannelId)
  {
   final FirestoreChannel channel = new FirestoreChannel(newChannelId, orgPushId, newChannelId, true, false);
   return orgRef.collection(COLLECTION_CHANNELS).document(newChannelId).set(channel);
  }

  public Task<Void> addActiveUserToChannelTask(ActiveUser activeUser, FirestoreChannel channel)
  {
    return orgs.document(channel.get_orgPushId()).collection(COLLECTION_CHANNELS).document(channel.get_pushId())
    .collection(COLLECTION_ACTIVE_USERS).document(activeUser.get_pushId()).set(activeUser);
  }

  public Task<Void> removeUserFromActiveChannelTask(ActiveUser activeUser, FirestoreChannel channel)
  {
    return orgs.document(channel.get_orgPushId()).collection(COLLECTION_CHANNELS).document(channel.get_pushId())
    .collection(COLLECTION_ACTIVE_USERS).document(activeUser.get_pushId()).delete();
  }

  public Task<Void> addChannelToUserTask(FirestoreChannel channel, DocumentReference userRef)
  {
    return userRef.collection(COLLECTION_USER_CHANNELS).document(channel.get_channelId()).set(channel);
  }

  public Task<Void> removeChannelFromUserTask(DocumentReference documentReference)
  {
    return documentReference.delete();
  }
  // {End Channels] //

  /*
   * Organizations
   */
  public Task<QuerySnapshot> getAllOrganizationUsersQuery(String orgCode)
  {
    return getUsers().whereEqualTo(ORG_CODE, orgCode).get();
  }

  public Task<Void> setOrganizationCodeTask(DocumentReference docRef, String code)
  {
    WriteBatch writeBatch = mFirestore.batch();
    writeBatch.update(docRef, ORG_CODE, code);
    return writeBatch.commit();
  }

  public Task<QuerySnapshot> queryOrgForDuplicateCodeTask(String orgCode)
  {
    return mFirestore.collection(Constants.COLLECTION_ORGS).whereEqualTo(ORG_CODE, orgCode).get();
  }

  public Task<QuerySnapshot> queryOrgByCodeTask(String orgCode)
  {
    return mFirestore.collection(Constants.COLLECTION_ORGS).whereEqualTo(ORG_CODE, orgCode).limit(1).get();
  }

  public Task<DocumentSnapshot> getOrgSnapshotTask(String pushId)
  {
    return mFirestore.collection(Constants.COLLECTION_ORGS).document(pushId).get();
  }

  public Task<Void> setOrg(FirestoreOrg org)
  {
    DocumentReference docRef = getOrgs().document(org.getPushId());
    WriteBatch batch = mFirestore.batch();
    batch.set(docRef, org);
    return batch.commit();
  }
  // {End Organizations] //

  /*
   *  User values
   */
  public Task<Void> setIsAdmin(DocumentReference docRef)
  {
    WriteBatch writeBatch = mFirestore.batch();
    writeBatch.update(docRef, IS_ADMIN, true);
    return writeBatch.commit();
  }

  public Task<Void> setNotAdmin(DocumentReference documentReference)
  {
    WriteBatch writeBatch = mFirestore.batch();
    writeBatch.update(documentReference, IS_ADMIN, false);
    return writeBatch.commit();
  }

  public Task<Void> setUserOrgPushId(String _organizationPushId, String uid)
  {
    return getUsers().document(uid).update(ORG__PUSHID, _organizationPushId);
  }

  public Task<Void> updateInstanceId(DocumentReference docRef, String _instanceId)
  {
    WriteBatch batch = mFirestore.batch();
    batch.update(docRef, INSTANCE_ID, _instanceId);
    return batch.commit();
  }

  public Task<Void> toggleNotifications(DocumentReference docRef, boolean status)
  {
    return docRef.update(ENABLE_NOTIFICATIONS, status);
  }

  public Task<Void> clockIn(DocumentReference documentReference, String uid)
  {
    WriteBatch batch = mFirestore.batch();
    ActiveUser activeUser = new ActiveUser(uid);
    batch.set(documentReference, activeUser);
    return batch.commit();
  }

  public Task<Void> clockOut(DocumentReference docRef)
  {
    WriteBatch batch = mFirestore.batch();
    batch.delete(docRef);
    return batch.commit();
  }

  public WriteBatch batch() {
    return mFirestore.batch();
  }

  public <T> T toFirestoreObject(DocumentSnapshot documentSnapshot, Class<T> tClass)
  {
    return documentSnapshot.toObject(tClass);
  }
}
