package vn.vietmap.services.android.navigation.v5.navigation;

import android.location.Location;
import android.util.Pair;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.api.directions.v5.models.StepIntersection;
import com.mapbox.geojson.Point;
import vn.vietmap.services.android.navigation.v5.offroute.OffRoute;
import vn.vietmap.services.android.navigation.v5.offroute.OffRouteCallback;
import vn.vietmap.services.android.navigation.v5.offroute.OffRouteDetector;
import vn.vietmap.services.android.navigation.v5.routeprogress.CurrentLegAnnotation;
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;
import vn.vietmap.services.android.navigation.v5.utils.RouteUtils;

import java.util.List;

class NavigationRouteProcessor implements OffRouteCallback {

  private static final int FIRST_LEG_INDEX = 0;
  private static final int FIRST_STEP_INDEX = 0;
  private static final int ONE_INDEX = 1;

  private RouteProgress routeProgress;
  private List<Point> currentStepPoints;
  private List<Point> upcomingStepPoints;
  private List<StepIntersection> currentIntersections;
  private List<Pair<StepIntersection, Double>> currentIntersectionDistances;
  private RouteLeg currentLeg;
  private LegStep currentStep;
  private LegStep upcomingStep;
  private CurrentLegAnnotation currentLegAnnotation;
  private NavigationIndices indices;
  private double stepDistanceRemaining;
  private boolean shouldIncreaseIndex;
  private NavigationIndices shouldUpdateToIndex;
  private RouteUtils routeUtils;

  NavigationRouteProcessor() {
    indices = NavigationIndices.create(FIRST_LEG_INDEX, FIRST_STEP_INDEX);
    routeUtils = new RouteUtils();
  }

  @Override
  public void onShouldIncreaseIndex() {
    shouldIncreaseIndex = true;
  }

  @Override
  public void onShouldUpdateToIndex(int legIndex, int stepIndex) {
    shouldUpdateToIndex = NavigationIndices.create(legIndex, stepIndex);
    onShouldIncreaseIndex();
  }

  /**
   * Will take a given location update and create a new {@link RouteProgress}
   * based on our calculations of the distances remaining.
   * <p>
   * Also in charge of detecting if a step / leg has finished and incrementing the
   * indices if needed ({@link NavigationRouteProcessor#advanceIndices(VietmapNavigation)} handles
   * the decoding of the next step point list).
   *
   * @param navigation for the current route / options
   * @param location   for step / leg / route distance remaining
   * @return new route progress along the route
   */
  RouteProgress buildNewRouteProgress(VietmapNavigation navigation, Location location) {
    DirectionsRoute directionsRoute = navigation.getRoute();
    VietmapNavigationOptions options = navigation.options();
    double completionOffset = options.maxTurnCompletionOffset();
    double maneuverZoneRadius = options.maneuverZoneRadius();
    checkNewRoute(navigation);
    stepDistanceRemaining = calculateStepDistanceRemaining(location, directionsRoute);
    checkManeuverCompletion(navigation, location, directionsRoute, completionOffset, maneuverZoneRadius);
    return assembleRouteProgress(directionsRoute);
  }

  RouteProgress getRouteProgress() {
    return routeProgress;
  }

  void setRouteProgress(RouteProgress routeProgress) {
    this.routeProgress = routeProgress;
  }

  /**
   * If the {@link OffRouteCallback#onShouldIncreaseIndex()} has been called by the
   * {@link OffRouteDetector}, shouldIncreaseIndex
   * will be true and the {@link NavigationIndices} index needs to be increased by one.
   *
   * @param navigation to get the next {@link LegStep#geometry()} and off-route engine
   */
  void checkIncreaseIndex(VietmapNavigation navigation) {
    if (shouldIncreaseIndex) {
      advanceIndices(navigation);
      shouldIncreaseIndex = false;
      shouldUpdateToIndex = null;
    }
  }

  /**
   * Checks if the route provided is a new route.  If it is, all {@link RouteProgress}
   * data and {@link NavigationIndices} needs to be reset.
   *
   * @param vietmapNavigation to get the current route and off-route engine
   */
  private void checkNewRoute(VietmapNavigation vietmapNavigation) {
    DirectionsRoute directionsRoute = vietmapNavigation.getRoute();
    if (routeUtils.isNewRoute(routeProgress, directionsRoute)) {
      createFirstIndices(vietmapNavigation);
      routeProgress = assembleRouteProgress(directionsRoute);
    }
  }

  /**
   * Given a location update, calculate the current step distance remaining.
   *
   * @param location        for current coordinates
   * @param directionsRoute for current {@link LegStep}
   * @return distance remaining in meters
   */
  private double calculateStepDistanceRemaining(Location location, DirectionsRoute directionsRoute) {
    Point snappedPosition = NavigationHelper.userSnappedToRoutePosition(location, currentStepPoints);
    return NavigationHelper.stepDistanceRemaining(
      snappedPosition, indices.legIndex(), indices.stepIndex(), directionsRoute, currentStepPoints
    );
  }

  private void checkManeuverCompletion(VietmapNavigation navigation, Location location, DirectionsRoute directionsRoute,
                                       double completionOffset, double maneuverZoneRadius) {
    boolean withinManeuverRadius = stepDistanceRemaining < maneuverZoneRadius;
    boolean bearingMatchesManeuver = NavigationHelper.checkBearingForStepCompletion(
      location, routeProgress, stepDistanceRemaining, completionOffset
    );
    boolean forceIncreaseIndices = stepDistanceRemaining == 0 && !bearingMatchesManeuver;

    if ((bearingMatchesManeuver && withinManeuverRadius) || forceIncreaseIndices) {
      advanceIndices(navigation);
      stepDistanceRemaining = calculateStepDistanceRemaining(location, directionsRoute);
    }
  }

