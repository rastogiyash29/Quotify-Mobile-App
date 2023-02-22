package com.example.quotify.view_models

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotify.models.Result
import com.example.quotify.models.MyQuote
import com.example.quotify.models.QuoteList
import com.example.quotify.repository.QuoteRepository
import com.example.quotify.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//Remember: LiveData only sets its content visible for its observers during its changeTime only
//*****     Diff b/w MutableLiveData and LiveData is Mutable one has getValue and setValue while other one don't
class MainViewModel(val repository: QuoteRepository, private var context: Context) :
    ViewModel() {

    var mode = 2      //0 is online mode and 1 is offline mode 2 is diary mode
    private val totalModes = 3

    //Defining LiveDatas For Different Modes
    private lateinit var quotesFromDiary: LiveData<List<MyQuote>>
    private lateinit var quotesFromDatabase:LiveData<List<Result>>
    private lateinit var quotesFromInternet: LiveData<QuoteList>

    init {
        quotesFromInternet=repository.getQuotesFromInternetLiveData()
        quotesFromDatabase=repository.getQuotesFromDatabaseLiveData()
        quotesFromDiary=repository.getQuotesFromDiaryLiveData()
        switchModes()
    }

    //defining getters
    fun getQuotesFromInternetLiveData():LiveData<QuoteList>{
        return quotesFromInternet
    }

    fun getQuotesFromDatabaseLiveData():LiveData<List<Result>>{
        return quotesFromDatabase
    }

    fun getQuotesFromDiaryLiveData():LiveData<List<MyQuote>>{
        return quotesFromDiary
    }

    //Defining List And Objects for EachModes
    private var quotesFromInternetList:QuoteList?=null
    private var quotesFromDatabaseList:List<Result>?=null
    private var quotesFromDiaryList:List<MyQuote>?=null

    //writting setters for above list to take care while liveData for eachMode Updates
    fun setquotesFromInternetList(quoteList: QuoteList){
        quotesFromInternetList=quoteList
    }

    fun setquotesFromDatabaseList(list:List<Result>){
        quotesFromDatabaseList=list
    }

    fun setquotesFromDiaryList(list:List<MyQuote>){
        quotesFromDiaryList=list
    }

    //It Keeps Information of Pages Downloaded from Internet (Cache)
    private var pageNumber=0
    private var downloadedPage:HashSet<Int> = HashSet()
    private var downloadedResultList:List<Result> = ArrayList()

    //Adding logic for additions of quotes in databases in various modes
    fun addQuote(result: Result){
        if(mode==0){
            //Will add new downloaded page from here
            Toast.makeText(context,"Addition in Internet",Toast.LENGTH_SHORT).show()
        }else if(mode==1){
            repository.addQuoteInDB(result)
        }else{
            //To auto-regenerate PrimaryKey put its value=0
            repository.addQuoteInDiary(MyQuote(0,"Consistency in Diary",result.author))
        }
    }

    //Testing Delete function
    fun delete(){
        if(mode==0){
            Toast.makeText(context,"Deleting from Internet",Toast.LENGTH_SHORT).show()
        }else if(mode==1){
            if(quotesFromDatabaseList?.isEmpty()==true){

            }else{
                repository.deleteFromDB(quotesFromDatabaseList!![0])
            }
        }else if(mode==2){
            if(quotesFromDiaryList?.isEmpty() == true){

            }else{
                repository.deleteFromDiary(quotesFromDiaryList!![0])
            }
        }
    }

    fun switchModes():String {
        mode = (mode + 1) % totalModes
        if (mode == 0) {    //online mode
            if (NetworkUtils.isInternetAvailable(context)) {
                return quotesFromInternetList.toString()
            } else {
                switchModes()
            }
        } else if (mode == 1) {  //offline mode
            return quotesFromDatabaseList.toString()
        } else {  //diary mode
            return quotesFromDiaryList.toString()
        }
        return ""
    }

    fun clearQuotes() {
        if(mode==0){
            Toast.makeText(context,"Clearing Online data",Toast.LENGTH_SHORT).show()
        }else if(mode==1){
            repository.clearDB()
        }else if (mode == 2)
            repository.clearDiary()
    }

}