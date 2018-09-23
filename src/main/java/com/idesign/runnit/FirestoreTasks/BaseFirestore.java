package com.idesign.runnit.FirestoreTasks;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
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
import com.idesign.runnit.Items.SubscribedUser;
import com.idesign.runnit.Items.UserChannel;
import com.idesign.runnit.VIewModels.UserChannelsViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BaseFirestore
{
  private final String IS_ADMIN = "_isAdmin";
  private final String ORG_CODE = "_organizationCode";
  private final String ORG__PUSHID = "_organizationPushId";
  private final String LOGGED_IN = "_loggedIn";
  private final String LAST_SENT = "_lastSent";
  private final String INSTANCE_ID = "_instanceId";
  private final String ORG_NAME_USER = "_orgName";
  private final String ORG_NAME_ORG = "orgName";

  private final String COLLECTION_CHANNELS = "Channels";
  private final String COLLECTION_ACTIVE_USERS = "ActiveUsers";
  private final String COLLECTION_SUBSCRIBED_USERS = "SubscribedUsers";

  private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

  private final CollectionReference users = mFirestore.collection(Constants.COLLECTION_USERS);
  private final CollectionReference orgs = mFirestore.collection(Constants.COLLECTION_ORGS);

  public BaseFirestore() { }

  public CollectionReference getUsers()
  {
    return users;
  }

  private CollectionReference getOrgs()
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

  public CollectionReference getChannelActiveUsersReference(DocumentReference channelRef)
  {
    return channelRef.collection(COLLECTION_ACTIVE_USERS);
  }

  public CollectionReference getAdminChannelsReference(String orgPushId)
  {
    return orgs.document(orgPushId).collection(COLLECTION_CHANNELS);
  }

  public Task<Void> deleteAdminChannel(DocumentReference documentReference)
  {
    return documentReference.delete();
  }

  /*
   *  Error: still setting _lastSent to current timestamp
   */
  public Task<Void> addChannelAdmin(DocumentReference orgRef, String orgPushId, String newChannelId)
  {
   final FirestoreChannel channel = new FirestoreChannel(newChannelId, orgPushId, newChannelId, true, false, null);
   return orgRef.collection(COLLECTION_CHANNELS).document(newChannelId).set(channel);
  }

  public Task<Void> addUserChannel(String uid, String channelId)
  {
    final UserChannel userChannel = new UserChannel(channelId);
    return getUsers().document(uid).collection(COLLECTION_CHANNELS).document(channelId).set(userChannel);
  }

  public Task<Void> deleteUserChannel(String uid, String channelId)
  {
    return getUsers().document(uid).collection(COLLECTION_CHANNELS).document(channelId).delete();
  }

  public CollectionReference getUserChannels(String uid)
  {
    return getUsers().document(uid).collection(COLLECTION_CHANNELS);
  }

  public Task<Void> updateLastSent(DocumentReference channelRef)
  {
    WriteBatch batch = mFirestore.batch();
    Date date = new Date();
    Timestamp now = new Timestamp(date);
    batch.update(channelRef, LAST_SENT, now);
    return batch.commit();
  }

  public Task<Void> setActiveUser(final CollectionReference activeUsersReference, final String uid, final String _message)
  {
    ActiveUser activeUser = new ActiveUser(uid, _message);
    return activeUsersReference.document(uid).set(activeUser);
  }

  public DocumentReference subscribedUserReference(DocumentReference channelRef, String uid)
  {
    return channelRef.collection(COLLECTION_SUBSCRIBED_USERS).document(uid);
  }

  public CollectionReference subscribedUsersReference(DocumentReference channelRef)
  {
    return channelRef.collection(COLLECTION_SUBSCRIBED_USERS);
  }

  public Task<Void> addSubscribedUserTask(DocumentReference channelRef, String firstName, String lastName, String uid)
  {
    SubscribedUser user = new SubscribedUser(uid, firstName, lastName, true);
    return channelRef.collection(COLLECTION_SUBSCRIBED_USERS).document(uid).set(user);
  }

  public Task<Void> deleteSubscribedUserTask(DocumentReference channelRef, String uid)
  {
    return channelRef.collection(COLLECTION_SUBSCRIBED_USERS).document(uid).delete();
  }

  public Task<Void> updateSubscribedUserTask(final DocumentReference subscribedUserRef, final boolean status)
  {
    return subscribedUserRef.update(LOGGED_IN, status);
  }
  // {End Channels] //

  /*
   * Organizations
   */
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

  public Task<QuerySnapshot> queryOrgByCodeTask(String orgCode, String orgName)
  {
    return mFirestore.collection(Constants.COLLECTION_ORGS).whereEqualTo(ORG_CODE, orgCode).whereEqualTo(ORG_NAME_ORG, orgName).limit(1).get();
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

  public Task<QuerySnapshot> getOrganizationUsers(String orgPushId)
  {
    return getUsers().whereEqualTo(ORG__PUSHID, orgPushId).get();
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

  public Task<Void> setUserOrgName(String _orgName, String uid)
  {
    return getUsers().document(uid).update(ORG_NAME_USER, _orgName);
  }

  public Task<Void> setUserOrganizationPushIdEmpty(String _organizationPushId, String uid)
  {
    return getUsers().document(uid).update(ORG__PUSHID, "");
  }

  public Task<Void> setUserOrganizationNameEmpty(String uid)
  {
    return getUsers().document(uid).update(ORG_NAME_USER, "");
  }

  public Task<Void> setUserOrganizationCodeEmpty(String uid)
  {
    return getUsers().document(uid).update(ORG_CODE, "");
  }

  public Task<Void> updateInstanceId(DocumentReference docRef, String _instanceId)
  {
    WriteBatch batch = mFirestore.batch();
    batch.update(docRef, INSTANCE_ID, _instanceId);
    return batch.commit();
  }

  /*
   *  Get WriteBatch
   */
  public WriteBatch batch() {
    return mFirestore.batch();
  }

  public <T> T toFirestoreObject(DocumentSnapshot documentSnapshot, Class<T> tClass)
  {
    return documentSnapshot.toObject(tClass);
  }

  public Task<Void> updateSubscribedUserTasks(String uid, WriteBatch batch, UserChannelsViewModel mUserChannelViewModel)
  {
    final List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
    final List<FirestoreChannel> channels = mUserChannelViewModel.getChannels().getValue();
    if (channels != null)
    {
      for (FirestoreChannel channel : channels)
      {
        final String orgPushId = channel.get_orgPushId();
        final String channelPushId = channel.get_pushId();
        final DocumentReference channelRef = getAdminChannel(orgPushId, channelPushId);
        final DocumentReference subscribedUserReference = subscribedUserReference(channelRef, uid);

        tasks.add(subscribedUserReference.get().addOnSuccessListener(ref ->
        {
          if (ref.exists())
            batch.update(ref.getReference(), LOGGED_IN, false);
        }));
      }
    }
    return Tasks.whenAll(tasks);
  }

  /*
   *  Removes user from specific orgs active users collection
   *  Main Activity : Logging out
   */
  public Task<Void> deleteActiveUserReferences(String uid, DocumentReference userRef, WriteBatch batch, UserChannelsViewModel mUserChannelViewModel)
  {
    final List<FirestoreChannel> channels = mUserChannelViewModel.getChannels().getValue();
    if (channels != null)
    {
      for (FirestoreChannel channel : channels)
      {
        final String orgPushId = channel.get_orgPushId();
        final String channelPushId = channel.get_pushId();
        final DocumentReference channelRef = getAdminChannel(orgPushId, channelPushId);
        final DocumentReference activeUserRef = getChannelActiveUsersReference(channelRef).document(uid);
        batch.delete(activeUserRef);
      }
    }
    batch.update(userRef, INSTANCE_ID, "");
    return batch.commit();
  }

  public Task<Void> deleteActiveUsersFromChannelBatch(QuerySnapshot activeUsers)
  {
    final WriteBatch batch = mFirestore.batch();

    if (activeUsers == null)
      return batch.commit();

    for (DocumentSnapshot ds : activeUsers.getDocuments())
    {
      final DocumentReference ref = ds.getReference();
      batch.delete(ref);
    }
    return batch.commit();
  }

  public Task<Void> deleteSubscribedUsersBatch(QuerySnapshot subscribedUsers, String channelId)
  {
    final WriteBatch batch = mFirestore.batch();
    final String COLLECTION_CHANNELS  = "Channels";

    if (subscribedUsers == null)
      return batch.commit();

    for (DocumentSnapshot ds : subscribedUsers.getDocuments())
    {
      final DocumentReference ref = ds.getReference();
      final DocumentReference userChannelRef = getUsers().document(ds.getId()).collection(COLLECTION_CHANNELS).document(channelId);

      batch.delete(userChannelRef);
      batch.delete(ref);
    }
    return batch.commit();
  }


  /*
   * In Delete Account Activity and All Users Adapter
   */
  public Task<QuerySnapshot> deleteAdminChannelUserReferencesReturnUserChannels(QuerySnapshot adminChannelsSnapshot,
                                                     CollectionReference userChannelsReference,
                                                     WriteBatch batch,
                                                     String uid)
  {
    {
      if (adminChannelsSnapshot != null)
      {
        for (DocumentSnapshot ds : adminChannelsSnapshot.getDocuments())
        {
          final DocumentReference activeUserRef = ds.getReference().collection(COLLECTION_ACTIVE_USERS).document(uid);
          final DocumentReference subscribedUserRef = ds.getReference().collection(COLLECTION_SUBSCRIBED_USERS).document(uid);
          batch.delete(activeUserRef);
          batch.delete(subscribedUserRef);
        }
      }
      return userChannelsReference.get();
    }
  }

  /*
   * In Delete Account Activity and All Users Adapter
   */
  public Task<Void> deleteUserChannelReferencesCommitBatch(QuerySnapshot userChannelsSnapshot, WriteBatch batch)
  {
    if (userChannelsSnapshot != null)
    {
      for (DocumentSnapshot ds : userChannelsSnapshot.getDocuments())
      {
        final DocumentReference channelRef = ds.getReference();
        batch.delete(channelRef);
      }
    }
    return batch.commit();
  }

  public void updateUserDetail(DocumentReference userReference, String key, String value, WriteBatch batch)
  {
    batch.update(userReference, key, value);
  }
}
