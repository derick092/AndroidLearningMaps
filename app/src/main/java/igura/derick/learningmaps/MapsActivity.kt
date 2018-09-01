package igura.derick.learningmaps

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.content.Intent
import android.content.DialogInterface
import android.location.LocationManager
import android.content.Context.LOCATION_SERVICE
import android.provider.Settings
import android.support.v7.app.AlertDialog


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var lastKnowLocation: Location? = null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private var markerIam: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkGPSStatus()

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 5)
        }

        getLastLocation()
    }

     fun checkGPSStatus() {
        var locationManager: LocationManager? = null
        var gps_enabled = false
        var network_enabled = false
        if (locationManager == null) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }

        if (!gps_enabled && !network_enabled) {
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("GPS not enabled")
            dialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                //this will navigate user to the device location settings screen
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            })
            val alert = dialog.create()
            alert.show()
        }
    }

    fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 2000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdate(){
        createLocationRequest()
        var mLocationCallBack = object: LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                if(locationResult == null){
                    return
                }

//                for(location in locationResult.locations){
//
//                }
                lastKnowLocation = locationResult.lastLocation
                centerMapIfPossible(false)
                super.onLocationResult(locationResult)
            }
        }

        createLocationRequest()
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, null)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                lastKnowLocation = it
                centerMapIfPossible(true)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun centerMapIfPossible(startUpdates: Boolean){
        if(lastKnowLocation != null && mMap != null){
            val lastPosition = LatLng(lastKnowLocation!!.latitude, lastKnowLocation!!.longitude)
            mMap.addMarker(MarkerOptions().position(lastPosition).title("Last Position"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 15f))

            if(markerIam == null){
                markerIam = mMap.addMarker(MarkerOptions().position(lastPosition).title("Last Position"))
            }else{
                markerIam!!.position = lastPosition
            }

            if(startUpdates){
                startLocationUpdate()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(grantResults.isNotEmpty() && grantResults.get(0) == PackageManager.PERMISSION_DENIED){
            getLastLocation()
            Toast.makeText(this, "Need Permission",Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, "Thank You",Toast.LENGTH_LONG).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        centerMapIfPossible(true)
        // Add a marker in Sydney and move the camera


        val arena = LatLng(-29.974045, -51.194916)
        val beiraRio = LatLng(-30.064863, -51.237317)

        val arenaOpt = CircleOptions()
        arenaOpt.center(arena)
        arenaOpt.radius(100.0)
        arenaOpt.strokeColor(Color.BLACK)
        arenaOpt.strokeWidth(2.0f)
        arenaOpt.fillColor(Color.argb(127, 0, 0, 255))

        val beiraRioOpt = CircleOptions()
        beiraRioOpt.center(beiraRio)
        beiraRioOpt.radius(100.0)
        beiraRioOpt.strokeColor(Color.BLACK)
        beiraRioOpt.strokeWidth(2.0f)
        beiraRioOpt.fillColor(Color.argb(127, 255, 0, 0))

        mMap.addCircle(arenaOpt)
        mMap.addCircle(beiraRioOpt)

        val steps: List<LatLng> = PolyUtil.decode("vd}uDbjnwHbAx@h@Np@Db@Ep@O?LBl@JRPNJDPj@^r@d@z@`@`@x@t@v@d@v@VjAXvCt@fCd@vA`@bDt@rLhC~D~@dKtBnBf@lAb@XDv@@|Cr@lCl@j@Hn@@d@Ir@\\?JBNFLHJD?V@F?TCJCDGzAMp@^`EhB`DfAj@d@`AVxDx@tD|@vGpAtAXnCj@X@lALrBb@|Ch@dALzAPd@LT?^@z@@NEpE|@dEfADr@Jd@TVt@\\JR?NKh@C`@BRNRTLd@JdBRnBNl@Hf@?pHhB~C~@`EvAb@R|FhC~UdKv[bN~GrC|ErBn@Zv@l@hB`ClAnBT`@JFh@x@dBpDl@z@r@v@l@l@bAzA^x@nApDxCbI~C~HzDzJf@xBVdCHvADj@N|@Np@`@rAzBtFdBxEnFhNzBxFxAxDrBtFb@v@h@b@bJzFzA|@dCdAnFbBdCn@v@Pd@GJBXBZAdB@fOQz@Cp@IfA[hAk@lAgAl@}@f@kAZmAReBp@yQVsGTwEp@}AXc@r@g@h@U|@Oj@?xIf@rJ`@|@DtBHtEKnN}@nAGt@EnB?p@B|CTrEj@lFlAxDrApBz@dBz@tCfBvC|BvApAzAhB`C~BpAdAJF`ABd@m@HS")

        val polylineOptions = PolylineOptions()
        polylineOptions.addAll(steps)
        polylineOptions.color(Color.BLACK)
        polylineOptions.width(3f)

        mMap.addPolyline(polylineOptions)

        var polygonOptions = PolygonOptions()
        polygonOptions.add(LatLng(-30.038720,-51.214645))
        polygonOptions.add(LatLng(-30.040206,-51.217048))
        polygonOptions.add(LatLng(-30.034745,-51.220525))
        polygonOptions.add(LatLng(-30.034931,-51.217692))
        polygonOptions.strokeColor(Color.GRAY)
        polygonOptions.strokeWidth(3f)
        polygonOptions.fillColor(Color.argb(127,0,255,0))
        mMap.addPolygon(polygonOptions)

        mMap.setOnMapClickListener {
            var mOpt = MarkerOptions()
            mOpt.position(it)
            mOpt.title("Clicked")
            mOpt.draggable(true)
            mMap.addMarker(mOpt)

        }
    }
}
