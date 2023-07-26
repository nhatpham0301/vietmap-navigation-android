package com.mapbox.services.android.navigation.testapp;

import android.app.Application;

import vn.vietmap.vietmapsdk.BuildConfig;
import vn.vietmap.vietmapsdk.Vietmap;

import timber.log.Timber;

public class NavigationApplication extends Application {

  private static final String LOG_TAG = NavigationApplication.class.getSimpleName();
  private static final String DEFAULT_MAPBOX_ACCESS_TOKEN = "YOUR_MAPBOX_ACCESS_TOKEN_GOES_HERE";

  @Override
  public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
//      Timber.plant(new Timber.DebugTree());
    }

    Vietmap.getInstance(getApplicationContext());
  }

}
