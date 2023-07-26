package vn.vietmap.services.android.navigation.ui.v5;

import android.app.Application;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.mapbox.api.directions.v5.models.BannerInstructions;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.geojson.Point;

import vn.vietmap.services.android.navigation.ui.v5.camera.DynamicCamera;
import vn.vietmap.services.android.navigation.ui.v5.instruction.BannerInstructionModel;
import vn.vietmap.services.android.navigation.ui.v5.instruction.InstructionModel;
import vn.vietmap.services.android.navigation.ui.v5.summary.SummaryModel;
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechAnnouncement;
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechPlayer;
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechPlayerProvider;
import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.services.android.navigation.ui.v5.voice.NavigationSpeechPlayer;
import vn.vietmap.services.android.navigation.v5.milestone.BannerInstructionMilestone;
import vn.vietmap.services.android.navigation.v5.milestone.Milestone;
import vn.vietmap.services.android.navigation.v5.milestone.MilestoneEventListener;
import vn.vietmap.services.android.navigation.v5.milestone.VoiceInstructionMilestone;
import vn.vietmap.services.android.navigation.v5.navigation.MapboxNavigation;
import vn.vietmap.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationEventListener;
import vn.vietmap.services.android.navigation.v5.navigation.NavigationTimeFormat;
import vn.vietmap.services.android.navigation.v5.navigation.camera.Camera;
import vn.vietmap.services.android.navigation.v5.offroute.OffRouteListener;
import vn.vietmap.services.android.navigation.v5.route.FasterRouteListener;
import vn.vietmap.services.android.navigation.v5.route.RouteFetcher;
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;
import vn.vietmap.services.android.navigation.v5.utils.DistanceFormatter;
import vn.vietmap.services.android.navigation.v5.utils.LocaleUtils;
import vn.vietmap.services.android.navigation.v5.utils.RouteUtils;

import org.jetbrains.annotations.TestOnly;

import java.util.List;

public class NavigationViewModel extends AndroidViewModel {

    public static final String EMPTY_STRING = "";
    public static final String OKHTTP_INSTRUCTION_CACHE = "okhttp-instruction-cache";
    public static final long TEN_MEGABYTE_CACHE_SIZE = 10 * 1024 * 1024;

    public final MutableLiveData<InstructionModel> instructionModel = new MutableLiveData<>();
    public final MutableLiveData<BannerInstructionModel> bannerInstructionModel = new MutableLiveData<>();
    public final MutableLiveData<SummaryModel> summaryModel = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isOffRoute = new MutableLiveData<>();
    public final MutableLiveData<Location> navigationLocation = new MutableLiveData<>();
    public final MutableLiveData<DirectionsRoute> route = new MutableLiveData<>();
    public final MutableLiveData<Boolean> shouldRecordScreenshot = new MutableLiveData<>();
    public final MutableLiveData<Point> destination = new MutableLiveData<>();

    public MapboxNavigation navigation;
    public NavigationViewRouter router;
    public LocationEngineConductor locationEngineConductor;
    public NavigationViewEventDispatcher navigationViewEventDispatcher;
    public SpeechPlayer speechPlayer;
    public int voiceInstructionsToAnnounce = 0;
    public RouteProgress routeProgress;

    Milestone milestone;
    public String language;
    public RouteUtils routeUtils;
    public LocaleUtils localeUtils;
    public DistanceFormatter distanceFormatter;
    @NavigationTimeFormat.Type
    public int timeFormatType;
    public boolean isRunning;
    public boolean isChangingConfigurations;
    public MapConnectivityController connectivityController;

    public NavigationViewModel(Application application) {
        super(application);
        initializeLocationEngine();
        initializeRouter();
        this.routeUtils = new RouteUtils();
        this.localeUtils = new LocaleUtils();
        this.connectivityController = new MapConnectivityController();
    }

    @TestOnly
        // Package public (no modifier) for testing purposes
    NavigationViewModel(Application application, MapboxNavigation navigation,
                        MapConnectivityController connectivityController,
                        NavigationViewRouter router) {
        super(application);
        this.navigation = navigation;
        this.router = router;
        this.connectivityController = connectivityController;
    }

