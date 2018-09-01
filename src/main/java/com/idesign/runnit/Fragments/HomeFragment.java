package com.idesign.runnit.Fragments;

import android.content.Context;

import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;

import com.idesign.runnit.R;
import com.idesign.runnit.Items.User;

public class HomeFragment extends Fragment
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private Button toggleButton;
  private FirebaseAuth.AuthStateListener authStateListener;

  public HomeFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
   return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
    toggleButton = view.findViewById(R.id.home_fragment_toggle_admin);
    toggleButton.setOnClickListener(l -> toggleAdmin());
  }

  public void toggleAdmin()
  {
    if (mAuth.user() == null)
    {
      showToast("Not logged in");
    }
    final String id = mAuth.user().getUid();
    final DocumentReference docRef = mFirestore.getUsers().document(id);
    docRef.get()
    .onSuccessTask(userRef ->
    {
      final User user = mFirestore.toFirestoreObject(userRef, User.class);
      final boolean isAdmin = user.get_isAdmin();
      final String notAdminString = "Not Admin";
      final String isAdminString = "Admin";

      if (isAdmin) {
        toggleButton.setText(notAdminString);
        return mFirestore.setNotAdmin(docRef);

      } else {
        toggleButton.setText(isAdminString);
        return mFirestore.setIsAdmin(docRef);
      }
    })
    .addOnSuccessListener(l -> Log.d("HOME FRAGMENT", "USER IS NOW ADMIN"))
    .addOnFailureListener(e -> showToast("error setting as admin: " + e.getMessage()));
  }

  public void setAuthStateListener()
  {
    if (!mAuth.doesHaveListener())
    {
      authStateListener = firebaseAuth -> {
        if (mAuth.user() != null) {
          toggleButton.setVisibility(View.VISIBLE);
        } else {
          toggleButton.setVisibility(View.GONE);
        }
      };
      mAuth.setHasListener(true);
      mAuth.setAuthListener(authStateListener);
    }
  }

  public void removeAuthListener()
  {
    if (mAuth.doesHaveListener())
    {
      mAuth.removeAuthListener(authStateListener);
      mAuth.setHasListener(false);
    }
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
    setAuthStateListener();
    // mUserViewModel.getUser().observe(this, getUserObserver());
  }

  @Override
  public void onPause()
  {
    super.onPause();
    removeAuthListener();
    // mUserViewModel.getUser().removeObserver(getUserObserver());
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
