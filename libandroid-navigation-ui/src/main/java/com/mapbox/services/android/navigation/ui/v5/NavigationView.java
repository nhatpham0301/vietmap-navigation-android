package com.mapbox.services.android.navigation.ui.v5;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.geojson.Point;
import vn.vietmap.vietmapsdk.camera.CameraPosition;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;
import vn.vietmap.vietmapsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.camera.NavigationCamera;
import com.mapbox.services.android.navigation.ui.v5.instruction.ImageCreator;
import com.mapbox.services.android.navigation.ui.v5.instruction.InstructionView;
import com.mapbox.services.android.navigation.ui.v5.instruction.NavigationAlertView;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMap;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMapInstanceState;
import com.mapbox.services.android.navigation.ui.v5.map.WayNameView;
import com.mapbox.services.android.navigation.ui.v5.summary.SummaryBottomSheet;
import com.mapbox.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationTimeFormat;
import com.mapbox.services.android.navigation.v5.utils.DistanceFormatter;
import com.mapbox.services.android.navigation.v5.utils.LocaleUtils;

import java.util.function.Function;

/**
 * View that creates the drop-in UI.
 * <p>
 * Once started, this view will check if the {@link Activity} that inflated
 * it was launched with a {@link DirectionsRoute}.
 * <p>
 * Or, if not found, this view will look for a set of {@link Point} coordinates.
 * In the latter case, a new {@link DirectionsRoute} will be retrieved from {@link NavigationRoute}.
 * <p>
 * Once valid data is obtained, this activity will immediately begin navigation
 * with {@link MapboxNavigation}.
 * <p>
 * If launched with the simulation boolean set to true, a {@link ReplayRouteLocationEngine}
 * will be initialized and begin pushing updates.
 * <p>
 * This activity requires user permissions ACCESS_FINE_LOCATION
 * and ACCESS_COARSE_LOCATION have already been granted.
 * <p>
 * A Mapbox access token must also be set by the developer (to initialize navigation).
 *
 * @since 0.7.0
 */
