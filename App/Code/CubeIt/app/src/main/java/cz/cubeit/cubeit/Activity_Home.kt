package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import androidx.lifecycle.ProcessLifecycleOwner
import android.provider.Settings
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import android.view.ContextThemeWrapper
import android.view.MotionEvent
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)&& Data.player.appearOnTop) {
                //If the draw over permission is not available open the settings screen to grant the permission.

                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                startActivityForResult(intent, 2084)
        }

        setupLifecycleListener()

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                handler.postDelayed({hideSystemUI()},1000)
            }
        }
        textViewHomeStats.text = "Coins: ${Data.player.money}\nCubeCoins: ${Data.player.cubeCoins}\nGold: ${Data.player.gold}"
        textViewHomeStats.setPadding(15, 10, 15, 10)

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

        Data.itemClasses = mutableListOf(                   //should be cloud saved
                LoadItems("0", mutableListOf(
                        Runes(name = "Backpack", type = "Runes", drawableIn =  "00303", levelRq =  1, quality =  0, charClass =  0, description =  "Why is all your stuff so heavy?!", slot = 10, price = 1)
                        ,Runes(name = "Zipper", type = "Runes", drawableIn =  "00300", levelRq =  1, quality =  0, charClass =  0, description =  "Helps you take enemy's loot faster", slot = 11, price = 1)
                        ,Wearable(name = "Universal item 1", type =  "Wearable", drawableIn = "00301", levelRq =  1, quality =  0, charClass = 0, description =  "For everyone", slot = 2, price = 1)
                        ,Wearable(name ="Universal item 2", type =  "Wearable", drawableIn =  "00302", levelRq =  1, quality = 0, charClass =  0, description =  "Not for everyone", slot =  3, price = 1)
                )),
                LoadItems("1", mutableListOf(
                        Weapon(name = "Minigun prototype", type = "Weapon", drawableIn =  "00506", levelRq = 1, quality = 0,charClass = 1, description = "RATATATATATATATA", slot = 0, price = 1)
                        ,Weapon(name = "Crossbow", type = "Weapon", drawableIn =  "00505", levelRq = 1, quality = 0,charClass = 1, description = "With your skill, it just whistles", slot = 0, price = 1)
                        ,Weapon(name = "Granpa's teeth", type = "Weapon", drawableIn = "00504", levelRq = 1, quality = 0,charClass = 1, description = "Heirloom", slot = 1, price = 1)
                        ,Wearable(name = "Metal mettalic boots", type ="Wearable", drawableIn = "00501", levelRq = 1, quality = 0,charClass = 1, description = "I'm not an emo, I just look like it", slot = 6, price = 1)
                        ,Wearable(name = "Bitten trousers", type ="Wearable", drawableIn = "00500", levelRq = 1, quality = 0,charClass = 1, description = "It's a heirloom from my grandpa and grandma", slot = 7, price = 1)
                        ,Wearable(name = "Cloak from the hood", type ="Wearable", drawableIn = "00502", levelRq = 1, quality = 0,charClass = 1, description = "That won't hide your face...unfortunately", slot = 8, price = 1)
                        ,Wearable(name = "Hood from the hood", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 1, description = "Nice to (not) see you", slot = 9, price = 1)
                )),
                LoadItems("2", mutableListOf(
                        Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 2, description = "The most sold weapon on black market", slot = 0, price = 1)
                        ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 2, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                        ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 2, description = "I can't breath", slot = 4, price = 1)
                        ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 2, description = "What is that smell?", slot = 5, price = 1)
                        ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 2, description = "Can't carry it anymore", slot = 6, price = 1)
                        ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 2, description = "Tight not high", slot = 7, price = 1)
                        ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 2, description = "Chestplate protects!", slot = 8, price = 1)
                        ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 2, description = "This doesn't make you any more clever", slot = 9, price = 1)
                )),
                LoadItems("3", mutableListOf(
                        Weapon(name = "Long bow", type = "Weapon", drawableIn =  "02504", levelRq = 1, quality = 0,charClass = 3, description = "Shoot below the apple", slot = 0, price = 1)
                        ,Weapon(name = "Shadow bow", type = "Weapon", drawableIn =  "02505", levelRq = 1, quality = 0,charClass = 3, description = "Shoot below the apple and it will hit the apple", slot = 0, price = 1)
                        ,Wearable(name = "Light boots", type ="Wearable", drawableIn = "02500", levelRq = 1, quality = 0,charClass = 3, description = "Step by step.. tadatatatataaataaaa", slot = 6, price = 1)
                        ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "02503", levelRq = 1, quality = 0,charClass = 3, description = "No idea how those bows fit in those", slot = 7, price = 1)
                        ,Wearable(name = "Light armor", type ="Wearable", drawableIn = "02501", levelRq = 1, quality = 0,charClass = 3, description = "It's all just a mind game", slot = 8, price = 1)
                        ,Wearable(name = "Light bycocket", type ="Wearable", drawableIn = "02502", levelRq = 1, quality = 0,charClass = 3, description = "Pointed in the front like a bird's beak", slot = 9, price = 1)
                )),
                LoadItems("4", mutableListOf(
                        Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 4, description = "The most sold weapon on black market", slot = 0, price = 1)
                        ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 4, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                        ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 4, description = "I can't breath", slot = 4, price = 1)
                        ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 4, description = "What is that smell?", slot = 5, price = 1)
                        ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 4, description = "Can't carry it anymore", slot = 6, price = 1)
                        ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 4, description = "Tight not high", slot = 7, price = 1)
                        ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 4, description = "Chestplate protects!", slot = 8, price = 1)
                        ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 4, description = "This doesn't make you any more clever", slot = 9, price = 1)
                )),
                LoadItems("5", mutableListOf(
                        Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 5, description = "The most sold weapon on black market", slot = 0, price = 1)
                        ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 5, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                        ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 5, description = "I can't breath", slot = 4, price = 1)
                        ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 5, description = "What is that smell?", slot = 5, price = 1)
                        ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 5, description = "Can't carry it anymore", slot = 6, price = 1)
                        ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 5, description = "Tight not high", slot = 7, price = 1)
                        ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 5, description = "Chestplate protects!", slot = 8, price = 1)
                        ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 5, description = "This doesn't make you any more clever", slot = 9, price = 1)
                )),
                LoadItems("6", mutableListOf(
                        Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 6, description = "The most sold weapon on black market", slot = 0, price = 1)
                        ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 6, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                        ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 6, description = "I can't breath", slot = 4, price = 1)
                        ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 6, description = "What is that smell?", slot = 5, price = 1)
                        ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 6, description = "Can't carry it anymore", slot = 6, price = 1)
                        ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 6, description = "Tight not high", slot = 7, price = 1)
                        ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 6, description = "Chestplate protects!", slot = 8, price = 1)
                        ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 6, description = "This doesn't make you any more clever", slot = 9, price = 1)
                )),
                LoadItems("7", mutableListOf(
                        Weapon(name = "Sword", type = "Weapon", drawableIn =  "00407", levelRq = 1, quality = 0,charClass = 7, description = "The most sold weapon on black market", slot = 0, price = 1)
                        ,Weapon(name = "Shield", type = "Weapon", drawableIn = "00401", levelRq = 1, quality = 0,charClass = 7, description = "Blocks 80% of next enemy attack\nYou can't use it as a boat anymore after all this", slot = 1, price = 1)
                        ,Wearable(name = "Belt", type ="Wearable", drawableIn = "00406", levelRq = 1, quality = 0,charClass = 7, description = "I can't breath", slot = 4, price = 1)
                        ,Wearable(name = "Overall", type ="Wearable", drawableIn = "00402", levelRq = 1, quality = 0,charClass = 7, description = "What is that smell?", slot = 5, price = 1)
                        ,Wearable(name = "Boots", type ="Wearable", drawableIn = "00405", levelRq = 1, quality = 0,charClass = 7, description = "Can't carry it anymore", slot = 6, price = 1)
                        ,Wearable(name = "Trousers", type ="Wearable", drawableIn = "00400", levelRq = 1, quality = 0,charClass = 7, description = "Tight not high", slot = 7, price = 1)
                        ,Wearable(name = "Chestplate", type ="Wearable", drawableIn = "00404", levelRq = 1, quality = 0,charClass = 7, description = "Chestplate protects!", slot = 8, price = 1)
                        ,Wearable(name = "Helmet", type ="Wearable", drawableIn = "00403", levelRq = 1, quality = 0,charClass = 7, description = "This doesn't make you any more clever", slot = 9, price = 1)
                )),
                LoadItems("8", mutableListOf(
                        Weapon(name = "Cane pickaxe", type = "Weapon", drawableIn =  "07505", levelRq = 1, quality = 0,charClass = 8, description = "Even the smallest lick may kill you", slot = 0, price = 1)
                        ,Weapon(name = "Cane shield", type = "Weapon", drawableIn = "07504", levelRq = 1, quality = 0,charClass = 8, description = "If you won't stop licking it, you're doomed", slot = 1, price = 1)
                        ,Wearable(name = "Santa's boots", type ="Wearable", drawableIn = "07500", levelRq = 1, quality = 0,charClass = 8, description = "Can't carry it anymore", slot = 6, price = 1)
                        ,Wearable(name = "Santa's metal boots", type ="Wearable", drawableIn = "07501", levelRq = 1, quality = 0,charClass = 8, description = "Can't carry it anymore", slot = 6, price = 1)
                        ,Wearable(name = "Santa's trousers", type ="Wearable", drawableIn = "07506", levelRq = 1, quality = 0,charClass = 8, description = "Little bit outstratched after last Christmas", slot = 7, price = 1)
                        ,Wearable(name = "Santa's sweater", type ="Wearable", drawableIn = "07502", levelRq = 1, quality = 0,charClass = 8, description = "Ew! Milk everywhere", slot = 8, price = 1)
                        ,Wearable(name = "Santa's hat", type ="Wearable", drawableIn = "07503", levelRq = 1, quality = 0,charClass = 8, description = "HO HO HO!", slot = 9, price = 1)
                ))
        )

        imageViewExit.setOnTouchListener(object : Class_OnSwipeTouchListener(this) {            //disconnect swipe / open menu
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalXExit = imageViewExit.x
                        initialTouchExitX = motionEvent.rawX
                        clickableExit = true
                        handler.postDelayed({clickableExit = false}, 100)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        imageViewExit.x = originalXExit
                        imageViewExitLeave.x = originalXExit + imageViewExitLeave.width

                        if(clickableExit){
                            handler.removeCallbacksAndMessages(null)

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
                                Data.uploadGlobalData()
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
            val intent = Intent(this, Activity_Story()::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        Hatch.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.ActivityFightBoard::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Skills.setOnClickListener{
            val intent = Intent(this, Spells()::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Character.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.Activity_Character::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        SettingsHome.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.ActivitySettings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Shop.setOnClickListener {
            val intent = Intent(this, cz.cubeit.cubeit.Activity_Shop::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Adventure.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.Adventure::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
    }
}
