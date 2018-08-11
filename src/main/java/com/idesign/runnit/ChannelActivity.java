package com.idesign.runnit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import android.view.View;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.Adapters.ChannelAdapter;
import com.idesign.runnit.Dialogs.NewChannelDialog;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.FirestoreChannel;

import com.idesign.runnit.Items.User;
import com.idesign.runnit.VIewModels.AdminChannelViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChannelActivity extends AppCompatActivity implements
  ChannelAdapter.AdminChannelAdapterListener,
  NewChannelDialog.ChannelDialogListener

{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private RecyclerView mRecyclerView;
  private ChannelAdapter mAdapter;

  private ProgressBar progressBar;
  private FloatingActionButton fab;

  private TextView noChannelView;

  private int WHITE;
  private int DARK_GREY;

  private AdminChannelViewModel mAdminChannelViewModel;
  private ListenerRegistration channelListener;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_channel);

    mRecyclerView = findViewById(R.id.channel_activity_admin_recycler_view);
    progressBar = findViewById(R.id.channel_activity_admin_progress_bar);
    noChannelView = findViewById(R.id.channel_activity_admin_no_channels);
    fab = findViewById(R.id.channel_activity_admin_fab);

    DARK_GREY = ContextCompat.getColor(this, R.color.colorDarkGray);
    WHITE = ContextCompat.getColor(this, R.color.colorWhite);

    final List<FirestoreChannel> channelsOnCreate = new ArrayList<>();
    mAdapter = new ChannelAdapter(channelsOnCreate, this, ChannelActivity.this);

    fab.setOnClickListener(l -> showDialog());
    fab.setClickable(false);
    fab.setEnabled(false);

    DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerView.addItemDecoration(itemDecoration);
    mRecyclerView.setAdapter(mAdapter);
    mAdminChannelViewModel = ViewModelProviders.of(this).get(AdminChannelViewModel.class);

    disableButton();
    progressBar.setVisibility(View.GONE);
    noChannelView.setVisibility(View.GONE);
  }

  private Observer<List<FirestoreChannel>> observer()
  {
    return channels -> mAdapter.setItems(channels);
  }

  public void setListener()
  {
    if (channelListener != null)
    {
      return;
    }

    disableButton();
    final String uid = mAuth.user().getUid();
    mFirestore.getUsers().document(uid).get()
    .addOnSuccessListener(userSnap ->
    {
      final User user = mFirestore.toFirestoreObject(userSnap, User.class);
      final String orgpushId = user.get_organizationPushId();
      channelListener = mFirestore.getAdminChannelsReference(orgpushId).addSnapshotListener(((querySnapshot, e) ->
      {
        if (e != null)
        {
          handleSnapshotError(e);
          return;
        }
        addChannelsFromListener(querySnapshot);
      }));
      enableButton();
    })
    .addOnFailureListener(err -> showToast("error setting listener: " + err.getMessage()));
    enableButton();
  }

  /*
   *  Handle Channel Listener Actions
   */
  public void handleSnapshotError(FirebaseException e)
  {
    showToast("error getting channels from database: " + e.getMessage());
    noChannelView.setVisibility(View.VISIBLE);
  }

  public void addChannelsFromListener(QuerySnapshot channelSnapshot)
  {
    if (channelSnapshot != null)
    {
      final List<FirestoreChannel> channels = new ArrayList<>();
      for (DocumentSnapshot ds : channelSnapshot.getDocuments())
      {
        final FirestoreChannel channel = mFirestore.toFirestoreObject(ds, FirestoreChannel.class);
        channels.add(channel);
      }
      toggleNoChannelView(channels);
      mAdminChannelViewModel.setChannels(channels);
    }
  }
  // {End Handle ChannelListener Actions] //

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

    } else {
      noChannelView.setVisibility(View.GONE);
    }
  }
  // [ End Listener ] //

  public void showDialog()
  {
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
    disableButton();
    final String uid = mAuth.user().getUid();
    mFirestore.getUsers().document(uid).get()
    .onSuccessTask(userRef ->
    {
      final User user = mFirestore.toFirestoreObject(userRef, User.class);
      final String orgPushid = user.get_organizationPushId();
      return mFirestore.getOrgSnapshotTask(orgPushid);
    })
    .onSuccessTask(orgSnapshot ->
    {
      Task<Void> task = Tasks.forResult(null);
      if (orgSnapshot == null)
      {
        return task;
      }
      final String orgPushid = orgSnapshot.getId();
      final List<FirestoreChannel> channels = new ArrayList<>();
      final String trimmed = name.trim();
      for (FirestoreChannel c : mAdapter.getItems())
      {
        if (c.get_channelId().equalsIgnoreCase(trimmed))
        {
          channels.add(c);
        }
      }
      if (channels.size() == 0) {
        fillIntent(name);
        task = mFirestore.addChannelAdmin(orgSnapshot.getReference(), orgPushid, name);

      } else {
        showToast("A channel with that name already exists...");
      }
      return task;
    })
    .addOnSuccessListener(l -> enableButton())
    .addOnFailureListener(e ->
    {
      showToast("error adding channel: " + e.getMessage());
      enableButton();
    });
  }

  public void disableButton()
  {
    progressBar.setVisibility(View.VISIBLE);
    fab.getDrawable().setTint(DARK_GREY);
    fab.setEnabled(false);
    fab.setClickable(false);
  }

  public void enableButton()
  {
    progressBar.setVisibility(View.GONE);
    fab.getDrawable().setTint(WHITE);
    fab.setEnabled(true);
    fab.setClickable(true);
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
  }

  @Override
  public void onResume()
  {
    super.onResume();
    setListener();
    mAdminChannelViewModel.getChannels().observe(this, observer());
  }

  public String trimmedString(String source)
  {
    return source.trim();
  }

  public void showToast(CharSequence message)
  {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
