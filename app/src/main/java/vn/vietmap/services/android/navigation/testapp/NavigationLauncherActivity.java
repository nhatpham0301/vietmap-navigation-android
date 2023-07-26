package vn.vietmap.services.android.navigation.testapp;

import static vn.vietmap.services.android.navigation.testapp.NavigationSettings.ACCESS_TOKEN;
import static vn.vietmap.services.android.navigation.testapp.NavigationSettings.BASE_URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.camera.CameraPosition;
import vn.vietmap.vietmapsdk.camera.CameraUpdate;
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory;
import vn.vietmap.vietmapsdk.exceptions.InvalidLatLngBoundsException;
import vn.vietmap.vietmapsdk.geometry.LatLng;
import vn.vietmap.vietmapsdk.geometry.LatLngBounds;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineCallback;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineRequest;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineResult;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.services.android.navigation.ui.v5.NavigationLauncher;
import vn.vietmap.services.android.navigation.ui.v5.NavigationLauncherOptions;
import vn.vietmap.services.android.navigation.ui.v5.camera.CameraUpdateMode;
import vn.vietmap.services.android.navigation.ui.v5.camera.NavigationCameraUpdate;
import vn.vietmap.services.android.navigation.ui.v5.map.NavigationVietmapGL;
import vn.vietmap.services.android.navigation.ui.v5.route.OnRouteSelectionChangeListener;
import vn.vietmap.services.android.navigation.v5.location.engine.LocationEngineProvider;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationRoute;
import vn.vietmap.services.android.navigation.v5.offroute.OffRouteListener;
import vn.vietmap.services.android.navigation.v5.utils.LocaleUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class NavigationLauncherActivity extends AppCompatActivity implements OnMapReadyCallback, VietMapGL.OnMapLongClickListener, OnRouteSelectionChangeListener, OffRouteListener {

    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_CAMERA_ZOOM = 16;
    private static final int CHANGE_SETTING_REQUEST_CODE = 1;
    private static final int INITIAL_ZOOM = 25;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;

    private final NavigationLauncherLocationCallback callback = new NavigationLauncherLocationCallback(this);
    private final LocaleUtils localeUtils = new LocaleUtils();
    private final List<Point> wayPoints = new ArrayList<>();
    private LocationEngine locationEngine;
    private NavigationVietmapGL navigationVietmapGL;
    private DirectionsRoute route;
    private Point currentLocation;
    private boolean locationFound;

    MapView mapView;
    Button launchRouteBtn;
    ProgressBar loading;
    FrameLayout launchBtnFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vietmap.getInstance(this);
        setContentView(R.layout.activity_navigation_launcher);
        ButterKnife.bind(this);
        mapView = findViewById(R.id.mapView);
        launchRouteBtn = findViewById(R.id.launch_route_btn);
        loading = findViewById(R.id.loading);
        launchBtnFrame = findViewById(R.id.launch_btn_frame);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        launchRouteBtn.setOnClickListener(v -> {
            launchNavigationWithRoute();
            System.out.print("launchNavigationWithRoute");
        });
    }

    private void launchNavigationWithRoute() {
        if (route == null) {
            Snackbar.make(mapView, R.string.error_route_not_available, Snackbar.LENGTH_SHORT).show();
            return;
        }

        NavigationLauncherOptions.Builder optionsBuilder = NavigationLauncherOptions.builder()
                .shouldSimulateRoute(true);
        CameraPosition initialPosition = new CameraPosition.Builder()
                .target(new LatLng(currentLocation.latitude(), currentLocation.longitude()))
                .zoom(INITIAL_ZOOM)
                .build();
        optionsBuilder.initialMapCameraPosition(initialPosition);

        optionsBuilder.directionsRoute(route);

        NavigationLauncher.startNavigation(this, optionsBuilder.build());
    }

    @Override
    public void userOffRoute(Location location) {
        System.out.print("off route" +
                "");
    }

    void updateCurrentLocation(Point currentLocation) {
        this.currentLocation = currentLocation;
    }

    private void animateCamera(LatLng point) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, DEFAULT_CAMERA_ZOOM);
        NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
        navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
        navigationVietmapGL.retrieveCamera().update(navigationCameraUpdate, CAMERA_ANIMATION_DURATION);
    }

    void onLocationFound(Location location) {
        navigationVietmapGL.updateLocation(location);
        if (!locationFound) {
            animateCamera(new LatLng(location.getLatitude(), location.getLongitude()));
            Snackbar.make(mapView, R.string.explanation_long_press_waypoint, Snackbar.LENGTH_LONG).show();
            locationFound = true;
            hideLoading();
        }
    }

    private void hideLoading() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.INVISIBLE);
        }
    }

    private void setCurrentMarkerPosition(LatLng position) {
        if (position != null) {
            navigationVietmapGL.addMarker(this, Point.fromLngLat(position.getLongitude(), position.getLatitude()));
        }
    }

    @NonNull
    private LocationEngineRequest buildEngineRequest() {
        return new LocationEngineRequest.Builder(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .build();
    }

    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(getApplicationContext());
        LocationEngineRequest request = buildEngineRequest();
        locationEngine.requestLocationUpdates(request, callback, null);
        locationEngine.getLastLocation(callback);
    }

    private String getRouteProfileFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(
                getString(R.string.route_profile_key), DirectionsCriteria.PROFILE_DRIVING_TRAFFIC
        );
    }
    private Locale getLanguageFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultLanguage = getString(R.string.default_locale);
        String language = sharedPreferences.getString(getString(R.string.language_key), defaultLanguage);
        if (language.equals(defaultLanguage)) {
            return localeUtils.inferDeviceLocale(this);
        } else {
            return new Locale(language);
        }
    }

    private String getUnitTypeFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultUnitType = getString(R.string.default_unit_type);
        String unitType = sharedPreferences.getString(getString(R.string.unit_type_key), defaultUnitType);
        if (unitType.equals(defaultUnitType)) {
            unitType = localeUtils.getUnitTypeForDeviceLocale(this);
        }

        return unitType;
    }
    private void setFieldsFromSharedPreferences(NavigationRoute.Builder builder) {
        builder
                .language(getLanguageFromSharedPreferences())
                .voiceUnits(getUnitTypeFromSharedPreferences());
    }

    private boolean validRouteResponse(Response<DirectionsResponse> response) {
        return response.body() != null && !response.body().routes().isEmpty();
    }

    public void boundCameraToRoute() {
        if (route != null) {
            List<Point> routeCoords = LineString.fromPolyline(route.geometry(),
                    Constants.PRECISION_6).coordinates();
            List<LatLng> bboxPoints = new ArrayList<>();
            for (Point point : routeCoords) {
                bboxPoints.add(new LatLng(point.latitude(), point.longitude()));
            }
            if (bboxPoints.size() > 1) {
                try {
                    LatLngBounds bounds = new LatLngBounds.Builder().includes(bboxPoints).build();
                    // left, top, right, bottom
                    int topPadding = launchBtnFrame.getHeight() * 2;
                    animateCameraBbox(bounds, CAMERA_ANIMATION_DURATION, new int[] {50, topPadding, 50, 100});
                } catch (InvalidLatLngBoundsException exception) {
                    Toast.makeText(this, R.string.error_valid_route_not_found, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void animateCameraBbox(LatLngBounds bounds, int animationTime, int[] padding) {
        CameraPosition position = navigationVietmapGL.retrieveMap().getCameraForLatLngBounds(bounds, padding);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);


        NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
        navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
        navigationVietmapGL.retrieveCamera().update(navigationCameraUpdate, animationTime);
    }
    private void fetchRoute() {
        NavigationRoute builder = NavigationRoute.builder(this)
                .apikey(ACCESS_TOKEN)
                .baseUrl(BASE_URL)
                .voiceUnits(DirectionsCriteria.METRIC)
                .origin(currentLocation)
                .destination(Point.fromLngLat(wayPoints.get(0).longitude(), wayPoints.get(0).latitude()))
                .profile(getRouteProfileFromSharedPreferences())
                .alternatives(true).build();

//        setFieldsFromSharedPreferences(builder);
        builder.getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (validRouteResponse(response)) {
                    hideLoading();
                    route = response.body().routes().get(0);
                    if (route.distance() > 25d) {
                        launchRouteBtn.setEnabled(true);
                        navigationVietmapGL.drawRoutes(response.body().routes());
                        boundCameraToRoute();
                    } else {
                        Snackbar.make(mapView, R.string.error_select_longer_route, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e(throwable, "onFailure: navigation.getRoute()");
            }
        });
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapReady(@NonNull VietMapGL mapboxMap) {
        NavigationLauncherActivity activity = this;
        mapboxMap.setStyle(new Style.Builder().fromUri(getString(R.string.map_view_style_url)), style -> {
            mapboxMap.addOnMapLongClickListener(activity);
            navigationVietmapGL = new NavigationVietmapGL(mapView, mapboxMap);
            navigationVietmapGL.setOnRouteSelectionChangeListener(activity);
            navigationVietmapGL.updateLocationLayerRenderMode(RenderMode.COMPASS);
            initializeLocationEngine();
        });
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        if (wayPoints.size() == 2) {
            Snackbar.make(mapView, "Max way points exceeded. Clearing route...", Snackbar.LENGTH_SHORT).show();
            wayPoints.clear();
            navigationVietmapGL.clearMarkers();
            navigationVietmapGL.removeRoute();
            return false;
        }
        wayPoints.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
        launchRouteBtn.setEnabled(false);
        loading.setVisibility(View.VISIBLE);
        setCurrentMarkerPosition(point);
        if (locationFound) {
            fetchRoute();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onNewPrimaryRouteSelected(DirectionsRoute directionsRoute) {
        route = directionsRoute;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates(buildEngineRequest(), callback, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    private static class NavigationLauncherLocationCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<NavigationLauncherActivity> activityWeakReference;

        NavigationLauncherLocationCallback(NavigationLauncherActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            NavigationLauncherActivity activity = activityWeakReference.get();
            if (activity != null) {
                Location location = result.getLastLocation();
                if (location == null) {
                    return;
                }
                activity.updateCurrentLocation(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
                activity.onLocationFound(location);
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            Timber.e(exception);
        }
    }
}