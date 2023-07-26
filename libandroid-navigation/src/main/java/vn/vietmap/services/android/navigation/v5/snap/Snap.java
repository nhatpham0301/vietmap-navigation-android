package vn.vietmap.services.android.navigation.v5.snap;

import android.location.Location;

import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;

public abstract class Snap {

  public abstract Location getSnappedLocation(Location location, RouteProgress routeProgress);
}
