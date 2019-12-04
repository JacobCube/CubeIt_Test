package cz.cubeit.cubeit_test

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.View
import kotlinx.android.synthetic.main.activity_login_register.*
import android.widget.Toast
import kotlin.system.exitProcess


class ActivityLoginRegister: SystemFlow.GameActivity(R.layout.activity_login_register, ActivityType.LoginRegister, false){
    var signOut = false

    fun onClickArrowLoginRegister(v:View){
        when(v.toString()[v.toString().lastIndex-1]){
            '0' -> viewPagerLoginRegister.setCurrentItem(1, true)
            '1' -> viewPagerLoginRegister.setCurrentItem(0, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageViewLoginRegisterBg.setImageResource(0)
    }

    override fun onBackPressed() {
        if(signOut) {
            finishAffinity()
            exitProcess(0)
        }
        else {
            Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_SHORT).show()
            signOut = true
            Handler().postDelayed({ signOut = false }, 2000)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewLoginRegisterBg.apply {
            setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.login_bg_scene, opts))
            setOnClickListener {

            }
        }

        runOnUiThread {
            val adapter = ViewPagerAdapterLoginRegister(supportFragmentManager)
            viewPagerLoginRegister!!.adapter = adapter
            viewPagerLoginRegister!!.offscreenPageLimit = 2

            Data.initialize(this)
            Data.loadingScreenType = LoadingType.Normal
        }
    }
    private class ViewPagerAdapterLoginRegister internal constructor(fm: FragmentManager): FragmentPagerAdapter(fm){

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
}