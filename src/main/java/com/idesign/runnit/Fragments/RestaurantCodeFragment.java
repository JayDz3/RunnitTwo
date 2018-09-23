package com.idesign.runnit.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import com.idesign.runnit.Items.OrganizationObject;
import com.idesign.runnit.R;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.VIewModels.OrganizationObjectViewModel;
import com.idesign.runnit.VIewModels.StateViewModel;
import com.idesign.runnit.VIewModels.UserViewModel;

public class RestaurantCodeFragment extends Fragment
{
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final MyAuth mAuth = new MyAuth();

  private EditText editRestaurantCode;
  private EditText editRestaurantCodeTwo;
  private EditText editTextOrganizationName;
  private TextView restaurantView;
  private Button submitButton;

  private UserViewModel mUserViewModel;
  private StateViewModel mStateViewModel;
  private OrganizationObjectViewModel mOrganizationObjectViewModel;

  private ListenerRegistration registration;

  public RestaurantCodeFragment() {}

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
    mStateViewModel = ViewModelProviders.of(getActivity()).get(StateViewModel.class);
    mOrganizationObjectViewModel = ViewModelProviders.of(getActivity()).get(OrganizationObjectViewModel.class);
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
    editRestaurantCode = view.findViewById(R.id.restaurant_code_code);
    editRestaurantCodeTwo = view.findViewById(R.id.restaurant_code_code_two);
    restaurantView = view.findViewById(R.id.restaurant_code_text_view);
    editTextOrganizationName = view.findViewById(R.id.organization_code_org_name);
    submitButton = view.findViewById(R.id.restaurant_code_submit_button);
    submitButton.setOnClickListener(l -> submit());
  }

  /* private Observer<User> userObserver()
  {
    return user ->
    {
      if (user == null)
        return;

      final String code = user.get_organizationCode();
      final String orgName = user.get_organizationName();
      editRestaurantCode.setText(code);
      editTextOrganizationName.setText(orgName);
    };
  } */

  private Observer<OrganizationObject> organizationObjectObserver()
  {
    return organizationObject ->
    {
      if (organizationObject == null)
        return;

      final String codeOne = organizationObject.get_orgCodeOne();
      final String codeTwo = organizationObject.get_orgCodeTwo();
      final String orgName = organizationObject.get_orgName();

      showToast(codeOne);
      editRestaurantCode.setText(codeOne);
      editRestaurantCodeTwo.setText(codeTwo);
      editTextOrganizationName.setText(orgName);
    };
  }

  public void observeUser(DocumentReference userRef)
  {
    if (registration != null)
      return;

    registration = userRef.addSnapshotListener(MetadataChanges.INCLUDE, (documentSnapshot, e) ->
    {
      if (e != null)
      {
        showToast("Error: " + e.getMessage());
        return;
      }

      if (documentSnapshot == null)
        return;

      final User user = mFirestore.toFirestoreObject(documentSnapshot, User.class);

      if (user.get_isAdmin()) {
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
      editRestaurantCode.setError("Field is required");

    if (isEmptyField(editRestaurantCodeTwo))
      editRestaurantCodeTwo.setError("Field is required");

    if (isEmptyField(editTextOrganizationName))
      editTextOrganizationName.setError("Field is required");

    if (isEmptyField(editRestaurantCode) || isEmptyField(editRestaurantCodeTwo) || isEmptyField(editTextOrganizationName))
    {
      showToast("Can not submit empty values");
      return;
    }
    final String uid = mAuth.user().getUid();
    final String text = trimmedString(getCodeOne());
    final String textTwo = trimmedString(getCodeTwo());
    final String name = trimmedString(getOrganizationName());
    final DocumentReference documentReference = mFirestore.getUsers().document(uid);

    if (!text.equals(textTwo))
    {
      editRestaurantCode.setError("Codes do not match");
      editRestaurantCodeTwo.setError("Codes do not match");
      return;
    }
    disableButton();
    mUserViewModel.setOrganizationCode(text);
    mUserViewModel.setOrganizationName(name);

    mFirestore.setOrganizationCodeTask(documentReference, text)
    .onSuccessTask(ignore -> mFirestore.queryOrgByCodeTask(text, name))
    .onSuccessTask(orgSnapshots ->
    {
      if (orgSnapshots == null)
        throw new RuntimeException("There was a problem accessing this organization");

      if (orgSnapshots.getDocuments().size() == 0)
        throw new RuntimeException("No organizations match the information entered...");

      final DocumentSnapshot docSnap = orgSnapshots.getDocuments().get(0);
      final FirestoreOrg org = mFirestore.toFirestoreObject(docSnap, FirestoreOrg.class);
      final String orgPushid = org.getPushId();
      return mFirestore.setUserOrgPushId(orgPushid, uid);
    })
    .onSuccessTask(ignore -> mFirestore.setUserOrgName(name, uid))
    .addOnSuccessListener(ignore ->
    {
      mUserViewModel.clear();
      editRestaurantCode.setText("");
      editRestaurantCodeTwo.setText("");
      editTextOrganizationName.setText("");
      mOrganizationObjectViewModel.clear();
      clearErrors();
      mStateViewModel.setFragmentState(Constants.STATE_HOME);
      showToast("Success");
      enableButton();
    })
    .addOnFailureListener(e ->
    {
      showToast("error setting your code: " + e.getMessage());
      enableButton();
    });
  }

  public void clearErrors()
  {
    editRestaurantCode.setError(null);
    editRestaurantCodeTwo.setError(null);
    editTextOrganizationName.setError(null);
  }

  public String getCodeOne()
  {
    return editRestaurantCode.getText().toString();
  }

  public String getCodeTwo()
  {
    return editRestaurantCodeTwo.getText().toString();
  }

  public String getOrganizationName()
  {
    return editTextOrganizationName.getText().toString();
  }

  public void enableButton()
  {
    submitButton.setClickable(true);
    submitButton.setEnabled(true);
  }

  public void disableButton()
  {
    submitButton.setClickable(false);
    submitButton.setEnabled(false);
  }

  public boolean isEmptyField(EditText editText)
  {
    return TextUtils.isEmpty(editText.getText().toString());
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mOrganizationObjectViewModel.getOrganizationObject().observe(this, organizationObjectObserver());
    final String userPushId = mUserViewModel.getPushId();
    if (userPushId != null)
    {
      final DocumentReference userRef = mFirestore.getUsers().document(userPushId);
      observeUser(userRef);
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
    final String codeOne = getCodeOne();
    final String codeTwo = getCodeTwo();
    final String name = getOrganizationName();
    mOrganizationObjectViewModel.getOrganizationObject().getValue().set_orgCodeOne(codeOne);
    mOrganizationObjectViewModel.getOrganizationObject().getValue().set_orgCodeTwo(codeTwo);
    mOrganizationObjectViewModel.getOrganizationObject().getValue().set_orgName(name);

    mOrganizationObjectViewModel.getOrganizationObject().removeObserver(organizationObjectObserver());
    if (registration != null)
      registration.remove();
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


  public String trimmedString(String source)
  {
    return source.trim();
  }

  public void showToast(String message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }
}