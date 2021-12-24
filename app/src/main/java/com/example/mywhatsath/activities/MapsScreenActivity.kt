package com.example.mywhatsath.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ActivityMapsScreenBinding
import com.example.mywhatsath.utils.CustomInfoWindowForGoogleMap
import com.example.mywhatsath.utils.retrofit.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*

class MapsScreenActivity : AppCompatActivity(),
    OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private lateinit var binding: ActivityMapsScreenBinding
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var retrofit: Retrofit
    private lateinit var suppleService: RetrofitService

    private var mMap: GoogleMap? = null
    internal lateinit var mLastLocation: Location
    internal var mCurrLocMarker: Marker? = null
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var currentPlace: MyPlace?= null
    internal lateinit var mLocationRequest: com.google.android.gms.location.LocationRequest

    companion object{
        const val TAG = "MAPS_SCREEN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init main toolbar
        setSupportActionBar(binding.mainToolbar)
        binding.mainToolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        // init retrofit
        retrofit = RetrofitClient.getInstance(Constants.GOOGLE_API_URL)
        suppleService = retrofit.create(RetrofitService::class.java)

        // init Places w api key
       if(!Places.isInitialized()){
            Places.initialize(applicationContext, AuthKey.MAPS_API_KEY)
        }

        val placesClient: PlacesClient = Places.createClient(this)

        // init autocomplete fragment
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocompleteFragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(
            Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.RATING)
        )

        autocompleteFragment.setOnPlaceSelectedListener(object: PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                val lat = place.latLng.latitude
                val lng = place.latLng.longitude
                nearByPlace(lat, lng)
                Log.d(TAG, "onPlaceSelected: $lat and $lng")
            }
            override fun onError(status: Status) {
                Log.d(TAG, "onError: status is $status ")
            }
        })

       val mapFragment = supportFragmentManager.findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    // inflate menu to toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // set menu functions
    override fun onOptionsItemSelected(item: MenuItem) = when(item?.itemId) {
        R.id.homeBtn -> {
            startActivity(Intent(this@MapsScreenActivity, DashboardUserActivity::class.java))
            true
        }
        R.id.logoutBtn -> {
            fbAuth.signOut()
            checkUser()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            ){
                buildGoogleApiClient()
                mMap!!.isMyLocationEnabled = true
            }
        }else{
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = true
        }
    }

   protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        mGoogleApiClient!!.connect()
    }

   override fun onLocationChanged(location: Location) {
        mLastLocation = location
        if(mCurrLocMarker != null){
            mCurrLocMarker!!.remove()
        }
        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("You are here!")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mCurrLocMarker = mMap!!.addMarker(markerOptions)

        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap!!.moveCamera(CameraUpdateFactory.zoomTo(20f))

        if(mGoogleApiClient != null){
            LocationServices.getFusedLocationProviderClient(this)
        }
    }

   override fun onConnected(p0: Bundle?) {
        mLocationRequest = com.google.android.gms.location.LocationRequest()

            mLocationRequest.interval = 1000
            mLocationRequest.fastestInterval = 5000
            mLocationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            if(ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            ){
                LocationServices.getFusedLocationProviderClient(this)
            }
        }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    // to be updated
    /*private fun searchLocation(place: Place) {

        var addressList: List<Address>? = null
        var location = place.name
        var placeId = place.id
        var addr = place.address
        var latitude = place.latLng.latitude
        var longitude = place.latLng.longitude
        var rating = place.rating

        if(location == null || location == ""){
            Toast.makeText(this, "provide correct location", Toast.LENGTH_SHORT).show()
        }else{
            val geoCoder = Geocoder(this)
            try{
                addressList = geoCoder.getFromLocationName(location, 1)
            }catch(e: IOException){
                Log.d(TAG, "searchLocation: ${e.message}")
            }

            val address = addressList!![0]
            val latLng = LatLng(latitude, longitude)
            // marker options with snippet adapter
            mMap!!.addMarker(MarkerOptions()
                .position(latLng)
                .title(location)
                .snippet(" Address: $addr\n Rating: $rating")
            )
            mMap!!.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this))
            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))

            // check if the user want to share the marked place
            mMap!!.setOnInfoWindowClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Share this place?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->

                    }
                    .setNegativeButton("No") { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()

            }
        }
    }*/

    private fun nearByPlace(latitude: Double, longitude: Double) {
        //clear all markers
        mMap?.clear()
        //build url request
        val url = getUrl(latitude, longitude)
        
        suppleService.getNearbyGyms(url)
            .enqueue(object: Callback<MyPlace>{
                override fun onResponse(call: Call<MyPlace>, response: Response<MyPlace>) {
                    currentPlace = response.body()

                    if (response!!.isSuccessful) {

                        for (i in 0 until response!!.body()!!.results!!.size) {
                            val googlePlace = response.body()!!.results!![i]
                            val lat = googlePlace.geometry!!.location!!.lat
                            val lng = googlePlace.geometry!!.location!!.lng
                            val placeName = googlePlace.name
                            val placeRating = googlePlace.rating
                            val placeVicinity = googlePlace.vicinity
                            val latLng = LatLng(lat, lng)

                            val placeId = googlePlace.place_id
                            val detailAddr = "${Constants.GOOGLE_PLACE_URL}$placeId"

                            Log.d(TAG, "onResponse: nearByPlace info $placeName / $latLng / $placeId")

                            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(13f))
                            mMap!!.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(placeName)
                                    .snippet(" Rating: $placeRating \n Vicinity: $placeVicinity \n")
                            )
                            mMap!!.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this@MapsScreenActivity))
                            mMap!!.setOnInfoWindowLongClickListener {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(detailAddr)))
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<MyPlace>, t: Throwable) {
                    Log.d(TAG, "onFailure: ${t.message}")
                }
            })
    }

    // to render nearby gyms according to the conditions
    private fun getUrl(latitude: Any, longitude: Any): String {
        val googlePlaceUrl = StringBuilder(
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=1000")
        googlePlaceUrl.append("&type=gym")
        googlePlaceUrl.append("&key=${AuthKey.MAPS_API_KEY}")

        Log.d(TAG, "getUrl: $googlePlaceUrl")

        return googlePlaceUrl.toString()
    }

    private fun checkUser() {
        // get current user
        val fbUser = fbAuth.currentUser
        if (fbUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

}