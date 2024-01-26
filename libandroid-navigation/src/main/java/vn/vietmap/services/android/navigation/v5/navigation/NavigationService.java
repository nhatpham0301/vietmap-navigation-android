package vn.vietmap.services.android.navigation.v5.navigation;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.mapbox.api.directions.v5.models.DirectionsRoute;

import vn.vietmap.services.android.navigation.v5.navigation.notification.NavigationNotification;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.services.android.navigation.v5.location.LocationValidator;
import vn.vietmap.services.android.navigation.v5.route.FasterRoute;
import vn.vietmap.services.android.navigation.v5.route.RouteFetcher;

import timber.log.Timber;

/**
 * Internal usage only, use navigation by initializing a new instance of {@link VietmapNavigation}
 * and customizing the navigation experience through that class.
 * <p>
 * This class is first created and started when {@link VietmapNavigation#startNavigation(DirectionsRoute)}
 * get's called and runs in the background until either the navigation sessions ends implicitly or
 * the hosting activity gets destroyed. Location updates are also tracked and handled inside this
 * service. Thread creation gets created in this service and maintains the thread until the service
 * gets destroyed.
 * </p>
 */
public class NavigationService extends Service {

    private final IBinder localBinder = new LocalBinder();
    private RouteProcessorBackgroundThread thread;
    private NavigationLocationEngineUpdater locationEngineUpdater;
    private RouteFetcher routeFetcher;
    private NavigationNotificationProvider notificationProvider;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    /**
     * Only should be called once since we want the service to continue running until the navigation
     * session ends.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (locationEngineUpdater != null)
            locationEngineUpdater.removeLocationEngineListener();
        super.onDestroy();
    }

    /**
     * This gets called when {@link VietmapNavigation#startNavigation(DirectionsRoute)} is called and
     * setups variables among other things on the Navigation Service side.
     */
    void startNavigation(VietmapNavigation vietmapNavigation) {
        initialize(vietmapNavigation);
        startForegroundNotification(notificationProvider.retrieveNotification());
        locationEngineUpdater.forceLocationUpdate(vietmapNavigation.getRoute());
    }

    /**
     * Removes the location / route listeners and  quits the thread.
     */
    void endNavigation() {
        routeFetcher.clearListeners();
        locationEngineUpdater.removeLocationEngineListener();
        notificationProvider.shutdown(getApplication());
        thread.quit();
    }

    /**
     * Called with {@link VietmapNavigation#setLocationEngine(LocationEngine)}.
     * Updates this service with the new {@link LocationEngine}.
     *
     * @param locationEngine to update the provider
     */
    void updateLocationEngine(LocationEngine locationEngine) {
        locationEngineUpdater.updateLocationEngine(locationEngine);
    }

    private void initialize(VietmapNavigation vietmapNavigation) {
        NavigationEventDispatcher dispatcher = vietmapNavigation.getEventDispatcher();
        initializeRouteFetcher(dispatcher, vietmapNavigation.retrieveEngineProvider());
        initializeNotificationProvider(vietmapNavigation);
        initializeRouteProcessorThread(dispatcher, routeFetcher, notificationProvider);
        initializeLocationProvider(vietmapNavigation);
    }

    private void initializeRouteFetcher(NavigationEventDispatcher dispatcher, NavigationEngineFactory engineProvider) {
        FasterRoute fasterRouteEngine = engineProvider.retrieveFasterRouteEngine();
        NavigationFasterRouteListener listener = new NavigationFasterRouteListener(dispatcher, fasterRouteEngine);
        routeFetcher = new RouteFetcher(getApplication());
        routeFetcher.addRouteListener(listener);
    }

    private void initializeNotificationProvider(VietmapNavigation vietmapNavigation) {
        notificationProvider = new NavigationNotificationProvider(getApplication(), vietmapNavigation);
    }

    private void initializeRouteProcessorThread(NavigationEventDispatcher dispatcher, RouteFetcher routeFetcher,
                                                NavigationNotificationProvider notificationProvider) {
        RouteProcessorThreadListener listener = new RouteProcessorThreadListener(
                dispatcher, routeFetcher, notificationProvider
        );
        thread = new RouteProcessorBackgroundThread(new Handler(), listener);
    }

    private void initializeLocationProvider(VietmapNavigation vietmapNavigation) {
        LocationEngine locationEngine = vietmapNavigation.getLocationEngine();
        int accuracyThreshold = vietmapNavigation.options().locationAcceptableAccuracyInMetersThreshold();
        LocationValidator validator = new LocationValidator(accuracyThreshold);
        NavigationLocationEngineListener listener = new NavigationLocationEngineListener(
                thread, vietmapNavigation, locationEngine, validator
        );
        locationEngineUpdater = new NavigationLocationEngineUpdater(locationEngine, listener);
    }

    private void startForegroundNotification(NavigationNotification navigationNotification) {
        Notification notification = navigationNotification.getNotification();
        int notificationId = navigationNotification.getNotificationId();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(notificationId, notification);
    }

    class LocalBinder extends Binder {
        NavigationService getService() {
            Timber.d("Local binder called.");
            return NavigationService.this;
        }
    }
}