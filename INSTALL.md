# **Tài liệu hướng dẫn cài đặt VietMap Navigation Android SDK**
## Mục lục
[1. Cấu hình gradle và AndroidManifest](/INSTALL.md#i-thêm-các-dependencies-vào-buildgradle-module-app)

[2. Thêm các values cấu hình cho project](/INSTALL.md#ii-thêm-các-values-cấu-hình-cho-project)

[3. Tạo activity navigation để sử dụng sdk](/INSTALL.md#iii-tạo-activity-navigation-để-sử-dụng-sdk)

[4. Fetch route (Tìm một tuyến đường)](/INSTALL.md#iv-tìm-một-tuyến-đường)

[5. Start Navigation (Bắt đầu dẫn đường)](/INSTALL.md#v-start-navigation)

[6. Custom UI (Tuỳ chỉnh giao diện)](/INSTALL.md#custom-ui-tuỳ-chỉnh-giao-diện)

[7. Thêm accessToken và styleUrl](/INSTALL.md#thêm-accesstoken-và-styleurl)

###  **I**. Thêm các dependencies vào build.gradle module app

```gradle
    implementation "androidx.recyclerview:recyclerview:1.2.1"
    implementation "androidx.cardview:cardview:1,0,0"
    implementation "androidx.lifecycle:lifecycle-extensions:2.2.0"
    implementation "com.google.android.gms:play-services-location:21.0.1"
    implementation "com.jakewharton:butterknife:10.2.3"
    implementation 'androidx.core:core-ktx:1.7.0'
    implementation 'com.github.nhatpham0301:vietmap-sdk:1.0.3'
    implementation 'com.github.nhatpham0301:vietmap-android-navigation-ui:1.1.2'
    implementation 'com.github.nhatpham0301:vietmap-android-navigation:1.0.2'
    implementation 'org.maplibre.gl:android-sdk-services:5.9.0'
    implementation 'org.maplibre.gl:android-sdk-turf:5.9.0'
    implementation 'com.squareup.picasso:picasso:2.8'
```
Cấu hình **jitpack repository** tại file **setting.gradle**
```gradle

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Thêm 2 dòng dưới đây vào repositories (tại file setting.gradle)
        maven { url 'https://plugins.gradle.org/m2' }
        maven { url 'https://jitpack.io' }
    }
}
```
Đối với các project cũ, thêm vào file **build.gradle tại module project**
```gradle
allprojects {
    repositories {
        google()
        maven { url "https://jitpack.io" }
    }
}
```
Chuyển **compileSdk** và **targetSdk** vể version **_33_**
```
compileSdk 33
```
```
targetSdk 33
```
Thêm các quyền sau vào **AndroidManifest.xml**
```xml
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
### **II**. Thêm các values cấu hình cho project

Thêm các mã màu sau vào **res/values/colors.xml**
```xml
    <color name="colorPrimary">#8D64F9</color>
    <color name="colorPrimaryDark">#7845F3</color>
    <color name="colorAccent">#F56FA3</color>
    <color name="red">#FF0000</color>
```
Tạo file **styles.xml** tại thư mục **res/values** và thêm đoạn code 
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
<style name="CustomNavigationMapRoute" parent="NavigationMapRoute">
    <item name="upcomingManeuverArrowBorderColor">@color/red</item>
</style>

<style name="CustomNavigationView" parent="NavigationViewLight">
    <item name="navigationViewRouteStyle">@style/CustomNavigationMapRoute</item>
</style>

<style name="customInstructionView">
    <item name="navigationViewLocationLayerStyle">@style/NavigationLocationLayerStyle</item>
    <item name="navigationViewRouteOverviewDrawable">@drawable/ic_route_preview</item>
</style>

<style name="CustomInstructionView" parent="Theme.AppCompat.Light.NoActionBar">
    <item name="navigationViewPrimary">@color/mapbox_navigation_view_color_primary</item>
    <item name="navigationViewSecondary">@color/mapbox_navigation_view_color_secondary</item>
    <item name="navigationViewAccent">@color/mapbox_navigation_view_color_accent</item>
    <item name="navigationViewPrimaryText">@color/mapbox_navigation_view_color_secondary</item>
    <item name="navigationViewSecondaryText">@color/mapbox_navigation_view_color_accent_text</item>
    <item name="navigationViewDivider">@color/mapbox_navigation_view_color_divider</item>

    <item name="navigationViewListBackground">@color/mapbox_navigation_view_color_list_background</item>

    <item name="navigationViewBannerBackground">@color/mapbox_navigation_view_color_banner_background</item>
    <item name="navigationViewBannerPrimaryText">@color/mapbox_navigation_view_color_banner_primary_text</item>
    <item name="navigationViewBannerSecondaryText">@color/mapbox_navigation_view_color_banner_secondary_text</item>
    <item name="navigationViewBannerManeuverPrimary">@color/mapbox_navigation_view_color_banner_maneuver_primary</item>
    <item name="navigationViewBannerManeuverSecondary">@color/mapbox_navigation_view_color_banner_maneuver_secondary</item>

    <item name="navigationViewProgress">@color/mapbox_navigation_view_color_progress</item>
    <item name="navigationViewProgressBackground">@color/mapbox_navigation_view_color_progress_background</item>

    <item name="navigationViewRouteStyle">@style/NavigationMapRoute</item>

    <item name="navigationViewLocationLayerStyle">@style/mapbox_LocationLayer</item>

    <item name="navigationViewDestinationMarker">@drawable/map_marker_light</item>

    <item name="navigationViewRouteOverviewDrawable">@drawable/ic_route_preview</item>

    <item name="navigationViewMapStyle">@string/navigation_guidance_day</item>
</style>

<!-- Base application theme. -->
<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
    <!-- Customize your theme here. -->
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
    <item name="colorAccent">@color/colorAccent</item>
</style>

<style name="TestNavigationViewDark" parent="NavigationViewDark">
    <!-- Map style URL -->
    <item name="navigationViewMapStyle">
        YOUR STYLE URL HERE
    </item>
</style>

<style name="TestNavigationViewLight" parent="NavigationViewLight">
    <!-- Map style URL -->
    <item name="navigationViewMapStyle">
        YOUR STYLE URL HERE
    </item>
</style>
</resources>

```
Thêm đoạn code sau vào file **string.xml**

```xml

    <string name="title_mock_navigation">Giả bộ Điều hướng</string>
    <string name="description_mock_navigation">Giả bộ phiên điều hướng dùng máy định vị giả.</string>

    <string name="title_off_route_detection">Nhận ra Lạc đường</string>
    <string name="description_off_route_detection">Sử dụng lớp RouteUtils để xác định người dùng bị lạc đường.</string>

    <string name="title_reroute">Tìm Đường đi Mới</string>
    <string name="description_reroute">Thử chức năng tìm đường đi mới trong SDK Điều hướng</string>

    <string name="title_navigation_route_ui">Tuyến đường trên Bản đồ Điều hướng</string>
    <string name="description_navigation_route_ui">Vẽ tuyến đường trên bản đồ</string>

    <string name="title_navigation_launcher">Trình khởi động Điều hướng</string>
    <string name="description_navigation_launcher">Trải nghiệm giao diện người dùng có thể xen vào</string>

    <string name="title_end_navigation">Kết thúc Điều hướng</string>
    <string name="description_end_navigation">Cho biết cách kết thúc điều hướng dùng NavigationView</string>

    <string name="title_dual_navigation_map">Đôi Bản đồ Điều hướng</string>
    <string name="description_dual_navigation_map">Chỉ cách thêm NavigationView và MapView vào cùng bố cục</string>

    <string name="title_waypoint_navigation">Điều hướng giữa các Tọa độ điểm</string>
    <string name="description_waypoint_navigation">Điều hướng giữa các tọa độ điểm</string>

    <string name="title_embedded_navigation">Điều hướng được Nhúng</string>
    <string name="description_embedded_navigation">Điều hướng trong khung nhìn chứa các khung nhìn khác</string>

    <string name="title_fragment_navigation">NavigationView thực hiện bằng Fragment</string>
    <string name="description_fragment_navigation">NavigationView thực hiện bằng Fragment</string>

    <string name="settings">Thiết lập</string>
    <string name="simulate_route">Mô phỏng Tuyến đường</string>
    <string name="language">Ngôn ngữ</string>
    <string name="unit_type">Hệ Đo lường</string>
    <string name="route_profile">Chế độ</string>

    <string name="error_route_not_available">Tuyến đường hiện tại không có sẵn</string>
    <string name="error_select_longer_route">Vui lòng chọn một tuyến đường dài hơn</string>
    <string name="error_valid_route_not_found">Không tìm thấy tuyến đi được.</string>
    <string name="explanation_long_press_waypoint">Chạm lâu vào bản đồ để thả ghim tọa độ điểm</string>

    <string name="title_navigation_ui">Navigation UI</string>
    <string name="description_navigation_ui">Showcase a Navigation UI session. Optional with simulation.</string>


    <string name="title_component_navigation">MapboxNavigation with UI components</string>
    <string name="description_component_navigation">MapboxNavigation with UI components</string>

    <string name="unit_type_key" translatable="false">unit_type</string>
    <string name="simulate_route_key" translatable="false">simulate_route</string>
    <string name="language_key" translatable="false">language</string>
    <string name="route_profile_key" translatable="false">route_profile</string>
    <string name="default_locale" translatable="false">default_for_device</string>
    <string name="default_unit_type" translatable="false">default_for_device</string>
    <string name="current_night_mode" translatable="false">current_night_mode</string>

    <string name="new_location">Vĩ độ: %1$s Kinh độ: %2$s</string>
    <string name="map_view_style_url" translatable="false">YOUR_STYLE_URL_HERE</string>

    <string name="user_location_permission_explanation">Ứng dụng này cần sử dụng quyền vị trí để hoạt động chính xác.</string>
    <string name="user_location_permission_not_granted">Bạn chưa cung cấp quyền vị trí.</string>

```

### **Lưu ý: Cần thêm styleUrl vào vị trí _*YOUR_STYLE_URL_HERE*_ cho key map_view_style_url để chạy navigation**


Thêm các file **values** sau vào thư mục **res**


### **III**. Tạo activity navigation để sử dụng sdk 


Tạo một **activity** mới với tên **VietMapNavigationActivity**

Tại file **xml** của **activity**, thêm đoạn code như sau
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/vietmapNavigation">

    <com.mapbox.services.android.navigation.ui.v5.NavigationView
        android:id="@+id/navigationView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:visibility="invisible"
        app:maplibre_cameraZoom="16"
        app:layout_constraintHeight_percent="0.5"
        app:layout_constraintTop_toTopOf="parent"
        app:navigationDarkTheme="@style/NavigationViewDark"
        app:navigationLightTheme="@style/NavigationViewLight"/>

    <com.mapbox.mapboxsdk.maps.MapView
        android:id="@+id/mapView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:maplibre_cameraZoom="16"
        android:visibility="visible"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintHeight_percent="1"/>

    <ProgressBar
        android:id="@+id/loading"
        style="?android:attr/progressBarStyle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:indeterminate="true"
        android:visibility="invisible"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"/>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/launchNavigation"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp"
        android:layout_marginEnd="16dp"
        android:layout_marginRight="16dp"
        android:src="@drawable/ic_navigation"
        android:tint="@android:color/white"
        android:visibility="invisible"
        app:backgroundTint="@color/colorPrimary"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>
</androidx.constraintlayout.widget.ConstraintLayout>

```

Activity cần implements một số class Listener dưới đây để hứng event và xử lý trong quá trình sdk đang dẫn đường


```java
public class VietMapNavigationMapActivity extends AppCompatActivity implements 
        OnNavigationReadyCallback,
        ProgressChangeListener,
        NavigationListener,
        Callback<DirectionsResponse>,
        OnMapReadyCallback,
        MapboxMap.OnMapClickListener,
        MapboxMap.OnMapLongClickListener,
        MapboxMap.OnMoveListener,
        OnRouteSelectionChangeListener,
        OffRouteListener, 
        RouteListener 
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Hàm Mapbox.getInstance cần được gọi ngay khi khởi tạo activity
        Mapbox.getInstance(this);
        super.onCreate(savedInstanceState);
    }
}
```

>   - OnNavigationReadyCallback: Lắng nghe khi SDK bắt đầu dẫn đường
>   - ProgressChangeListener(location, routeProgress):    Liên tục lắng nghe vị trí hiện tại của người dùng, thông tin tuyến đường hiện tại, tuyến đường tiếp theo, khoảng cách còn lại mà người dùng cần phải đi
>   - NavigationListener: Bao gồm 3 function:
>       - onCancelNavigation: Lắng nghe khi người dùng huỷ dẫn đường
>       - onNavigationFinished: Lắng nghe khi người dùng hoàn tất chuyến đi
>       - onNavigationRunning: Lắng nghe khi người dùng đang di chuyển
>   - Callback(DirectionsResponse): Trả về kết quả khi getRoute hoàn thành
>   - OnMapReadyCallback: Lắng nghe khi map init hoàn thành và gán style cho map
>   - MapboxMap.OnMapClickListener,MapboxMap.OnMapLongClickListener, MapboxMap.OnMoveListener: Lắng nghe các sự kiện của map
>   - OnRouteSelectionChangeListener(DirectionsRoute newRouteSelected):
>       - onNewPrimaryRouteSelected: Lắng nghe khi người dùng chọn tuyến đường khác so với tuyến đường hiện tại, trả về đường đi mới người dùng chọn
>   - OffRouteListener: Lắng nghe khi người dùng đi sai tuyến đường, từ đó tìm tuyến khác theo hướng di chuyển của người dùng
>       - userOffRoute(Location currentLocation): Hàm được gọi khi người dùng đi sai đường đi, từ đó tìm đường đi mới dựa trên vị trí hiện tại được trả về (currentLocation) 
>   - RouteListener: Lắng nghe khi người dùng tới đích
>       - onArrival(): Hàm được gọi khi người dùng đi tới đích (destination)

Khai báo các biến cần thiết 

```java
    private static final int DEFAULT_CAMERA_ZOOM = 20;
    private ConstraintLayout customUINavigation;
    private NavigationView navigationView;
    private MapView mapView;
    private ProgressBar loading;
    private FloatingActionButton launchNavigationFab;
    private Point origin = Point.fromLngLat(106.675789, 10.759050);
    private Point destination = Point.fromLngLat(106.686777, 10.775056);
    private DirectionsRoute route;
    private boolean isNavigationRunning;
    private MapboxNavigation mapboxNavigation;
    private LocationEngine locationEngine;
    private NavigationMapRoute mapRoute;
    private MapboxMap mapboxMap;
    private ConstraintSet navigationMapConstraint;
    private ConstraintSet navigationMapExpandedConstraint;
    private boolean[] constraintChanged;
    private LocationComponent locationComponent;
    private ReplayRouteLocationEngine mockLocationEngine;
    private FusedLocationProviderClient fusedLocationClient;
    private int BEGIN_ROUTE_MILESTONE = 1001;
    private boolean reRoute = false;
    private boolean isArrived = false;
    private NavigationViewOptions.Builder mapviewNavigationOptions;
```
Tại hàm **onCreate**, bắt đầu khởi tạo màn hình dẫn đường
```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Thêm đoạn code sau vào hàm onCreate
        CustomNavigationNotification customNotification = new CustomNavigationNotification(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            customNotification.createNotificationChannel(this);
        }
        MapboxNavigationOptions options = MapboxNavigationOptions.builder()
                .navigationNotification(customNotification)
                .build();
        mapboxNavigation = new MapboxNavigation(this, options);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initializeViews(savedInstanceState);
        navigationView.initialize(this);
        navigationMapConstraint = new ConstraintSet();
        navigationMapConstraint.clone(customUINavigation);
        navigationMapExpandedConstraint = new ConstraintSet();
        navigationMapExpandedConstraint.clone(this, R.layout.vietmap_navigation_expand);
        navigationView.initViewConfig(true);
        constraintChanged = new boolean[]{false};
    }
```
Hàm **initializeViews**
```java
    private void initializeViews(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_viet_map_navigation);
        customUINavigation = findViewById(R.id.vietmapNavigation);
        mapView = findViewById(R.id.mapView);
        navigationView = findViewById(R.id.navigationView);
        loading = findViewById(R.id.loading);
        launchNavigationFab = findViewById(R.id.launchNavigation);
        navigationView.onCreate(savedInstanceState);
        mapView.onCreate(savedInstanceState);
        launchNavigationFab.setOnClickListener(v -> {
            expandCollapse();
            launchNavigation();
        });
        mapView.getMapAsync(this);
    }
```
### Tại hàm **onMapReady**:
```java
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri(YOUR_STYLE_URL_HERE), style -> {
            initLocationEngine();
            getCurrentLocation();
            enableLocationComponent(style);
            initMapRoute();
        });
        this.mapboxMap.addOnMapClickListener(this);
    }
```
```java
    private void initLocationEngine() {
        mockLocationEngine = new ReplayRouteLocationEngine();
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        long DEFAULT_INTERVAL_IN_MILLISECONDS = 5000;
        long DEFAULT_MAX_WAIT_TIME = 30000;
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mockLocationEngine.assignLastLocation(origin);
            return;
        }
    }

    private void initMapRoute() {

        mapRoute = new NavigationMapRoute(mapView, mapboxMap);
        mapRoute.setOnRouteSelectionChangeListener(this);
        mapRoute.addProgressChangeListener(new MapboxNavigation(this));
    }


    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                        }
                    });
            return;
        }
    }
    private void enableLocationComponent(Style style) {
        locationComponent = mapboxMap.getLocationComponent();

        if (locationComponent != null) {
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, style).build()
            );
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
            locationComponent.zoomWhileTracking(DEFAULT_CAMERA_ZOOM);
            locationComponent.setRenderMode(RenderMode.GPS);
            locationComponent.setLocationEngine(locationEngine);
        }
    }
