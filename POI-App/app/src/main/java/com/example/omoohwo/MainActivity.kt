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
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class LatLon(var lat: Double, var lon: Double)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity(), LocationListener {
    lateinit var db : PoiDatabase
    val latLonViewModel : GpsViewModel by viewModels()
    val poiViewModel : GpsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        db = PoiDatabase.getDatabase(application)

        super.onCreate(savedInstanceState)

        setContent {
            OmoohwoTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val navController = rememberNavController()
                    val coroutineScope = rememberCoroutineScope()

                    MainNavBar(drawerState, navController, coroutineScope)
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

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { // this function is outdated for API 29(Android 10) and above hence, it will be ignored.
    // Nonetheless, it is required even though it does nothing because when excluded the application crashes on device API lower than 29 (Android 9 or older).
        // doNothing
    }

    @Composable
    fun MainNavBar(drawerState: DrawerState, navController: NavController, coroutineScope: CoroutineScope) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    title = {
                        Text("Points Of Interest App")
                    },
                    actions = {
                        IconButton(onClick = {
                            Log.d("madassignment", "clicked menu icon")
                            coroutineScope.launch {
                                if(drawerState.isClosed) {
                                    Log.d("madassignment", "drawer is closed, opening it")
                                    drawerState.open()
                                } else {
                                    Log.d("madassignment", "drawer is open, closing it")
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(imageVector = Icons.Filled.Menu, "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("addPoi") },
                    content = {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add POI")
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = true,
                        onClick = { navController.navigate("homeScreen") }
                    )
                    NavigationBarItem(
                        icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = "Add POI") },
                        label = { Text("Add POI") },
                        selected = false,
                        onClick = { navController.navigate("addPoi") }
                    )
                }
            }
        ) { innerPadding ->
            // NavigationDrawer and your NavHost
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(modifier = Modifier.padding(innerPadding)) {
                        NavigationDrawerItem(
                            selected = false,
                            label = { Text("Add POI") },
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                                navController.navigate("addPoi")
                            }
                        )
                        NavigationDrawerItem(
                            selected = false,
                            label = { Text("Save All POIs") },
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                                navController.navigate("savePoi")
                            }
                        )
                    }
                }
            ) {
                NavHost(navController= navController as NavHostController, startDestination="homeScreen") {
                    composable("homeScreen") {
                        HomeScreenComposable(innerPadding, settingsMenu ={
                            navController.navigate("settingsScreen")
                        })
                    }
                    composable("settingsScreen") {
                        SettingsComposable(innerPadding, onCallBack = {
                            navController.navigate("homeScreen")
                        })
                    }
                    composable("addPoi") {
                        AddPoi(innerPadding, homeMenu = {
                            navController.navigate("homeScreen")
                        })
                    }
                    composable("savePoi") {
                        SavePoi(innerPadding, homeMenu = {
                            navController.navigate("homeScreen")
                        })
                    }
                }
            }
        }
    }

    @Composable
    fun HomeScreenComposable(innerPaddingValues: PaddingValues, settingsMenu: () -> Unit) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            var latLon: LatLon by remember { mutableStateOf(LatLon(51.05, -0.72)) }
            var poi by remember { mutableStateOf(listOf<Poi>()) }

            poiViewModel.poiListLiveData.observe(this) {
                Log.d("addpoi", "live data updated")
                        poi = it }
            latLonViewModel.latLonLiveData.observe(this) { latLon = it } // "it" is the LatLon being observed and used to update user's location to the UI
            BoxWithConstraints(modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues)) {
                val mapHeight = this.maxHeight - 50.dp

                MapComposable(poi, GeoPoint(latLon.lat, latLon.lon), modifier = Modifier
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

    @Composable
    fun AddPoi(innerPaddingValues: PaddingValues, homeMenu: () -> Unit) {
        Column(modifier = Modifier.fillMaxSize()) {

            val lat = latLonViewModel.latLon.lat
            val  lon = latLonViewModel.latLon.lon

            var poiName by remember { mutableStateOf("") }
            var type by remember { mutableStateOf("") }
            var desc by remember { mutableStateOf("") }

            BoxWithConstraints(modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues)) {

                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)) {
                        TextField(value = poiName, onValueChange = { poiName = it })
                        TextField(value = type, onValueChange = { type = it })
                        TextField(value = desc, onValueChange = { desc = it })

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val newPoi = Poi(name = poiName, type = type, description = desc, latitude = lat, longitude = lon)
                            Button(onClick = {
                                Log.d("addpoi", "geopoint: lat: ${lat}, lon: ${lon}")
                                poiViewModel.addPoi(newPoi); homeMenu() }) {
                                Text("Add Poi")
                            }
                            Button(onClick = { homeMenu() }) {
                                Text("Back")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SavePoi(innerPaddingValues: PaddingValues, homeMenu: () -> Unit) {
        BoxWithConstraints(modifier = Modifier
            .fillMaxSize()
            .padding(innerPaddingValues)) {
            val mapHeight = this.maxHeight - 50.dp

            Surface(modifier = Modifier
                .fillMaxSize()
                .height(mapHeight)
                .border(1.dp, Color.Black)) {

                var id by remember { mutableStateOf("") }
                val currList = poiViewModel.getPois()

                Column(modifier = Modifier.fillMaxSize().padding(2.dp)) {
                    Text("Select the save button to save all POIs")
                    Row() {
                        Button(onClick = {
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    currList.forEach {
                                        id = db.poiDao().insert(it).toString()
                                    }
                                }
                            }
                        }) {
                            Text("Save All POIs")
                        }
                        Text("POIs saved successfully: $id")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = { homeMenu() }) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapComposable(poi: List<Poi>, geoPoint: GeoPoint, modifier: Modifier) {
    AndroidView(modifier = Modifier, factory = { ctx  -> Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        val map1 = MapView(ctx).apply {
            setMultiTouchControls(true)
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(14.0)
        }
        val marker = Marker(map1)
        poi.forEach {
            marker.apply {
                position = GeoPoint(it.latitude, it.longitude)
                title = "${it.name}, ${it.description}"
            }
            map1.overlays.add(marker)
        }
        map1
    }, update = { view -> view.controller.setCenter(geoPoint) })
}

@Composable
fun SettingsComposable(innerPaddingValues: PaddingValues, onCallBack: () -> Unit) {

    BoxWithConstraints(modifier = Modifier
        .fillMaxSize()
        .padding(innerPaddingValues)) {
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
                Button(modifier = Modifier.padding(end = 4.dp), onClick = { lat.toDouble(); lon.toDouble() }) {
                    Text("Update")
                }
                Button(onClick = { onCallBack() }) {
                    Text("Back")
                }
            }
        }
    }
}