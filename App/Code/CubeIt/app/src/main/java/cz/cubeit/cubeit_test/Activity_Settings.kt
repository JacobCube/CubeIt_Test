package cz.cubeit.cubeit_test

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_settings.*
import android.os.VibrationEffect
import android.text.method.PasswordTransformationMethod


class ActivitySettings : SystemFlow.GameActivity(R.layout.activity_settings, ActivityType.Settings, true, R.id.imageViewActivitySettings, R.color.colorSecondary){
    private val RC_SIGN_IN = 9001
    private var newPasswordShown = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                //updateUI(null)
                // [END_EXCLUDE]
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.overridePendingTransition(0,0)
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        switchNotificationsInbox.isChecked = Data.player.notificationsInbox
        switchNotificationsEvent.isChecked = Data.player.notificationsEvent
        switchNotificationsFaction.isChecked = Data.player.notificationsFaction
        switchVibrateEffects.isChecked = Data.player.vibrateEffects
        switchSettingsMusic.isChecked = Data.player.music
        switchAppearOnTop.isChecked = Data.player.appearOnTop

        seekBarSettingsTextSize.progress = (Data.player.textSize - 16f).toInt()
        textViewSettingsSeekBar.textSize = Data.player.textSize

        seekBarSettingsTextSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textViewSettingsSeekBar.textSize = (16f) + i.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Data.player.textSize = (16f) + seekBar.progress.toFloat()
                SystemFlow.writeFileText(this@ActivitySettings, "textSize${Data.player.username}.data", Data.player.textSize.toString())
            }
        })

        textViewSettingsTextFont.text = Data.player.textFont
        val gallery = Data.fontGallery.keys.toMutableList()
        var galleryCounter = gallery.indexOf(Data.player.textFont)

        imageViewSettingFontRight.setOnClickListener {
            if(++galleryCounter >= gallery.size) galleryCounter = 0
            textViewSettingsTextFont.text = gallery[galleryCounter]
            Data.player.textFont = gallery[galleryCounter]
            textViewSettingsTextFont.typeface = ResourcesCompat.getFont(this, Data.fontGallery[Data.player.textFont] ?: 0)
            textViewSettingsSeekBar.typeface = ResourcesCompat.getFont(this, Data.fontGallery[Data.player.textFont] ?: 0)
            SystemFlow.writeFileText(this@ActivitySettings, "textFont${Data.player.username}.data", Data.player.textFont)
        }

        switchSettingsMusic.setOnCheckedChangeListener { _, isChecked ->
            val svc = Intent(this, Data.bgMusic::class.java)
            if(isChecked){
                startService(svc)
            }else{
                stopService(svc)
                Data.bgMusic.pause()
            }
            Data.player.music = isChecked
        }
        switchSettingsSoundEffects.setOnCheckedChangeListener { _, isChecked ->
            Data.player.soundEffects = isChecked
        }

        if(Data.player.vibrationEasterEgg){
            buttonSettingsVibrate.visibility = View.VISIBLE
            editTextSettingsVibrate.visibility = View.VISIBLE
        }

        var easterEggCounter = 0
        switchVibrateEffects.setOnCheckedChangeListener { _, isChecked ->
            Data.player.vibrateEffects = isChecked
            SystemFlow.writeFileText(this, "vibrateEffect${Data.player.username}.data", isChecked.toString())

            if(isChecked){
                if(easterEggCounter >= 5 && !Data.player.vibrationEasterEgg){
                    buttonSettingsVibrate.visibility = View.VISIBLE
                    editTextSettingsVibrate.visibility = View.VISIBLE
                    Data.player.vibrationEasterEgg = true
                    Snackbar.make(switchVibrateEffects, "Morse vibrations easter egg unlocked!", Snackbar.LENGTH_LONG).show()
                }else {
                    easterEggCounter++
                }

                val v = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v!!.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    v!!.vibrate(50)
                }
            }
        }

        imageViewSettingsGoogleSignIn.setOnClickListener{
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = mGoogleSignInClient!!.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        var morseVibration = SystemFlow.translateIntoMorse("")

        buttonSettingsVibrate.setOnTouchListener(object: Class_HoldTouchListener(buttonSettingsVibrate, false, 0f, false){

            override fun onStartHold(x: Float, y: Float) {
                super.onStartHold(x, y)
                morseVibration = SystemFlow.translateIntoMorse(editTextSettingsVibrate.text.toString(), textViewSettingsVibrateLog)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v!!.vibrate(VibrationEffect.createWaveform(morseVibration.timing.toLongArray(), morseVibration.amplitudes.toIntArray(), -1))
                }
            }

            override fun onCancelHold() {
                super.onCancelHold()
                morseVibration.detachHandlers()
                textViewSettingsVibrateLog.text = ""
                if(v!!.hasVibrator()) v.cancel()
            }
        })

        imageViewSettingsChangePasswordShow.setOnClickListener {
            editTextSettingNewPassword.transformationMethod = if(newPasswordShown){
                newPasswordShown = false
                null
            }else {
                newPasswordShown = true
                PasswordTransformationMethod()
            }
        }

        buttonSettingChangePassword.setOnClickListener{
            if(editTextSettingNewPassword.visibility != View.VISIBLE) editTextSettingNewPassword.visibility = View.VISIBLE
            if(buttonSettingChangePasswordOk.visibility != View.VISIBLE) buttonSettingChangePasswordOk.visibility = View.VISIBLE
            if(textViewSettingNewPasswordReq.visibility != View.VISIBLE) textViewSettingNewPasswordReq.visibility = View.VISIBLE
            if(imageViewSettingsChangePasswordShow.visibility != View.VISIBLE) imageViewSettingsChangePasswordShow.visibility = View.VISIBLE

            buttonSettingChangePasswordOk.setOnClickListener {
                buttonSettingChangePasswordOk.isEnabled = false

                if(!editTextSettingNewPassword.text.isNullOrEmpty()){

                    val pass = editTextSettingNewPassword.text.toString()
                    if(pass.length > 7 && pass.contains("\\d+".toRegex()) /*&& pass.contains("[A-Z ]+".toRegex())*/){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            textViewSettingNewPasswordReq.setTextColor(getColor(R.color.loginColor_2))
                        }else {
                            textViewSettingNewPasswordReq.setTextColor(resources.getColor(R.color.loginColor_2))
                        }

                        Data.player.userSession.updatePassword(editTextSettingNewPassword.text.toString()).addOnCompleteListener {
                            buttonSettingChangePasswordOk.isEnabled = true
                            if(it.isSuccessful){
                                Snackbar.make(editTextSettingNewPassword, "Password successfully changed!", Snackbar.LENGTH_SHORT).show()

                                editTextSettingNewPassword.visibility = View.GONE
                                buttonSettingChangePasswordOk.visibility = View.GONE
                                textViewSettingNewPasswordReq.visibility = View.GONE
                            }else {
                                Snackbar.make(editTextSettingNewPassword, it.exception.toString(), Snackbar.LENGTH_SHORT).show()
                            }
                            editTextSettingNewPassword.setText("")
                        }
                    }else {
                        SystemFlow.vibrateAsError(this)
                        buttonSettingChangePasswordOk.isEnabled = true
                        textViewSettingNewPasswordReq.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_shaky_short_vertical))
                        Snackbar.make(editTextSettingNewPassword, "Not allowed!", Snackbar.LENGTH_SHORT).show()
                        textViewSettingNewPasswordReq.setTextColor(Color.WHITE)
                    }
                }else {
                    SystemFlow.vibrateAsError(this)
                    buttonSettingChangePasswordOk.isEnabled = true
                    editTextSettingNewPassword.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_shaky_short))
                    Snackbar.make(editTextSettingNewPassword, "Enter new password.", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        switchAppearOnTop.setOnCheckedChangeListener { _, isChecked ->
            Data.player.appearOnTop = isChecked
        }

        switchNotificationsInbox.setOnCheckedChangeListener { _, isChecked ->
            Data.player.notificationsInbox = isChecked
        }

        switchNotificationsFaction.setOnCheckedChangeListener{ _, isChecked ->
            Data.player.notificationsFaction = isChecked
        }

        switchNotificationsEvent.setOnCheckedChangeListener{_, isChecked ->
            Data.player.notificationsEvent = isChecked
        }

        imageViewSettingsBugIcon.layoutParams.height = (dm.heightPixels / 10 * 1.8).toInt()
        imageViewSettingsBugIcon.layoutParams.width = (dm.heightPixels / 10 * 1.8).toInt()
        imageViewSettingsBugIcon.y = 0f
        frameLayoutBugReport.layoutParams.height = (dm.heightPixels * 0.82 - imageViewSettingsBugIcon.layoutParams.height).toInt()
        frameLayoutBugReport.y =  0f - frameLayoutBugReport.layoutParams.height

        imageViewSettingsBugIcon.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutBugReport, Fragment_Bug_report()).commitNow()

            if(imageViewSettingsBugIcon.y == (dm.heightPixels * 0.82 - imageViewSettingsBugIcon.layoutParams.height).toFloat()){
                ValueAnimator.ofFloat(imageViewSettingsBugIcon.y, 0f    /*imageViewBugIcon.layoutParams.width.toFloat()*/).apply{
                    duration = 800
                    addUpdateListener {
                        imageViewSettingsBugIcon.y = it.animatedValue as Float  //- imageViewBugIcon.layoutParams.width
                        frameLayoutBugReport.y = it.animatedValue as Float - frameLayoutBugReport.layoutParams.height
                    }
                    start()
                }
            }else{
                ValueAnimator.ofFloat(imageViewSettingsBugIcon.y, (dm.heightPixels * 0.82 - imageViewSettingsBugIcon.layoutParams.height).toFloat() /*- imageViewBugIcon.layoutParams.width*/).apply{
                    duration = 800
                    addUpdateListener {
                        imageViewSettingsBugIcon.y = it.animatedValue as Float  //- imageViewBugIcon.layoutParams.width
                        frameLayoutBugReport.y = it.animatedValue as Float - frameLayoutBugReport.layoutParams.height
                    }
                    start()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        Data.player.userSession.linkWithCredential(credential).addOnCompleteListener {
            Snackbar.make(editTextSettingNewPassword, if (it.isSuccessful) {
                "Account successfully linked!"
            } else {
                it.exception!!.message.toString()
            }, Snackbar.LENGTH_SHORT).show()
        }
    }
}
