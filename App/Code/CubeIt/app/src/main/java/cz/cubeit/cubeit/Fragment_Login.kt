package cz.cubeit.cubeit


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.popup_dialog_register.view.*
import java.util.*


class FragmentLogin : Fragment() {
    private val RC_SIGN_IN = 9001

    private var dialog: AlertDialog? = null

    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]
    lateinit var viewTemp: View
    var popWindow: PopupWindow? = null
    lateinit var popView: View
    lateinit var auth: FirebaseAuth
    var connectedTimer: TimerTask? = null
    var playAnimation = true
    var wasRunning = false
    val pumpInOfflineIcon = ValueAnimator.ofFloat(0.95f, 1f)
    val pumpOutOfflineIcon = ValueAnimator.ofFloat(1f, 0.95f)

    private var mGoogleSignInClient: GoogleSignInClient? = null

    var loadingAnimation: Animation? = null

    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onStop() {
        super.onStop()
        loadingAnimation?.cancel()
        connectedTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if(viewTemp.textViewLoginOfflineInfo.visibility == View.VISIBLE){
            connectedTimer = object : TimerTask() {
                override fun run() {
                    activity?.runOnUiThread {
                        if(!isConnected(viewTemp.context)) {
                            viewTemp.textViewLoginOfflineInfo.text = "Offline mode"
                            viewTemp.textViewLoginOfflineInfo.setTextColor(Color.RED)
                            playAnimation = true
                            pumpInOfflineIcon.start()
                        }else {
                            viewTemp.textViewLoginOfflineInfo.text = "Online mode"
                            viewTemp.textViewLoginOfflineInfo.setTextColor(Color.GREEN)
                            playAnimation = false
                            pumpInOfflineIcon.cancel()
                            pumpOutOfflineIcon.cancel()
                        }
                    }
                }
            }
            Timer().scheduleAtFixedRate(connectedTimer, 0, 5000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                if(loadingAnimation != null) loadingAnimation!!.cancel()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        viewTemp = inflater.inflate(R.layout.fragment_login, container, false)
        viewTemp.loginVersionInfo.text = "Alpha \tv${BuildConfig.VERSION_NAME}"
        viewTemp.loginPopUpBackground.foreground.alpha = 0

        val opts = BitmapFactory.Options()
        opts.inScaled = false
        viewTemp.layoutLogin.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.login_bg, opts))

        viewTemp.imageViewLoginOfflineMG.setOnClickListener {
            val intent = Intent(viewTemp.context, ActivityOfflineMG()::class.java)
            startActivity(intent)
        }

        activity?.runOnUiThread {             //faster start up

            pumpInOfflineIcon.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if(playAnimation) pumpOutOfflineIcon.start()
                }
            })
            pumpOutOfflineIcon.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if(playAnimation) pumpInOfflineIcon.start()
                }
            })

            pumpInOfflineIcon.addUpdateListener {
                val value = it.animatedValue as Float
                viewTemp.imageViewLoginOfflineMG.scaleY = value
                viewTemp.imageViewLoginOfflineMG.scaleX = value
                viewTemp.imageViewLoginOfflineEncyclopedia.scaleX = value
                viewTemp.imageViewLoginOfflineEncyclopedia.scaleY = value
            }

            pumpOutOfflineIcon.addUpdateListener {
                val value = it.animatedValue as Float
                viewTemp.imageViewLoginOfflineMG.scaleY = value
                viewTemp.imageViewLoginOfflineMG.scaleX = value
                viewTemp.imageViewLoginOfflineEncyclopedia.scaleX = value
                viewTemp.imageViewLoginOfflineEncyclopedia.scaleY = value
            }


            if(!isConnected(viewTemp.context)){         //check if user is offline, if not, update UI accordingly and check his connectivity every 5 seconds
                viewTemp.textViewLoginOfflineInfo.visibility = View.VISIBLE

                connectedTimer = object : TimerTask() {
                    override fun run() {
                        activity?.runOnUiThread {
                            if(!isConnected(viewTemp.context)) {
                                viewTemp.textViewLoginOfflineInfo.text = "Offline mode"
                                viewTemp.textViewLoginOfflineInfo.setTextColor(Color.RED)
                                playAnimation = true
                                pumpInOfflineIcon.start()
                            }else {
                                viewTemp.textViewLoginOfflineInfo.text = "Online mode"
                                viewTemp.textViewLoginOfflineInfo.setTextColor(Color.GREEN)
                                playAnimation = false
                                pumpInOfflineIcon.cancel()
                                pumpOutOfflineIcon.cancel()
                            }
                        }
                    }
                }
                pumpInOfflineIcon.start()
                Timer().scheduleAtFixedRate(connectedTimer, 0, 5000)
            }else {
                pumpInOfflineIcon.cancel()
                viewTemp.textViewLoginOfflineInfo.visibility = View.GONE
                connectedTimer?.cancel()
            }

            if (SystemFlow.readFileText(viewTemp.context, "rememberMe.data") == "1") {
                viewTemp.checkBoxStayLogged.isChecked = true
                if (SystemFlow.readFileText(viewTemp.context, "emailLogin.data") != "0") viewTemp.inputEmailLogin.setText(SystemFlow.readFileText(viewTemp.context, "emailLogin.data"))
                if (SystemFlow.readFileText(viewTemp.context, "emailLogin.data") != "0") viewTemp.inputPassLogin.setText(SystemFlow.readFileText(viewTemp.context, "passwordLogin.data"))
            }

            auth = FirebaseAuth.getInstance()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            mGoogleSignInClient = GoogleSignIn.getClient(viewTemp.context, gso)
            mAuth = FirebaseAuth.getInstance()

            viewTemp.buttonLoginDiffAcc.visibility = if(mAuth!!.currentUser != null){
                View.VISIBLE
            }else View.GONE

            viewTemp.buttonLoginDiffAcc.setOnClickListener {
                signOut()
                viewTemp.buttonLoginDiffAcc.visibility = View.GONE
            }

            //val opr = Auth.GoogleSignInApi.silentSignIn(mGoogleSignInClient!!.asGoogleApiClient())

            loadingAnimation = AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_loading_rotate)

            loadingAnimation!!.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    viewTemp.buttonLoginDiffAcc.visibility = View.VISIBLE
                    viewTemp.imageViewLoginLoading.visibility = View.GONE
                    viewTemp.loginPopUpBackground.foreground.alpha = 0
                }

                override fun onAnimationStart(animation: Animation?) {
                    viewTemp.buttonLoginDiffAcc.visibility = View.GONE
                    viewTemp.loginPopUpBackground.bringToFront()
                    viewTemp.imageViewLoginLoading.bringToFront()
                    viewTemp.imageViewLoginLoading.visibility = View.VISIBLE
                    viewTemp.loginPopUpBackground.foreground.alpha = 150
                }
            })

            mGoogleSignInClient!!.silentSignIn().addOnSuccessListener {
                firebaseAuthWithGoogle(it)
                Snackbar.make(viewTemp, "Welcome back!", Snackbar.LENGTH_SHORT).show()
            }.addOnFailureListener {
                if(context != null) {
                    val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    if (cm.activeNetworkInfo?.isConnected == false) Snackbar.make(viewTemp, "Connection problem occurred.", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        viewTemp.imageViewLoginGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        viewTemp.buttonLogin.setOnClickListener {
            val userEmail = viewTemp.inputEmailLogin.text.toString()
            val intentSplash = Intent(viewTemp.context, Activity_Splash_Screen::class.java)

            val userPassword = viewTemp.inputPassLogin.text.toString()
            val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            Data.loadingStatus = LoadingStatus.LOGGING

            if (cm.activeNetworkInfo?.isConnected == true) {

                if (viewTemp.inputEmailLogin.text!!.isNotBlank()) {

                    if (viewTemp.inputEmailLogin.text.toString().isEmail()) {

                        if (viewTemp.inputPassLogin.text!!.isNotBlank()) {

                            startActivity(intentSplash)
                            val db = FirebaseFirestore.getInstance()
                            db.collection("Server").document("Generic").get().addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.getString("Status") == "on") {
                                    Data.loadGlobalData(viewTemp.context).addOnSuccessListener {

                                        if (GenericDB.AppInfo.appVersion > BuildConfig.VERSION_CODE) {
                                            Data.loadingStatus = LoadingStatus.CLOSELOADING
                                            Handler().postDelayed({ showNotification("Error", "Your version is too old, download more recent one. (Alpha versioned ${GenericDB.AppInfo.appVersion})", viewTemp.context) }, 100)
                                        }

                                        val textView = textViewLog?.get()

                                        if (userEmail.isNotEmpty() && userPassword.isNotEmpty() && GenericDB.AppInfo.appVersion <= BuildConfig.VERSION_CODE) {
                                            textView?.text = resources.getString(R.string.loading_log, "Your profile information")

                                            auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    if (checkBoxStayLogged.isChecked) {
                                                        SystemFlow.writeFileText(viewTemp.context, "emailLogin.data", userEmail)
                                                        SystemFlow.writeFileText(viewTemp.context, "passwordLogin.data", userPassword)
                                                        SystemFlow.writeFileText(viewTemp.context, "rememberMe.data", "1")
                                                    } else {
                                                        SystemFlow.writeFileText(viewTemp.context, "emailLogin.data", "")
                                                        SystemFlow.writeFileText(viewTemp.context, "passwordLogin.data", "")
                                                        SystemFlow.writeFileText(viewTemp.context, "rememberMe.data", "0")
                                                    }
                                                    val user = auth.currentUser

                                                    if (user != null) {
                                                        signInToUser(user, viewTemp.context)
                                                    } else {
                                                        showNotification("Oops", "User not found!", viewTemp.context)
                                                    }

                                                } else {
                                                    Data.loadingStatus = LoadingStatus.CLOSELOADING
                                                    showNotification("Oops", SystemFlow.exceptionFormatter(task.exception.toString()), viewTemp.context)
                                                    Log.d("Debug", task.exception.toString())
                                                }
                                            }
                                        } else Data.loadingStatus = LoadingStatus.CLOSELOADING
                                    }.addOnFailureListener {
                                        Data.loadingStatus = LoadingStatus.CLOSELOADING
                                        Log.d("login failure", it.message.toString())
                                    }
                                } else {
                                    Data.loadingStatus = LoadingStatus.CLOSELOADING

                                    val builder = AlertDialog.Builder(viewTemp.context)
                                    builder.setTitle("Server not available")
                                    builder.setMessage("Server is currently ${documentSnapshot.getString("Status")}.\nWe apologize for any inconvenience.\n" + if (documentSnapshot.getBoolean("ShowDsc")!!) documentSnapshot.getString("ExternalDsc") else "")
                                    if (dialog == null) dialog = builder.create()
                                    dialog!!.setCanceledOnTouchOutside(false)
                                    dialog!!.setCancelable(false)

                                    dialog!!.setButton(Dialog.BUTTON_POSITIVE, "OK") { dialogX, _ ->
                                        dialogX.dismiss()
                                    }
                                    if (!dialog!!.isShowing) dialog!!.show()
                                }
                            }.addOnFailureListener {
                                Data.loadingStatus = LoadingStatus.CLOSELOADING
                            }
                        } else {
                            viewTemp.inputPassLogin.startAnimation(AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_shaky_short))
                            Snackbar.make(viewTemp, "Field required!", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        viewTemp.inputEmailLogin.startAnimation(AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_shaky_short))
                        Snackbar.make(viewTemp, "Not valid email!", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    viewTemp.inputEmailLogin.startAnimation(AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_shaky_short))
                    Snackbar.make(viewTemp, "Field required!", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                viewTemp.buttonLogin.startAnimation(AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_shaky_short))
                Handler().postDelayed({ Snackbar.make(viewTemp, "Your device is not connected to the internet. Please check your connection and try again.", Snackbar.LENGTH_SHORT).show() }, 50)
                connectedTimer?.cancel()

                if(!pumpInOfflineIcon.isRunning && !pumpOutOfflineIcon.isRunning) pumpInOfflineIcon.start()
                viewTemp.textViewLoginOfflineInfo.visibility = View.VISIBLE
                connectedTimer = object : TimerTask() {
                    override fun run() {
                        activity?.runOnUiThread {
                            if(!isConnected(viewTemp.context)) {
                                viewTemp.textViewLoginOfflineInfo.text = "Offline mode"
                                viewTemp.textViewLoginOfflineInfo.setTextColor(Color.RED)
                                playAnimation = true
                                if(!pumpInOfflineIcon.isRunning && !pumpOutOfflineIcon.isRunning) pumpInOfflineIcon.start()
                            }else {
                                viewTemp.textViewLoginOfflineInfo.text = "Online mode"
                                viewTemp.textViewLoginOfflineInfo.setTextColor(Color.GREEN)
                                playAnimation = false
                                pumpInOfflineIcon.cancel()
                                pumpOutOfflineIcon.cancel()
                            }
                        }
                    }
                }
                Timer().scheduleAtFixedRate(connectedTimer, 0, 5000)
            }
        }

        viewTemp.resetPass.setOnClickListener {
            val userEmail = viewTemp.inputEmailLogin.text.toString()

            if (userEmail.isNotEmpty()) {
                auth.sendPasswordResetEmail(userEmail)
                showNotification("Alert", "A password reset link was sent to the above email account", viewTemp.context)
            } else {
                viewTemp.inputEmailLogin.startAnimation(AnimationUtils.loadAnimation(viewTemp.context, R.anim.animation_shaky_short))
                Snackbar.make(viewTemp, "This action requires email. Please enter a valid email above.", Snackbar.LENGTH_SHORT).show()
            }
        }
        return viewTemp
    }

    private fun showNotification(titleInput: String, textInput: String, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(titleInput)
        builder.setMessage(textInput)
        val dialog: AlertDialog = builder.create()
        if (isVisible && isAdded) dialog.show()
    }

    private fun signInToUser(user: FirebaseUser, context: Context) {
        Data.player.userSession = user

        val db = FirebaseFirestore.getInstance()
        db.collection("users").whereEqualTo("userId", user.uid).limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    try {
                        val document: DocumentSnapshot = querySnapshot.documents[0]
                        Data.player.username = document.getString("username")!!

                        Data.player.loadPlayerInstance(context).addOnSuccessListener {

                            Log.d("logged in user", "new player? ${Data.player.newPlayer} - username ${Data.player.username}")
                            if (Data.player.newPlayer) {
                                Log.d("Logged-in", "new user")
                                Data.loadingStatus = LoadingStatus.REGISTERED
                            } else {
                                Data.player.init(context)
                                Data.player.online = true
                                Data.player.uploadSingleItem("online").addOnSuccessListener {
                                    Data.loadingStatus = LoadingStatus.LOGGED
                                }.addOnFailureListener {
                                    Data.loadingStatus = LoadingStatus.CLOSELOADING
                                    Snackbar.make(viewTemp, "Oops. Request timed out", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }.addOnFailureListener {
                            Data.loadingStatus = LoadingStatus.CLOSELOADING
                            Snackbar.make(viewTemp, "Oops. Request timed out", Snackbar.LENGTH_SHORT).show()
                        }

                    } catch (e: IndexOutOfBoundsException) {
                        Data.loadingStatus = LoadingStatus.CLOSELOADING
                        signOut()
                        showNotification("Oops", "We're unable to find you in our database. Are you sure you have an account?", context)
                    }
                }.addOnFailureListener {
                    Data.loadingStatus = LoadingStatus.CLOSELOADING
                    Snackbar.make(viewTemp, "Oops. Request timed out", Snackbar.LENGTH_SHORT).show()
                }
    }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        if (mAuth != null && mGoogleSignInClient != null) {
            // Firebase sign out
            mAuth!!.signOut()

            // Google sign out
            mGoogleSignInClient!!.signOut()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        // [START_EXCLUDE silent]
        //showProgressDialog()
        // [END_EXCLUDE]
        if (mAuth == null) {
            signOut()
            return
        }

        viewTemp.buttonLoginDiffAcc.visibility = View.VISIBLE
        viewTemp.imageViewLoginLoading.startAnimation(loadingAnimation)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.fetchSignInMethodsForEmail(acct.email!!)
                .addOnSuccessListener { result ->

                    val signInMethods = result.signInMethods

                    when {
                        signInMethods!!.contains("google.com") -> {
                            val intentSplash = Intent(activity!!, Activity_Splash_Screen::class.java)
                            Data.loadingStatus = LoadingStatus.LOGGING

                            mAuth!!.signInWithCredential(credential)
                                    .addOnCompleteListener(activity!!) { task ->
                                        Handler().postDelayed({if(loadingAnimation != null)  loadingAnimation!!.cancel() }, 100)
                                        startActivity(intentSplash)
                                        if (task.isSuccessful) {
                                            // Sign in success, update UI with the signed-in user's information
                                            val user = mAuth!!.currentUser

                                            if (user != null) {

                                                val db = FirebaseFirestore.getInstance()
                                                db.collection("Server").document("Generic").get().addOnSuccessListener { documentSnapshot ->
                                                    if (documentSnapshot.getString("Status") == "on") {
                                                        Data.loadGlobalData(activity!!).addOnSuccessListener {
                                                            if (GenericDB.AppInfo.appVersion > BuildConfig.VERSION_CODE) {
                                                                Data.loadingStatus = LoadingStatus.CLOSELOADING
                                                                Handler().postDelayed({ showNotification("Error", "Your version is too old, download more recent one. (Alpha versioned ${GenericDB.AppInfo.appVersion})", activity!!) }, 100)
                                                            } else {
                                                                signInToUser(user, activity!!)
                                                            }
                                                        }.addOnFailureListener {
                                                            Data.loadingStatus = LoadingStatus.CLOSELOADING
                                                            Snackbar.make(viewTemp, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                                                        }
                                                    }else {
                                                        val mySnackbar = Snackbar.make(viewTemp, "Server is not available. ${if(documentSnapshot.getBoolean("ShowDsc")!!)documentSnapshot.getString("ExternalDsc") else ""}. You can check our social medias for more information and updates.", Snackbar.LENGTH_INDEFINITE)

                                                        class MyUndoListener : View.OnClickListener {
                                                            override fun onClick(v: View?) {
                                                                mySnackbar.dismiss()
                                                            }
                                                        }
                                                        mySnackbar.setAction("ok", MyUndoListener())
                                                        mySnackbar.show()

                                                        Data.loadingStatus = LoadingStatus.CLOSELOADING
                                                    }
                                                }
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Snackbar.make(viewTemp, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                                                //updateUI(null)
                                                Data.loadingStatus = LoadingStatus.CLOSELOADING
                                            }
                                            // [START_EXCLUDE]
                                            //hideProgressDialog()
                                            // [END_EXCLUDE]
                                        }else {
                                            Data.loadingStatus = LoadingStatus.CLOSELOADING
                                            Snackbar.make(viewTemp, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                                        }
                                    }
                        }
                        signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD) -> {
                            signOut()
                            if(loadingAnimation != null) loadingAnimation!!.cancel()
                            Snackbar.make(viewTemp, "User has password, thus is required in this case.", Snackbar.LENGTH_SHORT).show()
                        }
                        signInMethods.isEmpty() -> {
                            if(loadingAnimation != null) loadingAnimation!!.cancel()

                            val intentSplash = Intent(activity!!, Activity_Splash_Screen::class.java)
                            Data.loadingStatus = LoadingStatus.LOGGING
                            val viewP = layoutInflater.inflate(R.layout.popup_dialog_register, null, false)
                            popView = viewP
                            popWindow = PopupWindow(activity!!)
                            popWindow!!.contentView = viewP
                            popWindow!!.isOutsideTouchable = false
                            popWindow!!.isFocusable = true
                            popWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            popWindow!!.setOnDismissListener {
                                viewTemp.loginPopUpBackground.foreground.alpha = 0
                            }

                            val db = FirebaseFirestore.getInstance()
                            val docRef = db.collection("users")

                            viewP.editTextPopRegisterName.setText(acct.displayName ?: "")

                            viewP.editTextPopRegisterName.setOnFocusChangeListener { v, hasFocus ->
                                if (!hasFocus) {
                                    if (viewP.editTextPopRegisterName.text!!.isNotBlank() && viewP.editTextPopRegisterName.text!!.length in 6..12) {
                                        docRef.document(viewP.editTextPopRegisterName.text.toString()).get().addOnSuccessListener {
                                            if (it.exists()) {
                                                viewP.textViewPopRegisterError.visibility = View.VISIBLE
                                                viewP.editTextPopRegisterName.startAnimation(AnimationUtils.loadAnimation(activity!!, R.anim.animation_shaky_short))
                                            } else {
                                                viewP.textViewPopRegisterError.visibility = View.GONE
                                            }
                                        }
                                    } else {
                                        viewP.editTextPopRegisterName.startAnimation(AnimationUtils.loadAnimation(activity!!, R.anim.animation_shaky_short))
                                    }
                                }
                            }

                            viewP.buttonCloseDialogRegister.setOnClickListener {
                                popWindow?.dismiss()
                            }

                            viewP.buttonPopRegisterYes.setOnClickListener {
                                if (viewP.editTextPopRegisterName.text!!.isNotBlank() && viewP.editTextPopRegisterName.text!!.length in 6..12) {
                                    viewP.textViewPopRegisterError.visibility = View.GONE
                                    popWindow!!.dismiss()

                                    viewTemp.imageViewLoginLoading.startAnimation(loadingAnimation)

                                    docRef.document(viewP.editTextPopRegisterName.text.toString()).get().addOnSuccessListener { documentSnapshot ->
                                        if(loadingAnimation != null) loadingAnimation!!.cancel()
                                        if (documentSnapshot.exists() || viewP.editTextPopRegisterName.text.toString().toLowerCase() == "player") {
                                            if (!popWindow!!.isShowing) {
                                                viewTemp.loginPopUpBackground.bringToFront()
                                                popWindow!!.showAtLocation(viewTemp, Gravity.CENTER, 0, 0)
                                                viewTemp.loginPopUpBackground.foreground.alpha = 150
                                            }
                                            viewP.textViewPopRegisterError.visibility = View.VISIBLE
                                            viewP.textViewPopRegisterError.text = "Given username already exist."
                                            viewP.editTextPopRegisterName.startAnimation(AnimationUtils.loadAnimation(activity!!, R.anim.animation_shaky_short))

                                        } else {
                                            viewP.textViewPopRegisterError.visibility = View.GONE
                                            val tempPlayer = Player()
                                            tempPlayer.username = viewP.editTextPopRegisterName.text.toString()
                                            //tempPlayer.userSession = user

                                            startActivity(intentSplash)

                                            mAuth!!.signInWithCredential(credential).addOnSuccessListener {
                                                val user = mAuth!!.currentUser

                                                tempPlayer.createPlayer(user!!.uid, viewP.editTextPopRegisterName.text.toString()).addOnSuccessListener {
                                                    Data.player.username = viewP.editTextPopRegisterName.text.toString()

                                                    db.collection("GenericDB").document("AppInfo").get().addOnSuccessListener { itGeneric ->

                                                        if(itGeneric.toObject(GenericDB.AppInfo::class.java) != null){
                                                            GenericDB.AppInfo.updateData(itGeneric.toObject(GenericDB.AppInfo::class.java)!!)

                                                            if(GenericDB.AppInfo.appVersion <= BuildConfig.VERSION_CODE){
                                                                Data.loadGlobalData(activity!!).addOnCompleteListener {
                                                                    if(it.isSuccessful){
                                                                        Data.loadingStatus = LoadingStatus.REGISTERED
                                                                    }else {
                                                                        showNotification("Oops", "Timed out during loading content. The process of loading is saved.", activity!!)
                                                                        Data.loadingStatus = LoadingStatus.CLOSELOADING
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                }.addOnFailureListener {
                                                    Data.loadingStatus = LoadingStatus.CLOSELOADING
                                                }
                                            }.addOnFailureListener {
                                                Data.loadingStatus = LoadingStatus.CLOSELOADING
                                            }
                                        }
                                    }
                                } else {
                                    viewP.editTextPopRegisterName.startAnimation(AnimationUtils.loadAnimation(activity!!, R.anim.animation_shaky_short))
                                    viewP.textViewPopRegisterError.visibility = View.VISIBLE
                                    viewP.textViewPopRegisterError.text = getString(R.string.register_username)
                                }
                            }
                            popWindow!!.showAtLocation(viewTemp, Gravity.CENTER, 0, 0)
                            viewTemp.loginPopUpBackground.bringToFront()
                            viewTemp.loginPopUpBackground.foreground.alpha = 150
                            Handler().postDelayed({
                                viewTemp.loginPopUpBackground.foreground.alpha = 150
                            }, 50)
                        }
                    }
                }
                .addOnFailureListener {
                    Snackbar.make(viewTemp, "Oops. Something went wrong, sorry!", Snackbar.LENGTH_SHORT).show()
                    if(loadingAnimation != null) loadingAnimation!!.cancel()
                }
    }
}


