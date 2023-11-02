package vn.vietmap.services.android.navigation.ui.v5.camera;

import vn.vietmap.vietmapsdk.camera.CameraUpdate;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class CameraOverviewCancelableCallback implements VietMapGL.CancelableCallback {

  private static final int OVERVIEW_UPDATE_DURATION_IN_MILLIS = 750;

  public CameraUpdate overviewUpdate;
  public VietMapGL vietMapGL;

  public CameraOverviewCancelableCallback(CameraUpdate overviewUpdate, VietMapGL vietMapGL) {
    this.overviewUpdate = overviewUpdate;
    this.vietMapGL = vietMapGL;
  }

  @Override
  public void onCancel() {
    // No-op
  }

  @Override
  public void onFinish() {
    vietMapGL.animateCamera(overviewUpdate, OVERVIEW_UPDATE_DURATION_IN_MILLIS);
  }
}
