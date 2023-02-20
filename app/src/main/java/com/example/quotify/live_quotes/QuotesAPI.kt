package com.example.quotify.live_quotes

import com.example.quotify.models.QuoteList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface QuotesAPI {

    // we are getting full json data containing array of (Result) quotes.

    @GET("/quotes")
    suspend fun getQuotesbyPage(@Query("page") page:Int):Response<QuoteList>
}