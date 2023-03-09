package com.example.quotify.database

import android.util.Log

object QuoteBookDatabase {

    private var userDao:UserDao
    private var userNameDao:UserNameDao

    init{
        userDao= UserDao()
        userNameDao=UserNameDao()
    }

    fun getUserDao():UserDao{
        return userDao
    }

    fun getUserNameDao():UserNameDao{
        return userNameDao
    }
}