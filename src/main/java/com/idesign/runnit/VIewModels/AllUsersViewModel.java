package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.Items.User;

import java.util.ArrayList;
import java.util.List;

public class AllUsersViewModel extends ViewModel
{
  private final MutableLiveData<List<User>> users = new MutableLiveData<>();
  private final BaseFirestore mFirestore = new BaseFirestore();

  public MutableLiveData<List<User>> getUsers()
  {
    if (users.getValue() == null)
      users.setValue(new ArrayList<>());
    return users;
  }

  public void setUsers(List<User> users)
  {
    this.users.setValue(users);
  }

  public void setUsersFromQuerySnapshot(QuerySnapshot snapshot)
  {
    final List<User> allUsers = new ArrayList<>();
    for (DocumentSnapshot ds : snapshot)
    {
      final User user = mFirestore.toFirestoreObject(ds, User.class);
      allUsers.add(user);
    }
    setUsers(allUsers);
  }
}
