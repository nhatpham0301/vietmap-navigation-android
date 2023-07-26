package vn.vietmap.services.android.navigation.v5.navigation;

import android.content.Context;

import vn.vietmap.services.android.navigation.v5.navigation.notification.NavigationNotification;
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;

class NavigationNotificationProvider {

  private NavigationNotification navigationNotification;
  private boolean shouldUpdate = true;

  NavigationNotificationProvider(Context context, VietmapNavigation vietmapNavigation) {
    navigationNotification = buildNotificationFrom(context, vietmapNavigation);
  }

  NavigationNotification retrieveNotification() {
    return navigationNotification;
  }

  void updateNavigationNotification(RouteProgress routeProgress) {
    if (shouldUpdate) {
      navigationNotification.updateNotification(routeProgress);
    }
  }

  void shutdown(Context context) {
    try {
      navigationNotification.onNavigationStopped(context);
    }catch(Exception e){}
    navigationNotification = null;
    shouldUpdate = false;
  }

  private NavigationNotification buildNotificationFrom(Context context, VietmapNavigation vietmapNavigation) {
    VietmapNavigationOptions options = vietmapNavigation.options();
    if (options.navigationNotification() != null) {
      return options.navigationNotification();
    } else {
      return new VietmapNavigationNotification(context, vietmapNavigation);
    }
  }
}
