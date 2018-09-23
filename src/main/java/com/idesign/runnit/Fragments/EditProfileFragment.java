package com.idesign.runnit.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.idesign.runnit.Constants;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.EditProfileUser;

import com.idesign.runnit.R;

import com.idesign.runnit.VIewModels.EditProfileViewModel;
import com.idesign.runnit.VIewModels.StateViewModel;

public class EditProfileFragment extends Fragment
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private EditProfileViewModel mEditProfileViewModel;
  private StateViewModel mStateViewModel;

  private EditText firstNameEditText;
  private EditText lastNameEditText;

  private Button goCredentialsButton;
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
    mEditProfileViewModel = ViewModelProviders.of(getActivity()).get(EditProfileViewModel.class);
    mStateViewModel = ViewModelProviders.of(getActivity()).get(StateViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
    firstNameEditText = rootView.findViewById(R.id.edit_profile_fragment_firstname_edit_text);
    lastNameEditText = rootView.findViewById(R.id.edit_profile_fragment_lastname_edit_text);
    submitButton = rootView.findViewById(R.id.edit_profile_fragment_submit_button);
    submitButton.setOnClickListener(l -> submit());
    goCredentialsButton = rootView.findViewById(R.id.edit_profile_fragment_go_credentials_text);
    goCredentialsButton.setOnClickListener(l -> goEditCredentials());
    progressBar = rootView.findViewById(R.id.progress_bar);
    progressBar.setVisibility(View.GONE);

    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
    if (savedInstanceState == null)
      return;
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

  public void goEditCredentials()
  {
    final String firstName = trimmedString(getFirstName());
    final String lastName = trimmedString(getLastName());
    final EditProfileUser user = mEditProfileViewModel.getEditProfileUser().getValue();

    if (!firstName.equals("") && user != null)
      user.set_firstName(upperCaseFirstLetter(firstName));

    if (!lastName.equals("") && user != null)
      user.set_lastName(upperCaseFirstLetter(lastName));

    mEditProfileViewModel.setEditProfileUser(user);
    mStateViewModel.setFragmentState(Constants.STATE_EDIT_CREDENTIALS);
  }

  private Observer<EditProfileUser> userObserver()
  {
    return user ->
    {
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
    final String currentFirstName = trimmedString(mEditProfileViewModel.getEditProfileUser().getValue().get_firstName());
    final String currentLastName = trimmedString(mEditProfileViewModel.getEditProfileUser().getValue().get_lastName());
    final String newFirstName = trimmedString(getFirstName());
    final String newLastName = trimmedString(getLastName());

    if (!TextUtils.isEmpty(newFirstName) && !TextUtils.isEmpty(newLastName) && newFirstName.equalsIgnoreCase(currentFirstName) && newLastName.equalsIgnoreCase(currentLastName))
    {
      firstNameEditText.setText(upperCaseFirstLetter(newFirstName));
      lastNameEditText.setText(upperCaseFirstLetter(newLastName));
      return;
    }

    final WriteBatch batch = mFirestore.batch();
    final String uid = mAuth.user().getUid();
    final DocumentReference userRef = mFirestore.getUsers().document(uid);

    if (isEmpty(firstNameEditText))
      firstNameEditText.setError("No empty fields");

    if (isEmpty(lastNameEditText))
      lastNameEditText.setError("No empty fields");

    if (isEmpty(firstNameEditText) || isEmpty(lastNameEditText))
      return;

    if (!newFirstName.equals(currentFirstName))
    {
      final String uppercaseFirstname = upperCaseFirstLetter(newFirstName);
      mFirestore.updateUserDetail(userRef, KEY_FIRSTNAME, uppercaseFirstname, batch);
    }

    if (!newLastName.equals(currentLastName))
    {
      final String uppercaseLastname = upperCaseFirstLetter(newLastName);
      mFirestore.updateUserDetail(userRef, KEY_LASTNAME, uppercaseLastname, batch);
    }

    progressBar.setVisibility(View.VISIBLE);
    disableButtons();
    batch.commit()
    .addOnSuccessListener(l ->
    {
      progressBar.setVisibility(View.GONE);

      final String upperFirst = upperCaseFirstLetter(newFirstName);
      final String upperlast = upperCaseFirstLetter(newLastName);
      final String email = mEditProfileViewModel.getEditProfileUser().getValue().get_email();
      final String newEmail = mEditProfileViewModel.getEditProfileUser().getValue().get_newEmail();
      final String password = mEditProfileViewModel.getEditProfileUser().getValue().get_password();

      final EditProfileUser user = new EditProfileUser(upperFirst, upperlast, email, newEmail, password);
      mEditProfileViewModel.setEditProfileUser(user);

      showToast("Updated!");
      enableButtons();
    })
    .addOnFailureListener(e ->
    {
      progressBar.setVisibility(View.GONE);
      showToast(e.getMessage());
      enableButtons();
    });
  }

  public void enableButtons()
  {
    submitButton.setClickable(true);
    submitButton.setEnabled(true);
    goCredentialsButton.setClickable(true);
    goCredentialsButton.setEnabled(true);
  }

  public void disableButtons()
  {
    submitButton.setClickable(false);
    submitButton.setEnabled(false);
    goCredentialsButton.setClickable(false);
    goCredentialsButton.setEnabled(false);
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
    final String firstName = trimmedString(getFirstName());
    final String lastName = trimmedString(getLastName());
    final EditProfileUser user = mEditProfileViewModel.getEditProfileUser().getValue();

    if (!firstName.equals("") && user != null)
      user.set_firstName(upperCaseFirstLetter(firstName));

    if (!lastName.equals("") && user != null)
      user.set_lastName(upperCaseFirstLetter(lastName));

    mEditProfileViewModel.setEditProfileUser(user);
    mEditProfileViewModel.getEditProfileUser().removeObserver(userObserver());
  }


  @Override
  public void onResume()
  {
    super.onResume();
    mEditProfileViewModel.getEditProfileUser().observe(this, userObserver());
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState)
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
    if (source == null || source.equals(""))
      return "";

    if (source.length() == 1)
      return source.toUpperCase();

    return source.substring(0, 1).toUpperCase() + source.substring(1);
  }

}
