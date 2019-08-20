package cz.cubeit.cubeit


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*

class FragmentLogin : Fragment()  {
    private var dialog: AlertDialog? = null


    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        val view:View = inflater.inflate(R.layout.fragment_login, container, false)

        view.loginVersionInfo.text = "Alpha \tv${BuildConfig.VERSION_NAME}"

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        view.layoutLogin.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.login_bg, opts))

        if(SystemFlow.readFileText(view.context, "rememberMe.data") == "1"){
            view.checkBoxStayLogged.isChecked = true
            if(SystemFlow.readFileText(view.context, "emailLogin.data") != "0")view.inputEmailLogin.setText(SystemFlow.readFileText(view.context, "emailLogin.data"))
            if(SystemFlow.readFileText(view.context, "emailLogin.data") != "0")view.inputPassLogin.setText(SystemFlow.readFileText(view.context, "passwordLogin.data"))
        }

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
            val isConnected: Boolean = activeNetwork?.isConnected == true

            startActivity(intentSplash)
            Data.loadingStatus = LoadingStatus.LOGGING

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                handler.postDelayed({showNotification("Error", "Please fill out all fields.")},100)
                Data.loadingStatus = LoadingStatus.CLOSELOADING
            }
            if (!isConnected){
                handler.postDelayed({showNotification("Error", "Your device is not connected to the internet. Please check your connection and try again.")},100)
                Data.loadingStatus = LoadingStatus.CLOSELOADING
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("Server").document("Generic").get().addOnSuccessListener {
                if(it.getString("Status") == "on"){
                    Data.loadGlobalData(view.context).addOnSuccessListener{

                        if (GenericDB.AppInfo.appVersion > BuildConfig.VERSION_CODE){
                            Data.loadingStatus = LoadingStatus.CLOSELOADING
                            handler.postDelayed({showNotification("Error", "Your version is too old, download more recent one. (Alpha versioned ${GenericDB.AppInfo.appVersion})")},100)
                        }

                        if (userEmail.isNotEmpty() && userPassword.isNotEmpty() && GenericDB.AppInfo.appVersion <= BuildConfig.VERSION_CODE && isConnected){
                            Activity_Splash_Screen().setLogText(resources.getString(R.string.loading_log, "Your profile information"))

                            auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener{ task ->
                                if (task.isSuccessful) {
                                    if(checkBoxStayLogged.isChecked){
                                        SystemFlow.writeFileText(view.context, "emailLogin.data", userEmail)
                                        SystemFlow.writeFileText(view.context, "passwordLogin.data", userPassword)
                                        SystemFlow.writeFileText(view.context, "rememberMe.data", "1")
                                    }else{
                                        SystemFlow.writeFileText(view.context, "emailLogin.data", "")
                                        SystemFlow.writeFileText(view.context, "passwordLogin.data", "")
                                        SystemFlow.writeFileText(view.context, "rememberMe.data", "0")
                                    }
                                    val user = auth.currentUser

                                    Data.player.userSession = user!!

                                    db.collection("users").whereEqualTo("userId", user.uid).limit(1)
                                            .get()
                                            .addOnSuccessListener { querySnapshot ->
                                                try {

                                                    val document: DocumentSnapshot = querySnapshot.documents[0]
                                                    Data.player.username = document.getString("username")!!

                                                    Data.player.loadPlayer().addOnSuccessListener {
                                                        if(Data.player.newPlayer){
                                                            Data.loadingStatus = LoadingStatus.CLOSELOADING
                                                            val intent = Intent(view.context, Activity_Character_Customization::class.java)
                                                            startActivity(intent)
                                                        }else {
                                                            Data.player.init(view.context)
                                                            Data.player.online = true
                                                            Data.player.uploadSingleItem("online").addOnSuccessListener {
                                                                Data.loadingStatus = LoadingStatus.LOGGED
                                                            }
                                                        }
                                                    }

                                                }catch (e:IndexOutOfBoundsException){
                                                    showNotification("Oops", "We're unable to find you in our database. Are you sure you have an account?")
                                                }
                                            }
                                } else {
                                    Data.loadingStatus = LoadingStatus.CLOSELOADING
                                    showNotification("Oops", SystemFlow.exceptionFormatter(task.exception.toString()))
                                    Log.d("Debug", task.exception.toString())
                                }
                            }
                        }else Data.loadingStatus = LoadingStatus.CLOSELOADING
                    }
                }else {
                    Data.loadingStatus = LoadingStatus.CLOSELOADING

                    val builder = AlertDialog.Builder(view.context)
                    builder.setTitle("Server not available")
                    builder.setMessage("Server is currently ${it.getString("Status")}.\nWe apologize for any inconvenience.\n" + if(it.getBoolean("ShowDsc")!!)it.getString("ExternalDsc") else "")
                    if(dialog == null)dialog = builder.create()
                    dialog!!.setCanceledOnTouchOutside(false)
                    dialog!!.setCancelable(false)

                    dialog!!.setButton(Dialog.BUTTON_POSITIVE, "OK") { dialogX, _ ->
                        dialogX.dismiss()
                    }
                    if(!dialog!!.isShowing)dialog!!.show()
                }
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


