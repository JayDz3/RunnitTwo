package com.idesign.runnit.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.idesign.runnit.Constants;

import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.FirestoreOrg;
import com.idesign.runnit.R;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.VIewModels.StateViewModel;
import com.idesign.runnit.VIewModels.UserViewModel;

public class RestaurantCodeFragment extends Fragment
{
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final MyAuth mAuth = new MyAuth();

  private EditText editRestaurantCode;
  private TextView restaurantView;
  private Button submitButton;

  private UserViewModel mUserViewModel;
  private StateViewModel mStateViewModel;

  private ListenerRegistration registration;
  private ConstraintLayout restaurantLayout;
  private Snackbar snackbar;

  public RestaurantCodeFragment() {}

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
    mStateViewModel = ViewModelProviders.of(getActivity()).get(StateViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.fragment_restaurant_code, container, false);
    setViewItems(rootView);
    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
  }

  public void setViewItems(View view)
  {
    restaurantLayout = view.findViewById(R.id.restaurant_code_main_layout);
    editRestaurantCode = view.findViewById(R.id.restaurant_code_code);
    restaurantView = view.findViewById(R.id.restaurant_code_text_view);
    submitButton = view.findViewById(R.id.restaurant_code_submit_button);
    submitButton.setOnClickListener(l -> submit());
  }

  public void observeUser(DocumentReference userRef)
  {
    registration = userRef.addSnapshotListener(MetadataChanges.INCLUDE, (documentSnapshot, e) ->
    {
      if (e != null)
      {
        showToast("Error: " + e.getMessage());
        return;
      }
      if (documentSnapshot == null)
      {
        return;
      }
      final User user = mFirestore.toFirestoreObject(documentSnapshot, User.class);
      mUserViewModel.setIsAdmin(user.get_isAdmin());
      if (user.get_isAdmin())
      {
        restaurantView.setText(getResources().getString(R.string.restaurant_code_text_view_admin));
      } else {
        restaurantView.setText(getResources().getString(R.string.restaurant_code_text_view));
      }
    });
  }

  public void submit()
  {
    if (mAuth.user() == null)
    {
      showToast("you have not created an account...");
      return;
    }
    if (isEmptyField(editRestaurantCode))
    {
      showSnackbar("Can not submit empty values");
      return;
    }
    final String uid = mAuth.user().getUid();
    final String text = trimmedString(editRestaurantCode.getText().toString());
    final DocumentReference documentReference = mFirestore.getUsers().document(uid);
    mUserViewModel.setOrganizationCode(text);

    mFirestore.setOrganizationCodeTask(documentReference, text)
    .onSuccessTask(ignore -> mFirestore.queryOrgByCodeTask(text))
    .onSuccessTask(orgSnapshots ->
    {
      if (orgSnapshots == null)
      {
        throw new RuntimeException("There was a problem accessing this organization");
      }
      final DocumentSnapshot docSnap = orgSnapshots.getDocuments().get(0);
      final FirestoreOrg org = mFirestore.toFirestoreObject(docSnap, FirestoreOrg.class);
      final String orgPushid = org.getPushId();
      return mFirestore.setUserOrgPushId(orgPushid, uid);
    })
    .addOnSuccessListener(ignore -> showSnackbar("Success"))
    .addOnFailureListener(e -> showSnackbar("error setting your code: " + e.getMessage()));
  }

  public boolean isEmptyField(EditText editText)
  {
    return TextUtils.isEmpty(editText.getText().toString());
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if (registration != null)
    {
      registration.remove();
    }
  }

  @Override
  public void onResume()
  {
    super.onResume();
    final String userPushId = mUserViewModel.getPushId();
    if (userPushId != null)
    {
      final DocumentReference userRef = mFirestore.getUsers().document(userPushId);
      observeUser(userRef);
    }
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
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
  public void onSaveInstanceState(@NonNull Bundle outState)
  {
    super.onSaveInstanceState(outState);
  }

  public void showSnackbar(String message)
  {
    snackbar = Snackbar.make(restaurantLayout, message, Snackbar.LENGTH_INDEFINITE).setAction(R.string.submit, l ->
    {
      snackbar.dismiss();
      if (mAuth.user() != null)
      {
        mUserViewModel.clear();
        editRestaurantCode.setText("");
      }
      mStateViewModel.setFragmentState(Constants.STATE_HOME);
    });
    snackbar.show();
  }

  public String trimmedString(String source)
  {
    return source.trim();
  }

  public void showToast(String message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  public void logMessage(String message)
  {
    Log.d("RESTAURANT FRAGMENT", "message: " + message);
  }
}