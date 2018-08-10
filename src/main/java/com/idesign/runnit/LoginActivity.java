package com.idesign.runnit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.idesign.runnit.Items.LoginData;
import com.idesign.runnit.LoginFragments.LoginFragment;
import com.idesign.runnit.LoginFragments.ResetFragment;
import com.idesign.runnit.VIewModels.LoginDataViewModel;

public class LoginActivity extends AppCompatActivity
{

  private Fragment mLoginFragment;
  private Fragment mResetFragment;
  private LoginDataViewModel mLoginDataViewModel;
  private int fragmentBeingViewed = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    if (savedInstanceState != null)
    {
      getValuesFromBundle(savedInstanceState);
    }
    mLoginDataViewModel = ViewModelProviders.of(this).get(LoginDataViewModel.class);
  }

  private Observer<LoginData> loginDataObserver()
  {
    return loginData ->
    {
      if (loginData != null)
      {
        final int state = loginData.getNavigationState();
        switch (state) {
          case Constants.STATE_LOGIN_FRAGMENT:
            goLoginFragment();
            break;
          case Constants.PASSWORD_RESET_FRAGMENT:
            goResetFragment();
            break;
          case Constants.LOGIN_SUCCESS:
            finish();
            break;
          case Constants.RESET_SUCCESS:
            goLoginFragment();
            break;
          default:
            goLoginFragment();
            break;
        }
      }
    };
  }

  public void goLoginFragment()
  {
    if (mLoginFragment != null && mLoginFragment.isVisible())
    {
      return;
    }
    if (mLoginFragment == null)
    {
      mLoginFragment = new LoginFragment();
    }
    fragmentBeingViewed = Constants.LOGIN_FRAGMENT;
    replaceFragment(mLoginFragment);
  }

  public void goResetFragment()
  {
    if (mResetFragment != null && mResetFragment.isVisible())
    {
      return;
    }
    if (mResetFragment == null)
    {
      mResetFragment = new ResetFragment();
    }
    fragmentBeingViewed = Constants.PASSWORD_RESET_FRAGMENT;
    replaceFragment(mResetFragment);
  }

  public void replaceFragment(Fragment fragment)
  {
    getSupportFragmentManager()
    .beginTransaction()
    .replace(R.id.login_activity_frame_layout, fragment)
    .commit();
  }

  @Override
  public void onStart()
  {
    super.onStart();
  }

  @Override
  public void onStop()
  {
    super.onStop();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    mLoginDataViewModel.getLoginData().removeObserver(loginDataObserver());
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mLoginDataViewModel.getLoginData().observe(this, loginDataObserver());
  }

  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putInt(Constants.LOGIN_FRAGMENT_BEING_VIEWED, fragmentBeingViewed);
  }

  public void getValuesFromBundle(Bundle incoming)
  {
    if (incoming.keySet().contains(Constants.LOGIN_IN_PROGRESS))
    {
      fragmentBeingViewed = incoming.getInt(Constants.LOGIN_FRAGMENT_BEING_VIEWED);
    }
  }

  @Override
  public void onBackPressed()
  {
    final int j = mLoginDataViewModel.getNavigationState();
    if (j == 0 || j == Constants.LOGIN_FRAGMENT) {
      mLoginDataViewModel.clear();
      super.onBackPressed();

    } else if (j == 2) {
      mLoginDataViewModel.setNavigationState(Constants.LOGIN_FRAGMENT);
      mLoginDataViewModel.updateLoginData();

    } else {
      showToast("default on back pressed");
     // mLoginDataViewModel.clear();
     // super.onBackPressed();
    }
  }

  public void logMessage(String message)
  {
    Log.d("LOGIN ACTIVITY:", "Message:: " + message);
  }

  public void showToast(String message)
  {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
