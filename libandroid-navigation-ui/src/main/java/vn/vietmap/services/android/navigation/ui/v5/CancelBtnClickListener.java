package vn.vietmap.services.android.navigation.ui.v5;

import android.view.View;

public class CancelBtnClickListener implements View.OnClickListener {

  private NavigationViewEventDispatcher dispatcher;

  public CancelBtnClickListener(NavigationViewEventDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public void onClick(View view) {
    dispatcher.onCancelNavigation();
  }
}