```


Tạo **_layout xml_** _vietmap_navigation_expand_
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/vietmapNavigationExpand"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.mapbox.services.android.navigation.ui.v5.NavigationView
        android:id="@+id/navigationView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:visibility="visible"
        app:maplibre_cameraZoom="15"
        app:layout_constraintHeight_percent="1"
        app:layout_constraintTop_toTopOf="@+id/vietmapNavigationExpand"
        app:layout_constraintBottom_toBottomOf="@+id/vietmapNavigationExpand"
        app:navigationDarkTheme="@style/NavigationViewDark"
        app:navigationLightTheme="@style/NavigationViewLight"/>

    <com.mapbox.mapboxsdk.maps.MapView
        android:id="@+id/mapView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:maplibre_cameraZoom="15"
        android:visibility="gone"
        app:layout_constraintBottom_toBottomOf="@+id/vietmapNavigationExpand"
        app:layout_constraintHeight_percent="0"/>

    <ProgressBar
        android:id="@+id/loading"
        style="?android:attr/progressBarStyle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:indeterminate="true"
        android:visibility="gone"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"/>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/launchNavigation"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp"
        android:layout_marginEnd="16dp"
        android:layout_marginRight="16dp"
        android:src="@drawable/ic_navigation"
        android:tint="@android:color/white"
        android:visibility="gone"
        app:backgroundTint="@color/colorPrimary"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```
