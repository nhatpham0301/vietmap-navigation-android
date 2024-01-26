package vn.vietmap.services.android.navigation.testapp

import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import vn.vietmap.vietmapsdk.location.LocationComponentActivationOptions
import vn.vietmap.vietmapsdk.location.engine.LocationEngine
import vn.vietmap.vietmapsdk.location.modes.CameraMode
import vn.vietmap.vietmapsdk.location.modes.RenderMode
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.maps.OnMapReadyCallback
import vn.vietmap.vietmapsdk.maps.Style
import vn.vietmap.services.android.navigation.testapp.NavigationSettings.ACCESS_TOKEN
import vn.vietmap.services.android.navigation.testapp.NavigationSettings.STYLE_URL
import vn.vietmap.services.android.navigation.testapp.databinding.ActivitySample1Binding
import vn.vietmap.services.android.navigation.ui.v5.NavigationView
import vn.vietmap.services.android.navigation.ui.v5.NavigationViewOptions
import vn.vietmap.services.android.navigation.ui.v5.OnNavigationReadyCallback
import vn.vietmap.services.android.navigation.ui.v5.listeners.NavigationListener
import vn.vietmap.services.android.navigation.ui.v5.listeners.RouteListener
import vn.vietmap.services.android.navigation.ui.v5.route.NavigationMapRoute
import vn.vietmap.services.android.navigation.v5.location.engine.LocationEngineProvider
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigation
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigationOptions
import vn.vietmap.services.android.navigation.v5.navigation.NavigationRoute
import vn.vietmap.services.android.navigation.v5.routeprogress.ProgressChangeListener
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class sample_1 : AppCompatActivity(), OnMapReadyCallback,
    ProgressChangeListener,
    NavigationListener,
    OnNavigationReadyCallback,
    RouteListener {

    private lateinit var binding: ActivitySample1Binding
    private lateinit var vietMapGL: VietMapGL
    private lateinit var navigationMapRoute: NavigationMapRoute
    private lateinit var vietmapNavigation: VietmapNavigation
    private lateinit var navigationView: NavigationView
    private val points: List<Point> = ArrayList()
    private var dropoffDialogShown = false

    private val locationEngine: LocationEngine by lazy {
        LocationEngineProvider.getBestLocationEngine(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample1)
        binding = ActivitySample1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        (points as ArrayList).add(Point.fromLngLat(-77.04012393951416, 38.9111117447887))
        points.add(Point.fromLngLat(-77.03847169876099, 38.91113678979344))
        points.add(Point.fromLngLat(-77.03848242759705, 38.91040213277608))
        points.add(Point.fromLngLat(-77.03850388526917, 38.909650771013034))
        points.add(Point.fromLngLat(-77.03651905059814, 38.90894949285854))

        navigationView = binding.navigationView;
        navigationView.initialize(this)
        navigationView.onCreate(savedInstanceState,null)

        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val options = VietmapNavigationOptions.builder().build()
        vietmapNavigation =
            VietmapNavigation(
                this,
                options,
                locationEngine
            )
    }
//
    override fun onMapReady(mapGL: VietMapGL) {
        this.vietMapGL = mapGL
        navigationView.onMapReady(mapGL)
        navigationView.onStart()
//
        mapGL.setStyle(Style.Builder().fromUri(STYLE_URL)) { style ->
            enableLocationComponent(style)
            navigationMapRoute =
                NavigationMapRoute(
                    binding.mapView,
                    mapGL
                )
        }
//
     //   mapboxNavigation.addProgressChangeListener(this)
//
        val that = this
        binding.startButton.setOnClickListener {
            val options = NavigationRoute.builder(this)
                .apikey(ACCESS_TOKEN)
                .origin(Point.fromLngLat(-122.405640, 37.761345))
                .destination(Point.fromLngLat(-122.421339, 37.773584))
                .alternatives(true)
                .build()

            options.getRoute(object: Callback<DirectionsResponse>{
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    response.body()?.let { response ->
                        if (response.routes().isNotEmpty()) {
                            navigationMapRoute?.addRoute(response.routes()[0])
                            val options = NavigationViewOptions.builder()
                                .navigationListener(that)
                                .directionsRoute(response.routes()[0])
                                .navigationListener(that)
                                .progressChangeListener(that)
//                                .routeListener(that)
                                .shouldSimulateRoute(true);
                            navigationView.startNavigation(options.build())
                        }
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                    Timber.e(throwable, "onFailure: navigation.getRoute()")
                }

            })
        }
    }
//
    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        val locationComponent = vietMapGL.locationComponent

        locationComponent?.let {
            // Activate with a built LocationComponentActivationOptions object
            it.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, style).build(),
            )

            // Enable to make component visible
            it.isLocationComponentEnabled = true

            // Set the component's camera mode
            it.cameraMode = CameraMode.TRACKING_GPS_NORTH

            // Set the component's render mode
            it.renderMode = RenderMode.NORMAL
        }
    }
//
    override fun onProgressChange(location: Location, routeProgress: RouteProgress) {
        // Do something with the updated progress information
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        vietmapNavigation.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
        navigationView.onSaveInstanceState(outState)
    }

//    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
//        super.onRestoreInstanceState(savedInstanceState!!)
//        navigationView.onRestoreInstanceState(savedInstanceState)
//    }

    override fun onCancelNavigation() {
//        navigationView.stopNavigation()
//        expandCollapse()
    }

    override fun onNavigationFinished() {
        Timber.d("Url: %s", "onNavigationFinished")
    }

    override fun onNavigationRunning() {
        Timber.d("Url: %s", "onNavigationRunning")
    }

    override fun onNavigationReady(isRunning: Boolean) {
        fetchRoute((points as ArrayList).removeAt(0), points.removeAt(0))
    }

    private fun fetchRoute(origin: Point, destination: Point) {
        NavigationRoute.builder(this)
            .apikey(ACCESS_TOKEN)
            .origin(origin)
            .destination(destination)
            .alternatives(true)
            .build()
            .getRoute(object : Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse?>?,
                    response: Response<DirectionsResponse>
                ) {
                    startNavigation(response.body()!!.routes()[0])
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Timber.d("Url: %s", "failure")
                }
            })
    }

    private fun startNavigation(directionsRoute: DirectionsRoute) {
        val navigationViewOptions: NavigationViewOptions? = setupOptions(directionsRoute)
        navigationView.startNavigation(navigationViewOptions)
    }

    private fun setupOptions(directionsRoute: DirectionsRoute): NavigationViewOptions? {
        dropoffDialogShown = false
        val options = NavigationViewOptions.builder()
        options.directionsRoute(directionsRoute)
            .navigationListener(this)
            .progressChangeListener(this)
            .routeListener(this)
            .shouldSimulateRoute(true)
        return options.build()
    }

    override fun allowRerouteFrom(offRoutePoint: Point?): Boolean {
        Timber.d("Url: %s", "allowRerouteFrom")
        return false
    }

    override fun onOffRoute(offRoutePoint: Point?) {
        Timber.d("Url: %s", "onOffRoute")
    }

    override fun onRerouteAlong(directionsRoute: DirectionsRoute?) {
        Timber.d("Url: %s", "onRerouteAlong")
    }

    override fun onFailedReroute(errorMessage: String?) {
        Timber.d("Url: %s", "onFailedReroute")
    }

    override fun onArrival() {
        Timber.d("Url: %s", "onArrival")
    }

}