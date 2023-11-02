package vn.vietmap.services.android.navigation.ui.v5.map;

import vn.vietmap.vietmapsdk.maps.VietMapGL;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MapPaddingAdjustorTest {

  @Test
  public void adjustLocationIconWith_customPaddingIsSet() {
    VietMapGL vietMapGL = mock(VietMapGL.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 0, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(vietMapGL, defaultPadding);

    paddingAdjustor.adjustLocationIconWith(customPadding);

    verify(vietMapGL).setPadding(0, 0, 0, 0);
  }

  @Test
  public void isUsingDefault_falseAfterCustomPaddingIsSet() {
    VietMapGL vietMapGL = mock(VietMapGL.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 0, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(vietMapGL, defaultPadding);

    paddingAdjustor.adjustLocationIconWith(customPadding);

    assertFalse(paddingAdjustor.isUsingDefault());
  }

  @Test
  public void isUsingDefault_trueWithoutCustomPadding() {
    VietMapGL vietMapGL = mock(VietMapGL.class);
    int[] defaultPadding = {0, 250, 0, 0};

    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(vietMapGL, defaultPadding);

    assertTrue(paddingAdjustor.isUsingDefault());
  }

  @Test
  public void updatePaddingWithZero_updatesMapToZeroPadding() {
    VietMapGL vietMapGL = mock(VietMapGL.class);
    int[] defaultPadding = {0, 250, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(vietMapGL, defaultPadding);

    paddingAdjustor.updatePaddingWith(new int[]{0, 0, 0, 0});

    verify(vietMapGL).setPadding(0, 0, 0, 0);
  }

  @Test
  public void updatePaddingWithZero_retainsCustomPadding() {
    VietMapGL vietMapGL = mock(VietMapGL.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 350, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(vietMapGL, defaultPadding);
    paddingAdjustor.adjustLocationIconWith(customPadding);
    paddingAdjustor.updatePaddingWith(new int[]{0, 0, 0, 0});

    paddingAdjustor.resetPadding();

    verify(vietMapGL, times(2)).setPadding(0, 350, 0, 0);
  }

  @Test
  public void updatePaddingWithDefault_defaultIsRestoredAfterCustom() {
    VietMapGL vietMapGL = mock(VietMapGL.class);
    int[] defaultPadding = {0, 250, 0, 0};
    int[] customPadding = {0, 0, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(vietMapGL, defaultPadding);
    paddingAdjustor.adjustLocationIconWith(customPadding);

    paddingAdjustor.updatePaddingWithDefault();

    verify(vietMapGL).setPadding(0, 250, 0, 0);
  }

  @Test
  public void retrieveCurrentPadding_returnsCurrentMapPadding() {
    VietMapGL vietMapGL = mock(VietMapGL.class);
    int[] defaultPadding = {0, 250, 0, 0};
    MapPaddingAdjustor paddingAdjustor = new MapPaddingAdjustor(vietMapGL, defaultPadding);

    paddingAdjustor.retrieveCurrentPadding();

    verify(vietMapGL).getPadding();
  }
}