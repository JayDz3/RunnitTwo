package com.idesign.runnit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.ColorStateList;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import com.idesign.runnit.Adapters.ChannelAdapter;
import com.idesign.runnit.Dialogs.NewChannelDialog;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.FirestoreChannel;

import com.idesign.runnit.Items.User;
import com.idesign.runnit.VIewModels.AdminChannelViewModel;
import com.idesign.runnit.VIewModels.AppUserViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChannelActivity extends AppCompatActivity implements
  ChannelAdapter.AdminChannelAdapterListener,
  NewChannelDialog.ChannelDialogListener

{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private RecyclerView mRecyclerView;
  private EditText customMessageEditText;
  private ChannelAdapter mAdapter;
  private ProgressBar progressBar;
  private FloatingActionButton fab;

  private TextView noChannelView;
  private TextView leaveBlankText;

  private int PRIMARY;
  private int DARK_GREY;
  private int _open = -1;

  private final String EXTRA_OPEN = "extra_open";
  private final String EXTRA_MESSAGE = "extra_message";
  private final String CHANNEL_ID = "channel_id";
  private final String ORG_PUSHID = "org_pushid";

  private AdminChannelViewModel mAdminChannelViewModel;
  private AppUserViewModel mAppUserViewModel;

  private ListenerRegistration channelListener;
  private LinearLayoutManager mLayouManager;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_channel);
    setViewItems();
    getValuesFromBundle(savedInstanceState);
    setAdapterAndRecyclerView();

    DARK_GREY = ContextCompat.getColor(this, R.color.colorDarkGray);
    PRIMARY = ContextCompat.getColor(this, R.color.colorPrimary);

    fab.setOnClickListener(l -> showDialog());
    disableFab();

    mAdminChannelViewModel = ViewModelProviders.of(this).get(AdminChannelViewModel.class);
    mAppUserViewModel = ViewModelProviders.of(this).get(AppUserViewModel.class);

    noChannelView.setVisibility(View.GONE);

    if (savedInstanceState == null) {
      progressBar.setVisibility(View.VISIBLE);
      customMessageEditText.setVisibility(View.GONE);

    } else {
      progressBar.setVisibility(View.GONE);
    }
  }

  public void setViewItems()
  {
    mRecyclerView = findViewById(R.id.channel_activity_admin_recycler_view);
    progressBar = findViewById(R.id.channel_activity_admin_progress_bar);
    noChannelView = findViewById(R.id.channel_activity_admin_no_channels);
    fab = findViewById(R.id.channel_activity_admin_fab);
    customMessageEditText = findViewById(R.id.channel_activity_admin_custom_message);
    leaveBlankText = findViewById(R.id.channel_activity_admin_leave_blank);
    leaveBlankText.setOnClickListener(l -> clearMessage());

    customMessageEditText.setVisibility(View.GONE);
    leaveBlankText.setVisibility(View.GONE);
  }

  public void clearMessage()
  {
    customMessageEditText.setText("");
    mAdapter.setMessage("");
  }

  public String getMessage()
  {
    if (TextUtils.isEmpty(customMessageEditText.getText())) {
      return "";

    } else {
      return customMessageEditText.getText().toString();
    }
  }

  public void setMessage()
  {
    final String message = getMessage();
    mAdapter.setMessage(message);
  }

  public void setAdapterAndRecyclerView()
  {
    final List<FirestoreChannel> channelsOnCreate = new ArrayList<>();
    mAdapter = new ChannelAdapter(channelsOnCreate, this, ChannelActivity.this, _open, getMessage());
    DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
    mLayouManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayouManager);
    mRecyclerView.addItemDecoration(itemDecoration);
    mRecyclerView.setAdapter(mAdapter);
  }

  private Observer<List<FirestoreChannel>> observer()
  {
    return channels -> mAdapter.setItems(channels);
  }

  private Observer<User> userObserver()
  {
    return  user -> mAdapter.setUser(user);
  }

  public void setListener()
  {
    if (channelListener != null)
    {
      return;
    }
    disableFab();
    final String uid = mAuth.user().getUid();

    mFirestore.getUsers().document(uid).get()
    .addOnSuccessListener(userSnapshot ->
    {
      final User user = mFirestore.toFirestoreObject(userSnapshot, User.class);
      mAppUserViewModel.setmUser(user);

      final String mUserOrgpushid = user.get_organizationPushId();
      channelListener = mFirestore.getAdminChannelsReference(mUserOrgpushid).addSnapshotListener(((querySnapshot, e) ->
      {
        if (e != null)
        {
          showToast("error getting channels from database: " + e.getMessage());
          noChannelView.setVisibility(View.VISIBLE);
          /*
           *  enableButton() enable's fab as well
           */
          enableButton();
          return;
        }
        if (querySnapshot != null)
        {
          mAdminChannelViewModel.setChannelsFromSnapshot(querySnapshot);
          enableButton();
          toggleNoChannelView(mAdapter.getItems());
          scrollToOpenPosition();
        }
      }));
    });
  }

  // {End ChannelListener] //

  public void removeListener()
  {
    if (channelListener != null)
    {
      channelListener.remove();
      channelListener = null;
    }
  }

  public void toggleNoChannelView(List<FirestoreChannel> channels)
  {
    if (channels.size() == 0) {
      noChannelView.setVisibility(View.VISIBLE);
      customMessageEditText.setVisibility(View.GONE);
      leaveBlankText.setVisibility(View.GONE);

    } else {
      noChannelView.setVisibility(View.GONE);
      customMessageEditText.setVisibility(View.VISIBLE);
      leaveBlankText.setVisibility(View.VISIBLE);
    }
  }
  // [ End Listener ] //

  public void showDialog()
  {
    customMessageEditText.clearFocus();
    NewChannelDialog channelDialog = new NewChannelDialog();
    channelDialog.show(getSupportFragmentManager(), "NewChannelDialog");
  }

  /*
   *  Send Notification Intent
   *
   *  @param [name] Name of Channel to add
   */
  public void fillIntent(String name)
  {
    final String NOTIFICATION_ACTION_FILTER = "Notification_Action";
    final String NOTIFICATION_CHANNEL_ID = "channel_id";
    final Intent intent = new Intent(this, NotificationService.class);
    intent.setAction(NOTIFICATION_ACTION_FILTER);
    intent.putExtra(NOTIFICATION_CHANNEL_ID, name);
    sendBroadcast(intent);
  }

  public void addNewChannel(String name)
  {
    /*
     * disableButton() disable's fab as well
     */
    disableButton();
    final String orgPushid = mAppUserViewModel.getmUser().getValue().get_organizationPushId();
    final String upperCaseName = upperCaseFirstLetter(name);
    final String trimmed = upperCaseName.trim();

    mFirestore.getOrgSnapshotTask(orgPushid)
    .onSuccessTask(orgSnapshot ->
    {
      if (orgSnapshot == null)
      {
       throw new RuntimeException("org snapshot was null");
      }
      final List<FirestoreChannel> channels = new ArrayList<>();

      for (FirestoreChannel c : mAdapter.getItems())
      {
        if (c.get_channelId().equalsIgnoreCase(trimmed))
        {
          channels.add(c);
        }
      }
      if (channels.size() == 0) {
        return mFirestore.addChannelAdmin(orgSnapshot.getReference(), orgPushid, upperCaseName);

      } else {
        throw new RuntimeException("A channel with this name already exists");
      }
    })
    .addOnSuccessListener(l ->
    {
      fillIntent(upperCaseName);
      enableButton();
    })
    .addOnFailureListener(e ->
    {
      showToast(e.getMessage());
      enableButton();
    });
  }

  public void disableFab()
  {
    fab.setBackgroundTintList(ColorStateList.valueOf(DARK_GREY));
    fab.setEnabled(false);
    fab.setClickable(false);
  }

  public void enableFab()
  {
    fab.setBackgroundTintList(ColorStateList.valueOf(PRIMARY));
    fab.setEnabled(true);
    fab.setClickable(true);
  }

  public void disableButton()
  {
    progressBar.setVisibility(View.VISIBLE);
    disableFab();
  }


  public void enableButton()
  {
    progressBar.setVisibility(View.GONE);
    enableFab();
  }

  /*
   *  Channel Adapter listener
   */
  @Override
  public void disable()
  {
    disableButton();
  }

  @Override
  public void enable()
  {
    enableButton();
  }

  // store position of adapter item that has delete options open at the moment
  @Override
  public void setOpen(int open)
  {
    _open = open;
    scrollToOpenPosition();
  }


  public void scrollToOpenPosition()
  {
    if (_open > -1)
    {
      mLayouManager.scrollToPositionWithOffset(_open, 20);
      mAdapter.notifyDataSetChanged();
    }
  }

  public void getUsers(final String channelId, final String orgPushId)
  {
    Intent intent = new Intent(this, ChannelUsers.class);
    intent.putExtra(CHANNEL_ID, channelId);
    intent.putExtra(ORG_PUSHID, orgPushId);
    startActivity(intent);
    overridePendingTransition(R.transition.slide_up, R.transition.stay);
  }

  /*
   * Channel Dialog listener
   */
  @Override
  public void onConfirm(int which, String name)
  {
    if (name != null && !TextUtils.isEmpty(name)) {
      final String trimmed = trimmedString(name);
      addNewChannel(trimmed);

    } else {
      showToast("Channel name can not be empty");
    }
  }

  @Override
  public void onCancel(int which) { }

  /*
   * Top Level overrides
   */
  @Override
  public void onStart()
  {
    super.onStart();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    removeListener();
    mAdminChannelViewModel.getChannels().removeObserver(observer());
    mAppUserViewModel.getmUser().removeObserver(userObserver());
    mAppUserViewModel.clear();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mAdminChannelViewModel.getChannels().observe(this, observer());
    setListener();
    mAppUserViewModel.getmUser().observe(this, userObserver());
  }

  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putInt(EXTRA_OPEN, _open);
    outState.putString(EXTRA_MESSAGE, getMessage());
  }

  public void getValuesFromBundle(Bundle inState)
  {
    if (inState != null)
    {
      if (inState.keySet().contains(EXTRA_OPEN))
      {
        _open = inState.getInt(EXTRA_OPEN);
      }

      if (inState.keySet().contains(EXTRA_MESSAGE)) {
        final String message = inState.getString(EXTRA_MESSAGE);
        customMessageEditText.setText(message);

      } else {
        customMessageEditText.setText("");
      }
    }
  }

  public String trimmedString(String source)
  {
    return source.trim();
  }

  public String upperCaseFirstLetter(String source)
  {
    return source.substring(0, 1).toUpperCase() + source.substring(1);
  }

  public void showToast(CharSequence message)
  {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
