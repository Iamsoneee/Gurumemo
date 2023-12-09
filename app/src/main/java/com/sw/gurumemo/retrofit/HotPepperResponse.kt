package com.sw.gurumemo.retrofit

import android.os.Parcelable
import java.io.Serializable

data class HotPepperResponse(
    val results: Results
) : Serializable

data class Results(
    val shop: List<Shop>,
    val start: Int,
    val count: Int
) : Serializable

data class Shop(
    val id: String,
    val name: String,
    val logo_image: String?,
    val name_kana: String,
    val address: String,
    val keyword: String,
    val budget: Budget,
    val genre: Genre,
    val sub_genre: SubGenre,
    val catch: String,
    val access: String,
    val mobile_access: String,
    val photo: Photo,
    val open: String?,
    val lat: Double?,
    val lng: Double?,
    val urls: Urls,
    val small_area: SmallArea,
    val station_name: String
) : Serializable

data class Genre(
    val name: String?,
    val catch: String,
    val code: String
) : Serializable

data class SubGenre(
    val name: String?,
): Serializable

data class Budget(
    val name: String,
) : Serializable

data class Photo(
    val pc: PC,
) : Serializable

data class PC(
    val l: String
) : Serializable

data class Urls(
    val pc: String,
) : Serializable

data class SmallArea(
    val name: String
):Serializable
