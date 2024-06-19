package vn.vietmap.car_app_service

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class PlacesSession: Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return MainScreen(carContext)
    }
}