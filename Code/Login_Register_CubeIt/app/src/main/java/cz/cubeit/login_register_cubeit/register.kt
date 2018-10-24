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
import kotlinx.android.synthetic.main.activity_register.*

class register: AppCompatActivity(){

    val builder = AlertDialog.Builder(this@register)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Realm.init(this)                             // Initialzes Realm

        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        buttonRegisterIn.setOnClickListener() {
            userCreate(editTextMailRegister, editTextPasswordRegister, editTextRePasswordRegister)
        }
    }

    private fun userCreate(userMail: EditText, userPassword: EditText, userRePassword: EditText) {

        if(userPassword==userRePassword){
            val credentials: SyncCredentials = SyncCredentials.usernamePassword(userMail.text.toString(), userPassword.text.toString(), true)

            SyncUser.logInAsync(credentials, authURL, object: SyncUser.Callback<SyncUser> {
                override fun onSuccess(user: SyncUser) {

                    builder.setTitle("Success")
                    builder.setMessage("User created!")

                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }

                override fun onError(error: ObjectServerError) {

                    builder.setTitle("Fail")
                    builder.setMessage("User already exists!")

                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }
            })
        }else{
        }
    }
}