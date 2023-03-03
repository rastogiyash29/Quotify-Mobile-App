package com.example.quotify.view_models

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotify.models.Result
import com.example.quotify.models.MyQuote
import com.example.quotify.models.QuoteList
import com.example.quotify.repository.QuoteRepository
import com.example.quotify.utils.NetworkUtils
import kotlinx.coroutines.*

//Remember: LiveData only sets its content visible for its observers during its changeTime only
//*****     Diff b/w MutableLiveData and LiveData is Mutable one has getValue and setValue while other one don't
class MainViewModel(val repository: QuoteRepository, private var context: Context) :
    ViewModel() {

    private var mode = 0      //0 is online mode and 1 is offline mode 2 is diary mode
    fun getMode():Int{
        return mode
    }

    private val totalModes = 3

    //Defining LiveDatas For Different Modes
    private var quotesFromDiary: LiveData<List<MyQuote>>
    private var quotesFromDatabase: LiveData<List<Result>>
    private var quotesFromInternet: LiveData<QuoteList>

    init {
        quotesFromInternet = repository.getQuotesFromInternetLiveData()
        quotesFromDatabase = repository.getQuotesFromDatabaseLiveData()
        quotesFromDiary = repository.getQuotesFromDiaryLiveData()
    }

    //defining getters
    fun getQuotesFromInternetLiveData(): LiveData<QuoteList> {
        return quotesFromInternet
    }

    fun getQuotesFromDatabaseLiveData(): LiveData<List<Result>> {
        return quotesFromDatabase
    }

    fun getQuotesFromDiaryLiveData(): LiveData<List<MyQuote>> {
        return quotesFromDiary
    }

    //Defining List And Objects for EachModes
    private var quotesFromInternetList: ArrayList<Result> = ArrayList()
    private var quotesFromDatabaseList: List<Result>? = null
    private var quotesFromDiaryList: List<MyQuote>? = null

    //writting setters for above list to take care while liveData for eachMode Updates
    fun setquotesFromInternetList(quoteList: QuoteList) {
        if (quoteList != null) {
            if (!downloadedPage.contains(quoteList.page)) {
                for (result in quoteList.results) {
                    quotesFromInternetList.add(result)
                }
                downloadedPage.add(quoteList.page)
            }
            pageNumber++
        }
        setLiveQuote(getCurrentQuote())
    }

    fun setquotesFromDatabaseList(list: List<Result>) {
        quotesFromDatabaseList = list
        setFavouriteQuote(getCurrentQuote())
    }

    fun setquotesFromDiaryList(list: List<MyQuote>) {
        quotesFromDiaryList = list
        setFavouriteQuote(getCurrentQuote())
    }

    //It Keeps Information of Pages Downloaded from Internet (Cache)
    private var pageNumber = 0
    private var downloadedPage: HashSet<Int> = HashSet()
    private var pointersArray = IntArray(3)

    //Adding logic for additions of quotes in databases in various modes
    suspend fun addQuote(result: Result): Boolean {
        if(result.primaryId==-1)return false
        if (mode == 0) {
            //Will add new downloaded page from here
            repository.addQuoteInDB(result)
            Toast.makeText(context, "Added to Favourites", Toast.LENGTH_SHORT).show()
        } else if (mode == 1) {
            Toast.makeText(context, "Cannot create in Favourites", Toast.LENGTH_SHORT).show()
        } else {
            //To auto-regenerate PrimaryKey put its value=0
            repository.addQuoteInDiary(MyQuote(0, result.content, result.author))
        }
        return true
    }

    private suspend fun addNewPageInOnlineMode():Boolean {
        //Will add new downloaded page from here
        Toast.makeText(context, "Addition of New Page", Toast.LENGTH_SHORT).show()
        if (!downloadedPage.contains(pageNumber + 1)) {
            val job = repository.getQuotesByPage(pageNumber + 1)
            if(job)return true
        }
        return false
    }

    //Testing Delete function
    suspend fun delete(): Boolean {
        var result = getCurrentQuote()
        if (result.primaryId == -1) return false
        if (mode == 0) {
            Toast.makeText(context, "Deleting from Internet", Toast.LENGTH_SHORT).show()
            return false
        } else if (mode == 1) {
            repository.deleteFromDB(result)
            if (quotesFromDatabaseList!!.size > 1) {
                if (pointersArray[1] == 0) {
                    pointersArray[1]
                } else {
                    --pointersArray[1]
                }
            }
        } else if (mode == 2) {
//            repository.deleteFromDiary(result)
            if (quotesFromDiaryList!!.size > 1) {
                if (pointersArray[2] == 0) {
                    pointersArray[2]
                } else {
                    --pointersArray[2]
                }
            }
        }
        return true
    }

    //Setting Different Modes
    fun setLiveMode(): Boolean {
        if (mode == 0) return false
        mode = 0
        setLiveQuote(getCurrentQuote())
        return true
    }

    fun setFavouritesMode(): Boolean {
        if (mode == 1) return false
        mode = 1
        setFavouriteQuote(getCurrentQuote())
        return true
    }

    fun setDiaryMode(): Boolean {
        if (mode == 2) return false
        mode = 2
        setDiaryQuote(getCurrentQuote())
        return true
    }

    //It can clear quotes from Favourites and Dairy only
    suspend fun clearQuotes(): Boolean {
        if (getCurrentQuote().primaryId == -1) return false
        if (mode == 0) {
            Toast.makeText(context, "Clearing Online data", Toast.LENGTH_SHORT).show()
            return false
        } else if (mode == 1) {
            pointersArray[1] = 0
            repository.clearDB()
        } else if (mode == 2) {
            pointersArray[2] = 0
            repository.clearDiary()
        }
        return true
    }

    //NEXT and PREV button functionalities

    //Result with -1 id means (No Add and Remove operation will be performed in current set Quote)
    suspend fun nextQuote(): Boolean {  //returns true only if new list added in Online mode
        if (mode == 0) {
            if (quotesFromInternetList?.isEmpty() == false) {
                if (pointersArray[0] + 1 < quotesFromInternetList.size) {
                    pointersArray[0]++
                    refreshCurrentQuote()
                } else {
                    //Try to Add new Quotes Page in Online mode
                    val job=addNewPageInOnlineMode()
                    if(job){
                        pointersArray[0]++
                        return true
                    }
                }
            }else if(NetworkUtils.isInternetAvailable(context)){    //If earlier Internet was inactive but now its active
                val job=addNewPageInOnlineMode()
                if(job){
                    return true
                }
            }
        } else if (mode == 1) {
            if (quotesFromDatabaseList?.isEmpty() == false) {
                if (pointersArray[1] + 1 < quotesFromDatabaseList!!.size) {
                    pointersArray[1]++
                    refreshCurrentQuote()
                }
            }
        } else if (mode == 2) {
            if (quotesFromDiaryList?.isEmpty() == false) {
                if (pointersArray[2] + 1 < quotesFromDiaryList!!.size) {
                    pointersArray[2]++
                    refreshCurrentQuote()
                }
            }
        }
        return false
    }

    //Result with -1 id means (No Add and Remove operation will be performed in current set Quote)
    fun prevQuote(): Boolean {
        if (mode == 0) {
            if (quotesFromInternetList?.isEmpty() == false) {
                if (pointersArray[0] > 0) {
                    pointersArray[0]--
                    refreshCurrentQuote()
                }
            }
        } else if (mode == 1) {
            if (quotesFromDatabaseList?.isEmpty() == false) {
                if (pointersArray[1] > 0) {
                    pointersArray[1]--
                    refreshCurrentQuote()
                }
            }
        } else if (mode == 2) {
            if (quotesFromDiaryList?.isEmpty() == false) {
                if (pointersArray[2] > 0) {
                    pointersArray[2]--
                    refreshCurrentQuote()
                }
            }
        }
        return false
    }

    //GetCurrent Quote functionalities for All modes
    fun getCurrentQuote(): Result {
        if (mode == 0) {
            if (quotesFromInternetList?.isEmpty() == false) {
                return quotesFromInternetList[pointersArray[0]]
            } else {
                return Result(-1, "", "~Device", "", "Device not connected to Internet", "", "", 1)
            }
        } else if (mode == 1) {
            if (quotesFromDatabaseList?.isEmpty() == false) {
                return quotesFromDatabaseList!![pointersArray[1]]
            } else {
                return Result(-1, "", "~Favourites", "", "No Favourite Quote Available", "", "", 1)
            }
        } else if (mode == 2) {
            if (quotesFromDiaryList?.isEmpty() == false) {
                var myQuote = quotesFromDiaryList!![pointersArray[2]]
                return Result(myQuote.id, "", myQuote.author, "", myQuote.text, "", "", 1)
            } else {
                return Result(-1, "", "~Diary", "", "No Diary Quote Available", "", "", 1)
            }
        }
        return Result(-1, "", "~Device", "", "Internal Code Error", "", "", 1)
    }

    //Setting Live Quotes/Results for each mode ............ And also setters for these data
    private var LiveQuoteMutable = MutableLiveData<Result>()
    private var FavouriteQuoteMutable = MutableLiveData<Result>()
    private var DiaryQuoteMutable = MutableLiveData<Result>()

    val LiveQuote: LiveData<Result>
        get() = LiveQuoteMutable

    val FavouriteQuote: LiveData<Result>
        get() = FavouriteQuoteMutable

    val DiaryQuote: LiveData<Result>
        get() = DiaryQuoteMutable

    private fun setLiveQuote(result: Result) {
        LiveQuoteMutable.postValue(result)
    }

    private fun setFavouriteQuote(result: Result) {
        FavouriteQuoteMutable.postValue(result)
    }

    private fun setDiaryQuote(result: Result) {
        DiaryQuoteMutable.postValue(result)
    }

    //Setting Refresh function for current Quote
    fun refreshCurrentQuote() {
        if (mode == 0)
            setLiveQuote(getCurrentQuote())
        else if (mode == 1)
            setFavouriteQuote(getCurrentQuote())
        else if (mode == 2)
            setDiaryQuote(getCurrentQuote())
    }
}