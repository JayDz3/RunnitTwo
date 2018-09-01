package com.idesign.runnit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.Adapters.SubscribedUserAdapter;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.VIewModels.SubscribedUsersViewModel;
import com.idesign.runnit.VIewModels.UserChannelsViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChannelUsers extends AppCompatActivity implements SubscribedUserAdapter.SubscribedUserAdapterListener
{
  private final BaseFirestore mFirestore = new BaseFirestore();

  private TextView groupName;
  private TextView clickUser;
  private TextView noUsers;

  private final String CHANNEL_ID = "channel_id";
  private final String ORG_PUSHID = "org_pushid";
  private String _channelId;
  private String _orgPushId;

  private RecyclerView mRecyclerView;
  private SubscribedUserAdapter mAdapter;

  private SubscribedUsersViewModel mSubscribedUsersViewModel;

  private ListenerRegistration userListener;
  private int PRIMARY;
  private int DARK_GREY;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_channel_users);

   final Intent intent = getIntent();
    _channelId = intent.getStringExtra(CHANNEL_ID);
    _orgPushId = intent.getStringExtra(ORG_PUSHID);
    mSubscribedUsersViewModel = ViewModelProviders.of(this).get(SubscribedUsersViewModel.class);
    DARK_GREY = ContextCompat.getColor(this, R.color.colorDarkGray);
    PRIMARY = ContextCompat.getColor(this, R.color.colorPrimary);

    setView(_channelId);
    setRecyclerView();
  }

  private Observer<List<User>> usersObserver()
  {
    return users -> mAdapter.setUsers(users);
  }

  public void setView(String channelId)
  {
    groupName = findViewById(R.id.channel_users_group_name);
    groupName.setText(channelId);
    noUsers = findViewById(R.id.channel_users_activity_no_users);
    clickUser = findViewById(R.id.channel_users_activity_header);
  }

  public void setRecyclerView()
  {
    final List<User> users = new ArrayList<>();
    mRecyclerView = findViewById(R.id.channel_users_activity_recycler_view);
    mAdapter = new SubscribedUserAdapter(users, _channelId, _orgPushId, ChannelUsers.this, PRIMARY, DARK_GREY);
    DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerView.addItemDecoration(itemDecoration);
    mRecyclerView.setAdapter(mAdapter);
  }

  public void onSuccess(final String firstname, final String lastname)
  {
    showToast("Message Sent to" + " " + firstname + " " + lastname);
  }

  public void onFailure(final String errorMessage)
  {
    showToast("Error: " + errorMessage);
  }

  public void getSubscribedUsers(final String orgPushId, final String channelId, final List<User> users)
  {
    final DocumentReference channelRef = mFirestore.getAdminChannelsReference(orgPushId).document(channelId);
    final CollectionReference subscribedUsersRef = mFirestore.subscribedUsersReference(channelRef);
    if (userListener != null)
    {
      return;
    }
    userListener = subscribedUsersRef.addSnapshotListener(((querySnapshot, e) -> {
      if (e != null)
      {
        noUsers.setVisibility(View.VISIBLE);
        clickUser.setVisibility(View.GONE);
        showToast("error: " + e.getMessage());
        return;
      }
      getUsers(querySnapshot, users)
      .addOnSuccessListener(l -> mSubscribedUsersViewModel.setUsers(users));
    }));
  }

  public Task<DocumentSnapshot> getUsers(QuerySnapshot snapshot, List<User> users)
  {
    Task<DocumentSnapshot> task = Tasks.forResult(null);
    final List<String> userIds = new ArrayList<>();
    if (snapshot.isEmpty()) {
      noUsers.setVisibility(View.VISIBLE);
      clickUser.setVisibility(View.GONE);
    } else {
      noUsers.setVisibility(View.GONE);
      clickUser.setVisibility(View.VISIBLE);
    }
    for (DocumentSnapshot ds : snapshot)
    {
      final String userId = ds.getId();
      userIds.add(userId);
    }
    for (String id : userIds)
    {
      task = task.continueWithTask(ignore -> mFirestore.getUsers().document(id).get()
      .addOnSuccessListener(l -> {
        final User user = mFirestore.toFirestoreObject(l, User.class);
        users.add(user);
      }));
    }
    return task;
  }

  public void removeListener()
  {
    if (userListener != null)
    {
      userListener.remove();
      userListener = null;
    }
  }


  public void showToast(String message)
  {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

   public void logMessage(String message)
  {
    Log.d("Admin ACTIVITY: ", "message: " + message);
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
    final List<User> users = new ArrayList<>();
    getSubscribedUsers(_orgPushId, _channelId, users);
    mSubscribedUsersViewModel.getUsers().observe(this, usersObserver());
  }

  @Override
  public void onPause()
  {
    super.onPause();
    removeListener();
    mSubscribedUsersViewModel.getUsers().removeObserver(usersObserver());
  }

}
