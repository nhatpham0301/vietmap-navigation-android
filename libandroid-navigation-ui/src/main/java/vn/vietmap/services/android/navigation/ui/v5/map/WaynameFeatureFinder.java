package vn.vietmap.services.android.navigation.ui.v5.map;

import android.graphics.PointF;

import com.mapbox.geojson.Feature;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

import java.util.List;

class WaynameFeatureFinder {

  private VietMapGL mapboxMap;

  WaynameFeatureFinder(VietMapGL mapboxMap) {
    this.mapboxMap = mapboxMap;
  }

  List<Feature> queryRenderedFeatures(PointF point, String[] layerIds) {
    return mapboxMap.queryRenderedFeatures(point, layerIds);
  }
}
