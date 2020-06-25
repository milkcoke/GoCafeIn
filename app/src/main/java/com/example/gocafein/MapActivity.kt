package com.example.gocafein

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_end.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

class EndActivity : AppCompatActivity(), OnMapReadyCallback {
//    지도 Interface를 다루는 naverMap Class



    private lateinit var locationSource : FusedLocationSource
    lateinit var fusedLocationClient : FusedLocationProviderClient
    lateinit var currentLocation : LatLng
    lateinit var locationRequest : LocationRequest
    lateinit var locationCallBack : LocationCallback


//    도로명 주소
    lateinit var locationText : String
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_end)

        userInfoInit()
        startLocationUpdate()
        getUserLocation()
        mapInit()

        // Set up the user interaction to manually show or hide the system UI.
        user_name_text.setOnClickListener {

        }

//JSON Parsing Web Data 다시 해야겠다 + AsyncTask
//        RunAPI Task ,./

        // Offline API 요청은 Network 를 사용하기 때문에 AsyncTask 사용.
        class RequestApiTask : AsyncTask<URL?, Unit, String>() {
            override fun onPreExecute() {
            }
            override fun doInBackground(vararg params: URL?): String? {
                var result = ""
                val stream = params[0]?.openStream()
                val read = BufferedReader(InputStreamReader(stream, "UTF-8"))
                result = read.readLine()
                return result
            }
            override fun onPostExecute(content: String) {
//                val resultUserInfoJSON = JSONObject(content).getJSONObject("response")
//                val userEmail = resultUserInfoJSON.getString("email")
//                val userName = resultUserInfoJSON.getString("name")
                current_address_textView.text = content
                Log.i("address", content)
//                startActivity(userIntent)
            }

        }


        current_address_textView.setOnClickListener {
//            coords= 입력 좌표 (위도, 경도)
//            sourcecrs : 입력 좌표계 코드 (default: 위경도 좌표계(epsg:4326) == Google 좌표계)
            val urlString = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=${currentLocation.latitude},${currentLocation.longitude}" +
//                    orders: 변환 작업 이름,
//                    legalcode: 좌표 -> 법정동 , admcode : 좌표->행정동, addr: 좌표 -> 지번주소
                    "&sourcecrs=epsg:4326&" +
                    "orders=roadaddr" +
//                    output : json or xml
                    "&output=json" +
            "X-NCP-APIGW-API-KEY-ID:${getString(R.string.naver_platform_api_client_id)}" +
            "X-NCP-APIGW-API-KEY:${getString(R.string.naver_platform_api_client_secret)}"
            val url = URL(urlString)
            val task = RequestApiTask()
            task.execute(url)
        }

//        https://docs.ncloud.com/ko/naveropenapi_v3/maps/url-scheme/url-scheme.html
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


    }


    private fun userInfoInit() {
        user_name_text.text = "안녕하세요 " + intent.getStringExtra("userName") + "님!"
//        user_email_text.text = intent.getStringExtra("userEmail")
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
// 네이버 Local Info API -> Category '카페'인 것만 검색 =>카텍 좌표계 리턴-> 좌표계 변환-> naver Map 에 표시

}
