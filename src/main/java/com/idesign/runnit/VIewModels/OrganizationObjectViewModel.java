package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.idesign.runnit.Items.OrganizationObject;

public class OrganizationObjectViewModel extends ViewModel {
  private final MutableLiveData<OrganizationObject> organizationObject = new MutableLiveData<>();

  public MutableLiveData<OrganizationObject> getOrganizationObject()
  {
    if (organizationObject.getValue() == null)
      organizationObject.setValue(new OrganizationObject("", "", ""));

    return organizationObject;
  }

  public void setOrganizationObject(OrganizationObject organizationObject)
  {
    this.organizationObject.setValue(organizationObject);
  }

  public void clear()
  {
    OrganizationObject orgObject = new OrganizationObject("", "", "");
    setOrganizationObject(orgObject);
  }
}
