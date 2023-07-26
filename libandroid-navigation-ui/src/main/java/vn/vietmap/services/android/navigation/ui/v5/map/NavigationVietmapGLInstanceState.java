package vn.vietmap.services.android.navigation.ui.v5.map;

import android.os.Parcel;
import android.os.Parcelable;

public class NavigationVietmapGLInstanceState implements Parcelable {

  private final NavigationMapSettings settings;

  NavigationVietmapGLInstanceState(NavigationMapSettings settings) {
    this.settings = settings;
  }

  NavigationMapSettings retrieveSettings() {
    return settings;
  }

  private NavigationVietmapGLInstanceState(Parcel in) {
    settings = in.readParcelable(NavigationMapSettings.class.getClassLoader());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(settings, flags);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<NavigationVietmapGLInstanceState> CREATOR =
    new Creator<NavigationVietmapGLInstanceState>() {
      @Override
      public NavigationVietmapGLInstanceState createFromParcel(Parcel in) {
        return new NavigationVietmapGLInstanceState(in);
      }

      @Override
      public NavigationVietmapGLInstanceState[] newArray(int size) {
        return new NavigationVietmapGLInstanceState[size];
      }
    };
}
