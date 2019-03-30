package cz.cubeit.cubeit


import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.view.*

var player:Player = Player()

class ActivityLogin : Fragment()  {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_login, container, false)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.layoutLogin.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.login_bg, opts))

        view.inputUsernameLogin.setText(arguments?.getString("loginUsername"))
        view.inputEmailLogin.setText(arguments?.getString("loginEmail"))

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        var userEmail: String
        var userPassword: String

        fun showNotification(titleInput: String, textInput: String){
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle(titleInput)
            builder.setMessage(textInput)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }


        view.buttonLogin.setOnClickListener {
            val progress = ProgressDialog(view.context)
            progress.setTitle("Loading")
            progress.setMessage("We are checking if you're subscribed to PewDiePie or not, sorry for interruption")
            progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
            progress.show()

            userEmail = view.inputEmailLogin.text.toString()
            userPassword = view.inputPassLogin.text.toString()

            if (userEmail.isNotEmpty() && userPassword.isNotEmpty()){
                auth.signInWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(Activity()) { task ->
                            if (task.isSuccessful) {

                                val user = auth.currentUser

                                player.userSession = user!!

                                player.username = view.inputUsernameLogin.text.toString()

                                player.loadPlayer().addOnCompleteListener {
                                    if(player.newPlayer){
                                        val intent = Intent(view.context, Activity_Character_Customization::class.java)
                                        startActivity(intent)
                                    }else {
                                        player.online = true
                                        player.toLoadPlayer().uploadSingleItem("online").addOnCompleteListener {
                                            val intent = Intent(view.context, Home::class.java)
                                            startActivity(intent)
                                        }
                                    }
                                }

                            } else {
                                showNotification("Oops", "Please enter a valid email or password")
                            }
                        }
                progress.dismiss()
            }
        }

        view.resetPass.setOnClickListener {
            userEmail = view.inputEmailLogin.text.toString()

            if (userEmail.isNotEmpty()){
                auth!!.sendPasswordResetEmail(userEmail)
                showNotification("Alert", "A password reset link was sent to the above email account")
            }
            else {
                showNotification("Oops", "Please enter an email above")
            }
        }
        return view
    }
}


