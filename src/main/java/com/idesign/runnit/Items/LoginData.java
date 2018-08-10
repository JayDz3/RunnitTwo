package com.idesign.runnit.Items;

public class LoginData {
  private String email;
  private String resetEmail;
  private String password;
  private boolean isLoggingIn;
  private int navigationState;

  public LoginData() {}

  public void setEmail(String email)
  {
    this.email = email;
  }

  public void setResetEmail(String email)
  {
    this.resetEmail = email;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public void setLoggingIn(boolean status)
  {
    isLoggingIn = status;
  }

  public void setNavigationState(int state)
  {
    this.navigationState = state;
  }

  public String getEmail()
  {
    return email;
  }

  public String getResetEmail()
  {
    return resetEmail;
  }

  public String getPassword()
  {
    return password;
  }

  public boolean getLoggingIn()
  {
    return isLoggingIn;
  }

  public int getNavigationState()
  {
    return navigationState;
  }
}
