package com.example.omoohwo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GpsViewModel : ViewModel() {
    var latLon = LatLon(51.05, -0.72)
        set(newValue) {
            field = newValue
            latLonLiveData.value = newValue
        }
    var latLonLiveData = MutableLiveData<LatLon>()

    var poiList = mutableListOf<Poi>()
}