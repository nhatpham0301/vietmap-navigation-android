package vn.vietmap.services.android.navigation.v5.navigation;

import android.content.Context;
import androidx.annotation.NonNull;

import vn.vietmap.services.android.navigation.v5.navigation.notification.NavigationNotification;
import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NavigationNotificationProviderTest {

  @Test
  public void updateNavigationNotification() {
    NavigationNotification notification = mock(NavigationNotification.class);
    VietmapNavigation vietmapNavigation = buildNavigationWithNotificationOptions(notification);
    Context context = mock(Context.class);
    NavigationNotificationProvider provider = new NavigationNotificationProvider(context, vietmapNavigation);

    RouteProgress routeProgress = mock(RouteProgress.class);
    provider.updateNavigationNotification(routeProgress);

    verify(notification).updateNotification(eq(routeProgress));
  }

  @Test
  public void updateNavigationNotification_doesNotUpdateAfterShutdown() {
    NavigationNotification notification = mock(NavigationNotification.class);
    VietmapNavigation vietmapNavigation = buildNavigationWithNotificationOptions(notification);
    Context context = mock(Context.class);
    NavigationNotificationProvider provider = new NavigationNotificationProvider(context, vietmapNavigation);
    RouteProgress routeProgress = mock(RouteProgress.class);

    provider.shutdown(context);
    provider.updateNavigationNotification(routeProgress);

    verify(notification, times(0)).updateNotification(routeProgress);
  }

  @Test
  public void onShutdown_onNavigationStoppedIsCalled() {
    NavigationNotification notification = mock(NavigationNotification.class);
    VietmapNavigation vietmapNavigation = buildNavigationWithNotificationOptions(notification);
    Context context = mock(Context.class);
    NavigationNotificationProvider provider = new NavigationNotificationProvider(context, vietmapNavigation);

    provider.shutdown(context);

    verify(notification).onNavigationStopped(context);
  }

  @NonNull
  private VietmapNavigation buildNavigationWithNotificationOptions(NavigationNotification notification) {
    VietmapNavigation vietmapNavigation = mock(VietmapNavigation.class);
    VietmapNavigationOptions options = mock(VietmapNavigationOptions.class);
    when(options.navigationNotification()).thenReturn(notification);
    when(vietmapNavigation.options()).thenReturn(options);
    return vietmapNavigation;
  }
}