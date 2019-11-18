package cz.cubeit.cubeit_test

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import java.lang.Exception

class Fragment_Register : Fragment() {
    lateinit var tempView: View
    private var passwordShown = false

    override fun onDestroy() {
        super.onDestroy()
        System.gc()
        tempView.imageViewRegisterIcon.setImageResource(0)
        tempView.imageViewRegisterForm.setImageResource(0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_register, container, false)
        tempView = view

        view.post {
            System.gc()
            val opts = BitmapFactory.Options()
            opts.inScaled = false
            view.imageViewRegisterIcon.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.icon_cubeit_login, opts))
            view.imageViewRegisterForm.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.register_window, opts))
        }

        val auth = FirebaseAuth.getInstance()                                       // Initialize Firebase
        var userPassword: String

        fun showNotification(titleInput: String, textInput: String) {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle(titleInput)
            builder.setMessage(textInput)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        view.imageViewRegisterShowPass.setOnClickListener{
            if(passwordShown){
                passwordShown = false
                view.inputPassReg.transformationMethod = null
                view.inputRePassReg.transformationMethod = null

            }else {
                passwordShown = true
                view.inputPassReg.transformationMethod = PasswordTransformationMethod()
                view.inputRePassReg.transformationMethod = PasswordTransformationMethod()
            }
        }

        view.buttonRegister.setOnClickListener {
            val intentSplash = Intent(view.context, Activity_Splash_Screen::class.java)
            Data.loadingStatus = LoadingStatus.LOGGING
            val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true

            Data.loadingStatus = LoadingStatus.LOGGING

            if (isConnected) {

                if (view.inputUsernameReg.text.toString().isNotBlank() && view.inputUsernameReg.text!!.length < 16 && view.inputUsernameReg.text!!.length > 5 && view.inputUsernameReg.text.toString().toLowerCase() != "player") {

                    if (view.inputEmailReg.text.toString().isNotBlank()) {

                        if(view.inputEmailReg.text.toString().isEmail()){

                            if (view.inputPassReg.text.toString().isNotBlank()) {

                                val pass = view.inputPassReg.text.toString()
                                if(pass.length > 7 && pass.contains("\\d+".toRegex()) /*&& pass.contains("[A-Z ]+".toRegex())*/){

                                    if (inputPassReg.text.toString() == inputRePassReg.text.toString()){

                                        startActivity(intentSplash)

                                        Data.loadGlobalData(view.context).addOnSuccessListener {
                                            if (GenericDB.AppInfo.appVersion > BuildConfig.VERSION_CODE){
                                                Data.loadingStatus = LoadingStatus.CLOSELOADING
                                                Handler().postDelayed({showNotification("Error", "Your version is too old, download more recent one. (Alpha, versioned ${GenericDB.AppInfo.appVersion})")},100)
                                            }

                                            val textView = textViewLog?.get()
                                            if (view.inputEmailReg.text!!.isNotEmpty() && view.inputUsernameReg.text!!.isNotEmpty() && view.inputPassReg.text!!.isNotEmpty() && view.inputRePassReg.text!!.isNotEmpty() && view.inputPassReg.text.toString() == view.inputRePassReg.text.toString() && GenericDB.AppInfo.appVersion <= BuildConfig.VERSION_CODE && isConnected) {
                                                userPassword = view.inputPassReg.text.toString()

                                                textView?.text = resources.getString(R.string.loading_log, "Your profile information")

                                                auth.createUserWithEmailAndPassword(view.inputEmailReg.text.toString(), userPassword).addOnCompleteListener{ task: Task<AuthResult> ->
                                                    if (task.isSuccessful) {
                                                        val user = auth.currentUser

                                                        user!!.sendEmailVerification()
                                                        Snackbar.make(view, "Please confirm your account by clicking on the link sent to your email address!", Snackbar.LENGTH_SHORT).show()

                                                        val tempPlayer = Player()
                                                        tempPlayer.username = view.inputUsernameReg.text.toString()
                                                        //tempPlayer.userSession = user

                                                        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(view.inputUsernameReg.text.toString()).build()).continueWithTask {
                                                            tempPlayer.createPlayer(auth.currentUser!!.uid, view.inputUsernameReg.text.toString()).addOnSuccessListener {
                                                                Data.player.username = view.inputUsernameReg.text.toString()
                                                                Data.loadingStatus = LoadingStatus.REGISTERED
                                                                //Activity().overridePendingTransition(R.anim.animation_character_customization,R.anim.animation_character_customization)
                                                            }
                                                        }

                                                    }else {
                                                        try {
                                                            showNotification("Oops", SystemFlow.exceptionFormatter(task.result.toString()))
                                                        }
                                                        catch (e:Exception){
                                                            showNotification("Oops", "An account with this email already exists!")
                                                        }
                                                        Data.loadingStatus = LoadingStatus.CLOSELOADING
                                                    }
                                                }

                                            } else {
                                                Data.loadingStatus = LoadingStatus.CLOSELOADING
                                            }
                                        }
                                    }else {
                                        SystemFlow.vibrateAsError(view.context)
                                        view.inputPassReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                        view.inputRePassReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                        Snackbar.make(view, "Passwords must match!", Snackbar.LENGTH_SHORT).show()
                                    }
                                }else {
                                    SystemFlow.vibrateAsError(view.context)
                                    view.inputPassReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                    Snackbar.make(view, "Entered password is not valid!", Snackbar.LENGTH_SHORT).show()
                                }
                            }else {
                                SystemFlow.vibrateAsError(view.context)
                                view.inputPassReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                Snackbar.make(view, "Field required!", Snackbar.LENGTH_SHORT).show()
                            }
                        }else {
                            SystemFlow.vibrateAsError(view.context)
                            view.inputEmailReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                            Snackbar.make(view, "Not valid email!", Snackbar.LENGTH_SHORT).show()
                        }
                    }else {
                        SystemFlow.vibrateAsError(view.context)
                        view.inputEmailReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                        Snackbar.make(view, "Field required!", Snackbar.LENGTH_SHORT).show()
                    }
                }else {
                    SystemFlow.vibrateAsError(view.context)
                    view.inputUsernameReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                    Snackbar.make(view, "Not allowed!", Snackbar.LENGTH_SHORT).show()
                }
            }else {
                view.buttonRegister.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                Handler().postDelayed({Snackbar.make(view, "Your device is not connected to the internet. Please check your connection and try again.", Snackbar.LENGTH_SHORT).show()},50)
            }
        }

        return view
    }
}