Hàm **expandCollapse**:
```java
    private void expandCollapse() {
        TransitionManager.beginDelayedTransition(customUINavigation);
        ConstraintSet constraint;
        if (constraintChanged[0]) {
            constraint = navigationMapConstraint;
        } else {
            constraint = navigationMapExpandedConstraint;
        }
        constraint.applyTo(customUINavigation);
        constraintChanged[0] = !constraintChanged[0];
    }
```
Hàm **stopNavigationFunction**
```java

    void stopNavigationFunction(){
        navigationView.stopNavigation();
        mapboxNavigation.stopNavigation();
        launchNavigationFab.show();
    }
```
Hàm override **stopNavigation**:
```java
    @Override
    public void onCancelNavigation() {
        expandCollapse();
        stopNavigationFunction();
    }
```
Tạo class **CustomNavigationNotification** để bắn thông báo trên từng tuyến đường cho người dùng
```java
public class CustomNavigationNotification implements NavigationNotification {

    private static final int CUSTOM_NOTIFICATION_ID = 91234821;
    private static final String STOP_NAVIGATION_ACTION = "stop_navigation_action";
    private final Notification customNotification;
    private final NotificationCompat.Builder customNotificationBuilder;
    private final NotificationManager notificationManager;
    private BroadcastReceiver stopNavigationReceiver;
    private int numberOfUpdates;

    public CustomNavigationNotification(Context applicationContext) {
        notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        customNotificationBuilder = new NotificationCompat.Builder(applicationContext, NAVIGATION_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Custom Navigation Notification")
                .setContentText("Display your own content here!")
                .setContentIntent(createPendingStopIntent(applicationContext));
        customNotification = customNotificationBuilder.build();
    }

    @Override
    public Notification getNotification() {
        return customNotification;
    }

    @Override
    public int getNotificationId() {
        return CUSTOM_NOTIFICATION_ID;
    }

    @Override
    public void updateNotification(RouteProgress routeProgress) {
        // Update the builder with a new number of updates
        customNotificationBuilder.setContentText("Number of updates: " + numberOfUpdates++);
        notificationManager.notify(CUSTOM_NOTIFICATION_ID, customNotificationBuilder.build());
    }

    @Override
    public void onNavigationStopped(Context context) {
        try {
            context.unregisterReceiver(stopNavigationReceiver);
        }catch(Exception e){}
        notificationManager.cancel(CUSTOM_NOTIFICATION_ID);
    }

    public void register(BroadcastReceiver stopNavigationReceiver, Context applicationContext) {
        this.stopNavigationReceiver = stopNavigationReceiver;
        applicationContext.registerReceiver(stopNavigationReceiver, new IntentFilter(STOP_NAVIGATION_ACTION));
    }

    private PendingIntent createPendingStopIntent(Context context) {
        Intent stopNavigationIntent = new Intent(STOP_NAVIGATION_ACTION);
        return PendingIntent.getBroadcast(context, 0, stopNavigationIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public void createNotificationChannel(Context context) {
        NotificationChannel chan = new NotificationChannel(NAVIGATION_NOTIFICATION_CHANNEL, "CustomNavigationNotification", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (service != null) {
            service.createNotificationChannel(chan);
        }
    }
}
```
### **IV**. Tìm một tuyến đường
API tìm tuyến đường yêu cầu 2 params là **_origin_** và **_destination_**, là vị trí hiện tại của người dùng và vị trí đích đến.

