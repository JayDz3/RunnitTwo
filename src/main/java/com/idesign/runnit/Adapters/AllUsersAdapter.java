package com.idesign.runnit.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.QuerySnapshot;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.Items.User;
import com.idesign.runnit.R;

import java.util.List;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.AllUsersViewHolder>
{
  private List<User> mUsers;
  private AllUsersAdapterListener mListener;

  private final BaseFirestore mFirestore = new BaseFirestore();

  private boolean enabled = true;
  private final String orgPushId;

  class AllUsersViewHolder extends RecyclerView.ViewHolder
  {
    private TextView firstNameView;
    private TextView lastNameView;
    private Button deleteButton;

    AllUsersViewHolder(View view)
    {
      super(view);
      firstNameView = view.findViewById(R.id.all_users_item_firstname);
      lastNameView = view.findViewById(R.id.all_users_item_lastname);
      deleteButton = view.findViewById(R.id.all_users_item_delete);
    }
  }

  public AllUsersAdapter(final List<User> mUsers, AllUsersAdapterListener listener, String orgPushId)
  {
    this.mUsers = mUsers;
    this.orgPushId = orgPushId;
    setListener(listener);
  }

  private void setListener(AllUsersAdapterListener listener)
  {
    if (mListener == null)
    {
      mListener = listener;
    }
  }

  public void setItems(List<User> mUsers)
  {
    this.mUsers = mUsers;
    notifyDataSetChanged();
  }

  @Override
  @NonNull
  public AllUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
  {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_item, parent, false);
    return new AllUsersViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull AllUsersViewHolder viewHolder, final int position)
  {
    final User user = mUsers.get(position);
    viewHolder.firstNameView.setText(user.get_firstName());
    viewHolder.lastNameView.setText(user.get_lastName());
    viewHolder.deleteButton.setOnClickListener(l -> delete(user.get_pushId(), viewHolder));
  }

  public void delete(String uid, AllUsersViewHolder viewHolder)
  {
    if (!enabled)
    {
      return;
    }
    disableViewHolder(viewHolder);
    setEnabled(false);
    mListener.disable();

    mFirestore.setUserOrganizationPushIdEmpty(orgPushId, uid)
    .onSuccessTask(ignore -> mFirestore.getOrganizationUsers(orgPushId))
    .addOnSuccessListener(snapshot ->
    {
      mListener.enable();
      mListener.onSnapshot(snapshot);
      enableViewHolder(viewHolder);
    })
    .addOnFailureListener(e ->
    {
      mListener.enable();
      enableViewHolder(viewHolder);
      mListener.toast(e.getMessage());
    });
  }

  private void enableViewHolder(AllUsersViewHolder viewHolder)
  {
    viewHolder.deleteButton.setClickable(true);
    viewHolder.deleteButton.setEnabled(true);
  }

  private void disableViewHolder(AllUsersViewHolder viewHolder)
  {
    viewHolder.deleteButton.setClickable(false);
    viewHolder.deleteButton.setEnabled(false);
  }

  public void setEnabled(boolean status)
  {
    enabled = status;
  }

  @Override
  public int getItemCount()
  {
    return mUsers.size();
  }

  public interface AllUsersAdapterListener {
    void enable();
    void disable();
    void toast(String message);
    void onSnapshot(QuerySnapshot snapshot);
  }
}
