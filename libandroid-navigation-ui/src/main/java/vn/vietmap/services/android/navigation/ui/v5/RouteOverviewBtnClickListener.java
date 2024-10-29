package vn.vietmap.services.android.navigation.ui.v5;

import android.view.View;

public class RouteOverviewBtnClickListener implements View.OnClickListener {

  private NavigationPresenter presenter;

  public RouteOverviewBtnClickListener(NavigationPresenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void onClick(View view) {
    presenter.onRouteOverviewClick();
  }
}
