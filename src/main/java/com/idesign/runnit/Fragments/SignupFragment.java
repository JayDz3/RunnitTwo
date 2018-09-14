package com.idesign.runnit.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.idesign.runnit.Constants;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.R;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.VIewModels.PasswordViewModel;
import com.idesign.runnit.VIewModels.StateViewModel;
import com.idesign.runnit.VIewModels.UserViewModel;

import java.util.Objects;

public class SignupFragment extends Fragment
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private UserViewModel mUserViewModel;
  private PasswordViewModel mPasswordViewModel;

  private TextInputEditText editFirstName;
  private TextInputEditText editLastName;
  private TextInputEditText editEmail;
  private TextInputEditText editPassword;

  private StateViewModel mStateViewModel;

  private Button submitButton;
  private Button clearButton;

  private ProgressBar progressBar;

  public SignupFragment() { }


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mUserViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(UserViewModel.class);
    mStateViewModel = ViewModelProviders.of(getActivity()).get(StateViewModel.class);
    mPasswordViewModel = ViewModelProviders.of(getActivity()).get(PasswordViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.fragment_signup, container, false);
    setViewItems(rootView);
    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
    progressBar.setVisibility(View.GONE);
  }

  public void setViewItems(View view)
  {
    progressBar = view.findViewById(R.id.signup_progress_bar);
    editFirstName = view.findViewById(R.id.signup_first_name);
    editLastName = view.findViewById(R.id.signup_last_name);
    editEmail = view.findViewById(R.id.signup_email);
    editPassword = view.findViewById(R.id.signup_password);
    submitButton = view.findViewById(R.id.signup_submit);
    clearButton = view.findViewById(R.id.signup_reset_button);
    submitButton.setOnClickListener(l -> submit());
    clearButton.setOnClickListener(l -> clearForm());
  }

  private Observer<String> passwordObserver()
  {
    return password -> editPassword.setText(password);
  }

  private Observer<User> getUserObserver()
  {
    return user ->
    {
      editFirstName.setText(Objects.requireNonNull(user).get_firstName());
      editLastName.setText(user.get_lastName());
      editEmail.setText(user.get_email());
    };
  }

  public boolean fieldNotEmpty(EditText editText)
  {
    return !TextUtils.isEmpty(editText.getText());
  }

  public void submit()
  {
    disableButtons();

    if (fieldNotEmpty(editFirstName)
      && fieldNotEmpty(editLastName)
      && fieldNotEmpty(editEmail)
      && Patterns.EMAIL_ADDRESS.matcher(editEmail.getText()).matches()
      && fieldNotEmpty(editPassword))
    {
      final String trimmedFirstname = trimmedString(editFirstName.getText().toString());
      final String trimmedLastname = trimmedString(editLastName.getText().toString());
      final String trimmedEmail = trimmedString(editEmail.getText().toString());
      final String trimmedPassword = trimmedString(editPassword.getText().toString());

      setUserViewModelFirstName(trimmedFirstname);
      setUserViewModelLastName(trimmedLastname);
      setUserViewModelEmail(trimmedEmail);
      setPasswordViewModelValue(trimmedPassword);
      createUser();

    } else {
      enableButtons();
    }
  }

  public void createUser()
  {
    disableButtons();
    progressBar.setVisibility(View.VISIBLE);
    final String email = mUserViewModel.getEmail();
    final String pw = mPasswordViewModel.getPassword().getValue();
    final String firstname = mUserViewModel.getFirstName();
    final String lastname = mUserViewModel.getLastName();
    final String lowercaseEmail = lowercaseString(email);

    mAuth.createUser(lowercaseEmail, pw)
    .onSuccessTask(firebaseUser ->  setFirestoreUser(firstname, lastname))
    .addOnSuccessListener(task ->
    {
      mUserViewModel.setLoggedIn(true);
      mStateViewModel.setFragmentState(Constants.STATE_HOME);
      showToast("Navigate to Edit Org Code to complete registration!");
    })
    .addOnFailureListener(e ->
    {
      progressBar.setVisibility(View.GONE);
      showToast("error: " + e.getMessage());
      enableButtons();
    });
  }
  /*
   Set user in Cloud Firestore
   */
  // follow up task 4
  private Task<Void> setFirestoreUser(String firstname, String lastname)
  {
    final String pushid = mAuth.user().getUid();
    final DocumentReference documentReference = mFirestore.getUsers().document(pushid);

    mUserViewModel.setPushId(pushid);
    mUserViewModel.setIsAdmin(false);
    mUserViewModel.setOrganizationCode("");
    mUserViewModel.setSendNotification(false);
    mUserViewModel.setInstanceId("");
    mUserViewModel.setLoggedIn(false);

    final User user = mUserViewModel.getUser().getValue();

    Objects.requireNonNull(user).set_firstName(firstname);
    user.set_lastName(lastname);
    user.set_email(mAuth.user().getEmail());

    return documentReference.set(user);
  }

  public void clearForm()
  {
    clearField(editFirstName);
    clearField(editLastName);
    clearField(editEmail);
    clearField(editPassword);
    setUserViewModelFirstName("");
    setUserViewModelLastName("");
    setUserViewModelEmail("");
    setPasswordViewModelValue("");
  }

  public void setUserViewModelFirstName(String _firstName)
  {
    mUserViewModel.setFirstName(_firstName);
  }

  public void setUserViewModelLastName(String _lastName)
  {
    mUserViewModel.setLastName(_lastName);
  }

  public void setUserViewModelEmail(String _email)
  {
    mUserViewModel.setEmail(_email);
  }

  public void setPasswordViewModelValue(String _passWord)
  {
    mPasswordViewModel.setPassword(_passWord);
  }

  public void clearField(EditText editText)
  {
    editText.setText("");
  }

  public void disableButtons()
  {
    submitButton.setEnabled(false);
    submitButton.setClickable(false);
    clearButton.setEnabled(false);
    clearButton.setClickable(false);
  }

  public void enableButtons()
  {
    submitButton.setEnabled(true);
    submitButton.setClickable(true);
    clearButton.setEnabled(true);
    clearButton.setClickable(true);
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mPasswordViewModel.getPassword().observe(this, passwordObserver());
    mUserViewModel.getUser().observe(this, getUserObserver());
  }

  @Override
  public void onPause()
  {
    super.onPause();
    final String trimmedFirstname = trimmedString(editFirstName.getText().toString());
    final String trimmedLastname = trimmedString(editLastName.getText().toString());
    final String trimmedEmail = trimmedString(editEmail.getText().toString());
    final String trimmedPassword = trimmedString(editPassword.getText().toString());

    setUserViewModelFirstName(trimmedFirstname);
    setUserViewModelLastName(trimmedLastname);
    setUserViewModelEmail(trimmedEmail);
    setPasswordViewModelValue(trimmedPassword);
    mUserViewModel.getUser().removeObserver(getUserObserver());
    mPasswordViewModel.getPassword().removeObserver(passwordObserver());
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

  public String trimmedString(String source)
  {
    return source.trim();
  }

  public String lowercaseString(String source)
  {
    return source.toLowerCase();
  }

  public void showToast(String message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  /* public void logMessage(String message)
  {
    Log.d("SIGNUP FRAGMENT", "MESSAGE: " + message);
  } */
}
