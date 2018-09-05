package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.Items.SubscribedUser;

import java.util.ArrayList;
import java.util.List;

public class SubscribedUsersViewModel extends ViewModel {
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final MutableLiveData<List<SubscribedUser>> mUsers = new MutableLiveData<>();

  public MutableLiveData<List<SubscribedUser>> getUsers()
  {
    if (mUsers.getValue() == null)
    {
      mUsers.setValue(new ArrayList<>());
    }
    return mUsers;
  }

  private void setUsers(List<SubscribedUser> users)
  {
    mUsers.setValue(users);
  }


  public int setUsersFromSnapshots(QuerySnapshot snapshots)
  {
    final List<SubscribedUser> users = new ArrayList<>();
    for (DocumentSnapshot ds : snapshots)
    {
      final SubscribedUser subscribedUser = mFirestore.toFirestoreObject(ds, SubscribedUser.class);
      if (subscribedUser.get_loggedIn())
      {
        users.add(subscribedUser);
      }
    }
    setUsers(users);
    return users.size();
  }

  public void clear()
  {
    mUsers.setValue(new ArrayList<>());
  }
}
