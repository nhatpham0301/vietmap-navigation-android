package vn.vietmap.services.android.navigation.v5.navigation;

import android.location.Location;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class NavigationLocationUpdate {

  static NavigationLocationUpdate create(Location location, VietmapNavigation vietmapNavigation) {
    return new AutoValue_NavigationLocationUpdate(location, vietmapNavigation);
  }

  abstract Location location();

  abstract VietmapNavigation mapboxNavigation();
}
