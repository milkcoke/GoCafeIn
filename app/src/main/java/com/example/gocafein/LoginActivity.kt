package com.example.gocafein

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class LoginActivity : AppCompatActivity(), LocationPermissionDialogActivity.NoticeDialogListener {

    val REQUEST_CODE_LOCATION = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        checkLocationPermission()
    }

    //권한 요청이 필요한 설명 Dialog Activity에서 '확인'버튼을 누르면
    //     권한 요청을 수행함
    override fun onDialogConfirmButtonClick() {
        ActivityCompat.requestPermissions(this@LoginActivity,
            arrayOf(ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),REQUEST_CODE_LOCATION)
    }

    private fun init() {
    //        OAuthLogin 객체(Singleton instance)를 얻고
        val mOAuthLoginModule = OAuthLogin.getInstance()
        val userIntent = Intent(this@LoginActivity, MapActivity::class.java)

        //        LoginModule init method가 여러번 실행돼도
        //        access, refresh token은 유지됨.
        //        삭제를 위해서는 OAuthLogin.logout() or OAuthLogin.logoutAndDeleteToken() 호출 필요
        //        clientId: API 사용 허가 절차상 app을 등록하고 Naver Develper 사이트로부터 부여받은 id
        //        clientName: 네이버 로그인시 앱의 로그인 화면에 표시할 앱 이름.
        //        Mobile web의 로그인시 EndActivity 화면에서 서버에 저장된 앱의 이름이 표시됨.
        mOAuthLoginModule.init(
                this
                ,getString(R.string.login_api_client_id)
                ,getString(R.string.login_api_client_secret)
                ,getString(R.string.login_api_client_name)
        )


// Offline API 요청은 Network 를 사용하기 때문에 AsyncTask 사용.
        class RequestApiTask : AsyncTask<Void?, Void?, String>() {
            override fun onPreExecute() {
            }
            override fun doInBackground(vararg params: Void?): String? {
//                naver user profile 을 JSON 객체 형태로 얻어옴.
                val url = "https://openapi.naver.com/v1/nid/me"
//                mOAuthLoginHandler로부터 토큰을 따로 받아오지 않으므로
//                별도로 토큰을 얻는 메소드 호출 필요.
                val at: String = mOAuthLoginModule.getAccessToken(this@LoginActivity)
//      API 호출   실패시 :  null 반환.
//                성공시: 네이버 유저정보 JSON Format String return
                return mOAuthLoginModule.requestApi(this@LoginActivity, at, url)
            }
            override fun onPostExecute(content: String) {
                val resultUserInfoJSON = JSONObject(content).getJSONObject("response")
                val userEmail = resultUserInfoJSON.getString("email")
                val userName = resultUserInfoJSON.getString("name")
                userIntent.putExtra("userName", userName)
                userIntent.putExtra("userEmail", userEmail)
                startActivity(userIntent)
            }

        }

        fun successLogin() {
            naver_logout_button.visibility = View.VISIBLE
            RequestApiTask().execute()
        }

        val mOAuthLoginHandler: OAuthLoginHandler = object : OAuthLoginHandler() {
            //            run :
            override fun run(success: Boolean) {
                if (success) {
                    val accessToken = mOAuthLoginModule.getAccessToken(this@LoginActivity)
                    val refreshToken = mOAuthLoginModule.getRefreshToken(this@LoginActivity)
                    val expiresAt = mOAuthLoginModule.getExpiresAt(this@LoginActivity)
                    val tokenType = mOAuthLoginModule.getTokenType(this@LoginActivity)
                    Log.i("Token", "Login Access Token : $accessToken")
                    Log.i("Token", "Login refresh Token : $refreshToken")
                    Log.i("Token", "Login expiresAt : $expiresAt")
                    Log.i("Token", "Login Token Type : $tokenType")
                    successLogin()
                } else {
                    val errorCode = mOAuthLoginModule.getLastErrorCode(this@LoginActivity).code
                    val errorDesc = mOAuthLoginModule.getLastErrorDesc(this@LoginActivity)
                    Toast.makeText(this@LoginActivity, "errorCode:" + errorCode+ ", errorDesc:" + errorDesc,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        naver_login_button.setOnClickListener {
//            OAuthLoginState.OK: 이미 로그인 되어있는 경우
            if(mOAuthLoginModule.getState(this@LoginActivity) == OAuthLoginState.OK) {
//                Access Token 이 이미 있는 상태. 로그인 버튼을 숨기는 Logic

                Toast.makeText(this, "you've already logined!!", Toast.LENGTH_SHORT).show()
                successLogin()
            } else {
//                startOauthLoginActivity는 API 인증에 실패했을 때만 호출되어야함.
//                서버 오류 등으로 실패시 재시도를 무한으로 반복할 수 있음.
//                따라서 재시도 횟수를 제한하는 로직을 추가하는 것이 Best.
//                1st Parameter: 메소드를 실행하는 Activity의 Context 객체
//                2nd Parameter: Login 결과를 받을 콜백 메소드
                mOAuthLoginModule.startOauthLoginActivity(this@LoginActivity, mOAuthLoginHandler)
            }
        }
        naver_logout_button.setOnClickListener {

//            로그인 인스턴스는 총 4가지 State를 갖는다.
//            NEED_INIT: 초기화가 필요한 상태
//            NEED_LOGIN: 로그인이 필요한 상태. 접근 토큰(access token)과 갱신 토큰(refresh token)이 모두 없습니다.
//            NEED_REFRESH_TOKEN: 토큰 갱신이 필요한 상태. 접근 토큰은 없고, 갱신 토큰은 있습니다.
//            OK: 접근 토큰이 있는 상태. 단, 사용자가 네이버의 내정보 > 보안설정 > 외부 사이트 연결 페이지에서 연동을 해제했다면 서버에서는 상태 값이 유효하지 않을 수 있습니다.
            val currentState = mOAuthLoginModule.getState(this).toString()

//            로그인되어 있거나 토큰이 존재하는 경우 로그아웃 진행
//            로그아웃되면 토큰도 모두 삭제되고 NEED_LOGIN 상태로 돌입함.
            if (currentState != "NEED_LOGIN") {
                mOAuthLoginModule.logout(this@LoginActivity)
                naver_logout_button.visibility = View.GONE
            } else {
                Toast.makeText(this,"로그인된 상태가 아니에요", Toast.LENGTH_SHORT).show()
            }


        }



    }

    private fun checkLocationPermission() : Unit {
//            위치 정보 액세스 권한이 있는지 체크
        if(ContextCompat.checkSelfPermission(this@LoginActivity, ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {


//            이미 해당 권한에 관한 요청을 거절한 경우에만 권한이 필요한 이유를 설명하는 메시지를 제공하는 것이 좋다.
//                권한이 필요한 이유를 띄우는 메소드 사용
//            shouldShowRequestPermissionRationale method return type is 'Boolean'
//            사용자가 이전에 요청 거부한 경우  ->true
//            권한 요청 다이얼로그에서 다시묻지 않음 옵션 선택 or 기기 정책상 이 권한 금지 -> false
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@LoginActivity, ACCESS_FINE_LOCATION)) {
//                위치권한이 필요한 이유를 설명하는 다이얼로그 생성
                showLocationPermissionDescription()

            } else {
//                    설명이 필요하지 않을경우 (앱 최초실행, 거절이력 X) 권한 요청 다이얼로그 띄우기
                ActivityCompat.requestPermissions(this@LoginActivity,
                    arrayOf(ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_CODE_LOCATION)
//                콜백 메소드가 여기서 요청에대한 결과값을 반환함.
            }
        } else {
//            권한이 이미 있는 경우 (Permission has already been granted)

        }

    }

//    사용자의 응답 전달 메소드, 권한 부여 확인
//    이 콜백에는 requestPermission에 전달한 것과 동일한 요청 코드가 전달됨.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
//                요청이 취소되면 결과는 empty 값을 가짐
//                요청이 취소되지 않고 승인결과를 얻는다면 (권한 허용)
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "위치권한 허용", Toast.LENGTH_SHORT).show()
                } else {
//                    권한 요청에 거절된다면..
                    Toast.makeText(this, "위치권한 불허", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun showLocationPermissionDescription() {
        val permissionDialogActivity = LocationPermissionDialogActivity()
        permissionDialogActivity.show(supportFragmentManager, "Location Permission Description")
    }
}