public class NavigationView extends CoordinatorLayout implements LifecycleOwner, OnMapReadyCallback,
  NavigationContract.View {

  public static final String MAP_INSTANCE_STATE_KEY = "navgation_mapbox_map_instance_state";
  public static final int INVALID_STATE = 0;
  public MapView mapView;
  public InstructionView instructionView;
  public SummaryBottomSheet summaryBottomSheet;
  public BottomSheetBehavior summaryBehavior;
  public ImageButton cancelBtn;
  public RecenterButton recenterBtn;
  public WayNameView wayNameView;
  public ImageButton routeOverviewBtn;
  public boolean onRecenterClick;
  public NavigationPresenter navigationPresenter;
  public NavigationViewEventDispatcher navigationViewEventDispatcher;
  public NavigationViewModel navigationViewModel;
  public NavigationViewOptions navigationViewOptions;
  public NavigationMapboxMap navigationMap;
  public OnNavigationReadyCallback onNavigationReadyCallback;
  public NavigationOnCameraTrackingChangedListener onTrackingChangedListener;
  public NavigationMapboxMapInstanceState mapInstanceState;
  public CameraPosition initialMapCameraPosition;
  public boolean isMapInitialized;
  public boolean isSubscribed;
  public LifecycleRegistry lifecycleRegistry;


  public NavigationView(Context context) {
    this(context, null);
  }

  public NavigationView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public NavigationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    ThemeSwitcher.setTheme(context, attrs);
  }


  /**
   * Uses savedInstanceState as a cue to restore state (if not null).
   *
   * @param savedInstanceState to restore state if not null
   */
  public void onCreate(@Nullable Bundle savedInstanceState,@Nullable NavigationViewModel navigationViewModel) {
    initializeView(navigationViewModel);
    mapView.onCreate(savedInstanceState);
    updatePresenterState(savedInstanceState);
    lifecycleRegistry = new LifecycleRegistry(this);
    lifecycleRegistry.markState(Lifecycle.State.CREATED);
  }
  public NavigationPresenter getNavigationPresenter(){
      return  this.navigationPresenter;
  }
  public void setRecenterCallback(boolean b){
      onRecenterClick = b;
  }
  public NavigationViewEventDispatcher getNavigationViewEventDispatcher(){
    return  this.navigationViewEventDispatcher;
  }
  public void setRecenterBtn(RecenterButton recenterBtn){
    this.recenterBtn= recenterBtn;
  }
  public boolean retrieveRecenterButtonOnClick() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return recenterBtn.callOnClick();
  }
  public void initViewConfig(boolean isCustomizeView){
    if(!isCustomizeView) return;
    instructionView.setVisibility(GONE);
    summaryBottomSheet.setVisibility(GONE);
    routeOverviewBtn.setVisibility(GONE);
    wayNameView.setVisibility(GONE);
    recenterBtn.setVisibility(GONE);
  }
  /**
   * Low memory must be reported so the {@link MapView}
   * can react appropriately.
   */
  public void onLowMemory() {
    mapView.onLowMemory();
  }

  /**
   * If the instruction list is showing and onBackPressed is called,
   * hide the instruction list and do not hide the activity or fragment.
   *
   * @return true if back press handled, false if not
   */
  public boolean onBackPressed() {
    return instructionView.handleBackPressed();
  }

  /**
   * Used to store the bottomsheet state and re-center
   * button visibility.  As well as anything the {@link MapView}
   * needs to store in the bundle.
   *
   * @param outState to store state variables
   */
  public void onSaveInstanceState(Bundle outState) {
    int bottomSheetBehaviorState = summaryBehavior == null ? INVALID_STATE : summaryBehavior.getState();
    boolean isWayNameVisible = wayNameView.getVisibility() == VISIBLE;
    NavigationViewInstanceState navigationViewInstanceState = new NavigationViewInstanceState(
      bottomSheetBehaviorState, recenterBtn.getVisibility(), instructionView.isShowingInstructionList(),
      isWayNameVisible, wayNameView.retrieveWayNameText(), navigationViewModel.isMuted());
    String instanceKey = getContext().getString(R.string.navigation_view_instance_state);
    outState.putParcelable(instanceKey, navigationViewInstanceState);
    outState.putBoolean(getContext().getString(R.string.navigation_running), navigationViewModel.isRunning());
    mapView.onSaveInstanceState(outState);
    saveNavigationMapInstanceState(outState);
  }

  /**
   * Used to restore the bottomsheet state and re-center
   * button visibility.  As well as the {@link MapView}
   * position prior to rotation.
   *
   * @param savedInstanceState to extract state variables
   */
  @SuppressLint("WrongConstant")
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    String instanceKey = getContext().getString(R.string.navigation_view_instance_state);
    NavigationViewInstanceState navigationViewInstanceState = savedInstanceState.getParcelable(instanceKey);
    recenterBtn.setVisibility(navigationViewInstanceState.getRecenterButtonVisibility());
    wayNameView.setVisibility(navigationViewInstanceState.isWayNameVisible() ? VISIBLE : INVISIBLE);
    wayNameView.updateWayNameText(navigationViewInstanceState.getWayNameText());
    resetBottomSheetState(navigationViewInstanceState.getBottomSheetBehaviorState());
    updateInstructionListState(navigationViewInstanceState.isInstructionViewVisible());
    updateInstructionMutedState(navigationViewInstanceState.isMuted());
    mapInstanceState = savedInstanceState.getParcelable(MAP_INSTANCE_STATE_KEY);
  }

  /**
   * Called to ensure the {@link MapView} is destroyed
   * properly.
   * <p>
   * In an {@link Activity} this should be in .
   * <p>
   * In a {@link Fragment}, this should
   * be in {@link Fragment#onDestroyView()}.
   */
  public void onDestroy() {
    shutdown();
    lifecycleRegistry.markState(Lifecycle.State.DESTROYED);
  }

  public void onStart() {
    mapView.onStart();
    if (navigationMap != null) {
      navigationMap.onStart();
    }
    lifecycleRegistry.markState(Lifecycle.State.STARTED);
  }

  public void onResume() {
    mapView.onResume();
    lifecycleRegistry.markState(Lifecycle.State.RESUMED);
  }

  public void onPause() {
    mapView.onPause();
  }

  public void onStop() {
    mapView.onStop();
    if (navigationMap != null) {
      navigationMap.onStop();
    }
  }

  @NonNull
  @Override
  public Lifecycle getLifecycle() {
    return lifecycleRegistry;
  }

  /**
   * Fired after the map is ready, this is our cue to finish
   * setting up the rest of the plugins / location engine.
   * <p>
   * Also, we check for launch data (coordinates or route).
   *
   * @param mapboxMap used for route, camera, and location UI
   * @since 0.6.0
   */
  @Override
  public void onMapReady(final VietMapGL mapboxMap) {
    mapboxMap.setStyle(new Style.Builder().fromUri(getStyleUrl()), new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        initializeNavigationMap(mapView, mapboxMap);
        initializeWayNameListener();
        onNavigationReadyCallback.onNavigationReady(navigationViewModel.isRunning());
        isMapInitialized = true;
      }
    });
  }

  public String getStyleUrl() {
    return getContext().getString(getStyleUrlResourceId());
  }

  public int getStyleUrlResourceId() {
    return getContext().getResources().getIdentifier("map_view_style_url", "string", getContext().getPackageName());
  }

  @Override
  public void setSummaryBehaviorState(int state) {
    summaryBehavior.setState(state);
  }

  @Override
  public void setSummaryBehaviorHideable(boolean isHideable) {
    summaryBehavior.setHideable(isHideable);
  }

  @Override
  public boolean isSummaryBottomSheetHidden() {
    return summaryBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN;
  }

  @Override
  public void resetCameraPosition() {
    if (navigationMap != null) {
      navigationMap.resetPadding();
      navigationMap.resetCameraPositionWith(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);
    }
  }

  @Override
  public void showRecenterBtn() {
    recenterBtn.show();
  }

  @Override
  public void hideRecenterBtn() {
    recenterBtn.hide();
  }

  @Override
  public boolean isRecenterButtonVisible() {
    return recenterBtn.getVisibility() == View.VISIBLE;
  }

  @Override
  public void drawRoute(DirectionsRoute directionsRoute) {
    if (navigationMap != null) {
      navigationMap.drawRoute(directionsRoute);
    }
  }

  @Override
  public void addMarker(Point position) {
    if (navigationMap != null) {
      navigationMap.addDestinationMarker(position);
    }
  }

  /**
   * Provides the current visibility of the way name view.
   *
   * @return true if visible, false if not visible
   */
  public boolean isWayNameVisible() {
    return wayNameView.getVisibility() == VISIBLE;
  }

  /**
   * Updates the text of the way name view below the
   * navigation icon.
   * <p>
   * If you'd like to use this method without being overridden by the default way names
   * values we provide, please disabled auto-query with
   * {@link NavigationMapboxMap#updateWaynameQueryMap(boolean)}.
   *
   * @param wayName to update the view
   */
  @Override
  public void updateWayNameView(@NonNull String wayName) {
    wayNameView.updateWayNameText(wayName);
  }

  /**
   * Updates the visibility of the way name view that is show below
   * the navigation icon.
   * <p>
   * If you'd like to use this method without being overridden by the default visibility values
   * values we provide, please disabled auto-query with
   * {@link NavigationMapboxMap#updateWaynameQueryMap(boolean)}.
   *
   * @param isVisible true to show, false to hide
   */
  @Override
  public void updateWayNameVisibility(boolean isVisible) {
    wayNameView.updateVisibility(isVisible);
    if (navigationMap != null) {
      navigationMap.updateWaynameQueryMap(isVisible);
    }
  }
  /**
   * Used when starting this {@link android.app.Activity}
   * for the first time.
   * <p>
   * Zooms to the beginning of the {@link DirectionsRoute}.
   *
   * @param directionsRoute where camera should move to
   */
  @Override
  public void startCamera(DirectionsRoute directionsRoute) {
    if (navigationMap != null) {
      navigationMap.startCamera(directionsRoute);
    }
  }

  /**
   * Used after configuration changes to resume the camera
   * to the last location update from the Navigation SDK.
   *
   * @param location where the camera should move to
   */
  @Override
  public void resumeCamera(Location location) {
    if (navigationMap != null) {
      navigationMap.resumeCamera(location);
    }
  }

  @Override
  public void updateNavigationMap(Location location) {
    if (navigationMap != null) {
      navigationMap.updateLocation(location);
    }
  }

  @Override
  public void updateCameraRouteOverview() {
    if (navigationMap != null) {
      int[] padding = buildRouteOverviewPadding(getContext());
      navigationMap.showRouteOverview(padding);
    }
  }

  /**
   * Should be called when this view is completely initialized.
   *
   * @param options with containing route / coordinate data
   */
  public void startNavigation(NavigationViewOptions options) {
    initializeNavigation(options);
  }

  /**
   * Call this when the navigation session needs to end navigation without finishing the whole view
   *
   * @since 0.16.0
   */
  public void stopNavigation() {
    navigationPresenter.onNavigationStopped();
    navigationViewModel.stopNavigation();
  }

  /**
   * Should be called after {@link NavigationView#onCreate(Bundle)}.
   * <p>
   * This method adds the {@link OnNavigationReadyCallback},
   * which will fire the ready events for this view.
   *
   * @param onNavigationReadyCallback to be set to this view
   */
  public void initialize(OnNavigationReadyCallback onNavigationReadyCallback) {
    this.onNavigationReadyCallback = onNavigationReadyCallback;
    if (!isMapInitialized) {
      mapView.getMapAsync(this);
    } else {
      onNavigationReadyCallback.onNavigationReady(navigationViewModel.isRunning());
    }
  }

  /**
   * Should be called after {@link NavigationView#onCreate(Bundle)}.
   * <p>
   * This method adds the {@link OnNavigationReadyCallback},
   * which will fire the ready events for this view.
   * <p>
   * This method also accepts a {@link CameraPosition} that will be set as soon as the map is
   * ready.  Note, this position is ignored during rotation in favor of the last known map position.
   *
   * @param onNavigationReadyCallback to be set to this view
   * @param initialMapCameraPosition  to be shown once the map is ready
   */
  public void initialize(OnNavigationReadyCallback onNavigationReadyCallback,
                         @NonNull CameraPosition initialMapCameraPosition) {
    this.onNavigationReadyCallback = onNavigationReadyCallback;
    this.initialMapCameraPosition = initialMapCameraPosition;
    if (!isMapInitialized) {
      mapView.getMapAsync(this);
    } else {
      onNavigationReadyCallback.onNavigationReady(navigationViewModel.isRunning());
    }
  }

  /**
   * Gives the ability to manipulate the map directly for anything that might not currently be
   * supported. This returns null until the view is initialized.
   * <p>
   * The {@link NavigationMapboxMap} gives direct access to the map UI (location marker, route, etc.).
   *
   * @return navigation mapbox map object, or null if view has not been initialized
   */
  @Nullable
  public NavigationMapboxMap retrieveNavigationMapboxMap() {
    return navigationMap;
  }

  /**
   * Returns the instance of {@link MapboxNavigation} powering the {@link NavigationView}
   * once navigation has started.  Will return null if navigation has not been started with
   * {@link NavigationView#startNavigation(NavigationViewOptions)}.
   *
   * @return mapbox navigation, or null if navigation has not started
   */
  @Nullable
  public MapboxNavigation retrieveMapboxNavigation() {
    return navigationViewModel.retrieveNavigation();
  }

  /**
   * Returns the sound button used for muting instructions
   *
   * @return sound button
   */
  public NavigationButton retrieveSoundButton() {
    return instructionView.retrieveSoundButton();
  }


  /**
   * Returns the re-center button for recentering on current location
   *
   * @return recenter button
   */
  public NavigationButton retrieveRecenterButton() {
    return recenterBtn;
  }

  /**
   * Returns the {@link NavigationAlertView} that is shown during off-route events with
   * "Report a Problem" text.
   *
   * @return alert view that is used in the instruction view
   */
  public NavigationAlertView retrieveAlertView() {
    return instructionView.retrieveAlertView();
  }

  public void initializeView(@Nullable NavigationViewModel navigationViewModel) {
    inflate(getContext(), R.layout.navigation_view_layout, this);
    bind();
    initializeNavigationViewModel(navigationViewModel);
    initializeNavigationEventDispatcher();
    initializeNavigationPresenter();
    initializeInstructionListListener();
    initializeSummaryBottomSheet();
  }

  public void bind() {
    mapView = findViewById(R.id.navigationMapView);
    instructionView = findViewById(R.id.instructionView);
    ViewCompat.setElevation(instructionView, 10);
    summaryBottomSheet = findViewById(R.id.summaryBottomSheet);
    cancelBtn = findViewById(R.id.cancelBtn);
    if(recenterBtn==null){
      recenterBtn = findViewById(R.id.recenterBtn);
    }
    wayNameView = findViewById(R.id.wayNameView);
    routeOverviewBtn = findViewById(R.id.routeOverviewBtn);
  }

  public void initializeNavigationViewModel(@Nullable NavigationViewModel navigationViewModel) {
    try {
      if(navigationViewModel!=null){
        this.navigationViewModel = navigationViewModel;
      }else {
        this.navigationViewModel = new ViewModelProvider((FragmentActivity) getContext()).get(NavigationViewModel.class);
      }
    } catch (ClassCastException exception) {
      throw new ClassCastException("Please ensure that the provided Context is a valid FragmentActivity");
    }
  }

  public void initializeSummaryBottomSheet() {
    summaryBehavior = BottomSheetBehavior.from(summaryBottomSheet);
    summaryBehavior.setHideable(false);
    summaryBehavior.setBottomSheetCallback(new SummaryBottomSheetCallback(navigationPresenter,
      navigationViewEventDispatcher));
  }

  public void initializeNavigationEventDispatcher() {
    navigationViewEventDispatcher = new NavigationViewEventDispatcher();
    navigationViewModel.initializeEventDispatcher(navigationViewEventDispatcher);
  }

  public void initializeInstructionListListener() {
    instructionView.setInstructionListListener(new NavigationInstructionListListener(navigationPresenter,
      navigationViewEventDispatcher));
  }

  public void initializeNavigationMap(MapView mapView, VietMapGL map) {
    if (initialMapCameraPosition != null) {
      map.setCameraPosition(initialMapCameraPosition);
    }
    navigationMap = new NavigationMapboxMap(mapView, map);
    navigationMap.updateLocationLayerRenderMode(RenderMode.GPS);
    if (mapInstanceState != null) {
      navigationMap.restoreFrom(mapInstanceState);
      return;
    }
  }

  public void initializeWayNameListener() {
    NavigationViewWayNameListener wayNameListener = new NavigationViewWayNameListener(navigationPresenter);
    navigationMap.addOnWayNameChangedListener(wayNameListener);
  }

  public void saveNavigationMapInstanceState(Bundle outState) {
    if (navigationMap != null) {
      navigationMap.saveStateWith(MAP_INSTANCE_STATE_KEY, outState);
    }
  }

  public void resetBottomSheetState(int bottomSheetState) {
    if (bottomSheetState > INVALID_STATE) {
      boolean isShowing = bottomSheetState == BottomSheetBehavior.STATE_EXPANDED;
      summaryBehavior.setHideable(!isShowing);
      summaryBehavior.setState(bottomSheetState);
    }
  }

  public void updateInstructionListState(boolean visible) {
    if (visible) {
      instructionView.showInstructionList();
    } else {
      instructionView.hideInstructionList();
    }
  }

  public void updateInstructionMutedState(boolean isMuted) {
    if (isMuted) {
      ((SoundButton) instructionView.retrieveSoundButton()).soundFabOff();
    }
  }

  public int[] buildRouteOverviewPadding(Context context) {
    Resources resources = context.getResources();
    int leftRightPadding = (int) resources.getDimension(R.dimen.route_overview_left_right_padding);
    int paddingBuffer = (int) resources.getDimension(R.dimen.route_overview_buffer_padding);
    int instructionHeight = (int) (resources.getDimension(R.dimen.instruction_layout_height) + paddingBuffer);
    int summaryHeight = (int) resources.getDimension(R.dimen.summary_bottomsheet_height);
    return new int[] {leftRightPadding, instructionHeight, leftRightPadding, summaryHeight};
  }

  public boolean isChangingConfigurations() {
    try {
      return ((FragmentActivity) getContext()).isChangingConfigurations();
    } catch (ClassCastException exception) {
      throw new ClassCastException("Please ensure that the provided Context is a valid FragmentActivity");
    }
  }

  public void initializeNavigationPresenter() {
    navigationPresenter = new NavigationPresenter(this);
  }

  public void updatePresenterState(@Nullable Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      String navigationRunningKey = getContext().getString(R.string.navigation_running);
      boolean resumeState = savedInstanceState.getBoolean(navigationRunningKey);
      navigationPresenter.updateResumeState(resumeState);
    }
  }

  public void initializeNavigation(NavigationViewOptions options) {
    if(options.onMoveListener()!=null){
    navigationMap.setOnMoveListener(options.onMoveListener());}
    establish(options);
    navigationViewModel.initialize(options);
    initializeNavigationListeners(options, navigationViewModel);
    setupNavigationMapboxMap(options);
    if (!isSubscribed) {
      initializeClickListeners();
      initializeOnCameraTrackingChangedListener();
      subscribeViewModels();
    }
    navigationViewEventDispatcher.assignRouteListener(options.routeListener());

    navigationPresenter.onRouteOverviewClick();
    navigationPresenter.onRecenterClick();
//    navigationPresenter.resetCameraPosition();
  }

  public void initializeClickListeners() {
    cancelBtn.setOnClickListener(new CancelBtnClickListener(navigationViewEventDispatcher));
    recenterBtn.addOnClickListener(new RecenterBtnClickListener(navigationPresenter));
    routeOverviewBtn.setOnClickListener(new RouteOverviewBtnClickListener(navigationPresenter));
  }

  public void initializeOnCameraTrackingChangedListener() {
    onTrackingChangedListener = new NavigationOnCameraTrackingChangedListener(navigationPresenter, summaryBehavior);
    navigationMap.addOnCameraTrackingChangedListener(onTrackingChangedListener);
  }

  public void establish(NavigationViewOptions options) {
    LocaleUtils localeUtils = new LocaleUtils();
    establishDistanceFormatter(localeUtils, options);
    establishTimeFormat(options);
  }

  public void establishDistanceFormatter(LocaleUtils localeUtils, NavigationViewOptions options) {
    String unitType = establishUnitType(localeUtils, options);
    String language = establishLanguage(localeUtils, options);
    int roundingIncrement = establishRoundingIncrement(options);
    DistanceFormatter distanceFormatter = new DistanceFormatter(getContext(), language, unitType, roundingIncrement);

    instructionView.setDistanceFormatter(distanceFormatter);
    summaryBottomSheet.setDistanceFormatter(distanceFormatter);
  }

  public int establishRoundingIncrement(NavigationViewOptions navigationViewOptions) {
    MapboxNavigationOptions mapboxNavigationOptions = navigationViewOptions.navigationOptions();
    return mapboxNavigationOptions.roundingIncrement();
  }

  public String establishLanguage(LocaleUtils localeUtils, NavigationViewOptions options) {
    return localeUtils.getNonEmptyLanguage(getContext(), options.directionsRoute().voiceLanguage());
  }

  public String establishUnitType(LocaleUtils localeUtils, NavigationViewOptions options) {
    RouteOptions routeOptions = options.directionsRoute().routeOptions();
    String voiceUnits = routeOptions == null ? null : routeOptions.voiceUnits();
    return localeUtils.retrieveNonNullUnitType(getContext(), voiceUnits);
  }

  public void establishTimeFormat(NavigationViewOptions options) {
    @NavigationTimeFormat.Type
    int timeFormatType = options.navigationOptions().timeFormatType();
    summaryBottomSheet.setTimeFormat(timeFormatType);
  }

  public void initializeNavigationListeners(NavigationViewOptions options, NavigationViewModel navigationViewModel) {
    navigationMap.addProgressChangeListener(navigationViewModel.retrieveNavigation());
    navigationViewEventDispatcher.initializeListeners(options, navigationViewModel);
  }

  public void setupNavigationMapboxMap(NavigationViewOptions options) {
    navigationMap.updateWaynameQueryMap(options.waynameChipEnabled());
  }

  /**
   * Subscribes the {@link InstructionView} and {@link SummaryBottomSheet} to the {@link NavigationViewModel}.
   * <p>
   * Then, creates an instance of {@link NavigationViewSubscriber}, which takes a presenter.
   * <p>
   * The subscriber then subscribes to the view models, setting up the appropriate presenter / listener
   * method calls based on the {@link androidx.lifecycle.LiveData} updates.
   */
  public void subscribeViewModels() {
    instructionView.subscribe(this, navigationViewModel);
    summaryBottomSheet.subscribe(this, navigationViewModel);

    new NavigationViewSubscriber(this, navigationViewModel, navigationPresenter).subscribe();
    isSubscribed = true;
  }

  public void shutdown() {
    if (navigationMap != null) {
      navigationMap.removeOnCameraTrackingChangedListener(onTrackingChangedListener);
    }
    navigationViewEventDispatcher.onDestroy(navigationViewModel.retrieveNavigation());
    mapView.onDestroy();
    navigationViewModel.onDestroy(isChangingConfigurations());
    ImageCreator.getInstance().shutdown();
    navigationMap = null;
  }
}