# **Tài liệu hướng dẫn cài đặt VietMap Navigation Android SDK**

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

### **II**. Tạo layout xml cho giao diện xem trước quãng đường trước khi bắt đầu điều hướng


```xml

<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/dualNavigationMap">

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

### **III**. Tạo activity navigation để sử dụng sdk 



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
        LocationListener,
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
>   - ProgressChangeListener:    Liên tục lắng nghe vị trí hiện tại của người dùng, thông tin tuyến đường hiện tại, tuyến đường tiếp theo, khoảng cách còn lại mà người dùng cần phải đi
>   - NavigationListener: Bao gồm 3 function:
>       - onCancelNavigation: Lắng nghe khi người dùng huỷ dẫn đường
>       - onNavigationFinished: Lắng nghe khi người dùng hoàn tất chuyến đi
>       - onNavigationRunning: Lắng nghe khi người dùng đang di chuyển
>   - Callback(DirectionsResponse): Trả về kết quả khi getRoute hoàn thành
>   - OnMapReadyCallback: Lắng nghe khi map init hoàn thành và gán style cho map
>   - MapboxMap.OnMapClickListener,MapboxMap.OnMapLongClickListener, MapboxMap.OnMoveListener: Lắng nghe các sự kiện của map
>   - OnRouteSelectionChangeListener:
>       - onNewPrimaryRouteSelected: Lắng nghe khi người dùng chọn tuyến đường khác so với tuyến đường hiện tại
>   - LocationListener, RouteListener: Lắng nghe các event khi người dùng di chuyển
>   - OffRouteListener: Lắng nghe khi người dùng đi sai tuyến đường, từ đó tìm tuyến khác theo hướng di chuyển của người dùng

### **IV**. Tìm một tuyến đường
API tìm tuyến đường yêu cầu 2 params là origin và destination, là vị trí hiện tại của người dùng và vị trí đích đến.

Ví dụ:
```java
    Point origin = Point.fromLngLat(106.675884,10.759197);
    Point destination = Point.fromLngLat( 105.577136, 18.932147);
```
Từ hai điểm point và destination này, chúng ta có thể gọi hàm fetchRoute như sau:
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
Sau khi gọi hàm fetchRoute, bạn sẽ nhận được kết quả tại listener như sau:
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
                updateCustomScreenLoading(false);
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
                .milestoneEventListener(milestoneEventListener)
                .directionsRoute(route)
                .onMoveListener(this);
    }
```
Hàm **initNavigationOptions** sẽ được gọi trước khi bắt đầu dẫn đường
```java
    private void launchNavigation() {
        launchNavigationFab.hide();
        navigationView.setVisibility(View.VISIBLE);
        mapboxNavigation.addOffRouteListener(this);
        changeNavigationActionState(true);
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
Hàm **userOffRoute** lắng nghe khi người dùng đi không đúng với lộ trình được trả về, từ đó tìm tuyến đường mới phù hợp hơn với hướng di chuyển hiện tại của người dùng
```java
    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        
    }
```
Hàm **onProgressChange** lắng nghe khi người dùng di chuyển, liên tục cập nhật thông tin về tuyến đường người dùng đang di chuyển, khoảng cách còn lại,... 
```java
    @Override
    public void onArrival() {
        if(isArrived) return;
        changeNavigationActionState(false);
        isArrived=true;
    }
```
Hàm **onArrival** lắng nghe khi người dùng đã di chuyển tới đích **(destination)**, từ đó có thể tự tạo thông báo hoặc alert cho người dùng.


# **Custom UI (Tuỳ chỉnh giao diện)**
```java
    navigationView.initViewConfig(true);
``` 
Tại hàm onCreate, thêm đoạn code phía trên để ẩn đi toàn bộ giao diện mặc định, chỉ để lại phần bản đồ và phần dẫn đường. Các thông tin của chuyến đi sẽ được cung cấp đầy đủ.



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
- Các thông tin về đường đi, khoảng cách,... được trả về tại hàm [_**onProgressChange**_](INSTALL.md#L233) 