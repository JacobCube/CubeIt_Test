package cz.cubeit.cubeit

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_adventure.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.pop_up_adventure_quest.view.*
import java.time.LocalDateTime
import java.time.ZoneId

var viewPagerSideQ:ViewPager? = null
var viewPopQuest:View? = null
var resourcesAdventure: Resources? = null
val db = FirebaseFirestore.getInstance()
val activeQuestDocRef = db.collection("users").document(player.username).collection("ActiveQuest").document("Quest")
val currentTimeDocRef = db.collection("users").document(player.username).collection("ServerTimestamp").document("TemporaryTimestamp")
@RequiresApi(Build.VERSION_CODES.O)
var questStartTime: LocalDateTime = LocalDateTime.now()
@RequiresApi(Build.VERSION_CODES.O)
var questEndTime: LocalDateTime = LocalDateTime.now()
@RequiresApi(Build.VERSION_CODES.O)
var currentTime: LocalDateTime = LocalDateTime.now()
var questLength: Int = 0


val docRef = db.collection("users").document(player.username).collection("ServerTimestamp").document("TemporaryTimestamp")

val map = HashMap<String, FieldValue>()

class Adventure : AppCompatActivity() {

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_adventure)
        resourcesAdventure = resources
        viewPagerSideQ = findViewById(R.id.viewPagerAdventure)
        viewPopQuest = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null)

        val dm = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)
        val displayY = dm.heightPixels.toDouble()
        val displayX = dm.widthPixels.toDouble()

        map["timestamp"] = FieldValue.serverTimestamp()


        docRef.set(map).addOnSuccessListener {
            currentTimeDocRef.get().addOnSuccessListener { documentSnapshot ->

                currentTime = documentSnapshot.getDate("timestamp")!!.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()

                Log.d("TimeDebug", "CurrentTime: ${currentTime.toString()}")

            }
        }





        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }

        sideQuestsIcon.layoutParams.height = (displayY / 10).toInt()
        sideQuestsIcon.layoutParams.width = (displayY / 10).toInt()
        sideQuestsIcon.y = 0f
        sideQuestsIcon.x = displayX.toFloat()
        fragmentSideQuestsAdventure.view!!.x = displayX.toFloat() - (sideQuestsIcon.width).toFloat()

        var iconSideQuestsAnim = ValueAnimator()

        sideQuestsIcon.setOnClickListener {
            if(iconSideQuestsAnim.isRunning)iconSideQuestsAnim.pause()
            if(sideQuestsIcon.x == displayX.toFloat()){
                iconSideQuestsAnim = ValueAnimator.ofFloat(sideQuestsIcon.x, sideQuestsIcon.x-(displayX / 10 * 3).toFloat()).apply{
                    duration = 800
                    addUpdateListener {
                        sideQuestsIcon.x = it.animatedValue as Float
                        fragmentSideQuestsAdventure.view!!.x = it.animatedValue as Float + (sideQuestsIcon.width*1.5).toFloat()
                    }
                    start()
                }
            }else{
                iconSideQuestsAnim = ValueAnimator.ofFloat(sideQuestsIcon.x, displayX.toFloat()).apply{
                    duration = 800
                    addUpdateListener {
                        sideQuestsIcon.x = it.animatedValue as Float
                        fragmentSideQuestsAdventure.view!!.x = it.animatedValue as Float + (sideQuestsIcon.width*1.5).toFloat()
                    }
                    start()
                }
            }
        }


        viewPagerSideQ!!.adapter = ViewPagerAdapterAdventure(supportFragmentManager)
        viewPagerSideQ!!.offscreenPageLimit = 6

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutMenuAdventure, Fragment_Menu_Bar.newInstance(R.id.viewPagerAdventure, R.id.frameLayoutMenuAdventure, R.id.homeButtonBackAdventure)).commitNow()

    }

    fun changeSurface(surfaceIndex:Int){
        val handler = Handler()
        if(viewPagerSideQ != null) {
            handler.postDelayed({ viewPagerSideQ!!.setCurrentItem(surfaceIndex, true) }, 10)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onClickQuest(view: View){
        val index = view.toString()[view.toString().length - 2].toString().toInt()-1
        val surface = view.toString()[view.toString().length - 8].toString().toInt()
        val window = PopupWindow(this)
        val viewPop:View = layoutInflater.inflate(R.layout.pop_up_adventure_quest, null)
        window.elevation = 0.0f
        window.contentView = viewPop
        val textViewQuest: TextView = viewPop.textViewQuest
        val buttonAccept: Button = viewPop.buttonAccept
        val buttonClose: Button = viewPop.buttonClose

        val quest:Quest = player.currentSurfaces[surface].quests[index]

        /*

        **Proof of concept - to be taken as an example**

        var activeQuest = ActiveQuest(quest, FieldValue.serverTimestamp(), 5)

        player.setActiveQuest(activeQuest)

        activeQuestDocRef.get().addOnSuccessListener { documentSnapshot ->

            if (documentSnapshot.exists()){
                activeQuest = documentSnapshot.toObject(ActiveQuest::class.java)!!

                questStartTime = documentSnapshot.getDate("startTime")!!.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
            }
            else {
                Log.d("LoadActiveQuestDebug", "User has no active quest")
            }
        }
        */


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textViewQuest.setText(Html.fromHtml(quest.getStats(resourcesAdventure!!),Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
        }

        window.isOutsideTouchable = false
        window.isFocusable = true

        window.setOnDismissListener {
            window.dismiss()
            player.currentSurfaces[surface].quests[index].generateQuest()
        }
        buttonAccept.setOnClickListener {
            window.dismiss()
        }
        buttonClose.setOnClickListener {
            window.dismiss()
        }

        window.showAtLocation(view, Gravity.CENTER,0,0)
    }

    fun onClickQuestSideQuest(surface:Int, index:Int, context:Context){
        val window = PopupWindow(context)
        window.elevation = 0.0f
        window.contentView = viewPopQuest
        val textViewQuest: TextView = viewPopQuest!!.findViewById(R.id.textViewQuest)
        val buttonAccept: Button = viewPopQuest!!.findViewById(R.id.buttonAccept)
        val buttonClose: Button = viewPopQuest!!.findViewById(R.id.buttonClose)

        var quest:Quest = player.currentSurfaces[surface].quests[index]


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textViewQuest.setText(Html.fromHtml(quest.getStats(resourcesAdventure!!),Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
        }

        window.setOnDismissListener {
            window.dismiss()
            player.currentSurfaces[surface].quests[index].generateQuest()
        }

        window.isOutsideTouchable = false
        window.isFocusable = true
        buttonAccept.setOnClickListener {
            window.dismiss()
        }
        buttonClose.setOnClickListener {
            window.dismiss()
        }

        window.showAtLocation(viewPagerSideQ, Gravity.CENTER,0,0)
    }
}

class ViewPagerAdapterAdventure internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment? {
        return when(position) {
            0 -> Fragment_Adventure_First()
            1 -> Fragment_Adventure_Second()
            2 -> Fragment_Adventure_Third()
            3 -> Fragment_Adventure_Fourth()
            4 -> FragmentAdventureFifth()
            5 -> Fragment_Adventure_Sixth()
            else -> null
        }
    }

    override fun getCount(): Int {
        return 6
    }
}

