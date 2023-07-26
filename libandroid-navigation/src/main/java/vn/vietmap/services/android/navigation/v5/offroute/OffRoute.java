package vn.vietmap.services.android.navigation.v5.offroute;

import android.location.Location;

import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigationOptions;
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;

public abstract class OffRoute {

  public abstract boolean isUserOffRoute(Location location, RouteProgress routeProgress,
                                         VietmapNavigationOptions options);
}
