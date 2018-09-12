package com.idesign.runnit.NavigationHelpers;

import android.support.design.widget.NavigationView;
import android.view.MenuItem;

import com.idesign.runnit.R;

public class NavigationViewUtility
{

  public NavigationViewUtility() { }

  public void cleanUpMenu(NavigationView navigationView)
  {
    final int size = navigationView.getMenu().size();
    for (int i = 0; i < size; i++)
    {
      final MenuItem item = navigationView.getMenu().getItem(i);
      item.setChecked(false);
    }
  }

  public void setCheckedToFalse(int itemId, NavigationView navigationView)
  {
    navigationView.getMenu().findItem(itemId).setChecked(false);
  }

  public void isLoggedInHasRestaurantCode(NavigationView navigationView)
  {
    navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
    navigationView.getMenu().findItem(R.id.nav_signup).setVisible(false);

    navigationView.getMenu().findItem(R.id.nav_edit_info).setVisible(true);
    navigationView.getMenu().findItem(R.id.nav_verify_restaurant).setVisible(false);
    navigationView.getMenu().findItem(R.id.nav_channel).setVisible(true);
    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
  }

  public void isLoggedInNoRestaurantCode(NavigationView navigationView)
  {
    navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
    navigationView.getMenu().findItem(R.id.nav_signup).setVisible(false);

    navigationView.getMenu().findItem(R.id.nav_edit_info).setVisible(true);
    navigationView.getMenu().findItem(R.id.nav_verify_restaurant).setVisible(true);
    navigationView.getMenu().findItem(R.id.nav_channel).setVisible(true);
    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
  }

  public void isNotLoggedIn(NavigationView navigationView)
  {
    navigationView.getMenu().findItem(R.id.nav_login).setVisible(true);
    navigationView.getMenu().findItem(R.id.nav_signup).setVisible(true);

    navigationView.getMenu().findItem(R.id.nav_edit_info).setVisible(false);
    navigationView.getMenu().findItem(R.id.nav_verify_restaurant).setVisible(false);
    navigationView.getMenu().findItem(R.id.nav_admin).setVisible(false);
    navigationView.getMenu().findItem(R.id.nav_all_users).setVisible(false);
    navigationView.getMenu().findItem(R.id.nav_channel).setVisible(false);
    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
  }

  public void isAdmin(NavigationView navigationView)
  {
    navigationView.getMenu().findItem(R.id.nav_all_users).setVisible(true);
    navigationView.getMenu().findItem(R.id.nav_verify_restaurant).setVisible(false);
  }

  public void isNotAdmin(NavigationView navigationView)
  {
    navigationView.getMenu().findItem(R.id.nav_all_users).setVisible(false);
    navigationView.getMenu().findItem(R.id.nav_verify_restaurant).setVisible(true);
  }
}
