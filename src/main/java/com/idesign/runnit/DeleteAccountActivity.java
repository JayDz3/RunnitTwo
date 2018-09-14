package com.idesign.runnit;

import android.app.NotificationManager;
import android.os.Build;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.User;

import java.util.Objects;

public class DeleteAccountActivity extends AppCompatActivity
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private EditText fieldOne;
  private EditText fieldTwo;

  private Button confirmButton;

  private final String EXTRA_FIELD_ONE = "field_one";
  private final String EXTRA_FIELD_TWO = "field_two";

  private final String COLLECTION_ACTIVE_USERS = "ActiveUsers";
  private final String COLLECTION_SUBSCRIBED_USERS = "SubscribedUsers";

  private final String KEY_DELETE_IN_PROGRESS = "in_progress";
  private boolean inProgress = false;

  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_delete_account);
    fieldOne = findViewById(R.id.delete_account_activity_field_one);
    fieldTwo = findViewById(R.id.delete_account_activity_field_two);
    confirmButton = findViewById(R.id.delete_account_activity_confirm_button);
    confirmButton.setOnClickListener(l -> submit());
    progressBar = findViewById(R.id.delete_account_activity_progress_bar);
    progressBar.setVisibility(View.GONE);
    getValuesFromBundle(savedInstanceState);
  }

  public void submit()
  {
    if (isEmptyField(fieldOne) || isEmptyField(fieldTwo))
    {
      showToast("Can not submit empty fields");
      return;
    }
    final String password = fieldOne.getText().toString();
    final String passwordTwo = fieldTwo.getText().toString();
    if (!password.equals(passwordTwo))
    {
      showToast("Fields do not match");
      return;
    }

    inProgress = true;
    progressBar.setVisibility(View.VISIBLE);
    confirmButton.setEnabled(false);
    confirmButton.setClickable(false);

    final FirebaseUser user = mAuth.user();
    final String email = user.getEmail();
    final String uid = mAuth.user().getUid();

    final DocumentReference userRef = mFirestore.getUsers().document(uid);
    final CollectionReference channelsRef = mFirestore.getUserChannels(uid);
    final WriteBatch batch = mFirestore.batch();

    new Thread(new MyRunnable() {
      @Override
      public void run()
      {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        mAuth.reauth(email, password)
        .onSuccessTask(l -> user.delete())
        .onSuccessTask(ignore -> userRef.get())
        .onSuccessTask(snapshot -> getAdminChannelReference(snapshot))
        .onSuccessTask(channelsSnapshot ->
        {
          if (channelsSnapshot != null)
          {
            deleteAdminChannelsReferences(channelsSnapshot, uid, batch);
          }
          return channelsRef.get();
        })
        .onSuccessTask(userChannels ->
        {
          if (userChannels != null)
          {
            deleteUsersChannelReferences(userChannels, batch);
          }
          batch.delete(userRef);
          return batch.commit();
        })
        .addOnSuccessListener(l -> onDeleteSuccess())
        .addOnFailureListener(e -> onDeleteFailure(e));
      }
    }).start();
  }

  public void onDeleteSuccess()
  {
    inProgress = false;
    progressBar.setVisibility(View.GONE);
    showToast("Account Deleted");
    enableButton();
    finish();
  }

  public void onDeleteFailure(Exception e)
  {
    inProgress = false;
    progressBar.setVisibility(View.GONE);
    showToast(e.getMessage());
    enableButton();
  }

  public Task<QuerySnapshot> getAdminChannelReference(DocumentSnapshot snapshot)
  {
    final User firestoreUser = mFirestore.toFirestoreObject(snapshot, User.class);
    final String orgPushId = firestoreUser.get_organizationPushId();
    final CollectionReference adminChannelsRef = mFirestore.getAdminChannelsReference(orgPushId);

    return adminChannelsRef.get();
  }

  public void enableButton()
  {
    confirmButton.setEnabled(true);
    confirmButton.setClickable(true);
  }

  public void deleteAdminChannelsReferences(QuerySnapshot channelsSnapshot, String uid, WriteBatch batch)
  {
    for (DocumentSnapshot ds : channelsSnapshot.getDocuments())
    {
      final DocumentReference activeUserRef = ds.getReference().collection(COLLECTION_ACTIVE_USERS).document(uid);
      final DocumentReference subUserRef = ds.getReference().collection(COLLECTION_SUBSCRIBED_USERS).document(uid);
      batch.delete(activeUserRef);
      batch.delete(subUserRef);
    }
  }

  public void deleteUsersChannelReferences(QuerySnapshot channelsSnapshot, WriteBatch batch)
  {
    for (DocumentSnapshot ds : channelsSnapshot.getDocuments())
    {
      final DocumentReference ref = ds.getReference();
      deleteNotificationChannel(ds.getId());
      batch.delete(ref);
    }
  }

  public void deleteNotificationChannel(String chanelId)
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      if (Objects.requireNonNull(notificationManager).getNotificationChannel(chanelId) != null)
      {
        notificationManager.deleteNotificationChannel(chanelId);
      }
    }
  }

  public String getFieldOne()
  {
    return fieldOne.getText().toString();
  }

  public String getFieldTwo()
  {
    return fieldTwo.getText().toString();
  }

  public boolean isEmptyField(EditText editText)
  {
    return TextUtils.isEmpty(editText.getText());
  }

  @Override
  public void onStart()
  {
    super.onStart();
  }

  @Override
  public void onPause()
  {
    super.onPause();
  }

  @Override
  public void onResume()
  {
    super.onResume();
  }

  @Override
  public void onStop()
  {
    super.onStop();
  }

  @Override
  public void onBackPressed()
  {
    if (inProgress)
    {
      return;
    }
    super.onBackPressed();
  }

  public void getValuesFromBundle(Bundle savedInstanceState)
  {
    if (savedInstanceState != null)
    {
      if (savedInstanceState.keySet().contains(EXTRA_FIELD_ONE))
      {
        fieldOne.setText(savedInstanceState.getString(EXTRA_FIELD_ONE));
        fieldTwo.setText(savedInstanceState.getString(EXTRA_FIELD_TWO));
        inProgress = savedInstanceState.getBoolean(KEY_DELETE_IN_PROGRESS);
      }
      if (inProgress)
      {
        progressBar.setVisibility(View.VISIBLE);
        confirmButton.setClickable(false);
        confirmButton.setEnabled(false);
      }
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    outState.putString(EXTRA_FIELD_ONE, getFieldOne());
    outState.putString(EXTRA_FIELD_TWO, getFieldTwo());
    outState.putBoolean(KEY_DELETE_IN_PROGRESS, inProgress);
    super.onSaveInstanceState(outState);
  }

  public void showToast(String message)
  {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
