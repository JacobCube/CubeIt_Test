package cz.cubeit.cubeit


import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_login_register.*
import me.kungfucat.viewpagertransformers.DepthPageTransformer
import kotlin.system.exitProcess

class ActivityLoginRegister: SystemFlow.GameActivity(R.layout.activity_login_register, ActivityType.LoginRegister, hasMenu = false, hasSwipeMenu = false, hasSwipeDown = false){
    private var signOut = false
    private var imageViewBlockade: ImageView? = null

    fun onClickArrowLoginRegister(v:View){
        when(v.toString()[v.toString().lastIndex-1]){
            '0' -> viewPagerLoginRegister.setCurrentItem(1, true)
            '1' -> viewPagerLoginRegister.setCurrentItem(0, true)
        }
    }

    override fun onResume() {
        super.onResume()
        checkStoragePermission()
        Data.makeDefaultPlayer()
        Data.player = Player()
    }

    override fun onBackPressed() {
        if(signOut) {
            finishAffinity()
            exitProcess(0)
        }
        else {
            Toast.makeText(this, "Press back again to icon_exit.", Toast.LENGTH_SHORT).show()
            signOut = true
            Handler().postDelayed({ signOut = false }, 2000)
        }
    }

    fun checkStoragePermission(): Boolean{
        val result = SystemFlow.isStoragePermissionGranted(this)
        if(!result){
            if(imageViewBlockade == null) imageViewBlockade = SystemFlow.attachShadowBlock(this)
        }else {
            val parent = window.decorView.rootView.findViewById<ViewGroup>(android.R.id.content)
            parent.removeView(imageViewBlockade)
            imageViewBlockade = null
        }

        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewLoginRegisterBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.login_bg_scene, opts))

        window.decorView.post {
            viewPagerLoginRegister?.apply {
                adapter = ViewPagerAdapterLoginRegister(supportFragmentManager)
                offscreenPageLimit = 2
                setPageTransformer(true, DepthPageTransformer())
            }

            Data.initialize(this)
            Data.loadingScreenType = LoadingType.Normal
        }
    }
    private class ViewPagerAdapterLoginRegister internal constructor(fm: FragmentManager): FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

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