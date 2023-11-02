package vn.vietmap.services.android.navigation.ui.v5.camera;

import vn.vietmap.vietmapsdk.camera.CameraPosition;
import vn.vietmap.vietmapsdk.camera.CameraUpdate;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.location.OnLocationCameraTransitionListener;
import vn.vietmap.vietmapsdk.location.modes.CameraMode;
import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.services.android.navigation.ui.v5.BaseTest;
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigation;
import vn.vietmap.services.android.navigation.v5.navigation.camera.RouteInformation;
import vn.vietmap.services.android.navigation.v5.routeprogress.ProgressChangeListener;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NavigationCameraTest extends BaseTest {

  @Test
  public void sanity() {
    NavigationCamera camera = buildCamera();

    assertNotNull(camera);
  }

  @Test
  public void setTrackingEnabled_trackingIsEnabled() {
    LocationComponent locationComponent = mock(LocationComponent.class);
    NavigationCamera camera = buildCamera(locationComponent);

    verify(locationComponent, times(1)).setCameraMode(eq(CameraMode.TRACKING_GPS),
      any(OnLocationCameraTransitionListener.class));
    verify(locationComponent, times(0)).setCameraMode(eq(CameraMode.NONE),
      any(OnLocationCameraTransitionListener.class));

    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE);
    verify(locationComponent, times(1)).setCameraMode(eq(CameraMode.NONE),
      any(OnLocationCameraTransitionListener.class));

    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);
    verify(locationComponent, times(2)).setCameraMode(eq(CameraMode.TRACKING_GPS),
      any(OnLocationCameraTransitionListener.class));

    assertTrue(camera.isTrackingEnabled());
  }

  @Test
  public void setTrackingDisabled_trackingIsDisabled() {
    LocationComponent locationComponent = mock(LocationComponent.class);
    NavigationCamera camera = buildCamera(locationComponent);

    verify(locationComponent, times(1)).setCameraMode(eq(CameraMode.TRACKING_GPS),
      any(OnLocationCameraTransitionListener.class));
    verify(locationComponent, times(0)).setCameraMode(eq(CameraMode.NONE),
      any(OnLocationCameraTransitionListener.class));

    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);
    verify(locationComponent, times(2)).setCameraMode(eq(CameraMode.TRACKING_GPS),
      any(OnLocationCameraTransitionListener.class));

    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE);
    verify(locationComponent, times(1)).setCameraMode(eq(CameraMode.NONE),
      any(OnLocationCameraTransitionListener.class));

    assertFalse(camera.isTrackingEnabled());
  }

  @Test
  public void onResetCamera_trackingIsResumed() {
    NavigationCamera camera = buildCamera();

    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE);
    camera.resetCameraPositionWith(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);

    assertTrue(camera.isTrackingEnabled());
  }

  @Test
  public void onResetCamera_dynamicCameraIsReset() {
    VietMapGL vietmapGL = mock(VietMapGL.class);
    when(vietmapGL.getCameraPosition()).thenReturn(mock(CameraPosition.class));
    VietmapNavigation navigation = mock(VietmapNavigation.class);
    DynamicCamera dynamicCamera = mock(DynamicCamera.class);
    when(navigation.getCameraEngine()).thenReturn(dynamicCamera);
    RouteInformation currentRouteInformation = mock(RouteInformation.class);
    NavigationCamera camera = buildCamera(vietmapGL, navigation, currentRouteInformation);

    camera.resetCameraPositionWith(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);

    verify(dynamicCamera).forceResetZoomLevel();
  }

  @Test
  public void onStartWithNullRoute_progressListenerIsAdded() {
    VietmapNavigation navigation = mock(VietmapNavigation.class);
    ProgressChangeListener listener = mock(ProgressChangeListener.class);
    NavigationCamera camera = buildCamera(navigation, listener);

    camera.start(null);

    verify(navigation, times(1)).addProgressChangeListener(listener);
  }

  @Test
  public void onResumeWithNullLocation_progressListenerIsAdded() {
    VietmapNavigation navigation = mock(VietmapNavigation.class);
    ProgressChangeListener listener = mock(ProgressChangeListener.class);
    NavigationCamera camera = buildCamera(navigation, listener);

    camera.resume(null);

    verify(navigation, times(1)).addProgressChangeListener(listener);
  }

  @Test
  public void update_defaultIsIgnoredWhileTracking() {
    VietMapGL vietmapGL = mock(VietMapGL.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    when(locationComponent.getCameraMode()).thenReturn(CameraMode.TRACKING_GPS);
    when(vietmapGL.getLocationComponent()).thenReturn(locationComponent);
    CameraUpdate cameraUpdate = mock(CameraUpdate.class);
    VietMapGL.CancelableCallback callback = mock(VietMapGL.CancelableCallback.class);
    NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
    NavigationCamera camera = buildCamera(vietmapGL);

    camera.update(navigationCameraUpdate, 300, callback);

    verify(vietmapGL, times(0)).animateCamera(cameraUpdate);
  }

  @Test
  public void update_defaultIsAcceptedWithNoTracking() {
    VietMapGL vietmapGL = mock(VietMapGL.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    when(locationComponent.getCameraMode()).thenReturn(CameraMode.NONE);
    when(vietmapGL.getLocationComponent()).thenReturn(locationComponent);
    CameraUpdate cameraUpdate = mock(CameraUpdate.class);
    VietMapGL.CancelableCallback callback = mock(VietMapGL.CancelableCallback.class);
    NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
    NavigationCamera camera = buildCamera(vietmapGL);

    camera.update(navigationCameraUpdate, 300, callback);

    verify(vietmapGL).animateCamera(eq(cameraUpdate), eq(300), eq(callback));
  }

  @Test
  public void update_overrideIsAcceptedWhileTracking() {
    VietMapGL vietmapGL = mock(VietMapGL.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    when(locationComponent.getCameraMode()).thenReturn(CameraMode.TRACKING_GPS);
    when(vietmapGL.getLocationComponent()).thenReturn(locationComponent);
    CameraUpdate cameraUpdate = mock(CameraUpdate.class);
    VietMapGL.CancelableCallback callback = mock(VietMapGL.CancelableCallback.class);
    NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
    navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
    NavigationCamera camera = buildCamera(vietmapGL);

    camera.update(navigationCameraUpdate, 300, callback);

    verify(vietmapGL).animateCamera(eq(cameraUpdate), eq(300), eq(callback));
  }

  @Test
  public void update_overrideSetsLocationComponentCameraModeNone() {
    VietMapGL vietmapGL = mock(VietMapGL.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    when(locationComponent.getCameraMode()).thenReturn(CameraMode.TRACKING_GPS);
    when(vietmapGL.getLocationComponent()).thenReturn(locationComponent);
    CameraUpdate cameraUpdate = mock(CameraUpdate.class);
    VietMapGL.CancelableCallback callback = mock(VietMapGL.CancelableCallback.class);
    NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
    navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
    NavigationCamera camera = buildCamera(vietmapGL);

    camera.update(navigationCameraUpdate, 300, callback);

    verify(locationComponent).setCameraMode(eq(CameraMode.NONE));
  }

  private NavigationCamera buildCamera() {
    return new NavigationCamera(mock(VietMapGL.class), mock(VietmapNavigation.class), mock(LocationComponent.class));
  }

  private NavigationCamera buildCamera(VietMapGL vietmapGL) {
    return new NavigationCamera(vietmapGL, mock(VietmapNavigation.class), mock(LocationComponent.class));
  }

  private NavigationCamera buildCamera(LocationComponent locationComponent) {
    return new NavigationCamera(mock(VietMapGL.class), mock(VietmapNavigation.class), locationComponent);
  }

  private NavigationCamera buildCamera(VietmapNavigation navigation, ProgressChangeListener listener) {
    return new NavigationCamera(mock(VietMapGL.class), navigation, listener,
      mock(LocationComponent.class), mock(RouteInformation.class));
  }

  private NavigationCamera buildCamera(VietMapGL vietmapGL, VietmapNavigation navigation,
                                       RouteInformation routeInformation) {
    return new NavigationCamera(vietmapGL, navigation, mock(ProgressChangeListener.class),
      mock(LocationComponent.class), routeInformation);
  }
}
