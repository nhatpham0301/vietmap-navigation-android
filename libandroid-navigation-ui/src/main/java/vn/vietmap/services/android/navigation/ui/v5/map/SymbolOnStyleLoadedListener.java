package vn.vietmap.services.android.navigation.ui.v5.map;

import android.graphics.Bitmap;

import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

public class SymbolOnStyleLoadedListener implements MapView.OnDidFinishLoadingStyleListener {

  private final VietMapGL vietMapGL;
  private final Bitmap markerBitmap;

  SymbolOnStyleLoadedListener(VietMapGL vietMapGL, Bitmap markerBitmap) {
    this.vietMapGL = vietMapGL;
    this.markerBitmap = markerBitmap;
  }

  @Override
  public void onDidFinishLoadingStyle() {
    vietMapGL.getStyle().addImage(NavigationSymbolManager.MAPBOX_NAVIGATION_MARKER_NAME, markerBitmap);
  }
}
