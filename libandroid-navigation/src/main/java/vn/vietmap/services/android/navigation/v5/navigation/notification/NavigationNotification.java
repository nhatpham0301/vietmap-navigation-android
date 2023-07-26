package vn.vietmap.services.android.navigation.v5.navigation.notification;

import android.app.Notification;
import android.content.Context;

import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigation;
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigationOptions;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationService;
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;

/**
 * Defines a contract in which a custom notification must adhere to when
 * given to {@link VietmapNavigationOptions}.
 */
public interface NavigationNotification {

  String END_NAVIGATION_ACTION = "com.mapbox.intent.action.END_NAVIGATION";
  String OPEN_NAVIGATION_ACTION = "com.mapbox.intent.action.OPEN_NAVIGATION";

  /**
   * Provides a custom {@link Notification} to launch
   * with the {@link NavigationService}, specifically
   * {@link android.app.Service#startForeground(int, Notification)}.
   *
   * @return a custom notification
   */
  Notification getNotification();

  /**
   * An integer id that will be used to start this notification
   * from {@link NavigationService} with
   * {@link android.app.Service#startForeground(int, Notification)}.
   *
   * @return an int id specific to the notification
   */
  int getNotificationId();

  /**
   * If enabled, this method will be called every time a
   * new {@link RouteProgress} is generated.
   * <p>
   * This method can serve as a cue to update a {@link Notification}
   * with a specific notification id.
   *
   * @param routeProgress with the latest progress data
   */
  void updateNotification(RouteProgress routeProgress);

  /**
   * Callback for when navigation is stopped via {@link VietmapNavigation#stopNavigation()}.
   * <p>
   * This callback may be used to clean up any listeners or receivers, preventing leaks.
   *
   * @param context to be used if needed for Android-related work
   */
  void onNavigationStopped(Context context);
}
