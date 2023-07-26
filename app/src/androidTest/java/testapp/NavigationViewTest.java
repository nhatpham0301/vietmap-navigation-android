package testapp;

import vn.vietmap.services.android.navigation.testapp.test.TestNavigationActivity;
import vn.vietmap.services.android.navigation.ui.v5.map.NavigationVietmapGL;
import vn.vietmap.services.android.navigation.v5.navigation.VietmapNavigation;

import org.junit.Test;

import testapp.activity.BaseNavigationActivityTest;

import static junit.framework.Assert.assertNotNull;

public class NavigationViewTest extends BaseNavigationActivityTest {

  @Override
  protected Class getActivityClass() {
    return TestNavigationActivity.class;
  }

  @Test
  public void onInitialization_navigationMapboxMapIsNotNull() {
    validateTestSetup();

    NavigationVietmapGL navigationVietmapGL = getNavigationView().retrieveNavigationMapboxMap();

    assertNotNull(navigationVietmapGL);
  }

  @Test
  public void onNavigationStart_mapboxNavigationIsNotNull() {
    validateTestSetup();

    VietmapNavigation vietmapNavigation = getNavigationView().retrieveMapboxNavigation();

    assertNotNull(vietmapNavigation);
  }
}
