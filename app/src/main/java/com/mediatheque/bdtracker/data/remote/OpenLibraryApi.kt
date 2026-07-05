package com.mediatheque.bdtracker.data.remote

import com.mediatheque.bdtracker.data.remote.model.OpenLibrarySearchResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API publique et gratuite Open Library (aucune clé requise).
 * Documentation : https://openlibrary.org/dev/docs/api/search
 */
interface OpenLibraryApi {

    @GET("search.json")
    suspend fun rechercherLivres(
        @Query("q") requete: String,
        @Query("language") langue: String = "fre",
        @Query("limit") limite: Int = 20
    ): OpenLibrarySearchResponse

    companion object {
        private const val BASE_URL = "https://openlibrary.org/"

        fun creer(): OpenLibraryApi {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenLibraryApi::class.java)
        }
    }
}
