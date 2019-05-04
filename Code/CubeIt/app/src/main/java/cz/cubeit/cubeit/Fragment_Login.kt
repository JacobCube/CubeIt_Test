package cz.cubeit.cubeit


import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_fight_system.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*

var player:Player = Player()

class FragmentLogin : Fragment()  {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        val view:View = inflater.inflate(R.layout.fragment_login, container, false)

        view.loginVersionInfo.text = "Alpha \tv${BuildConfig.VERSION_NAME}"

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.layoutLogin.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.login_bg, opts))
        view.inputEmailLogin.setText(arguments?.getString("loginEmail"))

        val auth = FirebaseAuth.getInstance()

        fun showNotification(titleInput: String, textInput: String){
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle(titleInput)
            builder.setMessage(textInput)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        view.buttonLogin.setOnClickListener {
            val userEmail = view.inputEmailLogin.text.toString()
            val intentSplash = Intent(view.context, Activity_Splash_Screen::class.java)

            val userPassword = view.inputPassLogin.text.toString()
            val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

            startActivity(intentSplash)

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                handler.postDelayed({showNotification("Error", "Please fill out all fields.")},100)
                loadedLogin = LoginStatus.CLOSELOADING
            }
            if (!isConnected){
                handler.postDelayed({showNotification("Error", "Your device is not connected to the internet. Please check your connection and try again.")},100)
                loadedLogin = LoginStatus.CLOSELOADING
            }

            loadGlobalData().addOnSuccessListener{

                Log.d("ITEMS", player.charClass.toString())

                if (userEmail.isNotEmpty() && userPassword.isNotEmpty() && appVersion >= BuildConfig.VERSION_CODE && isConnected){
                    auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener{ task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            player.userSession = user!!

                            val db = FirebaseFirestore.getInstance()

                            db.collection("users").whereEqualTo("userId", user.uid).limit(1)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        try {

                                            val document: DocumentSnapshot = querySnapshot.documents[0]
                                            player.username = document.getString("username")!!

                                            player.loadPlayer().addOnCompleteListener {
                                                if(player.newPlayer){
                                                    loadedLogin = LoginStatus.CLOSELOADING
                                                    val intent = Intent(view.context, Activity_Character_Customization::class.java)
                                                    startActivity(intent)
                                                }else {
                                                    player.online = true
                                                    player.toLoadPlayer().uploadSingleItem("online").addOnCompleteListener {
                                                        loadedLogin = LoginStatus.LOGGED
                                                    }
                                                }
                                            }

                                        }catch (e:IndexOutOfBoundsException){
                                            showNotification("Oops", "We're unable to find you in our database. Are you sure you have an account?")
                                        }
                                    }

                            Log.d("CharclassesDebug", "Class name: ${charClasses[0].name.toString()}")
                        } else {
                            loadedLogin = LoginStatus.CLOSELOADING
                            showNotification("Oops", exceptionFormatter(task.exception.toString()))
                            Log.d("Debug", task.exception.toString())
                        }
                    }
                }else loadedLogin = LoginStatus.CLOSELOADING
           }
        }


        view.resetPass.setOnClickListener {
            val userEmail = view.inputEmailLogin.text.toString()

            if (userEmail.isNotEmpty()){
                auth!!.sendPasswordResetEmail(userEmail)
                showNotification("Alert", "A password reset link was sent to the above email account")
            }
            else {
                showNotification("Oops", "Please enter an email above")
            }
        }


        return view
    }
}


