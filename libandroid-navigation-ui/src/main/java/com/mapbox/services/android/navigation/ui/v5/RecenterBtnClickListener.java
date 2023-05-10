package com.mapbox.services.android.navigation.ui.v5;

import android.view.View;

public class RecenterBtnClickListener implements View.OnClickListener {

  public NavigationPresenter presenter;

  public RecenterBtnClickListener(NavigationPresenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void onClick(View view) {
    presenter.onRecenterClick();
  }
}
