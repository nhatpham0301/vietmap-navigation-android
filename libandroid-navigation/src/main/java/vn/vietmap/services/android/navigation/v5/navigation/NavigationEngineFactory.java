package vn.vietmap.services.android.navigation.v5.navigation;

import vn.vietmap.services.android.navigation.v5.navigation.camera.Camera;
import vn.vietmap.services.android.navigation.v5.navigation.camera.SimpleCamera;
import vn.vietmap.services.android.navigation.v5.offroute.OffRoute;
import vn.vietmap.services.android.navigation.v5.offroute.OffRouteDetector;
import vn.vietmap.services.android.navigation.v5.route.FasterRoute;
import vn.vietmap.services.android.navigation.v5.route.FasterRouteDetector;
import vn.vietmap.services.android.navigation.v5.snap.Snap;
import vn.vietmap.services.android.navigation.v5.snap.SnapToRoute;

class NavigationEngineFactory {

  private OffRoute offRouteEngine;
  private FasterRoute fasterRouteEngine;
  private Snap snapEngine;
  private Camera cameraEngine;

  NavigationEngineFactory() {
    initializeDefaultEngines();
  }

  OffRoute retrieveOffRouteEngine() {
    return offRouteEngine;
  }

  void updateOffRouteEngine(OffRoute offRouteEngine) {
    if (offRouteEngine == null) {
      return;
    }
    this.offRouteEngine = offRouteEngine;
  }

  FasterRoute retrieveFasterRouteEngine() {
    return fasterRouteEngine;
  }

  void updateFasterRouteEngine(FasterRoute fasterRouteEngine) {
    if (fasterRouteEngine == null) {
      return;
    }
    this.fasterRouteEngine = fasterRouteEngine;
  }

  Snap retrieveSnapEngine() {
    return snapEngine;
  }

  void updateSnapEngine(Snap snapEngine) {
    if (snapEngine == null) {
      return;
    }
    this.snapEngine = snapEngine;
  }

  Camera retrieveCameraEngine() {
    return cameraEngine;
  }

  void updateCameraEngine(Camera cameraEngine) {
    if (cameraEngine == null) {
      return;
    }
    this.cameraEngine = cameraEngine;
  }

  private void initializeDefaultEngines() {
    cameraEngine = new SimpleCamera();
    snapEngine = new SnapToRoute();
    offRouteEngine = new OffRouteDetector();
    fasterRouteEngine = new FasterRouteDetector();
  }
}
