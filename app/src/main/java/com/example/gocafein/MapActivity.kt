package com.example.gocafein

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_end.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL


private const val LOCATION_PERMISSION_REQUEST_CODE = 1000


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
//    지도 Interface를 다루는 naverMap Class

    private lateinit var locationSource : FusedLocationSource
    lateinit var currentNaverMap : NaverMap
    lateinit var fusedLocationClient : FusedLocationProviderClient
    lateinit var currentLocation : LatLng
    lateinit var locationRequest : LocationRequest
    lateinit var locationCallBack : LocationCallback
    lateinit var currentMarker : Marker
    lateinit var locationOverlay : Overlay
//    도로명 주소중 '동/읍'만 keep해둘 필요가있음.
    lateinit var currentlocationDistrict : String

    var nearCafeArray = ArrayList<Cafe>()


    private inner class RequestSearchTask(context: MapActivity) : AsyncTask<URL?, Unit, String>() {
        //        Background memory 누수를 막기위해 (Garbage Collector 대상 Reference 유지를 위한 레퍼런스)
        val activityReference = WeakReference(context)

        override fun onPreExecute() {

        }
        //            This step is used to perform background computation that can take a long time.
        override fun doInBackground(vararg params: URL?): String? {
            var result = ""
            val inputStream : InputStream
            var bufferedInputStream : BufferedInputStream? = null
            var bufferedReader : BufferedReader
            val httpUrlConnection = params[0]!!.openConnection() as HttpURLConnection
            httpUrlConnection.requestMethod = "GET"
            httpUrlConnection.setRequestProperty("X-Naver-Client-Id", getString(R.string.login_api_client_id))
            httpUrlConnection.setRequestProperty("X-Naver-Client-Secret", getString(R.string.login_api_client_secret))
            try {
                inputStream = BufferedInputStream(httpUrlConnection.inputStream)
                bufferedInputStream = BufferedInputStream(inputStream)
                bufferedReader = BufferedReader(InputStreamReader(bufferedInputStream))
                bufferedReader.forEachLine {
                    result += it
                }
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            } finally {
                bufferedInputStream?.close()
            }

            return result
        }

        //            invoked on the UI thread after the background computation finishes.
        override fun onPostExecute(content: String) {

            val activity = activityReference.get()
            val resultLocation = parsingNearCafeInfoJSON(content)
//            currentMarker.position = resultLocation
//            val cameraUpdate = CameraUpdate.scrollTo(resultLocation)
//            currentNaverMap.moveCamera(cameraUpdate)

            Log.i("search", content)
        }

    }

    private inner class RequestReverseGeocodingTask(context: MapActivity) : AsyncTask<URL?, Unit, String>() {
//        Background memory 누수를 막기위해 (Garbage Collector 대상 Reference 유지를 위한 레퍼런스)
        val activityReference = WeakReference(context)

        override fun onPreExecute() {

        }
        //            This step is used to perform background computation that can take a long time.
        override fun doInBackground(vararg params: URL?): String? {
            var result = ""
            val inputStream : InputStream
            var bufferedInputStream : BufferedInputStream? = null
            var bufferedReader : BufferedReader
            val httpUrlConnection = params[0]!!.openConnection() as HttpURLConnection
            httpUrlConnection.requestMethod = "GET"
            httpUrlConnection.setRequestProperty("X-NCP-APIGW-API-KEY-ID", getString(R.string.naver_platform_api_client_id))
            httpUrlConnection.setRequestProperty("X-NCP-APIGW-API-KEY", getString(R.string.naver_platform_api_client_secret))
            try {
                 inputStream = BufferedInputStream(httpUrlConnection.inputStream)
                bufferedInputStream = BufferedInputStream(inputStream)
                bufferedReader = BufferedReader(InputStreamReader(bufferedInputStream))
                bufferedReader.forEachLine {
                    result += it
                }
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            } finally {
                bufferedInputStream?.close()
            }

            return result
        }

        //            invoked on the UI thread after the background computation finishes.
        override fun onPostExecute(content: String) {

            val activity = activityReference.get()
            val textAddress = parsingCurrentAddressJSON(content)
            activity?.current_address_textView?.text = textAddress
            Log.i("address", textAddress)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_end)

        userInfoInit()
        mapInit()
        // Set up the user interaction to manually show or hide the system UI.
        user_name_text.setOnClickListener {

        }



        search_cafe_button.setOnClickListener {
            val requestUrl = URL("https://openapi.naver.com/v1/search/local.json" +
//                    query= 검색문자열
//                    UTF-8 Encoding space character == %20
                    "?query=$currentlocationDistrict%20카페" +
//                    display: 검색 결과 출력 건수
                    "&display=15" +
//                    start: 검색 시작 위치 (MAX: 1000)
                    "&start=1" +
//                    random sort : 유사도순
                    "&sort=random")

            try {
                RequestSearchTask(this@MapActivity).execute(requestUrl)
            } catch (illegalEx : IllegalStateException) {
                illegalEx.printStackTrace()
            }
        }

        current_address_textView.setOnClickListener {
////            coords= 입력 좌표 (위도, 경도)
////            sourcecrs : 입력 좌표계 코드 (default: 위경도 좌표계(epsg:4326) == Google 좌표계)
////          val urlString = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords=${currentLocation.latitude},${currentLocation.longitude}" +
////            Naver Reverse GeoCoding 서비스의 치명적 약점은 위경도가 살짝만 달라져도 , 행정구역 지도 검색이 되지 않는다.
//
////            longitude, latitude 순서에 유의.. 아 이걸로 몇시간을 버린거야..
//            val urlString = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords=${currentLocation.longitude},${currentLocation.latitude}" +
////                    orders: 변환 작업 이름,
////                    legalcode: 좌표 -> 법정동 , admcode : 좌표->행정동, roadaddr: 좌표 -> 도로명주소 (최신)
//                    "&sourcecrs=epsg:4326" +
//                    "&orders=admcode" +
////                    output : json or xml
//                    "&output=json"
//
////            val url = URL(urlString)
//            val url = URL(urlString)
//            val task = RequestReverseGeocodingTask(this@MapActivity)
//            task.execute(url)
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




    override fun onStop() {
        super.onStop()
        if (fusedLocationClient != null) {
//
            fusedLocationClient.removeLocationUpdates(locationCallBack)
        }
    }

    //    이미 초기화면에서 위치권한 받았다고 가정.
    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
    //            마지막으로 알려진 위치 가져오기
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient?.lastLocation?.addOnSuccessListener {
            currentLocation = LatLng(it.latitude, it.longitude)

// 해당 위치를 마커로 표시
            currentMarker.position = currentLocation
            currentMarker.map = currentNaverMap

//            마지막 위치에 오버레이 표시
            //    지도 객체에 종속된 객체로, 지도에 단 하나만 존재함.
            //    보여주고 숨기는 것은 오로지 isVisible 로만 가능.
            locationOverlay.isVisible = true
            (locationOverlay as LocationOverlay).position = currentLocation

//            마지막 위치로 카메라 이동
            val cameraUpdate = CameraUpdate.scrollTo(currentLocation)
            currentNaverMap.moveCamera(cameraUpdate)

            //            coords= 입력 좌표 (위도, 경도)
//            sourcecrs : 입력 좌표계 코드 (default: 위경도 좌표계(epsg:4326) == Google 좌표계)
//          val urlString = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords=${currentLocation.latitude},${currentLocation.longitude}" +
//            Naver Reverse GeoCoding 서비스의 치명적 약점은 위경도가 살짝만 달라져도 , 행정구역 지도 검색이 되지 않는다.

//            longitude, latitude 순서에 유의.. 아 이걸로 몇시간을 버린거야..
            val urlString = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords=${currentLocation.longitude},${currentLocation.latitude}" +
//                    orders: 변환 작업 이름,
//                    legalcode: 좌표 -> 법정동 , admcode : 좌표->행정동, roadaddr: 좌표 -> 도로명주소 (최신)
                    "&sourcecrs=epsg:4326" +
                    "&orders=admcode" +
//                    output : json or xml
                    "&output=json"

//            val url = URL(urlString)
            val url = URL(urlString)
            val task = RequestReverseGeocodingTask(this@MapActivity)
            task.execute(url)

        }
    }

    fun parsingCurrentAddressJSON (content: String) : String{
        var resultAddress = ""
        try {
            val json = JSONObject(content)
            Log.i("json", json.toString())
            val addressRegion = json.getJSONArray("results").getJSONObject(0).getJSONObject("region")
            Log.i("jsonAddress", addressRegion.toString())
            val city = addressRegion.getJSONObject("area1").getString("name")
            val county = addressRegion.getJSONObject("area2").getString("name")
            val district = addressRegion.getJSONObject("area3").getString("name")
            currentlocationDistrict = district
            resultAddress = "$city $county $district"
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return resultAddress
    }


    fun parsingNearCafeInfoJSON (content: String) : Unit {

        try {
            val json = JSONObject(content)
            val cafeListArray = json.getJSONArray("items")
//            var tempMapList =  ArrayList<Map<String, String>>()
            for (i in 0 until cafeListArray.length()) {
                val cafeInfo = cafeListArray.getJSONObject(i)
                val cafeLocation = katechToGEO(cafeInfo.getString("mapx").toDouble(), cafeInfo.getString("mapy").toDouble())
                nearCafeArray.add(Cafe(cafeInfo.getString("title"), cafeInfo.getString("link"), cafeInfo.getString("roadAddress"), cafeLocation))
            }


        } catch (jsonExc: JSONException) {
            jsonExc.printStackTrace()
        }
        for(i in nearCafeArray) {
            Log.i("cafe", i.name)
        }
    }

    fun katechToGEO(mapX : Double, mapY : Double) : LatLng {
// mapX, mapY좌표는 카텍 좌표계 (TM128)
//           구글맵의 위도/경도를 사용하는 WGS84로 좌표 변경이 필요.
//            천호 977 카페의 mapx , mapy 대입 테스트
        val naverKatechLocationPoint = GeoTransPoint(322546.toDouble(), 548996.toDouble())
        val transLocation = TransLocation()
        val geoTranslatedPosition: GeoTransPoint = transLocation.convert(transLocation.KATEC, transLocation.GEO, naverKatechLocationPoint)

        val latitude : Double = geoTranslatedPosition.y
        val longitude : Double = geoTranslatedPosition.x

//            resultAddress = "${wgs84Point.x * 10} , ${wgs84Point.y * 100}"
        val resultLatLng = LatLng(latitude, longitude)
        Log.i("Location", "$resultLatLng")
        return resultLatLng
    }

//    API Level 10 이상에서는 Background Location Update X
//    allow update location while using this app
//    실시간 위치 업데이트 구현 (굳이 이앱에서 사용할 일은 없을듯?)
    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
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
                    Log.i("Location", "update 되는중!")
//                    Location 위치가 업데이트 될때마다 카메라 이동
                    val cameraUpdate = CameraUpdate.scrollTo(currentLocation)
//                    Location 위치 업데이트 할 때마다 마커 재생성
                    currentNaverMap.moveCamera(cameraUpdate)
                }
            }
        }
//    권한은 이미 앞서서 허용했다고 가정.
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper())
    }

//    NaverMap 생성시 바로 호출되는 콜백 메소드
//    지도 Option Handling 하는데 사용

    override fun onMapReady(naverMap: NaverMap) {
//    locationOverlay: 현재 위치를 나타내는 Overlay
        currentNaverMap = naverMap
        locationOverlay = naverMap.locationOverlay
        currentMarker = Marker()
        val uiSettings = naverMap.uiSettings

//        Map이 준비된 후에 마지막 (최근) 사용자 위치를 받아옴
        getUserLocation()
//      그 후에 유저 위치 업데이트 요청 (일단 중단)
        startLocationUpdate()


        naverMap.locationSource = locationSource
        naverMap.addOnLocationChangeListener { location ->
            Toast.makeText(this, "${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
        }




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
