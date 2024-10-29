package vn.vietmap.services.android.navigation.testapp

import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import vn.vietmap.android.gestures.MoveGestureDetector
import vn.vietmap.services.android.navigation.ui.v5.NavigationLauncher
import vn.vietmap.services.android.navigation.ui.v5.NavigationView
import vn.vietmap.services.android.navigation.ui.v5.NavigationViewOptions
import vn.vietmap.services.android.navigation.ui.v5.OnNavigationReadyCallback
import vn.vietmap.services.android.navigation.ui.v5.listeners.NavigationListener
import vn.vietmap.services.android.navigation.ui.v5.map.NavigationVietmapGL
import vn.vietmap.services.android.navigation.v5.navigation.NavigationConstants
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigationOptions
import vn.vietmap.vietmapsdk.camera.CameraPosition
import vn.vietmap.vietmapsdk.location.modes.RenderMode
import vn.vietmap.vietmapsdk.maps.MapView
import vn.vietmap.vietmapsdk.maps.VietMapGL
import vn.vietmap.vietmapsdk.maps.VietMapGL.OnMoveListener

class VietMapNavigationViewScreen : AppCompatActivity(),
    OnNavigationReadyCallback,
    NavigationListener {

    private var navigationView: NavigationView? = null

    var navigationMap: NavigationVietmapGL? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_AppCompat_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        navigationView = findViewById(R.id.navigationView)
        navigationView?.onCreate(savedInstanceState, null)
        initialize()
    }

//    fun initializeNavigationMap(mapView: MapView?, map: VietMapGL) {
//        if (initialMapCameraPosition != null) {
//            map.cameraPosition = initialMapCameraPosition
//        }
//        navigationMap = NavigationVietmapGL(mapView!!, map)
//        navigationMap!!.updateLocationLayerRenderMode(RenderMode.GPS)
//        if (mapInstanceState != null) {
//            navigationMap!!.restoreFrom(mapInstanceState)
//            return
//        }
//    }
    public override fun onStart() {
        super.onStart()
        navigationView!!.onStart()
    }

    public override fun onResume() {
        super.onResume()
        navigationView!!.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        navigationView!!.onLowMemory()
    }

    override fun onBackPressed() {
        // If the navigation view didn't need to do anything, call super
        if (!navigationView!!.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        navigationView!!.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState!!)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState!!)
        navigationView!!.onRestoreInstanceState(savedInstanceState)
    }

    public override fun onPause() {
        super.onPause()
        navigationView!!.onPause()
    }

    public override fun onStop() {
        super.onStop()
        navigationView!!.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        navigationView!!.onDestroy()
    }

    override fun onNavigationReady(isRunning: Boolean) {
        val options = NavigationViewOptions.builder()
        options.navigationListener(this)
        extractRoute(options)
        extractConfiguration(options)

        options.navigationOptions(VietmapNavigationOptions.builder().build())
        options.onMoveListener(object : OnMoveListener {
            override fun onMoveBegin(moveGestureDetector: MoveGestureDetector) {
            }

            override fun onMove(moveGestureDetector: MoveGestureDetector) {
            }

            override fun onMoveEnd(moveGestureDetector: MoveGestureDetector) {
            }
        })
        navigationView!!.startNavigation(options.build())
    }

    override fun onCancelNavigation() {
        finishNavigation()
    }

    override fun onNavigationFinished() {
        finishNavigation()
    }

    override fun onNavigationRunning() {
        // Intentionally empty
    }

    private fun initialize() {
        val position =
            intent.getParcelableExtra<Parcelable>(NavigationConstants.NAVIGATION_VIEW_INITIAL_MAP_POSITION)
        if (position != null) {
            navigationView!!.initialize(this, (position as CameraPosition?)!!)
        } else {
            navigationView!!.initialize(this)
        }
    }

    private fun extractRoute(options: NavigationViewOptions.Builder) {
        val route = NavigationLauncher.extractRoute(this)
        options.directionsRoute(route)
    }

    private fun extractConfiguration(options: NavigationViewOptions.Builder) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        options.shouldSimulateRoute(
            preferences.getBoolean(
                NavigationConstants.NAVIGATION_VIEW_SIMULATE_ROUTE,
                false
            )
        )
    }

    private fun finishNavigation() {
        NavigationLauncher.cleanUpPreferences(this)
        finish()
    }
}