package com.example.quotify.view_models

import android.content.Context
import android.util.Log
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
    private lateinit var quotesFromDatabase: LiveData<List<Result>>
    private lateinit var quotesFromInternet: LiveData<QuoteList>

    init {
        quotesFromInternet = repository.getQuotesFromInternetLiveData()
        quotesFromDatabase = repository.getQuotesFromDatabaseLiveData()
        quotesFromDiary = repository.getQuotesFromDiaryLiveData()
        switchModes()       //Setting online mode
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
    }

    fun setquotesFromDatabaseList(list: List<Result>) {
        quotesFromDatabaseList = list
    }

    fun setquotesFromDiaryList(list: List<MyQuote>) {
        quotesFromDiaryList = list
    }

    //It Keeps Information of Pages Downloaded from Internet (Cache)
    private var pageNumber = 0
    private var downloadedPage: HashSet<Int> = HashSet()
    private var pointersArray = IntArray(3)

    //Adding logic for additions of quotes in databases in various modes
    fun addQuote(result: Result) {
        if (mode == 0) {
            //Will add new downloaded page from here
            Toast.makeText(context, "Addition in Internet", Toast.LENGTH_SHORT).show()
        } else if (mode == 1) {
            repository.addQuoteInDB(result)
        } else {
            //To auto-regenerate PrimaryKey put its value=0
            repository.addQuoteInDiary(MyQuote(0, "Consistency in Diary", result.author))
        }
    }

    private fun addNewPageInOnlineMode() {
        //Will add new downloaded page from here
        Toast.makeText(context, "Addition of New Page", Toast.LENGTH_SHORT).show()
        if (!downloadedPage.contains(pageNumber + 1)) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.getQuotesByPage(pageNumber + 1)
            }
        }
    }

    //Testing Delete function
    fun delete(result: Result) {
        if(result.primaryId==-1)return
        if (mode == 0) {
            Toast.makeText(context, "Deleting from Internet", Toast.LENGTH_SHORT).show()
        } else if (mode == 1) {
            repository.deleteFromDB(result)
            if(quotesFromDatabaseList!!.size-1==pointersArray[1])
                pointersArray[1]--
            if(pointersArray[1]<0)pointersArray[1]++
        } else if (mode == 2) {
            repository.deleteFromDiary(result)
            if(quotesFromDiaryList!!.size-1==pointersArray[2])
                pointersArray[2]--
            if(pointersArray[2]<0)pointersArray[2]++
        }
    }

    fun switchModes(): String {
        mode = (mode + 1) % totalModes
        if (mode == 0) {    //online mode
            if (NetworkUtils.isInternetAvailable(context)) {
                Toast.makeText(context, "${getCurrentQuote().toString()}", Toast.LENGTH_LONG).show()
                if (quotesFromInternetList != null)
                    return quotesFromInternetList.toString()
                else return "Nope"
            } else {
                return switchModes()
            }
        } else if (mode == 1) {  //offline mode
            Toast.makeText(context, "${getCurrentQuote().toString()}", Toast.LENGTH_SHORT).show()
            return quotesFromDatabaseList.toString()
        } else if (mode == 2) {  //diary mode
            Toast.makeText(context, "${getCurrentQuote().toString()}", Toast.LENGTH_SHORT).show()
            return quotesFromDiaryList.toString()
        }
        return ""
    }

    //It can clear quotes from Favourites and Dairy only
    fun clearQuotes() {
        if (mode == 0) {
            Toast.makeText(context, "Clearing Online data", Toast.LENGTH_SHORT).show()
        } else if (mode == 1) {
            pointersArray[1]=0
            repository.clearDB()
        } else if (mode == 2)
            pointersArray[2]=0
            repository.clearDiary()
    }

    //NEXT and PREV button functionalities
    //Result with -1 id means (No Add and Remove operation will be performed in current set Quote)
    fun prevQuote(): Result {
        if (mode == 0) {
            if (quotesFromInternetList?.isEmpty() == false) {
                if (pointersArray[0] > 0) {
                    pointersArray[0]--
                    return quotesFromInternetList[pointersArray[0]]
                } else {
                    //Do nothing, already first quote is set
                }
            } else {
                return Result(-1, "", "~Device", "", "Device not connected to Internet", "", "", 1)
            }
        } else if (mode == 1) {
            if (quotesFromDatabaseList?.isEmpty() == false) {
                if (pointersArray[1] > 0) {
                    pointersArray[1]--
                    return quotesFromDatabaseList!![pointersArray[1]]
                } else {
                    //Do nothing, already first quote is set
                }
            } else {
                return Result(-1, "", "~Favourites", "", "No Favourite Quote Available", "", "", 1)
            }
        } else if (mode == 2) {
            if (quotesFromDiaryList?.isEmpty() == false) {
                if (pointersArray[2] > 0) {
                    pointersArray[2]--
                    var myQuote = quotesFromDiaryList!![pointersArray[2]]
                    return Result(myQuote.id, "", myQuote.author, "", myQuote.text, "", "", 1)
                } else {
                    //Do nothing, already first quote is set
                }
            } else {
                return Result(-1, "", "~Diary", "", "No Diary Quote Available", "", "", 1)
            }
        }
        return getCurrentQuote()
    }

    //Result with -1 id means (No Add and Remove operation will be performed in current set Quote)
    fun nextQuote(): Result {
        if (mode == 0) {
            if (quotesFromInternetList?.isEmpty() == false) {
                if (pointersArray[0] + 1 < quotesFromInternetList.size) {
                    pointersArray[0]++
                    return quotesFromInternetList[pointersArray[0]]
                } else {
                    //Try to Add new Quote in Online mode
                    val oldsize=downloadedPage.size
                    addNewPageInOnlineMode()
                    if(downloadedPage.size>oldsize){
                        return nextQuote()
                    }
                }
            } else {
                return Result(-1, "", "~Device", "", "Device not connected to Internet", "", "", 1)
            }
        } else if (mode == 1) {
            if (quotesFromDatabaseList?.isEmpty() == false) {
                if (pointersArray[1] + 1 < quotesFromDatabaseList!!.size) {
                    pointersArray[1]++
                    return quotesFromDatabaseList!![pointersArray[1]]
                } else {
                    //Do nothing, already last quote is set
                }
            } else {
                return Result(-1, "", "~Favourites", "", "No Favourite Quote Available", "", "", 1)
            }
        } else if (mode == 2) {
            if (quotesFromDiaryList?.isEmpty() == false) {
                if (pointersArray[2] + 1 < quotesFromDiaryList!!.size) {
                    pointersArray[2]++
                    var myQuote = quotesFromDiaryList!![pointersArray[2]]
                    return Result(myQuote.id, "", myQuote.author, "", myQuote.text, "", "", 1)
                } else {
                    //Do nothing, already last quote is set
                }
            } else {
                return Result(-1, "", "~Diary", "", "No Diary Quote Available", "", "", 1)
            }
        }
        return getCurrentQuote()
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

}