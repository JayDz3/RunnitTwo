package com.idesign.runnit.LoginFragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.idesign.runnit.Constants;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.LoginData;
import com.idesign.runnit.R;
import com.idesign.runnit.VIewModels.LoginDataViewModel;

public class LoginFragment extends Fragment
{
  private final MyAuth mAuth = new MyAuth();
  private LoginDataViewModel mLoginDataViewModel;

  private ProgressBar loginProgressBar;
  private TextInputEditText emailView;
  private TextInputEditText passwordView;
  private TextView goResetClickable;
  private Button submitButton;

  private Context mContext;

  public LoginFragment() {}

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mLoginDataViewModel = ViewModelProviders.of(getActivity()).get(LoginDataViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.fragment_login, container, false);
    loginProgressBar = rootView.findViewById(R.id.login_progress_bar);
    emailView = rootView.findViewById(R.id.login_email);
    passwordView = rootView.findViewById(R.id.login_password);
    goResetClickable = rootView.findViewById(R.id.login_go_reset_clickable);
    submitButton = rootView.findViewById(R.id.login_submit);
    submitButton.setOnClickListener(l -> submit());
    goResetClickable.setOnClickListener(l -> goResetFragment());
    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
    hideProgressBar();
  }

  private Observer<LoginData> loginDataObserver()
  {
    return loginData ->
    {
      if (loginData == null)
      {
        return;
      }
      emailView.setText(loginData.getEmail());
      passwordView.setText(loginData.getPassword());
    };
  }

  public void submit()
  {
    if (emailIsValid() && passwordIsValid())
    {
      setLoggingIn(true);
      setEmail(emailView.getText().toString());
      setPassword(passwordView.getText().toString());
      disableButtons();
      showProgressBar();
      final String email = emailView.getText().toString();
      final String pw = passwordView.getText().toString();

      mAuth.signInWithEmailAndPassword(email, pw)
      .addOnSuccessListener(l -> onLoginSuccess())
      .addOnFailureListener(this::onLoginFailure);
    }
  }

  public void hideProgressBar()
  {
    loginProgressBar.setVisibility(View.GONE);
  }

  public void showProgressBar()
  {
    loginProgressBar.setVisibility(View.VISIBLE);
  }

  public void disableButtons()
  {
    final int DARK_GREY = ContextCompat.getColor(mContext, R.color.colorDarkGray);
    submitButton.setTextColor(DARK_GREY);
    submitButton.setEnabled(false);
    submitButton.setClickable(false);
    goResetClickable.setEnabled(false);
    goResetClickable.setClickable(false);
  }

  public void enableButtons()
  {
    final int PRIMARY_BLUE = ContextCompat.getColor(mContext, R.color.colorPrimary);
    submitButton.setTextColor(PRIMARY_BLUE);
    submitButton.setEnabled(true);
    submitButton.setClickable(true);
    goResetClickable.setEnabled(true);
    goResetClickable.setClickable(true);
  }

  public boolean emailIsValid()
  {
    return !TextUtils.isEmpty(emailView.getText()) && Patterns.EMAIL_ADDRESS.matcher(emailView.getText()).matches();
  }

  public boolean passwordIsValid()
  {
    return !TextUtils.isEmpty(passwordView.getText());
  }

  /*
   *  Nav actions
   *
   *  @param navigationState determines next step via view model
   */
  public void goResetFragment()
  {
    disableButtons();
    mLoginDataViewModel.setNavigationState(Constants.PASSWORD_RESET_FRAGMENT);
    mLoginDataViewModel.updateLoginData();
  }

  public void onLoginSuccess()
  {
    setLoggingIn(false);
    hideProgressBar();
    mLoginDataViewModel.setNavigationState(Constants.LOGIN_SUCCESS);
    mLoginDataViewModel.updateLoginData();
  }

  public void onLoginFailure(Exception e)
  {
    setLoggingIn(false);
    hideProgressBar();
    enableButtons();
    showToast("error: " + e.getMessage());
  }
  // End nav actions //

  /*
   *  Set / Get values
   *
   *  @ view model
   */
  public void setEmail(String email)
  {
    mLoginDataViewModel.setLoginEmail(email);
  }

  public void setPassword(String password)
  {
    mLoginDataViewModel.setPassword(password);
  }

  public void setLoggingIn(boolean status)
  {
    mLoginDataViewModel.setLoggingIn(status);
  }

  public boolean isLoggingIn()
  {
    return mLoginDataViewModel.getLoggingIn();
  }
  // End set / get //

  @Override
  public void onAttach(Context context)
  {
    super.onAttach(context);
    mContext = context;
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    setEmail(emailView.getText().toString());
    setPassword(passwordView.getText().toString());
    mLoginDataViewModel.getLoginData().removeObserver(loginDataObserver());
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mLoginDataViewModel.getLoginData().observe(this, loginDataObserver());
    if (isLoggingIn())
    {
      showProgressBar();
      disableButtons();
    }
  }

  public void showToast(String message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  /* public void logMessage(String message)
  {
    Log.d("LOGIN ACTIVITY:", "login fragment:: " + message);
  } */
}
