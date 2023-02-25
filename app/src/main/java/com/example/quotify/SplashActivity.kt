package com.example.quotify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //creating intent
        var intent=Intent(this,MainActivity::class.java)

        Log.d("loc->","At top to loop")

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("loc->","At inside to loop")
            startActivity(intent)
            finish()
        }, 2500)

        Log.d("loc->","At end of loop")
    }
}