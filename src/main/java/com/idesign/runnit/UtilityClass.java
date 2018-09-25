package com.idesign.runnit;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.text.TextUtils;
import android.widget.EditText;

public class UtilityClass {

  public UtilityClass() {}

  public <T>void observeViewModel(LifecycleOwner owner, LiveData<T> liveData, Observer<T> observer)
  {
    if (!liveData.hasActiveObservers())
      liveData.observe(owner, observer);
  }

  public String trimString(String source)
  {
    return source.trim();
  }

  public boolean isEmpty(String source)
  {
    return TextUtils.isEmpty(source);
  }

  public boolean isEmpty(EditText editText)
  {
    return TextUtils.isEmpty(editText.getText());
  }

  public String upperCaseFirstLetter(String source)
  {
    if (source.equals(""))
      return "";
    if (source.length() == 1)
      return source.substring(0, 1).toUpperCase();

    return source.substring(0, 1).toUpperCase() + source.substring(1);
  }

  public String lowercaseString(String source)
  {
    return source.toLowerCase();
  }
}
