package com.example.scalaapp

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class ParametriRequest(
    val korisnikId: String,
    val tezina: Double,
    val visina: Double,
    val tlakSys: Int,
    val tlakDia: Int
)
data class Namirnica(
    val naziv: String,
    val kalorije: Int
)
data class DanasnjaPrehranaResponse(
    @SerializedName("ukupnoKcal") val ukupnoKcal: Int,
    @SerializedName("namirnice") val namirnice: List<Namirnica>
)
data class PrehranaRequest(
    val korisnikId: String,
    val ukupnoKcal: Int,
    val namirnice: List<Namirnica>
)
data class ParametriResponse(
    val id: Int,
    val tezina: Double,
    val visina: Double,
    @SerializedName("tlak_sys") val tlakSys: Int,
    @SerializedName("tlak_dia") val tlakDia: Int,
    @SerializedName("zabiljezeno_u") val datum: String
)
data class Upozorenje(
    val poruka: String,
    val datum: String
)
data class AktivnostRequest(
    val korisnikId: String,
    val naziv: String,
    val trajanje: Int,
    val kalorije: Int
)
data class Biljeska(
    val id: Int? = null,
    val naslov: String,
    val sadrzaj: String,
    val datum: String? = null,
    val korisnikId: String? = null
)

interface ScalaApiService {
    @POST("api/Health/parametri")
    suspend fun unesiParametre(@Body podaci: ParametriRequest): Map<String, Any>

    @POST("api/Health/prehrana")
    suspend fun unesiPrehranu(@Body podaci: PrehranaRequest): Map<String, Any>

    @GET("api/Health/parametri")
    suspend fun dohvatiParametre(): List<ParametriResponse>

    @GET("api/Health/prehrana/danas")
    suspend fun dohvatiDanasnjuPrehranu(@retrofit2.http.Query("korisnikId") id: String): DanasnjaPrehranaResponse

    @GET("api/Health/upozorenja")
    suspend fun dohvatiUpozorenja(@retrofit2.http.Query("korisnikId") id: String): List<Upozorenje>

    @POST("api/Health/aktivnost")
    suspend fun unesiAktivnost(@Body podaci: AktivnostRequest): Map<String, Any>

    @GET("api/Health/aktivnost/danas")
    suspend fun dohvatiDanasnjeAktivnosti(@retrofit2.http.Query("korisnikId") id: String): List<AktivnostRequest>

    @GET("api/Health/biljeske")
    suspend fun dohvatiBiljeske(@retrofit2.http.Query("korisnikId") id: String): List<Biljeska>

    @POST("api/Health/biljeske")
    suspend fun unesiBiljesku(@Body biljeska: Biljeska): Map<String, Any>
}

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:5059/"

    val api: ScalaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScalaApiService::class.java)
    }
}