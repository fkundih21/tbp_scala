package com.example.scalaapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class ParametriRequest(
    val korisnikId: String,
    val tezina: Double,
    val visina: Double,
    val tlakSys: Int,
    val tlakDia: Int
)

interface ScalaApiService {
    @POST("api/Health/parametri")
    suspend fun unesiParametre(@Body podaci: ParametriRequest): Map<String, Any>
}
object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:5203/"

    val api: ScalaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScalaApiService::class.java)
    }
}