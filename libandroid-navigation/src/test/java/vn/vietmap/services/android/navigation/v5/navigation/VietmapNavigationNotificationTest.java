package vn.vietmap.services.android.navigation.v5.navigation;

import android.app.NotificationManager;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.api.directions.v5.DirectionsAdapterFactory;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import vn.vietmap.services.android.navigation.v5.BaseTest;

import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class VietmapNavigationNotificationTest extends BaseTest {

  private static final String DIRECTIONS_ROUTE_FIXTURE = "directions_v5_precision_6.json";

  @Mock
  NotificationManager notificationManager;

  private DirectionsRoute route;

  @Before
  public void setUp() throws Exception {
    final String json = loadJsonFixture(DIRECTIONS_ROUTE_FIXTURE);
    Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create();
    DirectionsResponse response = gson.fromJson(json, DirectionsResponse.class);
    route = response.routes().get(0);
  }

  @Ignore
  @Test
  public void sanity() throws Exception {
    VietmapNavigationNotification vietmapNavigationNotification = new VietmapNavigationNotification(
      Mockito.mock(Context.class), Mockito.mock(VietmapNavigation.class));
    Assert.assertNotNull(vietmapNavigationNotification);
  }

  @Ignore
  @Test
  public void updateDefaultNotification_onlyUpdatesNameWhenNew() throws Exception {
    RouteProgress routeProgress = RouteProgress.builder()
      .directionsRoute(route)
      .stepIndex(0)
      .legIndex(0)
      .build();

    VietmapNavigationNotification vietmapNavigationNotification = new VietmapNavigationNotification(
      Mockito.mock(Context.class), Mockito.mock(VietmapNavigation.class));

    vietmapNavigationNotification.updateNotification(routeProgress);
    //    notificationManager.getActiveNotifications()[0].getNotification().contentView;
    //    verify(notificationManager, times(1)).getActiveNotifications()[0];
  }
}
