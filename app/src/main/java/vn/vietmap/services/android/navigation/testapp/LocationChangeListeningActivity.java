package vn.vietmap.services.android.navigation.testapp;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;

import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineCallback;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineRequest;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineResult;
import vn.vietmap.vietmapsdk.location.permissions.PermissionsManager;

import vn.vietmap.vietmapsdk.location.permissions.PermissionsListener;
import vn.vietmap.vietmapsdk.Vietmap;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.location.modes.RenderMode;
import vn.vietmap.vietmapsdk.maps.MapView;
import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.services.android.navigation.v5.location.engine.LocationEngineProvider;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Use the Mapbox Core Library to receive updates when the device changes location.
 */
public class LocationChangeListeningActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    VietMapGL vietMapGL;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g1");
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Vietmap.getInstance(this);

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_location_change_listening);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final VietMapGL vietMapGL) {
        this.vietMapGL = vietMapGL;

//        vietMapGL.setStyle(Style.TRAFFIC_NIGHT,
//                new Style.OnStyleLoaded() {
//                    @Override public void onStyleLoaded(@NonNull Style style) {
//                        enableLocationComponent(style);
//                    }
//                });
    }

    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g2");
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = vietMapGL.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g3");
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.getLastLocation(callback);
        locationEngine.requestLocationUpdates(request, callback, getMainLooper());

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g4");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g5");
    }

    @Override
    public void onPermissionResult(boolean granted) {
        System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g6");
        if (granted) {
            System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g7");
            vietMapGL.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g8");
        }
    }

    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<LocationChangeListeningActivity> activityWeakReference;

        LocationChangeListeningActivityLocationCallback(LocationChangeListeningActivity activity) {
            System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g9");
            System.out.println(activity);
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g10");
            LocationChangeListeningActivity activity = activityWeakReference.get();

            if (activity != null) {
                System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g11");
                Location location = result.getLastLocation();

                System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g12");
                if (location == null) {
                    return;
                }

                System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g13");
                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.vietMapGL != null && result.getLastLocation() != null) {
                    System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g14");
                    activity.vietMapGL.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                    System.out.println("Loggg-gg--g-g-g-g-g-g--g-g-g-g-g--g-g-g--g-g15");
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can't be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            LocationChangeListeningActivity activity = activityWeakReference.get();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
