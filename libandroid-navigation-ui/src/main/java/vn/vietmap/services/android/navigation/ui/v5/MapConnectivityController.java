package vn.vietmap.services.android.navigation.ui.v5;

import vn.vietmap.vietmapsdk.Vietmap;

class MapConnectivityController {

  void assign(Boolean state) {
    Vietmap.setConnected(state);
  }
}
