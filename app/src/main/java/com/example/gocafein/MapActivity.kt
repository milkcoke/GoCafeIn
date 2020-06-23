package com.example.gocafein

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_end.*


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

class EndActivity : AppCompatActivity(), OnMapReadyCallback {
//    지도 Interface를 다루는 naverMap Class



    private lateinit var locationSource : FusedLocationSource
    lateinit var fusedLocationClient : FusedLocationProviderClient
    lateinit var currentLocation : LatLng
    lateinit var locationRequest : LocationRequest
    lateinit var locationCallBack : LocationCallback
//    NaverMap 생성시 바로 호출되는 콜백 메소드
//    지도 Option Handling하는데 사용

//    이미 초기화면에서 위치권한 받았다고 가정.
    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient?.lastLocation?.addOnSuccessListener {
            currentLocation = LatLng(it.latitude, it.longitude)
        }
    }
    fun startLocationUpdate() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        locationCallBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
//                null 이면 그냥 return
                locationResult ?: return
                for(updatedLocation in locationResult.locations) {
                    currentLocation = LatLng(updatedLocation.latitude, updatedLocation.longitude)
                }
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
//    locationOverlay: 현재 위치를 나타내는 Overlay
        val locationOverlay = naverMap.locationOverlay
        val uiSettings = naverMap.uiSettings
        val marker = Marker()
        val cameraUpdate = CameraUpdate.scrollTo(currentLocation)
        marker.position = currentLocation
        marker.map = naverMap

        naverMap.locationSource = locationSource
        naverMap.addOnLocationChangeListener { location ->
            Toast.makeText(this, "${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
        }


//    지도 객체에 종속된 객체로, 지도에 단 하나만 존재함.
//    보여주고 숨기는 것은 오로지 isVisible 로만 가능.
        locationOverlay.isVisible = true
        locationOverlay.position = currentLocation
        naverMap.setLocationSource(object : LocationSource {
//            LocationSource의 메소드는
//            NaverMap 객체가 알아서 호출하므로 개발자의 수동호출을 금함.
            override fun deactivate() {
            }

            override fun activate(p0: LocationSource.OnLocationChangedListener) {
            }
        })
//    현재 위치 추적모드 ON
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
//        아..현자 씨게오네 아 ㄹㅇ루다가 ㅋㅋㅋㅋㅋㅋㅋ
        naverMap.moveCamera(cameraUpdate)
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
        startLocationUpdate()
        getUserLocation()
        mapInit()

        // Set up the user interaction to manually show or hide the system UI.
        user_name_text.setOnClickListener { toggle() }
        user_email_text.setOnClickListener { toggle() }

//        URL Scheme 처리 (버튼 텍스트로 네이버지도 자동검색)
        search_button.setOnClickListener {

            val buttonText = search_button.text.toString()
            val url = "nmap://search?query=$buttonText&appname=com.example.gocafein"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)

            val list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
//            지도 Application이 아무것도 없을 경우  Playstore로 이동.
            if (list == null || list.isEmpty()) {
//                Context 무엇으로 할지?
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.nhn.android.nmap")
                    )
                )
            } else {
                startActivity(intent)
            }
        }
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

    private fun userInfoInit() {
        user_name_text.text = intent.getStringExtra("userName")
        user_email_text.text = intent.getStringExtra("userEmail")
    }

    private fun mapInit() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.naver_map_fragment) as MapFragment
        if (mapFragment == null) {
            val mapFragment = MapFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.naver_map_fragment, mapFragment).commit()
        }
        mapFragment.getMapAsync(this)


        //    Permission 처리를 위해 별도 프래그먼트 권한 요청 생성
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)



    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            //        권한이 거부될 경우 return
            if (!locationSource.isActivated) Toast.makeText(this, "거부됨", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "조아요!", Toast.LENGTH_SHORT).show()
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}
