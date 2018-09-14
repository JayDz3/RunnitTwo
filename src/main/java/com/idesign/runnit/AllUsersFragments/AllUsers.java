package com.idesign.runnit.AllUsersFragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.Adapters.AllUsersAdapter;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.R;
import com.idesign.runnit.VIewModels.AllUsersViewModel;

import java.util.ArrayList;
import java.util.List;

public class AllUsers extends Fragment implements AllUsersAdapter.AllUsersAdapterListener
{
  private AllUsersViewModel mUsersViewModel;
  private AllUsersFragmentListener mListener;

  private RecyclerView mRecyclerView;

  private AllUsersAdapter mAdapter;

  private final String ORG_PUSHID = "org_pushid";
  private final String USER_UID = "user_uid";

  private String uid;
  private String orgPushId;

  public AllUsers() { }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mUsersViewModel = ViewModelProviders.of(getActivity()).get(AllUsersViewModel.class);
    if (getArguments() != null)
    {
      uid = getArguments().getString(USER_UID);
      orgPushId = getArguments().getString(ORG_PUSHID);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.fragment_all_users, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
    mRecyclerView = view.findViewById(R.id.all_users_fragment_recycler_view);
    setRecyclerView();
  }

  public void setRecyclerView()
  {
    final List<User> empty = new ArrayList<>();
    mAdapter = new AllUsersAdapter(empty, AllUsers.this, orgPushId);
    DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mRecyclerView.addItemDecoration(itemDecoration);
    mRecyclerView.setAdapter(mAdapter);
  }

  public Observer<List<User>> usersObserver()
  {
    return users -> {
      if (users != null)
      {
        mAdapter.setItems(users);
      }
    };
  }

  public void setEnabled(boolean status)
  {
    mAdapter.setEnabled(status);
  }

  @Override
  public void onSnapshot(QuerySnapshot snapshot)
  {
    mUsersViewModel.setUsersFromQuerySnapshot(snapshot);
  }

  @Override
  public void toast(String message)
  {
    showToast(message);
  }

  @Override
  public void disable()
  {
    mListener.disable();
  }

  @Override
  public void enable()
  {
    mListener.enable();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    mUsersViewModel.getUsers().observe(this, usersObserver());
  }

  @Override
  public void onPause()
  {
    super.onPause();
    mUsersViewModel.getUsers().removeObserver(usersObserver());
  }

  @Override
  public void onAttach(Context context)
  {
    super.onAttach(context);
    try {
      mListener = (AllUsersFragmentListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " Must implement listener");
    }
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
    mListener = null;
  }

  public void showToast(String message)
  {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  public interface AllUsersFragmentListener {
    void enable();
    void disable();
  }
}
