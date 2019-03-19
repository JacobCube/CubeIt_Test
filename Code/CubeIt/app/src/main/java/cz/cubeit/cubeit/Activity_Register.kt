package cz.cubeit.cubeit

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_register.*

class Activity_Register : AppCompatActivity() {

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_register)

        val Auth = FirebaseAuth.getInstance() // Initialize Firebase
        var userPassword: String

        fun showPopUp(titleInput: String, textInput: String) {
            val builder = AlertDialog.Builder(this@Activity_Register)
            builder.setTitle(titleInput)
            builder.setMessage(textInput)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        fun registerUser(passwordInput: String) {


            val tempPlayer = Player("Player", arrayOf(0,0,0,0,0,0,0,0,0,0), 10, 1, 40, 0, 0.0, 0, 0, 1050.0, 100, 1,
                    8, mutableListOf(itemsClass1[0], itemsClass1[1], itemsClass1[2], itemsClass1[3], itemsClass1[4], itemsClass1[5]), arrayOfNulls(10),
                    arrayOfNulls(2),mutableListOf(spellsClass1[0],spellsClass1[1],spellsClass1[2],spellsClass1[3],spellsClass1[4]) , mutableListOf(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
                    arrayOfNulls(6), 100, arrayOf(ActivityShop.getItemOffer(player),ActivityShop.getItemOffer(player),ActivityShop.getItemOffer(player),ActivityShop.getItemOffer(player),ActivityShop.getItemOffer(player),ActivityShop.getItemOffer(player),ActivityShop.getItemOffer(player),ActivityShop.getItemOffer(player)), true, true)

            Log.d("TEMPPLAYER", "${tempPlayer.shopOffer}")
            Auth.createUserWithEmailAndPassword(userEmailText.text.toString(), passwordInput).addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = Auth!!.currentUser
                    user!!.sendEmailVerification()
                    Toast.makeText(this, "A confirmation email was sent!", Toast.LENGTH_SHORT).show()

                    tempPlayer.username = userUsernameText.text.toString()
                    tempPlayer.createPlayer(Auth.currentUser!!.uid, userUsernameText.text.toString())

                    val intent = Intent(this, cz.cubeit.cubeit.ActivityLogin::class.java)
                    startActivity(intent)
                    this.overridePendingTransition(0,0)
                } else {
                    showPopUp("Error", "There was an error processing your request")
                }
            }
        }

        registerButton.setOnClickListener { view ->

            if (userPasswordText.text.toString() != "" && userPasswordTextConfirm.text.toString() != "" && userPasswordText.text.toString() == userPasswordTextConfirm.text.toString()) {
                userPassword = userPasswordText.text.toString()
                registerUser(userPassword)
            } else {
                showPopUp("Alert", "Please enter a valid email address or password")
            }
        }
        accountExistsButton.setOnClickListener {
            // Teleport to user login screen
            val intent = Intent(this, ActivityLogin::class.java)
            startActivity(intent)
            this.overridePendingTransition(0,0)
        }
    }
}