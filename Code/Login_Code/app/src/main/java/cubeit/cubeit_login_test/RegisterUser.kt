package cubeit.cubeit_login_test

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import io.realm.ObjectServerError
import io.realm.Realm
import io.realm.SyncCredentials
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_register.*
import java.util.regex.Pattern
// Code by CubeIt / Max | 2019

/*
    To Olda: keep out!
    If you aren't Olda: contact me at fassinger[at]protonmail[dot]com I could use your help in securing this game, thanks :D
 */

class RegisterUser : AppCompatActivity() {

    val authURL: String = "https://cubeit-test.de1a.cloud.realm.io/auth" //static URL of Realm server (In final build this should be in a separate file)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Realm.init(this) // Initialize realm
        var Auth = FirebaseAuth.getInstance(); // Initialize Firebase

        var userEmail: String
        var userPassword: String

        registerButton.setOnClickListener { view ->
            userEmail = userEmailText.text.toString()

            if (userPasswordText.text.toString().equals(userPasswordTextConfirm.text.toString()) && TOSCheckbox.isChecked == true) // Check if passwords match
            {
                userPassword = userPasswordText.text.toString()
                Auth.createUserWithEmailAndPassword(userEmail, userPassword)

                val user = Auth!!.currentUser
                user!!.sendEmailVerification()

                val builder = AlertDialog.Builder(this@RegisterUser)
                builder.setTitle("Alert")
                builder.setMessage("Confirmation Email sent!")
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            else {
                val builder = AlertDialog.Builder(this@RegisterUser)
                builder.setTitle("Oops!")
                builder.setMessage("Please enter a valid email and/or password and agree to the terms and conditions")
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

        }
        accountExistsButton.setOnClickListener { // Teleport to user login screen
            val intent = Intent(this, LoginUser::class.java)
            startActivity(intent)
        }

        // go to main game
    }
}