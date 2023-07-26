package vn.vietmap.services.android.navigation.ui.v5.camera;

import vn.vietmap.vietmapsdk.camera.CameraUpdate;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

class CameraOverviewCancelableCallback implements VietMapGL.CancelableCallback {

  private static final int OVERVIEW_UPDATE_DURATION_IN_MILLIS = 750;

  private CameraUpdate overviewUpdate;
  private VietMapGL mapboxMap;

  public CameraOverviewCancelableCallback(CameraUpdate overviewUpdate, VietMapGL mapboxMap) {
    this.overviewUpdate = overviewUpdate;
    this.mapboxMap = mapboxMap;
  }

  @Override
  public void onCancel() {
    // No-op
  }

  @Override
  public void onFinish() {
    mapboxMap.animateCamera(overviewUpdate, OVERVIEW_UPDATE_DURATION_IN_MILLIS);
  }
}
