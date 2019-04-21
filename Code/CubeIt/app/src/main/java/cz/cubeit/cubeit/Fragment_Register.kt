package cz.cubeit.cubeit

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val Auth = FirebaseAuth.getInstance()
        var userPassword: String

        fun showNotification(titleInput: String, textInput: String) {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle(titleInput)
            builder.setMessage(textInput)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        val progress = ProgressDialog(view.context)
        progress.setTitle("Loading")
        progress.setMessage("We're checking if you're subscribed to PewDiePie, just a moment...")
        progress.setCancelable(false) // disable dismiss by tapping outside of the dialogv

        fun registerUser(passwordInput: String) {
            val tempPlayer = Player()
            progress.show()

            val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

            Auth.createUserWithEmailAndPassword(view.inputEmailReg.text.toString(), passwordInput).addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = Auth!!.currentUser
                    user!!.sendEmailVerification()
                    Toast.makeText(view.context, "Please confirm your account by clicking on the link sent to your email address!", Toast.LENGTH_SHORT).show()

                    tempPlayer.username = view.inputUsernameReg.text.toString()

                    tempPlayer.toLoadPlayer().createPlayer(Auth.currentUser!!.uid, view.inputUsernameReg.text.toString()).addOnCompleteListener {
                        player.username = view.inputUsernameReg.text.toString()
                        player.loadPlayer().addOnCompleteListener {
                            val intent = Intent(view.context, Activity_Character_Customization(view.inputUsernameReg.text.toString(), view.inputUsernameReg.text.toString())::class.java)
                            startActivity(intent)
                            //Activity().overridePendingTransition(R.anim.animation_character_customization,R.anim.animation_character_customization)
                        }
                    }

                }
                if (!isConnected){
                    showNotification("Error", "Your device isn't connected to the internet. Please check your connection and try again.")
                }
                if (!task.isSuccessful){
                    try {
                        showNotification("Oops", "${exceptionFormatter(task.result.toString())}")
                    }
                    catch (e:Exception){
                        showNotification("Oops", "An account with this email already exists!")
                    }
                }
                else {
//                    showNotification("Oops", "An unknown error has occurred, please try again later or contact support.")
                }
                progress.dismiss()
            }
        }

        view.buttonRegister.setOnClickListener {

            if (view.inputEmailReg.text.isNotEmpty() && view.inputUsernameReg.text.isNotEmpty() && view.inputPassReg.text.isNotEmpty() && view.inputRePassReg.text.isNotEmpty() && view.inputPassReg.text.toString() == view.inputRePassReg.text.toString()) {
                userPassword = view.inputPassReg.text.toString()
                registerUser(userPassword)
            }
            else {
                if (inputPassReg.text.toString() != inputRePassReg.text.toString()){
                    showNotification("Oops", "Passwords must match")
                }
                else {
                    showNotification("Oops", "Please enter a valid email address or password")
                }
            }

        }

        return view
    }
}