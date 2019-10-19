package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import androidx.lifecycle.ProcessLifecycleOwner
import android.provider.Settings
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.util.DisplayMetrics
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.PopupMenu
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import java.lang.ref.WeakReference


var playedSong = R.raw.playedsong

class Home : AppCompatActivity() {

    companion object {
        class HomeInitialization (context: Home): AsyncTask<Int, String, String?>(){
            private val innerContext: WeakReference<Context> = WeakReference(context)

            override fun doInBackground(vararg params: Int?): String? {
                val context = innerContext.get() as Home?
                //context leakage solution

                return if(context != null){

                    if(Data.inboxSnapshotHome == null){                     //inbox messages realtime listener
                        val db = FirebaseFirestore.getInstance()
                        Data.inbox.sortByDescending { it.id }
                        val docRef = db.collection("users").document(Data.player.username).collection("Inbox").orderBy("id", Query.Direction.DESCENDING)
                        Data.inboxSnapshotHome = docRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                            if (e != null) {
                                return@addSnapshotListener
                            }

                            if (snapshot != null && !snapshot.isEmpty) {
                                val inboxSnap: MutableList<InboxMessage> = mutableListOf()
                                for(i in snapshot.documentChanges){
                                    if(i.type == DocumentChange.Type.ADDED)inboxSnap.add(i.document.toObject(InboxMessage::class.java))
                                }

                                inboxSnap.sortByDescending { it.id }

                                if(snapshot.documents.size >= 1 && inboxSnap.size > 0 && inboxSnap != Data.inbox){
                                    for(i in inboxSnap){
                                        if(!Data.inbox.any { it.id == i.id }  && i.status != MessageStatus.Read){
                                            Data.inbox.add(i)
                                            if(i.status != MessageStatus.Sent){
                                                Toast.makeText(context, "New message has arrived.", Toast.LENGTH_SHORT).show()
                                                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    v!!.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                                                } else {
                                                    v!!.vibrate(20)
                                                }
                                                Data.inboxChanged = true
                                                Data.inboxChangedMessages++
                                                context.runOnUiThread {
                                                    context.textViewHomeMailNew.visibility = View.VISIBLE
                                                }
                                            }
                                        }
                                    }
                                    SystemFlow.writeFileText(context, "inboxNew${Data.player.username}", "${Data.inboxChanged},${Data.inboxChangedMessages}")
                                }
                            }
                        }
                    }

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
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Server not available")
                                builder.setMessage("Server is currently ${snapshot.getString("Status")}.\nWe apologize for any inconvenience.\n" + if(snapshot.getBoolean("ShowDsc")!!)snapshot.getString("ExternalDsc") else "")
                                if(context.dialog == null) context.dialog = builder.create()
                                context.dialog!!.setCanceledOnTouchOutside(false)
                                context.dialog!!.setCancelable(false)

                                context.dialog!!.setButton(Dialog.BUTTON_POSITIVE, "OK") { dialogX, _ ->
                                    dialogX.dismiss()
                                }
                                context.dialog!!.setOnDismissListener {
                                    if(!context.dialog!!.isShowing) Data.logOut(context) else context.dialog!!.dismiss()
                                }
                                if(context.dialog?.isShowing == false) context.dialog?.show()
                            }

                        } else {
                            Log.d("Server listener", "$source data: null n error")
                        }
                    }

                    context.setupLifecycleListener()

                    "true"
                }else "false"
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                val context = innerContext.get() as Home?

                if (result != null && result.toBoolean()){
                    //do something, my result is successful
                }else {
                    Toast.makeText(context, "Something went wrong! Try restarting your application", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var dialog: AlertDialog? = null

    private val lifecycleListener: SystemFlow.LifecycleListener by lazy{
        SystemFlow.LifecycleListener(this)
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

        val tempActivity = this
        this.window.decorView.rootView.post {
            HomeInitialization(tempActivity).execute()
        }

        /*for(i in 0 until Data.player.currentSurfaces.size){
            val boss = Boss(surface = i)
            boss.initialize().addOnSuccessListener {
                Data.player.currentSurfaces[i].boss = boss
            }
        }*/

        Log.w("test_home", Data.spellClasses.size.toString())

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        layoutHome.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.homebackground, opts))

        if(Data.player.username == "Anonymous"){
            SystemFlow.showNotification("Oops", "There's a problem with your game, we're really sorry.", this).setOnDismissListener {
                Data.logOut(this)
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)&& Data.player.appearOnTop) {
                //If the draw over permission is not available open the settings screen to grant the permission.

                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                startActivityForResult(intent, 2084)
        }

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
            textViewHomeMailNew.visibility = View.VISIBLE
        }else {
            textViewHomeMailNew.visibility = View.GONE
            textViewHomeMailNew.visibility = View.GONE
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

        imageViewExit.setOnTouchListener(object : Class_OnSwipeTouchListener(this, imageViewExit, false) {            //disconnect swipe / open menu
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
