package com.example.quotify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.quotify.activities.ActivityUser
import com.example.quotify.activities.HomePostsViewer
import com.example.quotify.activities.LoginActivity
import com.example.quotify.database.UserDao
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        GlobalScope.launch(Dispatchers.Main) {
            if (intent.extras?.getString("waitStatus").toString().compareTo("false") == 0) {
                checkAndReroute(0)
            } else {
                checkAndReroute(1500)
            }
        }
    }

    private suspend fun checkAndReroute(i: Long) {
        val jobdelay = GlobalScope.launch {
            delay(i)
        }
        var intent: Intent
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            intent = Intent(this, LoginActivity::class.java)
        } else {
            val uid = FirebaseAuth.getInstance().uid
            if (UserDao().getUserById(uid!!).await().exists()) {
                intent = Intent(this, HomePostsViewer::class.java)
            } else {
                intent = Intent(this, ActivityUser::class.java)
            }
        }
        jobdelay.join()
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
        finish()
    }
}