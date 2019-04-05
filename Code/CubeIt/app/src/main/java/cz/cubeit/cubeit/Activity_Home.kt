package cz.cubeit.cubeit

import android.app.ProgressDialog
import android.arch.lifecycle.Lifecycle
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.BroadcastReceiver
import android.provider.Settings
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast

var playedSong = R.raw.playedsong

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val service = Intent(context, Home()::class.java)
        service.putExtra("reason", intent.getStringExtra("reason"))
        service.putExtra("timestamp", intent.getLongExtra("timestamp", 0))

        context.startService(service)
    }
}


class Home : AppCompatActivity() {

    private val lifecycleListener: SampleLifecycleListener by lazy{
        SampleLifecycleListener(this)
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
        super.onBackPressed()
        null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_home)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        layoutHome.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.homebackground, opts))


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)&& player.appearOnTop) {
                //If the draw over permission is not available open the settings screen
                //to grant the permission.
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

        imageViewExit.setOnClickListener {
            val progress = ProgressDialog(this)
            progress.setTitle("Loading")
            progress.setMessage("Logging out...")
            progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
            progress.show()

            player.online = false
            player.toLoadPlayer().uploadPlayer().addOnCompleteListener {
                val svc = Intent(this, BackgroundSoundService()::class.java)
                stopService(svc)

                progress.dismiss()

                val intent = Intent(this, cz.cubeit.cubeit.ActivityLoginRegister()::class.java)
                startActivity(intent)
                this.overridePendingTransition(0,0)
            }
        }

        Hatch.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.ActivityFightBoard::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Skills.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.Spells()::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
        Character.setOnClickListener{
            val intent = Intent(this, cz.cubeit.cubeit.Character::class.java)
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
            val intent = Intent(this, cz.cubeit.cubeit.ActivityShop::class.java)
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