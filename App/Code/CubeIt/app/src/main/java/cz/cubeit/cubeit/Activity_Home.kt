package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import androidx.lifecycle.ProcessLifecycleOwner
import android.provider.Settings
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.PopupMenu


var playedSong = R.raw.playedsong

class Home : AppCompatActivity() {

    var dialog: AlertDialog? = null

    private val lifecycleListener: LifecycleListener by lazy{
        LifecycleListener(this)
    }

    private fun setupLifecycleListener() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleListener)
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onBackPressed() {
    }

    @ExperimentalStdlibApi
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_home)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        layoutHome.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.homebackground, opts))

        if(Data.player.username == "player"){
            SystemFlow.showNotification("Oops", "There's a problem with your game, we're really sorry.", this).setOnDismissListener {
                Data.logOut(this)
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)&& Data.player.appearOnTop) {
                //If the draw over permission is not available open the menu_settings_icon screen to grant the permission.

                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                startActivityForResult(intent, 2084)
        }

        setupLifecycleListener()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                Handler().postDelayed({hideSystemUI()},1000)
            }
        }
        textViewHomeStatsCoins.text = Data.player.cubeCoins.toString()
        textViewHomeStats.text = "\ncubix ${Data.player.cubix}\ngold ${Data.player.gold}"
        textViewHomeStats.setPadding(15, 10, 15, 10)

        val dm = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(dm)

        textViewHomeStats.setOnClickListener {
            val animation = SystemFlow.createLoading(this, dm.widthPixels)
            Handler().postDelayed({
                animation.cancel()
            }, 5000)
        }

        imageViewHomeFactionNew.visibility = if(Data.factionLogChanged){
            View.VISIBLE
        }else View.GONE

        val db = FirebaseFirestore.getInstance()                                                        //listens to every server status change
        val docRef = db.collection("Server").document("Generic")
        docRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
            if (e != null) {
                Log.w("Server listener", "Listen failed.", e)
                return@addSnapshotListener
            }

            val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                "Local"
            else
                "Server"

            if (snapshot != null && snapshot.exists()) {
                Log.d("Server listener", "data accepted")

                if(snapshot.getString("Status") != "on"){
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Server not available")
                    builder.setMessage("Server is currently ${snapshot.getString("Status")}.\nWe apologize for any inconvenience.\n" + if(snapshot.getBoolean("ShowDsc")!!)snapshot.getString("ExternalDsc") else "")
                    if(dialog == null)dialog = builder.create()
                    dialog!!.setCanceledOnTouchOutside(false)
                    dialog!!.setCancelable(false)

                    dialog!!.setButton(Dialog.BUTTON_POSITIVE, "OK") { dialogX, which ->
                        dialogX.dismiss()
                    }
                    dialog!!.setOnDismissListener {
                        if(!dialog!!.isShowing)Data.logOut(this) else dialog!!.dismiss()
                    }
                    if(!dialog!!.isShowing)dialog!!.show()
                }

            } else {
                Log.d("Server listener", "$source data: null n error")
            }
        }

        buttonHomeFaction.setOnClickListener {
            val intent = Intent(this, Activity_Faction_Base()::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        buttonHomeMarket.setOnClickListener {
            val intent = Intent(this, Activity_Market()::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        if(Data.inboxChanged && Data.inboxChangedMessages >= 1){
            textViewHomeMailNew.visibility = View.VISIBLE
            textViewHomeMailNew.text = Data.inboxChangedMessages.toString()
            imageViewHomeMailNew.visibility = View.VISIBLE
        }else {
            textViewHomeMailNew.visibility = View.GONE
            imageViewHomeMailNew.visibility = View.GONE
        }

        imageViewHomeInbox.setOnClickListener {
            val intent = Intent(this, Activity_Inbox()::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
            if(Data.inboxChanged) Data.inboxChanged = false
        }

        var originalXExit = 0f
        var initialTouchExitX = 0f
        var clickableExit = false

        imageViewExit.setOnTouchListener(object : Class_OnSwipeTouchListener(this, imageViewExit) {            //disconnect swipe / open menu
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalXExit = imageViewExit.x
                        initialTouchExitX = motionEvent.rawX
                        clickableExit = true
                        Handler().postDelayed({clickableExit = false}, 100)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        imageViewExit.x = originalXExit
                        imageViewExitLeave.x = originalXExit + imageViewExitLeave.width

                        if(clickableExit){
                            Handler().removeCallbacksAndMessages(null)

                            val wrapper = ContextThemeWrapper(this@Home, R.style.FactionPopupMenu)
                            val popup = PopupMenu(wrapper, imageViewExit)
                            val popupMenu = popup.menu

                            popupMenu.add("Exit")
                            popupMenu.add("Play rocket game")

                            popup.setOnMenuItemClickListener {
                                when(it.title){
                                    "Exit" -> {
                                        Data.logOut(this@Home)
                                        true
                                    }
                                    "Play rocket game" -> {
                                        val intentSplash = Intent(this@Home, Activity_Splash_Screen::class.java)
                                        Data.loadingScreenType = LoadingType.RocketGamePad
                                        SystemFlow.writeObject(this@Home, "loadingScreenType${Data.player.username}.data", Data.loadingScreenType)
                                        Data.loadingStatus = LoadingStatus.CLOSELOADING
                                        intentSplash.putExtra("keepLoading", true)
                                        this@Home.startActivity(intentSplash)
                                        true
                                    }
                                    else -> {
                                        true
                                    }
                                }
                            }
                            popup.show()
                        }else {
                            if(motionEvent.rawX <= originalXExit - imageViewExit.width){
                                if(!imageViewExit.isEnabled) return true
                                imageViewExit.isEnabled = false
                                Data.logOut(this@Home)
                                return true
                            }
                        }

                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        imageViewExit.x = ((originalXExit + (motionEvent.rawX - initialTouchExitX)/3))
                        imageViewExitLeave.x = ((originalXExit + imageViewExitLeave.width + ((motionEvent.rawX - initialTouchExitX))/3))
                        return true
                    }
                }

                return super.onTouch(view, motionEvent)
            }
        })

        Story.setOnClickListener {
            Story.isEnabled = false
            Handler().postDelayed({
                Story.isEnabled = true
            }, 150)

            val intent = Intent(this, Activity_Story()::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        Hatch.setOnClickListener{
            Hatch.isEnabled = false
            Handler().postDelayed({
                Hatch.isEnabled = true
            }, 150)

            val intent = Intent(this, cz.cubeit.cubeit.ActivityFightBoard::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Skills.setOnClickListener{
            Skills.isEnabled = false
            Handler().postDelayed({
                Skills.isEnabled = true
            }, 150)

            val intent = Intent(this, Spells()::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Character.setOnClickListener{
            Character.isEnabled = false
            Handler().postDelayed({
                Character.isEnabled = true
            }, 150)

            val intent = Intent(this, cz.cubeit.cubeit.Activity_Character::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        SettingsHome.setOnClickListener{
            SettingsHome.isEnabled = false
            Handler().postDelayed({
                SettingsHome.isEnabled = true
            }, 150)

            val intent = Intent(this, cz.cubeit.cubeit.ActivitySettings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Shop.setOnClickListener {
            Shop.isEnabled = false
            Handler().postDelayed({
                Shop.isEnabled = true
            }, 150)

            val intent = Intent(this, cz.cubeit.cubeit.Activity_Shop::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Adventure.setOnClickListener{
            Adventure.isEnabled = false
            Handler().postDelayed({
                Adventure.isEnabled = true
            }, 150)

            val intent = Intent(this, cz.cubeit.cubeit.Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
    }
}
