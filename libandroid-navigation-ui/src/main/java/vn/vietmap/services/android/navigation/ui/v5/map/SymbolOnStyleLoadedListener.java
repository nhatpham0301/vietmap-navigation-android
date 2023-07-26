package vn.vietmap.services.android.navigation.ui.v5.map;

import android.graphics.Bitmap;

import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

class SymbolOnStyleLoadedListener implements MapView.OnDidFinishLoadingStyleListener {

  private final VietMapGL mapboxMap;
  private final Bitmap markerBitmap;

  SymbolOnStyleLoadedListener(VietMapGL mapboxMap, Bitmap markerBitmap) {
    this.mapboxMap = mapboxMap;
    this.markerBitmap = markerBitmap;
  }

  @Override
  public void onDidFinishLoadingStyle() {
    mapboxMap.getStyle().addImage(NavigationSymbolManager.MAPBOX_NAVIGATION_MARKER_NAME, markerBitmap);
  }
}
