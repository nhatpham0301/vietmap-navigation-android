package vn.vietmap.services.android.navigation.ui.v5.map;

import android.content.Context;
import android.content.res.Resources;

import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.services.android.navigation.ui.v5.R;

class MapPaddingAdjustor {

  private static final int BOTTOMSHEET_PADDING_MULTIPLIER = 4;
  private static final int WAYNAME_PADDING_MULTIPLIER = 2;

  private final VietMapGL vietMapGL;
  private final int[] defaultPadding;
  private int[] customPadding;

  MapPaddingAdjustor(MapView mapView, VietMapGL vietMapGL) {
    this.vietMapGL = vietMapGL;
    defaultPadding = calculateDefaultPadding(mapView);
  }

  // Testing only
  MapPaddingAdjustor(VietMapGL vietMapGL, int[] defaultPadding) {
    this.vietMapGL = vietMapGL;
    this.defaultPadding = defaultPadding;
  }

  void updatePaddingWithDefault() {
    customPadding = null;
//    updatePaddingWith(defaultPadding);
    updatePaddingWith(new int[]{0, 0, 0, 0});
  }

  void adjustLocationIconWith(int[] customPadding) {
    this.customPadding = customPadding;
    updatePaddingWith(customPadding);
  }

  int[] retrieveCurrentPadding() {
    return vietMapGL.getPadding();
  }

  boolean isUsingDefault() {
    return customPadding == null;
  }

  void updatePaddingWith(int[] padding) {
    vietMapGL.setPadding(padding[0], padding[1], padding[2], padding[3]);
  }

  void resetPadding() {
    if (isUsingDefault()) {
      updatePaddingWithDefault();
    } else {
      adjustLocationIconWith(customPadding);
    }
  }

  private int[] calculateDefaultPadding(MapView mapView) {
    int defaultTopPadding = calculateTopPaddingWithoutWayname(mapView);
    Resources resources = mapView.getContext().getResources();
    int waynameLayoutHeight = (int) resources.getDimension(R.dimen.wayname_view_height);
    int topPadding = defaultTopPadding - (waynameLayoutHeight * WAYNAME_PADDING_MULTIPLIER);
    return new int[] {0, topPadding, 0, 0};
  }

  private int calculateTopPaddingWithoutWayname(MapView mapView) {
    Context context = mapView.getContext();
    Resources resources = context.getResources();
    int mapViewHeight = mapView.getHeight();
    int bottomSheetHeight = (int) resources.getDimension(R.dimen.summary_bottomsheet_height);
    return mapViewHeight - (bottomSheetHeight * BOTTOMSHEET_PADDING_MULTIPLIER);
  }
}
