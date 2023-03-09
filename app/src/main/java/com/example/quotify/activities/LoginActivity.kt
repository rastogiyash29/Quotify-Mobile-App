package com.example.quotify.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.example.quotify.R
import com.example.quotify.database.QuoteBookDatabase
import com.example.quotify.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var quoteBookDatabase: QuoteBookDatabase

    override fun onStart() {
        super.onStart()

        //checking if user is already signed In or not
        if (auth.currentUser != null) {
            updateUI(auth.currentUser)
        }else{
            enableSignIn()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        disableSignIn()
        //Initializing QuoteBookRepository
        quoteBookDatabase = QuoteBookDatabase

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        auth = Firebase.auth
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<SignInButton>(R.id.signInBtn).setOnClickListener {
            signIn()
        }
    }

    /********************* ALTERNATIVE OF START ACTIVITY FOR RESULT ************************/
    private fun signIn() {
        googleSignInClient.signOut()        //this line clears previously logged in credential and avoid autoLogin
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d("Tag", "${account.id}")
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.d("tag", "falied-> ${e.statusCode}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        disableSignIn()
        GlobalScope.launch(Dispatchers.IO) {
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            withContext(Dispatchers.Main) {
                updateUI(firebaseUser)
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            //it will check user and redirect activity to another accordingly
            startUserActivity()
        } else {
            enableSignIn()
        }
    }

    fun enableSignIn(){
        findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
        findViewById<SignInButton>(R.id.signInBtn).visibility = View.VISIBLE
    }

    fun disableSignIn(){
        findViewById<ProgressBar>(R.id.progress_bar).visibility = View.VISIBLE
        findViewById<SignInButton>(R.id.signInBtn).visibility = View.GONE
    }

    fun startUserActivity(){
        startActivity(Intent(this,ActivityUser::class.java))
        finish()
    }
}