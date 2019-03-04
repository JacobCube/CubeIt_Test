package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterUser : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        var player:Player = Player("Player", arrayOf(0,0,0,0,0,0,0,0,0,0), 10, 1, 40, 0, 0.0, 0, 0, 1050.0, 100, 1,
                10, mutableListOf(itemsClass1[0], itemsClass1[1], itemsClass1[2], itemsClass1[3], itemsClass1[4], itemsClass1[5]), arrayOfNulls(10),
                arrayOfNulls(2),mutableListOf(spellsClass1[0],spellsClass1[1],spellsClass1[2],spellsClass1[3],spellsClass1[4]) , mutableListOf(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
                arrayOfNulls(6), 100, arrayOfNulls(8), true)


        var Auth = FirebaseAuth.getInstance() // Initialize Firebase

        var userEmail: String
        var userPassword: String
        var username: String

        fun showPopUp(titleInput: String, textInput: String) {
            val builder = AlertDialog.Builder(this@RegisterUser)
            builder.setTitle(titleInput)
            builder.setMessage(textInput)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        fun registerUser(emailInput: String, passwordInput: String, usernameInput: String) {

            Auth.createUserWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = Auth!!.currentUser
                    user!!.sendEmailVerification()
                    showPopUp("Alert", "A confirmation email was sent!")

                    player?.createPlayer(Auth.currentUser!!.uid)
                    player?.username = usernameInput

                    showPopUp("Debug", "Username: " + player?.username)


                } else {
                    showPopUp("Error", "There was an error processing your request")
                }
            }
        }

        registerButton.setOnClickListener { view ->
            userEmail = userEmailText.text.toString()
            username = usernameEditText.text.toString()

            if (userPasswordText.text.toString() != "" && userPasswordTextConfirm.text.toString() != "" && userPasswordText.text.toString() == userPasswordTextConfirm.text.toString()) {
                userPassword = userPasswordText.text.toString()
                registerUser(userEmail, userPassword, username)
            } else {
                showPopUp("Alert", "Please enter a valid email address or password")
            }
        }
        accountExistsButton.setOnClickListener {
            // Teleport to user login screen
            val intent = Intent(this, LoginUser::class.java)
            startActivity(intent)
        }
    }
}