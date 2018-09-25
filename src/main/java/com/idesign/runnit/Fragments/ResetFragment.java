package com.idesign.runnit.Fragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;

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
import com.idesign.runnit.UtilityClass;
import com.idesign.runnit.VIewModels.LoginDataViewModel;
import com.idesign.runnit.VIewModels.StateViewModel;

public class ResetFragment extends Fragment
{
  private final MyAuth mAuth = new MyAuth();
  private final UtilityClass mUtility = new UtilityClass();

  private LoginDataViewModel mLoginViewModel;
  private StateViewModel mStateViewModel;

  private Button resetFragmentSubmitButton;
  private TextInputEditText emailEditView;

  public ResetFragment() {}

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
    View rootView = inflater.inflate(R.layout.fragment_reset, container, false);
    setViewItems(rootView);
    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
    if (savedInstanceState == null)
      mUtility.observeViewModel(this, mLoginViewModel.getLoginData(), loginDataObserver());
  }

  public void setViewItems(View view)
  {
    emailEditView = view.findViewById(R.id.reset_fragment_email);
    resetFragmentSubmitButton = view.findViewById(R.id.reset_submit);
    resetFragmentSubmitButton.setOnClickListener(l -> submit());
  }

  private Observer<LoginData> loginDataObserver()
  {
    return loginData ->
    {
      if (loginData != null)
        emailEditView.setText(loginData.getResetEmail());
    };
  }

  public void setViewModelEmail()
  {
    final String trimmed = mUtility.trimString(emailEditView.getText().toString());
    mLoginViewModel.setResetEmail(trimmed);
  }

  public void submit()
  {
    setViewModelEmail();
    if (isValidEmail())
    {
      final String email = mUtility.trimString(emailEditView.getText().toString());
      mAuth.sendResetPassword(email)
      .addOnSuccessListener(l ->
      {
        showToast("An email has been sent to you...");
        mLoginViewModel.setResetEmail("");
        emailEditView.setText("");
        mStateViewModel.setFragmentState(Constants.STATE_LOGIN);
      })
      .addOnFailureListener(e -> showToast("error sending reset email: " + e.getMessage()));
    }
  }

  public boolean isValidEmail()
  {
    return !mUtility.isEmpty(emailEditView) && Patterns.EMAIL_ADDRESS.matcher(emailEditView.getText()).matches();
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
    mLoginViewModel.getLoginData().removeObservers(this);
    setViewModelEmail();
  }

  @Override
  public void onResume()
  {
    super.onResume();
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
