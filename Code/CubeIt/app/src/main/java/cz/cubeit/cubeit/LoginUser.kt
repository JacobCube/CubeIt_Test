package cz.cubeit.cubeit


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*


class LoginUser : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val Auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        var userEmail: String
        var userPassword: String

        fun showNotification(titleInput: String, textInput: String){
            val builder = AlertDialog.Builder(this@LoginUser)
            builder.setTitle(titleInput)
            builder.setMessage(textInput)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }


        buttonLogin.setOnClickListener { view ->
            userEmail = userEmailTextLogin.text.toString()
            userPassword = userPasswordTextLogin.text.toString()

            if (userEmail.isNotEmpty() && userPassword.isNotEmpty()){
                Auth.signInWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {

                                var user = Auth.currentUser
                                player.userSession = user!!


                                player.createPlayer(Auth.currentUser!!.uid)

                                player.loadPlayer()

                                showNotification("user ${player.username}", "His level is ${player.level}")



                            } else {
                                showNotification("Oops", "Please enter a valid email or password")
                            }
                        }
            }
        }

        resetPassword.setOnClickListener { view ->
            userEmail = userEmailTextLogin.text.toString()

            if (userEmail.isNotEmpty()){
                Auth!!.sendPasswordResetEmail(userEmail)
                showNotification("Alert", "A password reset link was sent to the above email account")
            }
            else {
                showNotification("Oops", "Please enter an email above")
            }
        }

        userCreateButton.setOnClickListener {
            // Teleport to user register screen
            val intent = Intent(this, RegisterUser::class.java)
            startActivity(intent)
        }
    }
}


