package cz.cubeit.cubeit

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
import android.content.DialogInterface



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
                Log.d("Server listener", "$source data: ${snapshot.data}")

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

        imageViewHomeInbox.setOnClickListener {
            val intent = Intent(this, Activity_Inbox()::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }

        imageViewExit.setOnClickListener {
            Data.logOut(this)
        }

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
