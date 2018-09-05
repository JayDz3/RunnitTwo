package com.idesign.runnit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.Adapters.SubscribedUserAdapter;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.Items.SubscribedUser;
import com.idesign.runnit.VIewModels.SubscribedUsersViewModel;

import java.util.ArrayList;
import java.util.Collections;
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

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_channel_users);
    getValuesFromIntent();
    mSubscribedUsersViewModel = ViewModelProviders.of(this).get(SubscribedUsersViewModel.class);
    setView(_channelId);
    setRecyclerView();
  }

  public void getValuesFromIntent()
  {
    final Intent intent = getIntent();
    _channelId = intent.getStringExtra(CHANNEL_ID);
    _orgPushId = intent.getStringExtra(ORG_PUSHID);
  }

  private Observer<List<SubscribedUser>> usersObserver()
  {
    return users ->
    {
      Collections.sort(users, (a, b) -> a.get_lastName().compareToIgnoreCase(b.get_lastName()));
      mAdapter.setUsers(users);
    };
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
    final List<SubscribedUser> users = new ArrayList<>();
    mRecyclerView = findViewById(R.id.channel_users_activity_recycler_view);
    mAdapter = new SubscribedUserAdapter(users, _channelId, _orgPushId, ChannelUsers.this);
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

  public void setUsersListener(final String orgPushId, final String channelId)
  {
    final DocumentReference channelRef = mFirestore.getAdminChannelsReference(orgPushId).document(channelId);
    final CollectionReference subscribedUsersRef = mFirestore.subscribedUsersReference(channelRef);
    if (userListener != null)
    {
      return;
    }
    userListener = subscribedUsersRef.addSnapshotListener(((querySnapshot, e) ->
    {
      if (e != null)
      {
        noUsers.setVisibility(View.VISIBLE);
        clickUser.setVisibility(View.GONE);
        showToast("error: " + e.getMessage());
        return;
      }
      if (querySnapshot == null)
      {
        showToast("No users subscribed, or an error has occured");
        noUsers.setVisibility(View.VISIBLE);
        clickUser.setVisibility(View.GONE);
        return;
      }
      setSubscribedUsersViewModel(querySnapshot);
    }));
  }

  public void setSubscribedUsersViewModel(QuerySnapshot querySnapshot)
  {
    final int size = mSubscribedUsersViewModel.setUsersFromSnapshots(querySnapshot);

    if (size == 0) {
      noUsers.setVisibility(View.VISIBLE);
      clickUser.setVisibility(View.GONE);

    } else {
      noUsers.setVisibility(View.GONE);
      clickUser.setVisibility(View.VISIBLE);
    }
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

  @Override
  public void onStart()
  {
    super.onStart();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mSubscribedUsersViewModel.getUsers().observe(this, usersObserver());
    setUsersListener(_orgPushId, _channelId);
  }

  @Override
  public void onPause()
  {
    super.onPause();
    removeListener();
    mSubscribedUsersViewModel.getUsers().removeObserver(usersObserver());
  }

}
