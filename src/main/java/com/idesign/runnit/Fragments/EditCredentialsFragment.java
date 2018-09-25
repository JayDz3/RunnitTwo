package com.idesign.runnit.Fragments;

import com.idesign.runnit.R;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.util.Patterns;
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

import com.idesign.runnit.UtilityClass;
import com.idesign.runnit.VIewModels.EditProfileViewModel;
import com.idesign.runnit.VIewModels.StateViewModel;

import java.util.Objects;

public class EditCredentialsFragment extends Fragment
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final UtilityClass mUtility = new UtilityClass();

  private EditProfileViewModel mEditProfileViewModel;
  private StateViewModel mStateViewModel;

  private EditText editCurrentEmail;
  private EditText editNewEmail;
  private EditText passwordEditText;

  private Button confirmButton;
  private Button goProfileButton;

  private ProgressBar progressBar;

  private final String KEY_EMAIL = "_email";

  public EditCredentialsFragment() {}

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mEditProfileViewModel = ViewModelProviders.of(getActivity()).get(EditProfileViewModel.class);
    mStateViewModel = ViewModelProviders.of(getActivity()).get(StateViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.fragment_edit_credentials, container, false);
    editCurrentEmail = rootView.findViewById(R.id.edit_credentials_fragment_current_email_text);
    editNewEmail = rootView.findViewById(R.id.edit_credentials_fragment_new_email_text);
    passwordEditText = rootView.findViewById(R.id.edit_credentials_fragment_password);

    confirmButton = rootView.findViewById(R.id.edit_credentials_submit_button);
    confirmButton.setOnClickListener(l -> submit());

    goProfileButton = rootView.findViewById(R.id.edit_credentials_go_profile_button);
    goProfileButton.setOnClickListener(l -> goProfile());

    progressBar = rootView.findViewById(R.id.progress_bar);
    progressBar.setVisibility(View.GONE);

    if (savedInstanceState == null)
      mUtility.observeViewModel(this, mEditProfileViewModel.getEditProfileUser(), userObserver());

    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
  }

  private Observer<EditProfileUser> userObserver()
  {
    return user ->
    {
      if (user != null)
      {
        editCurrentEmail.setText(user.get_email());
        editNewEmail.setText(user.get_newEmail());
        passwordEditText.setText(user.get_password());
      }
    };
  }

  public String getPassword()
  {
    return passwordEditText.getText().toString();
  }

  public void submit()
  {
    final String uid = mAuth.user().getUid();
    final String fbUserEmail = mUtility.lowercaseString(mAuth.user().getEmail());
    final String currentEmail = getCurrentEmail().toLowerCase();
    final String newEmail = getNewEmail().toLowerCase();
    final String password =  getPassword();

    if (mUtility.isEmpty(currentEmail))
      editCurrentEmail.setError("Required field");

    if (mUtility.isEmpty(newEmail))
      editNewEmail.setError("Required field");

    if (!mUtility.isEmpty(currentEmail) && !fbUserEmail.equals(currentEmail))
      editCurrentEmail.setError("Email does not match");

    if (!mUtility.isEmpty(currentEmail) && !mUtility.isEmpty(newEmail) && !mUtility.isEmpty(password) && fbUserEmail.equals(mUtility.lowercaseString(currentEmail)) && isValidEmail(currentEmail) && isValidEmail(newEmail))
    {
      disableButtons();
      final DocumentReference userRef = mFirestore.getUsers().document(uid);
      final WriteBatch batch = mFirestore.batch();
      final String firstname = Objects.requireNonNull(mEditProfileViewModel.getEditProfileUser().getValue()).get_firstName();
      final String lastname = mEditProfileViewModel.getEditProfileUser().getValue().get_lastName();
      final String updateEmail = mUtility.lowercaseString(newEmail);

      showToast(password);
      mAuth.signInWithEmailAndPassword(updateEmail, password)
      .onSuccessTask(ignore -> mAuth.user().updateEmail(updateEmail))
      .onSuccessTask(ignore ->
      {
        mFirestore.updateUserDetail(userRef, KEY_EMAIL, updateEmail, batch);
        return batch.commit();
      })
      .addOnSuccessListener(l ->
      {
        showToast("Updated");
        final EditProfileUser user = new EditProfileUser(firstname, lastname, "", "", "");
        mEditProfileViewModel.setEditProfileUser(user);
        enableButtons();
      })
      .addOnFailureListener(e ->
      {
        showToast(e.getMessage());
        enableButtons();
      });
    }
  }

  public void showToast(String message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  public boolean isValidEmail(String email)
  {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches();
  }

  public void disableButtons()
  {
    editCurrentEmail.setEnabled(false);
    editCurrentEmail.setClickable(false);
    editNewEmail.setClickable(false);
    editNewEmail.setEnabled(false);
    passwordEditText.setClickable(false);
    passwordEditText.setEnabled(false);
    confirmButton.setEnabled(false);
    confirmButton.setClickable(false);
  }

  public void enableButtons()
  {
    editCurrentEmail.setEnabled(true);
    editCurrentEmail.setClickable(true);
    editNewEmail.setClickable(true);
    editNewEmail.setEnabled(true);
    passwordEditText.setEnabled(true);
    passwordEditText.setClickable(true);
    confirmButton.setEnabled(true);
    confirmButton.setClickable(true);
  }

  public String getCurrentEmail()
  {
    return editCurrentEmail.getText().toString();
  }

  public String getNewEmail()
  {
    return editNewEmail.getText().toString();
  }

  public void goProfile()
  {
    updateProfile();
    mStateViewModel.setFragmentState(Constants.STATE_EDIT_PROFILE);
  }

  public void updateProfile()
  {
    final String firstname = mUtility.trimString(mEditProfileViewModel.getEditProfileUser().getValue().get_firstName());
    final String lastname = mUtility.trimString(mEditProfileViewModel.getEditProfileUser().getValue().get_lastName());
    final String oldEmail = mUtility.trimString(getCurrentEmail());
    final String newEmail = mUtility.trimString(getNewEmail());
    final String password = mUtility.trimString(getPassword());
    mEditProfileViewModel.getEditProfileUser().getValue().set_firstName(firstname);
    mEditProfileViewModel.getEditProfileUser().getValue().set_lastName(lastname);
    mEditProfileViewModel.getEditProfileUser().getValue().set_email(oldEmail);
    mEditProfileViewModel.getEditProfileUser().getValue().set_newEmail(newEmail);
    mEditProfileViewModel.getEditProfileUser().getValue().set_password(password);
  }

  @Override
  public void onPause()
  {
    super.onPause();
    mEditProfileViewModel.getEditProfileUser().removeObservers(this);
    updateProfile();

    editCurrentEmail.setError(null);
    editNewEmail.setError(null);
    passwordEditText.setError(null);
  }

  @Override
  public void onResume()
  {
    super.onResume();
    final boolean inProgress = Objects.requireNonNull(mEditProfileViewModel.getEditProfileUser().getValue()).get_updateInProgress();
    if (inProgress)
      progressBar.setVisibility(View.VISIBLE);
  }
}
