package cubeit.cubeit_login_test

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import io.realm.ObjectServerError
import io.realm.Realm
import io.realm.SyncCredentials
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_main.*

public class MainActivity : AppCompatActivity()  {

    val authURL: String = "https://cubeit-test.de1a.cloud.realm.io/auth" //static URL of Realm server (In final build this should be in a seperate file)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Realm.init(this) // Initialzes Realm

        button.setOnClickListener({ view -> userLogin() })
        button4.setOnClickListener { view -> userCreate() }

    }

    fun userCreate() {

        val userEmail = editTextName.text.toString()
        val userPassword = editTextPassword.text.toString()

        if (!userEmail.isEmpty() && !userPassword.isEmpty()){

            val credentials: SyncCredentials = SyncCredentials.usernamePassword(userEmail, userPassword, true)

            SyncUser.logInAsync(credentials, authURL, object:SyncUser.Callback<SyncUser> {
                override fun onSuccess(user: SyncUser) {

                    val builder = AlertDialog.Builder(this@MainActivity)

                    builder.setTitle("Success")
                    builder.setMessage("User created!")

                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }

                override fun onError(error: ObjectServerError) {

                    val builder = AlertDialog.Builder(this@MainActivity)

                    builder.setTitle("Fail")
                    builder.setMessage("User already exists!")

                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }
            })

        }
        else {
                val builder = AlertDialog.Builder(this@MainActivity)

                builder.setTitle("Error")
                builder.setMessage("Please enter a valid email/password")

                val dialog: AlertDialog = builder.create()

                dialog.show()

        }


    }


    fun userLogin() {

        val userEmail = editTextName.text.toString()
        val userPassword = editTextPassword.text.toString()

        if (!userEmail.isEmpty() && !userPassword.isEmpty()){

            val credentials: SyncCredentials = SyncCredentials.usernamePassword(userEmail, userPassword, false)

            SyncUser.logInAsync(credentials, authURL, object:SyncUser.Callback<SyncUser> {
                override fun onSuccess(user: SyncUser) {

                    val builder = AlertDialog.Builder(this@MainActivity)

                    builder.setTitle("Success")
                    builder.setMessage("User created!")

                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }

                override fun onError(error: ObjectServerError) {

                    val builder = AlertDialog.Builder(this@MainActivity)

                    builder.setTitle("Fail")
                    builder.setMessage("User already exists!")

                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }
            })

        }
        else {
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("Error")
            builder.setMessage("Please enter a valid email/password")

            val dialog: AlertDialog = builder.create()

            dialog.show()

        }
    }

}
