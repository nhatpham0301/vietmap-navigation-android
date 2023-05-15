package com.mapbox.services.android.navigation.ui.v5;

import android.location.Location;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.utils.TextUtils;
import com.mapbox.geojson.Point;

public class NavigationPresenter {

  private NavigationContract.View view;
  private boolean resumeState;

  NavigationPresenter(NavigationContract.View view) {
    this.view = view;
  }

  public void updateResumeState(boolean resumeState) {
    this.resumeState = resumeState;
  }

  public void onRecenterClick() {
    view.setSummaryBehaviorHideable(false);
    view.setSummaryBehaviorState(BottomSheetBehavior.STATE_EXPANDED);
//    view.updateWayNameVisibility(true);
    view.resetCameraPosition();
    view.hideRecenterBtn();
  }
  public void resetCameraPosition(){
    view.resetCameraPosition();
  }

  public void onCameraTrackingDismissed() {
    if (!view.isSummaryBottomSheetHidden()) {
      view.setSummaryBehaviorHideable(true);
      view.setSummaryBehaviorState(BottomSheetBehavior.STATE_HIDDEN);
//      view.updateWayNameVisibility(false);
    }
  }

  public void onSummaryBottomSheetHidden() {
    if (view.isSummaryBottomSheetHidden()) {
      view.showRecenterBtn();
    }
  }

  public void onRouteUpdate(DirectionsRoute directionsRoute) {
    view.drawRoute(directionsRoute);
    if (resumeState && view.isRecenterButtonVisible()) {
      view.updateCameraRouteOverview();
    } else {
      view.startCamera(directionsRoute);
    }
  }

  public void onDestinationUpdate(Point point) {
    view.addMarker(point);
  }

  public void onNavigationLocationUpdate(Location location) {
    if (resumeState && !view.isRecenterButtonVisible()) {
      view.resumeCamera(location);
      resumeState = false;
    }
    view.updateNavigationMap(location);
  }

  public void onWayNameChanged(@NonNull String wayName) {
    if (TextUtils.isEmpty(wayName) || view.isSummaryBottomSheetHidden()) {
//      view.updateWayNameVisibility(false);
      return;
    }
    view.updateWayNameView(wayName);
//    view.updateWayNameVisibility(true);
  }

  public void onNavigationStopped() {
    view.updateWayNameVisibility(false);
  }

  public void onRouteOverviewClick() {
//    view.updateWayNameVisibility(false);
    view.updateCameraRouteOverview();
    view.showRecenterBtn();
  }
}