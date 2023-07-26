package vn.vietmap.services.android.navigation.v5.navigation;

import android.location.Location;

import androidx.annotation.NonNull;

import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineCallback;
import vn.vietmap.vietmapsdk.location.engine.LocationEngineResult;
import vn.vietmap.services.android.navigation.v5.location.LocationValidator;

class NavigationLocationEngineListener implements LocationEngineCallback<LocationEngineResult> {

    private final RouteProcessorBackgroundThread thread;
    private final LocationValidator validator;
    private final LocationEngine locationEngine;
    private VietmapNavigation vietmapNavigation;

    NavigationLocationEngineListener(RouteProcessorBackgroundThread thread, VietmapNavigation vietmapNavigation,
                                     LocationEngine locationEngine, LocationValidator validator) {
        this.thread = thread;
        this.vietmapNavigation = vietmapNavigation;
        this.locationEngine = locationEngine;
        this.validator = validator;
    }

    boolean isValidLocationUpdate(Location location) {
        return location != null && validator.isValidUpdate(location);
    }

    /**
     * Queues a new task created from a location update to be sent
     * to {@link RouteProcessorBackgroundThread} for processing.
     *
     * @param location to be processed
     */
    void queueLocationUpdate(Location location) {
        thread.queueUpdate(NavigationLocationUpdate.create(location, vietmapNavigation));
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        if (isValidLocationUpdate(result.getLastLocation())) {
            queueLocationUpdate(result.getLastLocation());
        }
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
    }
}