package com.mapbox.services.android.navigation.testapp;

import static com.mapbox.services.android.navigation.testapp.NavigationSettings.ACCESS_TOKEN;
import static com.mapbox.services.android.navigation.testapp.NavigationSettings.BASE_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
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
import com.mapbox.services.android.navigation.ui.v5.camera.CameraUpdateMode;
import com.mapbox.services.android.navigation.ui.v5.camera.NavigationCameraUpdate;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMap;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.instruction.Instruction;
import com.mapbox.services.android.navigation.v5.location.engine.LocationEngineProvider;
import com.mapbox.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
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
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class sample_3 extends AppCompatActivity implements OnMapReadyCallback,
        MapboxMap.OnMapClickListener, ProgressChangeListener, NavigationEventListener,
        MilestoneEventListener, OffRouteListener {
    private static final int BEGIN_ROUTE_MILESTONE = 1001;
    private static final double TWENTY_FIVE_METERS = 25d;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;
    // Map variables
    MapView mapView;

    FloatingActionButton newLocationFab;

    Button startRouteButton;

    private MapboxMap mapboxMap;

    // Navigation related variables
    private LocationEngine locationEngine;
    private MapboxNavigation navigation;
    private DirectionsRoute route;
    private NavigationMapRoute navigationMapRoute;
    private Point destination;
    private Point waypoint;
    private Point currentLocation;
    private boolean locationFound;
    private boolean isRefreshing = false;
    private final NavigationLauncherLocationCallback callback = new NavigationLauncherLocationCallback(this);

    private static class NavigationLauncherLocationCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<sample_3> activityWeakReference;

        NavigationLauncherLocationCallback(sample_3 activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            sample_3 activity = activityWeakReference.get();
            if (activity != null) {
                Location location = result.getLastLocation();
                if (location == null) {
                    return;
                }
                activity.updateCurrentLocation(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
//                activity.onLocationFound(location);
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            Timber.e(exception);
        }
    }

    private static class MyBroadcastReceiver extends BroadcastReceiver {
        private final WeakReference<MapboxNavigation> weakNavigation;

        MyBroadcastReceiver(MapboxNavigation navigation) {
            this.weakNavigation = new WeakReference<>(navigation);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MapboxNavigation navigation = weakNavigation.get();
            navigation.stopNavigation();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample3);
        ButterKnife.bind(this);
        mapView = findViewById(R.id.mapView);
        startRouteButton = findViewById(R.id.startRouteButton);
        newLocationFab = findViewById(R.id.newLocationFab);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Context context = getApplicationContext();
        CustomNavigationNotification customNotification = new CustomNavigationNotification(context);

        MapboxNavigationOptions options = MapboxNavigationOptions.builder()
                .navigationNotification(customNotification)
                .build();

        navigation = new MapboxNavigation(this, options);
        navigation.addMilestone(
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
        customNotification.register(new sample_3.MyBroadcastReceiver(navigation), context);

        startRouteButton.setOnClickListener(v -> {
            launchNavigationWithRoute();
        });
    }

    public void launchNavigationWithRoute() {
        boolean isValidNavigation = navigation != null;
        boolean isValidRoute = route != null && route.distance() > TWENTY_FIVE_METERS;
        if (isValidNavigation && isValidRoute) {

            // Hide the start button
            startRouteButton.setVisibility(View.INVISIBLE);

            // Attach all of our navigation listeners.
            navigation.addNavigationEventListener(this);
            navigation.addProgressChangeListener(this);
            navigation.addMilestoneEventListener(this);
            navigation.addOffRouteListener(this);

            navigation.setLocationEngine(locationEngine);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mapboxMap.getLocationComponent().setLocationComponentEnabled(true);
            navigation.startNavigation(route);
            mapboxMap.removeOnMapClickListener(this);
        }
    }

    void updateCurrentLocation(Point currentLocation) {
        this.currentLocation = currentLocation;
    }

    @NonNull
    private LocationEngineRequest buildEngineRequest() {
        return new LocationEngineRequest.Builder(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .build();
    }

    private void initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(getApplicationContext());
        LocationEngineRequest request = buildEngineRequest();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationEngine.requestLocationUpdates(request, callback, null);
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.mapboxMap.addOnMapClickListener(this);
        mapboxMap.setStyle(new Style.Builder().fromUri(getString(R.string.map_view_style_url)), style -> {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            if (locationComponent != null) {
                locationComponent.activateLocationComponent(
                        LocationComponentActivationOptions.builder(this, style).build()
                );
                locationComponent.setLocationComponentEnabled(true);
                locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
                locationComponent.setRenderMode(RenderMode.NORMAL);
            }
            navigationMapRoute = new NavigationMapRoute(navigation, mapView, mapboxMap);
            initializeLocationEngine();
        });
    }
    private static class BeginRouteInstruction extends Instruction {
        @Override
        public String buildInstruction(RouteProgress routeProgress) {
            return "Have a safe trip!";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigation.onDestroy();
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        if (destination == null) {
            destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        } else if (waypoint == null) {
            waypoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        } else {
            Toast.makeText(this, "Only 2 waypoints supported", Toast.LENGTH_LONG).show();
        }
        mapboxMap.addMarker(new MarkerOptions().position(point));
        calculateRoute();
        return false;
    }

    @SuppressLint("MissingPermission")
    private void calculateRoute() {
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                fetchRoute(result);
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                Timber.e(exception);
            }
        });
    }

    private void fetchRoute(LocationEngineResult result) {
        Location userLocation = result.getLastLocation();
        if (userLocation == null) {
            Timber.d("calculateRoute: User location is null, therefore, origin can't be set.");
            return;
        }
        Point origin = Point.fromLngLat(userLocation.getLongitude(), userLocation.getLatitude());
        if (TurfMeasurement.distance(origin, destination, TurfConstants.UNIT_METERS) < 50) {
            startRouteButton.setVisibility(View.GONE);
            return;
        }
        if (waypoint == null) {
            return;
        }

        NavigationRoute builder = NavigationRoute.builder(this)
                .apikey(ACCESS_TOKEN)
                .baseUrl(BASE_URL)
                .voiceUnits(DirectionsCriteria.METRIC)
                .origin(currentLocation)
                .destination(waypoint)
                .alternatives(true).build();
        builder.getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                Timber.d("Url: %s", call.request().url().toString());
                if (response.body() != null) {
                    if (!response.body().routes().isEmpty()) {
                        route = response.body().routes().get(0);
                        navigationMapRoute.addRoutes(response.body().routes());
                        startRouteButton.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable throwable) {
                Timber.e(throwable, "onFailure: navigation.getRoute()");
            }
        });
    }

    @Override
    public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {

    }

    @Override
    public void onRunning(boolean running) {
        if (running) {
            Timber.d("onRunning: Started");
        } else {
            Timber.d("onRunning: Stopped");
        }
    }

    @Override
    public void userOffRoute(Location location) {
        Timber.d("userOffRoute");
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        mapboxMap.getLocationComponent().forceLocationUpdate(location);
        if (!isRefreshing) {
            isRefreshing = true;
        }
        Timber.d("onProgressChange: fraction of route traveled");
    }
}