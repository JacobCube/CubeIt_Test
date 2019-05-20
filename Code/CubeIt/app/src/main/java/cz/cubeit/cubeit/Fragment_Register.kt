package cz.cubeit.cubeit

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.fragment_login.*
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
            val intentSplash = Intent(view.context, Activity_Splash_Screen::class.java)
            loadedLogin = LoginStatus.LOGGING
            val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

            startActivity(intentSplash)
            loadedLogin = LoginStatus.LOGGING

            if (!isConnected){
                loadedLogin = LoginStatus.CLOSELOADING
                handler.postDelayed({showNotification("Error", "Your device is not connected to the internet. Please check your connection and try again.")},100)
            }
            if (inputPassReg.text.toString() != inputRePassReg.text.toString()){
                loadedLogin = LoginStatus.CLOSELOADING
                handler.postDelayed({showNotification("Oops", "Passwords must match")},100)
            }

            loadGlobalData(view.context).addOnCompleteListener {
                if (appVersion < BuildConfig.VERSION_CODE){
                    loadedLogin = LoginStatus.CLOSELOADING
                    handler.postDelayed({showNotification("Oops", "Your version is too old, download more recent one.")},100)
                }

                if (view.inputEmailReg.text.isNotEmpty() && view.inputUsernameReg.text.isNotEmpty() && view.inputPassReg.text.isNotEmpty() && view.inputRePassReg.text.isNotEmpty() && view.inputPassReg.text.toString() == view.inputRePassReg.text.toString() && appVersion <= BuildConfig.VERSION_CODE && isConnected) {
                    userPassword = view.inputPassReg.text.toString()

                    Auth.createUserWithEmailAndPassword(view.inputEmailReg.text.toString(), userPassword).addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            if(textViewLog!= null){
                                textViewLog!!.text = resources.getString(R.string.loading_log, "Your profile information")
                            }

                            val user = Auth!!.currentUser
                            user!!.sendEmailVerification()
                            Toast.makeText(view.context, "Please confirm your account by clicking on the link sent to your email address!", Toast.LENGTH_SHORT).show()

                            val tempPlayer = Player()
                            tempPlayer.username = view.inputUsernameReg.text.toString()

                            tempPlayer.toLoadPlayer().createPlayer(Auth.currentUser!!.uid, view.inputUsernameReg.text.toString()).addOnCompleteListener {
                                player.username = view.inputUsernameReg.text.toString()
                                player.loadPlayer().addOnCompleteListener {
                                    val intent = Intent(view.context, Activity_Character_Customization::class.java)
                                    startActivity(intent)
                                    //Activity().overridePendingTransition(R.anim.animation_character_customization,R.anim.animation_character_customization)
                                }
                            }

                        }else {
                            try {
                                showNotification("Oops", exceptionFormatter(task.result.toString()))
                            }
                            catch (e:Exception){
                                showNotification("Oops", "An account with this email already exists!")
                            }
                        }
                        loadedLogin = LoginStatus.CLOSELOADING
                    }
                } else {
                    loadedLogin = LoginStatus.CLOSELOADING
                    showNotification("Alert", "Please enter a valid email address or password")
                }
            }
        }

        return view
    }
}