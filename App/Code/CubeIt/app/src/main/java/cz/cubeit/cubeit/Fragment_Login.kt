package cz.cubeit.cubeit


import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.OptionalPendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_fight_board.*
import kotlinx.android.synthetic.main.popup_dialog_register.view.*


class FragmentLogin : Fragment() {
    private val RC_SIGN_IN = 9001

    private var dialog: AlertDialog? = null

    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]
    lateinit var viewTemp: View
    var popWindow: PopupWindow? = null
    lateinit var popView: View

    private var mGoogleSignInClient: GoogleSignInClient? = null

    var loadingAnimation: Animation? = null

    override fun onStop() {
        super.onStop()
        if(loadingAnimation != null) loadingAnimation!!.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                //updateUI(null)
                // [END_EXCLUDE]
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

        if (SystemFlow.readFileText(viewTemp.context, "rememberMe.data") == "1") {
            viewTemp.checkBoxStayLogged.isChecked = true
            if (SystemFlow.readFileText(viewTemp.context, "emailLogin.data") != "0") viewTemp.inputEmailLogin.setText(SystemFlow.readFileText(viewTemp.context, "emailLogin.data"))
            if (SystemFlow.readFileText(viewTemp.context, "emailLogin.data") != "0") viewTemp.inputPassLogin.setText(SystemFlow.readFileText(viewTemp.context, "passwordLogin.data"))
        }

        val auth = FirebaseAuth.getInstance()

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
                viewTemp.imageViewLoginLoading.visibility = View.GONE
                viewTemp.loginPopUpBackground.foreground.alpha = 0
            }

            override fun onAnimationStart(animation: Animation?) {
                viewTemp.loginPopUpBackground.bringToFront()
                viewTemp.imageViewLoginLoading.bringToFront()
                viewTemp.imageViewLoginLoading.visibility = View.VISIBLE
                viewTemp.loginPopUpBackground.foreground.alpha = 150
            }
        })

        mGoogleSignInClient!!.silentSignIn().addOnSuccessListener {
            firebaseAuthWithGoogle(it)
            Snackbar.make(viewTemp, "Welcome back!", Snackbar.LENGTH_SHORT).show()
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
                                            Activity_Splash_Screen().closeLoading()
                                            handler.postDelayed({ showNotification("Error", "Your version is too old, download more recent one. (Alpha versioned ${GenericDB.AppInfo.appVersion})", viewTemp.context) }, 100)
                                        }

                                        if (userEmail.isNotEmpty() && userPassword.isNotEmpty() && GenericDB.AppInfo.appVersion <= BuildConfig.VERSION_CODE) {
                                            Activity_Splash_Screen().setLogText(resources.getString(R.string.loading_log, "Your profile information"))

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
                                                    Activity_Splash_Screen().closeLoading()
                                                    showNotification("Oops", SystemFlow.exceptionFormatter(task.exception.toString()), viewTemp.context)
                                                    Log.d("Debug", task.exception.toString())
                                                }
                                            }
                                        } else Activity_Splash_Screen().closeLoading()
                                    }
                                } else {
                                    Activity_Splash_Screen().closeLoading()

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
                handler.postDelayed({ Snackbar.make(viewTemp, "Your device is not connected to the internet. Please check your connection and try again.", Snackbar.LENGTH_SHORT).show() }, 50)
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
                            if (Data.player.newPlayer) {
                                Data.loadingStatus = LoadingStatus.REGISTERED
                            } else {
                                Data.player.init(context)
                                Data.player.online = true
                                Data.player.uploadSingleItem("online").addOnSuccessListener {
                                    Data.loadingStatus = LoadingStatus.LOGGED
                                }.addOnFailureListener {
                                    Activity_Splash_Screen().closeLoading()
                                    Snackbar.make(viewTemp, "Oops. Request timed out", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }.addOnFailureListener {
                            Activity_Splash_Screen().closeLoading()
                            Snackbar.make(viewTemp, "Oops. Request timed out", Snackbar.LENGTH_SHORT).show()
                        }

                    } catch (e: IndexOutOfBoundsException) {
                        Activity_Splash_Screen().closeLoading()
                        signOut()
                        showNotification("Oops", "We're unable to find you in our database. Are you sure you have an account?", context)
                    }
                }.addOnFailureListener {
                    Activity_Splash_Screen().closeLoading()
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
                    Log.d("testuju", result.signInMethods.toString())

                    when {
                        signInMethods!!.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD) -> {
                            signOut()
                            if(loadingAnimation != null) loadingAnimation!!.cancel()
                            Snackbar.make(viewTemp, "User has password, thus is required in this case.", Snackbar.LENGTH_SHORT).show()
                        }
                        signInMethods.contains("google.com") -> {
                            val intentSplash = Intent(activity!!, Activity_Splash_Screen::class.java)
                            Data.loadingStatus = LoadingStatus.LOGGING

                            mAuth!!.signInWithCredential(credential)
                                    .addOnCompleteListener(activity!!) { task ->
                                        handler.postDelayed({if(loadingAnimation != null)  loadingAnimation!!.cancel() }, 100)
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
                                                                Activity_Splash_Screen().closeLoading()
                                                                handler.postDelayed({ showNotification("Error", "Your version is too old, download more recent one. (Alpha versioned ${GenericDB.AppInfo.appVersion})", activity!!) }, 100)
                                                            } else {
                                                                signInToUser(user, activity!!)
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Snackbar.make(viewTemp, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                                                //updateUI(null)
                                            }

                                            // [START_EXCLUDE]
                                            //hideProgressDialog()
                                            // [END_EXCLUDE]
                                        }else Snackbar.make(viewTemp, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                                    }
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

                                                    Data.loadGlobalData(activity!!).addOnCompleteListener {
                                                        if(it.isSuccessful){
                                                            Data.loadingStatus = LoadingStatus.REGISTERED
                                                        }else {
                                                            showNotification("Oops", "Timed out during loading content. The process of loading is saved.", activity!!)
                                                            Data.loadingStatus = LoadingStatus.CLOSELOADING
                                                        }
                                                    }
                                                    //Activity().overridePendingTransition(R.anim.animation_character_customization,R.anim.animation_character_customization)


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
                                }
                            }
                            popWindow!!.showAtLocation(viewTemp, Gravity.CENTER, 0, 0)
                            viewTemp.loginPopUpBackground.bringToFront()
                            viewTemp.loginPopUpBackground.foreground.alpha = 150
                            handler.postDelayed({
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

    /*private fun silentLogin() {
        val pendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleSignInClient!!.asGoogleApiClient())
        if (pendingResult != null) {
            handleGooglePendingResult(pendingResult)
        }
    }

    private fun handleGooglePendingResult(pendingResult: OptionalPendingResult<GoogleSignInResult>) {
        if (pendingResult.isDone) {

            // There's immediate result available.
            val signInResult = pendingResult.get()
            onSilentSignInCompleted(signInResult)
        } else {
            loadingAnimation.start()
            pendingResult.setResultCallback { signInResult ->
                loadingAnimation.cancel()
                onSilentSignInCompleted(signInResult)
            }
        }
    }

    private fun onSilentSignInCompleted(signInResult: GoogleSignInResult) {
        val signInAccount = signInResult.signInAccount
        if (signInAccount != null) {

        }
    }*/
}