    @TestOnly
        // Package public (no modifier) for testing purposes
    NavigationViewModel(Application application, MapboxNavigation navigation,
                        LocationEngineConductor conductor, NavigationViewEventDispatcher dispatcher, SpeechPlayer speechPlayer) {
        super(application);
        this.navigation = navigation;
        this.locationEngineConductor = conductor;
        this.navigationViewEventDispatcher = dispatcher;
        this.speechPlayer = speechPlayer;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        destroyRouter();
    }

    public void onDestroy(boolean isChangingConfigurations) {
        this.isChangingConfigurations = isChangingConfigurations;
        if (!isChangingConfigurations) {
            endNavigation();
            deactivateInstructionPlayer();
            isRunning = false;
        }
        clearDynamicCameraMap();
        navigationViewEventDispatcher = null;
    }

    public void setMuted(boolean isMuted) {
        speechPlayer.setMuted(isMuted);
    }


    /**
     * Returns the current instance of {@link MapboxNavigation}.
     * <p>
     * Will be null if navigation has not been initialized.
     */
    @Nullable
    public MapboxNavigation retrieveNavigation() {
        return navigation;
    }

    void initializeEventDispatcher(NavigationViewEventDispatcher navigationViewEventDispatcher) {
        this.navigationViewEventDispatcher = navigationViewEventDispatcher;
    }

    /**
     * This method will pass {@link MapboxNavigationOptions} from the {@link NavigationViewOptions}
     * to this view model to be used to initialize {@link MapboxNavigation}.
     *
     * @param options to init MapboxNavigation
     */
    void initialize(NavigationViewOptions options) {
        MapboxNavigationOptions navigationOptions = options.navigationOptions();
        navigationOptions = navigationOptions.toBuilder().isFromNavigationUi(true).build();
        initializeLanguage(options);
        initializeTimeFormat(navigationOptions);
        initializeDistanceFormatter(options);
        if (!isRunning()) {
            LocationEngine locationEngine = initializeLocationEngineFrom(options);
            initializeNavigation(getApplication(), navigationOptions, locationEngine);
            addMilestones(options);
            initializeNavigationSpeechPlayer(options);
        }
        router.extractRouteOptions(options);
    }

    boolean isRunning() {
        return isRunning;
    }

    boolean isMuted() {
        if (speechPlayer == null) {
            return false;
        }
        return speechPlayer.isMuted();
    }

    void stopNavigation() {
        navigation.removeProgressChangeListener(null);
        navigation.removeMilestoneEventListener(null);
        navigation.stopNavigation();
    }

    boolean isOffRoute() {
        try {
            return isOffRoute.getValue();
        } catch (NullPointerException exception) {
            return false;
        }
    }

    void updateRoute(DirectionsRoute route) {
        this.route.setValue(route);
        if (!isChangingConfigurations) {
            startNavigation(route);
            updateReplayEngine(route);
            sendEventOnRerouteAlong(route);
            isOffRoute.setValue(false);
        }
        resetConfigurationFlag();
    }

    void updateRouteProgress(RouteProgress routeProgress) {
        this.routeProgress = routeProgress;
        sendEventArrival(routeProgress, milestone);
        instructionModel.setValue(new InstructionModel(distanceFormatter, routeProgress));
        summaryModel.setValue(new SummaryModel(getApplication(), distanceFormatter, routeProgress, timeFormatType));
    }

    void updateLocation(Location location) {
        router.updateLocation(location);
        navigationLocation.setValue(location);
    }

    void sendEventFailedReroute(String errorMessage) {
        if (navigationViewEventDispatcher != null) {
            navigationViewEventDispatcher.onFailedReroute(errorMessage);
        }
    }

    MutableLiveData<Location> retrieveNavigationLocation() {
        return navigationLocation;
    }

    MutableLiveData<DirectionsRoute> retrieveRoute() {
        return route;
    }

    MutableLiveData<Point> retrieveDestination() {
        return destination;
    }

    MutableLiveData<Boolean> retrieveShouldRecordScreenshot() {
        return shouldRecordScreenshot;
    }

