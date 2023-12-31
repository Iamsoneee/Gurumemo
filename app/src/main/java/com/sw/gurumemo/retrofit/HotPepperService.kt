package com.sw.gurumemo.retrofit

import com.sw.gurumemo.Constants
import retrofit2.http.GET
import retrofit2.http.Query

interface HotPepperService {
    @GET("gourmet/v1/")
    suspend fun getGourmetData(
        @Query("key") apiKey: String = Constants.HOTPEPPER_API_KEY,
        @Query("id") id: String? = null,
        @Query("lat") lat: String? = null,
        @Query("lng") lng: String? = null,
        @Query("name_any") nameAny: String? = null,  // 店舗名称の一部
        @Query("name_kana") nameKana: String? = null,  // 店舗名称の一部
        @Query("address") address: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("start") start: Int = 1,  // 検索スタート位置
        @Query("count") count: Int = 30,  // ページあたりの結果数
        @Query("range") range: Int? = 5,  // 検索範囲
        @Query("genre") genre: String? = null,  // ジャンル別
        @Query("large_area") largeArea: String? = null, // 例）東京
        @Query("middle_area") middleArea: String? = null, // 例）銀座・有楽町・新橋・築地・月島
        @Query("small_area") smallArea: String? = null, // 例）新橋
        @Query("order") order: Int = 1, // 初期値は店名かな順。位置から検索を行った場合は距離順
        @Query("format") format: String = "json"
    ): HotPepperResponse
}