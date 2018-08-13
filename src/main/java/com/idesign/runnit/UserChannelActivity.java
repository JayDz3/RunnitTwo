package com.idesign.runnit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
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
import com.idesign.runnit.VIewModels.AdminChannelViewModel;
import com.idesign.runnit.VIewModels.AppUserViewModel;

import java.util.ArrayList;
import java.util.List;

public class UserChannelActivity extends AppCompatActivity
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private UserChannelAdapter mAdapter;

  private AdminChannelViewModel mAdminChannelViewModel;
  private AppUserViewModel mAppUserViewModel;

  private ListenerRegistration channelListener;

  private RecyclerView mRecyclerView;
  private LinearLayoutManager mLayouManager;

  private ProgressBar progressBar;
  private FloatingActionButton fab;

  private TextView noChannelView;

  private int PRIMARY;
  private int DARK_GREY;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_channel);
    setViewItems();
    setAdapterAndRecyclerView();

    DARK_GREY = ContextCompat.getColor(this, R.color.colorDarkGray);
    PRIMARY = ContextCompat.getColor(this, R.color.colorPrimary);

    mAdminChannelViewModel = ViewModelProviders.of(this).get(AdminChannelViewModel.class);
    mAppUserViewModel = ViewModelProviders.of(this).get(AppUserViewModel.class);

    progressBar.setVisibility(View.GONE);
    noChannelView.setVisibility(View.GONE);
  }

  public void setViewItems()
  {
    mRecyclerView = findViewById(R.id.channel_activity_user_recycler_view);
    noChannelView = findViewById(R.id.channel_activity_user_no_channels);
    progressBar = findViewById(R.id.channel_activity_user_progress_bar);
  }

  public void setAdapterAndRecyclerView()
  {
    final List<FirestoreChannel> channelsOnCreate = new ArrayList<>();
    mAdapter = new UserChannelAdapter(channelsOnCreate, this);
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
    final String uid = mAuth.user().getUid();
    mFirestore.getUsers().document(uid).get()
    .addOnSuccessListener(userSnapshot ->
    {
      final User user = mFirestore.toFirestoreObject(userSnapshot, User.class);
      mAppUserViewModel.setmUser(user);

      final String mUserOrgpushid = mAppUserViewModel.getmUser().getValue().get_organizationPushId();
      channelListener = mFirestore.getAdminChannelsReference(mUserOrgpushid).addSnapshotListener(((querySnapshot, e) ->
      {
        if (e != null)
        {
          showToast("error getting channels from database: " + e.getMessage());
          noChannelView.setVisibility(View.VISIBLE);
          return;
        }
        if (querySnapshot != null)
        {
          mAdminChannelViewModel.setChannelsFromSnapshot(querySnapshot);
          toggleNoChannelView(mAdapter.getItems());
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
    setListener();
    mAdminChannelViewModel.getChannels().observe(this, observer());
    mAppUserViewModel.getmUser().observe(this, userObserver());
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
  public void onDestroy()
  {
    super.onDestroy();
  }

  public void showToast(CharSequence message)
  {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
