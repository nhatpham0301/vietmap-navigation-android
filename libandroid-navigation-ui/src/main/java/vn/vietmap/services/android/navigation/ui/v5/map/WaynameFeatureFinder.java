package vn.vietmap.services.android.navigation.ui.v5.map;

import android.graphics.PointF;

import com.mapbox.geojson.Feature;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

import java.util.List;

public class WaynameFeatureFinder {

  private VietMapGL vietMapGL;

  WaynameFeatureFinder(VietMapGL vietMapGL) {
    this.vietMapGL = vietMapGL;
  }

  List<Feature> queryRenderedFeatures(PointF point, String[] layerIds) {
    return vietMapGL.queryRenderedFeatures(point, layerIds);
  }
}