    public void initializeRouter() {
        RouteFetcher onlineRouter = new RouteFetcher(getApplication());
        Context applicationContext = getApplication().getApplicationContext();
        ConnectivityStatusProvider connectivityStatus = new ConnectivityStatusProvider(applicationContext);
        router = new NavigationViewRouter(onlineRouter, connectivityStatus, routeEngineListener);
    }

    public void initializeLocationEngine() {
        locationEngineConductor = new LocationEngineConductor();
    }

    public void initializeLanguage(NavigationUiOptions options) {
        RouteOptions routeOptions = options.directionsRoute().routeOptions();
        language = localeUtils.inferDeviceLanguage(getApplication());
        if (routeOptions != null) {
            language = routeOptions.language();
        }
    }

    public String initializeUnitType(NavigationUiOptions options) {
        RouteOptions routeOptions = options.directionsRoute().routeOptions();
        String unitType = localeUtils.getUnitTypeForDeviceLocale(getApplication());
        if (routeOptions != null) {
            unitType = routeOptions.voiceUnits();
        }
        return unitType;
    }

    public void initializeTimeFormat(MapboxNavigationOptions options) {
        timeFormatType = options.timeFormatType();
    }

    public int initializeRoundingIncrement(NavigationViewOptions options) {
        MapboxNavigationOptions navigationOptions = options.navigationOptions();
        return navigationOptions.roundingIncrement();
    }

    public void initializeDistanceFormatter(NavigationViewOptions options) {
        String unitType = initializeUnitType(options);
        int roundingIncrement = initializeRoundingIncrement(options);
        distanceFormatter = new DistanceFormatter(getApplication(), language, unitType, roundingIncrement);
    }

    public void initializeNavigationSpeechPlayer(NavigationViewOptions options) {
        SpeechPlayer speechPlayer = options.speechPlayer();
        if (speechPlayer != null) {
            this.speechPlayer = speechPlayer;
            return;
        }
        boolean isVoiceLanguageSupported = options.directionsRoute().voiceLanguage() != null;
        SpeechPlayerProvider speechPlayerProvider = initializeSpeechPlayerProvider(isVoiceLanguageSupported);
        this.speechPlayer = new NavigationSpeechPlayer(speechPlayerProvider);
    }

    @NonNull
    public SpeechPlayerProvider initializeSpeechPlayerProvider(boolean voiceLanguageSupported) {
        return new SpeechPlayerProvider(getApplication(), language, voiceLanguageSupported);
    }

    public LocationEngine initializeLocationEngineFrom(NavigationViewOptions options) {
        LocationEngine locationEngine = options.locationEngine();
        boolean shouldReplayRoute = options.shouldSimulateRoute();
        locationEngineConductor.initializeLocationEngine(getApplication(), locationEngine, shouldReplayRoute);
        return locationEngineConductor.obtainLocationEngine();
    }

    public void initializeNavigation(Context context, MapboxNavigationOptions options, LocationEngine locationEngine) {
        navigation = new MapboxNavigation(context, options, locationEngine);
        addNavigationListeners();
    }

    public void addNavigationListeners() {
        navigation.addProgressChangeListener(new NavigationViewModelProgressChangeListener(this));
        navigation.addOffRouteListener(offRouteListener);
        navigation.addMilestoneEventListener(milestoneEventListener);
        navigation.addNavigationEventListener(navigationEventListener);
        navigation.addFasterRouteListener(fasterRouteListener);
    }

    public void addMilestones(NavigationViewOptions options) {
        List<Milestone> milestones = options.milestones();
        if (milestones != null && !milestones.isEmpty()) {
            navigation.addMilestones(milestones);
        }
    }

    public OffRouteListener offRouteListener = new OffRouteListener() {
        @Override
        public void userOffRoute(Location location) {
            speechPlayer.onOffRoute();
            Point newOrigin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            handleOffRouteEvent(newOrigin);
        }
    };

    public MilestoneEventListener milestoneEventListener = (routeProgress, instruction, milestone) -> {
        NavigationViewModel.this.milestone = milestone;
        playVoiceAnnouncement(milestone);
        updateBannerInstruction(routeProgress, milestone);
        sendEventArrival(routeProgress, milestone);
    };

    public NavigationEventListener navigationEventListener = isRunning -> {
        NavigationViewModel.this.isRunning = isRunning;
        sendNavigationStatusEvent(isRunning);
    };

