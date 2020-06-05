package com.example.gocafein

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }



    fun init() {
    //        OAuthLogin 객체(Singleton instance)를 얻고
        val mOAuthLoginModule = OAuthLogin.getInstance()
        val userIntent = Intent(this@MainActivity, EndActivity::class.java)

        //        LoginModule init method가 여러번 실행돼도
        //        access, refresh token은 유지됨.
        //        삭제를 위해서는 OAuthLogin.logout() or OAuthLogin.logoutAndDeleteToken() 호출 필요
        //        clientId: API 사용 허가 절차상 app을 등록하고 Naver Develper 사이트로부터 부여받은 id
        //        clientName: 네이버 로그인시 앱의 로그인 화면에 표시할 앱 이름.
        //        Mobile web의 로그인 화셤ㄴ시 서버에 저장된 앱의 이름이 표시됨.
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
                val at: String = mOAuthLoginModule.getAccessToken(this@MainActivity)
//      API 호출   실패시 :  null 반환.
//                성공시: 네이버 유저정보 JSON Format String return
                return mOAuthLoginModule.requestApi(this@MainActivity, at, url)
            }
            override fun onPostExecute(content: String) {
                val resultUserInfoJSON = JSONObject(content).getJSONObject("response")
                val userEmail = resultUserInfoJSON.getString("email")
                val userName = resultUserInfoJSON.getString("name")
                Log.i("userInfo", "이름 : $userName \n이메일: $userEmail")
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
                    val accessToken = mOAuthLoginModule.getAccessToken(this@MainActivity)
                    val refreshToken = mOAuthLoginModule.getRefreshToken(this@MainActivity)
                    val expiresAt = mOAuthLoginModule.getExpiresAt(this@MainActivity)
                    val tokenType = mOAuthLoginModule.getTokenType(this@MainActivity)
                    Log.i("Token", "Login Access Token : $accessToken")
                    Log.i("Token", "Login refresh Token : $refreshToken")
                    Log.i("Token", "Login expiresAt : $expiresAt")
                    Log.i("Token", "Login Token Type : $tokenType")
                    successLogin()
                } else {
                    val errorCode = mOAuthLoginModule.getLastErrorCode(this@MainActivity).code
                    val errorDesc = mOAuthLoginModule.getLastErrorDesc(this@MainActivity)
                    Toast.makeText(this@MainActivity, "errorCode:" + errorCode+ ", errorDesc:" + errorDesc,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        naver_login_button.setOnClickListener {
//            OAuthLoginState.OK: 이미 로그인 되어있는 경우
            if(mOAuthLoginModule.getState(this@MainActivity) == OAuthLoginState.OK) {
//                Access Token 이 이미 있는 상태. 로그인 버튼을 숨기는 Logic

                Toast.makeText(this, "you've already logined!!", Toast.LENGTH_SHORT).show()
                successLogin()
            } else {
//                startOauthLoginActivity는 API 인증에 실패했을 때만 호출되어야함.
//                서버 오류 등으로 실패시 재시도를 무한으로 반복할 수 있음.
//                따라서 재시도 횟수를 제한하는 로직을 추가하는 것이 Best.
//                1st Parameter: 메소드를 실행하는 Activity의 Context 객체
//                2nd Parameter: Login 결과를 받을 콜백 메소드
                mOAuthLoginModule.startOauthLoginActivity(this@MainActivity, mOAuthLoginHandler)
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
            if (!currentState.equals("NEED_LOGIN")) {
                mOAuthLoginModule.logout(this@MainActivity)
                naver_logout_button.visibility = View.GONE
            } else {
                Toast.makeText(this,"로그인된 상태가 아니에요", Toast.LENGTH_SHORT).show()
            }


        }



    }
}
