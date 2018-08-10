package com.idesign.runnit.LoginFragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.idesign.runnit.Constants;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.LoginData;
import com.idesign.runnit.R;
import com.idesign.runnit.VIewModels.LoginDataViewModel;

public class ResetFragment extends Fragment
{

  private final MyAuth mAuth = new MyAuth();
  private LoginDataViewModel mLoginViewModel;

  private Button resetFragmentSubmitButton;
  private TextInputEditText emailEditView;

  public ResetFragment() {}


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mLoginViewModel = ViewModelProviders.of(getActivity()).get(LoginDataViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.fragment_reset, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
    emailEditView = view.findViewById(R.id.reset_fragment_email);
    resetFragmentSubmitButton = view.findViewById(R.id.reset_submit);
    resetFragmentSubmitButton.setOnClickListener(l -> submit());
  }

  private Observer<LoginData> loginDataObserver()
  {
    return loginData ->
    {
      if (loginData != null)
      {
        emailEditView.setText(loginData.getResetEmail());
      }
    };
  }

  public void setViewModelEmail()
  {
    mLoginViewModel.setResetEmail(emailEditView.getText().toString());
  }

  public void submit()
  {
    setViewModelEmail();
    if (isValidEmail())
    {
      final String email = emailEditView.getText().toString();
      mAuth.sendResetPassword(email)
      .addOnSuccessListener(l ->
      {
        setViewModelEmail();
        mLoginViewModel.setNavigationState(Constants.RESET_SUCCESS);
        showToast("An email has been sent to you...");
        mLoginViewModel.updateLoginData();
      })
      .addOnFailureListener(e -> showToast("error sending reset email: " + e.getMessage()));
    }
  }

  public boolean isValidEmail()
  {
    return !TextUtils.isEmpty(emailEditView.getText()) && Patterns.EMAIL_ADDRESS.matcher(emailEditView.getText()).matches();
  }

  @Override
  public void onStart()
  {
    super.onStart();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    mLoginViewModel.setResetEmail(emailEditView.getText().toString());
    mLoginViewModel.getLoginData().removeObserver(loginDataObserver());
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mLoginViewModel.getLoginData().observe(this, loginDataObserver());
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

  public void logMessage(String message)
  {
    Log.d("LOGIN ACTIVITY:", "login fragment:: " + message);
  }
}
