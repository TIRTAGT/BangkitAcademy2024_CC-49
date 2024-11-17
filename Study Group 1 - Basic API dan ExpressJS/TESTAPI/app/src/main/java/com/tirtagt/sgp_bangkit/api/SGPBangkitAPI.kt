package com.tirtagt.sgp_bangkit.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SGPBangkitAPIEndpoint {
    @GET("/item")
    suspend fun getAll(@Header("Authorization") token: String): SGPBangkitAPI_getAllResponse

    @POST("/item")
    suspend fun addItem(@Header("Authorization") token: String, @Body item: SGPBangkitItem)

    @DELETE("/item/{id}")
    suspend fun deleteById(@Header("Authorization") token: String, @Path("id") id: Int)
}

data class SGPBangkitAPI_getAllResponse(
    val data: ArrayList<SGPBangkitItem>
)

data class SGPBangkitItem(
    val id: Int,
    val nama: String
)

class SGPBangkitAPI {
    companion object {
        fun createApiInstance(): SGPBangkitAPIEndpoint {
            val gsonConverter = GsonConverterFactory.create()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://sgpbangkit.matthew.tirtagt.xyz/")
                .addConverterFactory(gsonConverter)
                .build()

            return retrofit.create(SGPBangkitAPIEndpoint::class.java)
        }
    }
}