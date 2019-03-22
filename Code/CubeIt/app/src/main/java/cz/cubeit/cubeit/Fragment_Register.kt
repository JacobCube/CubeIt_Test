package cz.cubeit.cubeit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
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

class Fragment_Register : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_register, container, false)

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.layoutRegister.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.register_bg, opts))

        val Auth = FirebaseAuth.getInstance()                                       // Initialize Firebase
        var userPassword: String

        fun showPopUp(titleInput: String, textInput: String) {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle(titleInput)
            builder.setMessage(textInput)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        fun registerUser(passwordInput: String) {
            val tempPlayer = Player()

            Auth.createUserWithEmailAndPassword(view.inputEmailReg.text.toString(), passwordInput).addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = Auth!!.currentUser
                    user!!.sendEmailVerification()
                    Toast.makeText(view.context, "A confirmation email was sent!", Toast.LENGTH_SHORT).show()

                    tempPlayer.username = view.inputUsernameReg.text.toString()
                    tempPlayer.toLoadPlayer().createPlayer(Auth.currentUser!!.uid, view.inputUsernameReg.text.toString())

                    view.arrowAccExists1.performClick()
                } else {
                    showPopUp("Error", "There was an error processing your request")
                }
            }
        }

        view.buttonRegister.setOnClickListener { _ ->

            if (view.inputPassReg.text.toString() != "" && view.inputRePassReg.text.toString() != "" && view.inputPassReg.text.toString() == view.inputRePassReg.text.toString()) {
                userPassword = view.inputPassReg.text.toString()
                registerUser(userPassword)
            } else {
                showPopUp("Alert", "Please enter a valid email address or password")
            }
        }

        return view
    }
}