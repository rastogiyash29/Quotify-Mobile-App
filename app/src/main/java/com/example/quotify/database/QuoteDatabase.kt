package com.example.quotify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.quotify.models.MyQuote
import com.example.quotify.models.Result

@Database(entities = [Result::class,MyQuote::class],version=1, exportSchema = false)
abstract class QuoteDatabase:RoomDatabase() {

    abstract fun quoteDao():QuoteDao
    abstract fun diaryDao():DiaryDao

    companion object{
        @Volatile
        private var INSTANCE:QuoteDatabase?=null

        fun getDatabase(context: Context):QuoteDatabase{
            if(INSTANCE==null){
                synchronized(this){
                    INSTANCE= Room.databaseBuilder(context.applicationContext,
                        QuoteDatabase::class.java,
                        "quoteDB").build()
                }
            }
            return INSTANCE!!
        }
    }
}