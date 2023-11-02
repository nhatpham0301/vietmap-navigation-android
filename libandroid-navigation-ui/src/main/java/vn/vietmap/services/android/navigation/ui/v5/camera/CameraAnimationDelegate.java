package vn.vietmap.services.android.navigation.ui.v5.camera;

import vn.vietmap.vietmapsdk.camera.CameraUpdate;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

class CameraAnimationDelegate {

  private final VietMapGL vietMapGL;

  CameraAnimationDelegate(VietMapGL vietMapGL) {
    this.vietMapGL = vietMapGL;
  }

  void render(NavigationCameraUpdate update, int durationMs, VietMapGL.CancelableCallback callback) {
    CameraUpdateMode mode = update.getMode();
    CameraUpdate cameraUpdate = update.getCameraUpdate();
    if (mode == CameraUpdateMode.OVERRIDE) {
      vietMapGL.getLocationComponent().setCameraMode(CameraMode.NONE);
      vietMapGL.animateCamera(cameraUpdate, durationMs, callback);
    } else if (!isTracking()) {
      vietMapGL.animateCamera(cameraUpdate, durationMs, callback);
    }
  }

  private boolean isTracking() {
    int cameraMode = vietMapGL.getLocationComponent().getCameraMode();
    return cameraMode != CameraMode.NONE;
  }
}