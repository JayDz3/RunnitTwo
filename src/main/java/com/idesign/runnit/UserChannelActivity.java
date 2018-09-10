package com.idesign.runnit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;
import com.idesign.runnit.Adapters.UserChannelAdapter;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.FirestoreChannel;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.Items.UserChannel;
import com.idesign.runnit.VIewModels.AdminChannelViewModel;
import com.idesign.runnit.VIewModels.AppUserViewModel;
import com.idesign.runnit.VIewModels.SimpleUserChannelsViewModel;

import java.util.ArrayList;
import java.util.List;

public class UserChannelActivity extends AppCompatActivity
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private UserChannelAdapter mAdapter;

  private AdminChannelViewModel mAdminChannelViewModel;
  private SimpleUserChannelsViewModel mUserChannelsViewModel;
  private AppUserViewModel mAppUserViewModel;

  private ListenerRegistration channelListener;
  private ListenerRegistration userChannelListener;

  private RecyclerView mRecyclerView;
  private ProgressBar progressBar;

  private TextView noChannelView;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_channel);
    setViewItems();
    setAdapterAndRecyclerView();

    mAdminChannelViewModel = ViewModelProviders.of(this).get(AdminChannelViewModel.class);
    mUserChannelsViewModel = ViewModelProviders.of(this).get(SimpleUserChannelsViewModel.class);
    mAppUserViewModel = ViewModelProviders.of(this).get(AppUserViewModel.class);

    progressBar.setVisibility(View.GONE);
    noChannelView.setVisibility(View.GONE);
    if (savedInstanceState == null)
    {
      progressBar.setVisibility(View.VISIBLE);
    }
  }

  public void setViewItems()
  {
    mRecyclerView = findViewById(R.id.channel_activity_user_recycler_view);
    noChannelView = findViewById(R.id.channel_activity_user_no_channels);
    progressBar = findViewById(R.id.channel_activity_user_progress_bar);
  }

  public void setAdapterAndRecyclerView()
  {
    final List<FirestoreChannel> empty = new ArrayList<>();
    mAdapter = new UserChannelAdapter(empty,this);
    DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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

  private Observer<List<UserChannel>> userChannelObserver()
  {
    return userchannels -> mAdapter.setUserChannels(userchannels);
  }

  public void setListener()
  {
    if (channelListener != null || userChannelListener != null)
    {
      return;
    }
    final String uid = mAuth.user().getUid();
    mFirestore.getUsers().document(uid).get()
    .addOnSuccessListener(userSnapshot ->
    {
      final User user = mFirestore.toFirestoreObject(userSnapshot, User.class);
      final String orgPushId = user.get_organizationPushId();

      mAppUserViewModel.setmUser(user);
      userChannelListener = mFirestore.getUserChannels(uid).addSnapshotListener((((querySnapshot, e) -> {
        if (e != null)
        {
          showToast("error getting channels from database: " + e.getMessage());
          return;
        }
        if (querySnapshot != null)
        {
          mUserChannelsViewModel.setUserChannelsFromSnapshot(querySnapshot);
        }
      })));
      channelListener = mFirestore.getAdminChannelsReference(orgPushId).addSnapshotListener(((querySnapshot, e) ->
      {
        if (e != null)
        {
          showToast("error getting channels from database: " + e.getMessage());
          noChannelView.setVisibility(View.GONE);
          progressBar.setVisibility(View.GONE);
          return;
        }
        if (querySnapshot != null)
        {
          mAdminChannelViewModel.setChannelsFromSnapshot(querySnapshot);
          toggleNoChannelView(mAdapter.getItems());
          progressBar.setVisibility(View.GONE);
        }
      }));
    });
  }

  public void removeListener()
  {
    if (channelListener != null)
    {
      channelListener.remove();
      channelListener = null;
    }
    if (userChannelListener != null)
    {
      userChannelListener.remove();
      userChannelListener = null;
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


  @Override
  public void onStart()
  {
    super.onStart();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mAdminChannelViewModel.getChannels().observe(this, observer());
    mUserChannelsViewModel.getChannels().observe(this, userChannelObserver());
    mAppUserViewModel.getmUser().observe(this, userObserver());
    setListener();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    removeListener();
    mAdminChannelViewModel.getChannels().removeObserver(observer());
    mUserChannelsViewModel.getChannels().removeObserver(userChannelObserver());
    mAppUserViewModel.getmUser().removeObserver(userObserver());
    mAppUserViewModel.clear();
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
  }

  public void showToast(CharSequence message)
  {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
