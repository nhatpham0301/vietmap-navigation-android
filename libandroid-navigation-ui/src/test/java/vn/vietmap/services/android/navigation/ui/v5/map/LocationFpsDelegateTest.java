package vn.vietmap.services.android.navigation.ui.v5.map;

import vn.vietmap.vietmapsdk.camera.CameraPosition;
import vn.vietmap.vietmapsdk.location.LocationComponent;
import vn.vietmap.vietmapsdk.maps.VietMapGL;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocationFpsDelegateTest {

  @Test
  public void onCameraIdle_newFpsIsSetZoom16() {
    double zoom = 16d;
    VietMapGL vietMapGL = mock(VietMapGL.class);
    when(vietMapGL.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(vietMapGL, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(25));
  }

  @Test
  public void onCameraIdle_newFpsIsSetZoom14() {
    double zoom = 14d;
    VietMapGL vietMapGL = mock(VietMapGL.class);
    when(vietMapGL.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(vietMapGL, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(15));
  }

  @Test
  public void onCameraIdle_newFpsIsSetZoom10() {
    double zoom = 10d;
    VietMapGL vietMapGL = mock(VietMapGL.class);
    when(vietMapGL.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(vietMapGL, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(10));
  }

  @Test
  public void onCameraIdle_newFpsIsSet5() {
    double zoom = 5d;
    VietMapGL vietMapGL = mock(VietMapGL.class);
    when(vietMapGL.getCameraPosition()).thenReturn(buildCameraPosition(zoom));
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(vietMapGL, locationComponent);

    locationFpsDelegate.onCameraIdle();

    verify(locationComponent).setMaxAnimationFps(eq(5));
  }

  @Test
  public void onStart_idleListenerAdded() {
    VietMapGL vietMapGL = mock(VietMapGL.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(vietMapGL, locationComponent);

    locationFpsDelegate.onStart();

    verify(vietMapGL, times(2)).addOnCameraIdleListener(eq(locationFpsDelegate));
  }

  @Test
  public void onStop_idleListenerRemoved() {
    VietMapGL vietMapGL = mock(VietMapGL.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(vietMapGL, locationComponent);

    locationFpsDelegate.onStop();

    verify(vietMapGL).removeOnCameraIdleListener(eq(locationFpsDelegate));
  }

  @Test
  public void updateEnabled_falseResetsToMax() {
    VietMapGL vietMapGL = mock(VietMapGL.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(vietMapGL, locationComponent);

    locationFpsDelegate.updateEnabled(false);

    verify(locationComponent).setMaxAnimationFps(eq(Integer.MAX_VALUE));
  }

  @Test
  public void isEnabled_returnsFalseWhenSet() {
    VietMapGL vietmapGL = mock(VietMapGL.class);
    LocationComponent locationComponent = mock(LocationComponent.class);
    LocationFpsDelegate locationFpsDelegate = new LocationFpsDelegate(vietmapGL, locationComponent);

    locationFpsDelegate.updateEnabled(false);

    assertFalse(locationFpsDelegate.isEnabled());
  }

  private CameraPosition buildCameraPosition(double zoom) {
    return new CameraPosition.Builder().zoom(zoom).build();
  }
}