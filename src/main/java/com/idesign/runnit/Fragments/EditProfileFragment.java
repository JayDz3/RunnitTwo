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

import com.idesign.runnit.UtilityClass;
import com.idesign.runnit.VIewModels.EditProfileViewModel;
import com.idesign.runnit.VIewModels.StateViewModel;

public class EditProfileFragment extends Fragment
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final UtilityClass mUtility = new UtilityClass();

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

  private String firstname;
  private String lastname;

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

    if (savedInstanceState == null)
      mUtility.observeViewModel(this, mEditProfileViewModel.getEditProfileUser(), userObserver());

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
    firstname = mUtility.trimString(getFirstName());
    lastname = mUtility.trimString(getLastName());
    final EditProfileUser user = mEditProfileViewModel.getEditProfileUser().getValue();

    if (!firstname.equals("") && user != null)
      user.set_firstName(mUtility.upperCaseFirstLetter(firstname));

    if (!lastname.equals("") && user != null)
      user.set_lastName(mUtility.upperCaseFirstLetter(lastname));

    mEditProfileViewModel.setEditProfileUser(user);
    mStateViewModel.setFragmentState(Constants.STATE_EDIT_CREDENTIALS);
  }

  private Observer<EditProfileUser> userObserver()
  {
    return user ->
    {
      if (user != null)
      {
        firstname = mUtility.trimString(user.get_firstName());
        lastname = mUtility.trimString(user.get_lastName());
        final String upperCaseFirstname = mUtility.upperCaseFirstLetter(firstname);
        final String upperCaseLastname = mUtility.upperCaseFirstLetter(lastname);
        firstNameEditText.setText(upperCaseFirstname);
        lastNameEditText.setText(upperCaseLastname);
      }
    };
  }

  public void submit()
  {
    final String currentFirstName = mUtility.trimString(mEditProfileViewModel.getEditProfileUser().getValue().get_firstName());
    final String currentLastName = mUtility.trimString(mEditProfileViewModel.getEditProfileUser().getValue().get_lastName());
    final String newFirstName = mUtility.trimString(getFirstName());
    final String newLastName = mUtility.trimString(getLastName());

    if (!TextUtils.isEmpty(newFirstName) && !TextUtils.isEmpty(newLastName) && newFirstName.equalsIgnoreCase(currentFirstName) && newLastName.equalsIgnoreCase(currentLastName))
    {
      firstNameEditText.setText(mUtility.upperCaseFirstLetter(newFirstName));
      lastNameEditText.setText(mUtility.upperCaseFirstLetter(newLastName));
      return;
    }

    final WriteBatch batch = mFirestore.batch();
    final String uid = mAuth.user().getUid();
    final DocumentReference userRef = mFirestore.getUsers().document(uid);

    if (mUtility.isEmpty(firstNameEditText))
      firstNameEditText.setError("No empty fields");

    if (mUtility.isEmpty(lastNameEditText))
      lastNameEditText.setError("No empty fields");

    if (mUtility.isEmpty(firstNameEditText) || mUtility.isEmpty(lastNameEditText))
      return;

    if (!newFirstName.equals(currentFirstName))
    {
      final String uppercaseFirstname = mUtility.upperCaseFirstLetter(newFirstName);
      mFirestore.updateUserDetail(userRef, KEY_FIRSTNAME, uppercaseFirstname, batch);
    }

    if (!newLastName.equals(currentLastName))
    {
      final String uppercaseLastname = mUtility.upperCaseFirstLetter(newLastName);
      mFirestore.updateUserDetail(userRef, KEY_LASTNAME, uppercaseLastname, batch);
    }

    progressBar.setVisibility(View.VISIBLE);
    disableButtons();
    batch.commit()
    .addOnSuccessListener(l ->
    {
      progressBar.setVisibility(View.GONE);

      final String upperFirst = mUtility.upperCaseFirstLetter(newFirstName);
      final String upperlast = mUtility.upperCaseFirstLetter(newLastName);
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
    firstname = mUtility.trimString(firstNameEditText.getText().toString());
    return firstname;
  }

  public String getLastName()
  {
    lastname = mUtility.trimString(lastNameEditText.getText().toString());
    return lastname;
  }

  @Override
  public void onResume()
  {
    super.onResume();
  }


  @Override
  public void onPause()
  {
    super.onPause();
    mEditProfileViewModel.getEditProfileUser().removeObservers(this);
    firstname = mUtility.trimString(getFirstName());
    lastname = mUtility.trimString(getLastName());

    if (!firstname.equals(""))
      mEditProfileViewModel.getEditProfileUser().getValue().set_firstName(mUtility.upperCaseFirstLetter(firstname));

    if (!lastname.equals(""))
      mEditProfileViewModel.getEditProfileUser().getValue().set_lastName(mUtility.upperCaseFirstLetter(lastname));
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putBoolean(KEY_IN_PROGRESS, inProgress);
  }

  /*============*
   *   Utility  *
   *============*/
  public void showToast(String message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }
}
