package com.sw.gurumemo.retrofit

import retrofit2.http.GET
import retrofit2.http.Query

interface HotPepperService {
    @GET("gourmet/v1/")
    suspend fun getGourmetData(
        @Query("key") apiKey: String,
        @Query("lat") lat: String = "35.68111",
        @Query("lng") lng: String = "139.76667",
//        @Query("name") name: String? = null,
//        @Query("name_kana") nameKana: String? = null,
//        @Query("name_any") nameAny: String? = null,  // 店舗名称の一部
//        @Query("address") address: String? = null,
//        @Query("keyword") keyword: String? = null,  // 検索キーワード
//        @Query("start") start: Int? = 1,  // 検索スタート位置
        @Query("count") count: Int? = 5,  // ページあたりの結果数
//        @Query("sub_genre") subGenre: String? = null,
        @Query("format") format: String = "json"
    ): HotPepperResponse
}