Ví dụ:
```java
    Point origin = Point.fromLngLat(106.675884,10.759197);
    Point destination = Point.fromLngLat( 105.577136, 18.932147);
```
### Từ hai điểm **_point_** và **_destination_** này, chúng ta có thể gọi hàm **fetchRoute** như sau:
```java
private void fetchRoute(Point origin, Point destination) {
        NavigationRoute builder = NavigationRoute.builder(this)
                .accessToken("YOUR_ACCESS_TOKEN_HERE")
                .origin(origin)
                .destination(destination)
                .alternatives(true)
                .build();
        builder.getRoute(this);
    }
```
Sau khi gọi hàm **fetchRoute**, bạn sẽ nhận được kết quả tại listener như sau:
```java
    @Override
    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        if (validRouteResponse(response)) {
            if (reRoute) {
                route = response.body().routes().get(0);
                initNavigationOptions();
                navigationView.updateCameraRouteOverview();
                mapboxNavigation.addNavigationEventListener(this);
                mapboxNavigation.startNavigation(route);
                navigationView.startNavigation(this.mapviewNavigationOptions.build());
                reRoute = false;
                isArrived=false;
            } else {
                launchNavigationFab.show();
                route = response.body().routes().get(0);
                mapRoute.addRoutes(response.body().routes());
                if (isNavigationRunning) {
                    launchNavigation();
                }
            }
        }
    }
```
### **V**. Start Navigation
Sau khi gọi được tuyến đường, tiếp theo cần cấu hình một số tuỳ chọn để bắt đầu dẫn đường
```java
void initNavigationOptions(){
        MapboxNavigationOptions navigationOptions = MapboxNavigationOptions.builder()
                .build();
        mapviewNavigationOptions =NavigationViewOptions.builder()
                .navigationListener(this)
                .routeListener(this)
                .navigationOptions(navigationOptions)
                .locationEngine(locationEngine)
                .shouldSimulateRoute(false)
                .progressChangeListener(progressChangeListener)
                .directionsRoute(route)
                .onMoveListener(this);
    }
```
Hàm **progressChangeListener** trả về 2 thông tin là **location** (vị trí hiện tại của người dùng) và **routeProgress** (Thông tin tuyến đường người dùng đang đi qua, hướng rẽ tiếp theo, khoảng cách,...)
```java
    private ProgressChangeListener progressChangeListener = (location, routeProgress) -> {
        System.out.println("Progress Changing");
    };
```
```java
    private boolean validRouteResponse(Response<DirectionsResponse> response) {
        return response.body() != null && !response.body().routes().isEmpty();
    }
```
Hàm **initNavigationOptions** sẽ được gọi trước khi bắt đầu dẫn đường
```java
    private void launchNavigation() {
        launchNavigationFab.hide();
        navigationView.setVisibility(View.VISIBLE);
        mapboxNavigation.addOffRouteListener(this);
        initNavigationOptions();
        mapboxNavigation.startNavigation(route);
        navigationView.startNavigation(this.mapviewNavigationOptions.build());
        isArrived=false;
    }
```
Hàm **launchNavigation** được gọi tại một button bất kì tuỳ theo người dùng khai báo

