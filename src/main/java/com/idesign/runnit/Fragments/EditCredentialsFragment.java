package com.idesign.runnit.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import com.idesign.runnit.R;
import com.idesign.runnit.VIewModels.EditProfileViewModel;
import com.idesign.runnit.VIewModels.StateViewModel;

import java.util.Objects;

public class EditCredentialsFragment extends Fragment
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

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
    final String fbUserEmail = lowercaseString(mAuth.user().getEmail());
    final String currentEmail = getCurrentEmail().toLowerCase();
    final String newEmail = getNewEmail().toLowerCase();
    final String password =  getPassword();

    if (isEmpty(currentEmail))
      editCurrentEmail.setError("Required field");

    if (isEmpty(newEmail))
      editNewEmail.setError("Required field");

    if (!isEmpty(currentEmail) && !fbUserEmail.equals(currentEmail))
      editCurrentEmail.setError("Email does not match");

    if (!isEmpty(currentEmail) && !isEmpty(newEmail) && !isEmpty(password) && fbUserEmail.equals(lowercaseString(currentEmail)) && isValidEmail(currentEmail) && isValidEmail(newEmail))
    {
      disableButtons();
      final DocumentReference userRef = mFirestore.getUsers().document(uid);
      final WriteBatch batch = mFirestore.batch();
      final String firstname = Objects.requireNonNull(mEditProfileViewModel.getEditProfileUser().getValue()).get_firstName();
      final String lastname = mEditProfileViewModel.getEditProfileUser().getValue().get_lastName();
      final String updateEmail = lowercaseString(newEmail);

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

  public boolean isEmpty(String source)
  {
    return TextUtils.isEmpty(source);
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
    final String firstname = mEditProfileViewModel.getEditProfileUser().getValue().get_firstName();
    final String lastname = mEditProfileViewModel.getEditProfileUser().getValue().get_lastName();
    final String oldEmail = getCurrentEmail();
    final String newEmail = getNewEmail();
    final String password = getPassword();
    final EditProfileUser user = new EditProfileUser(firstname, lastname, oldEmail, newEmail, password);
    mEditProfileViewModel.setEditProfileUser(user);
  }

  @Override
  public void onPause()
  {
    super.onPause();
    updateProfile();
    mEditProfileViewModel.getEditProfileUser().removeObserver(userObserver());

    editCurrentEmail.setError(null);
    editNewEmail.setError(null);
    passwordEditText.setError(null);
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mEditProfileViewModel.getEditProfileUser().observe(this, userObserver());
    final boolean inProgress = Objects.requireNonNull(mEditProfileViewModel.getEditProfileUser().getValue()).get_updateInProgress();
    if (inProgress)
      progressBar.setVisibility(View.VISIBLE);
  }

  public String lowercaseString(String source)
  {
    return source.toLowerCase();
  }
}
