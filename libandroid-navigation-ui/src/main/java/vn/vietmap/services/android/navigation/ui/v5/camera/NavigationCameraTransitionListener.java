package vn.vietmap.services.android.navigation.ui.v5.camera;

import vn.vietmap.vietmapsdk.location.OnLocationCameraTransitionListener;

class NavigationCameraTransitionListener implements OnLocationCameraTransitionListener {

  private final NavigationCamera camera;

  NavigationCameraTransitionListener(NavigationCamera camera) {
    this.camera = camera;
  }

  @Override
  public void onLocationCameraTransitionFinished(int cameraMode) {
    camera.updateTransitionListenersFinished(cameraMode);
  }

  @Override
  public void onLocationCameraTransitionCanceled(int cameraMode) {
    camera.updateTransitionListenersCancelled(cameraMode);
    camera.updateIsResetting(false);
  }
}