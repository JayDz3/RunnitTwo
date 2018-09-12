package com.idesign.runnit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;

import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;

import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Fragments.HomeFragment;
import com.idesign.runnit.Fragments.RestaurantCodeFragment;
import com.idesign.runnit.Fragments.SignupFragment;

import com.idesign.runnit.Items.FirestoreChannel;
import com.idesign.runnit.Items.StateEmitter;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.NavigationHelpers.NavigationViewUtility;
import com.idesign.runnit.VIewModels.AppUserViewModel;
import com.idesign.runnit.VIewModels.PasswordViewModel;
import com.idesign.runnit.VIewModels.StateViewModel;
import com.idesign.runnit.VIewModels.UserChannelsViewModel;
import com.idesign.runnit.VIewModels.UserViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity
{
  // Classes
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();
  private final NavigationViewUtility mNavUtility = new NavigationViewUtility();

  // Layouts
  private ConstraintLayout mainLayout;
  private NavigationView navigationView;
  private DrawerLayout mDrawerLayout;

  private Snackbar snackbar;
  private Toolbar toolbar;
  private ActionBar actionBar;

  // View models
  private StateViewModel mStateViewModel;
  private UserViewModel mUserViewModel;
  private PasswordViewModel mPasswordViewModel;
  private AppUserViewModel mAppUserViewModel;
  private UserChannelsViewModel mUserChannelViewModel;

  // Fragments
  private HomeFragment mHomeFragment;
  private SignupFragment mSignupFragment;
  private RestaurantCodeFragment mRestaurantFragment;

  // Listeners
  private FirebaseAuth.AuthStateListener authStateListener;
  private ListenerRegistration firestoreUserListener;

  private String selection;
  private int appState = 0;
  private boolean disabled = false;

  private ProgressBar progressBar;

  private final String COLLECTION_SUBSCRIBED_USERS = "SubscribedUsers";

  private final String ORG_PUSHID = "org_pushid";
  private final String USER_UID = "user_uid";

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
    toggleViewOnStart();
  }

  public void setViewItems()
  {
    mainLayout = findViewById(R.id.main_constraint_layout);
    mDrawerLayout = findViewById(R.id.drawer_layout);
    navigationView = findViewById(R.id.nav_view);
    toolbar = findViewById(R.id.toolbar);
    progressBar = findViewById(R.id.main_activity_progress_bar);
    progressBar.setVisibility(View.GONE);
  }

  public void setViewModels()
  {
    mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
    mAppUserViewModel = ViewModelProviders.of(this).get(AppUserViewModel.class);
    mStateViewModel = ViewModelProviders.of(this).get(StateViewModel.class);
    mPasswordViewModel = ViewModelProviders.of(this).get(PasswordViewModel.class);
    mUserChannelViewModel = ViewModelProviders.of(this).get(UserChannelsViewModel.class);
  }

  /*
   *  Only occurs onCreate
   */
  public void toggleViewOnStart()
  {
    switch (appState)
    {
      case Constants.STATE_HOME:
        buildHomeFrag(Constants.STATE_HOME);
        break;
      case Constants.STATE_DETAILS_FRAGMENT:
        buildSignupFrag(Constants.STATE_DETAILS_FRAGMENT);
        break;
      case Constants.STATE_RESTAURANT_FRAGMENT:
        buildRestaurantFrag(Constants.STATE_RESTAURANT_FRAGMENT);
        break;
      default:
        logMessage("nada");
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if (disabled)
    {
      return false;
    }
    switch (item.getItemId())
    {
      case android.R.id.home:
        mDrawerLayout.openDrawer(GravityCompat.START);
    }
    return super.onOptionsItemSelected(item);
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
        {
          return;
        }
        switch (selection)
        {
          case "Login":
            goLogin();
            break;
          case "New Account":
            mStateViewModel.setFragmentState(Constants.STATE_DETAILS_FRAGMENT);
            break;
          case "Logout":
            logout();
            break;
          case "Edit Info":
            mStateViewModel.setFragmentState(Constants.STATE_DETAILS_FRAGMENT);
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
          default:
            Log.d("DRAWER LISTENER", "default");
            break;
        }
        selection = null;
      }
      public void onDrawerStateChanged(int newState) { }
    });
  }

  /*
   *  LOGIN / LOGOUT
   *
   *  @ Other nav actions
   */
  public void goLogin()
  {
    if (mAuth.user() != null)
    {
      return;
    }
    Intent intent = new Intent(this, LoginActivity.class);
    startActivity(intent);
  }

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
    showToast("Clocked out!");
    mStateViewModel.setFragmentState(Constants.STATE_LOGGED_OUT);
    disabled = false;
  }

  public void onLogoutFailure(Exception e)
  {
    mNavUtility.isNotLoggedIn(navigationView);
    progressBar.setVisibility(View.GONE);
    showToast("error: " + e.getMessage() + " You are however, signed out");
    mStateViewModel.setFragmentState(Constants.STATE_LOGGED_OUT);
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
    final boolean mIsAdmin = mAppUserViewModel.getmUser().getValue().get_isAdmin();

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
    final boolean mIsAdmin = mAppUserViewModel.getmUser().getValue().get_isAdmin();
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

  // Listener for navigation drawer on item selected
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

  private Observer<StateEmitter> getEmitterObserver()
  {
    return stateEmitter ->
    {
     if (stateEmitter == null)
     {
       return;
     }
     switch (stateEmitter.getFragmentState())
     {
       case Constants.STATE_HOME:
         buildHomeFrag(Constants.STATE_HOME);
         break;
       case Constants.STATE_DETAILS_FRAGMENT:
         buildSignupFrag(Constants.STATE_DETAILS_FRAGMENT);
         mNavUtility.setCheckedToFalse(R.id.nav_signup, navigationView);
         mNavUtility.setCheckedToFalse(R.id.nav_edit_info, navigationView);
         break;
       case Constants.STATE_RESTAURANT_FRAGMENT:
         buildRestaurantFrag(Constants.STATE_RESTAURANT_FRAGMENT);
         mNavUtility.setCheckedToFalse(R.id.nav_verify_restaurant, navigationView);
         break;
       case Constants.STATE_LOGGED_IN:
         mNavUtility.setCheckedToFalse(R.id.nav_login, navigationView);
         break;
       case Constants.STATE_LOGGED_OUT:
         mNavUtility.setCheckedToFalse(R.id.nav_logout, navigationView);
         break;
     }
     mNavUtility.cleanUpMenu(navigationView);
     };
  }

  public void setActionBar()
  {
    setSupportActionBar(toolbar);
    actionBar = getSupportActionBar();
    if (actionBar != null)
    {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_primary_24dp);
    }
  }

  public void setActionBarForFragment()
  {
    toolbar.setNavigationOnClickListener(l -> setState(Constants.STATE_HOME));
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp);
  }

  /*
   *  Set state emitter
   */
  public void setState(int state)
  {
    mStateViewModel.setFragmentState(state);
  }

  /*
   *  Fragment Transactions
   */
  public void buildHomeFrag(int state)
  {
    appState = state;
    if (fragmentVisible(mHomeFragment))
    {
      return;
    }
    if (mHomeFragment == null)
    {
      mHomeFragment = new HomeFragment();
    }
    addDrawerListener();
    final int transition = FragmentTransaction.TRANSIT_FRAGMENT_FADE;
    replaceFragment(mHomeFragment, transition);
    setActionBar();
  }

  public void buildRestaurantFrag(int state)
  {
    appState = state;
    if (fragmentVisible(mRestaurantFragment))
    {
      return;
    }
    if (mRestaurantFragment == null)
    {
      mRestaurantFragment = new RestaurantCodeFragment();
    }
    replaceFragment(mRestaurantFragment);
    setActionBarForFragment();
  }

  public void buildSignupFrag(int state)
  {
    appState = state;
    if (fragmentVisible(mSignupFragment))
    {
      return;
    }
    if (mSignupFragment == null) {
      mSignupFragment = new SignupFragment();
    }
    replaceFragment(mSignupFragment);
    setActionBarForFragment();
  }

  public boolean fragmentVisible(Fragment fragment)
  {
    return fragment != null && fragment.isVisible();
  }

  public void replaceFragment(Fragment fragment)
  {
    getSupportFragmentManager()
    .beginTransaction()
    .replace(R.id.main_frame_layout, fragment).commit();
  }

  public void replaceFragment(Fragment fragment, int transition)
  {
    getSupportFragmentManager()
    .beginTransaction()
    .setTransition(transition)
    .replace(R.id.main_frame_layout, fragment).commit();
  }
  public void addAuthListener()
  {
    if (mAuth.doesHaveListener())
    {
      return;
    }
    authStateListener = firebaseAuth ->
    {
      if (mAuth.user() != null) {
        enableNotifications();
        final String uid = mAuth.user().getUid();
        final DocumentReference userRef = mFirestore.getUsers().document(uid);
        final List<FirestoreChannel> channels = new ArrayList<>();

        addUserListener(userRef);
        FirebaseInstanceId.getInstance().getInstanceId()
        .onSuccessTask(id ->  mFirestore.updateInstanceId(userRef, Objects.requireNonNull(id).getToken()))
        .continueWithTask(ignore -> userRef.get())
        .onSuccessTask(userSnapshot ->
        {
          final User user = mFirestore.toFirestoreObject(userSnapshot, User.class);
          final String orgPushid = user.get_organizationPushId();
          if (orgPushid.equals(""))
          {
            throw new RuntimeException("Please set your organization code");
          }
          return mFirestore.getAdminChannelsReference(orgPushid).get();
        })
        // channelsSnapshot is all channels belonging to org
        // if user exists on channel, they are updated as logged in to that channels collectionRef of SubscribedUsers
        .onSuccessTask(channelsSnapshot -> subscribeUserToChannels(channelsSnapshot, channels, uid))
        .addOnSuccessListener(l -> mUserChannelViewModel.setChannels(channels))
        .addOnFailureListener(e -> showToast("error: 510 " + e.getMessage()));
        mUserViewModel.clear();
        mPasswordViewModel.setPassword("");

      } else {
        mNavUtility.isNotLoggedIn(navigationView);
        removeUserListener();
        disableNotifications();

      }
    };
    mAuth.setAuthListener(authStateListener);
    mAuth.setHasListener(true);
  }

  /*
   *  Update info on user login / authListener
   *
   *  @param channelsSnapshot : All channels belonging to organization
   *
   *  @param channels : Empty list of channels populated here. Then added to userChannelsViewModel
   *
   *  @param uid : user uid
   */
  public Task<DocumentSnapshot> subscribeUserToChannels(QuerySnapshot channelsSnapshot, List<FirestoreChannel> channels, String uid)
  {
    if (channelsSnapshot == null)
    {
      throw new RuntimeException("no channels");
    }

    Task<DocumentSnapshot> task = Tasks.forResult(null);
    for (DocumentSnapshot ds : channelsSnapshot.getDocuments())
    {
      final DocumentReference subscribedUserRef = ds.getReference().collection(COLLECTION_SUBSCRIBED_USERS).document(uid);
      task = task.continueWithTask(ignore -> subscribedUserRef.get().addOnSuccessListener(documentSnapshot ->
      {
        if (documentSnapshot.exists())
        {
          final FirestoreChannel channel = mFirestore.toFirestoreObject(ds, FirestoreChannel.class);
          channels.add(channel);
          mFirestore.updateSubscribedUserTask(subscribedUserRef, true);
        }
      }));
    }
    return task;
  }

  public void removeAuthListener()
  {
    if (mAuth.doesHaveListener())
    {
      mAuth.removeAuthListener(authStateListener);
      mAuth.setHasListener(false);
    }
  }

  public void addUserListener(DocumentReference documentReference)
  {

    if (firestoreUserListener != null)
    {
      return;
    }
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
    mStateViewModel.getStateEmitter().observe(this, getEmitterObserver());
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
    {
      return;
    }
    if (inState.keySet().contains(Constants.EXTRA_APP_STATE))
    {
      appState = inState.getInt(Constants.EXTRA_APP_STATE);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putInt(Constants.EXTRA_APP_STATE, appState);
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
      setState(Constants.STATE_HOME);

    } else if (appState == Constants.STATE_RESTAURANT_FRAGMENT) {
      setState(Constants.STATE_HOME);

    } else if (appState == Constants.STATE_HOME) {
      super.onBackPressed();

    } else {
      showToast("uncaught state: " + appState);
    }
  }

  /*==============*
   *    Utility   *
   *==============*/
  void showToast(CharSequence message)
  {
    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
  }

  void showSnackbar(CharSequence message)
  {
   snackbar = Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG);
   snackbar.show();
  }

  public void logMessage(String message)
  {
    Log.d("MAIN ACTIVITY: ", "message: " + message);
  }
}