Tại hàm **launchNavigation**, có hai hàm **startNavigation** được khởi chạy:
-   Hàm của **mapboxNavigation** tương tự một controller để lắng nghe các trạng thái của chuyến đi và trả về toàn bộ các thông tin của chuyến đi.
-   Hàm của **navigationView** để bắt đầu hiển thị dẫn đường lên màn hình.
```java
    @Override
    public void userOffRoute(Location location) {
        if(isArrived) return;
            reRoute = true;
            fetchRoute(Point.fromLngLat(location.getLongitude(), location.getLatitude()), destination);
    }
```
#### Hàm **userOffRoute** lắng nghe khi người dùng đi không đúng với lộ trình được trả về, từ đó tìm tuyến đường mới phù hợp hơn với hướng di chuyển hiện tại của người dùng
```java
    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        
    }
```
#### Hàm **onProgressChange** lắng nghe khi người dùng di chuyển, liên tục cập nhật thông tin về tuyến đường người dùng đang di chuyển, khoảng cách còn lại,... 
```java
    @Override
    public void onArrival() {
        if(isArrived) return;
        //Xử lý thông báo và kết thúc dẫn đường tại đây
        isArrived=true;
    }
```
Hàm **onArrival** lắng nghe khi người dùng đã di chuyển tới đích **(destination)**, từ đó có thể tự tạo thông báo hoặc alert cho người dùng.

