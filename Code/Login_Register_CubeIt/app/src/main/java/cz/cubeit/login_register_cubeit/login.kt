package cz.cubeit.login_register_cubeit

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import io.realm.ObjectServerError
import io.realm.Realm
import io.realm.SyncCredentials
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_login.*

const val authURL: String = "https://cubeit-test.de1a.cloud.realm.io/auth" //static URL of Realm server (In final build this should be in a seperate file)

class login: AppCompatActivity(){

    val builder = AlertDialog.Builder(this@login)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Realm.init(this)                             // Initialzes Realm

        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        buttonLoginIn.setOnClickListener() {
            userLogin(editTextMailLogin, editTextPasswordLogin)
        }
    }


    private fun userLogin(userMail: EditText, userPassword: EditText) {
        if(!userMail.text.toString().isEmpty()&&!userPassword.text.toString().isEmpty()) {

            val credentials: SyncCredentials =
                SyncCredentials.usernamePassword(userMail.text.toString(), userPassword.text.toString(), false)

            SyncUser.logInAsync(credentials, authURL, object : SyncUser.Callback<SyncUser> {
                override fun onSuccess(user: SyncUser) {

                    builder.setTitle("Success")
                    builder.setMessage("User logged in!")

                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }

                override fun onError(error: ObjectServerError) {

                    builder.setTitle("Fail")
                    builder.setMessage("Email/Fassword incorrect!")

                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }
            })
        }else{

        }
    }
}
