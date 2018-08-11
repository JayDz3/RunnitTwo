
package com.idesign.runnit.Fragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;

import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;

import com.idesign.runnit.R;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.VIewModels.UserViewModel;

import java.util.Objects;


public class HomeFragment extends Fragment
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private UserViewModel mUserViewModel;

  public HomeFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mUserViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(UserViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
   return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
    /* if (mAuth.user() == null) {
      submitButton.setVisibility(View.GONE);
    } else {
      final String uid = mAuth.user().getUid();
      mFirestore.getUsers().document(uid).get()
      .addOnSuccessListener(userRef ->
      {
        User user = mFirestore.toFirestoreObject(userRef, User.class);
        if (user.get_isAdmin()) {
          submitButton.setVisibility(View.VISIBLE);
        } else {
          submitButton.setVisibility(View.GONE);
        }
      });
    } */
  }

  /*
   * must reference a local or class variable in order to avoid error being thrown
   *
   * @ error can not use same lifecycle context twice [something along those lines]
   *
   */
  private Observer<User> getUserObserver()
  {
    return user ->
    {
      if (user != null)
      {
        if (user.get_isAdmin()) {
          Log.d("Home frag", "not is admin");
        } else {
          Log.d("Home frag", "is admin");
        }
      }
    };
  }

  public void sendNotification()
  {
    /* if (mAuth.user() == null)
    {
      showToast("You are not currently logged in...");
      return;
    }
    final String uid = mAuth.user().getUid();
    mFirestore.setUserReference(uid);
    mFirestore.getUserReference()
    .onSuccessTask(userRef -> mFirestore.getOrg(uid))
    .onSuccessTask(mFirestore::getActiveUsers)
    .onSuccessTask(activeUsers ->
    {
      Task<Void> task = Tasks.forResult(null);
      if (activeUsers == null || activeUsers.getDocuments().size() == 0)
      {
        return task;
      }
      for (final DocumentSnapshot ds : activeUsers)
      {
        task = task.continueWithTask(ignored -> mFirestore.toggleNotifications(mFirestore.getUsers().document(uid), false));
      }
      return task;
    })
    .onSuccessTask(ignore -> mFirestore.getUserReference())
    .onSuccessTask(userRef -> mFirestore.getOrg(uid))
    .onSuccessTask(mFirestore::getActiveUsers)
    .onSuccessTask(activeUsers ->
    {
      Task<Void> task = Tasks.forResult(null);
      if (activeUsers == null || activeUsers.getDocuments().size() == 0)
      {
        return task;
      }
      for (final DocumentSnapshot ds : activeUsers)
      {
        task = task.continueWithTask(ignored -> mFirestore.toggleNotifications(mFirestore.getUsers().document(uid), true));
      }
      return task;
    })
    .addOnSuccessListener(finalTask -> showToast("Notifications sent!"))
    .addOnFailureListener(e -> showToast("error: " + e.getMessage())); */
  }

  @Override
  public void onAttach(Context context)
  {
    super.onAttach(context);
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mUserViewModel.getUser().observe(this, getUserObserver());
  }

  @Override
  public void onPause()
  {
    super.onPause();
    mUserViewModel.getUser().removeObserver(getUserObserver());
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
  }

  public void showToast(CharSequence message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }
}
