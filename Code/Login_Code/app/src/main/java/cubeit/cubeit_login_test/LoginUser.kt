package cubeit.cubeit_login_test

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import io.realm.*
import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.
import kotlinx.android.synthetic.main.activity_register.*
// Code by CubeIt / Max | 2019

/*
    To Olda: keep out!
    If you aren't Olda: contact me at fassinger[at]protonmail[dot]com I could use your help in securing this game, thanks :D
 */

class LoginUser : AppCompatActivity()  {

    val authURL: String = "https://cubeit-test.de1a.cloud.realm.io/auth" //static URL of Realm server (In final build this should be in a seperate file)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Realm.init(this)
        var Auth = FirebaseAuth.getInstance();
        val currentUser = Auth.currentUser


        var userEmail: String
        var userPassword: String

        buttonLogin.setOnClickListener { view ->
            userEmail = userEmailTextLogin.text.toString()
            userPassword = userPasswordTextLogin.text.toString()

            if (userEmail.isNotEmpty() && userPassword.isNotEmpty()){
                Auth.signInWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val user = Auth.currentUser
                                val builder = AlertDialog.Builder(this@LoginUser)
                                builder.setTitle("Success!")
                                builder.setMessage("User logged in")
                                val dialog: AlertDialog = builder.create()
                                dialog.show()
                            } else {
                                val builder = AlertDialog.Builder(this@LoginUser)
                                builder.setTitle("Oops!")
                                builder.setMessage("Incorrect Email or password")
                                val dialog: AlertDialog = builder.create()
                                dialog.show()
                            }
                        }
            }

        }

        resetPassword.setOnClickListener { view ->
            userEmail = userEmailTextLogin.text.toString()

            if (userEmail.isNotEmpty()){
                Auth!!.sendPasswordResetEmail(userEmail)
                val builder = AlertDialog.Builder(this@LoginUser)
                builder.setTitle("Done!")
                builder.setMessage("Sent password reset link to email")
                val dialog: AlertDialog = builder.create()
                dialog.show()

            }
            else {
                val builder = AlertDialog.Builder(this@LoginUser)
                builder.setTitle("Oops!")
                builder.setMessage("Please enter an email above")
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }

        userCreateButton.setOnClickListener { // Teleport to user register screen
            val intent = Intent(this, RegisterUser::class.java)
            startActivity(intent)
        }


}}


