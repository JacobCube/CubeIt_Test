package cz.cubeit.cubeit


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

var player:Player = Player("MexxFM", arrayOf(0,0,0,0,0,0,0,0,0,0), 10, 1, 40, 0, 0.0, 0, 0, 1050.0, 100, 1,
        10, mutableListOf(itemsClass1[0], itemsClass1[1], itemsClass1[2], itemsClass1[3], itemsClass1[4], itemsClass1[5]), arrayOfNulls(10),
        arrayOfNulls(2),mutableListOf(spellsClass1[0],spellsClass1[1],spellsClass1[2],spellsClass1[3],spellsClass1[4]) , mutableListOf(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null),
        arrayOfNulls(6), 100, arrayOfNulls(8), true)



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


                                //player.createPlayer(Auth.currentUser!!.uid)     //should be checked, if user exists: create, if not, update

//                                player.loadPlayer()


                                var enemy = getRandomPlayer()

                                Log.d("LoginUser.kt", "enemy: " + enemy?.username)

                                val intent = Intent(this, Home::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                this.overridePendingTransition(0,0)

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


