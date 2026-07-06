package com.mediatheque.bdtracker.data.remote

import com.mediatheque.bdtracker.BuildConfig
import com.mediatheque.bdtracker.data.remote.model.GoogleBooksResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API publique Google Books. Fonctionne sans clé (quota bas, partagé par IP),
 * ou avec une clé API personnelle (quota individuel bien plus large) — voir README.
 * Documentation : https://developers.google.com/books/docs/v1/using
 */
interface GoogleBooksApi {

    @GET("volumes")
    suspend fun rechercherVolumes(
        @Query("q") requete: String,
        @Query("startIndex") indexDeDepart: Int = 0,
        @Query("maxResults") maxResultats: Int = 40,
        @Query("langRestrict") langue: String = "fr",
        @Query("key") cleApi: String? = BuildConfig.GOOGLE_BOOKS_API_KEY.ifBlank { null }
    ): GoogleBooksResponse

    companion object {
        private const val BASE_URL = "https://www.googleapis.com/books/v1/"

        fun creer(): GoogleBooksApi {
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
                .create(GoogleBooksApi::class.java)
        }
    }
}
