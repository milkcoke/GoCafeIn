package com.example.gocafein

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_end.*


private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

//Fused Location 을 사용하기위해 위치정보 제공자 인터페이스 상속
class EndActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
//    지도 Interface를 다루는 naverMap Class

    lateinit var providerClient: FusedLocationProviderClient
    lateinit var googleApiClient: GoogleApiClient


    private lateinit var locationSource : FusedLocationSource

//    Not yet: 실시간 Update
    var currentLatitude : Double = 37.540839
    var currentLongtitude : Double = 127.079311

//    NaverMap 생성시 바로 호출되는 콜백 메소드
//    지도 Option Handling하는데 사용
    override fun onMapReady(naverMap: NaverMap) {
//    locationOverlay: 현재 위치를 나타내는 Overlay
        val locationOverlay = naverMap.locationOverlay
//        val marker = Marker()
        val uiSettings = naverMap.uiSettings
        naverMap.locationSource = locationSource
        naverMap.addOnLocationChangeListener { location ->
            Toast.makeText(this, "${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
        }

//        marker.position = LatLng(currentLatitude, currentLongtitude)
//        marker.map = naverMap


//    지도 객체에 종속된 객체로, 지도에 단 하나만 존재함.
//    보여주고 숨기는 것은 오로지 isVisible 로만 가능.
        locationOverlay.isVisible = true
        locationOverlay.position = LatLng(currentLatitude, currentLongtitude)

        var cameraUpdate = CameraUpdate.scrollTo(LatLng(currentLatitude, currentLongtitude)).animate(CameraAnimation.Linear)
        naverMap.moveCamera(cameraUpdate)

        naverMap.setLocationSource(object : LocationSource {
//            LocationSource의 메소드는
//            NaverMap 객체가 알아서 호출하므로 개발자의 수동호출을 금함.
            override fun deactivate() {
            }

            override fun activate(p0: LocationSource.OnLocationChangedListener) {
            }
        })

    uiSettings.isLocationButtonEnabled = true
//    현재 위치 추적모드 ON
//        naverMap.locationTrackingMode = LocationTrackingMode.Follow
    }






    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        user_name_text.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_end)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true
        userInfoInit()

        mapInit()

        // Set up the user interaction to manually show or hide the system UI.
        user_name_text.setOnClickListener { toggle() }
        user_email_text.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        dummy_button.setOnTouchListener(mDelayHideTouchListener)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        user_name_text.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }

    fun userInfoInit() {
        user_name_text.text = intent.getStringExtra("userName")
        user_email_text.text = intent.getStringExtra("userEmail")
    }

    fun mapInit() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.naver_map_fragment) as MapFragment
        if (mapFragment == null) {
            val mapFragment = MapFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.naver_map_fragment, mapFragment).commit()
        }
        mapFragment.getMapAsync(this)


        //    Permission 처리를 위해 별도 프래그먼트 권한 요청 생성
        locationSource = FusedLocationSource(mapFragment, LOCATION_PERMISSION_REQUEST_CODE)



    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            //        권한이 거부될 경우 return
            if (!locationSource.isActivated) Toast.makeText(this, "거부됨", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "좋아요!", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "좋아요!", Toast.LENGTH_SHORT).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    val locationListener = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                currentLatitude = it.lastLocation.latitude
                currentLongtitude = it.lastLocation.longitude
                Log.i("location", "current Location : $currentLatitude, $currentLongtitude")
            }
        }

//        override fun onLocationAvailability(p0: LocationAvailability?) {
//            super.onLocationAvailability(p0)
//        }
    }

//    위치 정보 제공자가 사용 가능상태가 되면 호출
    override fun onConnected(p0: Bundle?) {
        providerClient.lastLocation.addOnSuccessListener {
            it?.let{
                currentLatitude = it.latitude
                currentLongtitude = it.longitude
                Log.i("location", "current Location : $currentLatitude, $currentLongtitude")
            }
        }

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 3000 //3초
        providerClient.requestLocationUpdates(locationRequest, locationListener, null)
    }
// 위치 사용 불가능 상태가 되면 호출
    override fun onConnectionSuspended(p0: Int) {

    }

//    위치 정보 제공자를 얻지 못했을 때 호출
    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("Connection", "Can't get the location provider")
    }

}
