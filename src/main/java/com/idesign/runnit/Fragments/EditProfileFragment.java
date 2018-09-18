package com.idesign.runnit.Fragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.R;
import com.idesign.runnit.VIewModels.AppUserViewModel;

public class EditProfileFragment extends Fragment {

  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private AppUserViewModel mUserViewModel;

  private EditText firstNameEditText;
  private EditText lastNameEditText;

  private TextView goCredentials;

  private Button submitButton;
  private ProgressBar progressBar;

  private boolean inProgress = false;
  private final String KEY_IN_PROGRESS = "in_progress";

  private final String KEY_FIRSTNAME = "_firstName";
  private final String KEY_LASTNAME = "_lastName";

  public EditProfileFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mUserViewModel = ViewModelProviders.of(getActivity()).get(AppUserViewModel.class);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
    firstNameEditText = rootView.findViewById(R.id.edit_profile_fragment_firstname_edit_text);
    lastNameEditText = rootView.findViewById(R.id.edit_profile_fragment_lastname_edit_text);
    submitButton = rootView.findViewById(R.id.edit_profile_fragment_submit_button);
    submitButton.setOnClickListener(l -> submit());
    goCredentials = rootView.findViewById(R.id.edit_profile_fragment_go_credentials_text);
    progressBar = rootView.findViewById(R.id.progress_bar);
    progressBar.setVisibility(View.GONE);
    return rootView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState)
  {
    if (savedInstanceState != null)
    {
      if (savedInstanceState.keySet().contains(KEY_IN_PROGRESS))
      {
        inProgress = savedInstanceState.getBoolean(KEY_IN_PROGRESS);

        if (inProgress) {
          progressBar.setVisibility(View.VISIBLE);

        } else {
          progressBar.setVisibility(View.GONE);
        }

      }
    }
  }

  private Observer<User> userObserver()
  {
    return user -> {
      if (user != null)
      {
        final String firstname = trimmedString(user.get_firstName());
        final String lastname = trimmedString(user.get_lastName());
        final String upperCaseFirstname = upperCaseFirstLetter(firstname);
        final String upperCaseLastname = upperCaseFirstLetter(lastname);
        firstNameEditText.setText(upperCaseFirstname);
        lastNameEditText.setText(upperCaseLastname);
      }
    };
  }

  public boolean isEmpty(EditText editText)
  {
    return TextUtils.isEmpty(editText.getText());
  }

  public void submit()
  {
    final String currentFirstName = trimmedString(mUserViewModel.getmUser().getValue().get_firstName());
    final String currentLastName = trimmedString(mUserViewModel.getmUser().getValue().get_lastName());
    final String newFirstName = getFirstName();
    final String newLastName = getLastName();
    final WriteBatch batch = mFirestore.batch();
    final String uid = mAuth.user().getUid();
    final DocumentReference userRef = mFirestore.getUsers().document(uid);
    if (isEmpty(firstNameEditText))
    {
      firstNameEditText.setError("No empty fields");
    }
    if (isEmpty(lastNameEditText))
    {
      lastNameEditText.setError("No empty fields");
    }
    if (isEmpty(firstNameEditText) || isEmpty(lastNameEditText))
    {
      return;
    }
    if (!newFirstName.equals(currentFirstName))
    {
      mFirestore.updateUserDetail(userRef, KEY_FIRSTNAME, newFirstName, batch);
    }
    if (!newLastName.equals(currentLastName))
    {
      mFirestore.updateUserDetail(userRef, KEY_LASTNAME, newLastName, batch);
    }
    progressBar.setVisibility(View.VISIBLE);
    disableButtons();
    batch.commit()
    .addOnSuccessListener(l -> {
      progressBar.setVisibility(View.GONE);
      mUserViewModel.getmUser().getValue().set_firstName(newFirstName);
      mUserViewModel.getmUser().getValue().set_lastName(newLastName);
      showToast("Updated!");
      enableButtons();
    })
    .addOnFailureListener(e -> {
      progressBar.setVisibility(View.GONE);
      showToast(e.getMessage());
      enableButtons();
    });
  }

  public void enableButtons()
  {
    submitButton.setClickable(true);
    submitButton.setEnabled(true);
    goCredentials.setClickable(true);
    goCredentials.setEnabled(true);
  }

  public void disableButtons()
  {
    submitButton.setClickable(false);
    submitButton.setEnabled(false);
    goCredentials.setClickable(false);
    goCredentials.setEnabled(false);
  }

  public String getFirstName()
  {
    return firstNameEditText.getText().toString();
  }

  public String getLastName()
  {
    return lastNameEditText.getText().toString();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    final String firstName = getFirstName();
    final String lastName = getLastName();
    if (!firstName.equals(""))
      mUserViewModel.getmUser().getValue().set_firstName(firstName);
    if (!lastName.equals(""))
      mUserViewModel.getmUser().getValue().set_lastName(getLastName());

    mUserViewModel.getmUser().removeObserver(userObserver());
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mUserViewModel.getmUser().observe(this, userObserver());
  }

  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putBoolean(KEY_IN_PROGRESS, inProgress);
  }

  public void showToast(String message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  public String trimmedString(String source)
  {
    return source.trim();
  }

  public String upperCaseFirstLetter(String source)
  {
    return source.substring(0, 1).toUpperCase() + source.substring(1);
  }

}