Thêm các hàm **callbacks** sau để đảm bảo khởi tạo và quản lý bộ nhớ phù hợp, cũng như xử lý các actions của người dùng, thành phần NavigationView phải được liên kết với vòng đời của activity bằng cách sử dụng một số callbacks dưới đây. Điều này cho phép NavigationView xử lý đúng lifecycle của activity và phản hồi tương ứng. 
```java

    @Override
    public void onStart() {
        super.onStart();
        navigationView.onStart();
        mapView.onStart();
    }

    @Override
        public void onResume() {
        super.onResume();
        navigationView.onResume();
        mapView.onResume();
        if (locationEngine != null) {
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        stopNavigationFunction();

        if (!navigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        navigationView.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navigationView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        navigationView.onPause();
        mapView.onPause();
        if (locationEngine != null) {
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        navigationView.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationView.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null) {
        }
    }

```

# **Custom UI (Tuỳ chỉnh giao diện)**
```java
    navigationView.initViewConfig(true);
``` 
Tại hàm onCreate, thêm đoạn code phía trên để ẩn đi toàn bộ giao diện mặc định, chỉ để lại phần bản đồ và phần dẫn đường. Các thông tin của chuyến đi sẽ được cung cấp đầy đủ.

```xml
    <androidx.appcompat.widget.LinearLayoutCompat
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toStartOf="parent"
        android:id="@+id/navigationAction"
        android:orientation="horizontal"
        app:layout_constraintBottom_toBottomOf="parent">
        <Button
            android:id="@+id/overViewRouteButton"
            android:layout_width="wrap_content"
            app:layout_constraintStart_toEndOf="@+id/stopNavigation"
            android:layout_height="wrap_content"
            app:layout_constraintTop_toBottomOf="@id/stopNavigation"
            android:layout_gravity="top"
            android:layout_margin="16dp"
            android:text="Over View"
            app:layout_anchorGravity="top|left"
            android:visibility="gone"
            android:textColor="@color/black"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />
        <Button
            android:id="@+id/recenterBtnCustom"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:layout_constraintBottom_toBottomOf="parent"
            android:layout_gravity="top"
            android:layout_margin="16dp"
            android:text="Recenter"
            android:visibility="gone"
            android:textColor="@color/black"
            app:layout_anchorGravity="top|left"
            app:layout_constraintStart_toStartOf="parent" />
        <Button
            android:id="@+id/stopNavigation"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="@color/black"
            app:layout_constraintTop_toBottomOf="@id/overViewRouteButton"
            android:visibility="gone"
            app:layout_constraintBottom_toBottomOf="parent"
            android:layout_gravity="top"
            android:layout_margin="16dp"
            android:text="Stop"
            app:layout_anchorGravity="top|left"
            app:layout_constraintStart_toStartOf="parent" />
    </androidx.appcompat.widget.LinearLayoutCompat>
```
Thêm đoạn code trên vào file layout xml của VietmapNavigationActivity

