package cubeit.cubeit_login_test

import android.app.Application
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import io.realm.SyncCredentials
import kotlinx.android.synthetic.main.activity_main.*
import android.R.attr.password
import android.widget.Toast
import io.realm.ObjectServerError
import io.realm.SyncUser
import io.realm.permissions.UserCondition.username



public class MainActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Realm.init(this) // Initialzes Realm


        val authURL: String = "https://cubeit-test.de1a.cloud.realm.io/" //static URL of Realm server (In final build this should be in a seperate file)

        var userEmail: String = ""
        var userPassword: String = "" //Creds taken from login screen

        button.setOnClickListener {
            userEmail = editTextMail.text.toString()
            userPassword = editTextPassword.text.toString()
        }

        var credentials: SyncCredentials = SyncCredentials.usernamePassword(userEmail, userPassword)

        SyncUser.loginAsync(credentials, authURL, object:SyncUser.Callback<SyncUser> { // !Unresolved reference error!
            override fun onSuccess(user:SyncUser) {
                // User is ready
                // Can also be accessed using `SyncUser.currentUser()` if only one
                // user is logged in.
            }
            override fun onError(error:ObjectServerError) {
                // Something went wrong
            }
        })


    }
}
