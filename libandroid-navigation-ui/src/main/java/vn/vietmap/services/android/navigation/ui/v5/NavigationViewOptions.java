package vn.vietmap.services.android.navigation.ui.v5;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.auto.value.AutoValue;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigationOptions;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.services.android.navigation.ui.v5.listeners.BannerInstructionsListener;
import vn.vietmap.services.android.navigation.ui.v5.listeners.InstructionListListener;
import vn.vietmap.services.android.navigation.ui.v5.listeners.NavigationListener;
import vn.vietmap.services.android.navigation.ui.v5.listeners.RouteListener;
import vn.vietmap.services.android.navigation.ui.v5.listeners.SpeechAnnouncementListener;
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechPlayer;
import vn.vietmap.services.android.navigation.v5.milestone.Milestone;
import vn.vietmap.services.android.navigation.v5.milestone.MilestoneEventListener;
import vn.vietmap.services.android.navigation.v5.routeprogress.ProgressChangeListener;

import java.util.List;

@AutoValue
public abstract class NavigationViewOptions extends NavigationUiOptions {

  public abstract VietmapNavigationOptions navigationOptions();

  public abstract VietMapGL.OnMoveListener onMoveListener();

  @Nullable
  public abstract RouteListener routeListener();

  @Nullable
  public abstract NavigationListener navigationListener();

  @Nullable
  public abstract ProgressChangeListener progressChangeListener();

  @Nullable
  public abstract MilestoneEventListener milestoneEventListener();

  @Nullable
  public abstract List<Milestone> milestones();

  @Nullable
  public abstract BottomSheetCallback bottomSheetCallback();

  @Nullable
  public abstract InstructionListListener instructionListListener();

  @Nullable
  public abstract SpeechAnnouncementListener speechAnnouncementListener();

  @Nullable
  public abstract BannerInstructionsListener bannerInstructionsListener();

  @Nullable
  public abstract SpeechPlayer speechPlayer();

  @Nullable
  public abstract LocationEngine locationEngine();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder directionsRoute(DirectionsRoute directionsRoute);

    public abstract Builder lightThemeResId(Integer lightThemeResId);

    public abstract Builder darkThemeResId(Integer darkThemeResId);

    public abstract Builder onMoveListener(VietMapGL.OnMoveListener onMoveListener);

    public abstract Builder shouldSimulateRoute(boolean shouldSimulateRoute);

    public abstract Builder waynameChipEnabled(boolean waynameChipEnabled);

    public abstract Builder navigationOptions(VietmapNavigationOptions navigationOptions);

    public abstract Builder routeListener(RouteListener routeListener);

    public abstract Builder navigationListener(NavigationListener navigationListener);

    public abstract Builder progressChangeListener(ProgressChangeListener progressChangeListener);


    public abstract Builder milestoneEventListener(MilestoneEventListener milestoneEventListener);

    public abstract Builder milestones(List<Milestone> milestones);

    public abstract Builder bottomSheetCallback(BottomSheetCallback bottomSheetCallback);

    public abstract Builder instructionListListener(InstructionListListener instructionListListener);

    public abstract Builder speechAnnouncementListener(SpeechAnnouncementListener speechAnnouncementListener);

    public abstract Builder bannerInstructionsListener(BannerInstructionsListener bannerInstructionsListener);

    public abstract Builder speechPlayer(SpeechPlayer speechPlayer);

    public abstract Builder locationEngine(LocationEngine locationEngine);

    public abstract NavigationViewOptions build();
  }

  public static Builder builder() {
    return new AutoValue_NavigationViewOptions.Builder()
      .navigationOptions(VietmapNavigationOptions.builder().build())
      .shouldSimulateRoute(false)
      .waynameChipEnabled(true);
  }
}