package com.idesign.runnit;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.Adapters.ChannelAdapter;
import com.idesign.runnit.Dialogs.NewChannelDialog;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.FirestoreChannel;
import com.idesign.runnit.Items.FirestoreOrg;
import com.idesign.runnit.Items.User;

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
  private ImageButton addChannelIcon;

  private int PRIMARY;
  private int DARK_GREY;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_channel);

    List<FirestoreChannel> channels = new ArrayList<>();
    mAdapter = new ChannelAdapter(channels, this, ChannelActivity.this);

    DARK_GREY = ContextCompat.getColor(this, R.color.colorDarkGray);
    PRIMARY = ContextCompat.getColor(this, R.color.colorPrimary);

    progressBar = findViewById(R.id.channel_activity_admin_progress_bar);
    progressBar.setVisibility(View.GONE);

    addChannelIcon = findViewById(R.id.channel_activity_admin_new_channel);
    addChannelIcon.setOnClickListener(l -> showDialog());
    addChannelIcon.setClickable(false);
    addChannelIcon.setEnabled(false);

    DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

    mRecyclerView = findViewById(R.id.channel_activity_admin_recycler_view);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerView.addItemDecoration(itemDecoration);
    mRecyclerView.setAdapter(mAdapter);
    disableButton();
    getItems()
    .addOnSuccessListener(query -> loadItems(query))
    .addOnFailureListener(e -> enableButton());
  }

  public void showDialog()
  {
    NewChannelDialog channelDialog = new NewChannelDialog();
    channelDialog.show(getSupportFragmentManager(), "NewChannelDialog");
  }

  public void fillIntent(String name)
  {
    final String NOTIFICATION_ACTION_FILTER = "Notification_Action";
    final String NOTIFICATION_CHANNEL_ID = "channel_id";
    Intent intent = new Intent(this, NotificationService.class);
    intent.setAction(NOTIFICATION_ACTION_FILTER);
    intent.putExtra(NOTIFICATION_CHANNEL_ID, name);
    sendBroadcast(intent);
  }

  public Task<QuerySnapshot> getItems()
  {
    final String uid = mAuth.user().getUid();
    return mFirestore.getUsers().document(uid).get()
    .onSuccessTask(userRef ->
    {
      final User user = mFirestore.toFirestoreObject(userRef, User.class);
      final String orgpushId = user.get_organizationPushId();
      return mFirestore.getOrgSnapshotTask(orgpushId);
    })
    .onSuccessTask(orgRef ->
    {
      /*
       *  May / will need error handlling / alternative if orgRef is null
       */
      final FirestoreOrg org = mFirestore.toFirestoreObject(orgRef, FirestoreOrg.class);
      return mFirestore.getAdminChannelsReference(org.getPushId()).get();
    });
  }

  public void loadItems(QuerySnapshot query)
  {
    final List<FirestoreChannel> getChannels = new ArrayList<>();
    for (DocumentSnapshot ds : query)
    {
      FirestoreChannel channel = mFirestore.toFirestoreObject(ds, FirestoreChannel.class);
      getChannels.add(channel);
    }
    mAdapter.setItems(getChannels);
    enableButton();
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
      if (orgSnapshot != null)
      {
        final FirestoreOrg org = mFirestore.toFirestoreObject(orgSnapshot, FirestoreOrg.class);
        final FirestoreChannel channel = new FirestoreChannel(name, org.getPushId(), name, true, false);
        final List<FirestoreChannel> channels = new ArrayList<>();
        for (FirestoreChannel c : mAdapter.getItems()) {
          if (c.get_channelId().equalsIgnoreCase(channel.get_channelId())) {
            channels.add(c);
          }
        }
        if (channels.size() == 0) {
          fillIntent(name);
          task = mFirestore.addChannelAdmin(orgSnapshot.getReference(), org.getPushId(), name);
        } else {
          showToast("A channel with that name already exists...");
        }
      }
      return task;
    })
    .onSuccessTask(ignore -> getItems())
    .addOnSuccessListener(query -> loadItems(query))
    .addOnFailureListener(e ->
    {
      showToast("error adding channel: " + e.getMessage());
      enableButton();
    });
  }

  public void disableButton()
  {
    progressBar.setVisibility(View.VISIBLE);
    addChannelIcon.getDrawable().setTint(DARK_GREY);
    addChannelIcon.setEnabled(false);
    addChannelIcon.setClickable(false);
  }

  public void enableButton()
  {
    progressBar.setVisibility(View.GONE);
    addChannelIcon.getDrawable().setTint(PRIMARY);
    addChannelIcon.setEnabled(true);
    addChannelIcon.setClickable(true);
  }

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

  @Override
  public void onConfirm(int which, String name)
  {
    if (name != null && !TextUtils.isEmpty(name)) {
      final String trimmed = trimmedString(name);
      addNewChannel(trimmed);
    }
    else {
      showToast("Channel name can not be empty");
    }
  }

  @Override
  public void onCancel(int which)
  {

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
