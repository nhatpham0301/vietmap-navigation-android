package com.mapbox.services.android.navigation.ui.v5.camera;

import vn.vietmap.vietmapsdk.camera.CameraUpdate;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

class CameraAnimationDelegate {

  private final VietMapGL mapboxMap;

  CameraAnimationDelegate(VietMapGL mapboxMap) {
    this.mapboxMap = mapboxMap;
  }

  void render(NavigationCameraUpdate update, int durationMs, VietMapGL.CancelableCallback callback) {
    CameraUpdateMode mode = update.getMode();
    CameraUpdate cameraUpdate = update.getCameraUpdate();
    if (mode == CameraUpdateMode.OVERRIDE) {
      mapboxMap.getLocationComponent().setCameraMode(CameraMode.NONE);
      mapboxMap.animateCamera(cameraUpdate, durationMs, callback);
    } else if (!isTracking()) {
      mapboxMap.animateCamera(cameraUpdate, durationMs, callback);
    }
  }

  private boolean isTracking() {
    int cameraMode = mapboxMap.getLocationComponent().getCameraMode();
    return cameraMode != CameraMode.NONE;
  }
}