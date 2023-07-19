package com.mapbox.services.android.navigation.testapp;

import static com.mapbox.services.android.navigation.testapp.NavigationSettings.ACCESS_TOKEN;
import static com.mapbox.services.android.navigation.testapp.NavigationSettings.STYLE_URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.transition.TransitionManager;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.engine.LocationEngine;
import com.mapbox.mapboxsdk.location.engine.LocationEngineCallback;
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest;
import com.mapbox.mapboxsdk.location.engine.LocationEngineResult;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.NavigationPresenter;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.RecenterBtnClickListener;
import com.mapbox.services.android.navigation.ui.v5.RecenterButton;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.RouteListener;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.ui.v5.route.OnRouteSelectionChangeListener;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechAnnouncement;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechPlayer;
import com.mapbox.services.android.navigation.v5.instruction.Instruction;
import com.mapbox.services.android.navigation.v5.location.engine.LocationEngineProvider;
import com.mapbox.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine;
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener;
import com.mapbox.services.android.navigation.v5.milestone.RouteMilestone;
import com.mapbox.services.android.navigation.v5.milestone.Trigger;
import com.mapbox.services.android.navigation.v5.milestone.TriggerProperty;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.turf.TurfMisc;

import java.lang.ref.WeakReference;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomUINavigationMap extends AppCompatActivity implements OnNavigationReadyCallback, ProgressChangeListener, RouteListener,
        NavigationListener, Callback<DirectionsResponse>, OnMapReadyCallback, MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener, OnRouteSelectionChangeListener, LocationListener, OffRouteListener, NavigationEventListener, MapboxMap.OnMoveListener {

    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_CAMERA_ZOOM = 20;
    private ConstraintLayout customUINavigation;
    private NavigationView navigationView;
    private MapView mapView;
    private ProgressBar loading;
    private FloatingActionButton launchNavigationFab;
    private Point origin = Point.fromLngLat(106.675789, 10.759050);
    private Point currentLocation;
    private Point destination = Point.fromLngLat(106.686777, 10.775056);
    private DirectionsRoute route;
    private boolean isNavigationRunning;
    private MapboxNavigation mapboxNavigation;
    //    private LocationLayerPlugin locationLayer;
    private LocationEngine locationEngine;
    private NavigationMapRoute mapRoute;
    private MapboxMap mapboxMap;
    private Marker currentMarker;
    private boolean locationFound;
    private ConstraintSet navigationMapConstraint;
    private ConstraintSet navigationMapExpandedConstraint;
    private boolean[] constraintChanged;

    private LocationComponent locationComponent;
    private NavigationMapRoute navigationMapRoute;
    private ReplayRouteLocationEngine mockLocationEngine;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(new LocationChangeListeningActivity());
    private int BEGIN_ROUTE_MILESTONE = 1001;
    private boolean reRoute = false;
    private Button recenterButton;
    private Button overViewRouteButton;
    private NavigationViewOptions.Builder navigationOptions;

    private Button stopNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_uinavigation_map);
        Mapbox.getInstance(this);

        CustomNavigationNotification customNotification = new CustomNavigationNotification(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            customNotification.createNotificationChannel(this);
        }
        MapboxNavigationOptions options = MapboxNavigationOptions.builder()
                .navigationNotification(customNotification)

                .build();

        mapboxNavigation = new MapboxNavigation(this, options);
        mapboxNavigation.addMilestone(
                new RouteMilestone.Builder()
                        .setIdentifier(BEGIN_ROUTE_MILESTONE)
                        .setInstruction(new BeginRouteInstruction())
                        .setTrigger(
                                Trigger.all(
                                        Trigger.lt(TriggerProperty.STEP_INDEX, 3),
                                        Trigger.gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 200),
                                        Trigger.gte(TriggerProperty.STEP_DISTANCE_TRAVELED_METERS, 75)
                                )
                        ).build()
        );

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_custom_uinavigation_map);
        initializeViews(savedInstanceState);
        navigationView.initialize(this,new CameraPosition.Builder()
                .target(new LatLng(origin.latitude(), origin.longitude()))
                .zoom(22)
                .build());
        navigationMapConstraint = new ConstraintSet();
        navigationMapConstraint.clone(customUINavigation);
        navigationMapExpandedConstraint = new ConstraintSet();
        navigationMapExpandedConstraint.clone(this, R.layout.activity_dual_navigation_expand_map);
        /// Hide default UI and show custom navigation UI
        navigationView.initViewConfig(true);
        constraintChanged = new boolean[]{false};
        launchNavigationFab.setOnClickListener(v -> {
            expandCollapse();
            launchNavigation();
        });
    }

    private void expandCollapse() {
        TransitionManager.beginDelayedTransition(customUINavigation);
        ConstraintSet constraint;
        if (constraintChanged[0]) {
            constraint = navigationMapConstraint;
        } else {
            constraint = navigationMapExpandedConstraint;
        }
        constraint.applyTo(customUINavigation);
        constraintChanged[0] = !constraintChanged[0];
    }

    private void launchNavigation() {
        launchNavigationFab.hide();
        navigationView.setVisibility(View.VISIBLE);
//        navigationView.
        mapboxNavigation.addOffRouteListener(this);
        // Tạo đối tượng MapboxNavigationOptions.Builder để cấu hình các tùy chọn
        MapboxNavigationOptions navigationOptions = MapboxNavigationOptions.builder()
                .maxTurnCompletionOffset(15.0)
                .maneuverZoneRadius(40.0)
                .maximumDistanceOffRoute(250.0)
                .deadReckoningTimeInterval(8.0)
                .maxManipulatedCourseAngle(25.0)
                .userLocationSnapDistance(10.0)
                .secondsBeforeReroute(60)

                .defaultMilestonesEnabled(true)
                .snapToRoute(true)
                .enableOffRouteDetection(true)
                .enableFasterRouteDetection(true)
                .manuallyEndNavigationUponCompletion(false)
                .metersRemainingTillArrival(50.0)
                .isFromNavigationUi(true)
                .minimumDistanceBeforeRerouting(100.0)
                .isDebugLoggingEnabled(false)
                .locationAcceptableAccuracyInMetersThreshold(50)
                .build();
        initNavigationOptions();
//        recenterButton.set

        NavigationPresenter presenter = navigationView.getNavigationPresenter();
        recenterButton.setOnClickListener(view -> presenter.onRecenterClick());
        overViewRouteButton.setOnClickListener(view -> presenter.onRouteOverviewClick());

        stopNavigation.setOnClickListener(view -> {
            mapboxNavigation.stopNavigation();
            expandCollapse();
        });
        changeNavigationActionState(true);
        mapboxNavigation.startNavigation(route);
        navigationView.startNavigation(this.navigationOptions.build());

    }

    private ProgressChangeListener progressChangeListener = (location, routeProgress) -> {
        System.out.println("Progress Changing------------------------");
//        System.out.println(routeProgress);
//        System.out.println(location);

    };
    private MilestoneEventListener milestoneEventListener = (routeProgress, s, milestone) -> {
    };
    private void initializeViews(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_custom_uinavigation_map);
        customUINavigation = findViewById(R.id.customUINavigation);
