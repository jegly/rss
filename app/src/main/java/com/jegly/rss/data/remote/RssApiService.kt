package com.jegly.rss.data.remote
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface RssApiService {
    @GET
    suspend fun fetchFeedXml(@Url url: String): ResponseBody
}
