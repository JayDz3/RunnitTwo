package com.idesign.runnit;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.idesign.runnit.AllUsersFragments.AllUsers;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.VIewModels.AllUsersViewModel;

public class AllUsersActivity extends AppCompatActivity implements AllUsers.AllUsersFragmentListener
{
  private final BaseFirestore mFirestore = new BaseFirestore();

  private AllUsersViewModel mUsersViewModel;
  private AllUsers mAllUsersFragment;

  private String uid;
  private String orgPushId;
  private int viewedFragment = -1;

  private final String ORG_PUSHID = "org_pushid";
  private final String USER_UID = "user_uid";
  private final String VIEWED_FRAGMENT = "viewed_fragment";

  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_all_users);
    progressBar = findViewById(R.id.all_users_activity_progress_bar);

    if (savedInstanceState == null) {
      getValuesFromIntent();
      progressBar.setVisibility(View.VISIBLE);

    } else {
      progressBar.setVisibility(View.GONE);
      getValuesFromBundle(savedInstanceState);
    }

    mUsersViewModel = ViewModelProviders.of(this).get(AllUsersViewModel.class);
    if (viewedFragment == -1 || viewedFragment == 0)
      attachAllUsersFragment();
  }

  public void getUsers()
  {
    mAllUsersFragment.setEnabled(false);
    mFirestore.getOrganizationUsers(orgPushId)
    .addOnSuccessListener(usersSnapshot ->
    {
      progressBar.setVisibility(View.GONE);
      mUsersViewModel.setUsersFromQuerySnapshot(usersSnapshot);
      mAllUsersFragment.setEnabled(true);
    })
    .addOnFailureListener(e ->
    {
      progressBar.setVisibility(View.GONE);
      showToast(e.getMessage());
      mAllUsersFragment.setEnabled(true);
    });
  }

  public void attachAllUsersFragment()
  {
    if (mAllUsersFragment != null && mAllUsersFragment.isVisible())
      return;

    if (mAllUsersFragment == null)
      mAllUsersFragment = new AllUsers();

    Bundle args = new Bundle();
    args.putString(USER_UID, uid);
    args.putString(ORG_PUSHID, orgPushId);
    mAllUsersFragment.setArguments(args);

    getSupportFragmentManager()
    .beginTransaction()
    .replace(R.id.all_users_activity_frame_layout, mAllUsersFragment)
    .commit();
  }

  @Override
  public void disable()
  {
    progressBar.setVisibility(View.VISIBLE);
  }

  @Override
  public void enable()
  {
    progressBar.setVisibility(View.GONE);
  }

  public void getValuesFromIntent()
  {
    uid = getIntent().getStringExtra(USER_UID);
    orgPushId = getIntent().getStringExtra(ORG_PUSHID);
  }

  public void getValuesFromBundle(Bundle inState)
  {
    if (inState.keySet().contains(USER_UID))
    {
      uid = inState.getString(USER_UID);
      orgPushId = inState.getString(ORG_PUSHID);
      viewedFragment = inState.getInt(VIEWED_FRAGMENT, 0);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    outState.putString(USER_UID, uid);
    outState.putString(ORG_PUSHID, orgPushId);
    outState.putInt(VIEWED_FRAGMENT, viewedFragment);
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onStart()
  {
    super.onStart();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    getUsers();
  }

  @Override
  public void onPause()
  {
    super.onPause();
  }

  @Override
  public void onStop()
  {
    super.onStop();
  }

  public void showToast(String message)
  {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
