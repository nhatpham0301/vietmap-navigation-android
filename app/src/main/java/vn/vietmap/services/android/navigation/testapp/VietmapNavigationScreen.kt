package vn.vietmap.services.android.navigation.testapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.api.directions.v5.models.BannerInstructions
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import vn.vietmap.android.gestures.RotateGestureDetector
import vn.vietmap.services.android.navigation.ui.v5.NavigationViewOptions
import vn.vietmap.services.android.navigation.ui.v5.camera.CameraOverviewCancelableCallback
import vn.vietmap.services.android.navigation.ui.v5.listeners.BannerInstructionsListener
import vn.vietmap.services.android.navigation.ui.v5.listeners.NavigationListener
import vn.vietmap.services.android.navigation.ui.v5.listeners.RouteListener
import vn.vietmap.services.android.navigation.ui.v5.listeners.SpeechAnnouncementListener
import vn.vietmap.services.android.navigation.ui.v5.route.NavigationMapRoute
import vn.vietmap.services.android.navigation.ui.v5.voice.NavigationSpeechPlayer
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechAnnouncement
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechPlayer
import vn.vietmap.services.android.navigation.ui.v5.voice.SpeechPlayerProvider
import vn.vietmap.services.android.navigation.v5.location.engine.LocationEngineProvider
import vn.vietmap.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine
import vn.vietmap.services.android.navigation.v5.milestone.Milestone
import vn.vietmap.services.android.navigation.v5.milestone.MilestoneEventListener
import vn.vietmap.services.android.navigation.v5.milestone.VoiceInstructionMilestone
import vn.vietmap.services.android.navigation.v5.navigation.*
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigation
import vn.vietmap.services.android.navigation.v5.navigation.camera.RouteInformation
import vn.vietmap.services.android.navigation.v5.offroute.OffRouteListener
import vn.vietmap.services.android.navigation.v5.route.FasterRouteListener
import vn.vietmap.services.android.navigation.v5.routeprogress.ProgressChangeListener
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress
import vn.vietmap.services.android.navigation.v5.snap.SnapToRoute
import vn.vietmap.services.android.navigation.v5.utils.RouteUtils
import vn.vietmap.viet_navigation.util.IconUtils
import vn.vietmap.vietmapsdk.Vietmap
import vn.vietmap.vietmapsdk.annotations.Marker
import vn.vietmap.vietmapsdk.annotations.MarkerOptions
import vn.vietmap.vietmapsdk.camera.CameraPosition
import vn.vietmap.vietmapsdk.camera.CameraUpdate
import vn.vietmap.vietmapsdk.camera.CameraUpdateFactory
import vn.vietmap.vietmapsdk.geometry.LatLng
import vn.vietmap.vietmapsdk.geometry.LatLngBounds
import vn.vietmap.vietmapsdk.location.LocationComponent
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions
import vn.vietmap.vietmapsdk.location.engine.LocationEngine
import vn.vietmap.vietmapsdk.location.engine.LocationEngineCallback
import vn.vietmap.vietmapsdk.location.engine.LocationEngineResult
import vn.vietmap.vietmapsdk.location.modes.CameraMode
import vn.vietmap.vietmapsdk.location.modes.RenderMode
import vn.vietmap.vietmapsdk.maps.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class VietmapNavigationScreen :   AppCompatActivity(), OnMapReadyCallback, ProgressChangeListener,
    OffRouteListener, MilestoneEventListener, NavigationEventListener, NavigationListener,
    FasterRouteListener, SpeechAnnouncementListener, BannerInstructionsListener, RouteListener,
