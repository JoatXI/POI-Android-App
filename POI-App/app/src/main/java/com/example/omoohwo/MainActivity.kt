package com.example.omoohwo

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.omoohwo.ui.theme.OmoohwoTheme
import android.Manifest
import android.location.Location
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class LatLon(var lat: Double, var lon: Double)
class MainActivity : ComponentActivity(), LocationListener {

    val latLonViewModel : GpsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OmoohwoTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(navController=navController, startDestination="homeScreen") {
                        composable("homeScreen") {
                            HomeScreenComposable(settingsMenu ={
                                navController.navigate("settingsScreen")
                            })
                        }
                        composable("settingsScreen") {
                            SettingsComposable(onSettingsUpdated = { lat, lon, openTopo -> })
                        }
                    }
                }
            }
        }
        checkPermissions()
    }

    fun checkPermissions() {
        val requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION

        if(checkSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED) {
            startGPS() // function that starts the GPS after user's permission has been granted.
        } else {
            // doSomethingElse()
            val seekPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> // dialogue box that returns a lambda function after user's interaction
                if(isGranted) {
                    startGPS() // function that starts the GPS after user's permission has been granted.
                } else {
                    // User did not grant GPS permission
                    Toast.makeText(this, "Location Permission Denied!", Toast.LENGTH_LONG).show() // the Toast function produces a pop up box in the UI for a brief moment.
                }
            }
            seekPermission.launch(requiredPermission) // launches the permission request Launcher: this applies when it's the user's first time using the App or the user previously denied the GPS permission
        }
    }

    @SuppressLint("MissingPermission")
    fun startGPS() {
        val mgr = getSystemService(LOCATION_SERVICE) as LocationManager
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    override fun onLocationChanged(location: Location) {
        latLonViewModel.latLon = LatLon(location.latitude, location.longitude)
        Toast.makeText(this, "Latitude: ${location.latitude}, Longitude: ${location.longitude}", Toast.LENGTH_LONG).show() // this displays the new/current GPS location of the user using a Toast pop up
    }

    override fun onProviderEnabled(provider: String) {
        Toast.makeText(this, "GPS enabled", Toast.LENGTH_LONG).show() // handles the situation of a user turning the GPS on
    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(this, "GPS disabled", Toast.LENGTH_LONG).show() // handles the situation of a user turning the GPS off
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { // this function is outdated for API 29(Android 10) and above hence, it will be ignored. Nonetheless, it is required even though it does nothing because when excluded the application crashes on device API lower than 29 (Android 9 or older).
        // doNothing
    }

    @Composable
    fun HomeScreenComposable(settingsMenu: () -> Unit) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            var latLon: LatLon by remember { mutableStateOf(LatLon(51.05, -0.72)) }

            latLonViewModel.latLonLiveData.observe(this) { latLon = it } // "it" is the LatLon being observed and used to update user's location to the UI
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val mapHeight = this.maxHeight - 50.dp

                MapComposable(GeoPoint(latLon.lat, latLon.lon), modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(mapHeight))

                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier
                    .zIndex(2.0f)
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, Color.Black)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Text("Latitude: ${latLon.lat}, Longitude: ${latLon.lon}")
                        Button( onClick ={ settingsMenu() }) { Text("SETTINGS") }
                    }
                }
            }
        }
    }
}

@Composable
fun MapComposable(geoPoint: GeoPoint, modifier: Modifier) {
    AndroidView(modifier = Modifier, factory = { ctx  -> Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        val map1 = MapView(ctx).apply {
            setMultiTouchControls(true)
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(14.0)
        }
        val marker = Marker(map1)
        marker.apply {
            position = GeoPoint(51.05, -0.72)
            title = "Fernhurst, village in West Sussex"
        }
        map1.overlays.add(marker)
        map1
    }, update = { view -> view.controller.setCenter(geoPoint) })
}

@Composable
fun SettingsComposable(onSettingsUpdated: (Double, Double, Boolean) -> Unit) {
    Column {
        var lat by remember { mutableStateOf("") }
        var lon by remember { mutableStateOf("") }

        Row {
            TextField(value = lat, onValueChange = {lat=it}, modifier = Modifier
                .weight(1.0f)
                .zIndex(2.0f)
                .padding(end = 2.dp))
            TextField(value = lon, onValueChange = {lon=it}, modifier = Modifier
                .weight(1.0f)
                .zIndex(2.0f)
                .padding(start = 2.dp))
        }

        var openTopo by remember { mutableStateOf(false) }

        Row(modifier = Modifier.align(Alignment.End)) {
            Switch(modifier = Modifier.padding(end = 240.dp), checked = openTopo, onCheckedChange = { openTopo=it})
            Button(modifier = Modifier.padding(end = 4.dp), onClick = {onSettingsUpdated(lat.toDouble(), lon.toDouble(), openTopo)}) { Text("Update") }
        }
    }
}