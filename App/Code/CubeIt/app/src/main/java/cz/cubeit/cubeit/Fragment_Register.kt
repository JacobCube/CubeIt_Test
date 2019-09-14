package cz.cubeit.cubeit

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import java.lang.Exception

class Fragment_Register : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_register, container, false)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.layoutRegister.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.register_bg, opts))

        val Auth = FirebaseAuth.getInstance()                                       // Initialize Firebase
        var userPassword: String

        fun showNotification(titleInput: String, textInput: String) {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle(titleInput)
            builder.setMessage(textInput)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        view.buttonRegister.setOnClickListener {
            val splashScreen = Activity_Splash_Screen()
            val intentSplash = Intent(view.context, splashScreen::class.java)
            Data.loadingStatus = LoadingStatus.LOGGING
            val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true

            Data.loadingStatus = LoadingStatus.LOGGING

            if (isConnected) {

                if (view.inputUsernameReg.text!!.isNotBlank() && view.inputUsernameReg.text!!.length < 13 && view.inputUsernameReg.text!!.length > 5) {

                    if (view.inputEmailReg.text!!.isNotBlank()) {

                        if (view.inputPassReg.text!!.isNotBlank()) {

                            val pass = view.inputPassReg.text.toString()
                            if(pass.length > 7 && pass.contains("\\d+".toRegex()) /*&& pass.contains("[A-Z ]+".toRegex())*/){

                                if (inputPassReg.text.toString() == inputRePassReg.text.toString()){

                                    startActivity(intentSplash)

                                    Data.loadGlobalData(view.context).addOnSuccessListener {
                                        if (GenericDB.AppInfo.appVersion > BuildConfig.VERSION_CODE){
                                            Activity_Splash_Screen().closeLoading()
                                            handler.postDelayed({showNotification("Error", "Your version is too old, download more recent one. (Alpha, versioned ${GenericDB.AppInfo.appVersion})")},100)
                                        }

                                        if (view.inputEmailReg.text!!.isNotEmpty() && view.inputUsernameReg.text!!.isNotEmpty() && view.inputPassReg.text!!.isNotEmpty() && view.inputRePassReg.text!!.isNotEmpty() && view.inputPassReg.text.toString() == view.inputRePassReg.text.toString() && GenericDB.AppInfo.appVersion <= BuildConfig.VERSION_CODE && isConnected) {
                                            userPassword = view.inputPassReg.text.toString()

                                            Activity_Splash_Screen().setLogText(resources.getString(R.string.loading_log, "Your profile information"))

                                            Auth.createUserWithEmailAndPassword(view.inputEmailReg.text.toString(), userPassword).addOnCompleteListener{ task: Task<AuthResult> ->
                                                if (task.isSuccessful) {
                                                    val user = Auth.currentUser
                                                    user!!.sendEmailVerification()
                                                    Toast.makeText(view.context, "Please confirm your account by clicking on the link sent to your email address!", Toast.LENGTH_SHORT).show()

                                                    val tempPlayer = Player()
                                                    tempPlayer.username = view.inputUsernameReg.text.toString()
                                                    //tempPlayer.userSession = user

                                                    tempPlayer.createPlayer(Auth.currentUser!!.uid, view.inputUsernameReg.text.toString()).addOnSuccessListener {
                                                        Data.player.username = view.inputUsernameReg.text.toString()
                                                        Data.loadingStatus = LoadingStatus.REGISTERED
                                                        //Activity().overridePendingTransition(R.anim.animation_character_customization,R.anim.animation_character_customization)
                                                    }

                                                }else {
                                                    try {
                                                        showNotification("Oops", SystemFlow.exceptionFormatter(task.result.toString()))
                                                    }
                                                    catch (e:Exception){
                                                        showNotification("Oops", "An account with this email already exists!")
                                                    }
                                                    Activity_Splash_Screen().closeLoading()
                                                }
                                            }
                                        } else {
                                            Activity_Splash_Screen().closeLoading()
                                            showNotification("Alert", "Please enter a valid email address or password")
                                        }
                                    }
                                }else {
                                    view.inputPassReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                    view.inputRePassReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                    Toast.makeText(view.context, "Passwords must match!", Toast.LENGTH_SHORT).show()
                                }
                            }else {
                                view.inputPassReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                                Toast.makeText(view.context, "Entered password is not valid!", Toast.LENGTH_SHORT).show()
                            }
                        }else {
                            view.inputPassReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                            Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                        }
                    }else {
                        view.inputEmailReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                        Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                    }
                }else {
                    view.inputUsernameReg.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                    Toast.makeText(view.context, "This field is required!", Toast.LENGTH_SHORT).show()
                }
            }else {
                view.buttonRegister.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.animation_shaky_short))
                handler.postDelayed({showNotification("Error", "Your device is not connected to the internet. Please check your connection and try again.")},50)
            }
        }

        return view
    }
}