package com.mapbox.services.android.navigation.testapp;

import static com.mapbox.services.android.navigation.testapp.NavigationSettings.ACCESS_TOKEN;
import static com.mapbox.services.android.navigation.testapp.NavigationSettings.BASE_URL;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.utils.TextUtils;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.RouteListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationUITest extends AppCompatActivity implements NavigationListener, OnNavigationReadyCallback, RouteListener, ProgressChangeListener {

    private static final String DEFAULT_EMPTY_STRING = "";

    private static final String TEST_ROUTE_JSON = "test_route_json";

    private boolean dropoffDialogShown;

    private Location lastKnownLocation;

    private List<Point> points = new ArrayList<>();
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPoints();
        setContentView(R.layout.activity_navigation_uitest);
        navigationView = findViewById(R.id.navigationView);
        navigationView.onCreate(savedInstanceState,null);
        if (savedInstanceState != null) {
            navigationView.initialize(this);
        }
    }

    private void setPoints() {
        points.add(Point.fromLngLat(-77.04012393951416, 38.9111117447887));
        points.add(Point.fromLngLat(-77.03847169876099, 38.91113678979344));
        points.add(Point.fromLngLat(-77.03848242759705, 38.91040213277608));
        points.add(Point.fromLngLat(-77.03850388526917, 38.909650771013034));
        points.add(Point.fromLngLat(-77.03651905059814, 38.90894949285854));
    }

    @Override
    public void onStart() {
        super.onStart();
        navigationView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
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
    }

    @Override
    public void onStop() {
        super.onStop();
        navigationView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationView.onDestroy();
    }

    @Override
    public void onNavigationReady(boolean isRunning) {
//        if (isRunning) {
//            DirectionsRoute route = retrieveRouteForRotation();
//            if (route != null) {
//                navigationView.startNavigation(buildTestNavigationViewOptions(route));
//            }
//        }

        fetchRoute(points.remove(0), points.remove(0));
    }

    @Override
    public void onCancelNavigation() {
        finish();
    }

    @Override
    public void onNavigationFinished() {
        finish();
    }

    @Override
    public void onNavigationRunning() {

    }

    private NavigationViewOptions buildTestNavigationViewOptions(DirectionsRoute route) {
        return NavigationViewOptions.builder().directionsRoute(route).shouldSimulateRoute(true).build();
    }

    @Nullable
    private DirectionsRoute retrieveRouteForRotation() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String testRouteJson = preferences.getString(TEST_ROUTE_JSON, DEFAULT_EMPTY_STRING);
        if (TextUtils.isEmpty(testRouteJson)) {
            return null;
        }
        return DirectionsRoute.fromJson(testRouteJson);
    }

    @Override
    public boolean allowRerouteFrom(Point offRoutePoint) {
        return true;
    }

    @Override
    public void onOffRoute(Point offRoutePoint) {

    }

    @Override
    public void onRerouteAlong(DirectionsRoute directionsRoute) {

    }

    @Override
    public void onFailedReroute(String errorMessage) {

    }

    @Override
    public void onArrival() {
        if (!dropoffDialogShown && !points.isEmpty()) {
            showDropoffDialog();
            // Accounts for multiple arrival events
            dropoffDialogShown = true;
        }
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        lastKnownLocation = location;
    }

    private void startNavigation(DirectionsRoute directionsRoute) {
        NavigationViewOptions navigationViewOptions = setupOptions(directionsRoute);
        navigationView.startNavigation(navigationViewOptions);
    }

    private void showDropoffDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getString(R.string.dropoff_dialog_text));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dropoff_dialog_positive_text), (dialogInterface, in) -> fetchRoute(getLastKnownLocation(), points.remove(0)));
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dropoff_dialog_negative_text), (dialogInterface, in) -> {
            // Do nothing
        });
        alertDialog.show();
    }

    private void fetchRoute(Point origin, Point destination) {
        NavigationRoute.builder(this).apikey(ACCESS_TOKEN).origin(origin).destination(destination).alternatives(true).baseUrl(BASE_URL).build().getRoute(new Callback<DirectionsResponse>() {

            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (!response.body().routes().isEmpty()) {
                    startNavigation(response.body().routes().get(0));
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {

            }
        });
    }

    private NavigationViewOptions setupOptions(DirectionsRoute directionsRoute) {
        dropoffDialogShown = false;
        NavigationViewOptions.Builder options = NavigationViewOptions.builder();
        options.directionsRoute(directionsRoute).navigationListener(this).progressChangeListener(this).routeListener(this).shouldSimulateRoute(true);
        return options.build();
    }

    private Point getLastKnownLocation() {
        return Point.fromLngLat(lastKnownLocation.getLongitude(), lastKnownLocation.getLatitude());
    }
}