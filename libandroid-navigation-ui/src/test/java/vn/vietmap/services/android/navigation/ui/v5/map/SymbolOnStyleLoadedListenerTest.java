package vn.vietmap.services.android.navigation.ui.v5.map;

import android.graphics.Bitmap;

import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.vietmapsdk.maps.Style;

import org.junit.Test;

import static vn.vietmap.services.android.navigation.ui.v5.map.NavigationSymbolManager.MAPBOX_NAVIGATION_MARKER_NAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SymbolOnStyleLoadedListenerTest {

  @Test
  public void onDidFinishLoadingStyle_markerIsAdded() {
    VietMapGL mapboxMap = mock(VietMapGL.class);
    Style style = mock(Style.class);
    when(mapboxMap.getStyle()).thenReturn(style);
    Bitmap markerBitmap = mock(Bitmap.class);
    SymbolOnStyleLoadedListener listener = new SymbolOnStyleLoadedListener(mapboxMap, markerBitmap);

    listener.onDidFinishLoadingStyle();

    verify(style).addImage(eq(MAPBOX_NAVIGATION_MARKER_NAME), eq(markerBitmap));
  }
}