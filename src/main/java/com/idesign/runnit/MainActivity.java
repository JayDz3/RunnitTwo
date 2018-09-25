package com.idesign.runnit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;

import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;

import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;

import com.idesign.runnit.Dialogs.CancelChangesDialog;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Fragments.EditCredentialsFragment;
import com.idesign.runnit.Fragments.EditProfileFragment;
import com.idesign.runnit.Fragments.HomeFragment;
import com.idesign.runnit.Fragments.MainLoginFragment;
import com.idesign.runnit.Fragments.RestaurantCodeFragment;
import com.idesign.runnit.Fragments.SignupFragment;

import com.idesign.runnit.Items.FirestoreChannel;
import com.idesign.runnit.Items.StateEmitter;
import com.idesign.runnit.Fragments.ResetFragment;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.NavigationHelpers.NavigationViewUtility;
import com.idesign.runnit.VIewModels.AppUserViewModel;
import com.idesign.runnit.VIewModels.EditProfileViewModel;
import com.idesign.runnit.VIewModels.LoginDataViewModel;
import com.idesign.runnit.VIewModels.OrganizationObjectViewModel;
import com.idesign.runnit.VIewModels.PasswordViewModel;
import com.idesign.runnit.VIewModels.StateViewModel;
import com.idesign.runnit.VIewModels.UserChannelsViewModel;
import com.idesign.runnit.VIewModels.UserViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements
SignupFragment.SignupFragmentListener,
CancelChangesDialog.CancelChangesListener
{
  // Classes
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final NavigationViewUtility mNavUtility = new NavigationViewUtility();
  private final UtilityClass mUtility = new UtilityClass();

  // Layouts
  private NavigationView navigationView;
  private DrawerLayout mDrawerLayout;

  // private Snackbar snackbar;
  private Toolbar toolbar;
  private ActionBar actionBar;

  // View models
  private StateViewModel mStateViewModel;
  private PasswordViewModel mPasswordViewModel;

  private UserViewModel mUserViewModel;
  private AppUserViewModel mAppUserViewModel;
  private EditProfileViewModel mEditProfileViewModel;
  private OrganizationObjectViewModel mOrgViewModel;

  private UserChannelsViewModel mUserChannelViewModel;
  private LoginDataViewModel mLoginDataViewModel;

  // Fragments
  private HomeFragment mHomeFragment;
  private SignupFragment mSignupFragment;
  private RestaurantCodeFragment mRestaurantFragment;
  private MainLoginFragment mLoginFragment;
  private EditProfileFragment mEditProfileFragment;
  private ResetFragment mResetFragment;

  private EditCredentialsFragment mEditCredentialsFragment;

  // Listeners
  private FirebaseAuth.AuthStateListener authStateListener;
  private ListenerRegistration firestoreUserListener;

  private String selection;
  private int appState = 0;

  private boolean disabled = false;
  private boolean dialogOpen = false;

  private ProgressBar progressBar;

  private final String ORG_PUSHID = "org_pushid";
  private final String USER_UID = "user_uid";

  private int destination = -1;
  private final int GO_HOME_FRAGMENT = 0;
  private final int GO_EDITPROFILE_FRAGMENT = 1;
  private final int GO_EDITCREDENTIALS_FRAGMENT = 2;

  private final String EXTRA_DESTINATION = "destination";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getValuesFromBundle(savedInstanceState);
    setViewItems();
    setViewModels();
    setActionBar();
    addDrawerListener();
    addNavListener();

    if (savedInstanceState == null)
      toggleViewOnStart();
  }

  public void setViewItems()
  {
    mDrawerLayout = findViewById(R.id.drawer_layout);
    navigationView = findViewById(R.id.nav_view);
    toolbar = findViewById(R.id.toolbar);
    progressBar = findViewById(R.id.progress_bar);
    progressBar.setVisibility(View.GONE);
  }

  public void setViewModels()
  {
    mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
    mAppUserViewModel = ViewModelProviders.of(this).get(AppUserViewModel.class);
    mStateViewModel = ViewModelProviders.of(this).get(StateViewModel.class);
    mPasswordViewModel = ViewModelProviders.of(this).get(PasswordViewModel.class);
    mUserChannelViewModel = ViewModelProviders.of(this).get(UserChannelsViewModel.class);
    mLoginDataViewModel = ViewModelProviders.of(this).get(LoginDataViewModel.class);
    mEditProfileViewModel = ViewModelProviders.of(this).get(EditProfileViewModel.class);
    mOrgViewModel = ViewModelProviders.of(this).get(OrganizationObjectViewModel.class);
  }

  /*========================*
   *  Only occurs onCreate  *
   *========================*/
  public void toggleViewOnStart()
  {
    switch (appState)
    {
      case Constants.STATE_HOME:
        buildHomeFrag(Constants.STATE_HOME);
        break;
      case Constants.STATE_LOGIN:
        buildLoginFragment(Constants.STATE_LOGIN);
        break;
      case Constants.STATE_RESET:
        buildResetFragment(Constants.STATE_RESET);
        break;
      case Constants.STATE_DETAILS_FRAGMENT:
        buildSignupFrag(Constants.STATE_DETAILS_FRAGMENT);
        break;
      case Constants.STATE_RESTAURANT_FRAGMENT:
        buildRestaurantFrag(Constants.STATE_RESTAURANT_FRAGMENT);
        break;
      case Constants.STATE_EDIT_PROFILE:
        goEditProfile();
        break;
      case Constants.STATE_EDIT_CREDENTIALS:
        goEditCredentials();
        break;
      default:
        mUtility.logMessage("nada");
    }
  }
  // End on Create //

  /*========================*
   *  NAVIGATION LISTENERS  *
   *========================*/
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if (disabled)
      return false;

    switch (item.getItemId())
    {
      case android.R.id.home:
        mDrawerLayout.openDrawer(GravityCompat.START);
    }
    return super.onOptionsItemSelected(item);
  }

  // Listener for navigation drawer on item selected //
  public void addNavListener()
  {
    navigationView.setNavigationItemSelectedListener(item ->
    {
      item.setChecked(true);
      selection = item.toString();
      mDrawerLayout.closeDrawers();
      return true;
    });
  }

  public void addDrawerListener()
  {
    mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener()
    {
      public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }
      public void onDrawerOpened(@NonNull View drawerView) { }
      public void onDrawerClosed(@NonNull View view)
      {
        if (selection == null)
          return;

        switch (selection)
        {
          case "Logout":
            logout();
            break;
          case "Edit Info":
            mStateViewModel.setFragmentState(Constants.STATE_EDIT_PROFILE);
            break;
          case "Edit Org Code":
            mStateViewModel.setFragmentState(Constants.STATE_RESTAURANT_FRAGMENT);
            break;
          case "Admin":
            mNavUtility.setCheckedToFalse(R.id.nav_admin, navigationView);
            goCreateAdmin();
            break;
          case "Channels":
            mNavUtility.setCheckedToFalse(R.id.nav_channel, navigationView);
            goChannels();
            break;
          case "Users":
            mNavUtility.setCheckedToFalse(R.id.nav_all_users, navigationView);
            goAllUsers();
            break;
          case "Delete Account":
            mNavUtility.setCheckedToFalse(R.id.nav_delete_account, navigationView);
            goDeleteAccount();
            break;
          default:
            Log.d("DRAWER LISTENER", "default");
            break;
        }
        selection = null;
      }
      public void onDrawerStateChanged(int newState) { }
    });
  }
  // End Navigation Listeners //

  /*==================*
   *  LOGIN / LOGOUT  *
   *==================*/
  public void logout()
  {
    if (mAuth.user() != null)
    {
      final String uid = mAuth.user().getUid();
      final DocumentReference userRef = mFirestore.getUsers().document(uid);
      final WriteBatch batch = mFirestore.batch();
      mAuth.signOut();

      mFirestore.updateSubscribedUserTasks(uid, batch, mUserChannelViewModel)
      .continueWithTask(ignore -> mFirestore.deleteActiveUserReferences(uid, userRef, batch, mUserChannelViewModel))
      .addOnSuccessListener(ignore -> onLogoutSuccess())
      .addOnFailureListener(this::onLogoutFailure);
    }
  }

  public void onLogoutSuccess()
  {
    mNavUtility.isNotLoggedIn(navigationView);
    progressBar.setVisibility(View.GONE);
    disabled = false;
  }

  public void onLogoutFailure(Exception e)
  {
    mNavUtility.isNotLoggedIn(navigationView);
    progressBar.setVisibility(View.GONE);
    showToast("error: " + e.getMessage() + " You are however, signed out");
    mStateViewModel.setFragmentState(Constants.STATE_LOGIN);
    disabled = false;
  }

  public void disableNotifications()
  {
    ComponentName notificationService = new ComponentName(this, NotificationService.class);
    ComponentName notificationReceiver = new ComponentName(this, NotificationReceiver.class);
    PackageManager packageManager = this.getPackageManager();
    packageManager.setComponentEnabledSetting(notificationService, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    packageManager.setComponentEnabledSetting(notificationReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
  }

  public void enableNotifications()
  {
    ComponentName notificationService = new ComponentName(this, NotificationService.class);
    ComponentName notificationReceiver = new ComponentName(this, NotificationReceiver.class);
    PackageManager packageManager = this.getPackageManager();
    packageManager.setComponentEnabledSetting(notificationService, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    packageManager.setComponentEnabledSetting(notificationReceiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
  }

  /*==========================*
   *  Go To Other Activities  *
   *==========================*/
  public void goDeleteAccount()
  {
    Intent intent = new Intent(this, DeleteAccountActivity.class);
    startActivity(intent);
  }

  public void goCreateAdmin()
  {
    final Intent intent = new Intent(this, AdminActivity.class);
    startActivity(intent);
  }

  public void goChannels()
  {
    if (mAuth.user() == null)
    {
      showToast("Must log in to continue");
      return;
    }
    final boolean mIsAdmin = Objects.requireNonNull(mAppUserViewModel.getmUser().getValue()).get_isAdmin();

    if (mIsAdmin) {
      Intent intent = new Intent(this, ChannelActivity.class);
      startActivity(intent);

    } else {
      Intent intent = new Intent(this, UserChannelActivity.class);
      startActivity(intent);
    }
  }

  public void goAllUsers()
  {
    if (mAuth.user() == null)
    {
      showToast("Must log in to continue");
      return;
    }
    final boolean mIsAdmin = Objects.requireNonNull(mAppUserViewModel.getmUser().getValue()).get_isAdmin();
    final String uid = mAppUserViewModel.getmUser().getValue().get_pushId();
    final String orgPushId = mAppUserViewModel.getmUser().getValue().get_organizationPushId();

    if (uid == null || orgPushId == null || orgPushId.equals(""))
    {
      showToast("An error occured. Have you set up your organization id yet?");
      return;
    }

    if (mIsAdmin)
    {
      final Intent intent = new Intent(this, AllUsersActivity.class);
      intent.putExtra(USER_UID, uid);
      intent.putExtra(ORG_PUSHID, orgPushId);
      startActivity(intent);
    }
  }
  // END NAV ACTIONS //

  /*===============*
   *   ACTION BAR  *
   *===============*/
  public void setActionBar()
  {
    setSupportActionBar(toolbar);
    actionBar = getSupportActionBar();
    if (actionBar != null)
    {
      actionBar.show();
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_primary_24dp);
    }
  }

  public void hideActionBar()
  {
    actionBar.hide();
  }

  public void setActionBarForFragment()
  {
    toolbar.setNavigationOnClickListener(l -> {
      if (appState == Constants.STATE_RESTAURANT_FRAGMENT)
        mOrgViewModel.clear();

      setState(Constants.STATE_HOME);
    });
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp);
  }

  public void setActionBarForEditProfile()
  {
    toolbar.setNavigationOnClickListener(l ->
    {
      if (profileChangedOnClick()) {
        showCancelChangesDialog(0);

      } else {
        destination = -1;
        setState(Constants.STATE_HOME);
        clearProfile();
      }
    });
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp);
  }

  public void setActionBarForEditCredentials()
  {
    toolbar.setNavigationOnClickListener(l ->
    {
      if (credentialsChangedOnToolbarClick()) {
        showCancelChangesDialog(0);

      } else {
        setState(Constants.STATE_HOME);
        clearProfile();
      }
    });
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp);
  }
  // END ACTION BAR //

  /*==============================================*
   *  EDIT PROFILE AND EDIT CREDENTIALS GO FN'S   *
   *==============================================*/
  public void goEditProfile()
  {
    if (destination == GO_EDITPROFILE_FRAGMENT || fragmentVisible(mEditProfileFragment) || dialogOpen)
      return;

    if (fragmentVisible(mEditCredentialsFragment) && credentialsChangedOnGoProfile())
    {
      showCancelChangesDialog(1);
      return;
    }
    mUtility.updateUserProfileViewModel(mEditProfileViewModel, mAppUserViewModel);
    buildEditProfileFragment(Constants.STATE_EDIT_PROFILE);
  }

  public void goEditCredentials()
  {
    if (destination == GO_EDITCREDENTIALS_FRAGMENT || fragmentVisible(mEditCredentialsFragment) || dialogOpen)
      return;

    if (fragmentVisible(mEditProfileFragment) && profileChangedOnGoCredentials())
    {
      appState = Constants.STATE_EDIT_PROFILE;
      showCancelChangesDialog(2);
      return;
    }
    mUtility.updateUserProfileViewModel(mEditProfileViewModel, mAppUserViewModel);
    buildEditCredentialsFragment(Constants.STATE_EDIT_CREDENTIALS);
  }

  /*=============================================*
   *  Check For Profile and Credential Changes   *
   *=============================================*/
  public boolean profileChangedOnGoCredentials()
  {
    return mUtility.profileChangedOnGoCredentials(mEditProfileFragment, mAppUserViewModel);
  }

  public boolean profileChangedOnClick()
  {
    return mUtility.profileChangedOnClick(mEditProfileFragment, mAppUserViewModel);
  }

  public boolean credentialsChangedOnGoProfile()
  {
    return mUtility.credentialsChangedOnGoProfile(mEditCredentialsFragment, mAppUserViewModel);
  }

  public boolean credentialsChangedOnToolbarClick()
  {
    return mUtility.credentialsChangedOnToolbarClick(mEditCredentialsFragment, mAppUserViewModel);
  }
  // END CHECK PROFILE AND CREDENTIAL CHANGES

  /*===============================================================*
   *  Used when leaving EditProfile and EditCredentials Fragments  *
   *===============================================================*/
  public void clearProfile()
  {
    mUtility.clearProfile(mEditProfileViewModel);
  }

  public void clearCredentials()
  {
    mUtility.clearCredentials(mEditProfileViewModel);
  }

  public void resetProfileName()
  {
    mUtility.resetProfileName(mEditProfileViewModel, mAppUserViewModel);
  }

  /*=================================*
   *  Override CancelChangesDialog   *
   *=================================*/
  @Override
  public void onDialogDismiss()
  {
    dialogOpen = false;
    destination = -1;
    setState(appState);
  }

  @Override
  public void confirmNoChanges()
  {
    switch (destination)
    {
      case GO_HOME_FRAGMENT:
        clearProfile();
        buildHomeFrag(Constants.STATE_HOME);
        setState(Constants.STATE_HOME);
        destination = -1;
        break;
      case GO_EDITPROFILE_FRAGMENT:
        clearCredentials();
        buildEditProfileFragment(Constants.STATE_EDIT_PROFILE);
        setState(Constants.STATE_EDIT_PROFILE);
        destination = -1;
        break;
      case GO_EDITCREDENTIALS_FRAGMENT:
        resetProfileName();
        buildEditCredentialsFragment(Constants.STATE_EDIT_CREDENTIALS);
        setState(Constants.STATE_EDIT_CREDENTIALS);
        destination = -1;
        break;
    }
  }

  @Override
  public void cancelNoChanges()
  {
    dialogOpen = false;
    destination = -1;
  }

  public void showCancelChangesDialog(int destination)
  {
    if (dialogOpen)
      return;

    CancelChangesDialog dialog = new CancelChangesDialog();
    dialog.show(getSupportFragmentManager(), "cancelChangesDialog");
    dialogOpen = true;
    this.destination = destination;
  }
  // End Override Cancel Changes Dialog //

  /*=====================*
   *  Set state emitter  *
   *=====================*/
  public void setState(int state)
  {
    mStateViewModel.setFragmentState(state);
  }

  /*===================*
   *  BUILD FRAGMENTS  *
   *===================*/
  private Observer<StateEmitter> getEmitterObserver()
  {
    return stateEmitter ->
    {
      if (stateEmitter == null)
        return;

      switch (stateEmitter.getFragmentState())
      {
        case Constants.STATE_HOME:
          if (destination == GO_HOME_FRAGMENT || fragmentVisible(mHomeFragment))
            return;
          buildHomeFrag(Constants.STATE_HOME);
          break;
        case Constants.STATE_DETAILS_FRAGMENT:
          buildSignupFrag(Constants.STATE_DETAILS_FRAGMENT);
          mNavUtility.setCheckedToFalse(R.id.nav_edit_info, navigationView);
          break;
        case Constants.STATE_RESTAURANT_FRAGMENT:
          buildRestaurantFrag(Constants.STATE_RESTAURANT_FRAGMENT);
          mNavUtility.setCheckedToFalse(R.id.nav_verify_restaurant, navigationView);
          break;
        case Constants.STATE_LOGIN:
          buildLoginFragment(Constants.STATE_LOGIN);
          break;
        case Constants.STATE_RESET:
          buildResetFragment(Constants.STATE_RESET);
          break;
        case Constants.STATE_EDIT_PROFILE:
          goEditProfile();
          break;
        case Constants.STATE_EDIT_CREDENTIALS:
          goEditCredentials();
          break;
        case Constants.STATE_LOGGED_IN:
          break;
        case Constants.STATE_LOGGED_OUT:
          mNavUtility.setCheckedToFalse(R.id.nav_logout, navigationView);
          break;
      }
      mNavUtility.cleanUpMenu(navigationView);
    };
  }

  public void buildEditCredentialsFragment(int state)
  {
    appState = state;

    if (fragmentVisible(mEditCredentialsFragment))
      return;

    if (mEditCredentialsFragment == null)
      mEditCredentialsFragment = new EditCredentialsFragment();

    replaceFragment(mEditCredentialsFragment);
    setActionBarForEditCredentials();
  }

  public void buildEditProfileFragment(int state)
  {
    appState = state;

    if (fragmentVisible(mEditProfileFragment))
      return;

    if (mEditProfileFragment == null)
      mEditProfileFragment = new EditProfileFragment();

    replaceFragment(mEditProfileFragment);
    setActionBarForEditProfile();
  }

  public void buildHomeFrag(int state)
  {
    appState = state;

    if (fragmentVisible(mHomeFragment))
      return;

    if (mHomeFragment == null)
      mHomeFragment = new HomeFragment();

    replaceFragment(mHomeFragment);
    setActionBar();
  }

  public void buildLoginFragment(int state)
  {
    appState = state;

    if (fragmentVisible(mLoginFragment))
      return;

    if (mLoginFragment == null)
      mLoginFragment = new MainLoginFragment();

    replaceFragment(mLoginFragment);
    hideActionBar();
  }

  public void buildResetFragment(int state)
  {
    appState = state;

    if (fragmentVisible(mResetFragment))
      return;

    if (mResetFragment == null)
      mResetFragment = new ResetFragment();

    replaceFragment(mResetFragment);
    hideActionBar();
  }

  public void buildRestaurantFrag(int state)
  {
    appState = state;

    if (fragmentVisible(mRestaurantFragment))
      return;

    if (mRestaurantFragment == null)
      mRestaurantFragment = new RestaurantCodeFragment();
    replaceFragment(mRestaurantFragment);
    setActionBarForFragment();
  }

  public void buildSignupFrag(int state)
  {
    appState = state;

    if (fragmentVisible(mSignupFragment))
      return;

    if (mSignupFragment == null)
      mSignupFragment = new SignupFragment();

    replaceFragment(mSignupFragment);
    hideActionBar();
  }

  public void replaceFragment(Fragment fragment)
  {
    getSupportFragmentManager()
    .beginTransaction()
    .replace(R.id.main_frame_layout, fragment).commit();
  }

  public boolean fragmentVisible(Fragment fragment)
  {
    return fragment != null && fragment.isVisible();
  }
  // END BUILD FRAGMENTS //

  /*================================*
   *  AUTH STATE AND USER LISTENER  *
   *================================*/
  public void addAuthListener()
  {
    if (mAuth.doesHaveListener())
      return;

    authStateListener = firebaseAuth ->
    {
      if (mAuth.user() != null) {
        mLoginDataViewModel.clear();
        enableNotifications();

        final String uid = mAuth.user().getUid();
        final DocumentReference userRef = mFirestore.getUsers().document(uid);
        final List<FirestoreChannel> channels = new ArrayList<>();

        addUserListener(userRef);

        FirebaseInstanceId.getInstance().getInstanceId()
        .onSuccessTask(id ->  mFirestore.updateInstanceId(userRef, Objects.requireNonNull(id).getToken()))
        .continueWithTask(ignore -> userRef.get())
        .onSuccessTask(snapshot -> mFirestore.doGetAdminChannelReference(snapshot))
        .onSuccessTask(channelsSnapshot -> mFirestore.subscribeUserToChannels(channelsSnapshot, channels, uid))
        .addOnSuccessListener(l -> mUserChannelViewModel.setChannels(channels))
        .addOnFailureListener(e -> mUtility.logMessage(e.getMessage()));

        mUserViewModel.clear();
        mPasswordViewModel.setPassword("");

      } else {
        mNavUtility.isNotLoggedIn(navigationView);
        removeUserListener();
        disableNotifications();

        if (appState != Constants.STATE_RESET && appState != Constants.STATE_DETAILS_FRAGMENT)
          mStateViewModel.setFragmentState(Constants.STATE_LOGIN);
      }
    };
    mAuth.setAuthListener(authStateListener);
    mAuth.setHasListener(true);
  }

  public void addUserListener(DocumentReference documentReference)
  {
    if (firestoreUserListener != null)
      return;

    firestoreUserListener = documentReference.addSnapshotListener(MetadataChanges.INCLUDE, (snapshot, e) ->
    {
      if (e != null)
      {
        showToast("Error retrieving this user reference: " + e.getMessage());
        mNavUtility.isNotLoggedIn(navigationView);
        return;
      }
      final User user = mFirestore.toFirestoreObject(snapshot, User.class);
      mAppUserViewModel.setmUser(user);
      toggleAdmin(user);
      toggleAdminOrgSet(user);
      toggleOrgIsSet(user);
    });
  }

   // Check User Info on UserListener //
  public void toggleOrgIsSet(User user)
  {
    if (user.get_organizationPushId() == null || user.get_organizationPushId().equals("")) {
      mNavUtility.isLoggedInNoRestaurantCode(navigationView);
      navigationView.getMenu().findItem(R.id.nav_channel).setVisible(false);
      navigationView.getMenu().findItem(R.id.nav_verify_restaurant).setVisible(true);

    } else if (user.get_organizationPushId() != null && !user.get_organizationPushId().equals("")) {
      mNavUtility.isLoggedInHasRestaurantCode(navigationView);
      navigationView.getMenu().findItem(R.id.nav_channel).setVisible(true);
      navigationView.getMenu().findItem(R.id.nav_verify_restaurant).setVisible(false);

    }
  }

  public void toggleAdminOrgSet(User user)
  {
    if (user.get_organizationCode() != null && !user.get_organizationCode().equals("") || !user.get_isAdmin()) {
      navigationView.getMenu().findItem(R.id.nav_admin).setVisible(false);

    } else {
      navigationView.getMenu().findItem(R.id.nav_admin).setVisible(true);

    }
  }

  public void toggleAdmin(User user)
  {
    if (user != null && user.get_isAdmin()) {
      mNavUtility.isAdmin(navigationView);

    } else if (user != null && !user.get_isAdmin()) {
      mNavUtility.isNotAdmin(navigationView);

    } else {
      mNavUtility.isNotAdmin(navigationView);

    }
    toggleOrgIsSet(user);
  }

  public void removeUserListener()
  {
    if (firestoreUserListener != null)
    {
      firestoreUserListener.remove();
      firestoreUserListener = null;
    }
  }

  public void removeAuthListener()
  {
    if (mAuth.doesHaveListener())
    {
      mAuth.removeAuthListener(authStateListener);
      mAuth.setHasListener(false);
    }
  }
  // END LISTENERS //

  /*=======================*
   *  TOP LEVEL LISTENERS  *
   *=======================*/
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
  public void onResume()
  {
    super.onResume();
    mNavUtility.cleanUpMenu(navigationView);
    addAuthListener();
    mUtility.observeViewModel(this, mStateViewModel.getStateEmitter(), getEmitterObserver());
  }

  @Override
  public void onPause()
  {
    super.onPause();
    removeAuthListener();
    removeUserListener();
    mStateViewModel.getStateEmitter().removeObserver(getEmitterObserver());
  }


  public void getValuesFromBundle(Bundle inState)
  {
    if (inState == null)
      return;

    if (inState.keySet().contains(Constants.EXTRA_APP_STATE))
    {
      appState = inState.getInt(Constants.EXTRA_APP_STATE);
      destination = inState.getInt(EXTRA_DESTINATION);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putInt(Constants.EXTRA_APP_STATE, appState);
    outState.putInt(EXTRA_DESTINATION, destination);
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
  }

  @Override
  public void onBackPressed()
  {
    if (appState == Constants.STATE_DETAILS_FRAGMENT) {
      setState(Constants.STATE_LOGIN);

    } else if (appState == Constants.STATE_HOME) {
      super.onBackPressed();

    } else if (appState == Constants.STATE_LOGIN) {
      super.onBackPressed();

    } else if (appState == Constants.STATE_RESTAURANT_FRAGMENT) {
      mOrgViewModel.clear();
      setState(Constants.STATE_HOME);

    } else if (appState == Constants.STATE_RESET) {
      setState(Constants.STATE_LOGIN);

    } else if (appState == Constants.STATE_EDIT_PROFILE) {
      if (profileChangedOnClick()) {
        destination = 0;
        showCancelChangesDialog(0);

      } else {
        destination = -1;
        setState(Constants.STATE_HOME);
        clearProfile();
      }
    } else if (appState == Constants.STATE_EDIT_CREDENTIALS) {

      if (credentialsChangedOnGoProfile()) {
        destination = 1;
        showCancelChangesDialog(1);

      } else {
        setState(Constants.STATE_EDIT_PROFILE);
      }

    } else {
      showToast("uncaught state: " + appState);
    }
  }
  // End Top Level Overrides //

  /*==============*
   *    Utility   *
   *==============*/
  void showToast(CharSequence message)
  {
    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
  }

  public void toast(String message)
  {
    showToast(message);
  }
}
