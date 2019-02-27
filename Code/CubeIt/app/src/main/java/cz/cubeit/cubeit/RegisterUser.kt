package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_register.*
import cz.cubeit.cubeit.R

class RegisterUser : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        var Auth = FirebaseAuth.getInstance() // Initialize Firebase

        var userEmail: String
        var userPassword: String

        fun showNotification(titleInput: String, textInput: String) {
            val builder = AlertDialog.Builder(this@RegisterUser)
            builder.setTitle(titleInput)
            builder.setMessage(textInput)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        fun registerUser(emailInput: String, passwordInput: String) {

            Auth.createUserWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = Auth!!.currentUser
                    user!!.sendEmailVerification()
                    showNotification("Alert", "A confirmation email was sent!")
                    val player = player

                } else {
                    showNotification("Error", "There was an error processing your request")
                }
            }
        }

        registerButton.setOnClickListener { view ->
            userEmail = userEmailText.text.toString()

            if (userPasswordText.text.toString() != "" && userPasswordTextConfirm.text.toString() != "" && userPasswordText.text.toString() == userPasswordTextConfirm.text.toString()) {
                userPassword = userPasswordText.text.toString()
                registerUser(userEmail, userPassword)
            } else {
                showNotification("Alert", "Please enter a valid email address or password")
            }
        }
        accountExistsButton.setOnClickListener {
            // Teleport to user login screen
            val intent = Intent(this, LoginUser::class.java)
            startActivity(intent)
        }
    }
}