package com.idesign.runnit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
  private TextView noUsers;
  private EditText customMessageUsers;
  private ImageButton clearMessageButton;

  private final String EXTRA_MESSAGE = "extra_message";
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
    if (savedInstanceState != null)
    {
      getValuesFromBundle(savedInstanceState);
    }
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
    customMessageUsers = findViewById(R.id.channel_users_activity_custom_message);
    clearMessageButton = findViewById(R.id.channel_users_activity_clear_message);
    clearMessageButton.setOnClickListener(l -> clearMessage());
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
        noUsers();
        showToast("error: " + e.getMessage());
        return;
      }
      if (querySnapshot == null || querySnapshot.getDocuments().size() == 0)
      {
        noUsers();
        return;
      }
      setSubscribedUsersViewModel(querySnapshot);
    }));
  }

  public void noUsers()
  {
    noUsers.setVisibility(View.VISIBLE);
    customMessageUsers.setVisibility(View.GONE);
    clearMessageButton.setVisibility(View.GONE);
  }

  public void areUsers()
  {
    noUsers.setVisibility(View.GONE);
    customMessageUsers.setVisibility(View.VISIBLE);
    clearMessageButton.setVisibility(View.VISIBLE);
  }

  public void setSubscribedUsersViewModel(QuerySnapshot querySnapshot)
  {
    final int size = mSubscribedUsersViewModel.setUsersFromSnapshots(querySnapshot);

    if (size == 0) {
      noUsers();

    } else {
      areUsers();

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

  public String getMessage()
  {
    if (TextUtils.isEmpty(customMessageUsers.getText())) {
      return "";

    } else {
      return customMessageUsers.getText().toString();
    }
  }

  public void clearMessage()
  {
    customMessageUsers.setText("");
  }

  public void setMessage()
  {
    final String message = getMessage();
    mAdapter.setUserMessage(message);
  }

  public void showToast(String message)
  {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  public void getValuesFromBundle(Bundle savedInstanceState)
  {
    if (savedInstanceState.keySet().contains(EXTRA_MESSAGE))
    {
      final String message = savedInstanceState.getString(EXTRA_MESSAGE);
      customMessageUsers.setText(message);
    }
  }

  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putString(EXTRA_MESSAGE, getMessage());
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
