package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login_register.*
import android.os.Looper
import com.google.gson.Gson


val handler = Handler()

class ActivityLoginRegister(private val loginUsername: String = "", private val loginEmail:String = ""):AppCompatActivity(){

    fun onClickArrowLoginRegister(v:View){
        when(v.toString()[v.toString().lastIndex-1]){
            '0' -> viewPagerLoginRegister.setCurrentItem(1, true)
            '1' -> viewPagerLoginRegister.setCurrentItem(0, true)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_login_register)

        val adapter = ViewPagerAdapterLoginRegister(supportFragmentManager)
        viewPagerLoginRegister!!.adapter = adapter
        viewPagerLoginRegister!!.offscreenPageLimit = 2

        Data.loadingScreenType = LoadingType.Normal

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }
    }
}


class ViewPagerAdapterLoginRegister internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> FragmentLogin()
            1 -> Fragment_Register()
            else -> FragmentLogin()
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> "Login"
            1 -> "Register"
            else -> null
        }
    }
}