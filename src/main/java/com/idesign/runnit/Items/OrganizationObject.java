package com.idesign.runnit.Items;

public class OrganizationObject
{
  private String _orgCodeOne;
  private String _orgCodeTwo;
  private String _orgName;

  public OrganizationObject(String _orgCodeOne, String _orgCodeTwo, String _orgName)
  {
    this._orgCodeOne = _orgCodeOne;
    this._orgCodeTwo = _orgCodeTwo;
    this._orgName = _orgName;
  }

  public void set_orgCodeOne(String _orgCodeOne)
  {
    this._orgCodeOne = _orgCodeOne;
  }

  public void set_orgCodeTwo(String _orgCodeTwo)
  {
    this._orgCodeTwo = _orgCodeTwo;
  }

  public void set_orgName(String _orgName)
  {
    this._orgName = _orgName;
  }

  public String get_orgCodeOne()
  {
    return _orgCodeOne;
  }

  public String get_orgCodeTwo()
  {
    return _orgCodeTwo;
  }

  public String get_orgName()
  {
    return _orgName;
  }
}