```java

    private Button recenterButton;
    private Button overViewRouteButton;
    private Button stopNavigation;
```
Khai báo thêm 3 button để thực hiện các thao tác như về giữa, xem toàn bộ tuyến đường, huỷ dẫn đường

## **Các hàm lắng nghe và thực thi trong màn hình tuỳ chỉnh giao diện**
-   Khởi tạo biến **NavigationPresenter**
```java
    NavigationPresenter navigationPresenter = navigationView.getNavigationPresenter();
```
### Tạo controller để điều khiển các hàm:
-   Hàm về giữa **(recenterFunction)**:
```java
    recenterButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            presenter.onRecenterClick();
            changeNavigationActionState(true);
        }
    });
```
-   Hàm xem tổng quan đường đi **(routeOverViewFunction)**:
```java
    overViewRouteButton.setOnClickListener(view -> {
        presenter.onRouteOverviewClick();
        changeNavigationActionState(false);
    });
```
-   Hàm kết thúc dẫn đường **(stopNavigation)**:
```java
    stopNavigation.setOnClickListener(view -> {
        changeNavigationActionState(false);
        expandCollapse();
        stopNavigationFunction();
    });
```
```java
    void stopNavigationFunction(){
        navigationView.stopNavigation();
        mapboxNavigation.stopNavigation();
        recenterButton.setVisibility(View.GONE);
        overViewRouteButton.setVisibility(View.GONE);
        stopNavigation.setVisibility(View.GONE);
        launchNavigationFab.show();
    }
```
-   Hàm lắng nghe khi người dùng di chuyển bản đồ để hiển thị nút quay về đường đi **(recenterButton)**:
```java
    @Override
    public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
        changeNavigationActionState(false);
    }
```
-   Hàm thay đổi trạng thái của các nút nhấn **(changeNavigationActionState)**:
```java
    void changeNavigationActionState(boolean isNavigationRunning) {
        if (!isNavigationRunning) {
            overViewRouteButton.setVisibility(View.GONE);
            recenterButton.setVisibility(View.VISIBLE);
            stopNavigation.setVisibility(View.GONE);
        } else {
            overViewRouteButton.setVisibility(View.VISIBLE);
            recenterButton.setVisibility(View.GONE);
            stopNavigation.setVisibility(View.VISIBLE);
        }
    }
```
- Các thông tin về đường đi, khoảng cách,... được trả về tại hàm [_**onProgressChange**_](/INSTALL.md#hàm-onprogresschange-lắng-nghe-khi-người-dùng-di-chuyển-liên-tục-cập-nhật-thông-tin-về-tuyến-đường-người-dùng-đang-di-chuyển-khoảng-cách-còn-lại) 

# Thêm **accessToken** và  **styleUrl**
Để đảm bảo ứng dụng không bị crash khi chạy, bạn cần thêm đầy đủ **styleUrl** và **accessToken** mà VietMap cung cấp tại các vi trí sau:

[Thêm **_styleUrl_** tại file _src/values/string.xml_](/INSTALL.md#lưu-ý-cần-thêm-styleurl-cho-key-map_view_style_url-để-chạy-navigation)

[Thêm **_styleUrl_** tại hàm **onMapReady**](/INSTALL.md#tại-hàm-onmapready)


[Thêm **_accessToken_** tại hàm **fetchRoute**](/INSTALL.md#từ-hai-điểm-point-và-destination-này-chúng-ta-có-thể-gọi-hàm-fetchroute-như-sau)