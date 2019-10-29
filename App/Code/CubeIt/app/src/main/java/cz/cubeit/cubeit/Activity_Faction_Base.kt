package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_faction_base.*
import kotlinx.android.synthetic.main.fragment_faction_edit.*
import kotlinx.android.synthetic.main.fragment_faction_managment.*
import kotlinx.android.synthetic.main.fragment_faction_managment.view.*
import kotlinx.android.synthetic.main.popup_dialog.view.*
import java.lang.Exception

class Activity_Faction_Base: AppCompatActivity(){           //arguments - id: String

    var displayY = 0.0
    var displayX = 0.0
    lateinit var frameLayoutMenuFactionTemp: FrameLayout
    lateinit var viewPagerFactionTemp: StoryViewPager
    lateinit var tabLayoutFactionTemp: TabLayout
    lateinit var buttonFactionSaveTemp: Button
    var inviteList: MutableList<String> = mutableListOf()

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewRect = Rect()
        frameLayoutMenuFactionTemp.getGlobalVisibleRect(viewRect)

        if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) && frameLayoutMenuFactionTemp.y <= (displayY * 0.83).toFloat()) {

            ValueAnimator.ofFloat(frameLayoutMenuFactionTemp.y, displayY.toFloat()).apply {
                duration = (frameLayoutMenuFactionTemp.y/displayY * 160).toLong()
                addUpdateListener {
                    frameLayoutMenuFactionTemp.y = it.animatedValue as Float
                }
                start()
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Action_base", "has been determined")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_faction_base)
        frameLayoutMenuFactionTemp = frameLayoutMenuFaction
        viewPagerFactionTemp = viewPagerFaction
        tabLayoutFactionTemp = tabLayoutFaction

        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutMenuFaction, Fragment_Menu_Bar.newInstance(R.id.imageViewFactionBg, R.id.frameLayoutMenuFaction, R.id.homeButtonBackFaction, R.id.imageViewMenuUpFaction), "menuFaction").commit()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                Handler().postDelayed({ hideSystemUI() }, 1000)
            }
        }

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(dm)
        displayY = dm.heightPixels.toDouble()
        displayX = dm.widthPixels.toDouble()

        System.gc()
        frameLayoutMenuFaction.y = displayY.toFloat()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        imageViewFactionBg.setImageBitmap(BitmapFactory.decodeResource(resources, R.color.loginColor, opts))

        viewPagerFactionTemp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                /*tabLayoutFactionTemp.visibility = if(position > 2){
                    View.GONE
                }else {
                    View.VISIBLE
                }*/
            }
        })

        viewPagerFactionTemp.adapter = ViewPagerFactionOverview(supportFragmentManager, intent?.extras?.getString("id"))
        viewPagerFactionTemp.offScreenPageLimiCustom = 0
        //tabLayoutFactionTemp.setupWithViewPager(viewPagerFactionTemp)

        tabLayoutFactionTemp.addTab(tabLayoutFactionTemp.newTab(), 0)
        tabLayoutFactionTemp.addTab(tabLayoutFactionTemp.newTab(), 1)
        tabLayoutFactionTemp.addTab(tabLayoutFactionTemp.newTab(), 2)
        tabLayoutFactionTemp.getTabAt(0)!!.text = "Faction"
        tabLayoutFactionTemp.getTabAt(1)!!.text = "Edit"
        tabLayoutFactionTemp.getTabAt(2)!!.text = "MGMT"

        tabLayoutFactionTemp.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if(viewPagerFactionTemp.currentItem >= 1){
                    when(tab.position){
                        0 -> {
                            viewPagerFactionTemp.currentItem = 1
                            buttonFactionSaveTemp.visibility = View.GONE
                            tabLayoutFactionTemp.visibility = View.VISIBLE
                        }
                        1 -> {
                            viewPagerFactionTemp.currentItem = 2
                            buttonFactionSaveTemp.visibility = View.VISIBLE
                            tabLayoutFactionTemp.visibility = View.VISIBLE
                        }
                        2 -> {
                            viewPagerFactionTemp.currentItem = 3
                            buttonFactionSaveTemp.visibility = View.VISIBLE
                            tabLayoutFactionTemp.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }

        })

        frameLayoutMenuFactionTemp.bringToFront()

        buttonFactionSaveTemp = buttonFactionSave
        buttonFactionSaveTemp.setOnClickListener {
            if(viewPagerFactionTemp.currentItem == 2 && Data.player.faction != null){   //TODO porovnání provedených změn v pozadí
                val taxNum = try {
                    if(editTextFactionEditTax.text.toString().toInt() > 0){
                        editTextFactionEditTax.text.toString().toInt()
                    }else 0
                }catch (e: Exception){
                    0
                }

                val description = if(editTextFactionEditDescription.text.toString().length < 1000){
                    editTextFactionEditDescription.text.toString()
                }else {
                    Snackbar.make(window.decorView, "The given description is too long!", Snackbar.LENGTH_SHORT).show()
                    Data.player.faction!!.description
                }

                val invitationMessage = if(editTextFactionEditInvitationMsg.text.toString().length < 500){
                    editTextFactionEditInvitationMsg.text.toString().toString()
                }else {
                    Snackbar.make(window.decorView, "The given invitation message is too long!", Snackbar.LENGTH_SHORT).show()
                    Data.player.faction!!.invitationMessage
                }

                val warnMessage = if(editTextFactionEditWarnMsg.text.toString().length < 500){
                    editTextFactionEditWarnMsg.text.toString()
                }else {
                    Snackbar.make(window.decorView, "The given warn message is too long!", Snackbar.LENGTH_SHORT).show()
                    Data.player.faction!!.warnMessage
                }

                if(SystemFlow.factionChange){
                    val view = layoutInflater.inflate(R.layout.popup_dialog,null)
                    val window = PopupWindow(this)
                    window.contentView = view
                    val buttonYes:Button = view.buttonYes
                    val buttonNo: ImageView = view.buttonCloseDialog
                    window.isOutsideTouchable = false
                    window.isFocusable = true
                    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    buttonYes.setOnClickListener {
                        Data.player.faction!!.description = description
                        Data.player.faction!!.invitationMessage = invitationMessage
                        Data.player.faction!!.warnMessage = warnMessage
                        Data.player.faction!!.taxPerDay = taxNum
                        Data.player.faction!!.openToAllies = checkBoxFactionEditAllies.isChecked
                        Data.player.faction!!.upload()
                        window.dismiss()

                        for(i in inviteList){
                            Data.player.writeInbox(i, InboxMessage(status = MessageStatus.Faction, receiver = i, sender = Data.player.username, subject = "${Data.player.faction!!.name} invited you.", content = editTextFactionEditInvitationMsg.text.toString(), isInvitation1 = true, invitation = Invitation(Data.player.username, " invited you to faction ", Data.player.faction!!.name, InvitationType.faction, Data.player.factionID!!, Data.player.factionName!!)))
                        }
                        tabLayoutFactionTemp.getTabAt(0)?.select()
                    }
                    buttonNo.setOnClickListener {
                        window.dismiss()
                    }
                    window.showAtLocation(it, Gravity.CENTER,0,0)
                }else {
                    Data.player.faction!!.description = description
                    Data.player.faction!!.invitationMessage = invitationMessage
                    Data.player.faction!!.warnMessage = warnMessage
                    Data.player.faction!!.taxPerDay = taxNum
                    Data.player.faction!!.openToAllies = checkBoxFactionEditAllies.isChecked

                    for(i in inviteList){
                        Data.player.writeInbox(i, InboxMessage(status = MessageStatus.Faction, receiver = i, sender = Data.player.username, subject = "${Data.player.faction!!.name} invited you.", content = editTextFactionEditInvitationMsg.text.toString(), isInvitation1 = true, invitation = Invitation(Data.player.username, " invited you to faction ", Data.player.faction!!.name, InvitationType.faction, Data.player.factionID!!, Data.player.factionName!!)))
                    }

                    Data.player.faction!!.upload()
                    tabLayoutFactionTemp.getTabAt(0)?.select()
                }
            }else if(viewPagerFactionTemp.currentItem == 3 && Data.player.faction != null){
                if(editTextFactionMngExtDesc.text.toString().length < 1000){
                    Data.player.faction!!.externalDescription = editTextFactionMngExtDesc.text.toString()
                }else {
                    Snackbar.make(window.decorView, "The given description is too long!", Snackbar.LENGTH_SHORT).show()
                }
                Data.player.faction!!.democracy = checkBoxFactionMngDemocracy.isChecked
                Data.player.faction!!.upload()
                tabLayoutFactionTemp.getTabAt(0)?.select()
            }else Log.d("current item", viewPagerFactionTemp.currentItem.toString())
        }

        if(intent?.extras?.getString("id").toString() != "null" || Data.player.factionID != null) {
            viewPagerFactionTemp.currentItem = 1
            if(intent?.extras?.getString("id").toString() != "null")tabLayoutFactionTemp.visibility = View.GONE
        }else {
            tabLayoutFactionTemp.visibility = View.GONE
        }
    }

    fun changePage(index: Int){
        viewPagerFactionTemp.currentItem = index
    }

    private class ViewPagerFactionOverview internal constructor(fm: FragmentManager, private val fractionID: String?) : FragmentPagerAdapter(fm){

        override fun getItem(position: Int): Fragment {
            Log.d("faction index", position.toString())
            return when(position) {
                0 -> Fragment_Faction_Create()
                1 -> Fragment_Faction.newInstance(fractionID)
                2 -> Fragment_Faction_Edit()
                3 -> Fragment_Faction_Managment()
                else -> Fragment_Faction_Create()
            }
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position){
                2 -> "Faction"
                3 -> "Edit"
                4 -> "MGMT"
                else -> null
            }
        }
    }
}