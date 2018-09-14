package com.idesign.runnit.Fragments;

import android.content.Context;

import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;

import com.idesign.runnit.R;
import com.idesign.runnit.Items.User;

public class HomeFragment extends Fragment
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private Button toggleButton;
  private Button reminderButton;

  private FirebaseAuth.AuthStateListener authStateListener;
  private ListenerRegistration userListener;

  private Snackbar snackbar;
  private ConstraintLayout homeLayout;

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

    reminderButton = view.findViewById(R.id.home_fragment_reminder_button);
    homeLayout = view.findViewById(R.id.home_fragment_signup_constraint_layout);

    reminderButton.setOnClickListener(l -> getReminder());
  }

  public void getReminder()
  {
    final String uid = mAuth.user().getUid();
    mFirestore.getUsers().document(uid).get()
    .addOnSuccessListener(snapshot -> {
      final User user = mFirestore.toFirestoreObject(snapshot, User.class);
      final String orgCode = user.get_organizationCode();

      if (orgCode != null && !orgCode.equals("")) {
        showSnackbar(orgCode);

      } else {
        showToast("No code has been assigned to this account");
      }
    })
    .addOnFailureListener(e -> showToast(e.getMessage()));
  }

  public void toggleAdmin()
  {
    if (mAuth.user() == null)
    {
      showToast("Not logged in");
      return;
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
        reminderButton.setVisibility(View.GONE);
        return mFirestore.setNotAdmin(docRef);

      } else {
        toggleButton.setText(isAdminString);
        reminderButton.setVisibility(View.VISIBLE);
        return mFirestore.setIsAdmin(docRef);
      }
    })
    .addOnSuccessListener(l -> Log.d("HOME FRAGMENT", "USER IS NOW ADMIN"))
    .addOnFailureListener(e -> showToast("error setting as admin: " + e.getMessage()));
  }

  public void addAuthStateListener()
  {
    if (!mAuth.doesHaveListener())
    {
      authStateListener = firebaseAuth ->
      {
        if (mAuth.user() != null) {
          final String uid = mAuth.user().getUid();
          final DocumentReference userRef = mFirestore.getUsers().document(uid);
          toggleButton.setVisibility(View.VISIBLE);
          addUserListener(userRef);

        } else {
          removeUserListener();
          toggleButton.setVisibility(View.GONE);
          reminderButton.setVisibility(View.GONE);
        }
      };

      mAuth.setHasListener(true);
      mAuth.setAuthListener(authStateListener);
    }
  }

  public void addUserListener(DocumentReference userRef)
  {
    if (userListener == null)
    {
      userListener = userRef.addSnapshotListener((snapshot, e) ->
      {
        final User user = mFirestore.toFirestoreObject(snapshot, User.class);
        final boolean isAdmin = user.get_isAdmin();
        toggleButtonVisibility(isAdmin);
      });
    }
  }

  public void toggleButtonVisibility(boolean isAdmin)
  {
    if (isAdmin) {
      reminderButton.setVisibility(View.VISIBLE);

    } else {
      reminderButton.setVisibility(View.GONE);
    }
  }

  public void removeAuthStateListener()
  {
    if (mAuth.doesHaveListener())
    {
      mAuth.removeAuthListener(authStateListener);
      mAuth.setHasListener(false);
    }
  }

  public void removeUserListener()
  {
    if (userListener != null)
    {
      userListener.remove();
      userListener = null;
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
    toggleButton.setVisibility(View.GONE);
    reminderButton.setVisibility(View.GONE);
    addAuthStateListener();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    removeAuthStateListener();
    removeUserListener();
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
  }

  public void showSnackbar(String message)
  {
    snackbar = Snackbar.make(homeLayout, message, Snackbar.LENGTH_INDEFINITE).setAction(R.string.dismiss, l ->
    {
      snackbar.dismiss();
      toggleButton.setVisibility(View.VISIBLE);
      reminderButton.setVisibility(View.VISIBLE);
    });
    toggleButton.setVisibility(View.GONE);
    reminderButton.setVisibility(View.GONE);
    snackbar.show();
  }

  public void showToast(CharSequence message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }
}
