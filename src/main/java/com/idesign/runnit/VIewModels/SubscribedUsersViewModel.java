package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.Items.User;

import java.util.ArrayList;
import java.util.List;

public class SubscribedUsersViewModel extends ViewModel {
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final MutableLiveData<List<User>> mUsers = new MutableLiveData<>();

  public MutableLiveData<List<User>> getUsers()
  {
    if (mUsers.getValue() == null)
    {
      mUsers.setValue(new ArrayList<>());
    }
    return mUsers;
  }

  public void setUsers(List<User> users)
  {
    mUsers.setValue(users);
  }

  public void setUsersFromSnapshots(QuerySnapshot snapshots)
  {
    final List<User> users = new ArrayList<>();
    for (DocumentSnapshot ds : snapshots)
    {
      final User user = mFirestore.toFirestoreObject(ds, User.class);
      users.add(user);
    }
    setUsers(users);
  }

  public void clear()
  {
    mUsers.setValue(new ArrayList<>());
  }
}
