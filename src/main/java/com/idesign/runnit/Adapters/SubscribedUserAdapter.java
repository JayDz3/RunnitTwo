package com.idesign.runnit.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.Items.SubscribedUser;
import com.idesign.runnit.R;

import java.util.List;

public class SubscribedUserAdapter extends RecyclerView.Adapter<SubscribedUserAdapter.UserViewHolder> {
  private List<SubscribedUser> mUsers;
  private final String channelId;
  private final String orgPushId;
  private final BaseFirestore mFirestore = new BaseFirestore();
  private SubscribedUserAdapterListener mListener;
  private boolean enabled = true;

  class UserViewHolder extends RecyclerView.ViewHolder {
    private TextView _firstName;
    private TextView _lastName;
    private Button _send;

    UserViewHolder(View view)
    {
      super(view);
      _firstName = view.findViewById(R.id.subscribed_user_firstname);
      _lastName = view.findViewById(R.id.subscribed_user_lastname);
      _send = view.findViewById(R.id.subscribed_user_send);
    }
  }

  public SubscribedUserAdapter(List<SubscribedUser> users, final String channelId, final String orgPushId, SubscribedUserAdapterListener listener)
  {
    mUsers = users;
    this.channelId = channelId;
    this.orgPushId = orgPushId;
    setListener(listener);
  }

  private void setListener(SubscribedUserAdapterListener listener)
  {
    if (mListener == null)
    {
      mListener = listener;
    }
  }

  public void setUsers(List<SubscribedUser> users)
  {
    mUsers = users;
    notifyDataSetChanged();
  }

  @Override
  @NonNull
  public SubscribedUserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
  {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscribed_user, parent, false);
    return new UserViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull SubscribedUserAdapter.UserViewHolder viewHolder, final int position)
  {
    final SubscribedUser subscribedUser = mUsers.get(position);
    final String firstName = subscribedUser.get_firstName();
    final String lastName = subscribedUser.get_lastName();
    final String pushId = subscribedUser.get_pushId();
    viewHolder._firstName.setText(firstName);
    viewHolder._lastName.setText(lastName);
    viewHolder._send.setOnClickListener(l -> sendNotification(viewHolder, pushId, firstName, lastName));
  }

  private void sendNotification(UserViewHolder viewHolder, final String userId, final String firstname, final String lastname)
  {
    if (!enabled)
    {
      return;
    }
    disableViewHolder(viewHolder);
    final String COLLECTION_ACTIVE_USERS = "ActiveUsers";
    final DocumentReference channelRef = mFirestore.getAdminChannel(orgPushId, channelId);
    final CollectionReference activeUsersReference = channelRef.collection(COLLECTION_ACTIVE_USERS);
    final DocumentReference userRef = activeUsersReference.document(userId);
    final String _message = firstname + " " + lastname;

    userRef.delete()
    .onSuccessTask(ignore -> mFirestore.setActiveUser(activeUsersReference, userId, _message))
    .addOnSuccessListener(ignore -> {
      mListener.onSuccess(firstname, lastname);
      enableViewHolder(viewHolder);
    })
    .addOnFailureListener(e -> {
      final String errString = e.getMessage();
      mListener.onFailure(errString);
      enableViewHolder(viewHolder);
    });
  }

  private void disableViewHolder(UserViewHolder viewHolder)
  {
    enabled = false;
    viewHolder._send.setEnabled(false);
    viewHolder._send.setClickable(false);
  }

  private void enableViewHolder(UserViewHolder viewHolder)
  {
    enabled = true;
    viewHolder._send.setEnabled(true);
    viewHolder._send.setClickable(true);
  }

  public int getItemCount()
  {
    return mUsers.size();
  }

  public interface SubscribedUserAdapterListener {
    void onSuccess(final String firstname, final String lastname);
    void onFailure(final String errorMessage);
  }
}
