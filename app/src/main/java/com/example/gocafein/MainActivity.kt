package com.example.gocafein

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }
    fun init() {
    //        OAuthLogin 객체를 얻고
        val mOAuthLoginModule = OAuthLogin.getInstance()

        //        LoginModule init method가 여러번 실행돼도
        //        access, refresh token은 유지됨.
        //        삭제를 위해서는 OAuthLogin.logout() or OAuthLogin.logoutAndDeleteToken() 호출 필요
        //        clientId: API 사용 허가 절차상 app을 등록하고 Naver Develper 사이트로부터 부여받은 id
        //        clientName: 네이버 로그인시 앱의 로그인 화면에 표시할 앱 이름.
        //        Mobile web의 로그인 화셤ㄴ시 서버에 저장된 앱의 이름이 표시됨.
        mOAuthLoginModule.init(
                this
                ,getString(R.string.client_id)
                ,getString(R.string.client_secret)
                ,getString(R.string.client_name)
        )

        val mOAuthLoginHandler: OAuthLoginHandler = object : OAuthLoginHandler() {
            override fun run(success: Boolean) {
                if (success) {
                    val accessToken = mOAuthLoginModule.getAccessToken(this@MainActivity)
                    val refreshToken = mOAuthLoginModule.getRefreshToken(this@MainActivity)
                    val expiresAt = mOAuthLoginModule.getExpiresAt(this@MainActivity)
                    val tokenType = mOAuthLoginModule.getTokenType(this@MainActivity)
                    Log.i("Token", "nhn Login Access Token : $accessToken")
                    Log.i("Token", "nhn Login refresh Token : $refreshToken")
                    Log.i("Token", "nhn Login expiresAt : $expiresAt")
                    Log.i("Token", "nhn Login Token Type : $tokenType")
                } else {
                    val errorCode = mOAuthLoginModule.getLastErrorCode(this@MainActivity).code
                    val errorDesc = mOAuthLoginModule.getLastErrorDesc(this@MainActivity)
                    Toast.makeText(this@MainActivity, "errorCode:" + errorCode+ ", errorDesc:" + errorDesc,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        naver_login_button.setOnClickListener {
            if(mOAuthLoginModule.getState(this@MainActivity) == OAuthLoginState.OK) {
                Toast.makeText(this, "you've already logined!!", Toast.LENGTH_SHORT).show()
            } else {
                mOAuthLoginModule.startOauthLoginActivity(this, mOAuthLoginHandler)
            }
        }
        naver_logout_button.setOnClickListener {
//            호출시 클라이언트에 저장된 토큰이 삭제되고
//            OAuthLogin.getState() Method가 LoginState.NEED_LOGIN 을 반환함.
            mOAuthLoginModule.logout(this@MainActivity)
        }



    }
}
