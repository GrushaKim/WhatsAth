package com.example.mywhatsath.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ActivityMapsScreenBinding
import com.example.mywhatsath.utils.CustomInfoWindowForGoogleMap
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.io.IOException
import java.util.*

class MapsScreenActivity : AppCompatActivity(),
    OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private lateinit var binding: ActivityMapsScreenBinding
    private var mMap: GoogleMap? = null
    internal lateinit var mLastLocation: Location
    internal var mCurrLocMarker: Marker? = null
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal lateinit var mLocationRequest: com.google.android.gms.location.LocationRequest

    companion object{
        const val TAG = "MAPS_SCREEN_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // init Places w api key
        val apiKey = getString(R.string.google_maps_key)
       if(!Places.isInitialized()){
            Places.initialize(applicationContext,apiKey)
        }

        val placesClient: PlacesClient = Places.createClient(this)

        // init autocomplete fragment
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocompleteFragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(Arrays.asList(
            Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.RATING))

        autocompleteFragment.setOnPlaceSelectedListener(object: PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                Log.d(TAG, "onPlaceSelected: ${place.id} / ${place.name} / ${place.address} / ${place.latLng} / ${place.rating}")
                val locationName: String? = place.name
                val locationAddr: String? = place.address
                val locationRating: Double? = place.rating

                searchLocation(locationName, locationAddr, locationRating)
            }

            override fun onError(status: Status) {
                Log.d(TAG, "onError: status is $status ")
            }
        })

       val mapFragment = supportFragmentManager.findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
        markerOptions.title("Current Position")
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

    fun searchLocation(location: String?, locationAddr: String?, locationRating: Double?) {

        var addressList: List<Address>? = null
        
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
            val latLng = LatLng(address.latitude, address.longitude)
            // marker options with snippet adapter
            mMap!!.addMarker(MarkerOptions()
                .position(latLng)
                .title(location)
                .snippet(" Address: $locationAddr \n Rating: $locationRating")
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
    }
}