//        recenterButton = findViewById(R.id.customRecenterBtn);
        overViewRouteButton = findViewById(R.id.overViewRouteButton);
        stopNavigation = findViewById(R.id.stopNavigation);
        recenterButton = findViewById(R.id.recenterBtnCustom);
        mapView = findViewById(R.id.mapView);
        navigationView = findViewById(R.id.navigationView);
        loading = findViewById(R.id.loading);
        launchNavigationFab = findViewById(R.id.launchNavigation);
        navigationView.onCreate(savedInstanceState,null);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onRunning(boolean running) {
        System.out.println("-------------------------------------onRunning");
    }

    @Override
    public void onMoveBegin(@NonNull MoveGestureDetector detector) {
        System.out.println("onMoveBegin");

        LatLng centerCoordinate = mapboxMap.getCameraPosition().target;
        System.out.println(centerCoordinate);

    }

    @Override
    public void onMove(@NonNull MoveGestureDetector detector) {
        // get center location of map while user is moving map
        System.out.println("onMove");
        LatLng centerCoordinate = mapboxMap.getCameraPosition().target;
        System.out.println(centerCoordinate);
    }

    @Override
    public void onMoveEnd(@NonNull MoveGestureDetector detector) {
        // get center location of map after user stop moving map
        System.out.println("onMoveEnd");
        LatLng centerCoordinate = mapboxMap.getCameraPosition().target;
        System.out.println(centerCoordinate);
    }


    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<LocationChangeListeningActivity> activityWeakReference;

        LocationChangeListeningActivityLocationCallback(LocationChangeListeningActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            LocationChangeListeningActivity activity = activityWeakReference.get();
            List<Location> locations = result.getLocations();
            if (activity != null) {
                Location location = result.getLastLocation();
                System.out.println(location);
                if (location == null) {
                    return;
                }
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            System.out.println("GetLocationFailure---------------------Logg");
        }
    }
    private static class BeginRouteInstruction extends Instruction {

        @Override
        public String buildInstruction(RouteProgress routeProgress) {
            return "Have a safe trip!";
        }
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        origin = currentLocation;
        destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        updateLoadingTo(true);
        setCurrentMarkerPosition(point);
        if (origin != null) {
            fetchRoute(origin, destination);
        }
        return false;
    }


    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri(STYLE_URL), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                initLocationEngine();
                initListenGPS();
                enableLocationComponent(style);
                initMapRoute();
            }
        });
        this.mapboxMap.addOnMapClickListener(this);
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(Style style) {
        // Get an instance of the component
        locationComponent = mapboxMap.getLocationComponent();
        System.out.println("enableLocationComponent============================================================");
        if (locationComponent != null) {
            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, style).build()
            );
            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
            locationComponent.zoomWhileTracking(DEFAULT_CAMERA_ZOOM);
            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.GPS);
            locationComponent.setLocationEngine(locationEngine);
        }
    }
    void changeNavigationActionState(boolean isNavigationRunning){
        if(!isNavigationRunning){
            overViewRouteButton.setVisibility(View.GONE);
            recenterButton.setVisibility(View.GONE);
            stopNavigation.setVisibility(View.GONE);
        }else{

            overViewRouteButton.setVisibility(View.VISIBLE);
            recenterButton.setVisibility(View.VISIBLE);
            stopNavigation.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onNavigationReady(boolean isRunning) {

        isNavigationRunning = isRunning;
    }

    @Override
    public void onCancelNavigation() {

        changeNavigationActionState(false);
        navigationView.stopNavigation();
        expandCollapse();
    }

    @Override
    public void onNavigationFinished() {

        changeNavigationActionState(false);
    }

    @Override
    public void onNavigationRunning() {

    }

    @Override
    public boolean allowRerouteFrom(Point offRoutePoint) {
        return false;
    }

    @Override
    public void onOffRoute(Point offRoutePoint) {
        System.out.println("OnOffRoute--------------------------");
    }

    @Override
    public void onRerouteAlong(DirectionsRoute directionsRoute) {

    }

    @Override
    public void onFailedReroute(String errorMessage) {

    }

    @Override
    public void onArrival() {
        mapboxNavigation.stopNavigation();
        navigationView.stopNavigation();
        changeNavigationActionState(false);
        System.out.println("You're arrival---------------------------------------");
    }

    @Override
    public void onNewPrimaryRouteSelected(DirectionsRoute directionsRoute) {

        route = directionsRoute;
    }

    @Override
    public void userOffRoute(Location location) {
        Location targetLocation = new Location("");
        targetLocation.setLatitude(destination.latitude());
        targetLocation.setLongitude(destination.longitude());
       float distance= location.distanceTo(targetLocation);
        System.out.println(distance);
        Toast.makeText(this,"Rerouting",Toast.LENGTH_LONG);
        if(distance>100) {
            System.out.println("Rerouting---------------------------------------------------");
            reRoute = true;
            fetchRoute(Point.fromLngLat(location.getLongitude(), location.getLatitude()), destination);
        }else{

            System.out.println("Arrival---------------------------------------------------");
            Toast.makeText(this,"Bạn đã tới đích",Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {

    }
    void initNavigationOptions(){
        navigationOptions =NavigationViewOptions.builder()
            .navigationListener(this)
            .routeListener(this)
            .locationEngine(locationEngine)
            .shouldSimulateRoute(false)
            .progressChangeListener(progressChangeListener)
            .milestoneEventListener(milestoneEventListener)

            .directionsRoute(route)

                .onMoveListener(this);
    }
    @Override
    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        if (validRouteResponse(response)) {
            route = response.body().routes().get(0);
            initNavigationOptions();
            if (reRoute) {
                navigationView.updateCameraRouteOverview();

                mapboxNavigation.startNavigation(route);

                navigationView.startNavigation(navigationOptions.build());
                reRoute = false;
            } else {
                updateLoadingTo(false);
                launchNavigationFab.show();
                mapRoute.addRoutes(response.body().routes());
                if (isNavigationRunning) {
                    launchNavigation();
                }
            }
        }
    }

    @Override
    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
        System.out.println(call);
    }

    private boolean validRouteResponse(Response<DirectionsResponse> response) {
        return response.body() != null && !response.body().routes().isEmpty();
    }

    private void updateLoadingTo(boolean isVisible) {
        if (isVisible) {
            loading.setVisibility(View.VISIBLE);
        } else {
            loading.setVisibility(View.INVISIBLE);
        }
    }

    private void fetchRoute(Point origin, Point destination) {
        NavigationRoute builder = NavigationRoute.builder(this)
                .baseUrl("https://maps.vietmap.vn/api/navigations/route/")
                .apikey("95f852d9f8c38e08ceacfd456b59059d0618254a50d3854c")
                .origin(origin)
                .destination(destination)
                .alternatives(true)
                .build();
        builder.getRoute(this);
    }

    private void initMapRoute() {

        mapRoute = new NavigationMapRoute(mapView, mapboxMap);
        mapRoute.setOnRouteSelectionChangeListener(this);
        mapRoute.addProgressChangeListener(new MapboxNavigation(this));
    }

    private void initListenGPS() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> getCurrentLocation(), 5000);
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                            currentLocation = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                        }
                    });
            return;
        }
    }

    private void initLocationEngine() {
        mockLocationEngine = new ReplayRouteLocationEngine();
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        long DEFAULT_INTERVAL_IN_MILLISECONDS = 5000;
        long DEFAULT_MAX_WAIT_TIME = 30000;
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationEngine.getLastLocation(callback);
            mockLocationEngine.assignLastLocation(origin);
            locationEngine.requestLocationUpdates(request, callback, Looper.getMainLooper());
            return;
        }
    }

    private void setCurrentMarkerPosition(LatLng position) {
        if (position != null && currentMarker != null) {
            currentMarker.setPosition(position);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        navigationView.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationView.onResume();
        mapView.onResume();
        if (locationEngine != null) {
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        if (!navigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        navigationView.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navigationView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        navigationView.onPause();
        mapView.onPause();
        if (locationEngine != null) {
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        navigationView.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationView.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null) {
        }
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        updateLoadingTo(true);
        setCurrentMarkerPosition(point);
        if (origin != null) {
            fetchRoute(origin, destination);
        }
        return true;
    }
}