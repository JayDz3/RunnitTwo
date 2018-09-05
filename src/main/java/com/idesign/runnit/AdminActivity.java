package com.idesign.runnit;

import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.idesign.runnit.FirestoreTasks.BaseFirestore;
import com.idesign.runnit.FirestoreTasks.MyAuth;
import com.idesign.runnit.Items.FirestoreOrg;

import java.util.Objects;

public class AdminActivity extends AppCompatActivity
{
  private final MyAuth mAuth = new MyAuth();
  private final BaseFirestore mFirestore = new BaseFirestore();

  private final String EXTRA_ORG_NAME = "org.name";
  private final String EXTRA_ORG_CODE = "org.code";

  private EditText orgName;
  private EditText orgCode;

  private Snackbar snackbar;
  private ConstraintLayout mainLayout;

  private Button submitButton;
  private Button queryButton;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_admin);
    setViewItems();

    submitButton.setOnClickListener(l -> submitOrgInfo());
    queryButton.setOnClickListener(l -> queryOrg());

    if (savedInstanceState != null)
    {
      getValuesFromBundle(savedInstanceState);
    }
  }

  public void setViewItems()
  {
    mainLayout = findViewById(R.id.admin_activity_main_layout);
    orgName = findViewById(R.id.admin_activity_org_name);
    orgCode = findViewById(R.id.admin_activity_org_code);
    submitButton = findViewById(R.id.admin_activity_submit);
    queryButton = findViewById(R.id.admin_activity_query_org_button);
  }

  public void submitOrgInfo()
  {
    if (!isValidName() || !isValidCode())
    {
      showToast("Please ensure valid data has been entered");
      return;
    }
    final String _pushid = mAuth.user().getUid();
    final String _orgName = orgName.getText().toString();
    final String _orgCode = orgCode.getText().toString();

    final String trimmedName = trimmedString(_orgName);
    final String trimmedCode = trimmedString(_orgCode);

    final FirestoreOrg org = new FirestoreOrg(_pushid, trimmedName, trimmedCode);

    mFirestore.queryOrgForDuplicateCodeTask(trimmedCode)
    .addOnSuccessListener(orgQuery ->
    {
      if (orgQuery.getDocuments().size() > 0) {
        showToast("this code is taken. Please enter another");

      } else {
        setOrg(org);
      }
    })
    .addOnFailureListener(e -> showToast("error: " + e.getMessage()));
  }

  public void queryOrg()
  {
    final String uid = mAuth.user().getUid();
    mFirestore.getOrgSnapshotTask(uid)
    .addOnSuccessListener(orgSnapshot ->
    {
      FirestoreOrg fsOrg = orgSnapshot.toObject(FirestoreOrg.class);
      showSnackbar(Objects.requireNonNull(fsOrg).get_organizationCode());
    })
    .addOnFailureListener(e -> showToast("error: " + e.getMessage()));
  }

  public void setOrg(FirestoreOrg org)
  {
    final String uid = mAuth.user().getUid();
    final String orgPushid = org.getPushId();
    final String orgCode = org.get_organizationCode();
    final DocumentReference userReference = mFirestore.getUsers().document(uid);

    mFirestore.setOrg(org)
    .onSuccessTask(ignore -> mFirestore.setOrganizationCodeTask(userReference, orgCode))
    .onSuccessTask(ignore -> mFirestore.setUserOrgPushId(orgPushid, uid))
    .addOnSuccessListener(l ->
    {
      showToast("Your organization has been added!");
      finish();
    })
    .addOnFailureListener(e -> showToast("error: " + e.getMessage()));
  }

  public boolean isValidName()
  {
    return !TextUtils.isEmpty(orgName.getText());
  }

  public boolean isValidCode()
  {
    return !TextUtils.isEmpty(orgCode.getText().toString()) && orgCode.getText().toString().length() > 5;
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
  public void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    final String trimmedName = trimmedString(orgName.getText().toString());
    final String trimmedCode = trimmedString(orgCode.getText().toString());
    outState.putString(EXTRA_ORG_NAME, trimmedCode);
    outState.putString(EXTRA_ORG_NAME, trimmedName);
  }

  public void getValuesFromBundle(Bundle inState)
  {
    if (inState.keySet().contains(EXTRA_ORG_NAME))
    {
      orgName.setText(EXTRA_ORG_NAME);
      orgCode.setText(EXTRA_ORG_CODE);
    }
  }

  public String trimmedString(String source)
  {
    return source.trim();
  }

  void showToast(CharSequence message)
  {
    Toast.makeText(AdminActivity.this, message, Toast.LENGTH_SHORT).show();
  }

  void showSnackbar(CharSequence message)
  {
    snackbar = Snackbar.make(mainLayout, message, Snackbar.LENGTH_INDEFINITE).setAction(R.string.dismiss, l -> snackbar.dismiss());
    snackbar.show();
  }

 /* public void logMessage(String message)
  {
    Log.d("Admin ACTIVITY: ", "message: " + message);
  } */
}
