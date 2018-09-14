package com.idesign.runnit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.User;

public class DeleteAccountActivity extends AppCompatActivity
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private EditText fieldOne;
  private EditText fieldTwo;

  private Button confirmButton;

  private final String EXTRA_FIELD_ONE = "field_one";
  private final String EXTRA_FIELD_TWO = "field_two";

  private final String COLLECTION_CHANNELS = "Channels";
  private final String COLLECTION_ACTIVE_USERS = "ActiveUsers";
  private final String COLLECTION_SUBSCRIBED_USERS = "SubscribedUsers";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_delete_account);
    fieldOne = findViewById(R.id.delete_account_activity_field_one);
    fieldTwo = findViewById(R.id.delete_account_activity_field_two);
    confirmButton = findViewById(R.id.delete_account_activity_confirm_button);
    confirmButton.setOnClickListener(l -> submit());
    getValuesFromBundle(savedInstanceState);
  }

  public void submit()
  {
    if (isEmptyField(fieldOne) || isEmptyField(fieldTwo))
    {
      showToast("Can not submit empty fields");
      return;
    }
    final String text = fieldOne.getText().toString();
    final String textTwo = fieldTwo.getText().toString();
    if (!text.equals(textTwo))
    {
      showToast("Fields do not match");
      return;
    }
    confirmButton.setEnabled(false);
    confirmButton.setClickable(false);
    final FirebaseUser user = mAuth.user();
    final String email = user.getEmail();
    final String uid = mAuth.user().getUid();

    final WriteBatch batch = mFirestore.batch();
    mAuth.reauth(email, text)
    .onSuccessTask(l -> user.delete())
    .onSuccessTask(ignore -> mFirestore.getUsers().document(uid).get())
    .onSuccessTask(snapshot -> {
      final User firestoreUser = mFirestore.toFirestoreObject(snapshot, User.class);
      final String orgPushId = firestoreUser.get_organizationPushId();
      final CollectionReference adminChannelsRef = mFirestore.getAdminChannelsReference(orgPushId);
      return adminChannelsRef.get();
    })
    .onSuccessTask(channelsSnapshot -> {
      final CollectionReference channelsRef = mFirestore.getUserChannels(uid);
      if (channelsSnapshot == null)
      {
        return channelsRef.get();
      }
      for (DocumentSnapshot ds : channelsSnapshot.getDocuments())
      {
        final DocumentReference userRef = ds.getReference().collection(COLLECTION_ACTIVE_USERS).document(uid);
        final DocumentReference subUserRef = ds.getReference().collection(COLLECTION_SUBSCRIBED_USERS).document(uid);
        batch.delete(userRef);
        batch.delete(subUserRef);
      }
      return channelsRef.get();
    })
    .onSuccessTask(userChannels -> {
      final DocumentReference userRef = mFirestore.getUsers().document(uid);
      if (userChannels == null) {
        batch.delete(userRef);
        return batch.commit();
      } else {
        for (DocumentSnapshot ds : userChannels)
        {
          final DocumentReference ref = ds.getReference();
          batch.delete(ref);
        }
        batch.delete(userRef);
      }
     return batch.commit();
    })
    .addOnSuccessListener(l -> {
      showToast("success auth");
      confirmButton.setEnabled(true);
      confirmButton.setClickable(true);
      finish();
    })
    .addOnFailureListener(e ->
    {
      showToast(e.getMessage());
      confirmButton.setEnabled(true);
      confirmButton.setClickable(true);
    });
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

  public void getValuesFromBundle(Bundle savedInstanceState)
  {
    if (savedInstanceState != null)
    {
      if (savedInstanceState.keySet().contains(EXTRA_FIELD_ONE))
      {
        fieldOne.setText(savedInstanceState.getString(EXTRA_FIELD_ONE));
        fieldTwo.setText(savedInstanceState.getString(EXTRA_FIELD_TWO));
      }
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState)
  {
    outState.putString(EXTRA_FIELD_ONE, getFieldOne());
    outState.putString(EXTRA_FIELD_TWO, getFieldTwo());
    super.onSaveInstanceState(outState);
  }

  public void showToast(String message)
  {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
