package vn.vietmap.services.android.navigation.ui.v5;

import vn.vietmap.services.android.navigation.ui.v5.listeners.InstructionListListener;

public class NavigationInstructionListListener implements InstructionListListener {

  private NavigationPresenter presenter;
  private NavigationViewEventDispatcher dispatcher;

  public NavigationInstructionListListener(NavigationPresenter presenter, NavigationViewEventDispatcher dispatcher) {
    this.presenter = presenter;
    this.dispatcher = dispatcher;
  }

  @Override
  public void onInstructionListVisibilityChanged(boolean visible) {
    dispatcher.onInstructionListVisibilityChanged(visible);
  }
}
