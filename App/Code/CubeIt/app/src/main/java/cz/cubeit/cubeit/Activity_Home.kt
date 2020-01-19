package cz.cubeit.cubeit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import kotlinx.android.synthetic.main.activity_home.*
import androidx.lifecycle.ProcessLifecycleOwner
import android.provider.Settings
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.widget.PopupMenu
import android.widget.Toast
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.lang.ref.WeakReference

class ActivityHome : SystemFlow.GameActivity(contentLayoutId = R.layout.activity_home, activityType = ActivityType.Home, hasMenu = false, hasSwipeDown = false) {

    lateinit var genericContext: Context

    companion object {
        class HomeInitialization (context: ActivityHome): AsyncTask<Int, String, String?>(){
            private val innerContext: WeakReference<Context> = WeakReference(context)

            override fun doInBackground(vararg params: Int?): String? {
                val context = innerContext.get() as ActivityHome?
                //context leakage solution

                return if(context != null){
                    val db = FirebaseFirestore.getInstance()

                    //realtime admin messaging
                    if(Data.adminMessagesSnapshotHome == null){
                        val docRefAdminMessage = db.collection("Server").document("GlobalLiveMessage")
                        Data.adminMessagesSnapshotHome = docRefAdminMessage.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                            if (e != null) {
                                return@addSnapshotListener
                            }

                            if (snapshot != null && snapshot.exists()) {
                                Log.d("snapshot_listener", "is null, initializing")
                                val snackbar = snapshot.toObject(SystemFlow.CubeItSnackbar::class.java) ?: SystemFlow.CubeItSnackbar()
                                if(snackbar.showMessage){
                                    Log.d("snapshot_listener", "showing cubiet snackbar")
                                    SystemFlow.makeCubeItSnackbar(SystemFlow.currentGameActivity ?: context, snackbar.content, snackbar.durationMillis, snackbar.hasBackground, snackbar.realBackground, snackbar.realTextColor)
                                }
                            }
                        }
                    }

                    //inbox messages realtime listener
                    if(Data.inboxSnapshotHome == null){
                        Data.inbox.sortByDescending { it.sentTime }
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

                                inboxSnap.sortByDescending { it.sentTime }

                                if(snapshot.documents.size >= 1 && inboxSnap.size > 0 && inboxSnap != Data.inbox){
                                    for(i in inboxSnap){
                                        if(!Data.inbox.any { it.id == i.id }  && i.status != MessageStatus.Read){
                                            Data.inbox.add(i)
                                            if(i.status != MessageStatus.Sent){
                                                Toast.makeText(context, "New message has arrived.", Toast.LENGTH_SHORT).show()

                                                if(i.vibrate && Data.player.vibrateEffects){
                                                    val morseVibration = SystemFlow.translateIntoMorse(i.subject, null)
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createWaveform(morseVibration.timing.toLongArray(), morseVibration.amplitudes.toIntArray(), -1))
                                                    }else {
                                                        SystemFlow.vibrateAsError(context, 20)
                                                    }
                                                }else {
                                                    SystemFlow.vibrateAsError(context, 20)
                                                }

                                                Data.inboxChanged = true
                                                Data.inboxChangedMessages++
                                                context.runOnUiThread {
                                                    context.textViewHomeMailNew.visibility = View.VISIBLE
                                                    context.window.decorView.rootView.invalidate()
                                                }
                                            }
                                        }
                                    }
                                    SystemFlow.writeFileText(context, "inboxNew${Data.player.username}", "${Data.inboxChanged},${Data.inboxChangedMessages}")
                                }
                            }
                        }
                    }

                    if(Data.serverSnapshotHome == null){                                               //listens to every server status change
                        val docRef = db.collection("Server").document("Generic")
                        docRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                            if (snapshot != null && snapshot.exists()) {

                                if(snapshot.getString("Status") != "on"){
                                    val builder = AlertDialog.Builder(context)
                                    builder.setTitle("Server not available")
                                    builder.setMessage("Server is currently ${snapshot.getString("Status")}.\nWe apologize for any inconvenience.\n" + if(snapshot.getBoolean("ShowDsc")!!)snapshot.getString("ExternalDsc") else "")
                                    if(context.dialog == null) context.dialog = builder.create()
                                    context.dialog?.setCanceledOnTouchOutside(false)
                                    context.dialog?.setCancelable(false)

                                    context.dialog?.setButton(Dialog.BUTTON_POSITIVE, "OK") { dialogX, _ ->
                                        dialogX.dismiss()
                                    }
                                    context.dialog?.setOnDismissListener {
                                        if(context.dialog?.isShowing == false) Data.signOut(context) else context.dialog!!.dismiss()
                                    }
                                    if(context.dialog?.isShowing == false) context.dialog?.show()
                                }
                            }
                        }
                    }

                    if(Data.player.username == "Anonymous"){
                        SystemFlow.showNotification("Oops", "There's a problem with your game, we're really sorry.", context).setOnDismissListener {
                            Data.signOut(context)
                        }
                    }

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)&& Data.player.appearOnTop) {
                        //If the draw over permission is not available open the settings screen to grant the permission.

                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}"))
                        context.startActivityForResult(intent, 2084)
                    }

                    "true"
                }else "false"
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                val context = innerContext.get() as ActivityHome?

                if (result != null && result.toBoolean()){
                    //do something, my result is successful
                }else {
                    Toast.makeText(context, "Something went wrong! Try restarting your application", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var dialog: AlertDialog? = null
    var signOut: Boolean = false
    var STORY_LOBBY_CODE = 1000

    private val lifecycleListener: SystemFlow.LifecycleListener by lazy{
        SystemFlow.LifecycleListener(this)
    }

    private fun setupLifecycleListener() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleListener)
    }

    override fun onBackPressed() {
        if(signOut) Data.signOut(genericContext, true)
        else {
            Toast.makeText(genericContext, "Press back again to exit.", Toast.LENGTH_SHORT).show()
            signOut = true
            Handler().postDelayed({ signOut = false }, 2000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("onActivityResult", "requestCode: $requestCode, resultCode: $resultCode")
        when(resultCode){
            //story_lobby left swipe
            1001 -> {
                val intent = Intent(this, Activity_Story()::class.java)
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }
            //story_lobby right swipe
            1002 -> {
                val intent = Intent(this, Activity_Create_Story_Overview()::class.java)
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }
        }
    }

    @ExperimentalStdlibApi
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Data.uploadGlobalData()

        //Data.player.createFightRequest()

        Data.loggedInitialize(this)

        val tempActivity = this
        genericContext = this

        this.window.decorView.rootView.post {
            HomeInitialization(tempActivity).execute()

            setupLifecycleListener()

            imageViewHomeFactionNew.visibility = if(Data.factionLogChanged){
                View.VISIBLE
            }else View.GONE

            SystemFlow.GamePropertiesBar(this, null).attach()

            imageViewHomeFaction.setOnClickListener {
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
            var popUpMenuShown = false

            imageViewHomeExit.setOnTouchListener(object : Class_OnSwipeTouchListener(this, imageViewHomeExit, false) {            //disconnect swipe / open menu
                override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            imageViewExitLeave.setColorFilter(R.color.loginColor_2, PorterDuff.Mode.SRC_ATOP)
                            originalXExit = imageViewHomeExit.x
                            initialTouchExitX = motionEvent.rawX
                            clickableExit = true
                            Handler().postDelayed({clickableExit = false}, 100)
                            popUpMenuShown = false
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            imageViewHomeExit.x = originalXExit
                            imageViewExitLeave.x = originalXExit + imageViewExitLeave.width

                            if(clickableExit){
                                Handler().removeCallbacksAndMessages(null)

                                val wrapper = ContextThemeWrapper(this@ActivityHome, R.style.FactionPopupMenu)
                                val popup = PopupMenu(wrapper, imageViewHomeExit)
                                val popupMenu = popup.menu

                                popupMenu.add("Sign-out")
                                popupMenu.add("Play rocket game")

                                popup.setOnMenuItemClickListener {
                                    when(it.title){
                                        "Sign-out" -> {
                                            Data.signOut(this@ActivityHome)
                                            true
                                        }
                                        "Play rocket game" -> {
                                            val intentSplash = Intent(this@ActivityHome, Activity_Splash_Screen::class.java)
                                            Data.loadingScreenType = LoadingType.RocketGamePad
                                            SystemFlow.writeObject(this@ActivityHome, "loadingScreenType${Data.player.username}.data", Data.loadingScreenType)
                                            Data.loadingStatus = LoadingStatus.CLOSELOADING
                                            intentSplash.putExtra("keepLoading", true)
                                            this@ActivityHome.startActivity(intentSplash)
                                            true
                                        }
                                        else -> {
                                            true
                                        }
                                    }
                                }
                                if(!popUpMenuShown){
                                    Log.d("playComponentSound", "playing")
                                    popUpMenuShown = true
                                    SystemFlow.playComponentSound(this@ActivityHome)
                                    popup.show()
                                }
                            }else {
                                if(motionEvent.rawX <= originalXExit - imageViewExitLeave.width * 3){
                                    if(!imageViewHomeExit.isEnabled) return true
                                    imageViewHomeExit.isEnabled = false
                                    Data.signOut(this@ActivityHome)
                                    return true
                                }
                            }
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            imageViewHomeExit.x = ((originalXExit + (motionEvent.rawX - initialTouchExitX)/3))
                            imageViewExitLeave.x = ((originalXExit + imageViewExitLeave.width + ((motionEvent.rawX - initialTouchExitX))/3))

                            if(motionEvent.rawX <= originalXExit - imageViewExitLeave.width * 3){
                                imageViewExitLeave.setColorFilter(resources.getColor(R.color.leaderboard_first))
                            }else {
                                imageViewExitLeave.setColorFilter(resources.getColor(R.color.loginColor_2))
                            }
                            return true
                        }
                    }

                    return super.onTouch(view, motionEvent)
                }
            })

            imageViewHomeStory.setOnClickListener {
                imageViewHomeStory.isEnabled = false
                Handler().postDelayed({
                    imageViewHomeStory.isEnabled = true
                }, 150)

                val intent = Intent(this, Activity_Story_Lobby()::class.java)
                intent.putExtra("left", R.drawable.background1)
                intent.putExtra("right", R.drawable.background5)
                startActivityForResult(intent, STORY_LOBBY_CODE)
                this.overridePendingTransition(0,0)
            }

            imageViewHomeBoard.setOnClickListener{
                imageViewHomeBoard.isEnabled = false
                Handler().postDelayed({
                    imageViewHomeBoard.isEnabled = true
                }, 150)

                val intent = Intent(this, ActivityFightBoard::class.java)
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }
            imageViewHomeSkills.setOnClickListener{
                imageViewHomeSkills.isEnabled = false
                Handler().postDelayed({
                    imageViewHomeSkills.isEnabled = true
                }, 150)

                val intent = Intent(this, Spells()::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }
            imageViewHomeCharacter.setOnClickListener{
                imageViewHomeCharacter.isEnabled = false
                Handler().postDelayed({
                    imageViewHomeCharacter.isEnabled = true
                }, 150)

                val intent = Intent(this, Activity_Character::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }
            imageViewHomeSettings.setOnClickListener{
                imageViewHomeSettings.isEnabled = false
                Handler().postDelayed({
                    imageViewHomeSettings.isEnabled = true
                }, 150)

                val intent = Intent(this, ActivitySettings::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }
            imageViewHomeShop.setOnClickListener {
                imageViewHomeShop.isEnabled = false
                Handler().postDelayed({
                    imageViewHomeShop.isEnabled = true
                }, 150)

                val intent = Intent(this, Activity_Shop::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }
            imageViewHomeAdventure.setOnClickListener{
                imageViewHomeAdventure.isEnabled = false
                Handler().postDelayed({
                    imageViewHomeAdventure.isEnabled = true
                }, 150)

                val intent = Intent(this, ActivityAdventure::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }
        }

        Log.w("test_home", Data.spellClasses.size.toString())

        System.gc()
        val opts = BitmapFactory.Options()
        opts.inScaled = false
        layoutHome.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.homebackground, opts))
    }
}