  /**
   * Increases the step index in {@link NavigationIndices} by 1.
   * <p>
   * Decodes the step points for the new step and clears the distances from
   * maneuver stack, as the maneuver has now changed.
   *
   * @param vietmapNavigation to get the next {@link LegStep#geometry()} and {@link OffRoute}
   */
  private void advanceIndices(VietmapNavigation vietmapNavigation) {
    if(shouldUpdateToIndex != null){
      indices = shouldUpdateToIndex;
    }else{
      indices = NavigationHelper.increaseIndex(routeProgress, indices);
    }
    processNewIndex(vietmapNavigation);
  }

  /**
   * Initializes or resets the {@link NavigationIndices} for a new route received.
   *
   * @param vietmapNavigation to get the next {@link LegStep#geometry()} and {@link OffRoute}
   */
  private void createFirstIndices(VietmapNavigation vietmapNavigation) {
    indices = NavigationIndices.create(FIRST_LEG_INDEX, FIRST_STEP_INDEX);
    processNewIndex(vietmapNavigation);
  }

  /**
   * Called after {@link NavigationHelper#increaseIndex(RouteProgress, NavigationIndices)}.
   * <p>
   * Processes all new index-based data that is
   * needed for {@link NavigationRouteProcessor#assembleRouteProgress(DirectionsRoute)}.
   *
   * @param vietmapNavigation for the current route
   */
  private void processNewIndex(VietmapNavigation vietmapNavigation) {
    DirectionsRoute route = vietmapNavigation.getRoute();
    int legIndex = indices.legIndex();
    int stepIndex = indices.stepIndex();
    int upcomingStepIndex = stepIndex + ONE_INDEX;
    if(route.legs().size() <= legIndex || route.legs().get(legIndex).steps().size() <= stepIndex){
      // This catches a potential race condition when the route is changed, before the new index is processed
      createFirstIndices(vietmapNavigation);
      return;
    }
    updateSteps(route, legIndex, stepIndex, upcomingStepIndex);
    updateStepPoints(route, legIndex, stepIndex, upcomingStepIndex);
    updateIntersections();
    clearManeuverDistances(vietmapNavigation.getOffRouteEngine());
  }

  private RouteProgress assembleRouteProgress(DirectionsRoute route) {
    int legIndex = indices.legIndex();
    int stepIndex = indices.stepIndex();

    double legDistanceRemaining = NavigationHelper.legDistanceRemaining(stepDistanceRemaining, legIndex, stepIndex, route);
    double routeDistanceRemaining = NavigationHelper.routeDistanceRemaining(legDistanceRemaining, legIndex, route);
    currentLegAnnotation = NavigationHelper.createCurrentAnnotation(currentLegAnnotation, currentLeg, legDistanceRemaining);
    double stepDistanceTraveled = currentStep.distance() - stepDistanceRemaining;

    StepIntersection currentIntersection = NavigationHelper.findCurrentIntersection(
      currentIntersections, currentIntersectionDistances, stepDistanceTraveled
    );
    StepIntersection upcomingIntersection = NavigationHelper.findUpcomingIntersection(
      currentIntersections, upcomingStep, currentIntersection
    );

    RouteProgress.Builder progressBuilder = RouteProgress.builder()
      .stepDistanceRemaining(stepDistanceRemaining)
      .legDistanceRemaining(legDistanceRemaining)
      .distanceRemaining(routeDistanceRemaining)
      .directionsRoute(route)
      .currentStepPoints(currentStepPoints)
      .upcomingStepPoints(upcomingStepPoints)
      .stepIndex(stepIndex)
      .legIndex(legIndex)
      .intersections(currentIntersections)
      .currentIntersection(currentIntersection)
      .upcomingIntersection(upcomingIntersection)
      .intersectionDistancesAlongStep(currentIntersectionDistances)
      .currentLegAnnotation(currentLegAnnotation);

    addUpcomingStepPoints(progressBuilder);
    return progressBuilder.build();
  }

  private void addUpcomingStepPoints(RouteProgress.Builder progressBuilder) {
    if (upcomingStepPoints != null && !upcomingStepPoints.isEmpty()) {
      progressBuilder.upcomingStepPoints(upcomingStepPoints);
    }
  }

  private void updateSteps(DirectionsRoute route, int legIndex, int stepIndex, int upcomingStepIndex) {
    currentLeg = route.legs().get(legIndex);
    List<LegStep> steps = currentLeg.steps();
    currentStep = steps.get(stepIndex);
    upcomingStep = upcomingStepIndex < steps.size() - ONE_INDEX ? steps.get(upcomingStepIndex) : null;
  }

  private void updateStepPoints(DirectionsRoute route, int legIndex, int stepIndex, int upcomingStepIndex) {
    currentStepPoints = NavigationHelper.decodeStepPoints(route, currentStepPoints, legIndex, stepIndex);
    upcomingStepPoints = NavigationHelper.decodeStepPoints(route, null, legIndex, upcomingStepIndex);
  }

  private void updateIntersections() {
    currentIntersections = NavigationHelper.createIntersectionsList(currentStep, upcomingStep);
    currentIntersectionDistances = NavigationHelper.createDistancesToIntersections(currentStepPoints, currentIntersections);
  }

  private void clearManeuverDistances(OffRoute offRoute) {
    if (offRoute instanceof OffRouteDetector) {
      ((OffRouteDetector) offRoute).clearDistancesAwayFromManeuver();
    }
  }
}