VietMapGL.OnMapLongClickListener, VietMapGL.OnMapClickListener,
VietMapGL.OnRotateListener,
MapView.OnDidFinishRenderingMapListener {

    private var mapView: MapView? = null
    private var vietmapGL: VietMapGL? = null

    private var currentRoute: DirectionsRoute? = null
    private var routeClicked: Boolean = false
    private var locationEngine: LocationEngine? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var directionsRoutes: List<DirectionsRoute>? = null
    private val snapEngine = SnapToRoute()
    private var simulateRoute = false
    private var primaryRouteIndex = 0
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var navigation: VietmapNavigation? = null

    private var speechPlayerProvider: SpeechPlayerProvider? = null
    private var speechPlayer: SpeechPlayer? = null
    private var routeProgress: RouteProgress? = null
    private val routeUtils = RouteUtils()
    private var voiceInstructionsEnabled = true
    private var isBuildingRoute = false
    private var origin = Point.fromLngLat(106.675789, 10.759050)
    private var destination = Point.fromLngLat(106.686777, 10.775056)
    private var locationComponent: LocationComponent? = null
    private var currentCenterPoint: CurrentCenterPoint? = null
    private var isOverviewing = false
    private var animateBuildRoute = true
    private var isNavigationInProgress = false
    private var isNavigationCanceled = false
    var zoom = 20.0
    var bearing = 0.0
    var tilt = 0.0
    var padding: IntArray = intArrayOf(150, 500, 150, 500)
    var isRunning: Boolean = false
    private var options: VietMapGLOptions? = null

    /// You can change the options of navigation here (optional)
    private val navigationOptions =
        VietmapNavigationOptions.builder().maxTurnCompletionOffset(30.0).maneuverZoneRadius(40.0)
            .maximumDistanceOffRoute(50.0).deadReckoningTimeInterval(5.0)

            .maxManipulatedCourseAngle(25.0).userLocationSnapDistance(20.0).secondsBeforeReroute(3)
            .enableOffRouteDetection(true).enableFasterRouteDetection(true).snapToRoute(true)
            .manuallyEndNavigationUponCompletion(false).defaultMilestonesEnabled(true)
            .minimumDistanceBeforeRerouting(10.0).metersRemainingTillArrival(20.0)
            .isFromNavigationUi(false).isDebugLoggingEnabled(false)
            .roundingIncrement(NavigationConstants.ROUNDING_INCREMENT_FIFTY)
            .timeFormatType(NavigationTimeFormat.NONE_SPECIFIED)

            .locationAcceptableAccuracyInMetersThreshold(100).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Vietmap.getInstance(this)
        setContentView(R.layout.activity_vietmap_navigation)
        initLocationEngine()

        speechPlayerProvider = SpeechPlayerProvider(this, "vi", true);
        speechPlayer = NavigationSpeechPlayer(speechPlayerProvider)
        mapView = findViewById(R.id.ktMapView)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        options =
            VietMapGLOptions.createFromAttributes(this).compassEnabled(false).logoEnabled(true)
        mapView = MapView(this, options)
        navigation = VietmapNavigation(
            this, navigationOptions, locationEngine!!
        )

        mapView!!.getMapAsync(this)
        val btnStopNavigation: Button = findViewById(R.id.btnStopNavigation)
        btnStopNavigation.setOnClickListener {
            finishNavigation()
        }
        val btnStartNavigation: Button = findViewById(R.id.btnStartNavigation)
        btnStartNavigation.setOnClickListener {
            startNavigation()
        }
        val btnOverview: Button = findViewById(R.id.btnOverview)
        btnOverview.setOnClickListener {
            overViewRoute()
        }
        val btnRecenter: Button = findViewById(R.id.btnRecenter)
        btnRecenter.setOnClickListener {
            recenter()
        }


    }

    private fun overViewRoute() {
        isOverviewing = true
        routeProgress?.let { showRouteOverview(padding, it) }
    }


    private fun clearRoute() {
        if (navigationMapRoute != null) {
            navigationMapRoute?.removeRoute()
        }
        currentRoute = null
    }

    /*

    1. Can we get the Terminal/Vehicls's current speed information?
    3. Can we get the Compass Information?
     */
    private fun getUserCurrentSpeedAndCompass() {
        if (checkPermission()) {
            locationComponent?.locationEngine?.getLastLocation(object :
                LocationEngineCallback<LocationEngineResult> {
                override fun onSuccess(result: LocationEngineResult?) {
                    result?.lastLocation?.speed?.let {
                        if (it > 0) {
                            println("user speed $it")
                        }
                    }

                    /// bearing is angle of user direction (0 - 360)
                    result?.lastLocation?.bearing?.let {
                        if (it > 0) {
                            println("user bearing $it")
                        }
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.e("TAG", "onFailure: ${exception.message}")
                }
            })
        }
    }

    private fun initMapRoute() {
        if (vietmapGL != null) {
            navigationMapRoute = NavigationMapRoute(mapView!!, vietmapGL!!, "vmadmin_province")
        }

        navigationMapRoute?.setOnRouteSelectionChangeListener {
            routeClicked = true

            currentRoute = it

            val routePoints: List<Point> =
                currentRoute?.routeOptions()?.coordinates() as List<Point>
            animateVietmapGLForRouteOverview(padding, routePoints)
            primaryRouteIndex = try {
                it.routeIndex()?.toInt() ?: 0
            } catch (e: Exception) {
                0
            }
            if (isRunning) {
                finishNavigation(isOffRouted = true)
                startNavigation()
            }
        }

        vietmapGL?.addOnMapClickListener(this)
    }


    private fun finishNavigation(isOffRouted: Boolean = false) {

        zoom = 15.0
        bearing = 0.0
        tilt = 0.0
        isNavigationCanceled = true

//        destinationPoint?.let {
//            moveCamera(LatLng(it.latitude(), it.longitude()),null)
//        }
        if (!isOffRouted) {
            isNavigationInProgress = false
            moveCameraToOriginOfRoute()
        }

        if (currentRoute != null) {
            isRunning = false
            navigation!!.stopNavigation()
            navigation!!.removeFasterRouteListener(this)
            navigation!!.removeMilestoneEventListener(this)
            navigation!!.removeNavigationEventListener(this)
            navigation!!.removeOffRouteListener(this)
            navigation!!.removeProgressChangeListener(this)
        }

    }

    private fun moveCameraToOriginOfRoute() {
        currentRoute?.let {
            try {
                val originCoordinate = it.routeOptions()?.coordinates()?.get(0)
                originCoordinate?.let {
                    val location = LatLng(originCoordinate.latitude(), originCoordinate.longitude())
                    moveCamera(location, null)
                }
            } catch (e: java.lang.Exception) {
                Timber.i(String.format("moveCameraToOriginOfRoute, %s", "Error: ${e.message}"))
            }
        }
    }

    private fun moveCamera(location: LatLng, bearing: Float?) {

        val cameraPosition = CameraPosition.Builder().target(location).zoom(zoom).tilt(tilt)

        if (bearing != null) {
            cameraPosition.bearing(bearing.toDouble())
        }

        var duration = 3000
        if (!animateBuildRoute) duration = 1
        vietmapGL?.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition.build()), duration
        )
    }

    private fun startNavigation() {
        tilt = 60.0
        zoom = 19.0
        isOverviewing = false
        isNavigationCanceled = false
        vietmapGL?.locationComponent?.cameraMode = CameraMode.TRACKING_GPS_NORTH

        if (currentRoute != null) {
            if (simulateRoute) {
                val mockLocationEngine = ReplayRouteLocationEngine()
                mockLocationEngine.assign(currentRoute)
                navigation!!.locationEngine = mockLocationEngine
            } else {
                locationEngine?.let {
                    navigation!!.locationEngine = it
                }
            }
            isRunning = true
            vietmapGL?.locationComponent?.locationEngine = null
            navigation!!.addNavigationEventListener(this)
            navigation!!.addFasterRouteListener(this)
            navigation!!.addMilestoneEventListener(this)
            navigation!!.addOffRouteListener(this)
            navigation!!.addProgressChangeListener(this)
            navigation!!.snapEngine = snapEngine
            currentRoute?.let {
                isNavigationInProgress = true
                navigation!!.startNavigation(currentRoute!!)
                recenter()
            }
        }
    }

    private fun recenter() {
        isOverviewing = false
        if (currentCenterPoint != null) {
            moveCamera(
                LatLng(currentCenterPoint!!.latitude, currentCenterPoint!!.longitude),
                currentCenterPoint!!.bearing
            )
        }
    }

    private fun initLocationEngine() {
        locationEngine = if (simulateRoute) {
            ReplayRouteLocationEngine()
        } else {
            LocationEngineProvider.getBestLocationEngine(this)
        }
    }

    override fun onMapReady(p0: VietMapGL) {
        vietmapGL = p0
        vietmapGL!!.setStyle(
            Style.Builder()
                .fromUri("https://maps.vietmap.vn/api/maps/light/styles.json?apikey=6411732992b3c4def7a117893215b9163a15e69065c0874d")
        ) { style: Style? ->
            initLocationEngine()

            enableLocationComponent(style)

            initMapRoute()
        }
        vietmapGL!!.addOnMapClickListener(this)
        vietmapGL!!.addOnMapLongClickListener(this)
        vietmapGL!!.addOnRotateListener(this)
        Toast.makeText(
            this,
            "Long tap on the map to place the destination.",
            Toast.LENGTH_LONG
        ).show()


    }


    private fun enableLocationComponent(style: Style?) {
        locationComponent = vietmapGL!!.locationComponent
        if (locationComponent != null) {
            locationComponent!!.activateLocationComponent(
                LocationComponentActivationOptions.builder(
                    this, style!!
                ).build()
            )
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            locationComponent!!.setCameraMode(
                CameraMode.TRACKING_GPS_NORTH, 750L, 18.0, 10000.0, 10000.0, null
            )
            locationComponent!!.isLocationComponentEnabled = true
            locationComponent!!.zoomWhileTracking(19.0)
            locationComponent!!.renderMode = RenderMode.GPS
            locationComponent!!.locationEngine = locationEngine
        }

    }

    override fun onMapClick(p0: LatLng): Boolean {
        addMarker(p0)
        // handle on Map click here
        return false
    }

    private fun showRouteOverview(padding: IntArray?, currentRouteProgress: RouteProgress) {

        val routeInformation: RouteInformation =
            buildRouteInformationFromProgress(currentRouteProgress)
        animateCameraForRouteOverview(routeInformation, padding!!)
    }

    private fun buildRouteInformationFromProgress(routeProgress: RouteProgress?): RouteInformation {
        return if (routeProgress == null) {
            RouteInformation.create(null, null, null)
        } else RouteInformation.create(routeProgress.directionsRoute(), null, null)
    }

    private fun animateCameraForRouteOverview(
        routeInformation: RouteInformation, padding: IntArray
    ) {
        val cameraEngine = navigation!!.cameraEngine
        val routePoints = cameraEngine.overview(routeInformation)
        if (routePoints.isNotEmpty()) {
            animateVietmapGLForRouteOverview(padding, routePoints)
        }
    }

    private fun animateVietmapGLForRouteOverview(padding: IntArray, routePoints: List<Point>) {
        if (routePoints.size <= 1) {
            return
        }
        val resetUpdate: CameraUpdate = buildResetCameraUpdate()
        val overviewUpdate: CameraUpdate = buildOverviewCameraUpdate(padding, routePoints)
        vietmapGL?.animateCamera(
            resetUpdate, 150,  CameraOverviewCancelableCallback(overviewUpdate, vietmapGL)
        )
    }


    private fun buildResetCameraUpdate(): CameraUpdate {
        val resetPosition: CameraPosition = CameraPosition.Builder().tilt(0.0).bearing(0.0).build()
        return CameraUpdateFactory.newCameraPosition(resetPosition)
    }


    private fun buildOverviewCameraUpdate(
        padding: IntArray, routePoints: List<Point>
    ): CameraUpdate {
        val routeBounds = convertRoutePointsToLatLngBounds(routePoints)
        return CameraUpdateFactory.newLatLngBounds(
            routeBounds, padding[0], padding[1], padding[2], padding[3]
        )
    }

    private fun convertRoutePointsToLatLngBounds(routePoints: List<Point>): LatLngBounds {
        val latLngs: MutableList<LatLng> = ArrayList()
        for (routePoint in routePoints) {
            latLngs.add(LatLng(routePoint.latitude(), routePoint.longitude()))
        }
        return LatLngBounds.Builder().includes(latLngs).build()
    }

    override fun onProgressChange(location: Location?, routeProgress: RouteProgress?) {
        /// This method contains all of data while user is in navigation
        /// You can simple call speed or compass with this method
        // location?.speed
        // location?.bearing
        /// Some useful data

        /*

        val bannerInstructionsList: List<BannerInstructions> =
            routeProgress.currentLegProgress().currentStep().bannerInstructions() as List<BannerInstructions>
        currentModifier = bannerInstructionsList?.get(0)?.primary()?.modifier()
        currentModifierType= bannerInstructionsList?.get(0)?.primary()?.type()
        // val util = RouteUtils()
        // arrived = util.isArrivalEvent(routeProgress) && util.isLastLeg(routeProgress)
        distanceRemaining = routeProgress.distanceRemaining().toFloat()
        durationRemaining = routeProgress.durationRemaining()
        distanceTraveled = routeProgress.distanceTraveled().toFloat()
        legIndex = routeProgress.currentLegProgress()?.stepIndex()
        // stepIndex = routeProgress.stepIndex
        val leg = routeProgress.currentLeg()
        if (leg != null)
            currentLeg = VietMapRouteLeg(leg)

        currentStepInstruction = bannerInstructionsList?.get(0)
            ?.primary()
            ?.text()
        currentLegDistanceTraveled = routeProgress.currentLegProgress()?.distanceTraveled()?.toFloat()
        currentLegDistanceRemaining = routeProgress.currentLegProgress()?.distanceRemaining()?.toFloat()
        distanceToNextTurn = routeProgress.stepDistanceRemaining().toFloat()
         */
        if (!isNavigationCanceled && location != null && routeProgress != null) {
            try {
                val noRoutes: Boolean = directionsRoutes?.isEmpty() ?: true

                val newCurrentRoute: Boolean = !routeProgress!!.directionsRoute()
                    .equals(directionsRoutes?.get(primaryRouteIndex))
                val isANewRoute: Boolean = noRoutes || newCurrentRoute
                if (isANewRoute) {
                } else {

                    currentCenterPoint =
                        CurrentCenterPoint(location.latitude, location.longitude, location.bearing)

                    if (!isOverviewing) {
                        this.routeProgress = routeProgress
                        moveCamera(LatLng(location.latitude, location.longitude), location.bearing)
                    }

                    if (!isBuildingRoute) {
                        val snappedLocation: Location =
                            snapEngine.getSnappedLocation(location, routeProgress)
                        vietmapGL?.locationComponent?.forceLocationUpdate(snappedLocation)
                    }

                    if (simulateRoute && !isBuildingRoute) {
                        vietmapGL?.locationComponent?.forceLocationUpdate(location)
                    }

                }
                handleProgressChange(location, routeProgress)
            } catch (e: java.lang.Exception) {
            }
        }
    }

    private fun handleProgressChange(location: Location, routeProgress: RouteProgress) {
        val distanceRemainingToNextTurn =
            routeProgress.currentLegProgress()?.currentStepProgress()?.distanceRemaining()
        if (distanceRemainingToNextTurn != null && distanceRemainingToNextTurn < 30) {

            val resetPosition: CameraPosition =
                CameraPosition.Builder().tilt(0.0).zoom(17.0).build()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(resetPosition)
            vietmapGL?.animateCamera(
                cameraUpdate, 1000
            )
        } else {
            if (routeProgress.currentLegProgress().currentStepProgress()
                    .distanceTraveled() > 30 && !isOverviewing
            ) {
                recenter()
            }
        }
    }

    override fun userOffRoute(location: Location?) {
        location?.let {
            if (checkIfUserOffRoute(it)) {
                speechPlayer!!.onOffRoute()
                doOnNewRoute(Point.fromLngLat(location.longitude, location.latitude))
            }
        }

    }

    private fun doOnNewRoute(offRoutePoint: Point?) {
        if (!isBuildingRoute) {
            isBuildingRoute = true

            offRoutePoint?.let {

                finishNavigation(isOffRouted = true)

                moveCamera(LatLng(it.latitude(), it.longitude()), null)


            }


            origin = offRoutePoint
            isNavigationInProgress = true
            fetchRouteWithBearing(false)
        }
    }

    private fun checkIfUserOffRoute(location: Location): Boolean {

        val snapLocation: Location = snapEngine.getSnappedLocation(location, routeProgress)
        val distance: Double = calculateDistanceBetween2Point(location, snapLocation)
        return distance > 30

    }

    private fun calculateDistanceBetween2Point(location1: Location, location2: Location): Double {

        /// this function calculate distance between 2 point in the earth, with planetary arc
        val radius = 6371000.0 // meters

        val dLat = (location2.latitude - location1.latitude) * PI / 180.0
        val dLon = (location2.longitude - location1.longitude) * PI / 180.0

        val a =
            sin(dLat / 2.0) * sin(dLat / 2.0) + cos(location1.latitude * PI / 180.0) * cos(location2.latitude * PI / 180.0) * sin(
                dLon / 2.0
            ) * sin(dLon / 2.0)
        val c = 2.0 * kotlin.math.atan2(sqrt(a), sqrt(1.0 - a))

        return radius * c
    }

    override fun onMilestoneEvent(p0: RouteProgress?, p1: String?, p2: Milestone?) {

        if (voiceInstructionsEnabled) {
            playVoiceAnnouncement(p2)
        }
        if (p0 != null && p2 != null) {
            if (routeUtils.isArrivalEvent(p0, p2) && isNavigationInProgress) {
                vietmapGL?.locationComponent?.locationEngine = locationEngine
                finishNavigation()
            }
        }
    }

    private fun playVoiceAnnouncement(milestone: Milestone?) {
        if (milestone is VoiceInstructionMilestone) {
            var announcement = SpeechAnnouncement.builder()
                .voiceInstructionMilestone(milestone as VoiceInstructionMilestone?).build()
            speechPlayer!!.play(announcement)
        }
    }

    override fun onRunning(p0: Boolean) {
        /// Handle navigation running events here
    }

    override fun onCancelNavigation() {
        /// Handle a navigation cancel event here
    }

    override fun onNavigationFinished() {
        /// Handle a navigation finished event here
    }

    override fun onNavigationRunning() {
        /// Handle a navigation running event here
    }

    override fun fasterRouteFound(p0: DirectionsRoute?) {
        p0?.let {
            currentRoute = p0
            finishNavigation()
            startNavigation()
        }
    }

    override fun willVoice(p0: SpeechAnnouncement?): SpeechAnnouncement {
        /// return null if you turn off voice instruction
        return p0!!
    }

    override fun willDisplay(p0: BannerInstructions?): BannerInstructions {
        /// return null if you turn off banner instruction
        return p0!!
    }

    override fun allowRerouteFrom(p0: Point?): Boolean {

        return true
    }


    override fun onOffRoute(offRoutePoint: Point?) {
        doOnNewRoute(offRoutePoint)
    }

    override fun onRerouteAlong(p0: DirectionsRoute?) {
        p0?.let {
            currentRoute = p0
            finishNavigation()
            startNavigation()
        }
    }

    override fun onFailedReroute(p0: String?) {
        // handle failed reroute here
    }

    override fun onArrival() {
        vietmapGL?.locationComponent?.locationEngine = locationEngine
        // handle arrival here
        println("You have arrived at your destination")
    }

    override fun onMapLongClick(latLng: LatLng): Boolean {
        getCurrentLocation()
        destination = Point.fromLngLat(latLng.longitude, latLng.latitude)
        if (origin != null) {
            fetchRouteWithBearing(false)
        }
        return false
    }

    override fun onDidFinishRenderingMap(p0: Boolean) {
        /// Handle did finish rendering map event here
    }


    private fun getCurrentLocation() {

        if (checkPermission()) {
            fusedLocationClient!!.lastLocation.addOnSuccessListener(
                this
            ) { location: Location? ->
                if (location != null) {
                    origin = Point.fromLngLat(
                        location.longitude, location.latitude
                    )
                }
            }
        }
    }


    private fun fetchRouteWithBearing(isStartNavigation: Boolean) {
        if (checkPermission()) {
            fusedLocationClient?.lastLocation?.addOnSuccessListener(
                this
            ) { location: Location? ->
                if (location != null) {
                    fetchRoute(isStartNavigation, location.bearing)
                }
            }
        } else {
            fetchRoute(isStartNavigation, null)
        }
    }


    private fun fetchRoute(isStartNavigation: Boolean, bearing: Float?) {
        val builder =
            NavigationRoute.builder(this).apikey("6411732992b3c4def7a117893215b9163a15e69065c0874d")
                .origin(origin, bearing?.toDouble(), bearing?.toDouble())
                .destination(destination, bearing?.toDouble(), bearing?.toDouble())
        builder.build().getRoute(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse?>, response: Response<DirectionsResponse?>
            ) {
                println(call.request().url())
                directionsRoutes = response.body()!!.routes()
                currentRoute = if (directionsRoutes!!.size <= primaryRouteIndex) {
                    directionsRoutes!![0]
                } else {
                    directionsRoutes!![primaryRouteIndex]
                }

                // Draw the route on the map
                if (navigationMapRoute != null) {
                    navigationMapRoute?.removeRoute()
                } else {
                    navigationMapRoute =
                        NavigationMapRoute(mapView!!, vietmapGL!!, "vmadmin_province")

                }

                //show multiple route to map
                if (response.body()!!.routes().size > 1) {
                    navigationMapRoute?.addRoutes(directionsRoutes!!)
                } else {
                    navigationMapRoute?.addRoute(currentRoute)
                }


                isBuildingRoute = false
                // get route point from current route
                val routePoints: List<Point> =
                    currentRoute?.routeOptions()?.coordinates() as List<Point>
                animateVietmapGLForRouteOverview(padding, routePoints)
                //Start Navigation again from new Point, if it was already in Progress
                if (isNavigationInProgress || isStartNavigation) {
                    startNavigation()
                }
            }

            override fun onFailure(call: Call<DirectionsResponse?>, throwable: Throwable) {
                isBuildingRoute = false

            }
        })
    }

    private fun addMarker(position: LatLng): Marker {
        return vietmapGL!!.addMarker(
            MarkerOptions()
                .position(position)
                .icon(
                    IconUtils().drawableToIcon(
                        this,
                        R.drawable.alert_progress_drawable,
                        ResourcesCompat.getColor(resources,R.color.vietmap_blue , theme)
                    )
                )
        )
    }
    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRotateBegin(p0: RotateGestureDetector) {
    }

    override fun onRotate(p0: RotateGestureDetector) {

    }

    override fun onRotateEnd(p0: RotateGestureDetector) {
        println(p0.deltaSinceLast*360)
    }
}