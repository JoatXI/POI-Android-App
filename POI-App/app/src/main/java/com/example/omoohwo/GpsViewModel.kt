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
        set(newPoi) {
            field = newPoi
            poiListLiveData.value = newPoi
        }
    var poiListLiveData = MutableLiveData<MutableList<Poi>>()

    fun addPoi(poi: Poi) {
        poiList.add(poi)
        // update the live data to ensure that it's tracking the new live list
        poiListLiveData.value = poiList
    }
}