    public FasterRouteListener fasterRouteListener = directionsRoute -> updateRoute(directionsRoute);

    public ViewRouteListener routeEngineListener = new NavigationViewRouteEngineListener(this);

    public void startNavigation(DirectionsRoute route) {
        if (route != null) {
            navigation.startNavigation(route);
            voiceInstructionsToAnnounce = 0;
        }
    }

    public void updateReplayEngine(DirectionsRoute route) {
        if (locationEngineConductor.updateSimulatedRoute(route)) {
            LocationEngine replayEngine = locationEngineConductor.obtainLocationEngine();
            navigation.setLocationEngine(replayEngine);
        }
    }

    public void destroyRouter() {
        if (router != null) {
            router.onDestroy();
        }
    }

    public void endNavigation() {
        if (navigation != null) {
            navigation.onDestroy();
        }
    }

    public void clearDynamicCameraMap() {
        if (navigation != null) {
            Camera cameraEngine = navigation.getCameraEngine();
            boolean isDynamicCamera = cameraEngine instanceof DynamicCamera;
            if (isDynamicCamera) {
                ((DynamicCamera) cameraEngine).clearMap();
            }
        }
    }

    public void deactivateInstructionPlayer() {
        if (speechPlayer != null) {
            speechPlayer.onDestroy();
        }
    }

    public void playVoiceAnnouncement(Milestone milestone) {
        if (milestone instanceof VoiceInstructionMilestone) {
            voiceInstructionsToAnnounce++;
            SpeechAnnouncement announcement = SpeechAnnouncement.builder()
                .voiceInstructionMilestone((VoiceInstructionMilestone) milestone).build();
            announcement = retrieveAnnouncementFromSpeechEvent(announcement);
            speechPlayer.play(announcement);
        }
    }

    public void updateBannerInstruction(RouteProgress routeProgress, Milestone milestone) {
        if (milestone instanceof BannerInstructionMilestone) {
            BannerInstructions instructions = ((BannerInstructionMilestone) milestone).getBannerInstructions();
            instructions = retrieveInstructionsFromBannerEvent(instructions);
            if (instructions != null) {
                BannerInstructionModel model = new BannerInstructionModel(distanceFormatter, routeProgress, instructions);
                bannerInstructionModel.setValue(model);
            }
        }
    }

    public void sendEventArrival(RouteProgress routeProgress, Milestone milestone) {
        if (milestone == null || routeProgress == null) {
            return;
        }
        if (navigationViewEventDispatcher != null && routeUtils.isArrivalEvent(routeProgress, milestone)) {
            navigationViewEventDispatcher.onArrival();
        }
    }

    public void handleOffRouteEvent(Point newOrigin) {
        if (navigationViewEventDispatcher != null && navigationViewEventDispatcher.allowRerouteFrom(newOrigin)) {
            navigationViewEventDispatcher.onOffRoute(newOrigin);
            router.findRouteFrom(routeProgress);
            isOffRoute.setValue(true);
        }
    }

    public void sendNavigationStatusEvent(boolean isRunning) {
        if (navigationViewEventDispatcher != null) {
            if (isRunning) {
                navigationViewEventDispatcher.onNavigationRunning();
            } else {
                navigationViewEventDispatcher.onNavigationFinished();
            }
        }
    }

    public void sendEventOnRerouteAlong(DirectionsRoute route) {
        if (navigationViewEventDispatcher != null && isOffRoute()) {
            navigationViewEventDispatcher.onRerouteAlong(route);
        }
    }

    public void resetConfigurationFlag() {
        if (isChangingConfigurations) {
            isChangingConfigurations = false;
        }
    }

    public SpeechAnnouncement retrieveAnnouncementFromSpeechEvent(SpeechAnnouncement announcement) {
        if (navigationViewEventDispatcher != null) {
            announcement = navigationViewEventDispatcher.onAnnouncement(announcement);
        }
        return announcement;
    }

    public BannerInstructions retrieveInstructionsFromBannerEvent(BannerInstructions instructions) {
        if (navigationViewEventDispatcher != null) {
            instructions = navigationViewEventDispatcher.onBannerDisplay(instructions);
        }
        return instructions;
    }
}