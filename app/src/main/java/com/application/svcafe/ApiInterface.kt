package com.application.svcafe

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("api/json/v1/1/filter.php")
    fun getDrinks(@Query("a") alcoholContent: String = "Non_Alcoholic"): Call<Drinks>
}
