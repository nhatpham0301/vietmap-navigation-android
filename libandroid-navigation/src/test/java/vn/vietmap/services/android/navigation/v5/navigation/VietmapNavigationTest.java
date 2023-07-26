package vn.vietmap.services.android.navigation.v5.navigation;

import android.content.Context;

import vn.vietmap.vietmapsdk.location.engine.LocationEngine;
import vn.vietmap.services.android.navigation.v5.BaseTest;
import vn.vietmap.services.android.navigation.v5.milestone.BannerInstructionMilestone;
import vn.vietmap.services.android.navigation.v5.milestone.Milestone;
import vn.vietmap.services.android.navigation.v5.milestone.StepMilestone;
import vn.vietmap.services.android.navigation.v5.milestone.VoiceInstructionMilestone;
import vn.vietmap.services.android.navigation.v5.navigation.camera.SimpleCamera;
import vn.vietmap.services.android.navigation.v5.offroute.OffRoute;
import vn.vietmap.services.android.navigation.v5.snap.Snap;
import vn.vietmap.services.android.navigation.v5.snap.SnapToRoute;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static vn.vietmap.services.android.navigation.v5.navigation.NavigationConstants.BANNER_INSTRUCTION_MILESTONE_ID;
import static vn.vietmap.services.android.navigation.v5.navigation.NavigationConstants.VOICE_INSTRUCTION_MILESTONE_ID;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VietmapNavigationTest extends BaseTest {

  @Test
  public void sanityTest() {
    VietmapNavigation navigation = buildVietmapNavigation();

    assertNotNull(navigation);
  }

  @Test
  public void sanityTestWithOptions() {
    VietmapNavigationOptions options = VietmapNavigationOptions.builder().build();
    VietmapNavigation navigationWithOptions = buildVietmapNavigationWithOptions(options);

    assertNotNull(navigationWithOptions);
  }

  @Test
  public void voiceInstructionMilestone_onInitializationDoesGetAdded() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();

    int identifier = searchForVoiceInstructionMilestone(navigation);

    assertEquals(VOICE_INSTRUCTION_MILESTONE_ID, identifier);
  }

  @Test
  public void bannerInstructionMilestone_onInitializationDoesGetAdded() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();

    int identifier = searchForBannerInstructionMilestone(navigation);

    assertEquals(BANNER_INSTRUCTION_MILESTONE_ID, identifier);
  }

  @Test
  public void defaultMilestones_onInitializationDoNotGetAdded() throws Exception {
    VietmapNavigationOptions options = VietmapNavigationOptions.builder().defaultMilestonesEnabled(false).build();
    VietmapNavigation navigationWithOptions = buildVietmapNavigationWithOptions(options);

    assertEquals(0, navigationWithOptions.getMilestones().size());
  }

  @Test
  public void defaultEngines_offRouteEngineDidGetInitialized() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();

    assertNotNull(navigation.getOffRouteEngine());
  }

  @Test
  public void defaultEngines_snapEngineDidGetInitialized() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();

    assertNotNull(navigation.getSnapEngine());
  }

  @Test
  public void addMilestone_milestoneDidGetAdded() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();
    Milestone milestone = new StepMilestone.Builder().build();

    navigation.addMilestone(milestone);

    assertTrue(navigation.getMilestones().contains(milestone));
  }

  @Test
  public void addMilestone_milestoneOnlyGetsAddedOnce() throws Exception {
    VietmapNavigationOptions options = VietmapNavigationOptions.builder().defaultMilestonesEnabled(false).build();
    VietmapNavigation navigationWithOptions = buildVietmapNavigationWithOptions(options);

    Milestone milestone = new StepMilestone.Builder().build();
    navigationWithOptions.addMilestone(milestone);
    navigationWithOptions.addMilestone(milestone);

    assertEquals(1, navigationWithOptions.getMilestones().size());
  }

  @Test
  public void removeMilestone_milestoneDidGetRemoved() throws Exception {
    VietmapNavigationOptions options = VietmapNavigationOptions.builder().defaultMilestonesEnabled(false).build();
    VietmapNavigation navigationWithOptions = buildVietmapNavigationWithOptions(options);

    Milestone milestone = new StepMilestone.Builder().build();
    navigationWithOptions.addMilestone(milestone);
    navigationWithOptions.removeMilestone(milestone);

    assertEquals(0, navigationWithOptions.getMilestones().size());
  }

  @Test
  public void removeMilestone_milestoneDoesNotExist() throws Exception {
    VietmapNavigationOptions options = VietmapNavigationOptions.builder().defaultMilestonesEnabled(false).build();
    VietmapNavigation navigationWithOptions = buildVietmapNavigationWithOptions(options);

    Milestone milestone = new StepMilestone.Builder().build();
    navigationWithOptions.addMilestone(new StepMilestone.Builder().build());
    navigationWithOptions.removeMilestone(milestone);

    assertEquals(1, navigationWithOptions.getMilestones().size());
  }

  @Test
  public void removeMilestone_nullRemovesAllMilestones() throws Exception {
    VietmapNavigationOptions options = VietmapNavigationOptions.builder().defaultMilestonesEnabled(false).build();
    VietmapNavigation navigationWithOptions = buildVietmapNavigationWithOptions(options);
    navigationWithOptions.addMilestone(new StepMilestone.Builder().build());
    navigationWithOptions.addMilestone(new StepMilestone.Builder().build());
    navigationWithOptions.addMilestone(new StepMilestone.Builder().build());
    navigationWithOptions.addMilestone(new StepMilestone.Builder().build());

    navigationWithOptions.removeMilestone(null);

    assertEquals(0, navigationWithOptions.getMilestones().size());
  }

  @Test
  public void removeMilestone_correctMilestoneWithIdentifierGetsRemoved() throws Exception {
    VietmapNavigationOptions options = VietmapNavigationOptions.builder().defaultMilestonesEnabled(false).build();
    VietmapNavigation navigationWithOptions = buildVietmapNavigationWithOptions(options);
    int removedMilestoneIdentifier = 5678;
    Milestone milestone = new StepMilestone.Builder().setIdentifier(removedMilestoneIdentifier).build();
    navigationWithOptions.addMilestone(milestone);

    navigationWithOptions.removeMilestone(removedMilestoneIdentifier);

    assertEquals(0, navigationWithOptions.getMilestones().size());
  }

  @Test
  public void removeMilestone_noMilestoneWithIdentifierFound() throws Exception {
    VietmapNavigationOptions options = VietmapNavigationOptions.builder().defaultMilestonesEnabled(false).build();
    VietmapNavigation navigationWithOptions = buildVietmapNavigationWithOptions(options);
    navigationWithOptions.addMilestone(new StepMilestone.Builder().build());
    int removedMilestoneIdentifier = 5678;

    navigationWithOptions.removeMilestone(removedMilestoneIdentifier);

    assertEquals(1, navigationWithOptions.getMilestones().size());
  }

  @Test
  public void addMilestoneList_duplicateIdentifiersAreIgnored() throws Exception {
    VietmapNavigationOptions options = VietmapNavigationOptions.builder().defaultMilestonesEnabled(false).build();
    VietmapNavigation navigationWithOptions = buildVietmapNavigationWithOptions(options);
    int milestoneIdentifier = 5678;
    Milestone milestone = new StepMilestone.Builder().setIdentifier(milestoneIdentifier).build();
    navigationWithOptions.addMilestone(milestone);
    List<Milestone> milestones = new ArrayList<>();
    milestones.add(milestone);
    milestones.add(milestone);
    milestones.add(milestone);

    navigationWithOptions.addMilestones(milestones);

    assertEquals(1, navigationWithOptions.getMilestones().size());
  }

  @Test
  public void addMilestoneList_allMilestonesAreAdded() throws Exception {
    VietmapNavigationOptions options = VietmapNavigationOptions.builder().defaultMilestonesEnabled(false).build();
    VietmapNavigation navigationWithOptions = buildVietmapNavigationWithOptions(options);
    int firstMilestoneId = 5678;
    int secondMilestoneId = 5679;
    Milestone firstMilestone = new StepMilestone.Builder().setIdentifier(firstMilestoneId).build();
    Milestone secondMilestone = new StepMilestone.Builder().setIdentifier(secondMilestoneId).build();
    List<Milestone> milestones = new ArrayList<>();
    milestones.add(firstMilestone);
    milestones.add(secondMilestone);

    navigationWithOptions.addMilestones(milestones);

    assertEquals(2, navigationWithOptions.getMilestones().size());
  }

  @Test
  public void getLocationEngine_returnsCorrectLocationEngine() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();
    LocationEngine locationEngine = mock(LocationEngine.class);
    LocationEngine locationEngineInstanceNotUsed = mock(LocationEngine.class);

    navigation.setLocationEngine(locationEngine);

    assertNotSame(locationEngineInstanceNotUsed, navigation.getLocationEngine());
    assertEquals(locationEngine, navigation.getLocationEngine());
  }

  @Test
  public void startNavigation_doesSendTrueToNavigationEvent() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();
    NavigationEventListener navigationEventListener = mock(NavigationEventListener.class);

    navigation.addNavigationEventListener(navigationEventListener);
    navigation.startNavigation(buildTestDirectionsRoute());

    verify(navigationEventListener, times(1)).onRunning(true);
  }

  @Test
  public void setSnapEngine_doesReplaceDefaultEngine() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();

    Snap snap = mock(Snap.class);
    navigation.setSnapEngine(snap);

    assertTrue(!(navigation.getSnapEngine() instanceof SnapToRoute));
  }

  @Test
  public void setOffRouteEngine_doesReplaceDefaultEngine() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();

    OffRoute offRoute = mock(OffRoute.class);
    navigation.setOffRouteEngine(offRoute);

    assertEquals(offRoute, navigation.getOffRouteEngine());
  }

  @Test
  public void getCameraEngine_returnsNonNullEngine() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();

    navigation.setOffRouteEngine(null);

    assertNotNull(navigation.getCameraEngine());
  }

  @Test
  public void getCameraEngine_returnsSimpleCameraWhenNull() throws Exception {
    VietmapNavigation navigation = buildVietmapNavigation();

    navigation.setCameraEngine(null);

    assertTrue(navigation.getCameraEngine() instanceof SimpleCamera);
  }

  private VietmapNavigation buildVietmapNavigation() {
    Context context = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(context);
    return new VietmapNavigation(context, mock(LocationEngine.class));
  }

  private VietmapNavigation buildVietmapNavigationWithOptions(VietmapNavigationOptions options) {
    Context context = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(context);
    return new VietmapNavigation(context, options, mock(LocationEngine.class));
  }

  private int searchForVoiceInstructionMilestone(VietmapNavigation navigation) {
    int identifier = -1;
    for (Milestone milestone : navigation.getMilestones()) {
      if (milestone instanceof VoiceInstructionMilestone) {
        identifier = milestone.getIdentifier();
      }
    }
    return identifier;
  }

  private int searchForBannerInstructionMilestone(VietmapNavigation navigation) {
    int identifier = -1;
    for (Milestone milestone : navigation.getMilestones()) {
      if (milestone instanceof BannerInstructionMilestone) {
        identifier = milestone.getIdentifier();
      }
    }
    return identifier;
  }
}