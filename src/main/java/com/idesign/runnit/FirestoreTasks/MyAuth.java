package com.idesign.runnit.FirestoreTasks;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyAuth
{

  private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
  private boolean hasListener = false;

  public MyAuth() { }

  public void setHasListener(boolean status)
  {
    hasListener = status;
  }

  public boolean doesHaveListener()
  {
    return hasListener;
  }

  public FirebaseUser user()
  {
    return mAuth.getCurrentUser();
  }

  public Task<AuthResult> signInWithEmailAndPassword(String email, String password)
  {
   return mAuth.signInWithEmailAndPassword(email, password);
  }

  public void setAuthListener(FirebaseAuth.AuthStateListener listener)
  {
    mAuth.addAuthStateListener(listener);
  }

  public void removeAuthListener(FirebaseAuth.AuthStateListener listener)
  {
    mAuth.removeAuthStateListener(listener);
  }

  public void signOut()
  {
   mAuth.signOut();
  }

  public Task<Void> sendResetPassword(String email)
  {
    return mAuth.sendPasswordResetEmail(email);
  }

  public Task<AuthResult> createUser(String email, String password)
  {
    return mAuth.createUserWithEmailAndPassword(email, password);
  }

  public Task<Void> reauth(String email, String pw)
  {
    AuthCredential credential = EmailAuthProvider.getCredential(email, pw);
    return user().reauthenticate(credential);
  }
}
