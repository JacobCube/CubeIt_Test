package cubeit.cubeit_login_test

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import io.realm.ObjectServerError
import io.realm.Realm
import io.realm.SyncCredentials
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern

class LoginUser : AppCompatActivity()  {

    val authURL: String = "https://cubeit-test.de1a.cloud.realm.io/auth" //static URL of Realm server (In final build this should be in a seperate file)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Realm.init(this) // Initialzes Realm

        buttonLogin.setOnClickListener({ view -> userLogin() })
        userCreateButton.setOnClickListener {
            val intent = Intent(this, RegisterUser::class.java)
            startActivity(intent)
        }
    }

    fun isEmailValid(email: String): Boolean {
        val regExpn = ("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$")

        val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)

        return matcher.matches();
    }

    fun isPasswordValid(password: String): Boolean {

        // Password RQ's: 1+ uppercase letter, 2+ numbers, 1+ special character, length >= 8 (Max length is 31 characters

        val regExpn = ("^(?=.*[A-Z])(?=.*[!@#\$&*])(?=.*[0-9].*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{8,31}\$")

        val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(password)

        return matcher.matches();
    }

    fun userLogin() {

        val userEmail = userEmailText.text.toString()
        val userPassword = userPasswordText.text.toString()

        if (isEmailValid(userEmail) && isPasswordValid(userPassword)){

            val credentials: SyncCredentials = SyncCredentials.usernamePassword(userEmail, userPassword, false)

            SyncUser.logInAsync(credentials, authURL, object:SyncUser.Callback<SyncUser> {
                override fun onSuccess(user: SyncUser) {

                    val builder = AlertDialog.Builder(this@LoginUser)

                    builder.setTitle("Success")
                    builder.setMessage("User logged in!")

                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }

                override fun onError(error: ObjectServerError) {

                    val builder = AlertDialog.Builder(this@LoginUser)

                    builder.setTitle("Fail")
                    builder.setMessage("Username or password is incorrect!")

                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }
            })

        }
        else {
            val builder = AlertDialog.Builder(this@LoginUser)

            builder.setTitle("Error")
            builder.setMessage("Please enter a valid email/password")

            val dialog: AlertDialog = builder.create()

            dialog.show()

        }
    }

}
