package vn.vietmap.services.android.navigation.v5.milestone;

import vn.vietmap.services.android.navigation.v5.routeprogress.RouteProgress;

public interface MilestoneEventListener {

  void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone);

}
