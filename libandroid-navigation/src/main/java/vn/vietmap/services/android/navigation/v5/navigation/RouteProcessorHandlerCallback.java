package vn.vietmap.services.android.navigation.v5.navigation;

import android.location.Location;
import android.os.Handler;
import android.os.Message;

import vn.vietmap.services.android.navigation.v5.milestone.Milestone;
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;

import java.util.List;

class RouteProcessorHandlerCallback implements Handler.Callback {

  private NavigationRouteProcessor routeProcessor;
  private RouteProcessorBackgroundThread.Listener listener;
  private Handler responseHandler;

  RouteProcessorHandlerCallback(NavigationRouteProcessor routeProcessor, Handler responseHandler,
                                RouteProcessorBackgroundThread.Listener listener) {
    this.routeProcessor = routeProcessor;
    this.responseHandler = responseHandler;
    this.listener = listener;
  }

  @Override
  public boolean handleMessage(Message msg) {
    NavigationLocationUpdate update = ((NavigationLocationUpdate) msg.obj);
    handleRequest(update);
    return true;
  }

  /**
   * Takes a new location model and runs all related engine checks against it
   * (off-route, milestones, snapped location, and faster-route).
   * <p>
   * After running through the engines, all data is submitted to {@link NavigationService} via
   * {@link RouteProcessorBackgroundThread.Listener}.
   *
   * @param update hold location, navigation (with options), and distances away from maneuver
   */
  private void handleRequest(final NavigationLocationUpdate update) {
    final VietmapNavigation vietmapNavigation = update.mapboxNavigation();
    final Location rawLocation = update.location();
    RouteProgress routeProgress = routeProcessor.buildNewRouteProgress(vietmapNavigation, rawLocation);

    final boolean userOffRoute = determineUserOffRoute(update, vietmapNavigation, routeProgress);
    final List<Milestone> milestones = findTriggeredMilestones(vietmapNavigation, routeProgress);
    final Location location = findSnappedLocation(vietmapNavigation, rawLocation, routeProgress, userOffRoute);
    final boolean checkFasterRoute = findFasterRoute(update, vietmapNavigation, routeProgress, userOffRoute);

    final RouteProgress finalRouteProgress = updateRouteProcessorWith(routeProgress);
    sendUpdateToListener(userOffRoute, milestones, location, checkFasterRoute, finalRouteProgress);
  }

  private List<Milestone> findTriggeredMilestones(VietmapNavigation vietmapNavigation, RouteProgress routeProgress) {
    RouteProgress previousRouteProgress = routeProcessor.getRouteProgress();
    return NavigationHelper.checkMilestones(previousRouteProgress, routeProgress, vietmapNavigation);
  }

  private Location findSnappedLocation(VietmapNavigation vietmapNavigation, Location rawLocation,
                                       RouteProgress routeProgress, boolean userOffRoute) {
    boolean snapToRouteEnabled = vietmapNavigation.options().snapToRoute();
    return NavigationHelper.buildSnappedLocation(vietmapNavigation, snapToRouteEnabled,
      rawLocation, routeProgress, userOffRoute);
  }

  private boolean determineUserOffRoute(NavigationLocationUpdate navigationLocationUpdate,
                                        VietmapNavigation vietmapNavigation, RouteProgress routeProgress) {
    final boolean userOffRoute = NavigationHelper.isUserOffRoute(navigationLocationUpdate, routeProgress, routeProcessor);
    routeProcessor.checkIncreaseIndex(vietmapNavigation);
    return userOffRoute;
  }

  private boolean findFasterRoute(NavigationLocationUpdate navigationLocationUpdate, VietmapNavigation vietmapNavigation,
                                  RouteProgress routeProgress, boolean userOffRoute) {
    boolean fasterRouteEnabled = vietmapNavigation.options().enableFasterRouteDetection();
    return fasterRouteEnabled && !userOffRoute
      && NavigationHelper.shouldCheckFasterRoute(navigationLocationUpdate, routeProgress);
  }

  private RouteProgress updateRouteProcessorWith(RouteProgress routeProgress) {
    routeProcessor.setRouteProgress(routeProgress);
    return routeProgress;
  }

  private void sendUpdateToListener(final boolean userOffRoute, final List<Milestone> milestones,
                                    final Location location, final boolean checkFasterRoute,
                                    final RouteProgress finalRouteProgress) {
    responseHandler.post(new Runnable() {
      @Override
      public void run() {
        listener.onNewRouteProgress(location, finalRouteProgress);
        listener.onMilestoneTrigger(milestones, finalRouteProgress);
        listener.onUserOffRoute(location, userOffRoute);
        listener.onCheckFasterRoute(location, finalRouteProgress, checkFasterRoute);
      }
    });
  }
}
