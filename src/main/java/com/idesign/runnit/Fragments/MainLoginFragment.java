package com.idesign.runnit.Fragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.idesign.runnit.Constants;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.LoginData;
import com.idesign.runnit.R;
import com.idesign.runnit.VIewModels.LoginDataViewModel;
import com.idesign.runnit.VIewModels.StateViewModel;

public class MainLoginFragment extends Fragment {

  private final MyAuth mAuth = new MyAuth();

  private LoginDataViewModel mLoginViewModel;
  private StateViewModel mStateViewModel;

  private EditText emailEditText;
  private EditText passwordEditText;

  private Button submitButton;
  private Button createAccountButton;

  private TextView resetClickable;

  private ProgressBar progressBar;

  public MainLoginFragment() {}

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mLoginViewModel = ViewModelProviders.of(getActivity()).get(LoginDataViewModel.class);
    mStateViewModel = ViewModelProviders.of(getActivity()).get(StateViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.fragment_main_login, container, false);
    emailEditText = rootView.findViewById(R.id.login_fragment_email);
    passwordEditText = rootView.findViewById(R.id.login_fragment_password);
    submitButton = rootView.findViewById(R.id.login_fragment_submit);
    submitButton.setOnClickListener(l -> submit());

    resetClickable = rootView.findViewById(R.id.login_fragment_go_reset_clickable);
    resetClickable.setOnClickListener(l -> goReset());

    createAccountButton = rootView.findViewById(R.id.login_fragment_create_account_text);
    createAccountButton.setOnClickListener(l -> goCreateAccount());

    progressBar = rootView.findViewById(R.id.progress_bar);
    progressBar.setVisibility(View.GONE);
    return rootView;
  }

  public void hideKeyBoard()
  {
    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
  }

  private Observer<LoginData> loginDataObserver()
  {
    return loginData -> {
      if (loginData == null)
        return;

      if (loginData.getEmail() != null)
        emailEditText.setText(loginData.getEmail());

      if (loginData.getPassword() != null)
        passwordEditText.setText(loginData.getPassword());

      if (loginData.getLoggingIn())
        progressBar.setVisibility(View.VISIBLE);
    };
  }

  public void goReset()
  {
    mStateViewModel.setFragmentState(Constants.STATE_RESET);
  }

  public void goCreateAccount()
  {
    mStateViewModel.setFragmentState(Constants.STATE_DETAILS_FRAGMENT);
  }

  public void submit()
  {
    final String email = trimmedString(emailEditText.getText().toString());
    final String password = trimmedString(passwordEditText.getText().toString());
    final String lowercaseEmail = lowercaseString(email);

    if (isEmpty(emailEditText))
      emailEditText.setError("Email required");

    if (isEmpty(passwordEditText))
      passwordEditText.setError("Password required");

    if (!isEmpty(emailEditText) && !isValidEmail(lowercaseEmail))
      emailEditText.setError("Not a valid email");

    mLoginViewModel.setLoginEmail(lowercaseEmail);
    mLoginViewModel.setPassword(password);

    if (!isEmpty(emailEditText) && isValidEmail(lowercaseEmail) && !isEmpty(passwordEditText))
    {
      disableButtons();
      progressBar.setVisibility(View.VISIBLE);
      mLoginViewModel.setLoggingIn(true);
      mAuth.signInWithEmailAndPassword(lowercaseEmail, password)
      .addOnSuccessListener(l -> onLoginSuccess())
      .addOnFailureListener(this::onLogoutFailure);
    }
  }

  public void onLoginSuccess()
  {
    mLoginViewModel.setLoggingIn(false);
    progressBar.setVisibility(View.GONE);
    // emailEditText.setText("");
    // passwordEditText.setText("");
    mLoginViewModel.setNavigationState(Constants.LOGIN_SUCCESS);
    mStateViewModel.setFragmentState(Constants.STATE_HOME);
    hideKeyBoard();
    enableButtons();
  }

  public void onLogoutFailure(Exception e)
  {
    mLoginViewModel.setLoggingIn(false);
    progressBar.setVisibility(View.GONE);
    enableButtons();
    showToast(e.getMessage());
  }

  public void disableButtons()
  {
    resetClickable.setEnabled(false);
    resetClickable.setClickable(false);
    submitButton.setEnabled(false);
    submitButton.setClickable(false);
    createAccountButton.setEnabled(false);
    createAccountButton.setClickable(false);
  }

  public void enableButtons()
  {
    resetClickable.setEnabled(true);
    resetClickable.setClickable(true);
    submitButton.setEnabled(true);
    submitButton.setClickable(true);
    createAccountButton.setEnabled(true);
    createAccountButton.setClickable(true);
  }

  public String getEmail()
  {
    return emailEditText.getText().toString();
  }

  public String getPassword()
  {
    return passwordEditText.getText().toString();
  }

  public boolean isEmpty(EditText editText)
  {
    return TextUtils.isEmpty(editText.getText());
  }

  public boolean isValidEmail(String email)
  {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mLoginViewModel.getLoginData().observe(this, loginDataObserver());
  }

  @Override
  public void onPause()
  {
    super.onPause();
    final String email = trimmedString(getEmail());
    final String password = trimmedString(getPassword());
    final String lowercaseEmail = lowercaseString(email);

    mLoginViewModel.setLoginEmail(lowercaseEmail);
    mLoginViewModel.setPassword(password);
    mLoginViewModel.getLoginData().removeObserver(loginDataObserver());
  }

  public String trimmedString(String source)
  {
    return source.trim();
  }

  public String lowercaseString(String source)
  {
    return source.toLowerCase();
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

  public void showToast(String message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }
}
